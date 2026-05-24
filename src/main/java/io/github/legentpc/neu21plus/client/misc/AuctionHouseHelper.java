package io.github.legentpc.neu21plus.client.misc;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.api.APIManager;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.itemrepo.ItemRepo;
import io.github.legentpc.neu21plus.itemrepo.ItemResolutionQuery;
import io.github.legentpc.neu21plus.skyblock.SBInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuctionHouseHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuctionHouseHelper.class);

    private static final AuctionHouseHelper INSTANCE = new AuctionHouseHelper();

    private static final Pattern AH_PRICE_PATTERN = Pattern.compile(
            "(\\d[\\d,]*\\.?\\d*)\\s*coins?"
    );
    private static final Pattern BIN_PATTERN = Pattern.compile(
            ".*Buy It Now.*?(\\d[\\d,]*\\.?\\d*).*coins?"
    );
    private static final Pattern AUCTION_BID_PATTERN = Pattern.compile(
            ".*(?:Top Bid|Current Bid|Starting Bid).*?(\\d[\\d,]*\\.?\\d*).*coins?"
    );

    public static AuctionHouseHelper getInstance() {
        return INSTANCE;
    }

    public static class AuctionEntry {
        String itemName;
        String internalName;
        double listedPrice;
        double marketPrice;
        double priceDifference;
        double pricePercentBelow;
        boolean isBin;

        AuctionEntry(String itemName, String internalName, double listedPrice, double marketPrice, boolean isBin) {
            this.itemName = itemName;
            this.internalName = internalName;
            this.listedPrice = listedPrice;
            this.marketPrice = marketPrice;
            this.isBin = isBin;

            if (marketPrice > 0 && listedPrice > 0) {
                this.priceDifference = marketPrice - listedPrice;
                this.pricePercentBelow = (1.0 - listedPrice / marketPrice) * 100;
            } else {
                this.priceDifference = 0;
                this.pricePercentBelow = 0;
            }
        }

        public boolean isUnderpriced() {
            return pricePercentBelow > 20.0 && marketPrice > 0;
        }

        public boolean isGoodDeal() {
            return pricePercentBelow > 10.0 && marketPrice > 0;
        }
    }

    private final List<AuctionEntry> currentAhEntries = new ArrayList<>();
    private boolean inAuctionHouse = false;
    private int scanTickCount = 0;

    private AuctionHouseHelper() {
    }

    public void tick() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        SBInfo sbInfo = SBInfo.getInstance();
        String chestName = sbInfo.currentlyOpenChestName;
        boolean wasInAh = inAuctionHouse;
        inAuctionHouse = chestName != null && (chestName.contains("Auction House")
                || chestName.contains("Auctions"));

        if (inAuctionHouse && !wasInAh) {
            currentAhEntries.clear();
            LOGGER.debug("Entered Auction House");
        } else if (!inAuctionHouse && wasInAh) {
            currentAhEntries.clear();
            LOGGER.debug("Exited Auction House");
        }

        if (inAuctionHouse) {
            scanTickCount++;
            if (scanTickCount % 40 == 0) {
                scanAuctionHouse(client);
            }
        }
    }

    private void scanAuctionHouse(Minecraft client) {
        if (!(client.screen instanceof AbstractContainerScreen<?> containerScreen)) return;

        currentAhEntries.clear();

        var menu = containerScreen.getMenu();
        var items = menu.slots;

        APIManager apiManager = APIManager.getInstance();
        ItemRepo repo = ItemRepo.getInstance();

        for (int i = 0; i < items.size(); i++) {
            var slot = items.get(i);
            if (!slot.hasItem()) continue;

            ItemStack stack = slot.getItem();
            String displayName = stripColorCodes(stack.getDisplayName().getString()).trim();
            String internalName = new ItemResolutionQuery().withItemStack(stack).resolve();

            if (internalName == null || internalName.isEmpty()) continue;

            double marketPrice = getMarketPrice(apiManager, internalName);
            if (marketPrice <= 0) continue;

            double listedPrice = parsePriceFromLore(stack);
            if (listedPrice <= 0) continue;

            boolean isBin = isBinItem(stack);

            AuctionEntry entry = new AuctionEntry(displayName, internalName, listedPrice, marketPrice, isBin);
            currentAhEntries.add(entry);

            if (entry.isUnderpriced()) {
                LOGGER.debug("Underpriced BIN found: {} at {} (market: {}, {}% below)",
                        displayName, listedPrice, marketPrice, String.format("%.1f", entry.pricePercentBelow));
            }
        }
    }

    private double getMarketPrice(APIManager apiManager, String internalName) {
        APIManager.BazaarData bazaarData = apiManager.getBazaarData(internalName);
        if (bazaarData != null && bazaarData.buyPrice > 0) {
            return bazaarData.buyPrice;
        }

        double lowestBin = apiManager.getLowestBin(internalName);
        if (lowestBin > 0) {
            return lowestBin;
        }

        return 0;
    }

    private double parsePriceFromLore(ItemStack stack) {
        for (Component line : stack.getTooltipLines(Item.TooltipContext.EMPTY, null, TooltipFlag.NORMAL)) {
            String text = stripColorCodes(line.getString()).trim();

            Matcher binMatcher = BIN_PATTERN.matcher(text);
            if (binMatcher.find()) {
                return parseCoinString(binMatcher.group(1));
            }

            Matcher bidMatcher = AUCTION_BID_PATTERN.matcher(text);
            if (bidMatcher.find()) {
                return parseCoinString(bidMatcher.group(1));
            }

            Matcher priceMatcher = AH_PRICE_PATTERN.matcher(text);
            if (priceMatcher.find()) {
                return parseCoinString(priceMatcher.group(1));
            }
        }
        return -1;
    }

    private boolean isBinItem(ItemStack stack) {
        for (Component line : stack.getTooltipLines(Item.TooltipContext.EMPTY, null, TooltipFlag.NORMAL)) {
            String text = stripColorCodes(line.getString()).trim();
            if (text.contains("Buy It Now") || text.contains("BIN")) {
                return true;
            }
        }
        return false;
    }

    private double parseCoinString(String priceStr) {
        try {
            String cleaned = priceStr.replace(",", "").replace(" ", "").trim();
            if (cleaned.isEmpty()) return -1;

            double multiplier = 1;
            String upper = cleaned.toUpperCase();
            if (upper.endsWith("M")) {
                multiplier = 1_000_000;
                cleaned = cleaned.substring(0, cleaned.length() - 1);
            } else if (upper.endsWith("K")) {
                multiplier = 1_000;
                cleaned = cleaned.substring(0, cleaned.length() - 1);
            } else if (upper.endsWith("B")) {
                multiplier = 1_000_000_000;
                cleaned = cleaned.substring(0, cleaned.length() - 1);
            }

            return Double.parseDouble(cleaned) * multiplier;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public void render(GuiGraphicsExtractor context, int screenWidth, int screenHeight) {
        if (!inAuctionHouse || currentAhEntries.isEmpty()) return;

        Minecraft client = Minecraft.getInstance();

        List<AuctionEntry> underpriced = currentAhEntries.stream()
                .filter(AuctionEntry::isGoodDeal)
                .toList();

        if (underpriced.isEmpty()) return;

        int x = screenWidth - 150;
        int y = 4;

        int boxHeight = 14 + underpriced.size() * 12;
        context.fill(x - 2, y - 2, x + 148, y + boxHeight, 0x80000000);
        context.outline(x - 2, y - 2, 150, boxHeight + 2, 0xFFFFAA00);

        context.text(client.font, "\u00a76Underpriced:", x + 2, y + 1, 0xFFFFAA00, true);

        int lineY = y + 14;
        for (AuctionEntry entry : underpriced) {
            String text = entry.itemName + " -" + String.format("%.0f", entry.pricePercentBelow) + "%";
            int nameWidth = client.font.width(text);
            if (nameWidth > 142) {
                text = text.substring(0, Math.min(text.length(), 22)) + "... -" + String.format("%.0f", entry.pricePercentBelow) + "%";
            }
            int color = entry.isUnderpriced() ? 0xFFFF5555 : 0xFFFFFF55;
            context.text(client.font, text, x + 2, lineY, color, false);
            lineY += 12;
        }
    }

    public void reset() {
        currentAhEntries.clear();
        inAuctionHouse = false;
        scanTickCount = 0;
    }

    public boolean isInAuctionHouse() {
        return inAuctionHouse;
    }

    public List<AuctionEntry> getCurrentAhEntries() {
        return currentAhEntries;
    }

    private String stripColorCodes(String text) {
        return text.replaceAll("\u00a7[0-9a-fk-orA-FK-OR]", "");
    }
}
