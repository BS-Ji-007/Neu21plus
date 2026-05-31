package io.github.legentpc.neu21plus.client.bazaar;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.api.APIManager;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.itemrepo.ItemRepo;
import io.github.legentpc.neu21plus.skyblock.SBInfo;
import io.github.legentpc.neu21plus.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BazaarHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(BazaarHelper.class);

    private static final BazaarHelper INSTANCE = new BazaarHelper();

    public static BazaarHelper getInstance() {
        return INSTANCE;
    }

    private static final Pattern BAZAAR_ITEM_PATTERN = Pattern.compile(
            ".*Buy (\\d+)x (.+).*"
    );

    private boolean inBazaar = false;
    private final Map<String, Double> previousPrices = new HashMap<>();
    private final List<PriceAlert> activeAlerts = new ArrayList<>();
    private int tickCount = 0;

    private BazaarHelper() {
    }

    public void tick() {
        tickCount++;

        if (tickCount % 100 == 0) {
            checkPriceAlerts();
        }

        if (tickCount % 200 == 0) {
            updateBazaarState();
        }
    }

    private void updateBazaarState() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        SBInfo sbInfo = SBInfo.getInstance();
        String openChest = sbInfo.getOpenChestName();
        inBazaar = openChest != null && openChest.contains("Bazaar");
    }

    private void checkPriceAlerts() {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config == null || config.bazaar.priceAlertThreshold <= 0) return;

        APIManager apiManager = APIManager.getInstance();
        for (String itemId : previousPrices.keySet()) {
            Double oldPrice = previousPrices.get(itemId);
            APIManager.BazaarData current = apiManager.getBazaarData(itemId);
            if (current == null || oldPrice == null || oldPrice <= 0) continue;

            double change = Math.abs(current.buyPrice - oldPrice) / oldPrice * 100;
            double threshold = config.bazaar.priceAlertThreshold;

            if (change >= threshold) {
                String direction = current.buyPrice > oldPrice ? "\u00a7aUP" : "\u00a7cDOWN";
                String alert = itemId + " " + direction + " " + String.format("%.1f%%", change);
                activeAlerts.add(new PriceAlert(itemId, alert, System.currentTimeMillis()));
            }
        }
    }

    public void onChatMessage(Component message) {
        String text = message.getString();
        if (text.contains("Bazaar") || text.contains("bazaar")) {
            inBazaar = true;
        }
    }

    public void render(GuiGraphicsExtractor context, int screenWidth, int screenHeight) {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config == null || !config.bazaar.bazaarHelper) return;

        SBInfo sbInfo = SBInfo.getInstance();
        if (!sbInfo.hasSkyblockScoreboard()) return;

        if (!activeAlerts.isEmpty()) {
            renderAlerts(context, screenWidth);
        }

        if (inBazaar) {
            renderBazaarInfo(context, screenWidth, screenHeight);
        }
    }

    private void renderAlerts(GuiGraphicsExtractor context, int screenWidth) {
        Minecraft client = Minecraft.getInstance();
        int y = 80;
        long now = System.currentTimeMillis();

        activeAlerts.removeIf(alert -> now - alert.timestamp > 5000);

        for (PriceAlert alert : activeAlerts) {
            String text = "\u00a76\u26a0 " + alert.message;
            int width = client.font.width(text) + 8;
            context.fill(screenWidth / 2 - width / 2, y - 2, screenWidth / 2 + width / 2, y + client.font.lineHeight + 2, 0x80FFAA00);
            context.text(client.font, text, screenWidth / 2 - client.font.width(text) / 2, y, 0xFFFFAA00, true);
            y += client.font.lineHeight + 6;
        }
    }

    private void renderBazaarInfo(GuiGraphicsExtractor context, int screenWidth, int screenHeight) {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        Minecraft client = Minecraft.getInstance();

        int x = 4;
        int y = screenHeight / 2 - 40;
        int lineH = client.font.lineHeight + 2;

        context.fill(x - 2, y - 2, x + 140, y + 60, 0x80000000);
        context.outline(x - 2, y - 2, 142, 62, 0xFF555555);

        context.text(client.font, "\u00a76Bazaar Helper", x + 2, y + 2, 0xFFFFAA00, true);
        y += lineH + 4;

        if (config.bazaar.showCraftProfit) {
            context.text(client.font, "\u00a77Craft Profit: Check item tooltips", x + 2, y, 0xFFAAAAAA, false);
            y += lineH;
        }

        if (config.bazaar.showSellOffer) {
            context.text(client.font, "\u00a77Sell offers shown in tooltip", x + 2, y, 0xFFAAAAAA, false);
            y += lineH;
        }

        context.text(client.font, "\u00a77Alerts: " + activeAlerts.size(), x + 2, y, 0xFFAAAAAA, false);
    }

    public double calculateCraftProfit(String itemId) {
        APIManager apiManager = APIManager.getInstance();
        double craftCost = apiManager.getCraftCost(itemId);

        APIManager.BazaarData bazaar = apiManager.getBazaarData(itemId);
        double sellPrice = bazaar != null ? bazaar.sellPrice : 0;

        APIManager.AuctionData auction = null;
        double binPrice = apiManager.getLowestBin(itemId);

        double bestSell = Math.max(sellPrice, binPrice);

        return bestSell - craftCost;
    }

    public boolean isInBazaar() {
        return inBazaar;
    }

    public void reset() {
        inBazaar = false;
        activeAlerts.clear();
        previousPrices.clear();
    }

    private static class PriceAlert {
        final String itemId;
        final String message;
        final long timestamp;

        PriceAlert(String itemId, String message, long timestamp) {
            this.itemId = itemId;
            this.message = message;
            this.timestamp = timestamp;
        }
    }
}
