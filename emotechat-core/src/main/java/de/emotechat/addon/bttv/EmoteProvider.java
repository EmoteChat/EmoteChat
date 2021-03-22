package de.emotechat.addon.bttv;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.emotechat.addon.Constants;
import de.emotechat.addon.EmoteChatAddon;
import net.labymod.addon.AddonLoader;
import net.labymod.addon.online.AddonInfoManager;
import net.labymod.addon.online.info.AddonInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class EmoteProvider {

    private static final ScheduledExecutorService SERVICE = Executors.newScheduledThreadPool(1);

    private static final String ID_SPLITTER_ROUTE = "emote/globalIds/splitter";
    private static final String GLOBAL_IDS_ROUTE = "emote/globalIds/provide";
    private static final String EMOTE_INFO_ROUTE = "emote/get/%s";
    private static final String EMOTE_ADD_ROUTE = "emote/add";

    private final EmoteChatAddon addon;

    private final String backendServerURL;

    private final Map<BTTVGlobalId, BTTVEmote> emoteCache = new ConcurrentHashMap<>();
    private final Map<String, BTTVEmote> savedEmotes;
    private final Runnable emoteChangeListener;

    private final ScheduledFuture<?> scheduledFuture;

    private String idSplitter = "";

    public EmoteProvider(EmoteChatAddon addon, String backendServerURL, Map<String, BTTVEmote> savedEmotes, Runnable emoteChangeListener) {
        this.addon = addon;
        this.backendServerURL = backendServerURL + (backendServerURL.endsWith("/") ? "" : "/");
        this.savedEmotes = savedEmotes;
        this.emoteChangeListener = emoteChangeListener;

        this.scheduledFuture = SERVICE.scheduleAtFixedRate(this::loadIdSplitter, 0, 2, TimeUnit.MINUTES);
    }

    private void loadIdSplitter() {
        try {
            HttpURLConnection urlConnection = this.createRequest(this.backendServerURL + ID_SPLITTER_ROUTE);

            try (InputStream inputStream = urlConnection.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line = reader.readLine();
                this.idSplitter = line != null ? line : "";
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void close() {
        this.scheduledFuture.cancel(true);
    }

    public boolean init(Collection<BTTVEmote> emotes) {
        try {
            HttpURLConnection urlConnection = this.createRequest(this.backendServerURL + GLOBAL_IDS_ROUTE);
            urlConnection.setRequestMethod("POST");

            try (OutputStream outputStream = urlConnection.getOutputStream(); Writer writer = new OutputStreamWriter(outputStream)) {
                JsonArray array = new JsonArray();

                for (BTTVEmote emote : emotes) {
                    if (emote.getBttvId() == null) {
                        continue;
                    }
                    array.add(new JsonPrimitive(emote.getBttvId()));
                }

                Constants.GSON.toJson(array, writer);
            }

            try (InputStream inputStream = urlConnection.getInputStream(); Reader reader = new InputStreamReader(inputStream)) {
                ServerEmote[] result = Constants.GSON.fromJson(reader, ServerEmote[].class);
                for (ServerEmote emote : result) {
                    for (BTTVEmote presentEmote : emotes) {
                        if (presentEmote.getBttvId() == null || !presentEmote.getBttvId().equals(emote.getBttvId())) {
                            continue;
                        }

                        presentEmote.setGlobalId(emote.getGlobalId());
                        presentEmote.setImageType(emote.getImageType());
                    }
                }
            }

            return urlConnection.getResponseCode() == 200;
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return false;
    }

    public BTTVEmote getEmoteByName(String name) {
        return this.savedEmotes.get(name.toLowerCase());
    }

    public String getIdSplitter() {
        return this.idSplitter;
    }

    public boolean isEmoteSaved(BTTVEmote emote) {
        return this.savedEmotes.values().stream().anyMatch(saved -> saved.getGlobalId().equals(emote.getGlobalId()));
    }

    public boolean addEmote(BTTVEmote emote, String name) {
        if (emote == null || name.isEmpty() || name.contains(" ")) {
            return false;
        }

        if (!this.isEmoteSaved(emote)) {
            emote = this.sendEmoteToServer(emote.getBttvId());
        }

        BTTVEmote userEmote = new BTTVEmote(emote.getGlobalId(), emote.getBttvId(), name, emote.getImageType());

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
            if (emote == null || emote.getGlobalId() == null || emote.getGlobalId().getEmoteName().isEmpty() || emote.getGlobalId().getEmoteId().isEmpty()) {
                this.emoteCache.remove(name);
            }
        });
    }

    public BTTVEmote getByGlobalIdentifier(BTTVGlobalId id) {
        return this.emoteCache.computeIfAbsent(id, identifier -> {
            BTTVEmote toFill = new BTTVEmote(id, "", "", "");
            this.fillEmoteAsync(toFill, identifier);

            return toFill;
        });
    }

    private void fillEmoteAsync(BTTVEmote toFill, BTTVGlobalId globalIdentifier) {
        Constants.EXECUTOR_SERVICE.execute(() -> {
            ServerEmote emote = this.retrieveEmoteByGlobalIdentifier(globalIdentifier);

            if (emote != null) {
                toFill.setName(emote.getName());
                toFill.setImageType(emote.getImageType());
                toFill.setBttvId(emote.getBttvId());
            }
        });
    }

    public ServerEmote retrieveEmoteByGlobalIdentifier(BTTVGlobalId globalIdentifier) {
        try {
            HttpURLConnection urlConnection = this.createRequest(
                    this.backendServerURL + String.format(EMOTE_INFO_ROUTE, globalIdentifier.toString("")));
            urlConnection.connect();

            if (urlConnection.getResponseCode() != 200) {
                urlConnection.disconnect();
                return null;
            }

            try (InputStream inputStream = urlConnection.getInputStream(); Reader reader = new InputStreamReader(inputStream)) {
                return Constants.GSON.fromJson(reader, ServerEmote.class);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public BTTVEmote sendEmoteToServer(String bttvId) {
        BTTVEmote emote = null;

        try {
            HttpURLConnection urlConnection = this.createRequest(this.backendServerURL + EMOTE_ADD_ROUTE);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);

            try (OutputStream outputStream = urlConnection.getOutputStream(); Writer writer = new OutputStreamWriter(outputStream)) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("bttvId", bttvId);

                Constants.GSON.toJson(jsonObject, writer);
            }

            try (InputStream inputStream = urlConnection.getInputStream(); Reader reader = new InputStreamReader(inputStream)) {
                ServerEmote serverEmote = Constants.GSON.fromJson(reader, ServerEmote.class);
                emote = new BTTVEmote(serverEmote.getGlobalId(), serverEmote.getBttvId(), serverEmote.getName(), serverEmote.getImageType());
            }

            urlConnection.disconnect();
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return emote;
    }

    private HttpURLConnection createRequest(String url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();

        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.setUseCaches(false);
        urlConnection.setConnectTimeout(10000);
        urlConnection.setReadTimeout(10000);

        urlConnection.setRequestProperty("User-Agent", "EmoteChat v" + this.getVersion());
        urlConnection.setRequestProperty("Content-Type", "application/json");

        return urlConnection;
    }

    private int getVersion() {
        AddonInfo info = AddonInfoManager.getInstance().getAddonInfoMap().get(this.addon.about.uuid);
        if (info != null) {
            return info.getVersion();
        }

        for (AddonInfo offlineInfo : AddonLoader.getOfflineAddons()) {
            if (offlineInfo.getUuid().equals(this.addon.about.uuid)) {
                return offlineInfo.getVersion();
            }
        }

        return -1;
    }
}
