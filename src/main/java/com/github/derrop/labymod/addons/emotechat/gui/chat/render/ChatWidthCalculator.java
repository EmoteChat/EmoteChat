package com.github.derrop.labymod.addons.emotechat.gui.chat.render;

import com.github.derrop.labymod.addons.emotechat.Constants;
import com.github.derrop.labymod.addons.emotechat.gui.ChatLineEntry;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.Collection;
import java.util.List;

// TODO messages with emotes can be longer than max length
public class ChatWidthCalculator {

    public static String format(String text, boolean useColors) {
        return !useColors && !Minecraft.getMinecraft().gameSettings.chatColours ? EnumChatFormatting.getTextWithoutFormattingCodes(text) : text;
    }

    public static List<IChatComponent> calculateLines(IChatComponent component, int chatWidth, FontRenderer renderer, boolean unknown, boolean useColors) {
        int fullLength = 0;
        IChatComponent fullComponent = new ChatComponentText("");
        List<IChatComponent> output = Lists.newArrayList();
        List<IChatComponent> input = Lists.newArrayList(component);

        for (int j = 0; j < input.size(); ++j) {
            IChatComponent currentComponent = input.get(j);
            String inputText = currentComponent.getUnformattedTextForChat();
            boolean flag = false;
            int newLine = inputText.indexOf('\n');
            if (newLine != -1) {
                String nextText = inputText.substring(newLine + 1);
                inputText = inputText.substring(0, newLine + 1);
                ChatComponentText chatcomponenttext = new ChatComponentText(nextText);
                chatcomponenttext.setChatStyle(currentComponent.getChatStyle().createShallowCopy());
                input.add(j + 1, chatcomponenttext);
                flag = true;
            }

            String formattedText = format(currentComponent.getChatStyle().getFormattingCode() + inputText, useColors);
            String finalText = formattedText.endsWith("\n") ? formattedText.substring(0, formattedText.length() - 1) : formattedText;
            int currentWidth = getStringWidth(renderer, finalText);

            ChatComponentText finalComponent = new ChatComponentText(finalText);
            finalComponent.setChatStyle(currentComponent.getChatStyle().createShallowCopy());

            if (fullLength + currentWidth > chatWidth) {
                String trimmedText = trimStringToWidth(renderer, formattedText, chatWidth - fullLength, false);
                String overflowText = trimmedText.length() < formattedText.length() ? formattedText.substring(trimmedText.length()) : null;
                if (overflowText != null) {
                    int lastSpace = trimmedText.lastIndexOf(' ');
                    if (lastSpace >= 0 && getStringWidth(renderer, formattedText.substring(0, lastSpace)) > 0) {
                        trimmedText = formattedText.substring(0, lastSpace);
                        if (unknown) {
                            ++lastSpace;
                        }

                        overflowText = formattedText.substring(lastSpace);
                    } else if (fullLength > 0 && !formattedText.contains(" ")) {
                        trimmedText = "";
                        overflowText = formattedText;
                    }

                    overflowText = FontRenderer.getFormatFromString(trimmedText) + overflowText;
                    ChatComponentText overflowComponent = new ChatComponentText(overflowText);
                    overflowComponent.setChatStyle(currentComponent.getChatStyle().createShallowCopy());
                    input.add(j + 1, overflowComponent);
                }

                currentWidth = getStringWidth(renderer, trimmedText);
                finalComponent = new ChatComponentText(trimmedText);
                finalComponent.setChatStyle(currentComponent.getChatStyle().createShallowCopy());
                flag = true;
            }

            if (fullLength + currentWidth <= chatWidth) {
                fullLength += currentWidth;
                fullComponent.appendSibling(finalComponent);
            } else {
                flag = true;
            }

            if (flag) {
                output.add(fullComponent);
                fullLength = 0;
                fullComponent = new ChatComponentText("");
            }
        }

        output.add(fullComponent);
        return output;
    }

    private static int getStringWidth(FontRenderer renderer, String text) {
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

    private static String trimStringToWidth(FontRenderer renderer, String text, int maxWidth, boolean backwards) {
        StringBuilder builder = new StringBuilder();
        int currentWidth = 0;
        int offset = backwards ? text.length() - 1 : 0;
        int delta = backwards ? -1 : 1;
        boolean isColor = false;
        boolean ignoreColor = false;
        boolean emote = false;

        for(int i = offset; i >= 0 && i < text.length() && currentWidth < maxWidth; i += delta) {
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

    private static boolean checkEmote(String text, char current, int offset) {
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
