package de.emotechat.addon.gui.element;

import com.madgag.gif.fmsware.GifDecoder;
import de.emotechat.addon.Constants;
import net.labymod.main.ModTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AnimatedIconData extends DynamicIconData {

    private static final Map<String, AnimatedIconData> CACHED_ICONS = new ConcurrentHashMap<>();

    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(3);

    static {
        EXECUTOR_SERVICE.scheduleAtFixedRate(() -> {
            if (CACHED_ICONS.isEmpty()) {
                return;
            }
            for (AnimatedIconData value : CACHED_ICONS.values()) {
                if (value.images == null || value.images.length == 1) {
                    value.imageIndex = 0;
                    continue;
                }

                int imageDelay = value.imageDelays[value.imageIndex];

                if (value.imageDelay >= imageDelay) {
                    value.imageDelay = 0;

                    if (value.imageIndex >= value.images.length - 1) {
                        value.imageIndex = 0;
                    } else {
                        ++value.imageIndex;
                    }
                } else {
                    ++value.imageDelay;
                }
            }
        }, 0, 1, TimeUnit.MILLISECONDS);
    }

    private BufferedImage[] images;

    private ResourceLocation[] resourceLocations;

    private int[] imageDelays;

    private int imageDelay;

    private int imageIndex = 0;

    private AnimatedIconData(String identifier, String url) {
        super(identifier, url);
    }

    public static AnimatedIconData create(String identifier, String url) {
        if (CACHED_ICONS.containsKey(identifier)) {
            return CACHED_ICONS.get(identifier);
        }

        AnimatedIconData data = new AnimatedIconData(identifier, url);
        CACHED_ICONS.put(identifier, data);

        EXECUTOR_SERVICE.execute(() -> {
            URL urlObj;
            try {
                urlObj = new URL(url);
            } catch (MalformedURLException exception) {
                exception.printStackTrace();
                return;
            }

            try {
                data.fmsWare(urlObj);
            } catch (Throwable exception) {
                try {
                    data.java(urlObj);
                } catch (Throwable exception1) {
                    try {
                        data.thumbnail(urlObj);
                    } catch (Throwable exception2) {
                        data.images = null;
                        data.resourceLocations = null;
                        data.imageDelays = null;

                        exception.printStackTrace();
                        exception1.printStackTrace();
                        exception2.printStackTrace();
                    }
                }
            }
        });

        return data;
    }

    private void fmsWare(URL url) throws Throwable {
        GifDecoder decoder = new GifDecoder();
        decoder.read(url.openStream());

        int frameCount = decoder.getFrameCount();

        this.resourceLocations = new ResourceLocation[frameCount];
        this.images = new BufferedImage[frameCount];
        this.imageDelays = new int[frameCount];

        for (int i = 0; i < frameCount; i++) {
            this.images[i] = decoder.getFrame(i);

            int imageDelay = decoder.getDelay(i);
            this.imageDelays[i] = imageDelay < 1 ? Constants.ANIMATED_ICON_TICK_MILLIS : imageDelay;
        }
    }

    private static IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
        int nNodes = rootNode.getLength();
        for (int i = 0; i < nNodes; i++) {
            if (rootNode.item(i).getNodeName().compareToIgnoreCase(nodeName) == 0) {
                return ((IIOMetadataNode) rootNode.item(i));
            }
        }
        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        rootNode.appendChild(node);
        return (node);
    }

    private void java(URL url) throws Throwable {
        ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
        ImageInputStream inputStream = ImageIO.createImageInputStream(url.openStream());
        reader.setInput(inputStream, false);

        int num = reader.getNumImages(true);

        this.resourceLocations = new ResourceLocation[num];
        this.images = new BufferedImage[num];
        this.imageDelays = new int[num];
        Arrays.fill(this.imageDelays, Constants.ANIMATED_ICON_TICK_MILLIS);

        for (int i = 0; i < num; i++) {
            BufferedImage image = reader.read(i);
            if (image != null) {
                this.images[i] = image;

                IIOMetadata meta = reader.getImageMetadata(0);
                String metaFormatName = meta.getNativeMetadataFormatName();

                IIOMetadataNode root = (IIOMetadataNode) meta.getAsTree(metaFormatName);
                IIOMetadataNode gce = getNode(root, "GraphicControlExtension");

                int delay = Integer.parseInt(gce.getAttribute("delayTime"));
                if (delay > 0) {
                    this.imageDelays[i] = delay * 10;
                }
            }
        }
    }

    private void thumbnail(URL url) {
        this.resourceLocations = new ResourceLocation[1];
        try {
            this.images = new BufferedImage[]{ImageIO.read(url)};
        } catch (IOException ignored) {
        }
    }

    @Override
    public ResourceLocation getTextureIcon() {
        if (this.images == null || this.images.length == 0) {
            return ModTextures.MISC_HEAD_QUESTION;
        }
        if (this.imageIndex >= this.resourceLocations.length) {
            return ModTextures.MISC_HEAD_QUESTION;
        }

        if (this.resourceLocations[this.imageIndex] != null) {
            return this.resourceLocations[this.imageIndex];
        }

        BufferedImage image = this.images[this.imageIndex];
        if (image == null) {
            return ModTextures.MISC_HEAD_QUESTION;
        }

        ResourceLocation location = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(super.identifier + "_" + this.imageIndex, new DynamicTexture(image));
        this.resourceLocations[this.imageIndex] = location;

        return location;
    }
}