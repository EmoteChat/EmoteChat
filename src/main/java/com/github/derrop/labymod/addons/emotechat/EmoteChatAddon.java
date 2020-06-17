package com.github.derrop.labymod.addons.emotechat;

import com.github.derrop.labymod.addons.emotechat.bttv.BTTVEmote;
import com.github.derrop.labymod.addons.emotechat.bttv.BTTVSearch;
import com.github.derrop.labymod.addons.emotechat.gui.EmoteList;
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

    private static final Type SAVED_EMOTES_TYPE_TOKEN = new TypeToken<Map<String, BTTVEmote>>() {
    }.getType();

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(1);

    private boolean enabled;

    private Map<String, BTTVEmote> savedEmotes;

    @Override
    public void onEnable() {
        if (this.enabled) {
            // TODO: register listeners
        }
    }

    @Override
    public void loadConfig() {
        this.enabled = !super.getConfig().has("enabled") || super.getConfig().get("enabled").getAsBoolean();

        this.savedEmotes = super.getConfig().has("savedEmotes")
                ? GSON.fromJson(super.getConfig().get("savedEmotes"), SAVED_EMOTES_TYPE_TOKEN)
                : new HashMap<>();
    }

    @Override
    protected void fillSettings(List<SettingsElement> list) {
        list.add(new BooleanElement("Enabled", this, new ControlElement.IconData(Material.REDSTONE_COMPARATOR), "enabled", true));

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
                        searchResultList.update(new BTTVSearch.Builder(input).build().execute());
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
                BTTVEmote userEmote = new BTTVEmote(selectedEmote.getId(), input);
                this.savedEmotes.put(selectedEmote.getId(), userEmote);

                savedEmoteList.update(this.savedEmotes.values());

                super.getConfig().add("savedEmotes", GSON.toJsonTree(this.savedEmotes));
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
