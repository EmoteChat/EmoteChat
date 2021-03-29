package de.emotechat.addon;

import com.google.gson.reflect.TypeToken;
import de.emotechat.addon.adapter.EmoteChatAdapter;
import de.emotechat.addon.adapter.mappings.Mappings;
import de.emotechat.addon.asm.chat.sending.SendMessageHandler;
import de.emotechat.addon.bttv.BTTVEmote;
import de.emotechat.addon.bttv.BTTVSearch;
import de.emotechat.addon.bttv.EmoteProvider;
import de.emotechat.addon.gui.chat.UserInputHandler;
import de.emotechat.addon.gui.chat.menu.ChatShortcut;
import de.emotechat.addon.gui.chat.render.ChatWidthCalculator;
import de.emotechat.addon.gui.chat.suggestion.EmoteSuggestionsMenu;
import de.emotechat.addon.gui.element.ModifiableBooleanElement;
import de.emotechat.addon.gui.element.PreviewedDropDownElement;
import de.emotechat.addon.gui.element.button.ButtonElement;
import de.emotechat.addon.gui.emote.EmoteDropDownMenu;
import de.emotechat.addon.gui.emote.EmoteListContainerElement;
import de.emotechat.addon.listener.ChatInjectListener;
import de.emotechat.addon.listener.ChatSendListener;
import de.emotechat.addon.listener.MinecraftTickExecutor;
import net.labymod.api.LabyModAddon;
import net.labymod.ingamechat.GuiChatCustom;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.BooleanElement;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.ListContainerElement;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.settings.elements.StringElement;
import net.labymod.utils.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class EmoteChatAddon extends LabyModAddon {

    public static final Type SAVED_EMOTES_TYPE_TOKEN = new TypeToken<Map<String, BTTVEmote>>() {
    }.getType();

    private static final String ADAPTER_CLASS_BASE = "de.emotechat.addon.adapter.%s.%sEmoteChatAdapter";

    private final MinecraftTickExecutor minecraftTickExecutor = new MinecraftTickExecutor();

    private EmoteProvider emoteProvider;

    private EmoteChatAdapter emoteChatAdapter;

    private boolean enabled;

    private Map<String, BTTVEmote> savedEmotes = new HashMap<>();

    private final Collection<EmoteListContainerElement> emoteLists = new ArrayList<>();

    private GuiButton addGuiButton;

    private long emoteErrorRenderStart;

    @Override
    public void onEnable() {
        this.loadEmoteChatAdapter();

        super.getApi().registerForgeListener(this.minecraftTickExecutor);
        super.getApi().registerForgeListener(new ChatInjectListener(this));

        EmoteSuggestionsMenu emoteSuggestionsMenu = new EmoteSuggestionsMenu(this);
        super.getApi().registerForgeListener(emoteSuggestionsMenu);
        UserInputHandler.addMouseListener(emoteSuggestionsMenu);
        UserInputHandler.addKeyListener(emoteSuggestionsMenu);
        GuiChatCustom.getModuleGui().getKeyTypeListeners().add(emoteSuggestionsMenu);

        SendMessageHandler.setChatModifier(new ChatSendListener(this));

        ChatShortcut.initListener(this);
    }

    private void loadEmoteChatAdapter() {
        String versionDiscriminator = Mappings.ACTIVE_MAPPINGS.name();

        try {
            Class<?> emoteChatAdapterClass = Class.forName(String.format(
                    ADAPTER_CLASS_BASE,
                    versionDiscriminator.toLowerCase(),
                    versionDiscriminator
            ));

            this.emoteChatAdapter = (EmoteChatAdapter) emoteChatAdapterClass.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void loadConfig() {
        this.enabled = !super.getConfig().has("enabled") || super.getConfig().get("enabled").getAsBoolean();

        String backendServerURL = super.getConfig().has("backendServerURL")
                ? super.getConfig().get("backendServerURL").getAsString()
                : "https://api.emotechat.de";
        super.getConfig().addProperty("backendServerURL", backendServerURL);

        this.savedEmotes = super.getConfig().has("savedEmotes")
                ? Constants.GSON.fromJson(super.getConfig().get("savedEmotes"), SAVED_EMOTES_TYPE_TOKEN)
                : new HashMap<>();

        if (this.emoteProvider != null) {
            this.emoteProvider.close();
        }

        this.emoteProvider = new EmoteProvider(this, backendServerURL, this.savedEmotes, this::updateEmotes);
        this.emoteProvider.retrieveEmotesFromServer(this.savedEmotes.values());
        ChatWidthCalculator.setEmoteProvider(this.emoteProvider);

        super.saveConfig();
    }

    public void updateEmotes() {
        for (EmoteListContainerElement emoteList : this.emoteLists) {
            emoteList.update(this.savedEmotes);
        }

        super.getConfig().add("savedEmotes", Constants.GSON.toJsonTree(this.savedEmotes));
        super.saveConfig();
    }

    @Override
    protected void fillSettings(List<SettingsElement> list) {
        BooleanElement toggleEnabledElement = new ModifiableBooleanElement(
                "Enabled", this,
                new ControlElement.IconData(Material.REDSTONE_COMPARATOR), "enabled",
                () -> this.enabled
        );
        toggleEnabledElement.addCallback(enabled -> this.enabled = enabled);
        list.add(toggleEnabledElement);

        EmoteListContainerElement emoteList = new EmoteListContainerElement("Saved emotes", new ControlElement.IconData(Material.CHEST), this);
        emoteList.update(this.savedEmotes);

        ButtonElement cleanupButton = new ButtonElement("Clear emote cache");
        cleanupButton.setClickListener(this.emoteProvider::clearCache);

        list.add(emoteList);
        list.add(this.createEmoteAddMenu());
        list.add(cleanupButton);

        this.emoteLists.add(emoteList);
    }

    @Override
    public void onRenderPreview(int mouseX, int mouseY, float partialTicks) {
        if (this.emoteErrorRenderStart == -1) {
            return;
        }

        String secondLine = "It might be banned or you are sending too many requests.";

        LabyMod.getInstance().getDrawUtils().drawHoveringText(
                (Minecraft.getMinecraft().currentScreen.width / 2)
                        - (LabyMod.getInstance().getDrawUtils().getStringWidth(secondLine) / 2)
                        - 5,
                this.emoteChatAdapter.getButtonY(this.addGuiButton) + 35,
                "Error while adding the emote!",
                secondLine);

        if ((System.currentTimeMillis() - this.emoteErrorRenderStart) > 3000) {
            this.emoteErrorRenderStart = -1;
        }
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

            this.getEmoteProvider().addEmote(selectedEmote, emoteName, success -> {
                if (success) {
                    emoteAddButton.setText("Override");
                } else {
                    this.emoteErrorRenderStart = System.currentTimeMillis();
                }
            });
        });

        this.addGuiButton = emoteAddButton.getGuiButton();

        StringElement emoteNameInput = new StringElement("Set emote name", new ControlElement.IconData(Material.PAPER), "", emoteName -> {
            emoteAddButton.setText(this.savedEmotes.containsKey(emoteName.toLowerCase()) ? "Override" : "Save emote");
            emoteNameReference.set(emoteName);
        });

        emoteAddMenu.getSubSettings().addAll(new ArrayList<>(Arrays.asList(
                searchBarElement,
                new PreviewedDropDownElement("Results", searchResultList,
                        () -> searchResultList.getHoverSelected() != null
                                ? searchResultList.getHoverSelected()
                                : searchResultList.getSelected()),
                emoteNameInput,
                emoteAddButton
        )));

        return emoteAddMenu;
    }

    public EmoteChatAdapter getEmoteChatAdapter() {
        return emoteChatAdapter;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
}
