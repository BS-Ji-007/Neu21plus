package io.github.legentpc.neu21plus.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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

public class TradeRecipe implements NeuRecipe {

    private final List<Ingredient> inputs;
    private final Ingredient output;
    private final String outputItemId;

    public TradeRecipe(@NotNull String outputItemId, @NotNull Ingredient output, @NotNull List<Ingredient> inputs) {
        this.outputItemId = outputItemId;
        this.output = output;
        this.inputs = inputs;
    }

    @Nullable
    public static TradeRecipe parse(@NotNull JsonObject recipeJson, @NotNull JsonObject outputItemJson) {
        String outputId = outputItemJson.has("internalname")
                ? outputItemJson.get("internalname").getAsString()
                : "";

        int outputCount = 1;
        if (recipeJson.has("count")) {
            outputCount = recipeJson.get("count").getAsInt();
        }

        Ingredient output = new Ingredient(outputId, outputCount);
        List<Ingredient> inputs = new ArrayList<>();

        if (recipeJson.has("inputs")) {
            JsonArray inputsArray = recipeJson.getAsJsonArray("inputs");
            for (JsonElement elem : inputsArray) {
                Ingredient ing = Ingredient.parse(elem.getAsString());
                if (!ing.isEmpty()) {
                    inputs.add(ing);
                }
            }
        }

        return new TradeRecipe(outputId, output, inputs);
    }

    @Override
    @NotNull
    public RecipeType getType() {
        return RecipeType.TRADE;
    }

    @Override
    @NotNull
    public List<Ingredient> getIngredients() {
        return new ArrayList<>(inputs);
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

        int yOffset = 0;
        for (Ingredient ing : inputs) {
            ItemStack stack = repo.createItemStack(ing.getInternalItemId());
            if (stack == null) {
                stack = new ItemStack(Items.BARRIER);
                stack.setCustomName(Text.literal("\u00a7c" + ing.getInternalItemId()));
            }
            if (ing.getCount() > 1) {
                stack.setCount(ing.getCount());
            }
            slots.add(new RecipeSlot(10, 10 + yOffset, ing.getInternalItemId(), ing.getCount(), stack));
            yOffset += 22;
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
        obj.addProperty("type", RecipeType.TRADE.getId());

        JsonArray inputsArray = new JsonArray();
        for (Ingredient ing : inputs) {
            inputsArray.add(ing.toString());
        }
        obj.add("inputs", inputsArray);

        return obj;
    }
}
