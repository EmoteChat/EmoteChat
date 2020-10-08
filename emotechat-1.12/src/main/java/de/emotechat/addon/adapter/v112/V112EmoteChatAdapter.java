package de.emotechat.addon.adapter.v112;


import de.emotechat.addon.adapter.EmoteChatAdapter;
import net.labymod.core_implementation.mc112.gui.GuiChatAdapter;
import net.labymod.core_implementation.mc112.gui.GuiIngameCustom;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
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
        return guiIngame instanceof GuiIngameCustom && guiIngame.getChatGUI() instanceof GuiChatAdapter;
    }

    @Override
    public int getX(GuiTextField textField) {
        return textField.x;
    }

    @Override
    public int getY(GuiTextField textField) {
        return textField.y;
    }

    @Override
    public Class<?> getChatPacketClass() {
        return CPacketChatMessage.class;
    }

    @Override
    public String getChatPacketMessage(Packet<?> packet) {
        if (packet instanceof CPacketChatMessage) {
            return ((CPacketChatMessage) packet).getMessage();
        }
        return null;
    }

}
