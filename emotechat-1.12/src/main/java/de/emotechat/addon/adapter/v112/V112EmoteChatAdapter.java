package de.emotechat.addon.adapter.v112;


import de.emotechat.addon.adapter.EmoteChatAdapter;
import net.labymod.core_implementation.mc112.gui.GuiChatAdapter;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.client.event.GuiOpenEvent;

public class V112EmoteChatAdapter implements EmoteChatAdapter {

    @Override
    public GuiScreen getGui(GuiOpenEvent event) {
        return event.getGui();
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
        return CPacketChatMessage.class;
    }

    @Override
    public String getChatPacketMessage(Packet<?> packet) {
        return packet instanceof CPacketChatMessage ? ((CPacketChatMessage) packet).getMessage() : null;
    }

    @Override
    public int getButtonX(GuiButton button) {
        return button.x;
    }

    @Override
    public int getButtonY(GuiButton button) {
        return button.y;
    }
}
