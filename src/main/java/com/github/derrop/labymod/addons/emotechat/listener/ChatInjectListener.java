package com.github.derrop.labymod.addons.emotechat.listener;

import com.github.derrop.labymod.addons.emotechat.EmoteChatAddon;
import com.github.derrop.labymod.addons.emotechat.gui.chatrender.EmoteChatRendererMain;
import com.github.derrop.labymod.addons.emotechat.gui.chatrender.EmoteChatRendererSecond;
import net.labymod.core_implementation.mc18.gui.GuiChatAdapter;
import net.labymod.core_implementation.mc18.gui.GuiIngameCustom;
import net.labymod.ingamechat.IngameChatManager;
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

    private static final Field CHAT_MANAGER_MAIN_CHAT_FIELD;
    private static final Field CHAT_MANAGER_SECOND_CHAT_FIELD;
    private static final Field CHAT_MANAGER_CHAT_RENDERERS_FIELD;

    static {
        Field mainChat = null;
        Field secondChat = null;
        Field chatRenderers = null;

        Field chatManagerMainChat = null;
        Field chatManagerSecondChat = null;
        Field chatManagerChatRenderers = null;

        try {
            mainChat = GuiChatAdapter.class.getDeclaredField("chatMain");
            mainChat.setAccessible(true);

            secondChat = GuiChatAdapter.class.getDeclaredField("chatSecond");
            secondChat.setAccessible(true);

            chatRenderers = GuiChatAdapter.class.getDeclaredField("chatRenderers");
            chatRenderers.setAccessible(true);

            chatManagerMainChat = IngameChatManager.class.getDeclaredField("main");
            chatManagerMainChat.setAccessible(true);

            chatManagerSecondChat = IngameChatManager.class.getDeclaredField("second");
            chatManagerSecondChat.setAccessible(true);

            chatManagerChatRenderers = IngameChatManager.class.getDeclaredField("chatRenderers");
            chatManagerChatRenderers.setAccessible(true);
        } catch (NoSuchFieldException exception) {
            System.err.println("Cannot initialize emote chat gui:");
            exception.printStackTrace();
        }

        MAIN_CHAT_FIELD = mainChat;
        SECOND_CHAT_FIELD = secondChat;
        CHAT_RENDERERS_FIELD = chatRenderers;

        CHAT_MANAGER_MAIN_CHAT_FIELD = chatManagerMainChat;
        CHAT_MANAGER_SECOND_CHAT_FIELD = chatManagerSecondChat;
        CHAT_MANAGER_CHAT_RENDERERS_FIELD = chatManagerChatRenderers;
    }

    private final EmoteChatAddon addon;

    private boolean injected;

    public ChatInjectListener(EmoteChatAddon addon) {
        this.addon = addon;
    }

    @SubscribeEvent
    public void handleGameOverlayRender(RenderGameOverlayEvent event) {
        GuiIngame ingameGUI = Minecraft.getMinecraft().ingameGUI;
        if (this.injected) {
            return;
        }

        if (ingameGUI instanceof GuiIngameCustom && ingameGUI.getChatGUI() instanceof GuiChatAdapter) {
            GuiChatAdapter adapter = (GuiChatAdapter) ingameGUI.getChatGUI();

            IngameChatManager ingameChatManager = LabyMod.getInstance().getIngameChatManager();

            ChatRenderer main = new EmoteChatRendererMain(ingameChatManager, this.addon, ingameChatManager.getMain().getChatLines());
            ChatRenderer second = new EmoteChatRendererSecond(ingameChatManager, this.addon, ingameChatManager.getSecond().getChatLines());

            ChatRenderer[] chatRenderers = new ChatRenderer[]{main, second};

            try {
                MAIN_CHAT_FIELD.set(adapter, main);
                SECOND_CHAT_FIELD.set(adapter, second);
                CHAT_RENDERERS_FIELD.set(adapter, chatRenderers);

                CHAT_MANAGER_MAIN_CHAT_FIELD.set(ingameChatManager, main);
                CHAT_MANAGER_SECOND_CHAT_FIELD.set(ingameChatManager, second);
                CHAT_MANAGER_CHAT_RENDERERS_FIELD.set(ingameChatManager, chatRenderers);
            } catch (IllegalAccessException exception) {
                exception.printStackTrace();
            }

            this.injected = true;
        }
    }

}
