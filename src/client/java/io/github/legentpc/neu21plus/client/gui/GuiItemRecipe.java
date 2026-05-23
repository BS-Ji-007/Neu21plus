package io.github.legentpc.neu21plus.client.gui;

import io.github.legentpc.neu21plus.itemrepo.ItemRepo;
import io.github.legentpc.neu21plus.itemrepo.ItemResolutionQuery;
import io.github.legentpc.neu21plus.recipe.NeuRecipe;
import io.github.legentpc.neu21plus.recipe.RecipeSlot;
import io.github.legentpc.neu21plus.recipe.RecipeType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GuiItemRecipe extends Screen {

    private static final Logger LOGGER = LoggerFactory.getLogger(GuiItemRecipe.class);

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 222;
    private static final int TAB_WIDTH = 24;
    private static final int TAB_HEIGHT = 22;
    private static final int TAB_SPACING = 2;

    private final String itemId;
    private final List<NeuRecipe> recipes;
    private final List<NeuRecipe> usages;
    private final boolean showUsages;

    private int guiLeft;
    private int guiTop;

    private final Map<RecipeType, List<NeuRecipe>> recipesByType = new LinkedHashMap<>();
    private RecipeType selectedType;
    private int currentPage = 0;
    private int totalPages = 1;

    private RecipeSlot hoveredSlot = null;
    private ItemStack hoveredStack = null;

    private final Screen parentScreen;

    public GuiItemRecipe(@NotNull String itemId, boolean showUsages, @Nullable Screen parentScreen) {
        super(Text.literal("NEU Recipe"));
        this.itemId = itemId;
        this.showUsages = showUsages;
        this.parentScreen = parentScreen;

        ItemRepo repo = ItemRepo.getInstance();
        this.recipes = repo.getRecipesFor(itemId);
        this.usages = repo.getUsagesFor(itemId);
    }

    @Override
    protected void init() {
        super.init();

        this.guiLeft = (this.width - GUI_WIDTH) / 2;
        this.guiTop = (this.height - GUI_HEIGHT) / 2;

        organizeRecipesByType();
        selectFirstAvailableType();
    }

    private void organizeRecipesByType() {
        recipesByType.clear();

        List<NeuRecipe> relevantRecipes = showUsages ? usages : recipes;

        for (NeuRecipe recipe : relevantRecipes) {
            RecipeType type = recipe.getType();
            recipesByType.computeIfAbsent(type, k -> new ArrayList<>()).add(recipe);
        }

        List<RecipeType> sortedTypes = new ArrayList<>(recipesByType.keySet());
        sortedTypes.sort(Comparator.comparing(RecipeType::getLabel));

        Map<RecipeType, List<NeuRecipe>> sorted = new LinkedHashMap<>();
        for (RecipeType type : sortedTypes) {
            sorted.put(type, recipesByType.get(type));
        }
        recipesByType.clear();
        recipesByType.putAll(sorted);
    }

    private void selectFirstAvailableType() {
        if (!recipesByType.isEmpty()) {
            selectedType = recipesByType.keySet().iterator().next();
            currentPage = 0;
            updateTotalPages();
        }
    }

    private void updateTotalPages() {
        List<NeuRecipe> typeRecipes = recipesByType.get(selectedType);
        if (typeRecipes == null || typeRecipes.isEmpty()) {
            totalPages = 1;
        } else {
            totalPages = typeRecipes.size();
        }
        currentPage = Math.min(currentPage, totalPages - 1);
        currentPage = Math.max(0, currentPage);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        this.renderBackground(context, mouseX, mouseY, deltaTicks);

        drawMainBackground(context);

        drawTabs(context, mouseX, mouseY);

        drawRecipeContent(context, mouseX, mouseY);

        drawPageNavigation(context);

        drawTitle(context);

        drawTooltip(context, mouseX, mouseY);
    }

    private void drawMainBackground(DrawContext context) {
        context.fill(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, 0xC0000000);
        context.drawBorder(guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT, 0xFF555555);
    }

    private void drawTitle(DrawContext context) {
        ItemRepo repo = ItemRepo.getInstance();
        String displayName = repo.getDisplayName(itemId);
        String cleanName = displayName != null ? stripColor(displayName) : itemId;

        String title = showUsages ? "\u00a79Usages: " : "\u00a7aRecipe: ";
        title += "\u00a7f" + cleanName;

        context.drawText(textRenderer, title,
                guiLeft + GUI_WIDTH / 2 - textRenderer.getWidth(stripColor(title)) / 2,
                guiTop + 4, 0xFFFFFF, true);
    }

    private void drawTabs(DrawContext context, int mouseX, int mouseY) {
        int tabIndex = 0;
        for (RecipeType type : recipesByType.keySet()) {
            int tabX = guiLeft - TAB_WIDTH - TAB_SPACING;
            int tabY = guiTop + tabIndex * (TAB_HEIGHT + TAB_SPACING);

            boolean isSelected = type == selectedType;
            boolean isHovered = mouseX >= tabX && mouseX < tabX + TAB_WIDTH
                    && mouseY >= tabY && mouseY < tabY + TAB_HEIGHT;

            int bgColor = isSelected ? 0xFF333333 : (isHovered ? 0xFF222222 : 0xFF111111);
            int borderColor = isSelected ? 0xFFAAAAAA : 0xFF555555;

            context.fill(tabX, tabY, tabX + TAB_WIDTH, tabY + TAB_HEIGHT, bgColor);
            context.drawBorder(tabX, tabY, TAB_WIDTH, TAB_HEIGHT, borderColor);

            String label = type.getLabel().substring(0, Math.min(3, type.getLabel().length()));
            int textColor = isSelected ? 0xFFFFAA00 : 0xFFAAAAAA;
            context.drawText(textRenderer, label,
                    tabX + TAB_WIDTH / 2 - textRenderer.getWidth(label) / 2,
                    tabY + TAB_HEIGHT / 2 - textRenderer.fontHeight / 2, textColor, false);

            List<NeuRecipe> typeRecipes = recipesByType.get(type);
            if (typeRecipes != null && typeRecipes.size() > 1) {
                String count = String.valueOf(typeRecipes.size());
                context.drawText(textRenderer, count,
                        tabX + TAB_WIDTH - textRenderer.getWidth(count) - 2,
                        tabY + 2, 0xFF888888, false);
            }

            tabIndex++;
        }
    }

    private void drawRecipeContent(DrawContext context, int mouseX, int mouseY) {
        List<NeuRecipe> typeRecipes = recipesByType.get(selectedType);
        if (typeRecipes == null || typeRecipes.isEmpty()) {
            context.drawText(textRenderer, "\u00a77No recipes available",
                    guiLeft + 20, guiTop + 50, 0xAAAAAA, false);
            return;
        }

        if (currentPage >= typeRecipes.size()) {
            currentPage = 0;
        }

        NeuRecipe currentRecipe = typeRecipes.get(currentPage);

        String typeName = "\u00a77" + selectedType.getLabel();
        context.drawText(textRenderer, typeName, guiLeft + 8, guiTop + 18, 0xAAAAAA, false);

        hoveredSlot = null;
        hoveredStack = null;

        List<RecipeSlot> slots = currentRecipe.getSlots();
        for (RecipeSlot slot : slots) {
            int slotX = guiLeft + slot.getX();
            int slotY = guiTop + slot.getY();

            context.fill(slotX - 1, slotY - 1, slotX + 17, slotY + 17, 0xFF333333);
            context.drawBorder(slotX - 1, slotY - 1, 18, 18, 0xFF666666);

            ItemStack stack = slot.getStack();
            if (stack != null && !stack.isEmpty()) {
                context.drawItem(stack, slotX, slotY);

                if (slot.getCount() > 1) {
                    String countText = String.valueOf(slot.getCount());
                    context.drawText(textRenderer, countText,
                            slotX + 16 - textRenderer.getWidth(countText),
                            slotY + 8, 0xFFFFFF, true);
                }

                if (mouseX >= slotX && mouseX < slotX + 16 && mouseY >= slotY && mouseY < slotY + 16) {
                    context.fill(slotX, slotY, slotX + 16, slotY + 16, 0x40FFFFFF);
                    hoveredSlot = slot;
                    hoveredStack = stack;
                }
            }
        }

        if (currentRecipe.getType() == RecipeType.FORGE) {
            io.github.legentpc.neu21plus.recipe.ForgeRecipe forgeRecipe =
                    (io.github.legentpc.neu21plus.recipe.ForgeRecipe) currentRecipe;
            if (forgeRecipe.getDuration() != null) {
                context.drawText(textRenderer, "\u00a77Duration: " + forgeRecipe.getDuration(),
                        guiLeft + 8, guiTop + GUI_HEIGHT - 40, 0xAAAAAA, false);
            }
            if (forgeRecipe.getHotmLevel() > 0) {
                context.drawText(textRenderer, "\u00a77HOTM Level: " + forgeRecipe.getHotmLevel(),
                        guiLeft + 8, guiTop + GUI_HEIGHT - 28, 0xAAAAAA, false);
            }
        }

        if (currentRecipe.getType() == RecipeType.MOB_LOOT) {
            io.github.legentpc.neu21plus.recipe.MobLootRecipe mobRecipe =
                    (io.github.legentpc.neu21plus.recipe.MobLootRecipe) currentRecipe;
            if (mobRecipe.getMobName() != null) {
                context.drawText(textRenderer, "\u00a77Mob: " + stripColor(mobRecipe.getMobName()),
                        guiLeft + 8, guiTop + GUI_HEIGHT - 52, 0xAAAAAA, false);
            }
            if (mobRecipe.getCoins() > 0) {
                context.drawText(textRenderer, "\u00a76Coins: " + mobRecipe.getCoins(),
                        guiLeft + 8, guiTop + GUI_HEIGHT - 40, 0xFFAA00, false);
            }
            if (mobRecipe.getCombatXp() > 0) {
                context.drawText(textRenderer, "\u00a7bCombat XP: " + mobRecipe.getCombatXp(),
                        guiLeft + 8, guiTop + GUI_HEIGHT - 28, 0x55FFFF, false);
            }
        }
    }

    private void drawPageNavigation(DrawContext context) {
        if (totalPages <= 1) return;

        String pageText = "\u00a77< \u00a7f" + (currentPage + 1) + "/" + totalPages + " \u00a77>";
        context.drawText(textRenderer, pageText,
                guiLeft + GUI_WIDTH / 2 - textRenderer.getWidth(stripColor(pageText)) / 2,
                guiTop + GUI_HEIGHT - 14, 0xAAAAAA, false);
    }

    private void drawTooltip(DrawContext context, int mouseX, int mouseY) {
        if (hoveredStack != null) {
            List<Text> tooltip = getTooltipFromItem(MinecraftClient.getInstance(), hoveredStack);
            context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int tabIndex = 0;
        for (RecipeType type : recipesByType.keySet()) {
            int tabX = guiLeft - TAB_WIDTH - TAB_SPACING;
            int tabY = guiTop + tabIndex * (TAB_HEIGHT + TAB_SPACING);

            if (mouseX >= tabX && mouseX < tabX + TAB_WIDTH
                    && mouseY >= tabY && mouseY < tabY + TAB_HEIGHT) {
                selectedType = type;
                currentPage = 0;
                updateTotalPages();
                return true;
            }
            tabIndex++;
        }

        if (hoveredSlot != null && button == 0) {
            String clickedItemId = hoveredSlot.getInternalItemId();
            if (clickedItemId != null && !clickedItemId.isEmpty() && !clickedItemId.equals(itemId)) {
                MinecraftClient client = MinecraftClient.getInstance();
                client.setScreen(new GuiItemRecipe(clickedItemId, false, this.parentScreen));
                return true;
            }
        }

        if (totalPages > 1) {
            int navY = guiTop + GUI_HEIGHT - 14;
            int navLeft = guiLeft + GUI_WIDTH / 2 - 30;
            int navRight = guiLeft + GUI_WIDTH / 2 + 30;

            if (mouseY >= navY && mouseY <= navY + 12) {
                if (mouseX >= navLeft && mouseX < guiLeft + GUI_WIDTH / 2) {
                    previousPage();
                    return true;
                } else if (mouseX >= guiLeft + GUI_WIDTH / 2 && mouseX <= navRight) {
                    nextPage();
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }

        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_BRACKET) {
            previousPage();
            return true;
        }

        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_BRACKET) {
            nextPage();
            return true;
        }

        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_R) {
            if (hoveredSlot != null) {
                String clickedItemId = hoveredSlot.getInternalItemId();
                if (clickedItemId != null && !clickedItemId.isEmpty()) {
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.setScreen(new GuiItemRecipe(clickedItemId, false, this.parentScreen));
                    return true;
                }
            }
        }

        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_U) {
            if (hoveredSlot != null) {
                String clickedItemId = hoveredSlot.getInternalItemId();
                if (clickedItemId != null && !clickedItemId.isEmpty()) {
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.setScreen(new GuiItemRecipe(clickedItemId, true, this.parentScreen));
                    return true;
                }
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount > 0) {
            previousPage();
        } else if (verticalAmount < 0) {
            nextPage();
        }
        return true;
    }

    private void previousPage() {
        if (currentPage > 0) {
            currentPage--;
        } else {
            currentPage = totalPages - 1;
        }
    }

    private void nextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
        } else {
            currentPage = 0;
        }
    }

    @Override
    public void close() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (parentScreen != null) {
            client.setScreen(parentScreen);
        } else {
            client.setScreen(null);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private String stripColor(String text) {
        if (text == null) return "";
        return text.replaceAll("\u00a7[0-9a-fk-orA-FK-OR]", "");
    }

    @NotNull
    public String getItemId() {
        return itemId;
    }

    public boolean isShowingUsages() {
        return showUsages;
    }

    @Nullable
    public RecipeType getSelectedType() {
        return selectedType;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
