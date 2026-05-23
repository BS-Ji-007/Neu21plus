package io.github.legentpc.neu21plus.recipe;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RecipeSlot {

    private final int x;
    private final int y;
    private final ItemStack stack;
    private final String internalItemId;
    private final int count;

    public RecipeSlot(int x, int y, @NotNull String internalItemId, int count, @Nullable ItemStack stack) {
        this.x = x;
        this.y = y;
        this.internalItemId = internalItemId;
        this.count = count;
        this.stack = stack;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Nullable
    public ItemStack getStack() {
        return stack;
    }

    @NotNull
    public String getInternalItemId() {
        return internalItemId;
    }

    public int getCount() {
        return count;
    }

    public boolean hasStack() {
        return stack != null && !stack.isEmpty();
    }
}
