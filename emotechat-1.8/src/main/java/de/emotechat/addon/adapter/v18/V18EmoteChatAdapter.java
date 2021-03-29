package de.emotechat.addon.adapter.v18;


import de.emotechat.addon.adapter.EmoteChatAdapter;
import net.labymod.core_implementation.mc18.gui.GuiChatAdapter;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraftforge.client.event.GuiOpenEvent;

public class V18EmoteChatAdapter implements EmoteChatAdapter {

    @Override
    public GuiScreen getGui(GuiOpenEvent event) {
        return event.gui;
    }

    @Override
    public Class<?> getGuiChatAdapterClass() {
        return GuiChatAdapter.class;
    }

    @Override
    public boolean isLabyModChat(GuiIngame guiIngame) {
        return guiIngame.getChatGUI() instanceof GuiChatAdapter;
    }

    @Override
    public Class<?> getChatPacketClass() {
        return C01PacketChatMessage.class;
    }

    @Override
    public String getChatPacketMessage(Packet<?> packet) {
        return packet instanceof C01PacketChatMessage ? ((C01PacketChatMessage) packet).getMessage() : null;
    }

    @Override
    public int getButtonX(GuiButton button) {
        return button.xPosition;
    }

    @Override
    public int getButtonY(GuiButton button) {
        return button.yPosition;
    }
}
