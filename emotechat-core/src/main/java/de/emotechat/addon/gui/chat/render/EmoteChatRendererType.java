package de.emotechat.addon.gui.chat.render;

import net.minecraft.client.gui.GuiChat;

public interface EmoteChatRendererType {

    void renderDefault(int updateCounter);

    GuiChat getLastChatGui();
}
