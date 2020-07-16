package com.github.derrop.labymod.addons.emotechat.gui.chat.tabcomplete;


import com.github.derrop.labymod.addons.emotechat.Constants;
import com.github.derrop.labymod.addons.emotechat.EmoteChatAddon;
import com.github.derrop.labymod.addons.emotechat.bttv.BTTVEmote;
import com.github.derrop.labymod.addons.emotechat.gui.emote.EmoteDropDownMenu;
import net.labymod.ingamechat.GuiChatCustom;
import net.labymod.ingamegui.ModuleGui;
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

public class TabCompleteConsumer implements ModuleGui.KeyConsumer {

    private final EmoteChatAddon addon;

    private final EmoteDropDownMenu dropDownMenu = new EmoteDropDownMenu(null, 0, 0, 0, 13);

    private GuiTextField textField;

    private String lastQuery;

    private GuiScreen lastGui;

    public TabCompleteConsumer(EmoteChatAddon addon) {
        this.addon = addon;
    }

    @Override
    public void accept(char typedChar, int keyCode) {
        if (this.dropDownMenu.getEmoteList().size() > 0) {
            if (keyCode == 200 || keyCode == 208) {
                BTTVEmote selected = this.dropDownMenu.getSelected();

                if (selected != null) {
                    List<BTTVEmote> emoteList = this.dropDownMenu.getEmoteList();

                    int currentIndex = emoteList.indexOf(selected);
                    int newIndex = currentIndex + (keyCode == 200 ? -1 : 1);

                    if (newIndex > 0 && newIndex < emoteList.size()) {
                        this.dropDownMenu.setSelected(emoteList.get(newIndex));
                        return;
                    }
                }
            }
        }

        String chatText = this.textField.getText();

        if (chatText != null && !chatText.isEmpty()) {
            String[] words = chatText.split(" ");

            if (words.length > 0) {
                String currentWord = words[words.length - 1];

                if (currentWord.startsWith(Constants.EMOTE_WRAPPER) && !currentWord.endsWith(Constants.EMOTE_WRAPPER)) {
                    if (keyCode == 205) {
                        BTTVEmote selected = this.dropDownMenu.getSelected();

                        if (selected != null) {
                            this.textField.setText(chatText.replace(currentWord, Constants.EMOTE_WRAPPER + selected.getName() + Constants.EMOTE_WRAPPER));
                            this.dropDownMenu.update(new ArrayList<>());

                            return;
                        }
                    }

                    String query = currentWord.replaceFirst(Constants.EMOTE_WRAPPER, "").toLowerCase();

                    if (!Objects.equals(query, this.lastQuery)) {
                        List<BTTVEmote> emotes = this.addon.getSavedEmotes().values().stream()
                                .filter(emote -> emote.getName().toLowerCase().contains(query))
                                .sorted(Comparator.comparingInt(emote -> emote.getName().length() - query.length()))
                                .collect(Collectors.toList());

                        this.dropDownMenu.update(emotes);

                        this.lastQuery = query;
                    }

                    return;
                }
            }
        }

        this.dropDownMenu.update(new ArrayList<>());
    }

    @SubscribeEvent
    public void handleScreenRender(RenderGameOverlayEvent event) {
        GuiScreen currentGui = Minecraft.getMinecraft().currentScreen;

        if (!Objects.equals(currentGui, this.lastGui)) {
            if (this.lastGui instanceof GuiChat) {
                this.dropDownMenu.clear();
            }

            if (currentGui instanceof GuiChat) {
                try {
                    Field textFieldField = Arrays.stream(GuiChat.class.getDeclaredFields())
                            .filter(field -> field.getType().equals(GuiTextField.class))
                            .findFirst()
                            .orElse(null);

                    if (textFieldField != null) {
                        textFieldField.setAccessible(true);

                        this.textField = (GuiTextField) textFieldField.get(currentGui);
                    }
                } catch (IllegalAccessException exception) {
                    exception.printStackTrace();
                }
            }

            this.lastGui = currentGui;
        }

        if (this.dropDownMenu.getEmoteList().size() > 0) {
            this.dropDownMenu.setOpen(true);

            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

            int queryStart = fontRenderer.getStringWidth(this.textField.getText()) - fontRenderer.getStringWidth(this.lastQuery);

            this.dropDownMenu.setX(this.textField.xPosition + queryStart);
            this.dropDownMenu.setY(this.textField.yPosition - (this.dropDownMenu.getHeight() * (this.dropDownMenu.getEmoteList().size() + 1)) - 3);

            this.dropDownMenu.getEmoteList()
                    .stream()
                    .max(Comparator.comparingInt(emote -> emote.getName().length()))
                    .ifPresent(emote ->
                            this.dropDownMenu.setWidth(
                                    (int) (fontRenderer.getStringWidth(emote.getName()) + (Constants.SETTINGS_EMOTE_SIZE * 1.5) + 30)
                            )
                    );

            ModuleGui moduleGui = GuiChatCustom.getModuleGui();

            this.dropDownMenu.draw((int) moduleGui.getMouseX(), (int) moduleGui.getMouseY());
        }
    }

}
