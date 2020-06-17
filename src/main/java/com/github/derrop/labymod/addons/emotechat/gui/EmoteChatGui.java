package com.github.derrop.labymod.addons.emotechat.gui;

import com.github.derrop.labymod.addons.emotechat.Constants;
import com.github.derrop.labymod.addons.emotechat.EmoteChatAddon;
import com.github.derrop.labymod.addons.emotechat.bttv.BTTVEmote;
import net.labymod.main.LabyMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

// TODO: multiple emotes cannot be displayed in one line
public class EmoteChatGui extends GuiNewChat {

    private static final Field CHAT_LINES_FIELD, SCROLL_POS_FIELD, SCROLLED_FIELD;

    static {
        try {
            CHAT_LINES_FIELD = GuiNewChat.class.getDeclaredField("i"); // field_146253_i
            CHAT_LINES_FIELD.setAccessible(true);

            SCROLL_POS_FIELD = GuiNewChat.class.getDeclaredField("j"); // scrollPos
            SCROLL_POS_FIELD.setAccessible(true);

            SCROLLED_FIELD = GuiNewChat.class.getDeclaredField("k"); // isScrolled
            SCROLLED_FIELD.setAccessible(true);
        } catch (NoSuchFieldException exception) {
            throw new Error(exception);
        }
    }

    private final EmoteChatAddon addon;
    private final Minecraft mc;

    private List<ChatLine> lines;

    public EmoteChatGui(EmoteChatAddon addon, Minecraft mc) {
        super(mc);
        this.addon = addon;
        this.mc = mc;
    }

    private int getScrollPos() {
        try {
            return SCROLL_POS_FIELD.getInt(this);
        } catch (IllegalAccessException exception) {
            exception.printStackTrace();
            return 0;
        }
    }

