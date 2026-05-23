package io.github.legentpc.neu21plus.recipe;

import com.google.gson.JsonObject;
import io.github.legentpc.neu21plus.itemrepo.Ingredient;
import io.github.legentpc.neu21plus.itemrepo.ItemRepo;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ShopRecipe implements NeuRecipe {

    private final Ingredient cost;
    private final Ingredient output;
    private final String outputItemId;
    private final String shopName;

    public ShopRecipe(@NotNull String outputItemId, @NotNull Ingredient output,
                      @NotNull Ingredient cost, @Nullable String shopName) {
        this.outputItemId = outputItemId;
        this.output = output;
        this.cost = cost;
        this.shopName = shopName;
    }

    @Nullable
    public static ShopRecipe parse(@NotNull JsonObject recipeJson, @NotNull JsonObject outputItemJson) {
        String outputId = outputItemJson.has("internalname")
                ? outputItemJson.get("internalname").getAsString()
                : "";

        int outputCount = 1;
        if (recipeJson.has("count")) {
            outputCount = recipeJson.get("count").getAsInt();
        }

        Ingredient output = new Ingredient(outputId, outputCount);

        String costStr = recipeJson.has("cost") ? recipeJson.get("cost").getAsString() : "SKYBLOCK_COIN:1";
        Ingredient cost = Ingredient.parse(costStr);

        String shopName = recipeJson.has("shop_name") ? recipeJson.get("shop_name").getAsString() : null;

        return new ShopRecipe(outputId, output, cost, shopName);
    }

    @Override
    @NotNull
    public RecipeType getType() {
        return RecipeType.NPC_SHOP;
    }

    @Override
    @NotNull
    public List<Ingredient> getIngredients() {
        List<Ingredient> list = new ArrayList<>();
        list.add(cost);
        return list;
    }

    @Override
    @NotNull
    public List<Ingredient> getOutputs() {
        List<Ingredient> list = new ArrayList<>();
        list.add(output);
        return list;
    }

    @Override
    @NotNull
    public List<RecipeSlot> getSlots() {
        List<RecipeSlot> slots = new ArrayList<>();
        ItemRepo repo = ItemRepo.getInstance();

        ItemStack costStack = repo.createItemStack(cost.getInternalItemId());
        if (costStack == null) {
            costStack = new ItemStack(Items.GOLD_INGOT);
            costStack.setCustomName(Text.literal("\u00a76" + cost.getCount() + " Coins"));
        }
        if (cost.getCount() > 1) {
            costStack.setCount(Math.min(cost.getCount(), 64));
        }
        slots.add(new RecipeSlot(10, 32, cost.getInternalItemId(), cost.getCount(), costStack));

        ItemStack outputStack = repo.createItemStack(outputItemId);
        if (outputStack == null) {
            outputStack = new ItemStack(Items.BARRIER);
            outputStack.setCustomName(Text.literal("\u00a7c" + outputItemId));
        }
        if (output.getCount() > 1) {
            outputStack.setCount(output.getCount());
        }
        slots.add(new RecipeSlot(86, 32, outputItemId, output.getCount(), outputStack));

        return slots;
    }

    @Override
    @Nullable
    public JsonObject serialize() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", RecipeType.NPC_SHOP.getId());
        obj.addProperty("cost", cost.toString());
        if (shopName != null) {
            obj.addProperty("shop_name", shopName);
        }
        return obj;
    }

    @NotNull
    public Ingredient getCost() {
        return cost;
    }

    @Nullable
    public String getShopName() {
        return shopName;
    }
}
