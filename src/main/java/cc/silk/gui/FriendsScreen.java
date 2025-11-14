package cc.silk.gui;

import cc.silk.utils.friend.FriendManager;
import cc.silk.utils.render.nanovg.NanoVGRenderer;
import cc.silk.utils.render.GuiGlowHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class FriendsScreen extends Screen {
    private static final Color PANEL_BG = new Color(18, 18, 22, 250);
    private static final Color HEADER_BG = new Color(28, 28, 32, 255);
    private static final Color BORDER_COLOR = new Color(50, 50, 60, 255);
    private static final Color TEXT_COLOR = new Color(240, 240, 245, 255);
    private static final Color TEXT_SECONDARY = new Color(160, 160, 170, 255);
    private static final Color FRIEND_COLOR = new Color(100, 255, 120, 255);
    private static final Color HOVER_COLOR = new Color(35, 35, 42, 255);
    private static final Color SEARCH_BG = new Color(25, 25, 30, 255);

    private String searchQuery = "";
    private boolean searchFocused = false;
    private float scrollOffset = 0f;
    private float targetScrollOffset = 0f;
    private List<PlayerEntry> playerEntries = new ArrayList<>();
    private PlayerEntry hoveredPlayer = null;
    private float animationProgress = 0f;
    private long lastFrameTime = System.currentTimeMillis();

    private static final int PANEL_WIDTH = 280;
    private static final int PANEL_HEIGHT = 380;
    private static final int HEADER_HEIGHT = 40;
    private static final int SEARCH_HEIGHT = 32;
    private static final int PLAYER_ENTRY_HEIGHT = 32;
    private static final int PADDING = 10;

    public FriendsScreen() {
        super(Text.literal("Friends"));
    }

    @Override
    protected void init() {
        super.init();
        updatePlayerList();
    }

    private void updatePlayerList() {
        playerEntries.clear();
        if (client == null || client.getNetworkHandler() == null)
            return;

        for (PlayerListEntry entry : client.getNetworkHandler().getPlayerList()) {
            if (entry.getProfile() == null)
                continue;
            String name = entry.getProfile().getName();
            UUID uuid = entry.getProfile().getId();

            if (searchQuery.isEmpty() || name.toLowerCase().contains(searchQuery.toLowerCase())) {
                playerEntries.add(new PlayerEntry(name, uuid));
            }
        }

        playerEntries.sort(Comparator.comparing(p -> p.name));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastFrameTime) / 1000f;
        lastFrameTime = currentTime;

        animationProgress += deltaTime * 6f;
        if (animationProgress > 1f)
            animationProgress = 1f;

        float scale = easeOutBack(animationProgress);
        float alpha = animationProgress;

        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = (height - PANEL_HEIGHT) / 2;

        NanoVGRenderer.beginFrame();

        NanoVGRenderer.save();
        NanoVGRenderer.translate(width / 2f, height / 2f);
        NanoVGRenderer.scale(scale, scale);
        NanoVGRenderer.translate(-width / 2f, -height / 2f);

        int bgAlpha = (int) (250 * alpha);
        Color panelBg = new Color(PANEL_BG.getRed(), PANEL_BG.getGreen(), PANEL_BG.getBlue(), bgAlpha);
        Color borderColor = new Color(BORDER_COLOR.getRed(), BORDER_COLOR.getGreen(), BORDER_COLOR.getBlue(),
                (int) (255 * alpha));

        NanoVGRenderer.drawRoundedRect(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, 10f, panelBg);
        NanoVGRenderer.drawRoundedRectOutline(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, 10f, 1.5f, borderColor);

        renderHeader(panelX, panelY, alpha);
        renderSearchBar(panelX, panelY + HEADER_HEIGHT + PADDING, mouseX, mouseY, alpha);
        renderPlayerList(panelX, panelY + HEADER_HEIGHT + PADDING + SEARCH_HEIGHT + PADDING, mouseX, mouseY, alpha);

        NanoVGRenderer.restore();
        NanoVGRenderer.endFrame();

        GuiGlowHelper.drawGuiGlow(context, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, 10f);
    }

    private float easeOutBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1f;
        return 1f + c3 * (float) Math.pow(t - 1f, 3) + c1 * (float) Math.pow(t - 1f, 2);
    }

    private void renderHeader(int x, int y, float alpha) {
        int headerAlpha = (int) (255 * alpha);
        Color headerBg = new Color(HEADER_BG.getRed(), HEADER_BG.getGreen(), HEADER_BG.getBlue(), headerAlpha);
        Color accentColor = getAccentColor();
        Color accentWithAlpha = new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(),
                headerAlpha);
        Color textColor = new Color(TEXT_COLOR.getRed(), TEXT_COLOR.getGreen(), TEXT_COLOR.getBlue(), headerAlpha);
        Color secondaryColor = new Color(TEXT_SECONDARY.getRed(), TEXT_SECONDARY.getGreen(), TEXT_SECONDARY.getBlue(),
                headerAlpha);

        NanoVGRenderer.drawRoundedRect(x, y, PANEL_WIDTH, HEADER_HEIGHT, 10f, headerBg);
        NanoVGRenderer.drawRect(x, y + HEADER_HEIGHT - 1, PANEL_WIDTH, 1.5f, accentWithAlpha);

        String title = "Friends";
        float titleSize = 14f;
        NanoVGRenderer.drawText(title, x + PADDING, y + 12, titleSize, textColor);

        int friendCount = FriendManager.getFriends().size();
        int totalPlayers = playerEntries.size();
        String stats = friendCount + "/" + totalPlayers;
        float statsSize = 10f;
        float statsWidth = NanoVGRenderer.getTextWidth(stats, statsSize);
        NanoVGRenderer.drawText(stats, x + PANEL_WIDTH - PADDING - statsWidth, y + 14, statsSize, secondaryColor);
    }

    private void renderSearchBar(int x, int y, int mouseX, int mouseY, float alpha) {
        float searchX = x + PADDING;
        float searchY = y;
        float searchWidth = PANEL_WIDTH - PADDING * 2;

        int bgAlpha = (int) (255 * alpha);
        Color accentColor = getAccentColor();
        Color searchBg = new Color(SEARCH_BG.getRed(), SEARCH_BG.getGreen(), SEARCH_BG.getBlue(), bgAlpha);
        Color searchBorder = searchFocused
                ? new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), bgAlpha)
                : new Color(BORDER_COLOR.getRed(), BORDER_COLOR.getGreen(), BORDER_COLOR.getBlue(), bgAlpha);

        NanoVGRenderer.drawRoundedRect(searchX, searchY, searchWidth, SEARCH_HEIGHT, 6f, searchBg);
        NanoVGRenderer.drawRoundedRectOutline(searchX, searchY, searchWidth, SEARCH_HEIGHT, 6f,
                searchFocused ? 1.5f : 1f, searchBorder);

        String displayText = searchQuery.isEmpty() ? "Search..." : searchQuery;
        Color textColor = searchQuery.isEmpty()
                ? new Color(TEXT_SECONDARY.getRed(), TEXT_SECONDARY.getGreen(), TEXT_SECONDARY.getBlue(), bgAlpha)
                : new Color(TEXT_COLOR.getRed(), TEXT_COLOR.getGreen(), TEXT_COLOR.getBlue(), bgAlpha);

        float fontSize = 11f;
        NanoVGRenderer.drawText(displayText, searchX + 10, searchY + 10, fontSize, textColor);

        if (searchFocused && System.currentTimeMillis() % 1000 < 500) {
            float cursorX = searchX + 10 + NanoVGRenderer.getTextWidth(searchQuery, fontSize);
            NanoVGRenderer.drawRect(cursorX, searchY + 9, 1f, fontSize + 2, textColor);
        }
    }

    private void renderPlayerList(int x, int y, int mouseX, int mouseY, float alpha) {
        int listHeight = PANEL_HEIGHT - (y - ((height - PANEL_HEIGHT) / 2)) - PADDING;

        float smoothScroll = scrollOffset + (targetScrollOffset - scrollOffset) * 0.2f;
        scrollOffset = smoothScroll;

        int bgAlpha = (int) (255 * alpha);
        Color accentColor = getAccentColor();

        NanoVGRenderer.save();
        NanoVGRenderer.scissor(x + PADDING, y, PANEL_WIDTH - PADDING * 2, listHeight);

        hoveredPlayer = null;
        float entryY = y - scrollOffset;

        if (playerEntries.isEmpty()) {
            String emptyText = searchQuery.isEmpty() ? "No players" : "Not found";
            float fontSize = 11f;
            Color emptyColor = new Color(TEXT_SECONDARY.getRed(), TEXT_SECONDARY.getGreen(), TEXT_SECONDARY.getBlue(),
                    bgAlpha);
            float textWidth = NanoVGRenderer.getTextWidth(emptyText, fontSize);
            NanoVGRenderer.drawText(emptyText, x + (PANEL_WIDTH - textWidth) / 2f, y + listHeight / 2f - 6, fontSize,
                    emptyColor);
        }

        for (PlayerEntry player : playerEntries) {
            if (entryY + PLAYER_ENTRY_HEIGHT < y) {
                entryY += PLAYER_ENTRY_HEIGHT;
                continue;
            }
            if (entryY > y + listHeight)
                break;

            boolean isHovered = mouseX >= x + PADDING && mouseX <= x + PANEL_WIDTH - PADDING &&
                    mouseY >= entryY && mouseY <= entryY + PLAYER_ENTRY_HEIGHT;
            boolean isFriend = FriendManager.isFriend(player.uuid);

            if (isHovered) {
                hoveredPlayer = player;
                Color hoverColor = new Color(HOVER_COLOR.getRed(), HOVER_COLOR.getGreen(), HOVER_COLOR.getBlue(),
                        bgAlpha);
                NanoVGRenderer.drawRoundedRect(x + PADDING, entryY, PANEL_WIDTH - PADDING * 2,
                        PLAYER_ENTRY_HEIGHT, 6f, hoverColor);
            }

            String playerIcon = isFriend ? "★" : "○";
            float iconSize = 11f;
            Color iconColor = isFriend
                    ? new Color(FRIEND_COLOR.getRed(), FRIEND_COLOR.getGreen(), FRIEND_COLOR.getBlue(), bgAlpha)
                    : new Color(TEXT_SECONDARY.getRed(), TEXT_SECONDARY.getGreen(), TEXT_SECONDARY.getBlue(), bgAlpha);
            NanoVGRenderer.drawText(playerIcon, x + PADDING + 8, entryY + 10, iconSize, iconColor);

            Color nameColor = isFriend
                    ? new Color(FRIEND_COLOR.getRed(), FRIEND_COLOR.getGreen(), FRIEND_COLOR.getBlue(), bgAlpha)
                    : new Color(TEXT_COLOR.getRed(), TEXT_COLOR.getGreen(), TEXT_COLOR.getBlue(), bgAlpha);
            float fontSize = 11f;
            NanoVGRenderer.drawText(player.name, x + PADDING + 25, entryY + 10, fontSize, nameColor);

            entryY += PLAYER_ENTRY_HEIGHT;
        }

        NanoVGRenderer.restore();

        if (playerEntries.size() * PLAYER_ENTRY_HEIGHT > listHeight) {
            renderScrollbar(x + PANEL_WIDTH - PADDING - 8, y, listHeight, alpha);
        }
    }

    private void renderScrollbar(int x, int y, int listHeight, float alpha) {
        int totalHeight = playerEntries.size() * PLAYER_ENTRY_HEIGHT;
        float scrollbarHeight = Math.max(20, (float) listHeight * listHeight / totalHeight);
        float scrollbarY = y + (scrollOffset / totalHeight) * listHeight;

        Color accentColor = getAccentColor();
        Color scrollbarBg = new Color(40, 40, 50, (int) (80 * alpha));
        Color scrollbarColor = new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(),
                (int) (160 * alpha));

        NanoVGRenderer.drawRoundedRect(x, y, 4, listHeight, 2f, scrollbarBg);
        NanoVGRenderer.drawRoundedRect(x, scrollbarY, 4, scrollbarHeight, 2f, scrollbarColor);
    }

    private Color getAccentColor() {
        return cc.silk.module.modules.client.NewClickGUIModule.getAccentColor();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = (height - PANEL_HEIGHT) / 2;

        float searchX = panelX + PADDING;
        float searchY = panelY + 50;
        float searchWidth = PANEL_WIDTH - PADDING * 2;

        if (mouseX >= searchX && mouseX <= searchX + searchWidth &&
                mouseY >= searchY && mouseY <= searchY + SEARCH_HEIGHT) {
            searchFocused = true;
            return true;
        }

        searchFocused = false;

        if (button == 0 && hoveredPlayer != null) {
            FriendManager.toggleFriend(hoveredPlayer.uuid);
            if (client != null && client.player != null) {
                if (FriendManager.isFriend(hoveredPlayer.uuid)) {
                    client.player.sendMessage(Text.literal("§a" + hoveredPlayer.name + " added to friends"), false);
                } else {
                    client.player.sendMessage(Text.literal("§c" + hoveredPlayer.name + " removed from friends"), false);
                }
            }
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (searchFocused) {
            searchQuery += chr;
            updatePlayerList();
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchFocused) {
            if (keyCode == 259) {
                if (!searchQuery.isEmpty()) {
                    searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                    updatePlayerList();
                }
                return true;
            } else if (keyCode == 257 || keyCode == 335) {
                searchFocused = false;
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = (height - PANEL_HEIGHT) / 2;
        int listY = panelY + 50 + SEARCH_HEIGHT + PADDING;
        int listHeight = PANEL_HEIGHT - (listY - panelY) - PADDING;

        if (mouseX >= panelX + PADDING && mouseX <= panelX + PANEL_WIDTH - PADDING &&
                mouseY >= listY && mouseY <= listY + listHeight) {

            int totalHeight = playerEntries.size() * PLAYER_ENTRY_HEIGHT;
            int maxScroll = Math.max(0, totalHeight - listHeight);

            targetScrollOffset -= (float) verticalAmount * 20;
            targetScrollOffset = Math.max(0, Math.min(maxScroll, targetScrollOffset));

            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        if (cc.silk.module.modules.client.ClientSettingsModule.isGuiBlurEnabled()) {
            super.renderBackground(context, mouseX, mouseY, delta);
        }
    }

    private static class PlayerEntry {
        final String name;
        final UUID uuid;

        PlayerEntry(String name, UUID uuid) {
            this.name = name;
            this.uuid = uuid;
        }
    }
}
