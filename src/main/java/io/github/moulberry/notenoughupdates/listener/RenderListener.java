/*
 * Copyright (C) 2022-2026 NotEnoughUpdates contributors
 */

package io.github.moulberry.notenoughupdates.listener;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NEUApi;
import io.github.moulberry.notenoughupdates.NEUOverlay;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.dungeons.DungeonWin;
import io.github.moulberry.notenoughupdates.miscfeatures.AuctionBINWarning;
import io.github.moulberry.notenoughupdates.miscfeatures.BetterContainers;
import io.github.moulberry.notenoughupdates.miscfeatures.CrystalMetalDetectorSolver;
import io.github.moulberry.notenoughupdates.miscfeatures.EnchantingSolvers;
import io.github.moulberry.notenoughupdates.miscfeatures.HexPriceWarning;
import io.github.moulberry.notenoughupdates.miscfeatures.PresetWarning;
import io.github.moulberry.notenoughupdates.miscfeatures.StorageManager;
import io.github.moulberry.notenoughupdates.miscgui.AccessoryBagOverlay;
import io.github.moulberry.notenoughupdates.miscgui.CalendarOverlay;
import io.github.moulberry.notenoughupdates.miscgui.GuiCustomEnchant;
import io.github.moulberry.notenoughupdates.miscgui.GuiInvButtonEditor;
import io.github.moulberry.notenoughupdates.miscgui.GuiItemRecipe;
import io.github.moulberry.notenoughupdates.miscgui.StorageOverlay;
import io.github.moulberry.notenoughupdates.miscgui.TradeWindow;
import io.github.moulberry.notenoughupdates.miscgui.hex.GuiCustomHex;
import io.github.moulberry.notenoughupdates.options.NEUConfig;
import io.github.moulberry.notenoughupdates.overlays.OverlayManager;
import io.github.moulberry.notenoughupdates.overlays.TextOverlay;
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer;
import io.github.moulberry.notenoughupdates.util.ItemUtils;
import io.github.moulberry.notenoughupdates.util.NotificationHandler;
import io.github.moulberry.notenoughupdates.util.Rectangle;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.ScreenReplacer;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RenderListener {
	private static final ResourceLocation EDITOR = new ResourceLocation("notenoughupdates", "invbuttons/editor.png");
	public static boolean disableCraftingText = false;
	public static boolean drawingGuiScreen = false;
	public static long lastGuiClosed = 0;
	public static boolean inventoryLoaded = false;
	private final NotEnoughUpdates neu;
	private final Pattern ESSENCE_PATTERN = Pattern.compile("§d(.+) Essence §8x([\\d,]+)");
	ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
	private boolean hoverInv = false;
	private boolean focusInv = false;
	private boolean doInventoryButtons = false;
	private NEUConfig.InventoryButton buttonHovered = null;
	private long buttonHoveredMillis = 0;
	private String loadedInvName = "";
	private int lastTickCount = 0;
	private int ticksStable = 0;
	private static final int REQUIRED_STABLE_TICKS = 10;

	public RenderListener(NotEnoughUpdates neu) {
		this.neu = neu;
	}

    public void registerEvents() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> onTick());
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> onRenderHud(drawContext, tickDelta));
        WorldRenderEvents.LAST.register(context -> onRenderWorld(context.tickDelta()));
    }

	public void onRenderHud(Object drawContext, float tickDelta) {
		if (neu.hasSkyblockScoreboard()) {
			DungeonWin.render(tickDelta);
            // Hud rendering logic needs modern DrawContext in 26.1+
		}
		NotificationHandler.renderNotification();
	}

    public void onRenderWorld(float partialTicks) {
        CrystalMetalDetectorSolver.render(partialTicks);
    }

	public void onTick() {
		if (Minecraft.getInstance().level == null) return;
		if (Minecraft.getInstance().player == null) return;

		if (Minecraft.getInstance().screen instanceof ContainerScreen) {
            // Stability check for inventory loading
		}
	}
}
