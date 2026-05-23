package io.github.legentpc.neu21plus.client.overlay;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.api.APIManager;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.itemrepo.ItemRepo;
import io.github.legentpc.neu21plus.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemPriceInformation {

    private static final ItemPriceInformation INSTANCE = new ItemPriceInformation();

    public static ItemPriceInformation getInstance() {
        return INSTANCE;
    }

    private final Map<String, PriceData> priceCache = new HashMap<>();
    private long lastCacheUpdate = 0;
    private static final long CACHE_DURATION = 60000;

    private ItemPriceInformation() {
    }

    public void addToTooltip(@NotNull String internalName, @NotNull List<Component> lines, boolean showStackPrice) {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config == null) return;

        ItemRepo repo = ItemRepo.getInstance();
        if (!repo.isLoaded()) return;

        var itemJson = repo.getItemJson(internalName);
        if (itemJson == null) return;

        PriceData price = getPriceData(internalName);
        if (price == null) return;

        boolean addedLine = false;

        if (price.npcSellPrice > 0) {
            String sellText = "\u00a76NPC Sell: \u00a7a" + TextUtils.formatNumber(price.npcSellPrice) + " coins";
            if (showStackPrice && price.npcSellPrice > 1) {
                sellText += " \u00a77(\u00a7a" + TextUtils.formatNumber(price.npcSellPrice * 64) + "\u00a77)";
            }
            lines.add(Component.literal(sellText));
            addedLine = true;
        }

        if (price.binPrice > 0) {
            String binText = "\u00a76BIN: \u00a7a" + TextUtils.formatNumber(price.binPrice) + " coins";
            lines.add(Component.literal(binText));
            addedLine = true;
        }

        if (price.bazaarBuy > 0) {
            String bazaarText = "\u00a76Bazaar Buy: \u00a7a" + TextUtils.formatNumber(price.bazaarBuy) + " coins";
            lines.add(Component.literal(bazaarText));
            addedLine = true;
        }

        if (price.bazaarSell > 0) {
            String bazaarText = "\u00a76Bazaar Sell: \u00a7a" + TextUtils.formatNumber(price.bazaarSell) + " coins";
            lines.add(Component.literal(bazaarText));
            addedLine = true;
        }

        if (price.craftCost > 0) {
            String craftText = "\u00a76Craft Cost: \u00a7c" + TextUtils.formatNumber(price.craftCost) + " coins";
            lines.add(Component.literal(craftText));
            addedLine = true;
        }

        if (addedLine) {
            lines.add(Component.literal(""));
        }
    }

    @Nullable
    public PriceData getPriceData(@NotNull String internalName) {
        updateCacheIfNeeded();

        PriceData cached = priceCache.get(internalName);
        if (cached != null) {
            return cached;
        }

        PriceData data = fetchPriceData(internalName);
        if (data != null) {
            priceCache.put(internalName, data);
        }
        return data;
    }

    @Nullable
    private PriceData fetchPriceData(@NotNull String internalName) {
        PriceData data = new PriceData();

        ItemRepo repo = ItemRepo.getInstance();
        var itemJson = repo.getItemJson(internalName);
        if (itemJson == null) return null;

        if (itemJson.has("npc_sell")) {
            data.npcSellPrice = itemJson.get("npc_sell").getAsDouble();
        }

        APIManager apiManager = APIManager.getInstance();

        APIManager.BazaarData bazaar = apiManager.getBazaarData(internalName);
        if (bazaar != null) {
            data.bazaarBuy = bazaar.buyPrice;
            data.bazaarSell = bazaar.sellPrice;
        }

        data.binPrice = apiManager.getLowestBin(internalName);

        double craftCost = apiManager.getCraftCost(internalName);
        if (craftCost > 0) {
            data.craftCost = craftCost;
        }

        return data;
    }

    private void updateCacheIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastCacheUpdate > CACHE_DURATION) {
            lastCacheUpdate = now;
        }
    }

    public void updatePrices(@NotNull String internalName, double binPrice, double bazaarBuy, double bazaarSell) {
        PriceData data = priceCache.computeIfAbsent(internalName, k -> new PriceData());
        data.binPrice = binPrice;
        data.bazaarBuy = bazaarBuy;
        data.bazaarSell = bazaarSell;
    }

    public void clearCache() {
        priceCache.clear();
        lastCacheUpdate = 0;
    }

    public static class PriceData {
        public double npcSellPrice = 0;
        public double binPrice = 0;
        public double bazaarBuy = 0;
        public double bazaarSell = 0;
        public double craftCost = 0;
    }
}
