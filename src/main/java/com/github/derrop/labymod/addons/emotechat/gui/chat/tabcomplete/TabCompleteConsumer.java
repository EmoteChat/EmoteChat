package com.github.derrop.labymod.addons.emotechat.gui.chat.tabcomplete;


import com.github.derrop.labymod.addons.emotechat.Constants;
import com.github.derrop.labymod.addons.emotechat.EmoteChatAddon;
import com.github.derrop.labymod.addons.emotechat.bttv.BTTVEmote;
import com.github.derrop.labymod.addons.emotechat.gui.emote.EmoteDropDownMenu;
import net.labymod.ingamechat.GuiChatCustom;
import net.labymod.ingamegui.ModuleGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TabCompleteConsumer implements ModuleGui.KeyConsumer {

    private final EmoteChatAddon addon;

    private final EmoteDropDownMenu dropDownMenu = new EmoteDropDownMenu(null, 0, 0, 100, 60);

    private boolean drawDropDown = false;

    private GuiTextField textField;

    public TabCompleteConsumer(EmoteChatAddon addon) {
        this.addon = addon;
        this.dropDownMenu.setOpen(true);
    }

    @Override
    public void accept(char typedChar, int keyCode) {
        if (this.drawDropDown) {
            if (keyCode == 200 || keyCode == 208) {
                BTTVEmote selected = this.dropDownMenu.getSelected();

                if (selected != null) {
                    List<BTTVEmote> emoteList = this.dropDownMenu.getEmoteList();

                    int currentIndex = emoteList.indexOf(selected);
                    int newIndex = currentIndex + (keyCode == 200 ? -1 : 1);

                    if (newIndex > 0 && newIndex < emoteList.size()) {
                        this.dropDownMenu.setSelected(emoteList.get(newIndex));
                    }
                }
            }
        }

        String chatText = this.getChatText();

        if (chatText != null && !chatText.isEmpty()) {
            String[] words = chatText.split(" ");

            String currentWord = words[words.length - 1];

            if (currentWord.startsWith(Constants.EMOTE_WRAPPER) && !currentWord.endsWith(Constants.EMOTE_WRAPPER)) {
                String query = currentWord.replaceFirst(Constants.EMOTE_WRAPPER, "").toLowerCase();

                List<BTTVEmote> emotes = this.addon.getSavedEmotes().values().stream()
                        .filter(emote -> emote.getName().toLowerCase().contains(query))
                        .sorted(Comparator.comparingInt(emote -> emote.getName().length() - query.length()))
                        .collect(Collectors.toList());

                if (emotes.size() > 0) {
                    this.drawDropDown = true;
                }

                this.dropDownMenu.update(emotes);
            } else {
                this.drawDropDown = false;
            }
        }
    }

    @SubscribeEvent
    public void handleScreenRender(RenderGameOverlayEvent event) {
        if (Minecraft.getMinecraft().currentScreen instanceof GuiChat) {
            List<ModuleGui.KeyConsumer> keyConsumers = GuiChatCustom.getModuleGui().getKeyTypeListeners();

            if (!keyConsumers.contains(this)) {
                keyConsumers.add(this);
            }
        } else {
            this.drawDropDown = false;
        }

        if (this.drawDropDown) {
            this.dropDownMenu.setX(this.textField.xPosition);
            this.dropDownMenu.setY(this.textField.yPosition - this.dropDownMenu.getHeight());

            ModuleGui moduleGui = GuiChatCustom.getModuleGui();

            this.dropDownMenu.draw((int) moduleGui.getMouseX(), (int) moduleGui.getMouseY());
        }
    }

    private String getChatText() {
        if (this.textField != null) {
            return this.textField.getText();
        }

        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;

        if (currentScreen instanceof GuiChat) {
            GuiChat guiChat = (GuiChat) Minecraft.getMinecraft().currentScreen;

            try {
                Field textFieldField = Arrays.stream(GuiChat.class.getDeclaredFields())
                        .filter(field -> field.getType().equals(GuiTextField.class))
                        .findFirst()
                        .orElse(null);

                if (textFieldField != null) {
                    textFieldField.setAccessible(true);

                    this.textField = (GuiTextField) textFieldField.get(guiChat);
                    return this.textField.getText();
                }
            } catch (IllegalAccessException exception) {
                exception.printStackTrace();
            }
        }

        return null;
    }

}
