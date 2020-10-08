package de.emotechat.addon.adapter.v18;


import de.emotechat.addon.adapter.EmoteChatAdapter;
import net.labymod.core_implementation.mc18.gui.GuiChatAdapter;
import net.labymod.core_implementation.mc18.gui.GuiIngameCustom;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
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
        return guiIngame instanceof GuiIngameCustom && guiIngame.getChatGUI() instanceof GuiChatAdapter;
    }

    @Override
    public int getX(GuiTextField textField) {
        return textField.xPosition;
    }

    @Override
    public int getY(GuiTextField textField) {
        return textField.yPosition;
    }

    @Override
    public Class<?> getChatPacketClass() {
        return C01PacketChatMessage.class;
    }

    @Override
    public String getChatPacketMessage(Packet<?> packet) {
        if (packet instanceof C01PacketChatMessage) {
            ((C01PacketChatMessage) packet).getMessage();
        }

        return null;
    }

}
