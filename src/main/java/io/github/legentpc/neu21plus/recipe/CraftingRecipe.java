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

public class CraftingRecipe implements NeuRecipe {

    private static final String[] GRID_SLOTS = {"A1", "A2", "A3", "B1", "B2", "B3", "C1", "C2", "C3"};
    private static final int[] SLOT_X = {10, 32, 54, 10, 32, 54, 10, 32, 54};
    private static final int[] SLOT_Y = {10, 10, 10, 32, 32, 32, 54, 54, 54};

    private final Ingredient[] ingredients = new Ingredient[9];
    private final Ingredient output;
    private final String extraText;
    private final String outputItemId;

    public CraftingRecipe(@NotNull String outputItemId, @NotNull Ingredient output, @Nullable String extraText) {
        this.outputItemId = outputItemId;
        this.output = output;
        this.extraText = extraText;
        for (int i = 0; i < 9; i++) {
            ingredients[i] = new Ingredient("", 0);
        }
    }

    public void setIngredient(int index, @NotNull Ingredient ingredient) {
        if (index >= 0 && index < 9) {
            ingredients[index] = ingredient;
        }
    }

    @NotNull
    public Ingredient[] getIngredientsArray() {
        return ingredients;
    }

    @Nullable
    public static CraftingRecipe parse(@NotNull JsonObject recipeJson, @NotNull JsonObject outputItemJson) {
        String outputId = outputItemJson.has("internalname")
                ? outputItemJson.get("internalname").getAsString()
                : "";

        int outputCount = 1;
        if (recipeJson.has("count")) {
            outputCount = recipeJson.get("count").getAsInt();
        }

        Ingredient output = new Ingredient(outputId, outputCount);
        String extraText = recipeJson.has("extraText") ? recipeJson.get("extraText").getAsString() : null;

        CraftingRecipe recipe = new CraftingRecipe(outputId, output, extraText);

        for (int i = 0; i < 9; i++) {
            String slotKey = GRID_SLOTS[i];
            if (recipeJson.has(slotKey)) {
                String ingredientStr = recipeJson.get(slotKey).getAsString();
                if (!ingredientStr.isEmpty()) {
                    recipe.setIngredient(i, Ingredient.parse(ingredientStr));
                }
            }
        }

        return recipe;
    }

    @Override
    @NotNull
    public RecipeType getType() {
        return RecipeType.CRAFTING;
    }

    @Override
    @NotNull
    public List<Ingredient> getIngredients() {
        List<Ingredient> list = new ArrayList<>();
        for (Ingredient ing : ingredients) {
            if (!ing.isEmpty()) {
                list.add(ing);
            }
        }
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

        for (int i = 0; i < 9; i++) {
            Ingredient ing = ingredients[i];
            if (ing.isEmpty()) continue;

            ItemStack stack = repo.createItemStack(ing.getInternalItemId());
            if (stack == null) {
                stack = new ItemStack(Items.BARRIER);
                stack.setCustomName(Text.literal("\u00a7c" + ing.getInternalItemId()));
            }
            if (ing.getCount() > 1) {
                stack.setCount(ing.getCount());
            }
            slots.add(new RecipeSlot(SLOT_X[i], SLOT_Y[i], ing.getInternalItemId(), ing.getCount(), stack));
        }

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
        obj.addProperty("type", RecipeType.CRAFTING.getId());
        for (int i = 0; i < 9; i++) {
            if (!ingredients[i].isEmpty()) {
                obj.addProperty(GRID_SLOTS[i], ingredients[i].toString());
            }
        }
        if (output.getCount() > 1) {
            obj.addProperty("count", output.getCount());
        }
        if (extraText != null) {
            obj.addProperty("extraText", extraText);
        }
        return obj;
    }

    @Nullable
    public String getExtraText() {
        return extraText;
    }
}
