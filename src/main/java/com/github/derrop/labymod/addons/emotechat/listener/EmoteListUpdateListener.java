package com.github.derrop.labymod.addons.emotechat.listener;

import com.github.derrop.labymod.addons.emotechat.EmoteChatAddon;
import com.github.derrop.labymod.addons.emotechat.QueuedEmoteUpdate;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class EmoteListUpdateListener {

    private final EmoteChatAddon addon;

    public EmoteListUpdateListener(EmoteChatAddon addon) {
        this.addon = addon;
    }

    @SubscribeEvent
    public void handleTick(TickEvent.ClientTickEvent event) {
        if (this.addon.getEmoteUpdateQueue().isEmpty()) {
            return;
        }

        while (!this.addon.getEmoteUpdateQueue().isEmpty()) {
            QueuedEmoteUpdate update = this.addon.getEmoteUpdateQueue().poll();
            if (update != null) {
                update.apply();
            }
        }
    }

}
