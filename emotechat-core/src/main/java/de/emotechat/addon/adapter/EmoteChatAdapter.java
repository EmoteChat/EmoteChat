package de.emotechat.addon.adapter;


import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.network.Packet;
import net.minecraftforge.client.event.GuiOpenEvent;

public interface EmoteChatAdapter {

    GuiScreen getGui(GuiOpenEvent event);

    Class<?> getGuiChatAdapterClass();

    boolean isLabyModChat(GuiIngame guiIngame);

    Class<?> getChatPacketClass();

    String getChatPacketMessage(Packet<?> packet);

    int getButtonX(GuiButton button);

    int getButtonY(GuiButton button);
}
