package com.github.derrop.labymod.addons.emotechat;

import com.google.gson.Gson;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface Constants {

    Gson GSON = new Gson();

    ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    String EMOTE_WRAPPER = ":";
    int CHAT_EMOTE_SIZE = 17;
    int SETTINGS_EMOTE_SIZE = 10;
    int LINE_HEIGHT = 9;

}
