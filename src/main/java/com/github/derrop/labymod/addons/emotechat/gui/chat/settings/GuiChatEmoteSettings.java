package com.github.derrop.labymod.addons.emotechat.gui.chat.settings;

import com.github.derrop.labymod.addons.emotechat.EmoteChatAddon;
import net.labymod.ingamechat.GuiChatCustom;

public class GuiChatEmoteSettings extends GuiChatCustom {

    private final EmoteChatAddon addon;

    public GuiChatEmoteSettings(String defaultText, EmoteChatAddon addon) {
        super(defaultText);
        this.addon = addon;
    }
}
