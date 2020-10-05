package de.emotechat.addon.gui.emote;

import de.emotechat.addon.EmoteChatAddon;
import de.emotechat.addon.bttv.BTTVEmote;
import de.emotechat.addon.gui.element.button.ButtonElement;
import net.labymod.settings.elements.ListContainerElement;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

public class EmoteListContainerElement extends ListContainerElement {

    private final EmoteChatAddon addon;

    public EmoteListContainerElement(String displayName, IconData iconData, EmoteChatAddon addon) {
        super(displayName, iconData);
        this.addon = addon;
    }

    public void update(Map<String, BTTVEmote> emotes) {
        super.subSettings.getElements().clear();

        super.subSettings.addAll(new ArrayList<>(
                emotes.values().stream()
                        .map(emote -> {
                            ButtonElement emoteButton = new ButtonElement(emote.getName(), emote.asIconData(), "Remove");

                            emoteButton.setClickListener(() -> {
                                this.addon.removeEmote(emote);
                                Minecraft.getMinecraft().currentScreen.initGui();
                            });

                            return emoteButton;
                        })
                        .collect(Collectors.toList())
        ));
    }

}
