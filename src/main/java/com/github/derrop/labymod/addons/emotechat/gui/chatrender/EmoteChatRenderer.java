package com.github.derrop.labymod.addons.emotechat.gui.chatrender;

import com.github.derrop.labymod.addons.emotechat.Constants;
import com.github.derrop.labymod.addons.emotechat.EmoteChatAddon;
import com.github.derrop.labymod.addons.emotechat.bttv.BTTVEmote;
import com.github.derrop.labymod.addons.emotechat.gui.ChatLineEntry;
import com.github.derrop.labymod.addons.emotechat.gui.emote.EmoteGuiYesNo;
import net.labymod.core.LabyModCore;
import net.labymod.ingamechat.IngameChatManager;
import net.labymod.ingamechat.renderer.ChatLine;
import net.labymod.ingamechat.renderer.ChatRenderer;
import net.labymod.main.LabyMod;
import net.labymod.main.lang.LanguageManager;
import net.labymod.utils.DrawUtils;
import net.labymod.utils.ModColor;
import net.labymod.utils.texture.ThreadDownloadTextureImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

public class EmoteChatRenderer {

    private static final Field SCROLL_POS_FIELD;
    private static final Field ANIMATION_SHIFT_FIELD;
    private static final Field TAB_MENU_FIELD;
    private static final Field HOVERING_ROOM_FIELD;
    private static final Field LAST_RENDERED_LINES_COUNT_FIELD;

    private static final Field DOWNLOADED_IMAGE_CONTENT_FIELD;

    private static final int SPACE_LENGTH = Minecraft.getMinecraft().fontRendererObj.getStringWidth(" ");

    static {
        Field scrollPos = null;
        Field animationShift = null;
        Field tabMenu = null;
        Field hoveringRoom = null;
        Field lastRenderedLinesCount = null;

        Field downloadedImageContentField = null;

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

            downloadedImageContentField = ThreadDownloadTextureImage.class.getDeclaredField("bufferedImage");
            downloadedImageContentField.setAccessible(true);
        } catch (NoSuchFieldException exception) {
            exception.printStackTrace();
        }

        SCROLL_POS_FIELD = scrollPos;
        ANIMATION_SHIFT_FIELD = animationShift;
        TAB_MENU_FIELD = tabMenu;
        HOVERING_ROOM_FIELD = hoveringRoom;
        LAST_RENDERED_LINES_COUNT_FIELD = lastRenderedLinesCount;

