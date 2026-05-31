package io.github.legentpc.neu21plus.client.tab;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.skyblock.SBInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TabOverlay {

    private static final Logger LOGGER = LoggerFactory.getLogger(TabOverlay.class);

    private static final TabOverlay INSTANCE = new TabOverlay();

    public static TabOverlay getInstance() {
        return INSTANCE;
    }

    private boolean showing = false;

    private TabOverlay() {
    }

    public void render(GuiGraphicsExtractor context, int screenWidth, int screenHeight) {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config == null || !config.display.customTab) return;

        SBInfo sbInfo = SBInfo.getInstance();
        if (!sbInfo.hasSkyblockScoreboard()) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.getConnection() == null) return;

        if (!client.options.keyPlayerList.isDown()) return;

        List<PlayerInfo> players = new ArrayList<>(client.getConnection().getOnlinePlayers());
        players.sort(Comparator.comparing(p -> p.getProfile() != null ? p.getProfile().name() : ""));

        int entryHeight = client.font.lineHeight + 2;
        int columns = Math.max(1, Math.min(4, (int) Math.ceil(players.size() / 20.0)));
        int columnWidth = 150;
        int maxPerColumn = (int) Math.ceil((double) players.size() / columns);

        int totalWidth = columns * columnWidth;
        int totalHeight = maxPerColumn * entryHeight + 24;

        int x = (screenWidth - totalWidth) / 2;
        int y = Math.max(4, screenHeight / 2 - totalHeight / 2 - 40);

        context.fill(x - 4, y - 4, x + totalWidth + 4, y + totalHeight + 4, 0xC0101010);
        context.outline(x - 4, y - 4, totalWidth + 8, totalHeight + 8, 0xFF555555);

        String title = "\u00a76Hypixel SkyBlock";
        context.text(client.font, title, screenWidth / 2 - client.font.width(title) / 2, y, 0xFFFFAA00, true);
        y += client.font.lineHeight + 4;

        String location = sbInfo.getLocation();
        if (location != null && !location.isEmpty()) {
            String locText = "\u00a77Location: \u00a7f" + location;
            context.text(client.font, locText, screenWidth / 2 - client.font.width(locText) / 2, y, 0xFFAAAAAA, false);
        }
        y += client.font.lineHeight + 4;

        for (int i = 0; i < players.size(); i++) {
            int col = i / maxPerColumn;
            int row = i % maxPerColumn;

            PlayerInfo info = players.get(i);
            String name = info.getProfile() != null ? info.getProfile().name() : "???";
            int ping = info.getLatency();

            String pingStr = ping < 50 ? "\u00a7a" + ping : (ping < 100 ? "\u00a7e" + ping : "\u00a7c" + ping);
            String text = "\u00a7f" + name + " \u00a77[" + pingStr + "\u00a77]";

            int drawX = x + col * columnWidth;
            int drawY = y + row * entryHeight;

            context.text(client.font, text, drawX, drawY, 0xFFE0E0E0, false);
        }

        showing = true;
    }

    public boolean isShowing() {
        return showing;
    }
}
