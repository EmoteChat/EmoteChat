package com.github.derrop.labymod.addons.emotechat.gui.chatrender;

import com.github.derrop.labymod.addons.emotechat.EmoteChatAddon;
import net.labymod.ingamechat.IngameChatManager;
import net.labymod.ingamechat.renderer.types.ChatRendererSecond;

public class EmoteChatRendererSecond extends ChatRendererSecond implements EmoteChatRendererType {

    private final EmoteChatRenderer renderer;

    public EmoteChatRendererSecond(IngameChatManager manager, EmoteChatAddon addon) {
        super(manager);
        this.renderer = new EmoteChatRenderer(this, manager, addon);
    }

    @Override
    public void renderChat(int updateCounter) {
        this.renderer.renderChat(updateCounter);
    }

    @Override
    public void renderDefault(int updateCounter) {
        super.renderChat(updateCounter);
    }

    @Override
    public float getChatPositionY() {
        return this.renderer.getChatPositionY();
    }

    @Override
    public boolean renderHoveringResizeX(boolean forceRender) {
        return this.renderer.renderHoveringResizeX(forceRender);
    }

    @Override
    public boolean renderHoveringResizeY(boolean forceRender) {
        return this.renderer.renderHoveringResizeY(forceRender);
    }

}
