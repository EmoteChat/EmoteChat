package com.github.derrop.labymod.addons.emotechat.gui;

import com.github.derrop.labymod.addons.emotechat.Constants;

import java.util.ArrayList;
import java.util.Collection;

public class ChatLineEntry {

    private final boolean emote;

    private final String content;

    public ChatLineEntry(boolean emote, String content) {
        this.emote = emote;
        this.content = content;
    }

    public boolean isEmote() {
        return this.emote;
    }

    public String getContent() {
        return this.content;
    }

    public static Collection<ChatLineEntry> parseEntries(String line) {
        Collection<ChatLineEntry> entries = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();

        char[] chars = line.toCharArray();
        boolean inEmote = false;
        for (char c : chars) {
            if (c == Constants.EMOTE_WRAPPER) {
                if (currentLine.length() != 0) {
                    // TODO: use the colors from the last entry that wasn't an emote
                    entries.add(new ChatLineEntry(inEmote, currentLine.toString()));
                    currentLine.setLength(0);
                }
                inEmote = !inEmote;
                continue;
            }

            currentLine.append(c);
        }

        if (currentLine.length() != 0) {
            entries.add(new ChatLineEntry(false, currentLine.toString()));
        }

        return entries;
    }

    @Override
    public String toString() {
        return "ChatLineEntry{" +
                "emote=" + emote +
                ", content='" + content + '\'' +
                '}';
    }
}
