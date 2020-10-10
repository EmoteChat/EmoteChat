package de.emotechat.addon.adapter;


import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.network.Packet;
import net.minecraftforge.client.event.GuiOpenEvent;

public interface EmoteChatAdapter {

    GuiScreen getGui(GuiOpenEvent event);

    Class<?> getGuiChatAdapterClass();

    boolean isLabyModChat(GuiIngame guiIngame);

    int getX(GuiTextField textField);

    int getY(GuiTextField textField);

    Class<?> getChatPacketClass();

    String getChatPacketMessage(Packet<?> packet);

}
