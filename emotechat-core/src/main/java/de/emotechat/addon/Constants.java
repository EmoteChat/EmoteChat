package de.emotechat.addon;

import com.google.gson.Gson;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface Constants {

    Gson GSON = new Gson();

    ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    char EMOTE_WRAPPER = ':';
    int CHAT_EMOTE_SIZE = 17;
    int SETTINGS_EMOTE_PREVIEW_SIZE = 40;
    int SETTINGS_EMOTE_SIZE = 10;
    int LINE_HEIGHT = 9;

    int ANIMATED_ICON_TICK_MILLIS = 50;

}
