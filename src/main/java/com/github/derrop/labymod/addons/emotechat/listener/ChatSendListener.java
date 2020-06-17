package com.github.derrop.labymod.addons.emotechat.listener;

import com.github.derrop.labymod.addons.emotechat.Constants;
import com.github.derrop.labymod.addons.emotechat.EmoteChatAddon;
import com.github.derrop.labymod.addons.emotechat.bttv.BTTVEmote;
import net.labymod.api.events.MessageSendEvent;
import net.minecraft.client.Minecraft;

public class ChatSendListener implements MessageSendEvent {

    private final EmoteChatAddon addon;

    public ChatSendListener(EmoteChatAddon addon) {
        this.addon = addon;
    }

    @Override
    public boolean onSend(String message) {
        if (!this.addon.isEnabled() || Minecraft.getMinecraft().thePlayer == null) {
            return false;
        }

        StringBuilder outputMessage = new StringBuilder();
        StringBuilder currentEmote = new StringBuilder();

        char[] input = message.toCharArray();
        boolean inEmote = false;

        for (char c : input) {
            if (c == Constants.EMOTE_WRAPPER) {
                inEmote = !inEmote;

                if (!inEmote) {
                    String emoteName = currentEmote.toString();
                    currentEmote.setLength(0);

                    BTTVEmote emote = this.addon.getEmoteByName(emoteName);
                    String emoteId = emote == null ? emoteName : emote.getId();

                    outputMessage.append(Constants.EMOTE_WRAPPER).append(emoteId).append(Constants.EMOTE_WRAPPER);
                }

                continue;
            }

            if (inEmote) {
                currentEmote.append(c);
                continue;
            }

            outputMessage.append(c);
        }

        outputMessage.append(currentEmote);

        Minecraft.getMinecraft().thePlayer.sendChatMessage(outputMessage.toString());

        return true;
    }
}
