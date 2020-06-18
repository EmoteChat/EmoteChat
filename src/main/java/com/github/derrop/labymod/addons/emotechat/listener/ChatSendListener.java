package com.github.derrop.labymod.addons.emotechat.listener;

import com.github.derrop.labymod.addons.emotechat.Constants;
import com.github.derrop.labymod.addons.emotechat.EmoteChatAddon;
import com.github.derrop.labymod.addons.emotechat.bttv.BTTVEmote;
import net.labymod.api.events.MessageSendEvent;
import net.minecraft.client.Minecraft;

import java.util.Arrays;
import java.util.stream.Collectors;

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

        String outputMessage = Arrays.stream(message.split(" "))
                .map(word -> {
                    if (word.startsWith(Constants.EMOTE_WRAPPER) && word.endsWith(Constants.EMOTE_WRAPPER)) {
                        String emoteName = word.substring(1, word.length() - 1);

                        BTTVEmote emote = this.addon.getEmoteByName(emoteName);
                        String emoteId = emote == null ? emoteName : emote.getId();

                        return Constants.EMOTE_WRAPPER + emoteId + Constants.EMOTE_WRAPPER;
                    }

                    return word;
                })
                .collect(Collectors.joining(" "));

        Minecraft.getMinecraft().thePlayer.sendChatMessage(outputMessage);
        return true;
    }
}
