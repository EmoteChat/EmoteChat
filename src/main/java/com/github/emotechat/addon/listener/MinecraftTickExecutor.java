package com.github.emotechat.addon.listener;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class MinecraftTickExecutor {

    private Runnable task;

    @SubscribeEvent
    public void handleTick(TickEvent.ClientTickEvent event) {
        if (this.task != null) {
            this.task.run();
            this.task = null;
        }
    }

    public void setTask(Runnable task) {
        this.task = task;
    }

}
