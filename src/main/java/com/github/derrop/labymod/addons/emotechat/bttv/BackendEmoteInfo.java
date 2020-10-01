package com.github.derrop.labymod.addons.emotechat.bttv;


import com.github.derrop.labymod.addons.emotechat.Constants;
import com.github.derrop.labymod.addons.emotechat.EmoteChatAddon;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class BackendEmoteInfo {

    private static final String EMOTE_INFO_ROUTE = "/emotes/emote/%s";

    private static final String EMOTE_CACHE_ROUTE = "/emotes/cache";

    public static BackendEmoteInfo retrieveInfoByGlobalIdentifier(String globalIdentifier) {
        try {
            HttpURLConnection urlConnection = createRequest(EmoteChatAddon.BACKEND_SERVER_URL + String.format(EMOTE_INFO_ROUTE, globalIdentifier));
            urlConnection.connect();

            try (InputStream inputStream = urlConnection.getInputStream(); InputStreamReader reader = new InputStreamReader(inputStream)) {
                return Constants.GSON.fromJson(reader, BackendEmoteInfo.class);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public static void sendEmotesToServer(List<String> bttvIds) {
        try {
            HttpURLConnection urlConnection = createRequest(EmoteChatAddon.BACKEND_SERVER_URL + EMOTE_CACHE_ROUTE);
            urlConnection.setRequestMethod("POST");

            try (OutputStream outputStream = urlConnection.getOutputStream(); OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
                Constants.GSON.toJson(bttvIds, writer);
            }

            urlConnection.getResponseCode();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private static HttpURLConnection createRequest(String url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();

        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.setUseCaches(true);
        urlConnection.setConnectTimeout(5000);
        urlConnection.setReadTimeout(5000);

        urlConnection.setRequestProperty("User-Agent", "EmoteChat");
        urlConnection.setRequestProperty("Content-Type", "application/json");

        return urlConnection;
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
