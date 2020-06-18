package com.github.derrop.labymod.addons.emotechat.listener;

import com.github.derrop.labymod.addons.emotechat.EmoteChatAddon;
import com.github.derrop.labymod.addons.emotechat.gui.chatrender.EmoteChatRendererMain;
import com.github.derrop.labymod.addons.emotechat.gui.chatrender.EmoteChatRendererSecond;
import net.labymod.core_implementation.mc18.gui.GuiChatAdapter;
import net.labymod.core_implementation.mc18.gui.GuiIngameCustom;
import net.labymod.ingamechat.renderer.ChatRenderer;
import net.labymod.main.LabyMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;

public class ChatInjectListener {

    private static final Field MAIN_CHAT_FIELD;
    private static final Field SECOND_CHAT_FIELD;
    private static final Field CHAT_RENDERERS_FIELD;

    static {
        Field mainChat = null;
        Field secondChat = null;
        Field chatRenderers = null;

        try {
            mainChat = GuiChatAdapter.class.getDeclaredField("chatMain");
            mainChat.setAccessible(true);

            secondChat = GuiChatAdapter.class.getDeclaredField("chatSecond");
            secondChat.setAccessible(true);

            chatRenderers = GuiChatAdapter.class.getDeclaredField("chatRenderers");
            chatRenderers.setAccessible(true);

        } catch (NoSuchFieldException exception) {
            System.err.println("Cannot initialize emote chat gui:");
            exception.printStackTrace();
        }

        MAIN_CHAT_FIELD = mainChat;
        SECOND_CHAT_FIELD = secondChat;
        CHAT_RENDERERS_FIELD = chatRenderers;
    }

    private final EmoteChatAddon addon;

    private boolean injected;

    public ChatInjectListener(EmoteChatAddon addon) {
        this.addon = addon;
    }

    @SubscribeEvent
    public void handleGameOverlayRender(RenderGameOverlayEvent event) {
        if (!this.addon.isEnabled()) {
            return;
        }

        GuiIngame ingameGUI = Minecraft.getMinecraft().ingameGUI;
        if (this.injected) {
            return;
        }

        if (ingameGUI instanceof GuiIngameCustom && ingameGUI.getChatGUI() instanceof GuiChatAdapter) {
            GuiChatAdapter adapter = (GuiChatAdapter) ingameGUI.getChatGUI();

            ChatRenderer main = new EmoteChatRendererMain(LabyMod.getInstance().getIngameChatManager(), this.addon);
            ChatRenderer second = new EmoteChatRendererSecond(LabyMod.getInstance().getIngameChatManager(), this.addon);

            try {
                MAIN_CHAT_FIELD.set(adapter, main);
                SECOND_CHAT_FIELD.set(adapter, second);
                CHAT_RENDERERS_FIELD.set(adapter, new ChatRenderer[]{main, second});
            } catch (IllegalAccessException exception) {
                exception.printStackTrace();
            }

            this.injected = true;
        }
    }

}
