package com.github.derrop.labymod.addons.emotechat.bttv;

import com.github.derrop.labymod.addons.emotechat.EmoteChatAddon;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class BTTVEmote {

    private static final String EMOTE_IMAGE_ENDPOINT = "https://cdn.betterttv.net/emote/%s/%dx";

    private static final String EMOTE_INFO_ENDPOINT = "https://api.betterttv.net/3/emotes/%s";

    public static BTTVEmote requestById(int id) {
        try {
            URLConnection urlConnection = new URL(String.format(EMOTE_INFO_ENDPOINT, id)).openConnection();

            urlConnection.setUseCaches(true);
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);
            urlConnection.setRequestProperty("User-Agent", "LabyMod Emote addon");

            urlConnection.connect();

            try (InputStream inputStream = urlConnection.getInputStream(); InputStreamReader reader = new InputStreamReader(inputStream)) {
                return EmoteChatAddon.GSON.fromJson(reader, BTTVEmote.class);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    private String id;

    @SerializedName("code")
    private String name;

    public BTTVEmote(String id, String code) {
        this.id = id;
        this.name = code;
    }

    public String getImageURL(int size) {
        return String.format(EMOTE_IMAGE_ENDPOINT, this.id, size);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
