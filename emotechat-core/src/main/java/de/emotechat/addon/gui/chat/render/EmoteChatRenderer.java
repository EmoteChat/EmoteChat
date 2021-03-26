package de.emotechat.addon.gui.chat.render;

import de.emotechat.addon.Constants;
import de.emotechat.addon.EmoteChatAddon;
import de.emotechat.addon.bttv.BTTVEmote;
import de.emotechat.addon.gui.ChatLineEntry;
import de.emotechat.addon.gui.emote.EmoteGuiYesNo;
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
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EmoteChatRenderer {

    private static final Field SCROLL_POS_FIELD;
    protected static final Field ANIMATION_SHIFT_FIELD;
    private static final Field TAB_MENU_FIELD;
    private static final Field HOVERING_ROOM_FIELD;
    private static final Field LAST_RENDERED_LINES_COUNT_FIELD;

    private static final Field DOWNLOADED_IMAGE_CONTENT_FIELD;

    private static final int SPACE_LENGTH = LabyModCore.getMinecraft().getFontRenderer().getCharWidth(' ');

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

    private BTTVEmote hoveredEmote;

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

    public boolean addChatLine(String message, boolean secondChat, String room, Object component, int updateCounter, int chatLineId, Integer highlightColor, boolean refresh) {
        Collection<ChatLineEntry> entries = ChatLineEntry.parseEntries(this.addon.getEmoteProvider().getIdSplitter(), message);
        if (entries.stream().noneMatch(ChatLineEntry::isEmote)) {
            return true;
        }

        this.renderer.getChatLines().add(0, new EmoteChatLine(
                entries,
                true,
                message,
                secondChat,
                room,
                component,
                updateCounter,
                chatLineId,
                highlightColor
        ));

        if (!refresh) {
            try {
                EmoteChatRenderer.ANIMATION_SHIFT_FIELD.set(this.renderer, System.currentTimeMillis());
            } catch (IllegalAccessException exception) {
                exception.printStackTrace();
            }
        }

        return false;
    }

    public float getRenderedChatHeight() {
        int totalLinesCount = 0;
        try {
            totalLinesCount = (int) LAST_RENDERED_LINES_COUNT_FIELD.get(this.renderer);
        } catch (IllegalAccessException exception) {
            exception.printStackTrace();
        }

        float textLineHeight = totalLinesCount * LabyModCore.getMinecraft().getFontRenderer().FONT_HEIGHT;

        return (textLineHeight) * this.renderer.getChatScale();
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

        this.hoveredEmote = null;

        List<ChatLine> chatLines = new ArrayList<>(this.renderer.getChatLines());

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
        if (LabyMod.getSettings().chatAnimation) {
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

        int mouseX = (int) ((this.mouseX - posX) / scale);
        int mouseY = (int) ((this.mouseY - posY) / scale);

        int pos = -this.renderer.getScrollPos();

        for (ChatLine chatline : chatLines) {
            if (chatline == null) {
                continue;
            }
            if (!chatline.getRoom().equals(this.manager.getSelectedRoom())) {
                continue;
            }

            boolean firstLine = (pos == -this.renderer.getScrollPos());
            boolean lastLine = (pos == chatLineCount);

            pos++;
            totalMessages++;
            totalHeight += fontHeight;

            if (!lastLine || shift == 0.0D) {
                if (pos > chatLineCount || pos <= 0) {
                    continue;
                }
            }
            int updateCounterDifference = Minecraft.getMinecraft().ingameGUI.getUpdateCounter() - chatline.getUpdateCounter();
            if (updateCounterDifference >= 200 && !chatOpen) {
                continue;
            }

            visibleMessages++;
            visibleHeight += fontHeight;

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

            this.drawLine(LabyModCore.getMinecraft().getFontRenderer(), chatline, x, y, width, alpha, mouseX, mouseY);

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

        IngameChatManager ingameChatManager = IngameChatManager.INSTANCE;

        if (LabyMod.getSettings().chatFilter && TAB_MENU_FIELD.getBoolean(this.renderer) && ingameChatManager.getVisibleRooms().size() > 1) {
            HOVERING_ROOM_FIELD.set(this.renderer, null);
            double roomX = chatOpen ? 2.0D : 1.0D;
            double roomY = chatOpen ? (draw.getHeight() - 27) : (draw.getHeight() - 9);
            for (String roomName : ingameChatManager.getVisibleRooms()) {
                if (roomName == null) {
                    continue;
                }
                Integer unread = ingameChatManager.getRoomsUnread().get(roomName);
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
                if (!LabyMod.getSettings().fastChat) {
                    draw.drawRect(roomX, roomY - (double) (selected ? 1 : -1), roomX + roomWidth, roomY + roomHeight, notificationColor);
                }
                draw.drawString(ModColor.cl(hover ? "e" : (selected ? "f" : "7")) + string, roomX + 1.0D, roomY + 2.0D, 0.7D);
                roomX += roomWidth + 2.0D;
                if (hover) {
                    HOVERING_ROOM_FIELD.set(this.renderer, roomName);
                }
            }
        }

        if (this.hoveredEmote != null) {
            String hoverText = this.hoveredEmote.getName();
            if (!this.addon.getEmoteProvider().isEmoteSaved(this.hoveredEmote)) {
                hoverText += " (Click to add)";
            }
            LabyMod.getInstance().getDrawUtils().drawHoveringText(this.mouseX, this.mouseY, hoverText);
        }
    }

    private void drawLine(FontRenderer font, ChatLine chatLine, float x, float y, int width, int alpha, int mouseX, int mouseY) {
        int rgb = 16777215 + (alpha << 24);

        if (!LabyMod.getSettings().fastChat || chatLine.getHighlightColor() != null) {
            DrawUtils.drawRect(
                    (int) x,
                    (int) y - Constants.LINE_HEIGHT,
                    (int) x + width,
                    (int) y,
                    chatLine.getHighlightColor() != null ? chatLine.getHighlightColor() : (alpha / 2 << 24)
            );
        }

        GlStateManager.enableBlend();

        x += 1;
        y -= 8;

        if (chatLine instanceof EmoteChatLine) {
            EmoteChatLine emoteChatLine = (EmoteChatLine) chatLine;

            if (emoteChatLine.shouldRender()) {
                float textY = emoteChatLine.getEntries().stream().anyMatch(ChatLineEntry::isLoadedEmote)
                        ? y + ((float) Constants.LINE_HEIGHT / 2F)
                        : y;

                for (ChatLineEntry entry : emoteChatLine.getEntries()) {
                    if (entry.isEmote() && !entry.getContent().contains(" ")
                            && this.drawImage(this.addon.getEmoteProvider().getByGlobalIdentifier(entry.getEmoteId()), x, y, alpha)) {

                        if (this.renderer.isChatOpen()
                                && mouseX >= x
                                && mouseX <= (x + Constants.CHAT_EMOTE_SIZE)
                                && mouseY >= y
                                && mouseY <= (y + Constants.CHAT_EMOTE_SIZE)) {

                            BTTVEmote emote = this.addon.getEmoteProvider().getByGlobalIdentifier(entry.getEmoteId());

                            if (emote.isComplete()) {
                                this.hoveredEmote = emote;
                            }
                        }

                        if (!entry.isLoadedEmote()) {
                            entry.setLoadedEmote(true);

                            if (!emoteChatLine.isGhostLineAdded()) {
                                int currentLineIndex = this.renderer.getChatLines().indexOf(emoteChatLine);
                                this.renderer.getChatLines().add(currentLineIndex, new EmoteChatLine(
                                        emoteChatLine.getEntries(),
                                        false,
                                        emoteChatLine.getMessage(),
                                        emoteChatLine.isSecondChat(),
                                        emoteChatLine.getRoom(),
                                        emoteChatLine.getComponent(),
                                        emoteChatLine.getUpdateCounter(),
                                        emoteChatLine.getChatLineId(),
                                        emoteChatLine.getHighlightColor()
                                ));

                                emoteChatLine.setGhostLineAdded(true);
                            }
                        }

                        x += Constants.CHAT_EMOTE_SIZE;
                    } else {
                        String content = entry.getColors() + entry.getContent();
                        this.drawLineComponent(content, x, textY, rgb);
                        x += font.getStringWidth(content);
                    }
                    x += SPACE_LENGTH;
                }
            }
        } else {
            this.drawLineComponent(chatLine.getMessage(), x + 1, y, rgb);
        }

        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
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

    public void handleClicked(GuiChat lastGuiChat) {
        if (this.hoveredEmote != null) {
            if (this.addon.getEmoteProvider().isEmoteSaved(this.hoveredEmote)) {
                return;
            }

            // this can't be a lambda because it causes issues with the obfuscation mappings on Forge
            @SuppressWarnings("Convert2Lambda") GuiScreen gui = new EmoteGuiYesNo(this.hoveredEmote, new GuiYesNoCallback() {
                @Override
                public void confirmClicked(boolean accepted, int id) {
                    if (accepted) {
                        addon.getEmoteProvider().addEmote(hoveredEmote, hoveredEmote.getName());
                        LabyMod.getInstance().displayMessageInChat("§7The emote was successfully added to your local emotes");
                    }
                    Minecraft.getMinecraft().displayGuiScreen(lastGuiChat);
                }
            }, 0);
            Minecraft.getMinecraft().displayGuiScreen(gui);
        }
    }

}
