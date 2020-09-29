package com.github.derrop.labymod.addons.emotechat.listener;

import com.github.derrop.labymod.addons.emotechat.Constants;
import com.github.derrop.labymod.addons.emotechat.EmoteChatAddon;
import com.github.derrop.labymod.addons.emotechat.asm.packet.ChatModifier;
import com.github.derrop.labymod.addons.emotechat.bttv.BTTVEmote;
import com.github.derrop.labymod.addons.emotechat.bttv.BackendEmoteInfo;
import net.minecraft.client.Minecraft;

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
                        String emoteId = emote == null ? emoteName : BackendEmoteInfo.retrieveInfoByBTTVId(emote.getId()).getGlobalIdentifier();

                        return Constants.EMOTE_WRAPPER + emoteId + Constants.EMOTE_WRAPPER;
                    }

                    return word;
                })
                .collect(Collectors.joining(" "));
    }

    @Override
    public boolean shouldReplace(String message) {
        return this.addon.isEnabled() && Minecraft.getMinecraft().thePlayer != null;
    }
}
