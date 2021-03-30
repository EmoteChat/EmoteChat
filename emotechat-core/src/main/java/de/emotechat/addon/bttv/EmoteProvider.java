package de.emotechat.addon.bttv;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.emotechat.addon.Constants;
import de.emotechat.addon.EmoteChatAddon;
import net.labymod.addon.AddonLoader;
import net.labymod.addon.online.AddonInfoManager;
import net.labymod.addon.online.info.AddonInfo;
import net.minecraft.client.Minecraft;

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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class EmoteProvider {

    private static final ScheduledExecutorService SERVICE = Executors.newScheduledThreadPool(1);

    private static final String ID_SPLITTER_ROUTE = "emote/globalIds/splitter";
    private static final String GLOBAL_IDS_ROUTE = "emote/globalIds/provide";
    private static final String LEGACY_EMOTE_INFO_ROUTE = "emotes/emote/%s";
    private static final String EMOTE_INFO_ROUTE = "emote/get/%s";
    private static final String EMOTE_REPORT_ROUTE = "emote/report/%s";
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

    public void retrieveEmotesFromServer(Collection<BTTVEmote> emotes) {
        try {
            HttpURLConnection urlConnection = this.createRequest(this.backendServerURL + GLOBAL_IDS_ROUTE);
            urlConnection.setRequestMethod("POST");

            try (OutputStream outputStream = urlConnection.getOutputStream();
                 Writer writer = new OutputStreamWriter(outputStream)) {
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

                Map<String, ServerEmote> serverEmotes = new HashMap<>();
                for (ServerEmote serverEmote : result) {
                    serverEmotes.put(serverEmote.getBttvId(), serverEmote);
                }

                emotes.removeIf(presentEmote -> {
                    ServerEmote serverEmote = serverEmotes.get(presentEmote.getBttvId());

                    if (serverEmote == null || presentEmote.getName() == null) {
                        return true;
                    }

                    presentEmote.setGlobalId(serverEmote.getGlobalId());
                    presentEmote.setImageType(serverEmote.getImageType());
                    return false;
                });
            }

            urlConnection.getResponseCode();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public BTTVEmote getEmoteByName(String name) {
        return this.savedEmotes.get(name.toLowerCase());
    }

    public String getIdSplitter() {
        return this.idSplitter;
    }

    public boolean isEmoteSaved(BTTVEmote emote) {
        return this.savedEmotes.values().stream()
                .anyMatch(saved -> saved.getBttvId().equals(emote.getBttvId()));
    }

    public void addEmote(final BTTVEmote emote, String name, Consumer<Boolean> successConsumer) {
        if (emote == null || name.isEmpty() || name.contains(" ")) {
            successConsumer.accept(false);
            return;
        }

        if (this.isEmoteSaved(emote)) {
            successConsumer.accept(true);
            return;
        }

        Constants.EXECUTOR_SERVICE.execute(() -> {
            BTTVEmote newEmote = this.sendEmoteToServer(emote.getBttvId());

            Minecraft.getMinecraft().addScheduledTask(() -> {
                if (newEmote == null) {
                    successConsumer.accept(false);
                    return;
                }

                BTTVEmote userEmote = new BTTVEmote(newEmote.getGlobalId(), newEmote.getBttvId(), name, newEmote.getImageType());

                this.savedEmotes.put(userEmote.getName().toLowerCase(), userEmote);
                this.emoteChangeListener.run();

                successConsumer.accept(true);
            });
        });
    }

    public void reportEmote(BTTVEmote emote, Consumer<Boolean> successCallback) {
        Constants.EXECUTOR_SERVICE.execute(() -> {
            try {
                HttpURLConnection urlConnection = this.createRequest(
                        this.backendServerURL + String.format(EMOTE_REPORT_ROUTE, emote.getBttvId()));
                urlConnection.connect();

                successCallback.accept(urlConnection.getResponseCode() == 200);

                urlConnection.disconnect();
            } catch (IOException exception) {
                exception.printStackTrace();
                successCallback.accept(false);
            }
        });
    }

    public void removeEmote(BTTVEmote emote) {
        this.savedEmotes.remove(emote.getName().toLowerCase());
        this.emoteChangeListener.run();
    }

    public void clearCache() {
        this.emoteCache.clear();
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
            if (globalIdentifier instanceof LegacyBTTVGlobalId) {
                HttpURLConnection urlConnection = this.createRequest(
                        this.backendServerURL + String.format(LEGACY_EMOTE_INFO_ROUTE, globalIdentifier.getEmoteName() + "+" + globalIdentifier.getEmoteId()));
                urlConnection.connect();

                if (urlConnection.getResponseCode() != 200) {
                    urlConnection.disconnect();
                    return null;
                }

                try (InputStream inputStream = urlConnection.getInputStream(); Reader reader = new InputStreamReader(inputStream)) {
                    JsonObject object = Constants.GSON.fromJson(reader, JsonObject.class);
                    return new ServerEmote(
                            globalIdentifier,
                            object.get("id").getAsString(),
                            object.get("code").getAsString(),
                            object.get("imageType").getAsString()
                    );
                }
            }

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
                emote = serverEmote.toBTTVEmote();
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
