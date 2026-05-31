package io.github.legentpc.neu21plus.recipe;

import com.google.gson.JsonObject;
import io.github.legentpc.neu21plus.itemrepo.Ingredient;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface NeuRecipe {

    @NotNull
    RecipeType getType();

    @NotNull
    List<Ingredient> getIngredients();

    @NotNull
    List<Ingredient> getOutputs();

    @NotNull
    List<RecipeSlot> getSlots();

    @Nullable
    JsonObject serialize();

    @Nullable
    default ItemStack getOutputItemStack() {
        List<Ingredient> outputs = getOutputs();
        if (outputs.isEmpty()) return null;
        Ingredient first = outputs.get(0);
        return io.github.legentpc.neu21plus.itemrepo.ItemRepo.getInstance().createItemStack(first.getInternalItemId());
    }

    @NotNull
    default String getOutputItemId() {
        List<Ingredient> outputs = getOutputs();
        if (outputs.isEmpty()) return "";
        return outputs.get(0).getInternalItemId();
    }
}
