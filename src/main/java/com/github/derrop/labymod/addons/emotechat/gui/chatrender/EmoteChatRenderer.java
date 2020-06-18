package com.github.derrop.labymod.addons.emotechat.gui.chatrender;

import com.github.derrop.labymod.addons.emotechat.Constants;
import com.github.derrop.labymod.addons.emotechat.EmoteChatAddon;
import com.github.derrop.labymod.addons.emotechat.bttv.BTTVEmote;
import com.github.derrop.labymod.addons.emotechat.gui.ChatLineEntry;
import net.labymod.core.LabyModCore;
import net.labymod.ingamechat.IngameChatManager;
import net.labymod.ingamechat.renderer.ChatLine;
import net.labymod.ingamechat.renderer.ChatRenderer;
import net.labymod.main.LabyMod;
import net.labymod.main.lang.LanguageManager;
import net.labymod.utils.DrawUtils;
import net.labymod.utils.ModColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

// TODO: wrong line heights
public class EmoteChatRenderer {

    private static final Field SCROLL_POS_FIELD;
    private static final Field ANIMATION_SHIFT_FIELD;
    private static final Field TAB_MENU_FIELD;
    private static final Field HOVERING_ROOM_FIELD;
    private static final Field LAST_RENDERED_LINES_COUNT_FIELD;

    private static final int SPACE_LENGTH = Minecraft.getMinecraft().fontRendererObj.getStringWidth(" ");

    static {
        Field scrollPos = null;
        Field animationShift = null;
        Field tabMenu = null;
        Field hoveringRoom = null;
        Field lastRenderedLinesCount = null;

        try {
            scrollPos = ChatRenderer.class.getDeclaredField("scrollPos");
            scrollPos.setAccessible(true);

            animationShift = ChatRenderer.class.getDeclaredField("animationShift");
            animationShift.setAccessible(true);

            tabMenu = ChatRenderer.class.getDeclaredField("tabMenu");
            tabMenu.setAccessible(true);

            hoveringRoom = ChatRenderer.class.getDeclaredField("hoveringRoom");
            hoveringRoom.setAccessible(true);

            lastRenderedLinesCount = ChatRenderer.class.getDeclaredField("lastRenderedLinesCount");
            lastRenderedLinesCount.setAccessible(true);
        } catch (NoSuchFieldException exception) {
            exception.printStackTrace();
        }

        SCROLL_POS_FIELD = scrollPos;
        ANIMATION_SHIFT_FIELD = animationShift;
        TAB_MENU_FIELD = tabMenu;
        HOVERING_ROOM_FIELD = hoveringRoom;
        LAST_RENDERED_LINES_COUNT_FIELD = lastRenderedLinesCount;
    }

    private final IngameChatManager manager;
    private final EmoteChatAddon addon;
    private final ChatRenderer renderer;
    private final EmoteChatRendererType type;

    public EmoteChatRenderer(EmoteChatRendererType renderer, IngameChatManager manager, EmoteChatAddon addon) {
        this.renderer = (ChatRenderer) renderer;
        this.type = renderer;
        this.manager = manager;
        this.addon = addon;
    }

    public void renderChat(int updateCounter) {
        if (!this.addon.isEnabled()) {
            this.type.renderDefault(updateCounter);
            return;
        }

        try {
            this.renderChat();
        } catch (IllegalAccessException exception) {
            exception.printStackTrace();
        }
    }

