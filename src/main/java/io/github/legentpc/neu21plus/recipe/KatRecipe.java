package io.github.legentpc.neu21plus.recipe;

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

public class KatRecipe implements NeuRecipe {

    private final Ingredient input;
    private final Ingredient output;
    private final String outputItemId;
    private final Ingredient cost;
    private final String duration;

    public KatRecipe(@NotNull String outputItemId, @NotNull Ingredient output,
                     @NotNull Ingredient input, @NotNull Ingredient cost, @Nullable String duration) {
        this.outputItemId = outputItemId;
        this.output = output;
        this.input = input;
        this.cost = cost;
        this.duration = duration;
    }

    @Nullable
    public static KatRecipe parse(@NotNull JsonObject recipeJson, @NotNull JsonObject outputItemJson) {
        String outputId = outputItemJson.has("internalname")
                ? outputItemJson.get("internalname").getAsString()
                : "";

        Ingredient output = new Ingredient(outputId, 1);

        String inputId = recipeJson.has("input") ? recipeJson.get("input").getAsString() : "";
        Ingredient input = Ingredient.parse(inputId);

        String costStr = recipeJson.has("cost") ? recipeJson.get("cost").getAsString() : "SKYBLOCK_COIN:1";
        Ingredient cost = Ingredient.parse(costStr);

        String duration = recipeJson.has("duration") ? recipeJson.get("duration").getAsString() : null;

        return new KatRecipe(outputId, output, input, cost, duration);
    }

    @Override
    @NotNull
    public RecipeType getType() {
        return RecipeType.KAT;
    }

    @Override
    @NotNull
    public List<Ingredient> getIngredients() {
        List<Ingredient> list = new ArrayList<>();
        list.add(input);
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

        ItemStack inputStack = repo.createItemStack(input.getInternalItemId());
        if (inputStack == null) {
            inputStack = new ItemStack(Items.BARRIER);
            inputStack.set(DataComponents.CUSTOM_NAME, Component.literal("\u00a7c" + input.getInternalItemId()));
        }
        slots.add(new RecipeSlot(10, 10, input.getInternalItemId(), input.getCount(), inputStack));

        ItemStack costStack = repo.createItemStack(cost.getInternalItemId());
        if (costStack == null) {
            costStack = new ItemStack(Items.GOLD_INGOT);
            costStack.set(DataComponents.CUSTOM_NAME, Component.literal("\u00a76" + cost.getCount() + " Coins"));
        }
        slots.add(new RecipeSlot(10, 32, cost.getInternalItemId(), cost.getCount(), costStack));

        ItemStack outputStack = repo.createItemStack(outputItemId);
        if (outputStack == null) {
            outputStack = new ItemStack(Items.BARRIER);
            outputStack.set(DataComponents.CUSTOM_NAME, Component.literal("\u00a7c" + outputItemId));
        }
        slots.add(new RecipeSlot(86, 32, outputItemId, output.getCount(), outputStack));

        return slots;
    }

    @Override
    @Nullable
    public JsonObject serialize() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", RecipeType.KAT.getId());
        obj.addProperty("input", input.toString());
        obj.addProperty("cost", cost.toString());
        if (duration != null) {
            obj.addProperty("duration", duration);
        }
        return obj;
    }

    @NotNull
    public Ingredient getInput() {
        return input;
    }

    @NotNull
    public Ingredient getCost() {
        return cost;
    }

    @Nullable
    public String getDuration() {
        return duration;
    }
}
