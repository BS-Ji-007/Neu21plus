package io.github.legentpc.neu21plus.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.itemrepo.ItemRepo;
import io.github.legentpc.neu21plus.util.NeuManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class APIManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(APIManager.class);

    private static final String HYPIXEL_API_BASE = "https://api.hypixel.net/";
    private static final String SKYBLOCK_AUCTION_ENDPOINT = "skyblock/auctions";
    private static final String BAZAAR_ENDPOINT = "skyblock/bazaar";

    private static final APIManager INSTANCE = new APIManager();

    public static APIManager getInstance() {
        return INSTANCE;
    }

    private final Map<String, BazaarData> bazaarCache = new HashMap<>();
    private final Map<String, AuctionData> auctionCache = new HashMap<>();
    private long lastBazaarUpdate = 0;
    private long lastAuctionUpdate = 0;
    private static final long CACHE_DURATION = 60000;

    private String apiKey = null;

    private APIManager() {
    }

    public void setApiKey(@Nullable String apiKey) {
        this.apiKey = apiKey;
    }

    @Nullable
    public String getApiKey() {
        return apiKey;
    }

    public void updateBazaarPrices() {
        if (apiKey == null || apiKey.isEmpty()) return;

        long now = System.currentTimeMillis();
        if (now - lastBazaarUpdate < CACHE_DURATION) return;

        try {
            JsonObject response = fetchApi(BAZAAR_ENDPOINT);
            if (response == null) return;

            if (!response.has("success") || !response.get("success").getAsBoolean()) return;

            JsonObject products = response.getAsJsonObject("products");
            if (products == null) return;

            for (Map.Entry<String, JsonElement> entry : products.entrySet()) {
                String productId = entry.getKey();
                JsonObject product = entry.getValue().getAsJsonObject();

                BazaarData data = new BazaarData();

                if (product.has("quick_status")) {
                    JsonObject status = product.getAsJsonObject("quick_status");
                    data.buyPrice = status.has("buyPrice") ? status.get("buyPrice").getAsDouble() : 0;
                    data.sellPrice = status.has("sellPrice") ? status.get("sellPrice").getAsDouble() : 0;
                    data.buyVolume = status.has("buyVolume") ? status.get("buyVolume").getAsLong() : 0;
                    data.sellVolume = status.has("sellVolume") ? status.get("sellVolume").getAsLong() : 0;
                    data.buyMovingWeek = status.has("buyMovingWeek") ? status.get("buyMovingWeek").getAsLong() : 0;
                    data.sellMovingWeek = status.has("sellMovingWeek") ? status.get("sellMovingWeek").getAsLong() : 0;
                }

                bazaarCache.put(productId, data);
            }

            lastBazaarUpdate = now;
            LOGGER.debug("Updated bazaar prices for {} products", products.size());
        } catch (Exception e) {
            LOGGER.warn("Failed to update bazaar prices", e);
        }
    }

    @Nullable
    public BazaarData getBazaarData(@NotNull String internalName) {
        BazaarData cached = bazaarCache.get(internalName);
        if (cached != null) return cached;

        String bazaarId = internalName;
        return bazaarCache.get(bazaarId);
    }

    public double getLowestBin(@NotNull String internalName) {
        AuctionData data = auctionCache.get(internalName);
        if (data != null) {
            return data.lowestBin;
        }
        return 0;
    }

    public double getCraftCost(@NotNull String internalName) {
        ItemRepo repo = ItemRepo.getInstance();
        var recipes = repo.getRecipesFor(internalName);

        double totalCost = 0;
        for (var recipe : recipes) {
            for (var ingredient : recipe.getIngredients()) {
                String ingId = ingredient.getInternalItemId();
                if (Ingredient.SKYBLOCK_COIN.equals(ingId)) {
                    totalCost += ingredient.getCount();
                    continue;
                }

                BazaarData bazaar = getBazaarData(ingId);
                if (bazaar != null && bazaar.buyPrice > 0) {
                    totalCost += bazaar.buyPrice * ingredient.getCount();
                } else {
                    AuctionData auction = auctionCache.get(ingId);
                    if (auction != null && auction.lowestBin > 0) {
                        totalCost += auction.lowestBin * ingredient.getCount();
                    }
                }
            }
        }

        return totalCost;
    }

    @Nullable
    private JsonObject fetchApi(@NotNull String endpoint) {
        try {
            String url = HYPIXEL_API_BASE + endpoint;
            HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("API-Key", apiKey);
            connection.setRequestProperty("User-Agent", "Neu21Plus");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);

            if (connection.getResponseCode() != 200) {
                LOGGER.debug("API returned status {} for {}", connection.getResponseCode(), endpoint);
                return null;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
            )) {
                NeuManager manager = Neu21PlusMod.getInstance().getManager();
                JsonElement elem = manager.getGson().fromJson(reader, JsonElement.class);
                if (elem != null && elem.isJsonObject()) {
                    return elem.getAsJsonObject();
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Failed to fetch API endpoint: {}", endpoint, e);
        }
        return null;
    }

    public void clearCache() {
        bazaarCache.clear();
        auctionCache.clear();
        lastBazaarUpdate = 0;
        lastAuctionUpdate = 0;
    }

    public static class BazaarData {
        public double buyPrice = 0;
        public double sellPrice = 0;
        public long buyVolume = 0;
        public long sellVolume = 0;
        public long buyMovingWeek = 0;
        public long sellMovingWeek = 0;
    }

    public static class AuctionData {
        public double lowestBin = 0;
        public double secondLowestBin = 0;
    }

    private static class Ingredient {
        public static final String SKYBLOCK_COIN = "SKYBLOCK_COIN";
    }
}