    private boolean isScrolled() {
        try {
            return SCROLLED_FIELD.getBoolean(this);
        } catch (IllegalAccessException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private void initLines() {
        try {
            this.lines = (List<ChatLine>) CHAT_LINES_FIELD.get(this);
        } catch (IllegalAccessException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void drawChat(int p_drawChat_1_) {
        if (!this.addon.isEnabled()) {
            super.drawChat(p_drawChat_1_);
            return;
        }

        this.initLines();

        if (this.mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN) {
            int maxDisplayedLines = this.getLineCount();
            boolean chatOpen = false;
            int drawnLines = 0;
            int lineSize = this.lines.size();
            float opacity = this.mc.gameSettings.chatOpacity * 0.9F + 0.1F;
            if (lineSize > 0) {
                if (this.getChatOpen()) {
                    chatOpen = true;
                }

                float scale = this.getChatScale();
                int ceilScale = MathHelper.ceiling_float_int((float)this.getChatWidth() / scale);
                GlStateManager.pushMatrix();
                GlStateManager.translate(2.0F, 20.0F, 0.0F);
                GlStateManager.scale(scale, scale, 1.0F);

                int displayPos = 0;
                int pos;
                int allLines;
                int maxDisplayLines;
                boolean lastEmote = false;

                int scrollPos = this.getScrollPos();

                for(pos = 0; pos + scrollPos < this.lines.size() && displayPos < maxDisplayedLines; ++pos) {
                    ++displayPos;
                    ChatLine line = this.lines.get(pos + scrollPos);
                    if (line != null) {
                        allLines = p_drawChat_1_ - line.getUpdatedCounter();
                        if (allLines < 200 || chatOpen) {
                            double filledPercent = (double)allLines / 200.0D;
                            filledPercent = 1.0D - filledPercent;
                            filledPercent *= 10.0D;
                            filledPercent = MathHelper.clamp_double(filledPercent, 0.0D, 1.0D);
                            filledPercent *= filledPercent;
                            maxDisplayLines = (int)(255.0D * filledPercent);
                            if (chatOpen) {
                                maxDisplayLines = 255;
                            }

                            maxDisplayLines = (int)((float)maxDisplayLines * opacity);
                            ++drawnLines;
                            if (maxDisplayLines > 3) {
                                int x = 0;
                                int y = -displayPos * 9;
                                String text = line.getChatComponent().getFormattedText();

                                int modifier = this.drawLine(this.mc.fontRendererObj, text, (float) x, (float) (y - 8), ceilScale, maxDisplayLines, lastEmote);
                                displayPos += modifier;
                                lastEmote = modifier != 0;
                            }
                        }
                    }
                }

                if (chatOpen) {
                    pos = this.mc.fontRendererObj.FONT_HEIGHT;
                    GlStateManager.translate(-3.0F, 0.0F, 0.0F);
                    int lvt_10_2_ = lineSize * pos + lineSize;
                    allLines = drawnLines * pos + drawnLines;
                    int lvt_12_2_ = this.getScrollPos() * allLines / lineSize;
                    int lvt_13_1_ = allLines * allLines / lvt_10_2_;
                    if (lvt_10_2_ != allLines) {
                        maxDisplayLines = lvt_12_2_ > 0 ? 170 : 96;
                        int lvt_15_2_ = this.isScrolled() ? 13382451 : 3355562;
                        drawRect(0, -lvt_12_2_, 2, -lvt_12_2_ - lvt_13_1_, lvt_15_2_ + (maxDisplayLines << 24));
                        drawRect(2, -lvt_12_2_, 1, -lvt_12_2_ - lvt_13_1_, 13421772 + (maxDisplayLines << 24));
                    }
                }

                GlStateManager.popMatrix();
            }
        }
    }

    private int drawLine(FontRenderer font, String text, float x, float y, int ceilScale, int maxDisplayLines, boolean lastEmote) {
        Collection<ChatLineEntry> entries = ChatLineEntry.parseEntries(text);

        boolean hasEmote = false;
        for (ChatLineEntry entry : entries) {
            if (entry.isEmote()) {
                hasEmote = true;
                break;
            }
        }

        drawRect(
                (int) x,
                (int) (lastEmote && !hasEmote ? y + (Constants.LINE_HEIGHT / 2) : y - (hasEmote ? (Constants.LINE_HEIGHT * 0.5D) + 1 : -Constants.LINE_HEIGHT)),
                (int) x + ceilScale + 4,
                (int) y + (hasEmote ? (int) ((double) Constants.LINE_HEIGHT * 1.5D) : 0),
                maxDisplayLines / 2 << 24
        );
        // TODO: displayed wrong when mixing normal and emote messages

        int rgb = 16777215 + (maxDisplayLines << 24);
        GlStateManager.enableBlend();

        for (ChatLineEntry entry : entries) {
            if (entry.isEmote() && this.drawImage(entry.getContent(), x, y, rgb)) {
                x += Constants.CHAT_EMOTE_SIZE;
            } else {
                this.drawLineComponent(entry.getContent(), x, y, rgb);
                x += font.getStringWidth(entry.getContent());
            }
        }

        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();

        return hasEmote ? 1 : 0;
    }

    private void drawLineComponent(String text, float x, float y, int rgb) {
        this.mc.fontRendererObj.drawStringWithShadow(text, x, y, rgb);
    }

    private boolean drawImage(String emoteId, float x, float y, int rgb) {
        if (emoteId.contains(" ")) {
            return false;
        }

        BTTVEmote emote = new BTTVEmote(emoteId, "");

        // TODO: Check for 404
        ResourceLocation resourceLocation = LabyMod.getInstance().getDynamicTextureManager().getTexture(emoteId, emote.getURL(3));
        Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocation);

        int alpha = (rgb >> 24) & 0xff;

        LabyMod.getInstance().getDrawUtils().drawTexture(x, y - Constants.LINE_HEIGHT, 256, 256, Constants.CHAT_EMOTE_SIZE, Constants.CHAT_EMOTE_SIZE, alpha);

        return true;
    }

}
