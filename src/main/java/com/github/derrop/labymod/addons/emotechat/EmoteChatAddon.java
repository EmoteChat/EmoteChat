package com.github.derrop.labymod.addons.emotechat;

import com.github.derrop.labymod.addons.emotechat.bttv.BTTVEmote;
import com.github.derrop.labymod.addons.emotechat.bttv.BTTVSearch;
import com.github.derrop.labymod.addons.emotechat.gui.EmoteList;
import com.github.derrop.labymod.addons.emotechat.listener.ChatInjectListener;
import com.github.derrop.labymod.addons.emotechat.listener.ChatSendListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.labymod.api.LabyModAddon;
import net.labymod.settings.elements.*;
import net.labymod.utils.Material;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EmoteChatAddon extends LabyModAddon {

    public static final Gson GSON = new Gson();

    private static final Type SAVED_EMOTES_TYPE_TOKEN = new TypeToken<Collection<BTTVEmote>>() {
    }.getType();

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(1);

    private boolean enabled;

    private final Map<String, BTTVEmote> savedEmotes = new HashMap<>();

    @Override
    public void onEnable() {
        super.getApi().registerForgeListener(new ChatInjectListener(this));
        super.getApi().getEventManager().register(new ChatSendListener(this));
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public BTTVEmote getEmoteByName(String name) {
        return this.savedEmotes.get(name.toLowerCase());
    }

    @Override
    public void loadConfig() {
        this.enabled = !super.getConfig().has("enabled") || super.getConfig().get("enabled").getAsBoolean();

        Collection<BTTVEmote> emotes = super.getConfig().has("savedEmotes")
                ? GSON.fromJson(super.getConfig().get("savedEmotes"), SAVED_EMOTES_TYPE_TOKEN)
                : Collections.emptyList();

        this.savedEmotes.clear();
        for (BTTVEmote emote : emotes) {
            this.savedEmotes.put(emote.getName().toLowerCase(), emote);
        }
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

        EmoteList savedEmoteList = new EmoteList("Saved emotes");
        savedEmoteList.update(this.savedEmotes.values());
        savedEmoteList.setEmotesSelectable(false);

        list.add(new DropDownElement<BTTVEmote>("Saved emotes", savedEmoteList));
        list.add(this.createEmoteAddMenu(savedEmoteList));
    }

    private ListContainerElement createEmoteAddMenu(EmoteList savedEmoteList) {
        ListContainerElement emoteAddMenu = new ListContainerElement("Add emote", new ControlElement.IconData(Material.NETHER_STAR));
        EmoteList searchResultList = new EmoteList("Results");

        StringElement searchBarElement = new StringElement("Search for emotes on BTTV", new ControlElement.IconData(Material.ITEM_FRAME), "", input -> {
            if (input.length() > 2) {
                EXECUTOR_SERVICE.execute(() -> {
                    try {
                        searchResultList.update(new BTTVSearch.Builder(input).build().execute()); // TODO: call the update method sync
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    }
                });
            } else {
                searchResultList.clear();
            }
        });

        StringElement emoteNameInput = new StringElement("Set emote name", new ControlElement.IconData(Material.PAPER), "", input -> {
            BTTVEmote selectedEmote = searchResultList.getSelected();

            if (!input.isEmpty() && selectedEmote != null) {
                // TODO: the name may not contain Constants.EMOTE_WRAPPER
                BTTVEmote userEmote = new BTTVEmote(selectedEmote.getId(), input);

                this.savedEmotes.values().removeIf(value -> value.getId().equals(selectedEmote.getId()));

                this.savedEmotes.put(selectedEmote.getName().toLowerCase(), userEmote);
                savedEmoteList.update(this.savedEmotes.values());

                super.getConfig().add("savedEmotes", GSON.toJsonTree(this.savedEmotes.values()));
                super.saveConfig();
            }
        });

        emoteAddMenu.getSubSettings().addAll(new ArrayList<>(Arrays.asList(
                searchBarElement,
                new DropDownElement<BTTVEmote>("Results", searchResultList),
                emoteNameInput
        )));

        return emoteAddMenu;
    }

}
