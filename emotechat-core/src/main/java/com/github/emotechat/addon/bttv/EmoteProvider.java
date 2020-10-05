package com.github.emotechat.addon.bttv;


import com.github.emotechat.addon.Constants;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class EmoteProvider {

    private static final String EMOTE_INFO_ROUTE = "emotes/emote/%s";

    private static final String EMOTE_CACHE_ROUTE = "emotes/cache";

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
        return this.emoteCache.computeIfAbsent(globalIdentifier, identifier -> {
            BTTVEmote toFill = new BTTVEmote("", "", "", "");
            fillEmoteAsync(toFill, identifier);

            return toFill;
        });
    }

    private void fillEmoteAsync(BTTVEmote toFill, String globalIdentifier) {
        Constants.EXECUTOR_SERVICE.execute(() -> {
            BTTVEmote emote = this.retrieveEmoteByGlobalIdentifier(globalIdentifier);

            if (emote != null) {
                toFill.id = emote.getId();
                toFill.name = emote.getName();
                toFill.originalName = emote.getName();
                toFill.imageType = emote.getImageType();
                toFill.iconData = null;
            }
        });
    }

    public BTTVEmote retrieveEmoteByGlobalIdentifier(String globalIdentifier) {
        try {
            HttpURLConnection urlConnection = this.createRequest(this.backendServerURL + String.format(EMOTE_INFO_ROUTE, globalIdentifier));
            urlConnection.connect();

            try (InputStream inputStream = urlConnection.getInputStream(); InputStreamReader reader = new InputStreamReader(inputStream)) {
                return Constants.GSON.fromJson(reader, BTTVEmote.class);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public void sendEmotesToServer() {
        this.sendEmotesToServer(this.savedEmotes.values().stream().map(BTTVEmote::getId).collect(Collectors.toList()));
    }

    public void sendEmotesToServer(Collection<String> bttvIds) {
        Constants.EXECUTOR_SERVICE.execute(() -> {
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
        });
    }

    private HttpURLConnection createRequest(String url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();

        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.setUseCaches(false);
        urlConnection.setConnectTimeout(10000);
        urlConnection.setReadTimeout(10000);

        urlConnection.setRequestProperty("User-Agent", "EmoteChat");
        urlConnection.setRequestProperty("Content-Type", "application/json");

        return urlConnection;
    }

}