    private void renderChat() throws IllegalAccessException {
        List<ChatLine> chatLines = this.renderer.getChatLines();

        if (chatLines.size() == 0 || !this.renderer.isVisible())
            return;
        GlStateManager.pushMatrix();
        DrawUtils draw = LabyMod.getInstance().getDrawUtils();
        int fontHeight = (LabyModCore.getMinecraft().getFontRenderer()).FONT_HEIGHT;
        float scale = this.renderer.getChatScale();
        int chatLineCount = this.renderer.getLineCount();
        boolean chatOpen = this.renderer.isChatOpen();
        float opacity = this.renderer.getChatOpacity() * 0.9F + 0.1F;
        int width = this.renderer.getVisualWidth() + 1;
        int visibleMessages = 0;
        double totalMessages = 0.0D;
        double animationSpeed = 20.0D;
        float lineHeight = 10.0F * scale;
        double shift = 0.0D;
        if ((LabyMod.getSettings()).chatAnimation) {
            shift = (System.currentTimeMillis() - lineHeight * animationSpeed - ANIMATION_SHIFT_FIELD.getLong(this.renderer)) / animationSpeed;
            if (shift > 0.0D)
                shift = 0.0D;
        }
        double posX = this.renderer.getChatPositionX() - (float) (this.renderer.isRightBound() ? width : 0) * scale;
        double posY = this.renderer.getChatPositionY() - shift;
        GlStateManager.translate(posX, posY, 0.0D);
        GlStateManager.scale(scale, scale, 1.0F);
        if (!this.renderer.isChatOpen()) {
            SCROLL_POS_FIELD.setInt(this.renderer, 0);
        }
        int pos = -this.renderer.getScrollPos();
        int displayPos = pos;

        for (ChatLine chatline : chatLines) {
            if (chatline == null) {
                continue;
            }
            if (!chatline.getRoom().equals(this.manager.getSelectedRoom())) {
                continue;
            }
            boolean firstLine = (pos == -this.renderer.getScrollPos());
            boolean lastLine = (displayPos == chatLineCount);

            pos++;
            displayPos++;
            totalMessages++;

            if (!lastLine || shift == 0.0D) {
                if (displayPos > chatLineCount || pos <= 0) {
                    continue;
                }
            }
            int updateCounterDifference = (Minecraft.getMinecraft()).ingameGUI.getUpdateCounter() - chatline.getUpdateCounter();
            if (updateCounterDifference >= 200 && !chatOpen) {
                continue;
            }
            visibleMessages++;
            int alpha = 255;
            if (!chatOpen) {
                double percent = updateCounterDifference / 200.0D;
                percent = 1.0D - percent;
                percent *= 10.0D;
                percent = LabyModCore.getMath().clamp_double(percent, 0.0D, 1.0D);
                percent *= percent;
                alpha = (int) (255.0D * percent);
            }
            if (shift != 0.0D && firstLine) {
                double fadeIn = 25.5D * -shift;
                alpha = (int) (255.0D - fadeIn);
            }
            if (shift != 0.0D && lastLine) {
                double fadeIn = 25.5D * -shift;
                alpha = (int) fadeIn;
            }
            alpha = (int) (alpha * opacity);
            if (alpha <= 3) {
                continue;
            }
            int x = 0;
            int y = (displayPos - 1) * -9;

            int modifier = this.drawLine(Minecraft.getMinecraft().fontRendererObj, chatline, x, y, width, fontHeight, alpha);
            displayPos += modifier;

            LAST_RENDERED_LINES_COUNT_FIELD.setInt(this.renderer, visibleMessages);
        }
        if (chatOpen) {
            double totalHeight = totalMessages * fontHeight;
            double visibleHeight = (visibleMessages * fontHeight);
            double yStart = this.renderer.getScrollPos() * visibleHeight / totalMessages;
            double yEnd = visibleHeight * visibleHeight / totalHeight;
            double xStart = this.renderer.isRightBound() ? width : -1.0D;
            double xEnd = this.renderer.isRightBound() ? (width + 1) : 0.0D;
            if (totalHeight != visibleHeight)
                draw.drawRect(xStart, -yStart, xEnd, -yStart - yEnd, -1);
            if (this.renderer.moving) {
                double midY = yStart - visibleHeight / 2.0D;
                double x = (-this.renderer.getChatPositionX() / scale);
                double y = (-this.renderer.getChatPositionY() / scale);
                float percentX = this.renderer.getChatPercentX();
                float percentY = this.renderer.getChatPercentY();
                if (this.renderer.isRightBound()) {
                    if (percentX < 98.0F) {
                        draw.drawRect(this.renderer.getVisualWidth(), midY, x + (draw.getWidth() / scale) + width, midY + 1.0D, Color.YELLOW.getRGB());
                        draw.drawRightString(ModColor.cl('e') + (int) (100.0F - percentX) + "%", x + ((draw.getWidth() - 1) / scale) + width, midY - 10.0D);
                    }
                } else if (percentX > 2.0F) {
                    draw.drawRect(x, midY, 0.0D, midY + 1.0D, Color.YELLOW.getRGB());
                    draw.drawString(ModColor.cl('e') + (int) percentX + "%", x + 1.0D, midY - 10.0D);
                }
                if (percentY > 50.0F) {
                    if (percentY < 98.0F) {
                        draw.drawRect(width / 2.0D, 0.0D, width / 2.0D + 1.0D, y + ((draw.getHeight() - 28) / scale), Color.YELLOW.getRGB());
                        draw.drawString(ModColor.cl('e') + (int) (100.0F - percentY) + "%", width / 2.0D + 4.0D, y + ((draw.getHeight() - 28) / scale) - 7.0D);
                    }
                } else if (percentY > 2.0F) {
                    draw.drawRect(width / 2.0D, y, width / 2.0D + 1.0D, -visibleHeight, Color.YELLOW.getRGB());
                    draw.drawString(ModColor.cl('e') + (int) percentY + "%", width / 2.0D + 4.0D, y + 2.0D);
                }
            }
        }
        GlStateManager.popMatrix();
        if ((LabyMod.getSettings()).chatFilter && TAB_MENU_FIELD.getBoolean(this.renderer) && LabyMod.getInstance().getIngameChatManager().getVisibleRooms().size() > 1) {
            HOVERING_ROOM_FIELD.set(this.renderer, null);
            double roomX = chatOpen ? 2.0D : 1.0D;
            double roomY = chatOpen ? (draw.getHeight() - 27) : (draw.getHeight() - 9);
            for (String roomName : LabyMod.getInstance().getIngameChatManager().getVisibleRooms()) {
                if (roomName == null) {
                    continue;
                }
                Integer unread = LabyMod.getInstance().getIngameChatManager().getRoomsUnread().get(roomName);
                if (unread == null) {
                    unread = 0;
                }
                if (!chatOpen && unread <= 0) {
                    continue;
                }
                int notificationColor = Integer.MIN_VALUE;
                boolean selected = roomName.equals(this.manager.getSelectedRoom());
                if (roomName.equals("Global")) {
                    roomName = LanguageManager.translate("ingame_chat_room_global");
                }
                String string = (unread > 0) ? (roomName + ModColor.cl("a") + " [" + unread + "]") : roomName;
                double roomWidth = draw.getStringWidth(string) * 0.7D + 2.0D;
                double roomHeight = 8.0D;
                boolean hover = ((this.renderer.lastMouseX - 2) >= roomX && (this.renderer.lastMouseX - 2) < roomX + roomWidth && posY - this.renderer.lastMouseY < 0.0D && posY - this.renderer.lastMouseY > -roomHeight);
                if (!(LabyMod.getSettings()).fastChat) {
                    draw.drawRect(roomX, roomY - (double) (selected ? 1 : -1), roomX + roomWidth, roomY + roomHeight, notificationColor);
                }
                draw.drawString(ModColor.cl(hover ? "e" : (selected ? "f" : "7")) + string, roomX + 1.0D, roomY + 2.0D, 0.7D);
                roomX += roomWidth + 2.0D;
                if (hover) {
                    HOVERING_ROOM_FIELD.set(this.renderer, roomName);
                }
            }
        }
    }

