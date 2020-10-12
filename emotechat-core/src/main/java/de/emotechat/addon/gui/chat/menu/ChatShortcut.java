package de.emotechat.addon.gui.chat.menu;

import de.emotechat.addon.EmoteChatAddon;
import net.labymod.ingamechat.GuiChatCustom;
import net.labymod.ingamegui.enums.EnumDisplayType;
import net.labymod.settings.elements.ControlElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class ChatShortcut {

    private static final Field BUTTONS_FIELD;
    private static final Field DISPLAY_NAME_FIELD;
    private static final Field BUTTON_ID_FIELD;
    private static final Constructor<?> BUTTON_CONSTRUCTOR;
    private static final Class<?> BUTTON_CLASS;
    private static final Field TEXT_INPUT_FIELD;

    public static final int BUTTON_ID = 1234;

    static {
        Field buttonsField = null;
        Field displayNameField = null;
        Field buttonIdField = null;
        Field textInputField = null;
        Constructor<?> buttonConstructor = null;
        Class<?> buttonClass = null;

        try {
            buttonsField = GuiChatCustom.class.getDeclaredField("chatButtons");
            buttonsField.setAccessible(true);

            buttonClass = Class.forName(GuiChatCustom.class.getName() + "$ChatButton");

            displayNameField = buttonClass.getDeclaredField("displayName");
            displayNameField.setAccessible(true);

            buttonIdField = buttonClass.getDeclaredField("id");
            buttonIdField.setAccessible(true);

            buttonConstructor = buttonClass.getDeclaredConstructor(GuiChatCustom.class, int.class, String.class, ControlElement.IconData.class, boolean.class);
            buttonConstructor.setAccessible(true);

            textInputField = Arrays.stream(GuiChat.class.getDeclaredFields()).filter(field -> field.getType().equals(GuiTextField.class)).findFirst()
                    .map(field -> {
                        field.setAccessible(true);
                        return field;
                    })
                    .orElse(null);

        } catch (NoSuchFieldException | ClassNotFoundException | NoSuchMethodException exception) {
            exception.printStackTrace();
        }

        BUTTONS_FIELD = buttonsField;
        BUTTON_ID_FIELD = buttonIdField;
        DISPLAY_NAME_FIELD = displayNameField;
        BUTTON_CONSTRUCTOR = buttonConstructor;
        BUTTON_CLASS = buttonClass;
        TEXT_INPUT_FIELD = textInputField;
    }

    public static boolean shouldInitialize(GuiChatCustom chat) throws IllegalAccessException {
        if (BUTTON_CLASS == null || BUTTONS_FIELD == null || DISPLAY_NAME_FIELD == null || BUTTON_ID_FIELD == null || BUTTON_CONSTRUCTOR == null) {
            return false;
        }

        Object buttons = BUTTONS_FIELD.get(chat);
        if (buttons == null) {
            return false;
        }
        Object button = Array.get(buttons, 0);
        int id = BUTTON_ID_FIELD.getInt(button);

        return id != BUTTON_ID;
    }

    public static void init(GuiChatCustom chat) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Object button = BUTTON_CONSTRUCTOR.newInstance(chat, BUTTON_ID, "", new ControlElement.IconData(new ResourceLocation("minecraft:emotechat/peepohappy.png")), true);
        DISPLAY_NAME_FIELD.set(button, "Emote menu");

        Object[] buttons = (Object[]) BUTTONS_FIELD.get(chat);
        Object[] newButtons = (Object[]) Array.newInstance(BUTTON_CLASS, Array.getLength(buttons) + 1);

        System.arraycopy(buttons, 0, newButtons, 1, Array.getLength(buttons));
        newButtons[0] = button;

        BUTTONS_FIELD.set(chat, newButtons);
    }

    public static void initListener(EmoteChatAddon addon) {
        GuiChatCustom.getModuleGui().getMouseClickListeners().add((mouseX, mouseY, mouseButton, displayType) -> {
            if (displayType != EnumDisplayType.INGAME) {
                return;
            }

            GuiScreen screen = Minecraft.getMinecraft().currentScreen;
            if (screen instanceof GuiChatCustom) {
                click(addon, (GuiChatCustom) screen, mouseX, mouseY);
            }
        });
    }

    private static void click(EmoteChatAddon addon, GuiChatCustom chat, int mouseX, int mouseY) {
        Object[] buttons = new Object[0];
        try {
            buttons = (Object[]) BUTTONS_FIELD.get(chat);
        } catch (IllegalAccessException exception) {
            exception.printStackTrace();
        }

        for (int slot = 0; slot < buttons.length; slot++) {
            boolean hoverSymbols = (mouseX > chat.width - 2 - 13 - slot * 14 && mouseX < chat.width - 2 - slot * 14 && mouseY > chat.height - 14 && mouseY < chat.height - 2);

            if (hoverSymbols) {
                try {
                    int clickedButtonId = BUTTON_ID_FIELD.getInt(buttons[slot]);

                    if (clickedButtonId == BUTTON_ID) {
                        GuiChatCustom.activeTab = slot;

                        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
                        GuiTextField textField = (GuiTextField) TEXT_INPUT_FIELD.get(chat);
                        String text = textField.getText();

                        Minecraft.getMinecraft().displayGuiScreen(currentScreen instanceof GuiChatEmoteMenu ? new GuiChatCustom(text) : new GuiChatEmoteMenu(text, addon));
                    }
                } catch (IllegalAccessException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

}
