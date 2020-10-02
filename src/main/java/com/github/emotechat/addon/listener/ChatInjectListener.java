package com.github.emotechat.addon.listener;

import com.github.emotechat.addon.EmoteChatAddon;
import com.github.emotechat.addon.gui.chat.render.EmoteChatRendererMain;
import com.github.emotechat.addon.gui.chat.render.EmoteChatRendererSecond;
import com.github.emotechat.addon.gui.chat.settings.ChatShortcut;
import net.labymod.core_implementation.mc18.gui.GuiChatAdapter;
import net.labymod.core_implementation.mc18.gui.GuiIngameCustom;
import net.labymod.ingamechat.GuiChatCustom;
import net.labymod.ingamechat.IngameChatManager;
import net.labymod.ingamechat.renderer.ChatRenderer;
import net.labymod.main.LabyMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiIngame;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

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

    private EmoteChatRendererMain main;

    private EmoteChatRendererSecond second;

    private boolean injected;

    public ChatInjectListener(EmoteChatAddon addon) {
        this.addon = addon;
    }

    @SubscribeEvent
    public void handleGameOverlayRender(RenderGameOverlayEvent event) {
        if (Minecraft.getMinecraft().currentScreen instanceof GuiChatCustom) {
            GuiChatCustom customChat = (GuiChatCustom) Minecraft.getMinecraft().currentScreen;
            try {
                if (ChatShortcut.shouldInitialize(customChat)) {
                    ChatShortcut.init(customChat);
                }
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException exception) {
                exception.printStackTrace();
            }
        }

        GuiIngame ingameGUI = Minecraft.getMinecraft().ingameGUI;
        if (this.injected) {
            return;
        }

        if (ingameGUI instanceof GuiIngameCustom && ingameGUI.getChatGUI() instanceof GuiChatAdapter) {
            GuiChatAdapter adapter = (GuiChatAdapter) ingameGUI.getChatGUI();

            IngameChatManager ingameChatManager = LabyMod.getInstance().getIngameChatManager();

            this.main = new EmoteChatRendererMain(ingameChatManager, this.addon, ingameChatManager.getMain().getChatLines());
            this.second = new EmoteChatRendererSecond(ingameChatManager, this.addon, ingameChatManager.getSecond().getChatLines());

            ChatRenderer[] chatRenderers = new ChatRenderer[]{this.main, this.second};

            try {
                MAIN_CHAT_FIELD.set(adapter, this.main);
                SECOND_CHAT_FIELD.set(adapter, this.second);
                CHAT_RENDERERS_FIELD.set(adapter, chatRenderers);

                CHAT_MANAGER_MAIN_CHAT_FIELD.set(ingameChatManager, this.main);
                CHAT_MANAGER_SECOND_CHAT_FIELD.set(ingameChatManager, this.second);
                CHAT_MANAGER_CHAT_RENDERERS_FIELD.set(ingameChatManager, chatRenderers);
            } catch (IllegalAccessException exception) {
                exception.printStackTrace();
            }

            this.injected = true;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void handleChatOpen(GuiOpenEvent event) {
        if (this.main == null) {
            return;
        }
        if (!(event.gui instanceof GuiChat)) {
            return;
        }

        Collection<String> newEmoteIds = this.addon.getNewEmoteIds();
        if (!newEmoteIds.isEmpty()) {
            this.addon.getEmoteProvider().sendEmotesToServer(newEmoteIds);
        }

        GuiChat chat = (GuiChat) event.gui;

        this.main.setLastGuiChat(chat);
        this.second.setLastGuiChat(chat);
    }

}
