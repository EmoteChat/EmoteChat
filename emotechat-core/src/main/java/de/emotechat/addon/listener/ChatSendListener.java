package de.emotechat.addon.listener;

import de.emotechat.addon.Constants;
import de.emotechat.addon.EmoteChatAddon;
import de.emotechat.addon.asm.packet.ChatModifier;
import de.emotechat.addon.bttv.BTTVEmote;
import net.labymod.core.LabyModCore;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ChatSendListener implements ChatModifier {

    private final EmoteChatAddon addon;

    public ChatSendListener(EmoteChatAddon addon) {
        this.addon = addon;
    }

    @Override
    public String replaceMessage(String message) {
        return Arrays.stream(message.split(" "))
                .map(word -> {
                    if (word.length() > 2 && word.charAt(0) == Constants.EMOTE_WRAPPER && word.charAt(word.length() - 1) == Constants.EMOTE_WRAPPER) {
                        String emoteName = word.substring(1, word.length() - 1);

                        BTTVEmote emote = this.addon.getEmoteByName(emoteName);

                        String globalIdentifier = emoteName;

                        if (emote != null) {
                            String id = emote.getId();
                            int idLength = id.length();

                            globalIdentifier = emote.getOriginalName() + "+" + id.substring(idLength - 5, idLength);
                        }

                        return Constants.EMOTE_WRAPPER + globalIdentifier + Constants.EMOTE_WRAPPER;
                    }

                    return word;
                })
                .collect(Collectors.joining(" "));
    }

    @Override
    public boolean shouldReplace(String message) {
        return this.addon.isEnabled() && LabyModCore.getMinecraft().getPlayer() != null;
    }

}
