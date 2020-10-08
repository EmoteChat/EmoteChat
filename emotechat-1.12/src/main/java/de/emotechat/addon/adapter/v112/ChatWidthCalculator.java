package de.emotechat.addon.adapter.v112;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import java.util.List;

public class ChatWidthCalculator extends de.emotechat.addon.gui.chat.render.ChatWidthCalculator {

    public static String format(String text, boolean useColors) {
        return !useColors && !Minecraft.getMinecraft().gameSettings.chatColours ? ChatFormatting.stripFormatting(text) : text;
    }

    public static List<ITextComponent> calculateLines(ITextComponent component, int chatWidth, FontRenderer renderer, boolean unknown, boolean useColors) {
        int fullLength = 0;
        ITextComponent fullComponent = new TextComponentString("");
        List<ITextComponent> output = Lists.newArrayList();
        List<ITextComponent> input = Lists.newArrayList(component);

        for (int j = 0; j < input.size(); ++j) {
            ITextComponent currentComponent = input.get(j);
            String inputText = currentComponent.getUnformattedText();
            boolean flag = false;
            int newLine = inputText.indexOf('\n');
            if (newLine != -1) {
                String nextText = inputText.substring(newLine + 1);
                inputText = inputText.substring(0, newLine + 1);
                TextComponentString chatcomponenttext = new TextComponentString(nextText);
                chatcomponenttext.setStyle(currentComponent.getStyle().createShallowCopy());
                input.add(j + 1, chatcomponenttext);
                flag = true;
            }

            String formattedText = format(currentComponent.getStyle().getFormattingCode() + inputText, useColors);
            String finalText = formattedText.endsWith("\n") ? formattedText.substring(0, formattedText.length() - 1) : formattedText;
            int currentWidth = getStringWidth(renderer, finalText);

            TextComponentString finalComponent = new TextComponentString(finalText);
            finalComponent.setStyle(currentComponent.getStyle().createShallowCopy());

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
                    TextComponentString overflowComponent = new TextComponentString(overflowText);
                    overflowComponent.setStyle(currentComponent.getStyle().createShallowCopy());
                    input.add(j + 1, overflowComponent);
                }

                currentWidth = getStringWidth(renderer, trimmedText);
                finalComponent = new TextComponentString(trimmedText);
                finalComponent.setStyle(currentComponent.getStyle().createShallowCopy());
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
                fullComponent = new TextComponentString("");
            }
        }

        output.add(fullComponent);
        return output;
    }

}
