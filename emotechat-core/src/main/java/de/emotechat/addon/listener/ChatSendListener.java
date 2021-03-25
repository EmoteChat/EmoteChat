package de.emotechat.addon.listener;

import de.emotechat.addon.Constants;
import de.emotechat.addon.EmoteChatAddon;
import de.emotechat.addon.asm.packet.ChatModifier;
import de.emotechat.addon.bttv.BTTVEmote;
import net.labymod.core.LabyModCore;

public class ChatSendListener implements ChatModifier {

    private final EmoteChatAddon addon;

    public ChatSendListener(EmoteChatAddon addon) {
        this.addon = addon;
    }

    @Override
    public String replaceMessage(String message) {
        String[] words = message.split(" ");

        for (int i = 0; i < words.length; i++) {
            String word = words[i];

            if (word.length() > 2 && word.charAt(0) == Constants.EMOTE_WRAPPER && word.charAt(word.length() - 1) == Constants.EMOTE_WRAPPER) {
                String emoteName = word.substring(1, word.length() - 1);
                BTTVEmote emote = this.addon.getEmoteProvider().getEmoteByName(emoteName);

                if (emote != null && emote.getGlobalId() != null) {
                    words[i] = emote.getGlobalId().toString(this.addon.getEmoteProvider().getIdSplitter());
                }
            }
        }

        return String.join(" ", words);
    }

    @Override
    public boolean shouldReplace(String message) {
        return this.addon.isEnabled() && LabyModCore.getMinecraft().getPlayer() != null;
    }

}
