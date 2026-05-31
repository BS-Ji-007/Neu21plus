package io.github.legentpc.neu21plus.recipe;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public enum RecipeType {

    CRAFTING("crafting", "Crafting", CraftingRecipe::parse),
    FORGE("forge", "Forge", ForgeRecipe::parse),
    TRADE("trade", "Trade", TradeRecipe::parse),
    MOB_LOOT("mob_loot", "Mob Loot", MobLootRecipe::parse),
    NPC_SHOP("npc_shop", "NPC Shop", ShopRecipe::parse),
    ESSENCE("essence", "Essence Upgrades", null),
    KAT("kat", "Kat Upgrade", KatRecipe::parse);

    private final String id;
    private final String label;
    private final BiFunction<JsonObject, JsonObject, NeuRecipe> factory;

    RecipeType(String id, String label, BiFunction<JsonObject, JsonObject, NeuRecipe> factory) {
        this.id = id;
        this.label = label;
        this.factory = factory;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public String getLabel() {
        return label;
    }

    @Nullable
    public NeuRecipe createRecipe(@NotNull JsonObject recipeJson, @NotNull JsonObject outputItemJson) {
        if (factory == null) return null;
        return factory.apply(recipeJson, outputItemJson);
    }

    @Nullable
    public static RecipeType fromId(@NotNull String id) {
        for (RecipeType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return null;
    }
}
