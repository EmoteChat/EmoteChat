package com.github.derrop.labymod.addons.emotechat.gui;

import com.github.derrop.labymod.addons.emotechat.Constants;
import com.github.derrop.labymod.addons.emotechat.bttv.BTTVEmote;
import net.labymod.settings.elements.ControlElement;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

public class ChatLineEntry {

    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");
    private static final char COLOR_CHAR = 167;

    private final boolean emote;

    private final String content;
    private final String rawContent;
    private String colors = "";

    public ChatLineEntry(boolean emote, String content, String rawContent) {
        this.emote = emote;
        this.content = content;
        this.rawContent = rawContent;
    }

    public BTTVEmote getAsEmote() {
        return BTTVEmote.getById(this.content);
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

    // TODO: still wrong colors
    // TODO: spaces are not displayed with fat (§l) strings
    public static Collection<ChatLineEntry> parseEntries(String line) {
        if (line.endsWith("§r")) {
            line = line.substring(0, line.length() - 2);
        }

        ChatLineEntry[] entries = Arrays.stream(line.split(" ")).map(word -> {
            String strippedWord = STRIP_COLOR_PATTERN.matcher(word).replaceAll("");
            boolean emote = strippedWord.length() > (Constants.EMOTE_WRAPPER.length() * 2)
                    && strippedWord.startsWith(Constants.EMOTE_WRAPPER) && strippedWord.endsWith(Constants.EMOTE_WRAPPER);

            return new ChatLineEntry(emote, (emote ? strippedWord.substring(1, strippedWord.length() - 1) : word), word);
        }).toArray(ChatLineEntry[]::new);

        for (int i = 0; i < entries.length; i++) {
            if (i != 0) {
                for (int j = i; j >= 0; j--) {
                    String colors = getLastColors(entries[j].rawContent);
                    if (!colors.isEmpty()) {
                        entries[i].colors = colors;
                        break;
                    }
                }

            }
        }

        return Arrays.asList(entries);
    }

    public static String getLastColors(String input) {
        StringBuilder result = new StringBuilder();
        int length = input.length();

        for (int index = length - 1; index >= 0; index--) {
            char section = input.charAt(index);
            if (section == COLOR_CHAR && index < length - 1) {
                char c = input.charAt(index + 1);

                if ((c >= 48 && c <= 57) || (c >= 97 && c <= 102) || c == 114) { // color/reset
                    result.insert(0, COLOR_CHAR + "" + c);
                    break;
                } else if (c >= 107 && c <= 111) { // formatting
                    result.insert(0, COLOR_CHAR + "" + c);
                }
            }
        }

        return result.toString();
    }

    @Override
    public String toString() {
        return "ChatLineEntry{" +
                "emote=" + emote +
                ", content='" + content + '\'' +
                '}';
    }
}
