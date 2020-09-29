package com.github.derrop.labymod.addons.emotechat.bttv;


import com.github.derrop.labymod.addons.emotechat.Constants;
import com.github.derrop.labymod.addons.emotechat.EmoteChatAddon;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;

public class BackendEmoteInfo {

    private static final String EMOTE_INFO_ROUTE = "/emote/%s";

    private static final String EMOTE_IDENTIFIER_ROUTE = "/emoteIdentifier/%s";

    public static BackendEmoteInfo retrieveInfoByGlobalIdentifier(String globalIdentifier) {
        return executeRequest(String.format(EMOTE_INFO_ROUTE, globalIdentifier));
    }

    public static BackendEmoteInfo retrieveInfoByBTTVId(String bttvId) {
        return executeRequest(String.format(EMOTE_IDENTIFIER_ROUTE, bttvId));
    }

    private static BackendEmoteInfo executeRequest(String route) {
        try {
            URLConnection urlConnection = new URL(EmoteChatAddon.BACKEND_SERVER_URL + route).openConnection();

            urlConnection.setUseCaches(true);
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);
            urlConnection.setRequestProperty("User-Agent", "EmoteChat");

            urlConnection.connect();

            try (InputStream inputStream = urlConnection.getInputStream(); Reader reader = new InputStreamReader(inputStream)) {
                return Constants.GSON.fromJson(reader, BackendEmoteInfo.class);
            }
        } catch (IOException ignored) {
            return null;
        }
    }

    private final String bttvId;

    private final String imageType;

    private final String globalIdentifier;

    public BackendEmoteInfo(String bttvId, String imageType, String globalIdentifier) {
        this.bttvId = bttvId;
        this.imageType = imageType;
        this.globalIdentifier = globalIdentifier;
    }

    public String getBttvId() {
        return bttvId;
    }

    public String getName() {
        return this.globalIdentifier.split("\\+")[0];
    }

    public String getImageType() {
        return imageType;
    }

    public String getGlobalIdentifier() {
        return globalIdentifier;
    }

}
