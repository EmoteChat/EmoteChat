package com.github.emotechat.addon.gui.chat.suggestion;


public class KeyTypedHandler {

    private static EmoteSuggestionsMenu emoteSuggestionsMenu;

    public static void setEmoteSuggestionsMenu(EmoteSuggestionsMenu emoteSuggestionsMenu) {
        KeyTypedHandler.emoteSuggestionsMenu = emoteSuggestionsMenu;
    }

    public static boolean blockExecution(char charTyped, int keyCode) {
        if (emoteSuggestionsMenu != null && emoteSuggestionsMenu.getSuggestionMenu().getEmoteList().size() > 0) {
            switch (keyCode) {
                case 1:
                case 28:
                case 200:
                case 208:
                    emoteSuggestionsMenu.accept(charTyped, keyCode);
                    return true;
            }
        }
        return false;
    }

}
