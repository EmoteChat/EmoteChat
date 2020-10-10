package de.emotechat.addon.gui.chat.render;

import de.emotechat.addon.Constants;
import de.emotechat.addon.gui.ChatLineEntry;
import net.minecraft.client.gui.FontRenderer;

import java.util.Collection;

// TODO messages with emotes can be longer than max length
public class ChatWidthCalculator {

    protected static int getStringWidth(FontRenderer renderer, String text) {
        Collection<ChatLineEntry> entries = ChatLineEntry.parseEntries(text);
        if (entries.isEmpty()) {
            return 0;
        }

        int spaceWidth = renderer.getCharWidth(' ');
        int finalWidth = 0;

        for (ChatLineEntry entry : entries) {
            // TODO causes problems when there is an argument which begins and ends with ':' but is not an emote
            finalWidth += entry.isEmote() ? Constants.CHAT_EMOTE_SIZE : renderer.getStringWidth(entry.getContent());
        }

        return finalWidth + ((entries.size() - 1) * spaceWidth);
    }

    protected static String trimStringToWidth(FontRenderer renderer, String text, int maxWidth, boolean backwards) {
        StringBuilder builder = new StringBuilder();
        int currentWidth = 0;
        int offset = backwards ? text.length() - 1 : 0;
        int delta = backwards ? -1 : 1;
        boolean isColor = false;
        boolean ignoreColor = false;
        boolean emote = false;

        for (int i = offset; i >= 0 && i < text.length() && currentWidth < maxWidth; i += delta) {
            char c = text.charAt(i);
            if (emote) {
                if (c != Constants.EMOTE_WRAPPER) {
                    continue;
                }
                emote = false;
                continue;
            }

            emote = checkEmote(text, c, i);

            if (emote) {
                currentWidth += Constants.CHAT_EMOTE_SIZE;
            } else {
                int cWidth = renderer.getCharWidth(c);
                if (isColor) {
                    isColor = false;
                    if (c != 'l' && c != 'L') {
                        if (c == 'r' || c == 'R') {
                            ignoreColor = false;
                        }
                    } else {
                        ignoreColor = true;
                    }
                } else if (cWidth < 0) {
                    isColor = true;
                } else {
                    currentWidth += cWidth;
                    if (ignoreColor) {
                        ++currentWidth;
                    }
                }
            }

            if (currentWidth > maxWidth) {
                break;
            }

            if (backwards) {
                builder.insert(0, c);
            } else {
                builder.append(c);
            }
        }

        return builder.toString();
    }

    protected static boolean checkEmote(String text, char current, int offset) {
        if (current == Constants.EMOTE_WRAPPER && (offset == 0 || text.charAt(offset - 1) == ' ')) {
            for (int x = offset; x < text.length(); x++) {
                // TODO causes problems when there is an argument which begins and ends with ':' but is not an emote
                if (text.charAt(x) == Constants.EMOTE_WRAPPER) {
                    return true;
                }
                if (text.charAt(x) == ' ') {
                    return false;
                }
            }
        }

        return false;
    }

}
