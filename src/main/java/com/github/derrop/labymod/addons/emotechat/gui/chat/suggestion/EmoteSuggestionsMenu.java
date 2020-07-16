package com.github.derrop.labymod.addons.emotechat.gui.chat.suggestion;


import com.github.derrop.labymod.addons.emotechat.Constants;
import com.github.derrop.labymod.addons.emotechat.EmoteChatAddon;
import com.github.derrop.labymod.addons.emotechat.bttv.BTTVEmote;
import com.github.derrop.labymod.addons.emotechat.gui.emote.EmoteDropDownMenu;
import net.labymod.ingamechat.GuiChatCustom;
import net.labymod.ingamegui.ModuleGui;
import net.labymod.ingamegui.enums.EnumDisplayType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class EmoteSuggestionsMenu implements ModuleGui.KeyConsumer, ModuleGui.CoordinatesConsumer {

    private final EmoteChatAddon addon;

    private final EmoteDropDownMenu suggestionMenu = new EmoteDropDownMenu(null, 0, 0, 0, 13);

    private GuiTextField textField;

    private String lastQuery;

    private GuiScreen lastGui;

    public EmoteSuggestionsMenu(EmoteChatAddon addon) {
        this.addon = addon;
    }

    @Override
    public void accept(char typedChar, int keyCode) {
        if (this.suggestionMenu.getEmoteList().size() > 0) {
            if (keyCode == 200 || keyCode == 208) {
                BTTVEmote selected = this.suggestionMenu.getSelected();

                if (selected != null) {
                    List<BTTVEmote> emoteList = this.suggestionMenu.getEmoteList();

                    int currentIndex = emoteList.indexOf(selected);
                    int newIndex = currentIndex + (keyCode == 200 ? -1 : 1);

                    if (newIndex >= 0 && newIndex < emoteList.size()) {
                        this.suggestionMenu.setSelected(emoteList.get(newIndex));
                        return;
                    }
                }
            }
        }

        Optional<String> currentWordOptional = this.getCurrentEmoteWord();

        if (currentWordOptional.isPresent()) {
            String currentEmoteWord = currentWordOptional.get();

            if (keyCode == 205) {
                this.replaceCurrentEmoteWord(currentEmoteWord);
                return;
            }

            String query = currentEmoteWord.replaceFirst(Constants.EMOTE_WRAPPER, "").toLowerCase();

            if (!Objects.equals(query, this.lastQuery)) {
                List<BTTVEmote> emotes = this.addon.getSavedEmotes().values().stream()
                        .filter(emote -> emote.getName().toLowerCase().contains(query))
                        .sorted(Comparator.comparingInt(emote -> emote.getName().length() - query.length()))
                        .collect(Collectors.toList());

                this.suggestionMenu.update(emotes);

                this.lastQuery = query;
            }
        } else {
            this.suggestionMenu.update(new ArrayList<>());
        }
    }

    @Override
    public void accept(int mouseX, int mouseY, int state, EnumDisplayType displayType) {
        BTTVEmote hoverSelected = this.suggestionMenu.getHoverSelected();

        if (hoverSelected != null) {
            this.suggestionMenu.setSelected(hoverSelected);
            this.getCurrentEmoteWord().ifPresent(this::replaceCurrentEmoteWord);
        }
    }

    private Optional<String> getCurrentEmoteWord() {
        String chatText = this.textField.getText();

        if (chatText != null && !chatText.isEmpty()) {
            String[] words = chatText.split(" ");

            if (words.length > 0) {
                String currentWord = words[words.length - 1];

                if (currentWord.startsWith(Constants.EMOTE_WRAPPER) && !currentWord.endsWith(Constants.EMOTE_WRAPPER)) {
                    return Optional.of(currentWord);
                }
            }
        }

        return Optional.empty();
    }

    private void replaceCurrentEmoteWord(String currentEmoteWord) {
        BTTVEmote selected = this.suggestionMenu.getSelected();

        if (selected != null) {
            this.textField.setText(this.textField.getText().replace(
                    currentEmoteWord, Constants.EMOTE_WRAPPER + selected.getName() + Constants.EMOTE_WRAPPER
            ));
            this.suggestionMenu.update(new ArrayList<>());
        }
    }

    @SubscribeEvent
    public void handleScreenRender(RenderGameOverlayEvent event) {
        GuiScreen currentGui = Minecraft.getMinecraft().currentScreen;

        if (!Objects.equals(currentGui, this.lastGui)) {
            if (this.lastGui instanceof GuiChat) {
                this.suggestionMenu.clear();
            }

            this.textField = this.getTextField(currentGui);

            this.lastGui = currentGui;
        }

        if (this.suggestionMenu.getEmoteList().size() > 0) {
            this.drawSuggestionMenu();
        }
    }

    private GuiTextField getTextField(GuiScreen gui) {
        if (gui instanceof GuiChat) {
            try {
                Field textFieldField = Arrays.stream(GuiChat.class.getDeclaredFields())
                        .filter(field -> field.getType().equals(GuiTextField.class))
                        .findFirst()
                        .orElse(null);

                if (textFieldField != null) {
                    textFieldField.setAccessible(true);

                    return (GuiTextField) textFieldField.get(gui);
                }
            } catch (IllegalAccessException exception) {
                exception.printStackTrace();
            }
        }

        return null;
    }

    private void drawSuggestionMenu() {
        this.suggestionMenu.setEnabled(false);
        this.suggestionMenu.setOpen(true);

        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

        int queryWidth = fontRenderer.getStringWidth(this.textField.getText()) - fontRenderer.getStringWidth(this.lastQuery);

        this.suggestionMenu.setX(this.textField.xPosition + queryWidth - 1);
        this.suggestionMenu.setY(this.textField.yPosition - (this.suggestionMenu.getHeight() * (this.suggestionMenu.getEmoteList().size() + 1)) - 3);

        this.suggestionMenu.getEmoteList()
                .stream()
                .max(Comparator.comparingInt(emote -> emote.getName().length()))
                .ifPresent(emote ->
                        this.suggestionMenu.setWidth(
                                (int) (fontRenderer.getStringWidth(emote.getName()) + (Constants.SETTINGS_EMOTE_SIZE * 2.5))
                        )
                );

        ModuleGui moduleGui = GuiChatCustom.getModuleGui();

        this.suggestionMenu.draw((int) moduleGui.getMouseX(), (int) moduleGui.getMouseY());
    }

}
