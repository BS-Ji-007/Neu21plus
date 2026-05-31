package io.github.legentpc.neu21plus.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.legentpc.neu21plus.itemrepo.Ingredient;
import io.github.legentpc.neu21plus.itemrepo.ItemRepo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MobLootRecipe implements NeuRecipe {

    private final List<DropEntry> drops;
    private final String outputItemId;
    private final String mobName;
    private final int coins;
    private final int combatXp;

    public MobLootRecipe(@NotNull String outputItemId, @NotNull List<DropEntry> drops,
                         @Nullable String mobName, int coins, int combatXp) {
        this.outputItemId = outputItemId;
        this.drops = drops;
        this.mobName = mobName;
        this.coins = coins;
        this.combatXp = combatXp;
    }

    @Nullable
    public static MobLootRecipe parse(@NotNull JsonObject recipeJson, @NotNull JsonObject outputItemJson) {
        String outputId = outputItemJson.has("internalname")
                ? outputItemJson.get("internalname").getAsString()
                : "";

        List<DropEntry> drops = new ArrayList<>();
        if (recipeJson.has("drops")) {
            JsonArray dropsArray = recipeJson.getAsJsonArray("drops");
            for (JsonElement elem : dropsArray) {
                if (elem.isJsonObject()) {
                    JsonObject dropObj = elem.getAsJsonObject();
                    String itemId = dropObj.has("id") ? dropObj.get("id").getAsString() : "";
                    int count = dropObj.has("count") ? dropObj.get("count").getAsInt() : 1;
                    float chance = dropObj.has("chance") ? dropObj.get("chance").getAsFloat() : -1;
                    if (!itemId.isEmpty()) {
                        drops.add(new DropEntry(itemId, count, chance));
                    }
                }
            }
        }

        String mobName = recipeJson.has("name") ? recipeJson.get("name").getAsString() : null;
        int coins = recipeJson.has("coins") ? recipeJson.get("coins").getAsInt() : 0;
        int combatXp = recipeJson.has("combat_xp") ? recipeJson.get("combat_xp").getAsInt() : 0;

        return new MobLootRecipe(outputId, drops, mobName, coins, combatXp);
    }

    @Override
    @NotNull
    public RecipeType getType() {
        return RecipeType.MOB_LOOT;
    }

    @Override
    @NotNull
    public List<Ingredient> getIngredients() {
        return new ArrayList<>();
    }

    @Override
    @NotNull
    public List<Ingredient> getOutputs() {
        List<Ingredient> list = new ArrayList<>();
        for (DropEntry drop : drops) {
            list.add(new Ingredient(drop.itemId, drop.count));
        }
        return list;
    }

    @Override
    @NotNull
    public List<RecipeSlot> getSlots() {
        List<RecipeSlot> slots = new ArrayList<>();
        ItemRepo repo = ItemRepo.getInstance();

        int yOffset = 0;
        for (DropEntry drop : drops) {
            ItemStack stack = repo.createItemStack(drop.itemId);
            if (stack == null) {
                stack = new ItemStack(Items.BARRIER);
                stack.set(DataComponents.CUSTOM_NAME, Component.literal("\u00a7c" + drop.itemId));
            }
            if (drop.count > 1) {
                stack.setCount(drop.count);
            }
            slots.add(new RecipeSlot(10, 10 + yOffset, drop.itemId, drop.count, stack));
            yOffset += 22;
        }

        return slots;
    }

    @Override
    @Nullable
    public JsonObject serialize() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", RecipeType.MOB_LOOT.getId());

        JsonArray dropsArray = new JsonArray();
        for (DropEntry drop : drops) {
            JsonObject dropObj = new JsonObject();
            dropObj.addProperty("id", drop.itemId);
            dropObj.addProperty("count", drop.count);
            if (drop.chance >= 0) {
                dropObj.addProperty("chance", drop.chance);
            }
            dropsArray.add(dropObj);
        }
        obj.add("drops", dropsArray);

        if (mobName != null) {
            obj.addProperty("name", mobName);
        }
        if (coins > 0) {
            obj.addProperty("coins", coins);
        }
        if (combatXp > 0) {
            obj.addProperty("combat_xp", combatXp);
        }

        return obj;
    }

    @NotNull
    public List<DropEntry> getDrops() {
        return new ArrayList<>(drops);
    }

    @Nullable
    public String getMobName() {
        return mobName;
    }

    public int getCoins() {
        return coins;
    }

    public int getCombatXp() {
        return combatXp;
    }

    public static class DropEntry {
        public final String itemId;
        public final int count;
        public final float chance;

        public DropEntry(@NotNull String itemId, int count, float chance) {
            this.itemId = itemId;
            this.count = count;
            this.chance = chance;
        }
    }
}
