package com.github.emotechat.addon.gui;

import com.github.emotechat.addon.Constants;
import net.minecraft.client.gui.FontRenderer;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChatLineEntry {

    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");

    private static final Pattern EMOTE_PATTERN = Pattern.compile(Constants.EMOTE_WRAPPER + "[A-Za-z0-9:]+\\+[A-Za-z0-9]{5}" + Constants.EMOTE_WRAPPER);

    private static final char COLOR_CHAR = 167;

    private final boolean emote;

    private final String content;

    private final String rawContent;

    private final String colors;

    public ChatLineEntry(boolean emote, String content, String rawContent, String colors) {
        this.emote = emote;
        this.content = content;
        this.rawContent = rawContent;
        this.colors = colors;
    }

    public boolean isEmote() {
        return this.emote;
    }

    public String getContent() {
        return this.content;
    }

    public String getColors() {
        return this.colors;
    }

    // TODO: spaces are not displayed with fat (§l) strings
    // TODO: spaces are missing after an emote that doesn't exist
    public static Collection<ChatLineEntry> parseEntries(String line) {
        StringBuilder currentLine = new StringBuilder();

        return Arrays.stream(line.split(" ")).map(word -> {
            String strippedWord = STRIP_COLOR_PATTERN.matcher(word).replaceAll("");
            boolean emote = EMOTE_PATTERN.matcher(strippedWord).matches();

            String colors = FontRenderer.getFormatFromString(currentLine.toString());
            currentLine.append(word);

            return new ChatLineEntry(emote, (emote ? strippedWord.substring(1, strippedWord.length() - 1) : word), word, colors);
        }).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "ChatLineEntry{" +
                "emote=" + emote +
                ", content='" + content + '\'' +
                '}';
    }
}
