package io.github.legentpc.neu21plus.client.collection;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.skyblock.SBInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CollectionDisplay {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionDisplay.class);

    private static final CollectionDisplay INSTANCE = new CollectionDisplay();

    public static CollectionDisplay getInstance() {
        return INSTANCE;
    }

    private static final Pattern COLLECTION_PATTERN = Pattern.compile(
            ".*Collection: (.+?) - (\\d+(?:,\\d+)*)"
    );
    private static final Pattern COLLECTION_TIER_PATTERN = Pattern.compile(
            ".*Collection: (.+?) (\\d+)"
    );

    private final Map<String, Long> collections = new HashMap<>();
    private final Map<String, Integer> collectionTiers = new HashMap<>();

    private CollectionDisplay() {
    }

    public void onChatMessage(Component message) {
        String text = message.getString();
        String cleaned = text.replaceAll("\u00a7[0-9a-fk-orA-FK-OR]", "").trim();

        Matcher matcher = COLLECTION_PATTERN.matcher(cleaned);
        if (matcher.find()) {
            String name = matcher.group(1);
            long amount = parseLong(matcher.group(2));
            collections.put(name, amount);
        }

        Matcher tierMatcher = COLLECTION_TIER_PATTERN.matcher(cleaned);
        if (tierMatcher.find()) {
            String name = tierMatcher.group(1);
            int tier = Integer.parseInt(tierMatcher.group(2));
            collectionTiers.put(name, tier);
        }
    }

    private long parseLong(String str) {
        try {
            return Long.parseLong(str.replace(",", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void render(GuiGraphicsExtractor context, int screenWidth, int screenHeight) {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config == null || !config.display.collectionDisplay) return;

        SBInfo sbInfo = SBInfo.getInstance();
        if (!sbInfo.hasSkyblockScoreboard()) return;

        if (collections.isEmpty()) return;

        Minecraft client = Minecraft.getInstance();
        int x = screenWidth - 160;
        int y = 10;
        int lineH = client.font.lineHeight + 2;

        String openChest = sbInfo.getOpenChestName();
        if (openChest == null || !openChest.contains("Collection")) return;

        int panelHeight = Math.min(collections.size(), 12) * lineH + 24;
        context.fill(x - 4, y - 4, x + 156, y + panelHeight, 0x80000000);
        context.outline(x - 4, y - 4, 160, panelHeight, 0xFF555555);

        context.text(client.font, "\u00a76Collections:", x, y, 0xFFFFAA00, true);
        y += lineH + 4;

        int count = 0;
        for (Map.Entry<String, Long> entry : collections.entrySet()) {
            if (count >= 12) {
                context.text(client.font, "\u00a77... and " + (collections.size() - 12) + " more", x, y, 0xFFAAAAAA, false);
                break;
            }

            Integer tier = collectionTiers.get(entry.getKey());
            String tierStr = tier != null ? " \u00a7aT" + tier : "";
            String text = "\u00a7f" + entry.getKey() + ": \u00a77" + formatNumber(entry.getValue()) + tierStr;
            context.text(client.font, text, x + 2, y, 0xFFAAAAAA, false);
            y += lineH;
            count++;
        }
    }

    private String formatNumber(long num) {
        if (num >= 1_000_000_000) return String.format("%.1fB", num / 1_000_000_000.0);
        if (num >= 1_000_000) return String.format("%.1fM", num / 1_000_000.0);
        if (num >= 1_000) return String.format("%.1fK", num / 1_000.0);
        return String.valueOf(num);
    }

    public void reset() {
        collections.clear();
        collectionTiers.clear();
    }

    public Map<String, Long> getCollections() {
        return collections;
    }

    public Map<String, Integer> getCollectionTiers() {
        return collectionTiers;
    }
}
