package com.github.derrop.labymod.addons.emotechat.gui.emote;


import com.github.derrop.labymod.addons.emotechat.bttv.BTTVEmote;
import com.github.derrop.labymod.addons.emotechat.gui.ButtonElement;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ListContainerElement;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

public class EmoteListContainerElement extends ListContainerElement {

    public EmoteListContainerElement(String displayName, IconData iconData) {
        super(displayName, iconData);
    }

    public void update(Map<String, BTTVEmote> emotes) {
        super.subSettings.getElements().clear();

        super.subSettings.addAll(new ArrayList<>(
                emotes.values().stream()
                        .map(emote -> {
                            ResourceLocation resourceLocation = LabyMod.getInstance().getDynamicTextureManager().getTexture(emote.getId(), emote.getURL(3));
                            Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocation);

                            ButtonElement emoteButton = new ButtonElement(emote.getName(), new IconData(resourceLocation), "Remove");

                            emoteButton.setClickListener(() -> {
                                emotes.remove(emote.getName().toLowerCase());
                                super.subSettings.getElements().remove(emoteButton);
                            });

                            return emoteButton;
                        })
                        .collect(Collectors.toList())
        ));
    }

}
