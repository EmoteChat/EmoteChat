package com.github.derrop.labymod.addons.emotechat;

import com.github.derrop.labymod.addons.emotechat.asm.packet.PacketHandler;
import com.github.derrop.labymod.addons.emotechat.bttv.BTTVEmote;
import com.github.derrop.labymod.addons.emotechat.bttv.BTTVSearch;
import com.github.derrop.labymod.addons.emotechat.gui.chat.tabcomplete.TabCompleteConsumer;
import com.github.derrop.labymod.addons.emotechat.gui.element.ButtonElement;
import com.github.derrop.labymod.addons.emotechat.gui.emote.EmoteDropDownMenu;
import com.github.derrop.labymod.addons.emotechat.gui.emote.EmoteListContainerElement;
import com.github.derrop.labymod.addons.emotechat.listener.ChatInjectListener;
import com.github.derrop.labymod.addons.emotechat.listener.ChatSendListener;
import com.github.derrop.labymod.addons.emotechat.listener.MinecraftTickExecutor;
import com.google.gson.reflect.TypeToken;
import net.labymod.api.LabyModAddon;
import net.labymod.ingamechat.GuiChatCustom;
import net.labymod.settings.elements.*;
import net.labymod.utils.Material;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

// TODO: Line width is calculated with the text, but an emote is only Constants.CHAT_EMOTE_SIZE in width
public class EmoteChatAddon extends LabyModAddon {

    public static final Type SAVED_EMOTES_TYPE_TOKEN = new TypeToken<Map<String, BTTVEmote>>() {
    }.getType();

    private final MinecraftTickExecutor minecraftTickExecutor = new MinecraftTickExecutor();

    private boolean enabled;

    private Map<String, BTTVEmote> savedEmotes = new HashMap<>();

    private final Collection<EmoteListContainerElement> emoteLists = new ArrayList<>();

    @Override
    public void onEnable() {
        super.getApi().registerForgeListener(this.minecraftTickExecutor);
        super.getApi().registerForgeListener(new ChatInjectListener(this));

        TabCompleteConsumer tabCompleteConsumer = new TabCompleteConsumer(this);
        super.getApi().registerForgeListener(tabCompleteConsumer);
        GuiChatCustom.getModuleGui().getKeyTypeListeners().add(tabCompleteConsumer);
        GuiChatCustom.getModuleGui().getMouseClickListeners().add(tabCompleteConsumer);

        PacketHandler.setChatModifier(new ChatSendListener(this));
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

        this.savedEmotes = super.getConfig().has("savedEmotes")
                ? Constants.GSON.fromJson(super.getConfig().get("savedEmotes"), SAVED_EMOTES_TYPE_TOKEN)
                : new HashMap<>();
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

        EmoteListContainerElement emoteList = new EmoteListContainerElement("Saved emotes", new ControlElement.IconData(Material.CHEST));
        emoteList.update(this.savedEmotes);

        list.add(emoteList);
        list.add(this.createEmoteAddMenu());

        this.emoteLists.add(emoteList);
    }

    public boolean addEmote(BTTVEmote emote, String name) {
        if (emote == null || name.isEmpty() || name.contains(" ")) {
            return false;
        }

        BTTVEmote userEmote = new BTTVEmote(emote.getId(), name, emote.getImageType());

        this.savedEmotes.put(userEmote.getName().toLowerCase(), userEmote);

        for (EmoteListContainerElement emoteList : this.emoteLists) {
            emoteList.update(this.savedEmotes);
        }

        super.getConfig().add("savedEmotes", Constants.GSON.toJsonTree(this.savedEmotes));
        super.saveConfig();

        return true;
    }

    private ListContainerElement createEmoteAddMenu() {
        ListContainerElement emoteAddMenu = new ListContainerElement("Add emote", new ControlElement.IconData(Material.NETHER_STAR));
        EmoteDropDownMenu searchResultList = new EmoteDropDownMenu("Results");

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
                new DropDownElement<BTTVEmote>("Results", searchResultList),
                emoteNameInput,
                emoteAddButton
        )));

        return emoteAddMenu;
    }

    public Map<String, BTTVEmote> getSavedEmotes() {
        return savedEmotes;
    }

}