        DOWNLOADED_IMAGE_CONTENT_FIELD = downloadedImageContentField;
    }

    private final IngameChatManager manager;
    private final EmoteChatAddon addon;
    private final ChatRenderer renderer;
    private final EmoteChatRendererType type;

    private int mouseX;
    private int mouseY;
    
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

    public float getChatPositionY() {
        double screenHeight = LabyMod.getInstance().getDrawUtils().getHeight() - 28;
        float percent = this.renderer.getChatPercentY();
        if (percent > 99.0F) {
            return (float) screenHeight;
        } else {
            return percent < 50.0F ? (float) ((double) (this.getRenderedChatHeight() + 2.0F) + screenHeight / 100.0D * (double) percent) : (float) (screenHeight / 100.0D * (double) percent);
        }
    }

    public float getRenderedChatHeight() {
        int totalLinesCount = 0;
        try {
            totalLinesCount = (int) LAST_RENDERED_LINES_COUNT_FIELD.get(this.renderer);
        } catch (IllegalAccessException exception) {
            exception.printStackTrace();
        }

        int emoteLinesCount = 0;
        for (int i = 0; i < totalLinesCount; i++) {
            ChatLine chatLine = this.renderer.getChatLines().get(totalLinesCount - i - 1);

            Collection<ChatLineEntry> entries = ChatLineEntry.parseEntries(chatLine.getMessage());
            boolean hasEmote = entries.stream().anyMatch(entry -> entry.isEmote() && this.isTextureDownloaded(entry.getAsEmote().getTextureLocation()));

            if (hasEmote) {
                emoteLinesCount++;
            }
        }

        float textLineHeight = (totalLinesCount - emoteLinesCount) * LabyModCore.getMinecraft().getFontRenderer().FONT_HEIGHT;
        float emoteLineHeight = emoteLinesCount * Constants.CHAT_EMOTE_SIZE;

        return (textLineHeight + emoteLineHeight) * this.renderer.getChatScale();
    }

    public boolean renderHoveringResizeX(boolean forceRender) {
        if (this.renderer.getChatLines().size() != 0 && LabyMod.getSettings().scalableChat) {
            float x = this.renderer.getChatPositionX();
            float y = this.getChatPositionY();
            float scale = this.renderer.getChatScale();
            float width = (float) this.renderer.getVisualWidth() * scale;
            float height = this.getRenderedChatHeight();
            float thickness = 2.0F;
            boolean hoverY = (float) this.mouseY < y && (float) this.mouseY > y - height;
            boolean hover;
            if (this.renderer.isRightBound()) {
                hover = (float) this.mouseX < x - width + thickness && (float) this.mouseX > x - width - thickness && hoverY;
            } else {
                hover = (float) this.mouseX > x + width - thickness && (float) this.mouseX < x + width + thickness && hoverY;
            }

            if (hover || forceRender) {
                DrawUtils draw = LabyMod.getInstance().getDrawUtils();
                draw.drawString("|||", this.mouseX - 2, this.mouseY - 2);
                if (this.renderer.isRightBound()) {
                    LabyMod.getInstance().getDrawUtils().drawRect(x - width - 1.0F, y, x - width, y - height, 2147483647);
                } else {
                    LabyMod.getInstance().getDrawUtils().drawRect(x + width, y, x + width + 1.0F, y - height, 2147483647);
                }
            }

            return hover;
        } else {
            return false;
        }
    }

    public boolean renderHoveringResizeY(boolean forceRender) {
        if (this.renderer.getChatLines().size() != 0 && LabyMod.getSettings().scalableChat) {
            float x = this.renderer.getChatPositionX();
            float y = this.getChatPositionY();
            float scale = this.renderer.getChatScale();
            float width = (float) this.renderer.getVisualWidth() * scale;
            float height = forceRender ? this.renderer.getLineCount() * LabyModCore.getMinecraft().getFontRenderer().FONT_HEIGHT * scale : this.getRenderedChatHeight();
            float thickness = 2.0F;
            boolean hoverY = (float) this.mouseY > y - height - thickness && (float) this.mouseY < y - height;
            boolean hover;
            if (this.renderer.isRightBound()) {
                hover = (float) this.mouseX < x && (float) this.mouseX > x - width && hoverY;
            } else {
                hover = (float) this.mouseX > x && (float) this.mouseX < x + width && hoverY;
            }
            if (hover || forceRender) {
                DrawUtils draw = LabyMod.getInstance().getDrawUtils();
                draw.drawString("==", this.mouseX - 5, this.mouseY - 3);
                if (this.renderer.isRightBound()) {
                    LabyMod.getInstance().getDrawUtils().drawRect(x - width, y - height - 1.0F, x, y - height, 2147483647);
                } else {
                    LabyMod.getInstance().getDrawUtils().drawRect(x, y - height - 1.0F, x + width, y - height, 2147483647);
                }
            }

            return hover;
        } else {
            return false;
        }
    }

    private void renderChat() throws IllegalAccessException {
        DrawUtils draw = LabyMod.getInstance().getDrawUtils();

        this.mouseX = this.renderer.lastMouseX;
        this.mouseY = this.renderer.lastMouseY;

        List<ChatLine> chatLines = this.renderer.getChatLines();

        if (chatLines.size() == 0 || !this.renderer.isVisible()) {
            return;
        }

        GlStateManager.pushMatrix();

        int fontHeight = LabyModCore.getMinecraft().getFontRenderer().FONT_HEIGHT;

        float scale = this.renderer.getChatScale();
        int chatLineCount = this.renderer.getLineCount();
        boolean chatOpen = this.renderer.isChatOpen();
        float opacity = this.renderer.getChatOpacity() * 0.9F + 0.1F;
        int width = this.renderer.getVisualWidth() + 1;

        int visibleMessages = 0;
        double totalMessages = 0.0D;

        double totalHeight = 0.0D;
        double visibleHeight = 0.0D;

        double animationSpeed = 20.0D;
        float lineHeight = 10.0F * scale;
        double shift = 0.0D;
        if ((LabyMod.getSettings()).chatAnimation) {
            shift = (System.currentTimeMillis() - lineHeight * animationSpeed - ANIMATION_SHIFT_FIELD.getLong(this.renderer)) / animationSpeed;
            if (shift > 0.0D) {
                shift = 0.0D;
            }
        }
        double posX = this.renderer.getChatPositionX() - (float) (this.renderer.isRightBound() ? width : 0) * scale;
        double posY = this.getChatPositionY() - shift;
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

            Collection<ChatLineEntry> entries = ChatLineEntry.parseEntries(chatline.getMessage());
            boolean hasEmote = entries.stream().anyMatch(entry -> entry.isEmote() && this.isTextureDownloaded(entry.getAsEmote().getTextureLocation()));

            boolean firstLine = (pos == -this.renderer.getScrollPos());
            boolean lastLine = (displayPos == chatLineCount);

            pos++;
            displayPos++;

            totalMessages++;
            totalHeight += hasEmote ? Constants.CHAT_EMOTE_SIZE : fontHeight;

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
            visibleHeight += hasEmote ? Constants.CHAT_EMOTE_SIZE : fontHeight;

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
            int y = (pos - 1) * -9;

            int modifier = this.drawLine(Minecraft.getMinecraft().fontRendererObj, chatline, entries, hasEmote, x, y, width, alpha);
            pos += modifier;

            LAST_RENDERED_LINES_COUNT_FIELD.setInt(this.renderer, visibleMessages);
        }
        if (chatOpen) {
            double yStart = this.renderer.getScrollPos() * visibleHeight / totalMessages;
            double yEnd = visibleHeight * visibleHeight / totalHeight;
            double xStart = this.renderer.isRightBound() ? width : -1.0D;
            double xEnd = this.renderer.isRightBound() ? (width + 1) : 0.0D;
            if (totalHeight != visibleHeight) {
                draw.drawRect(xStart, -yStart, xEnd, -yStart - yEnd, -1);
            }
            if (this.renderer.moving) {
                double midY = yStart - visibleHeight / 2.0D;
                double x = (-this.renderer.getChatPositionX() / scale);
                double y = (-this.getChatPositionY() / scale);
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
                boolean hover = ((this.mouseX - 2) >= roomX && (this.mouseX - 2) < roomX + roomWidth && posY - this.mouseY < 0.0D && posY - this.mouseY > -roomHeight);
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

        ChatLineEntry hovered = this.getHoveredEmote();
        if (hovered != null) {
            BTTVEmote emote = hovered.getAsEmote();
            if (emote.isComplete()) {
                // TODO hover y not working
                ScaledResolution scaledResolution = LabyMod.getInstance().getDrawUtils().getScaledResolution();
                int scaleFactor = scaledResolution.getScaleFactor();
                float chatScale = this.renderer.getChatScale();

                int mouseX = Mouse.getX() / scaleFactor - 3;
                int mouseY = Mouse.getY() / scaleFactor - 27;

                mouseX = MathHelper.floor_float((float) mouseX / chatScale);
                mouseY = MathHelper.floor_float((float) mouseY / chatScale);

                String hoverText = emote.getName();
                if (!this.addon.isEmoteSaved(emote)) {
                    hoverText += " (Click to add)";
                }
                LabyMod.getInstance().getDrawUtils().drawHoveringText(mouseX, mouseY, hoverText);
            }
        }
    }

    private int drawLine(FontRenderer font, ChatLine chatLine, Collection<ChatLineEntry> entries, boolean hasEmote, float x, float y, int width, int alpha) {
        int rgb = 16777215 + (alpha << 24);

        if (!(LabyMod.getSettings()).fastChat || chatLine.getHighlightColor() != null) {
            DrawUtils.drawRect(
                    (int) x,
                    (int) y - Constants.LINE_HEIGHT * (hasEmote ? 2 : 1),
                    (int) x + width,
                    (int) y,
                    (chatLine.getHighlightColor() != null) ? chatLine.getHighlightColor() : (alpha / 2 << 24)
            );
        }

        GlStateManager.enableBlend();

        y -= 8;
        x += 1;

        if (hasEmote) {
            y -= Constants.LINE_HEIGHT;
        }

        for (ChatLineEntry entry : entries) {
            BTTVEmote emote = entry.getAsEmote();

            if (entry.isEmote() && !entry.getContent().contains(" ") && this.drawImage(emote, x, y, alpha)) {
                x += Constants.CHAT_EMOTE_SIZE;
            } else {
                String content = entry.getContent();
                if (entry.isEmote()) {
                    content = Constants.EMOTE_WRAPPER + content + Constants.EMOTE_WRAPPER;
                }
                content = entry.getColors() + content;
                this.drawLineComponent(content, x, hasEmote ? y + (Constants.LINE_HEIGHT / 2f) : y, rgb);
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

    private boolean drawImage(BTTVEmote emote, float x, float y, int alpha) {
        ResourceLocation emoteTexture = emote.getTextureLocation();

        if (!this.isTextureDownloaded(emoteTexture)) {
            return false;
        }

        Minecraft.getMinecraft().getTextureManager().bindTexture(emoteTexture);

        GlStateManager.color(1F, 1F, 1F, alpha / 255F);
        LabyMod.getInstance().getDrawUtils().drawTexture(x, y, 256, 256, Constants.CHAT_EMOTE_SIZE, Constants.CHAT_EMOTE_SIZE, alpha);

        return true;
    }

    private boolean isTextureDownloaded(ResourceLocation resourceLocation) {
        ITextureObject textureObject = Minecraft.getMinecraft().getTextureManager().getTexture(resourceLocation);

        if (textureObject instanceof ThreadDownloadTextureImage) {
            ThreadDownloadTextureImage downloadedTexture = (ThreadDownloadTextureImage) textureObject;

            try {
                if (DOWNLOADED_IMAGE_CONTENT_FIELD.get(downloadedTexture) == null) {
                    return false;
                }
            } catch (IllegalAccessException exception) {
                exception.printStackTrace();
            }
        }

        return true;
    }

    public ChatLineEntry getHoveredEmote() {
        ScaledResolution scaledResolution = LabyMod.getInstance().getDrawUtils().getScaledResolution();
        int scaleFactor = scaledResolution.getScaleFactor();
        float chatScale = this.renderer.getChatScale();

        int mouseX = Mouse.getX() / scaleFactor - 3;
        int mouseY = Mouse.getY() / scaleFactor - 27;

        mouseX = MathHelper.floor_float((float) mouseX / chatScale);
        mouseY = MathHelper.floor_float((float) mouseY / chatScale);

        return this.getHoveredEmote(mouseX, mouseY);
    }

    public ChatLineEntry getHoveredEmote(int mouseX, int mouseY) {
        if (!this.renderer.isChatOpen()) {
            return null;
        }

        if (mouseX >= 0 && mouseY >= 0) {
            int lineCount = Math.min(this.renderer.getLineCount(), this.renderer.getChatLines().size());

            if (mouseX <= MathHelper.floor_float(this.renderer.getChatWidth() / this.renderer.getChatScale()) && mouseY < Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * lineCount + lineCount) {
                int pos = mouseY / Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT + this.renderer.getScrollPos();
                // TODO not working with multiple emotes in the chat because not every line has the same height, with emotes a line has twice the normal height

                if (pos >= 0 && pos < this.renderer.getChatLines().size()) {
                    ChatLine[] chatLines = new ChatLine[]{
                            this.renderer.getChatLines().get(pos),
                            pos > 0 ? this.renderer.getChatLines().get(pos - 1) : null
                    };

                    for (ChatLine chatLine : chatLines) {
                        if (chatLine == null) {
                            continue;
                        }

                        int currentX = 0;

                        Collection<ChatLineEntry> entries = ChatLineEntry.parseEntries(chatLine.getMessage());
                        for (ChatLineEntry entry : entries) {
                            if (!entry.isEmote()) {
                                currentX += Minecraft.getMinecraft().fontRendererObj.getStringWidth(entry.getContent()) + SPACE_LENGTH;
                                continue;
                            }

                            currentX += Constants.CHAT_EMOTE_SIZE;

                            if (mouseX >= currentX - Constants.CHAT_EMOTE_SIZE && mouseX <= currentX) {
                                return entry;
                            }

                            currentX += SPACE_LENGTH;
                        }
                    }
                }
            }
        }

        return null;
    }

    public void handleClicked(GuiChat lastGuiChat) {
        ChatLineEntry entry = this.getHoveredEmote();
        if (entry != null) {
            BTTVEmote emote = entry.getAsEmote();
            if (this.addon.isEmoteSaved(emote)) {
                return;
            }
            GuiScreen gui = new EmoteGuiYesNo(emote, (accepted, id) -> {
                if (accepted) {
                    this.addon.addEmote(emote, emote.getName());
                    LabyMod.getInstance().displayMessageInChat("§7The emote was successfully added to your local emotes");
                }
                Minecraft.getMinecraft().displayGuiScreen(lastGuiChat);
            }, 0);
            Minecraft.getMinecraft().displayGuiScreen(gui);
        }
    }

}
