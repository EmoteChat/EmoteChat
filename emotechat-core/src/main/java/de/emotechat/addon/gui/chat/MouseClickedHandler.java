package de.emotechat.addon.gui.chat;


import java.util.ArrayList;
import java.util.List;

public class MouseClickedHandler {

    private static final List<MouseClickListener> LISTENERS = new ArrayList<>();

    public static void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        LISTENERS.forEach(mouseClickListener -> mouseClickListener.mouseClicked(mouseX, mouseY, mouseButton));
    }

    public static void addListener(MouseClickListener listener) {
        LISTENERS.add(listener);
    }

    public interface MouseClickListener {

        void mouseClicked(int mouseX, int mouseY, int mouseButton);

    }

}
