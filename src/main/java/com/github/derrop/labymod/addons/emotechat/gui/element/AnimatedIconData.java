package com.github.derrop.labymod.addons.emotechat.gui.element;

import com.github.derrop.labymod.addons.emotechat.Constants;
import com.madgag.gif.fmsware.GifDecoder;
import net.labymod.main.ModTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AnimatedIconData extends DynamicIconData {

    private static final Map<String, AnimatedIconData> CACHED_ICONS = new ConcurrentHashMap<>();
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(3);

    static {
        EXECUTOR_SERVICE.execute(() -> {
            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(Constants.ANIMATED_ICON_TICK_MILLIS);
                } catch (InterruptedException exception) {
                    exception.printStackTrace();
                }

                if (CACHED_ICONS.isEmpty()) {
                    continue;
                }
                for (AnimatedIconData value : CACHED_ICONS.values()) {
                    if (value.images == null) {
                        continue;
                    }

                    if (value.index >= value.images.length - 1) {
                        value.index = 0;
                    } else {
                        ++value.index;
                    }
                }
            }
        });
    }

    private BufferedImage[] images;
    private ResourceLocation[] resourceLocations;
    private int index;

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

            Collection<BufferedImage> output = new ArrayList<>();

            try {
                GifDecoder decoder = new GifDecoder();
                decoder.read(new URL(url).openStream());

                for (int i = 0; i < decoder.getFrameCount(); i++) {
                    BufferedImage image = decoder.getFrame(i);
                    // TODO implement delay
                    if (image != null) {
                        output.add(image);
                    }
                }
            } catch (Throwable exception) {
                try {
                    ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
                    ImageInputStream inputStream = ImageIO.createImageInputStream(new URL(url).openStream());
                    reader.setInput(inputStream, false);

                    int num = reader.getNumImages(true);
                    for (int i = 0; i < num; i++) {
                        BufferedImage image = reader.read(i);
                        if (image != null) {
                            output.add(image);
                        }
                    }

                } catch (Throwable exception1) {
                    exception.printStackTrace();
                    exception1.printStackTrace();
                    return;
                }
            }

            data.resourceLocations = new ResourceLocation[output.size()];
            data.images = output.toArray(new BufferedImage[0]);
        });

        return data;
    }

    @Override
    public ResourceLocation getTextureIcon() {
        if (this.images == null || this.images.length == 0) {
            return ModTextures.MISC_HEAD_QUESTION;
        }
        if (this.resourceLocations[this.index] != null) {
            return this.resourceLocations[this.index];
        }

        BufferedImage image = this.images[this.index];
        if (image == null) {
            return ModTextures.MISC_HEAD_QUESTION;
        }

        ResourceLocation location = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(super.identifier + "_" + this.index, new DynamicTexture(image));
        this.resourceLocations[this.index] = location;
        return location;
    }
}
