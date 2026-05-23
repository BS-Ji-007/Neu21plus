package io.github.legentpc.neu21plus.client.overlay;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.itemrepo.Ingredient;
import io.github.legentpc.neu21plus.itemrepo.ItemRepo;
import io.github.legentpc.neu21plus.itemrepo.ItemResolutionQuery;
import io.github.legentpc.neu21plus.recipe.CraftingRecipe;
import io.github.legentpc.neu21plus.recipe.NeuRecipe;
import io.github.legentpc.neu21plus.recipe.RecipeSlot;
import io.github.legentpc.neu21plus.util.TextUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CraftingOverlay {

    private static final Logger LOGGER = LoggerFactory.getLogger(CraftingOverlay.class);

    private static CraftingOverlay instance;

    public static CraftingOverlay getInstance() {
        if (instance == null) {
            instance = new CraftingOverlay();
        }
        return instance;
    }

    private String currentCraftingItem = null;
    private CraftingRecipe currentRecipe = null;

    private CraftingOverlay() {
    }

    public void render(DrawContext context, int guiLeft, int guiTop) {
        if (currentCraftingItem == null || currentRecipe == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        ItemRepo repo = ItemRepo.getInstance();
        if (!repo.isLoaded()) return;

        String extraText = currentRecipe.getExtraText();
        if (extraText != null && !extraText.isEmpty()) {
            String cleaned = TextUtils.stripColorCodes(extraText);
            context.drawText(client.textRenderer, "\u00a77" + cleaned,
                    guiLeft + 8, guiTop - 12, 0xAAAAAA, false);
        }

        Ingredient[] ingredients = currentRecipe.getIngredientsArray();
        for (int i = 0; i < 9; i++) {
            Ingredient ing = ingredients[i];
            if (ing.isEmpty()) continue;

            int slotX = guiLeft + getCraftingSlotX(i);
            int slotY = guiTop + getCraftingSlotY(i);

            ItemStack stack = repo.createItemStack(ing.getInternalItemId());
            if (stack == null) continue;

            boolean hasItem = playerHasItem(ing.getInternalItemId(), ing.getCount());

            int color = hasItem ? 0x4000FF00 : 0x40FF0000;
            context.fill(slotX, slotY, slotX + 16, slotY + 16, color);

            context.drawItem(stack, slotX, slotY);

            if (ing.getCount() > 1) {
                String countText = "\u00a7f" + ing.getCount();
                context.drawText(client.textRenderer, countText,
                        slotX + 16 - client.textRenderer.getWidth(countText),
                        slotY + 8, 0xFFFFFF, true);
            }
        }
    }

    private int getCraftingSlotX(int index) {
        int col = index % 3;
        return 30 + col * 18;
    }

    private int getCraftingSlotY(int index) {
        int row = index / 3;
        return 17 + row * 18;
    }

    private boolean playerHasItem(String internalItemId, int count) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return false;

        int found = 0;
        for (int i = 0; i < client.player.getInventory().size(); i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            String resolved = new ItemResolutionQuery().withItemStack(stack).resolve();
            if (internalItemId.equals(resolved)) {
                found += stack.getCount();
                if (found >= count) return true;
            }
        }
        return false;
    }

    public void setCraftingRecipe(@Nullable String itemId) {
        if (itemId == null) {
            currentCraftingItem = null;
            currentRecipe = null;
            return;
        }

        ItemRepo repo = ItemRepo.getInstance();
        List<NeuRecipe> recipes = repo.getRecipesFor(itemId);

        for (NeuRecipe recipe : recipes) {
            if (recipe instanceof CraftingRecipe craftingRecipe) {
                currentCraftingItem = itemId;
                currentRecipe = craftingRecipe;
                return;
            }
        }

        currentCraftingItem = null;
        currentRecipe = null;
    }

    public void clear() {
        currentCraftingItem = null;
        currentRecipe = null;
    }

    @Nullable
    public String getCurrentCraftingItem() {
        return currentCraftingItem;
    }
}
