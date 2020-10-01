package com.github.derrop.labymod.addons.emotechat.bttv;


import com.github.derrop.labymod.addons.emotechat.Constants;
import com.github.derrop.labymod.addons.emotechat.bttv.backend.BackendEmoteInfo;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class EmoteProvider {

    private static final String EMOTE_INFO_ROUTE = "/emotes/emote/%s";

    private static final String EMOTE_CACHE_ROUTE = "/emotes/cache";

    private final Map<String, BTTVEmote> emoteCache = new ConcurrentHashMap<>();

    private final String backendServerURL;

    private final Map<String, BTTVEmote> savedEmotes;

    public EmoteProvider(String backendServerURL, Map<String, BTTVEmote> savedEmotes) {
        this.backendServerURL = backendServerURL + (backendServerURL.endsWith("/") ? "" : "/");
        this.savedEmotes = savedEmotes;
    }

    public void cleanupCache() {
        this.emoteCache.forEach((name, emote) -> {
            if (emote == null || emote.getId() == null || emote.getId().isEmpty()) {
                this.emoteCache.remove(name);
            }
        });
    }

    public BTTVEmote getByGlobalIdentifier(String globalIdentifier) {
        if (!this.emoteCache.containsKey(globalIdentifier)) {
            BTTVEmote toFill = new BTTVEmote("", "", "", "");

            this.emoteCache.put(globalIdentifier, toFill);
            fillEmoteAsync(toFill, globalIdentifier);
        }
        return this.emoteCache.get(globalIdentifier);
    }

    private void fillEmoteAsync(BTTVEmote toFill, String globalIdentifier) {
        Constants.EXECUTOR_SERVICE.execute(() -> {
            BackendEmoteInfo emoteInfo = this.retrieveInfoByGlobalIdentifier(globalIdentifier);

            if (emoteInfo != null) {
                toFill.id = emoteInfo.getBttvId();
                toFill.name = emoteInfo.getName();
                toFill.originalName = emoteInfo.getName();
                toFill.imageType = emoteInfo.getImageType();
                toFill.iconData = null;
            }
        });
    }

    public BackendEmoteInfo retrieveInfoByGlobalIdentifier(String globalIdentifier) {
        try {
            HttpURLConnection urlConnection = this.createRequest(this.backendServerURL + String.format(EMOTE_INFO_ROUTE, globalIdentifier));
            urlConnection.connect();

            try (InputStream inputStream = urlConnection.getInputStream(); InputStreamReader reader = new InputStreamReader(inputStream)) {
                return Constants.GSON.fromJson(reader, BackendEmoteInfo.class);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public void sendEmotesToServer() {
        this.sendEmotesToServer(this.savedEmotes.values().stream().map(BTTVEmote::getId).collect(Collectors.toList()));
    }

    public void sendEmotesToServer(List<String> bttvIds) {
        try {
            HttpURLConnection urlConnection = this.createRequest(this.backendServerURL + EMOTE_CACHE_ROUTE);
            urlConnection.setRequestMethod("POST");

            try (OutputStream outputStream = urlConnection.getOutputStream(); OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
                Constants.GSON.toJson(bttvIds, writer);
            }

            urlConnection.getResponseCode();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private HttpURLConnection createRequest(String url) throws IOException {
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

}
