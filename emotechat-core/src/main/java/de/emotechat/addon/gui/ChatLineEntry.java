package de.emotechat.addon.gui;

import de.emotechat.addon.Constants;
import de.emotechat.addon.bttv.BTTVGlobalId;
import net.minecraft.client.gui.FontRenderer;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChatLineEntry {

    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");

    private final boolean emote;

    private final String content;

    private final BTTVGlobalId emoteId;

    private final String colors;

    private boolean loadedEmote = false;

    public ChatLineEntry(BTTVGlobalId emoteId, String content, String colors) {
        this.emote = emoteId != null;
        this.emoteId = emoteId;
        this.content = content;
        this.colors = colors;
    }

    public boolean isEmote() {
        return this.emote;
    }

    public String getContent() {
        return this.content;
    }

    public BTTVGlobalId getEmoteId() {
        return this.emoteId;
    }

    public String getColors() {
        return this.colors;
    }

    public boolean isLoadedEmote() {
        return loadedEmote;
    }

    public void setLoadedEmote(boolean loadedEmote) {
        this.loadedEmote = loadedEmote;
    }

    public static Collection<ChatLineEntry> parseEntries(String line) {
        StringBuilder currentLine = new StringBuilder();

        return Arrays.stream(line.split(" ")).map(word -> {
            String strippedWord = STRIP_COLOR_PATTERN.matcher(word).replaceAll("");
            BTTVGlobalId emoteId = BTTVGlobalId.parse(strippedWord);

            String colors = FontRenderer.getFormatFromString(currentLine.toString());
            currentLine.append(word);

            return new ChatLineEntry(emoteId, word, colors);
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
