package com.github.derrop.labymod.addons.emotechat.listener;

import com.github.derrop.labymod.addons.emotechat.EmoteChatAddon;
import com.github.derrop.labymod.addons.emotechat.gui.chat.EmoteChatGui;
import net.labymod.core_implementation.mc18.gui.GuiIngameCustom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ChatInjectListener {

    private static final Field FIELD;
    private static final Field CUSTOM_FIELD;

    static {
        Field field = null;
        Field customField = null;
        try {
            field = GuiIngame.class.getDeclaredField("l"); // persistantChatGUI
            field.setAccessible(true);

            customField = GuiIngameCustom.class.getDeclaredField("persistantChatGUI");
            customField.setAccessible(true);

            removeFinal(field);
            removeFinal(customField);

        } catch (NoSuchFieldException | IllegalAccessException exception) {
            System.err.println("Cannot initialize emote chat gui:");
            exception.printStackTrace();
        }

        FIELD = field;
        CUSTOM_FIELD = customField;
    }

    private static void removeFinal(Field field) throws IllegalAccessException, NoSuchFieldException {
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }

    private final EmoteChatAddon addon;

    public ChatInjectListener(EmoteChatAddon addon) {
        this.addon = addon;
    }

    @SubscribeEvent
    public void handleGameOverlayRender(RenderGameOverlayEvent event) {
        if (!this.addon.isEnabled()) {
            return;
        }

        GuiIngame ingameGUI = Minecraft.getMinecraft().ingameGUI;
        if (ingameGUI.getChatGUI() instanceof EmoteChatGui) {
            return;
        }

        Field field = ingameGUI instanceof GuiIngameCustom ? CUSTOM_FIELD : FIELD;
        if (field == null) {
            return;
        }

        // TODO: if the ingameGUI is GuiIngameCustom, the chat gui will also be custom (LabyMod's GuiChatAdapter), this should be used because if it isn't, the right chat is no more available

        try {
            field.set(ingameGUI, new EmoteChatGui(this.addon, Minecraft.getMinecraft()));
        } catch (IllegalAccessException exception) {
            exception.printStackTrace();
        }
    }

}
