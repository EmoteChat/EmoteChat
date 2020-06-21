package com.github.derrop.labymod.addons.emotechat.gui;

import com.github.derrop.labymod.addons.emotechat.Constants;
import com.github.derrop.labymod.addons.emotechat.bttv.BTTVEmote;
import net.labymod.main.LabyMod;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

public class ChatLineEntry {

    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");
    private static final char COLOR_CHAR = 167;

    private final boolean emote;

    private String content;

    public ChatLineEntry(boolean emote, String content) {
        this.emote = emote;
        this.content = content;
    }

    public ResourceLocation getEmoteTexture() {
        BTTVEmote emote = new BTTVEmote(this.content, "");

        return LabyMod.getInstance().getDynamicTextureManager().getTexture(emote.getId(), emote.getURL(3));
    }

    public boolean isEmote() {
        return this.emote;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public static Collection<ChatLineEntry> parseEntries(String line) {
        ChatLineEntry[] entries = Arrays.stream(line.split(" ")).map(word -> {
            String strippedWord = STRIP_COLOR_PATTERN.matcher(word).replaceAll("");
            boolean emote = strippedWord.length() > (Constants.EMOTE_WRAPPER.length() * 2)
                    && strippedWord.startsWith(Constants.EMOTE_WRAPPER) && strippedWord.endsWith(Constants.EMOTE_WRAPPER);

            return new ChatLineEntry(emote, (emote ? strippedWord.substring(1, strippedWord.length() - 1) : word));
        }).toArray(ChatLineEntry[]::new);

        for (int i = 0; i < entries.length; i++) {
            if (i != 0) {
                if (entries[i].emote) {
                    continue;
                }

                for (int j = i - 1; j >= 0; j--) {
                    String colors = getLastColors(entries[j].content);
                    if (!colors.isEmpty()) {
                        entries[i].content = colors + entries[i].content;
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
