package com.github.emotechat.addon;

import com.github.emotechat.addon.asm.packet.PacketHandler;
import com.github.emotechat.addon.bttv.BTTVEmote;
import com.github.emotechat.addon.bttv.BTTVSearch;
import com.github.emotechat.addon.bttv.EmoteProvider;
import com.github.emotechat.addon.gui.chat.settings.ChatShortcut;
import com.github.emotechat.addon.gui.chat.suggestion.EmoteSuggestionsMenu;
import com.github.emotechat.addon.gui.chat.suggestion.KeyTypedHandler;
import com.github.emotechat.addon.gui.element.PreviewedDropDownElement;
import com.github.emotechat.addon.gui.element.button.ButtonElement;
import com.github.emotechat.addon.gui.element.button.TimedButtonElement;
import com.github.emotechat.addon.gui.emote.EmoteDropDownMenu;
import com.github.emotechat.addon.gui.emote.EmoteListContainerElement;
import com.github.emotechat.addon.listener.ChatInjectListener;
import com.github.emotechat.addon.listener.ChatSendListener;
import com.github.emotechat.addon.listener.MinecraftTickExecutor;
import com.google.gson.reflect.TypeToken;
import net.labymod.api.LabyModAddon;
import net.labymod.ingamechat.GuiChatCustom;
import net.labymod.settings.elements.*;
import net.labymod.utils.Material;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class EmoteChatAddon extends LabyModAddon {

    public static final Type SAVED_EMOTES_TYPE_TOKEN = new TypeToken<Map<String, BTTVEmote>>() {
    }.getType();

    private final MinecraftTickExecutor minecraftTickExecutor = new MinecraftTickExecutor();

    private EmoteProvider emoteProvider;

    private boolean enabled;

    private Map<String, BTTVEmote> savedEmotes = new HashMap<>();

    private final Collection<EmoteListContainerElement> emoteLists = new ArrayList<>();

    private final Collection<String> newEmoteIds = new HashSet<>();

    @Override
    public void onEnable() {
        super.getApi().registerForgeListener(this.minecraftTickExecutor);
        super.getApi().registerForgeListener(new ChatInjectListener(this));

        EmoteSuggestionsMenu emoteSuggestionsMenu = new EmoteSuggestionsMenu(this);
        super.getApi().registerForgeListener(emoteSuggestionsMenu);
        GuiChatCustom.getModuleGui().getKeyTypeListeners().add(emoteSuggestionsMenu);
        GuiChatCustom.getModuleGui().getMouseClickListeners().add(emoteSuggestionsMenu);

        KeyTypedHandler.setEmoteSuggestionsMenu(emoteSuggestionsMenu);

        PacketHandler.setChatModifier(new ChatSendListener(this));
        ChatShortcut.initListener(this);
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public BTTVEmote getEmoteByName(String name) {
        return this.savedEmotes.get(name.toLowerCase());
    }

    public boolean isEmoteSaved(BTTVEmote emote) {
        return this.savedEmotes.values().stream().anyMatch(saved -> saved.getId().equals(emote.getId()));
    }

    @Override
    public void loadConfig() {
        this.enabled = !super.getConfig().has("enabled") || super.getConfig().get("enabled").getAsBoolean();

        String backendServerURL = super.getConfig().has("backendServerURL")
                ? super.getConfig().get("backendServerURL").getAsString()
                : "";

        this.savedEmotes = super.getConfig().has("savedEmotes")
                ? Constants.GSON.fromJson(super.getConfig().get("savedEmotes"), SAVED_EMOTES_TYPE_TOKEN)
                : new HashMap<>();

        this.emoteProvider = new EmoteProvider(backendServerURL, this.savedEmotes);

        this.emoteProvider.sendEmotesToServer(this.savedEmotes.values().stream().map(BTTVEmote::getId).collect(Collectors.toList()));
    }

    @Override
    protected void fillSettings(List<SettingsElement> list) {
        BooleanElement toggleEnabledElement = new BooleanElement(
                "Enabled", this,
                new ControlElement.IconData(Material.REDSTONE_COMPARATOR), "enabled",
                true
        );
        toggleEnabledElement.addCallback(enabled -> this.enabled = enabled);
        list.add(toggleEnabledElement);

        EmoteListContainerElement emoteList = new EmoteListContainerElement("Saved emotes", new ControlElement.IconData(Material.CHEST), this);
        emoteList.update(this.savedEmotes);

        list.add(emoteList);
        list.add(this.createEmoteAddMenu());

        ButtonElement reloadButton = new TimedButtonElement("Reload emotes", TimeUnit.MINUTES.toMillis(2));
        reloadButton.setClickListener(() -> {
            this.emoteProvider.cleanupCache();
            this.emoteProvider.sendEmotesToServer();
        });
        list.add(reloadButton);

        this.emoteLists.add(emoteList);
    }

    public boolean addEmote(BTTVEmote emote, String name) {
        if (emote == null || name.isEmpty() || name.contains(" ")) {
            return false;
        }

        this.newEmoteIds.add(emote.getId());

        BTTVEmote userEmote = new BTTVEmote(emote.getId(), name, emote.getName(), emote.getImageType());

        this.savedEmotes.put(userEmote.getName().toLowerCase(), userEmote);
        this.updateEmotes();
        return true;
    }

    public void removeEmote(BTTVEmote emote) {
        this.savedEmotes.remove(emote.getName().toLowerCase());
        this.updateEmotes();
    }

    public void updateEmotes() {
        for (EmoteListContainerElement emoteList : this.emoteLists) {
            emoteList.update(this.savedEmotes);
        }

        super.getConfig().add("savedEmotes", Constants.GSON.toJsonTree(this.savedEmotes));
        super.saveConfig();
    }

    private ListContainerElement createEmoteAddMenu() {
        ListContainerElement emoteAddMenu = new ListContainerElement("Add emote", new ControlElement.IconData(Material.NETHER_STAR));
        EmoteDropDownMenu searchResultList = new EmoteDropDownMenu(false, "Results");

        StringElement searchBarElement = new StringElement("Search for emotes on BTTV", new ControlElement.IconData(Material.ITEM_FRAME), "", input -> {
            if (input.length() > 2) {
                Constants.EXECUTOR_SERVICE.execute(() -> {
                    try {
                        List<BTTVEmote> results = new BTTVSearch.Builder(input).build().execute();
                        this.minecraftTickExecutor.setTask(() -> searchResultList.update(results));
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    }
                });
            } else {
                searchResultList.clear();
            }
        });

        AtomicReference<String> emoteNameReference = new AtomicReference<>("");

        ButtonElement emoteAddButton = new ButtonElement("Save emote") {

            @Override
            public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
                super.draw(x, y, maxX, maxY, mouseX, mouseY);

                String emoteName = emoteNameReference.get();
                super.setEnabled(!(searchResultList.getSelected() == null || emoteName.isEmpty() || emoteName.contains(" ")));
            }

        };
        emoteAddButton.setEnabled(false);
        emoteAddButton.setClickListener(() -> {
            String emoteName = emoteNameReference.get();
            BTTVEmote selectedEmote = searchResultList.getSelected();

            if (!this.addEmote(selectedEmote, emoteName)) {
                return;
            }

            emoteAddButton.setText("Override");
        });

        StringElement emoteNameInput = new StringElement("Set emote name", new ControlElement.IconData(Material.PAPER), "", emoteName -> {
            emoteAddButton.setText(this.savedEmotes.containsKey(emoteName.toLowerCase()) ? "Override" : "Save emote");
            emoteNameReference.set(emoteName);
        });

        emoteAddMenu.getSubSettings().addAll(new ArrayList<>(Arrays.asList(
                searchBarElement,
                new PreviewedDropDownElement("Results", searchResultList, searchResultList::getHoverSelected),
                emoteNameInput,
                emoteAddButton
        )));

        return emoteAddMenu;
    }

    public EmoteProvider getEmoteProvider() {
        return emoteProvider;
    }

    public Collection<EmoteListContainerElement> getEmoteLists() {
        return emoteLists;
    }

    public Map<String, BTTVEmote> getSavedEmotes() {
        return savedEmotes;
    }

    public Collection<String> getNewEmoteIds() {
        return newEmoteIds;
    }

}
