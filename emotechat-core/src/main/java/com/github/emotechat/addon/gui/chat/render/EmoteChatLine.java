package com.github.emotechat.addon.gui.chat.render;

import com.github.emotechat.addon.gui.ChatLineEntry;
import net.labymod.ingamechat.renderer.ChatLine;

import java.util.Collection;

public class EmoteChatLine extends ChatLine {

    private final Collection<ChatLineEntry> entries;

    private final boolean render;

    public EmoteChatLine(Collection<ChatLineEntry> entries, boolean render, String message, boolean secondChat, String room, Object component, int updateCounter, int chatLineId, Integer highlightColor) {
        super(message, secondChat, room, component, updateCounter, chatLineId, highlightColor);
        this.entries = entries;
        this.render = render;
    }

    public Collection<ChatLineEntry> getEntries() {
        return this.entries;
    }

    public boolean shouldRender() {
        return this.render;
    }
}
