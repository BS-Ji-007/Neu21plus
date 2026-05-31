package io.github.legentpc.neu21plus.client.overlay;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.client.accessory.AccessoryHelper;
import io.github.legentpc.neu21plus.client.bazaar.BazaarHelper;
import io.github.legentpc.neu21plus.client.collection.CollectionDisplay;
import io.github.legentpc.neu21plus.client.dungeon.DungeonFeatures;
import io.github.legentpc.neu21plus.client.dungeon.DungeonMap;
import io.github.legentpc.neu21plus.client.dungeon.DungeonWinMessage;
import io.github.legentpc.neu21plus.client.dungeon.PuzzleSolver;
import io.github.legentpc.neu21plus.client.fairysoul.FairySouls;
import io.github.legentpc.neu21plus.client.mayor.MayorDisplay;
import io.github.legentpc.neu21plus.client.mining.DrillFuelBar;
import io.github.legentpc.neu21plus.client.mining.MetalDetectorSolver;
import io.github.legentpc.neu21plus.client.mining.MiningFeatures;
import io.github.legentpc.neu21plus.client.mining.MiningOverlay;
import io.github.legentpc.neu21plus.client.mining.FossilSolver;
import io.github.legentpc.neu21plus.client.misc.MiscFeatures;
import io.github.legentpc.neu21plus.client.actionbar.ActionBarDisplay;
import io.github.legentpc.neu21plus.client.gui.GuiItemRecipe;
import io.github.legentpc.neu21plus.client.notification.NotificationSystem;
import io.github.legentpc.neu21plus.client.storage.StorageViewer;
import io.github.legentpc.neu21plus.client.tab.TabOverlay;
import io.github.legentpc.neu21plus.config.NeuConfig;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OverlayRenderer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OverlayRenderer.class);

    private static final int DUNGEON_MAP_SIZE = 80;

    private static final OverlayRenderer INSTANCE = new OverlayRenderer();

    public static OverlayRenderer getInstance() {
        return INSTANCE;
    }

    private boolean registered = false;

    private OverlayRenderer() {
    }

    public void register() {
        if (registered) return;

        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof AbstractContainerScreen) {
                registerForAbstractContainerScreen(screen, client);
            }

            if (screen instanceof GuiItemRecipe) {
                registerForRecipeScreen(screen, client);
            }
        });

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                Identifier.fromNamespaceAndPath("neu21plus", "all_overlays"),
                (drawContext, deltaTracker) -> {
                    Minecraft client = Minecraft.getInstance();
                    renderDungeonOverlays(drawContext, client);
                    renderMiningOverlays(drawContext, client);
                    renderMiscOverlays(drawContext, client);
                    renderMayorOverlay(drawContext, client);
                    renderFairySoulWaypoints(drawContext, client, deltaTracker.getGameTimeDeltaPartialTick(true));
                    renderFairySoulTracker(drawContext, client);
                    renderStorageOverlay(drawContext, client);
                    renderAccessoryOverlay(drawContext, client);
                    renderBazaarOverlay(drawContext, client);
                    renderCollectionOverlay(drawContext, client);
                    renderActionBarOverlay(drawContext, client);
                    renderTabOverlay(drawContext, client);
                }
        );

        registered = true;
        LOGGER.info("Overlay renderer registered");
    }

    private void registerForAbstractContainerScreen(net.minecraft.client.gui.screens.Screen screen, Minecraft client) {
        NEUOverlay overlay = NEUOverlay.getInstance();

        ScreenEvents.afterExtract(screen).register((s, drawContext, mouseX, mouseY, tickDelta) -> {
            overlay.render(drawContext, client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight());
            NotificationSystem.getInstance().render(drawContext, client.getWindow().getGuiScaledWidth());
            renderDungeonOverlays(drawContext, client);
            renderMiningOverlays(drawContext, client);
            renderMiscOverlays(drawContext, client);
            renderMayorOverlay(drawContext, client);
            renderStorageOverlay(drawContext, client);
            renderAccessoryOverlay(drawContext, client);
            renderBazaarOverlay(drawContext, client);
            renderCollectionOverlay(drawContext, client);
            renderActionBarOverlay(drawContext, client);
        });

        ScreenMouseEvents.beforeMouseClick(screen).register((s, event) -> {
            if (overlay.onMouseClick(event.x(), event.y(), event.button())) {
                return;
            }
            io.github.legentpc.neu21plus.client.buttons.InventoryButtons.getInstance().onMouseClick(event.x(), event.y(), event.button());
        });

        ScreenMouseEvents.beforeMouseScroll(screen).register((s, mouseX, mouseY, horizontalAmount, verticalAmount) -> {
            if (overlay.onMouseScroll(mouseX, mouseY, horizontalAmount, verticalAmount)) {
                return;
            }
        });

        ScreenKeyboardEvents.beforeKeyPress(screen).register((s, event) -> {
            overlay.onKeyPress(event.key(), event.scancode(), event.modifiers());
        });
    }

    private void registerForRecipeScreen(net.minecraft.client.gui.screens.Screen screen, Minecraft client) {
        ScreenEvents.afterExtract(screen).register((s, drawContext, mouseX, mouseY, tickDelta) -> {
            NotificationSystem.getInstance().render(drawContext, client.getWindow().getGuiScaledWidth());
            renderDungeonOverlays(drawContext, client);
            renderMiningOverlays(drawContext, client);
            renderMiscOverlays(drawContext, client);
        });
    }

    private void renderDungeonOverlays(net.minecraft.client.gui.GuiGraphicsExtractor drawContext, Minecraft client) {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config == null) return;

        DungeonFeatures dungeonFeatures = DungeonFeatures.getInstance();
        if (!dungeonFeatures.isInDungeon()) return;

        if (config.dungeons.dungeonMap) {
            float scale = config.dungeons.dungeonMapScale;
            int mapSize = (int) (DUNGEON_MAP_SIZE * scale);
            int mapX = 4;
            int mapY = client.getWindow().getGuiScaledHeight() - mapSize - 4;

            DungeonMap.getInstance().render(drawContext, mapX, mapY, mapSize);
        }

        if (config.dungeons.dungeonWinMessage) {
            DungeonWinMessage.getInstance().render(drawContext);
        }

        if (config.dungeons.blazeOverlay) {
            dungeonFeatures.getPuzzleSolver().renderBlazeOverlay(drawContext);
        }

        if (config.dungeons.scoreDisplay) {
            renderScoreOverlay(drawContext, client, dungeonFeatures);
        }
    }

    private void renderScoreOverlay(net.minecraft.client.gui.GuiGraphicsExtractor drawContext, Minecraft client,
                                    DungeonFeatures dungeonFeatures) {
        var score = dungeonFeatures.getScore();
        var grade = score.getGrade();

        int screenWidth = client.getWindow().getGuiScaledWidth();

        String gradeText = grade.getDisplay().replaceAll("\u00a7.", "");
        String scoreText = "Score: " + score.getTotalScore();

        int x = screenWidth - Math.max(client.font.width(gradeText), client.font.width(scoreText)) - 6;
        int y = 4;

        if (dungeonFeatures.isInDungeon() && !dungeonFeatures.getDungeonMap().isDungeonActive()) {
            return;
        }

        drawContext.text(client.font, gradeText, x, y, grade.getColor(), true);
        drawContext.text(client.font, scoreText, x, y + client.font.lineHeight + 1, 0xFFAAAAAA, true);

        if (score.getSecretCount() > 0 || score.getSecretTotal() > 0) {
            String secretText = "Secrets: " + score.getSecretCount() + "/" + score.getSecretTotal();
            drawContext.text(client.font, secretText, x, y + (client.font.lineHeight + 1) * 2, 0xFF55FF55, true);
        }

        if (score.getDeathCount() > 0) {
            String deathText = "Deaths: " + score.getDeathCount();
            drawContext.text(client.font, deathText, x, y + (client.font.lineHeight + 1) * 3, 0xFFFF5555, true);
        }
    }

    private void renderMiningOverlays(net.minecraft.client.gui.GuiGraphicsExtractor drawContext, Minecraft client) {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config == null) return;

        MiningFeatures miningFeatures = MiningFeatures.getInstance();
        if (!miningFeatures.isInMiningArea()) return;

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        if (config.mining.drillFuelBar) {
            DrillFuelBar.getInstance().render(drawContext, screenWidth, screenHeight);
        }

        if (config.mining.metalDetectorSolver) {
            MetalDetectorSolver.getInstance().render(drawContext, screenWidth, screenHeight);
        }

        if (config.mining.miningOverlay) {
            MiningOverlay.getInstance().render(drawContext, screenWidth, screenHeight);
        }

        if (config.mining.fossilSolver) {
            FossilSolver.getInstance().render(drawContext, screenWidth, screenHeight);
        }
    }

    private void renderMiscOverlays(net.minecraft.client.gui.GuiGraphicsExtractor drawContext, Minecraft client) {
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        MiscFeatures.getInstance().render(drawContext, screenWidth, screenHeight);
    }

    private void renderMayorOverlay(net.minecraft.client.gui.GuiGraphicsExtractor drawContext, Minecraft client) {
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        MayorDisplay.getInstance().render(drawContext, screenWidth, screenHeight);
    }

    private void renderFairySoulWaypoints(net.minecraft.client.gui.GuiGraphicsExtractor drawContext, Minecraft client, float partialTicks) {
        FairySouls.getInstance().renderWaypoints(drawContext, partialTicks);
    }

    private void renderFairySoulTracker(net.minecraft.client.gui.GuiGraphicsExtractor drawContext, Minecraft client) {
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        FairySouls.getInstance().renderTracker(drawContext, screenWidth, screenHeight);
    }

    private void renderStorageOverlay(net.minecraft.client.gui.GuiGraphicsExtractor drawContext, Minecraft client) {
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        StorageViewer.getInstance().render(drawContext, screenWidth, screenHeight);
    }

    private void renderAccessoryOverlay(net.minecraft.client.gui.GuiGraphicsExtractor drawContext, Minecraft client) {
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        AccessoryHelper.getInstance().render(drawContext, screenWidth, screenHeight);
    }

    private void renderBazaarOverlay(net.minecraft.client.gui.GuiGraphicsExtractor drawContext, Minecraft client) {
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        BazaarHelper.getInstance().render(drawContext, screenWidth, screenHeight);
    }

    private void renderCollectionOverlay(net.minecraft.client.gui.GuiGraphicsExtractor drawContext, Minecraft client) {
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        CollectionDisplay.getInstance().render(drawContext, screenWidth, screenHeight);
    }

    private void renderActionBarOverlay(net.minecraft.client.gui.GuiGraphicsExtractor drawContext, Minecraft client) {
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        ActionBarDisplay.getInstance().render(drawContext, screenWidth, screenHeight);
    }

    private void renderTabOverlay(net.minecraft.client.gui.GuiGraphicsExtractor drawContext, Minecraft client) {
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        TabOverlay.getInstance().render(drawContext, screenWidth, screenHeight);
    }
}
