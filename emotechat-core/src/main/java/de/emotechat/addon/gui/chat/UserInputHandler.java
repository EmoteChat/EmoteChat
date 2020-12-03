package de.emotechat.addon.gui.chat;


import java.util.ArrayList;
import java.util.List;

public class UserInputHandler {

    private static final List<MouseClickListener> MOUSE_LISTENERS = new ArrayList<>();

    private static final List<KeyListener> KEY_LISTENERS = new ArrayList<>();

    /**
     * This method is called in {@link net.labymod.ingamegui.ModuleGui#keyTyped(char, int)}, call injected via ASM
     */
    public static void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        MOUSE_LISTENERS.forEach(mouseClickListener -> mouseClickListener.mouseClicked(mouseX, mouseY, mouseButton));
    }

    /**
     * This method is called in {@link net.labymod.ingamechat.GuiChatCustom#keyTyped(char, int)}, call injected via ASM
     *
     * @return if the minecraft internal key input handling should be prevented
     */
    public static boolean keyTyped(char charTyped, int keyCode) {
        boolean prevention = false;

        for (KeyListener keyListener : KEY_LISTENERS) {
            if (keyListener.keyTyped(charTyped, keyCode)) {
                prevention = true;
            }
        }

        return prevention;
    }

    public static void addMouseListener(MouseClickListener listener) {
        MOUSE_LISTENERS.add(listener);
    }

    public static void addKeyListener(KeyListener listener) {
        KEY_LISTENERS.add(listener);
    }

    public interface MouseClickListener {
        void mouseClicked(int mouseX, int mouseY, int mouseButton);
    }

    public interface KeyListener {
        /**
         * @return if the minecraft internal key input handling should be prevented
         */
        boolean keyTyped(char charTyped, int keyCode);
    }

}