    private int drawLine(FontRenderer font, ChatLine chatLine, float x, float y, int width, int fontHeight, int alpha) {
        int rgb = 16777215 + (alpha << 24);

        Collection<ChatLineEntry> entries = ChatLineEntry.parseEntries(chatLine.getMessage());

        boolean hasEmote = false;
        for (ChatLineEntry entry : entries) {
            if (entry.isEmote()) {
                hasEmote = true;
                break;
            }
        }

        if (!(LabyMod.getSettings()).fastChat || chatLine.getHighlightColor() != null) {
            DrawUtils.drawRect(
                    (int) x,
                    (int) (!hasEmote ? y + (Constants.LINE_HEIGHT / 2) : y - (hasEmote ? (Constants.LINE_HEIGHT * 0.5D) + 1 : -Constants.LINE_HEIGHT)),
                    (int) x + width,
                    (int) y + (hasEmote ? (int) ((double) Constants.LINE_HEIGHT * 1.5D) : 0),
                    /*x,
                    y - fontHeight,
                    x + width,
                    y,*/
                    (chatLine.getHighlightColor() != null) ? chatLine.getHighlightColor() : (alpha / 2 << 24)
            );
        }

        GlStateManager.enableBlend();

        y -= 8;
        x += 1;

        if (hasEmote) {
            y += (float) Constants.LINE_HEIGHT / 2F;
        }

        for (ChatLineEntry entry : entries) {
            if (entry.isEmote() && this.drawImage(entry.getContent(), x, y, alpha)) {
                x += Constants.CHAT_EMOTE_SIZE;
            } else {
                this.drawLineComponent(entry.getContent(), x, y, rgb);
                x += font.getStringWidth(entry.getContent());
            }
            x += SPACE_LENGTH;
        }

        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();

        return hasEmote ? 1 : 0;
    }

    private void drawLineComponent(String text, float x, float y, int rgb) {
        LabyMod.getInstance().getDrawUtils().drawStringWithShadow(text, x, y, rgb);
    }

    private boolean drawImage(String emoteId, float x, float y, int alpha) {
        if (emoteId.contains(" ")) {
            return false;
        }

        BTTVEmote emote = new BTTVEmote(emoteId, "");

        // TODO: Check for 404
        ResourceLocation resourceLocation = LabyMod.getInstance().getDynamicTextureManager().getTexture(emoteId, emote.getURL(3));
        Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocation);

        LabyMod.getInstance().getDrawUtils().drawTexture(x, y - ((float) Constants.LINE_HEIGHT / 2F), 256, 256, Constants.CHAT_EMOTE_SIZE, Constants.CHAT_EMOTE_SIZE, alpha);

        return true;
    }

}
