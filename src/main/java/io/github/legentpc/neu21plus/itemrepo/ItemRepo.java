package io.github.legentpc.neu21plus.itemrepo;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.recipe.NeuRecipe;
import io.github.legentpc.neu21plus.recipe.RecipeType;
import io.github.legentpc.neu21plus.util.NeuManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ItemRepo {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemRepo.class);

    private static final ItemRepo INSTANCE = new ItemRepo();

    public static ItemRepo getInstance() {
        return INSTANCE;
    }

    private final TreeMap<String, JsonObject> itemMap = new TreeMap<>();
    private final Set<NeuRecipe> recipes = new HashSet<>();
    private final HashMap<String, List<NeuRecipe>> recipesMap = new HashMap<>();
    private final HashMap<String, List<NeuRecipe>> usagesMap = new HashMap<>();
    private final Map<String, ItemStack> itemstackCache = new HashMap<>();
    private final Map<String, String> displayNameCache = new HashMap<>();
    private final TreeMap<String, TreeMap<String, String>> titleWordMap = new TreeMap<>();
    private final TreeMap<String, TreeMap<String, String>> loreWordMap = new TreeMap<>();

    private boolean loaded = false;

    private ItemRepo() {
    }

    public void load() {
        if (loaded) return;

        NeuManager manager = Neu21PlusMod.getInstance().getManager();
        File itemsDir = new File(manager.getRepoLocation(), "items");

        if (!itemsDir.exists() || !itemsDir.isDirectory()) {
            LOGGER.warn("Items directory not found: {}", itemsDir.getAbsolutePath());
            loaded = true;
            return;
        }

        File[] files = itemsDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) {
            loaded = true;
            return;
        }

        LOGGER.info("Loading {} item files...", files.length);

        for (File file : files) {
            loadItem(file);
        }

        loadRecipes();
        buildSearchIndex();

        LOGGER.info("Loaded {} items, {} recipes", itemMap.size(), recipes.size());
        loaded = true;
    }

    public void reload() {
        clear();
        loaded = false;
        load();
    }

    private void clear() {
        itemMap.clear();
        recipes.clear();
        recipesMap.clear();
        usagesMap.clear();
        itemstackCache.clear();
        displayNameCache.clear();
        titleWordMap.clear();
        loreWordMap.clear();
    }

    private void loadItem(File file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8)
        )) {
            NeuManager manager = Neu21PlusMod.getInstance().getManager();
            JsonObject obj = manager.getGson().fromJson(reader, JsonObject.class);
            if (obj != null && obj.has("internalname")) {
                String internalname = obj.get("internalname").getAsString();
                itemMap.put(internalname, obj);

                if (obj.has("displayname")) {
                    displayNameCache.put(internalname, obj.get("displayname").getAsString());
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Failed to load item from file: {}", file.getName(), e);
        }
    }

    private void loadRecipes() {
        for (Map.Entry<String, JsonObject> entry : itemMap.entrySet()) {
            JsonObject itemJson = entry.getValue();
            String itemId = entry.getKey();

            parseSingleRecipe(itemJson, itemId, "recipe");
            parseMultipleRecipes(itemJson, itemId, "recipes");

            if (itemJson.has("custom_drops")) {
                parseCustomDrops(itemJson, itemId);
            }

            if (itemJson.has("slayer_recipes")) {
                parseMultipleRecipes(itemJson, itemId, "slayer_recipes");
            }
        }
    }

    private void parseSingleRecipe(JsonObject itemJson, String itemId, String key) {
        if (!itemJson.has(key)) return;

        JsonObject recipeJson = itemJson.getAsJsonObject(key);
        NeuRecipe recipe = parseRecipeFromJson(recipeJson, itemJson);
        if (recipe != null) {
            addRecipe(itemId, recipe);
        }
    }

    private void parseMultipleRecipes(JsonObject itemJson, String itemId, String key) {
        if (!itemJson.has(key)) return;

        JsonArray recipesArray = itemJson.getAsJsonArray(key);
        for (JsonElement elem : recipesArray) {
            if (elem.isJsonObject()) {
                NeuRecipe recipe = parseRecipeFromJson(elem.getAsJsonObject(), itemJson);
                if (recipe != null) {
                    addRecipe(itemId, recipe);
                }
            }
        }
    }

    private void parseCustomDrops(JsonObject itemJson, String itemId) {
        if (!itemJson.has("custom_drops")) return;

        JsonObject dropsJson = itemJson.getAsJsonObject("custom_drops");
        JsonObject recipeObj = new JsonObject();
        recipeObj.addProperty("type", "mob_loot");
        recipeObj.add("drops", dropsJson.has("drops") ? dropsJson.getAsJsonArray("drops") : new JsonArray());

        NeuRecipe recipe = parseRecipeFromJson(recipeObj, itemJson);
        if (recipe != null) {
            addRecipe(itemId, recipe);
        }
    }

    @Nullable
    private NeuRecipe parseRecipeFromJson(JsonObject recipeJson, JsonObject outputItemJson) {
        String typeStr = recipeJson.has("type") ? recipeJson.get("type").getAsString() : "crafting";
        RecipeType type = RecipeType.fromId(typeStr);
        if (type == null) {
            type = RecipeType.CRAFTING;
        }
        return type.createRecipe(recipeJson, outputItemJson);
    }

    private void addRecipe(String itemId, NeuRecipe recipe) {
        recipes.add(recipe);

        recipesMap.computeIfAbsent(itemId, k -> new ArrayList<>()).add(recipe);

        for (Ingredient ingredient : recipe.getIngredients()) {
            String ingId = ingredient.getInternalItemId();
            if (!ingId.isEmpty()) {
                usagesMap.computeIfAbsent(ingId, k -> new ArrayList<>()).add(recipe);
            }
        }
    }

    private void buildSearchIndex() {
        for (Map.Entry<String, JsonObject> entry : itemMap.entrySet()) {
            String itemId = entry.getKey();
            JsonObject itemJson = entry.getValue();

            String displayName = itemJson.has("displayname")
                    ? itemJson.get("displayname").getAsString()
                    : "";
            indexWords(titleWordMap, itemId, displayName);

            if (itemJson.has("lore")) {
                JsonArray lore = itemJson.getAsJsonArray("lore");
                for (JsonElement loreElem : lore) {
                    indexWords(loreWordMap, itemId, loreElem.getAsString());
                }
            }
        }
    }

    private void indexWords(TreeMap<String, TreeMap<String, String>> index, String itemId, String text) {
        String cleaned = text.replaceAll("\u00a7[0-9a-fk-orA-FK-OR]", "").toLowerCase().trim();
        String[] words = cleaned.split("[\\s\\-_,./:;!?]+");
        for (String word : words) {
            if (word.length() < 2) continue;
            index.computeIfAbsent(word, k -> new TreeMap<>()).put(itemId, "");
        }
    }

    @Nullable
    public JsonObject getItemJson(@NotNull String internalName) {
        return itemMap.get(internalName);
    }

    public boolean hasItem(@NotNull String internalName) {
        return itemMap.containsKey(internalName);
    }

    @NotNull
    public TreeMap<String, JsonObject> getItemMap() {
        return itemMap;
    }

    @NotNull
    public List<NeuRecipe> getRecipesFor(@NotNull String internalName) {
        return recipesMap.getOrDefault(internalName, new ArrayList<>());
    }

    @NotNull
    public List<NeuRecipe> getUsagesFor(@NotNull String internalName) {
        return usagesMap.getOrDefault(internalName, new ArrayList<>());
    }

    @NotNull
    public Set<NeuRecipe> getAllRecipes() {
        return recipes;
    }

    @Nullable
    public String getDisplayName(@NotNull String internalName) {
        if (displayNameCache.containsKey(internalName)) {
            return displayNameCache.get(internalName);
        }
        JsonObject json = getItemJson(internalName);
        if (json != null && json.has("displayname")) {
            return json.get("displayname").getAsString();
        }
        return null;
    }

    @NotNull
    public List<String> search(@NotNull String query) {
        String cleaned = query.replaceAll("\u00a7[0-9a-fk-orA-FK-OR]", "").toLowerCase().trim();
        if (cleaned.isEmpty()) {
            return new ArrayList<>(itemMap.keySet());
        }

        String[] words = cleaned.split("[\\s]+");
        Map<String, Integer> scores = new HashMap<>();

        for (String word : words) {
            if (word.length() < 2) continue;

            TreeMap<String, String> titleMatches = titleWordMap.get(word);
            if (titleMatches != null) {
                for (String itemId : titleMatches.keySet()) {
                    scores.merge(itemId, 10, Integer::sum);
                }
            }

            TreeMap<String, String> loreMatches = loreWordMap.get(word);
            if (loreMatches != null) {
                for (String itemId : loreMatches.keySet()) {
                    scores.merge(itemId, 1, Integer::sum);
                }
            }

            for (String itemId : itemMap.keySet()) {
                if (itemId.toLowerCase().contains(word)) {
                    scores.merge(itemId, 5, Integer::sum);
                }
            }
        }

        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(scores.entrySet());
        sorted.sort(Map.Entry.<String, Integer>comparingByValue().reversed());

        List<String> results = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : sorted) {
            results.add(entry.getKey());
        }
        return results;
    }

    @Nullable
    public ItemStack createItemStack(@NotNull String internalName) {
        if (itemstackCache.containsKey(internalName)) {
            ItemStack cached = itemstackCache.get(internalName);
            return cached.copy();
        }

        JsonObject json = getItemJson(internalName);
        if (json == null) return null;

        ItemStack stack = jsonToItemStack(json);
        if (stack != null) {
            itemstackCache.put(internalName, stack.copy());
        }
        return stack;
    }

    @Nullable
    private ItemStack jsonToItemStack(@NotNull JsonObject json) {
        String itemId = json.has("itemid") ? json.get("itemid").getAsString() : "minecraft:barrier";

        if (itemId.startsWith("minecraft:")) {
            itemId = itemId.substring(10);
        }

        Identifier identifier = Identifier.fromNamespaceAndPath("minecraft", itemId);
        var item = BuiltInRegistries.ITEM.getValue(identifier);
        if (item == null) {
            return null;
        }

        ItemStack stack = new ItemStack(item);

        if (json.has("displayname")) {
            String displayName = json.get("displayname").getAsString();
            Component nameText = Component.literal(displayName);
            stack.set(DataComponents.CUSTOM_NAME, nameText);
        }

        if (json.has("lore")) {
            JsonArray loreArray = json.getAsJsonArray("lore");
            List<Component> loreLines = new ArrayList<>();
            for (JsonElement elem : loreArray) {
                loreLines.add(Component.literal(elem.getAsString()));
            }
            stack.set(DataComponents.LORE, new ItemLore(loreLines));
        }

        if (json.has("damage")) {
            int damage = json.get("damage").getAsInt();
            if (damage > 0) {
                stack.set(DataComponents.DAMAGE, damage);
            }
        }

        if (json.has("nbttag")) {
            applyNbtToStack(stack, json.get("nbttag").getAsString());
        }

        if (json.has("count")) {
            stack.setCount(json.get("count").getAsInt());
        }

        return stack;
    }

    private void applyNbtToStack(ItemStack stack, String nbtString) {
        try {
            CompoundTag nbt = TagParser.parseCompoundFully(nbtString);
            CompoundTag extraAttributes = nbt.getCompoundOrEmpty("ExtraAttributes");
            if (!extraAttributes.isEmpty()) {
                String skyblockId = extraAttributes.getStringOr("id", "");
                if (skyblockId != null && !skyblockId.isEmpty()) {
                    stack.set(DataComponents.CUSTOM_NAME,
                            Component.literal(stack.getHoverName().getString()));
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Failed to parse NBT for item", e);
        }
    }

    public boolean isLoaded() {
        return loaded;
    }

    public int getItemCount() {
        return itemMap.size();
    }

    public int getRecipeCount() {
        return recipes.size();
    }

    @NotNull
    public Set<Map.Entry<String, String>> getDisplayNames() {
        return displayNameCache.entrySet();
    }
}
