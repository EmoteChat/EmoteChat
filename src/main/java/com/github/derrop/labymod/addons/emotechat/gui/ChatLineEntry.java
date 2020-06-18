package com.github.derrop.labymod.addons.emotechat.gui;

import com.github.derrop.labymod.addons.emotechat.Constants;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChatLineEntry {

    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");

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
        return Arrays.stream(line.split(" "))
                .map(word -> {
                    String strippedWord = STRIP_COLOR_PATTERN.matcher(word).replaceAll("");
                    boolean emote = strippedWord.startsWith(Constants.EMOTE_WRAPPER) && strippedWord.endsWith(Constants.EMOTE_WRAPPER);

                    return new ChatLineEntry(emote, (emote ? strippedWord.substring(1, strippedWord.length() - 1) : word));
                })
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "ChatLineEntry{" +
                "emote=" + emote +
                ", content='" + content + '\'' +
                '}';
    }
}
