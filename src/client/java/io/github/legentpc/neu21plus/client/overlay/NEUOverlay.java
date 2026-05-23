package io.github.legentpc.neu21plus.client.overlay;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.itemrepo.ItemRepo;
import io.github.legentpc.neu21plus.itemrepo.ItemResolutionQuery;
import io.github.legentpc.neu21plus.skyblock.SBInfo;
import io.github.legentpc.neu21plus.util.TextUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class NEUOverlay {

    private static final Logger LOGGER = LoggerFactory.getLogger(NEUOverlay.class);

    private static final int ITEM_SIZE = 16;
    private static final int ITEM_PADDING = 4;
    private static final int PANE_WIDTH = 120;
    private static final int SEARCH_BAR_HEIGHT = 16;
    private static final int ITEMS_PER_ROW = 5;
    private static final int ITEMS_VISIBLE_ROWS = 8;

    private static NEUOverlay instance;

    public static NEUOverlay getInstance() {
        if (instance == null) {
            instance = new NEUOverlay();
        }
        return instance;
    }

    private final LerpingFloat itemPaneOffsetFactor = new LerpingFloat(0, 0.15f);
    private final LerpingFloat infoPaneOffsetFactor = new LerpingFloat(0, 0.15f);

    private boolean overlayEnabled = false;
    private boolean searchFocused = false;
    private String searchText = "";
    private int scrollOffset = 0;

    private List<String> searchResults = new ArrayList<>();
    private String hoveredItemId = null;
    private String selectedItem = null;

    private final RecipeHistory recipeHistory = new RecipeHistory();

    private NEUOverlay() {
    }

    public void tick() {
        itemPaneOffsetFactor.tick();
        infoPaneOffsetFactor.tick();
    }

    public void render(DrawContext context, int screenWidth, int screenHeight) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen == null) return;
        if (!(client.currentScreen instanceof HandledScreen)) return;

        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config == null || !config.overlay.showOverlay) return;

        SBInfo sbInfo = SBInfo.getInstance();
        if (!sbInfo.hasSkyblockScoreboard() && !overlayEnabled) return;

        ItemRepo repo = ItemRepo.getInstance();
        if (!repo.isLoaded()) return;

        if (overlayEnabled) {
            itemPaneOffsetFactor.setTarget(1.0f);
        } else {
            itemPaneOffsetFactor.setTarget(0.0f);
            infoPaneOffsetFactor.setTarget(0.0f);
        }

        if (itemPaneOffsetFactor.getValue() < 0.01f && !overlayEnabled) return;

        int paneX = (int) (-PANE_WIDTH + PANE_WIDTH * itemPaneOffsetFactor.getValue());

        drawItemPane(context, paneX, 0, screenWidth, screenHeight, client);

        if (selectedItem != null && itemPaneOffsetFactor.getValue() > 0.5f) {
            infoPaneOffsetFactor.setTarget(1.0f);
            int infoX = (int) (screenWidth + (int) (-PANE_WIDTH * infoPaneOffsetFactor.getValue()));
            drawInfoPane(context, infoX, 0, screenWidth, screenHeight, client);
        }

        if (searchFocused) {
            drawSearchBar(context, paneX, client);
        }
    }

    private void drawItemPane(DrawContext context, int x, int y, int screenWidth, int screenHeight, MinecraftClient client) {
        int paneHeight = screenHeight;

        context.fill(x, y, x + PANE_WIDTH, y + paneHeight, 0xC0000000);

        context.drawBorder(x, y, PANE_WIDTH, paneHeight, 0xFF555555);

        context.drawText(client.textRenderer, "\u00a7bNEU Items", x + 4, y + 4, 0xFFFFFF, true);

        String query = searchText;
        if (query.isEmpty() && !searchFocused) {
            context.drawText(client.textRenderer, "\u00a77Search...", x + 4, y + SEARCH_BAR_HEIGHT + 6, 0xAAAAAA, false);
        } else if (!query.isEmpty()) {
            context.drawText(client.textRenderer, "\u00a7f" + query, x + 4, y + SEARCH_BAR_HEIGHT + 6, 0xFFFFFF, false);
        }

        if (searchResults.isEmpty() && !query.isEmpty()) {
            context.drawText(client.textRenderer, "\u00a77No results", x + 4, y + SEARCH_BAR_HEIGHT + 24, 0xAAAAAA, false);
            return;
        }

        int startY = y + SEARCH_BAR_HEIGHT + 22;
        int itemsToDraw = Math.min(searchResults.size() - scrollOffset * ITEMS_PER_ROW,
                ITEMS_PER_ROW * ITEMS_VISIBLE_ROWS);

        hoveredItemId = null;

        for (int i = 0; i < itemsToDraw; i++) {
            int index = scrollOffset * ITEMS_PER_ROW + i;
            if (index >= searchResults.size()) break;

            String itemId = searchResults.get(index);
            int row = i / ITEMS_PER_ROW;
            int col = i % ITEMS_PER_ROW;

            int itemX = x + 4 + col * (ITEM_SIZE + ITEM_PADDING);
            int itemY = startY + row * (ITEM_SIZE + ITEM_PADDING);

            int mouseX = (int) client.mouse.getX() / client.options.getGuiScale().getValue();
            int mouseY = (int) client.mouse.getY() / client.options.getGuiScale().getValue();

            if (mouseX >= itemX && mouseX < itemX + ITEM_SIZE && mouseY >= itemY && mouseY < itemY + ITEM_SIZE) {
                context.fill(itemX - 1, itemY - 1, itemX + ITEM_SIZE + 1, itemY + ITEM_SIZE + 1, 0x40FFFFFF);
                hoveredItemId = itemId;
            }

            ItemStack stack = ItemRepo.getInstance().createItemStack(itemId);
            if (stack != null) {
                context.drawItem(stack, itemX, itemY);
            }
        }

        int totalPages = Math.max(1, (searchResults.size() + ITEMS_PER_ROW * ITEMS_VISIBLE_ROWS - 1) / (ITEMS_PER_ROW * ITEMS_VISIBLE_ROWS));
        int currentPage = scrollOffset + 1;
        String pageText = currentPage + "/" + totalPages;
        context.drawText(client.textRenderer, pageText, x + PANE_WIDTH / 2 - client.textRenderer.getWidth(pageText) / 2,
                y + paneHeight - 14, 0xAAAAAA, false);
    }

    private void drawInfoPane(DrawContext context, int x, int y, int screenWidth, int screenHeight, MinecraftClient client) {
        int paneHeight = screenHeight;

        context.fill(x, y, x + PANE_WIDTH, y + paneHeight, 0xC0000000);
        context.drawBorder(x, y, PANE_WIDTH, paneHeight, 0xFF555555);

        if (selectedItem == null) return;

        ItemRepo repo = ItemRepo.getInstance();
        String displayName = repo.getDisplayName(selectedItem);
        String cleanName = displayName != null ? TextUtils.stripColorCodes(displayName) : selectedItem;

        context.drawText(client.textRenderer, "\u00a7b" + cleanName, x + 4, y + 4, 0xFFFFFF, true);

        ItemStack stack = repo.createItemStack(selectedItem);
        if (stack != null) {
            context.drawItem(stack, x + 4, y + 20);
        }

        List<NeuRecipe> recipes = repo.getRecipesFor(selectedItem);
        List<NeuRecipe> usages = repo.getUsagesFor(selectedItem);

        int infoY = y + 42;
        if (!recipes.isEmpty()) {
            context.drawText(client.textRenderer, "\u00a7aRecipes: " + recipes.size(), x + 4, infoY, 0xFFFFFF, false);
            infoY += 12;
        }
        if (!usages.isEmpty()) {
            context.drawText(client.textRenderer, "\u00a79Usages: " + usages.size(), x + 4, infoY, 0xFFFFFF, false);
            infoY += 12;
        }

        var itemJson = repo.getItemJson(selectedItem);
        if (itemJson != null && itemJson.has("clickcommand")) {
            String clickCommand = itemJson.get("clickcommand").getAsString();
            context.drawText(client.textRenderer, "\u00a77Click: " + clickCommand, x + 4, infoY, 0xAAAAAA, false);
            infoY += 12;
        }

        if (recipeHistory.canGoBack()) {
            context.drawText(client.textRenderer, "\u00a77[\u00a7e\u00a7l\u2190\u00a77] Back", x + 4, y + paneHeight - 28, 0xAAAAAA, false);
        }
        if (recipeHistory.canGoForward()) {
            context.drawText(client.textRenderer, "\u00a77[\u00a7e\u00a7l\u2192\u00a77] Forward", x + 4, y + paneHeight - 14, 0xAAAAAA, false);
        }
    }

    private void drawSearchBar(DrawContext context, int paneX, MinecraftClient client) {
        int barX = paneX + 2;
        int barY = SEARCH_BAR_HEIGHT + 2;
        int barWidth = PANE_WIDTH - 4;

        context.fill(barX, barY, barX + barWidth, barY + 12, 0xFF000000);
        context.drawBorder(barX, barY, barWidth, 12, 0xFF555555);
    }

    public boolean onMouseClick(double mouseX, double mouseY, int button) {
        if (!overlayEnabled) return false;

        MinecraftClient client = MinecraftClient.getInstance();
        if (!(client.currentScreen instanceof HandledScreen)) return false;

        if (hoveredItemId != null) {
            if (button == 0) {
                selectedItem = hoveredItemId;
                recipeHistory.push(hoveredItemId, true);
                return true;
            } else if (button == 1) {
                searchText = "";
                searchResults = new ArrayList<>(ItemRepo.getInstance().getItemMap().keySet());
                scrollOffset = 0;
                return true;
            }
        }
        return false;
    }

    public boolean onMouseScroll(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!overlayEnabled) return false;

        int scroll = (int) Math.signum(verticalAmount);
        int maxScroll = Math.max(0, (searchResults.size() - 1) / (ITEMS_PER_ROW * ITEMS_VISIBLE_ROWS));
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - scroll));
        return true;
    }

    public boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        if (!overlayEnabled) return false;

        if (searchFocused) {
            return true;
        }
        return false;
    }

    public boolean onCharTyped(char chr, int modifiers) {
        if (!overlayEnabled || !searchFocused) return false;

        searchText += chr;
        performSearch();
        return true;
    }

    public void performSearch() {
        ItemRepo repo = ItemRepo.getInstance();
        if (searchText.isEmpty()) {
            searchResults = new ArrayList<>(repo.getItemMap().keySet());
        } else {
            searchResults = repo.search(searchText);
        }
        scrollOffset = 0;
    }

    public void toggleOverlay() {
        overlayEnabled = !overlayEnabled;
        if (overlayEnabled) {
            searchFocused = true;
            performSearch();
        } else {
            searchFocused = false;
            searchText = "";
            selectedItem = null;
        }
    }

    public void viewRecipe() {
        if (!overlayEnabled) return;
        MinecraftClient client = MinecraftClient.getInstance();
        String hovered = getHoveredItemId(client);
        if (hovered != null) {
            selectedItem = hovered;
            recipeHistory.push(hovered, true);
            if (!overlayEnabled) {
                overlayEnabled = true;
                performSearch();
            }
        }
    }

    public void viewUsages() {
        if (!overlayEnabled) return;
        MinecraftClient client = MinecraftClient.getInstance();
        String hovered = getHoveredItemId(client);
        if (hovered != null) {
            selectedItem = hovered;
            recipeHistory.push(hovered, false);
            if (!overlayEnabled) {
                overlayEnabled = true;
                performSearch();
            }
        }
    }

    public void navigatePrevious() {
        RecipeHistory.Entry entry = recipeHistory.goBack();
        if (entry != null) {
            selectedItem = entry.itemId;
        }
    }

    public void navigateNext() {
        RecipeHistory.Entry entry = recipeHistory.goForward();
        if (entry != null) {
            selectedItem = entry.itemId;
        }
    }

    @Nullable
    private String getHoveredItemId(@NotNull MinecraftClient client) {
        if (hoveredItemId != null) return hoveredItemId;

        if (client.currentScreen instanceof HandledScreen<?> handledScreen) {
            var handler = handledScreen.getScreenHandler();
            var hoveredSlot = handledScreen.getSlotAt(
                    client.mouse.getX() / client.options.getGuiScale().getValue(),
                    client.mouse.getY() / client.options.getGuiScale().getValue()
            );
            if (hoveredSlot != null && hoveredSlot.hasStack()) {
                ItemStack stack = hoveredSlot.getStack();
                String resolved = new ItemResolutionQuery().withItemStack(stack).resolve();
                if (resolved != null) return resolved;
            }
        }
        return null;
    }

    public void toggleFavourite() {
    }

    public boolean isOverlayEnabled() {
        return overlayEnabled;
    }

    public boolean isSearchFocused() {
        return searchFocused;
    }

    @Nullable
    public String getHoveredItemId() {
        return hoveredItemId;
    }

    @Nullable
    public String getSelectedItem() {
        return selectedItem;
    }

    public RecipeHistory getRecipeHistory() {
        return recipeHistory;
    }
}
