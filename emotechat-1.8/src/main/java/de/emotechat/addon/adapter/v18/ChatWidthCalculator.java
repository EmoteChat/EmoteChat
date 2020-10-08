package de.emotechat.addon.adapter.v18;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.List;

public class ChatWidthCalculator extends de.emotechat.addon.gui.chat.render.ChatWidthCalculator {

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

}
