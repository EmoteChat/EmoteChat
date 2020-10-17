package de.emotechat.addon.bttv;


import com.google.gson.JsonObject;
import de.emotechat.addon.Constants;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EmoteProvider {

    private static final String EMOTE_INFO_ROUTE = "emotes/emote/%s";

    private static final String EMOTE_ADD_ROUTE = "emotes/add";

    private final Map<String, BTTVEmote> emoteCache = new ConcurrentHashMap<>();

    private final String backendServerURL;

    private final Map<String, BTTVEmote> savedEmotes;

    private final Runnable emoteChangeListener;

    public EmoteProvider(String backendServerURL, Map<String, BTTVEmote> savedEmotes, Runnable emoteChangeListener) {
        this.backendServerURL = backendServerURL + (backendServerURL.endsWith("/") ? "" : "/");
        this.savedEmotes = savedEmotes;
        this.emoteChangeListener = emoteChangeListener;
    }

    public BTTVEmote getEmoteByName(String name) {
        return this.savedEmotes.get(name.toLowerCase());
    }

    public boolean isEmoteSaved(BTTVEmote emote) {
        return this.savedEmotes.values().stream().anyMatch(saved -> saved.getId().equals(emote.getId()));
    }

    public boolean addEmote(BTTVEmote emote, String name) {
        if (emote == null || name.isEmpty() || name.contains(" ")) {
            return false;
        }

        if (!this.isEmoteSaved(emote)) {
            this.sendEmoteToServer(emote.getId());
        }

        BTTVEmote userEmote = new BTTVEmote(emote.getId(), name, emote.getName(), emote.getImageType());

        this.savedEmotes.put(userEmote.getName().toLowerCase(), userEmote);
        this.emoteChangeListener.run();
        return true;
    }

    public void removeEmote(BTTVEmote emote) {
        this.savedEmotes.remove(emote.getName().toLowerCase());
        this.emoteChangeListener.run();
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

    public void sendEmoteToServer(String bttvId) {
        Constants.EXECUTOR_SERVICE.execute(() -> {
            try {
                HttpURLConnection urlConnection = this.createRequest(this.backendServerURL + EMOTE_ADD_ROUTE);
                urlConnection.setRequestMethod("POST");

                try (OutputStream outputStream = urlConnection.getOutputStream(); OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("emoteId", bttvId);

                    Constants.GSON.toJson(jsonObject, writer);
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
