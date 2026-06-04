/*
 * Copyright (C) 2022-2026 NotEnoughUpdates contributors
 */

package io.github.moulberry.notenoughupdates.listener;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.BackgroundBlur;
import io.github.moulberry.notenoughupdates.cosmetics.CapeManager;
import io.github.moulberry.notenoughupdates.dungeons.DungeonBlocks;
import io.github.moulberry.notenoughupdates.dungeons.DungeonWin;
import io.github.moulberry.notenoughupdates.miscfeatures.CookieWarning;
import io.github.moulberry.notenoughupdates.miscfeatures.CrystalMetalDetectorSolver;
import io.github.moulberry.notenoughupdates.miscfeatures.CrystalOverlay;
import io.github.moulberry.notenoughupdates.miscfeatures.FairySouls;
import io.github.moulberry.notenoughupdates.miscfeatures.NPCRetexturing;
import io.github.moulberry.notenoughupdates.miscgui.AccessoryBagOverlay;
import io.github.moulberry.notenoughupdates.miscgui.GuiCustomEnchant;
import io.github.moulberry.notenoughupdates.miscgui.GuiItemRecipe;
import io.github.moulberry.notenoughupdates.miscgui.StorageOverlay;
import io.github.moulberry.notenoughupdates.miscgui.hex.GuiCustomHex;
import io.github.moulberry.notenoughupdates.miscgui.itemcustomization.ItemCustomizeManager;
import io.github.moulberry.notenoughupdates.overlays.OverlayManager;
import io.github.moulberry.notenoughupdates.overlays.TextOverlay;
import io.github.moulberry.notenoughupdates.overlays.TextTabOverlay;
import io.github.moulberry.notenoughupdates.recipes.RecipeHistory;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.NotificationHandler;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.TabSkillInfoParser;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NEUEventListener {

	private final NotEnoughUpdates neu;
	private final ExecutorService itemPreloader = Executors.newFixedThreadPool(10);
	private final List<ItemStack> toPreload = new ArrayList<>();
	private boolean joinedSB = false;

	private boolean preloadedItems = false;
	private long lastLongUpdate = 0;
	private long lastSkyblockScoreboard = 0;

	public NEUEventListener(NotEnoughUpdates neu) {
		this.neu = neu;
	}

    public void registerEvents() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> onTick());
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> onWorldLoad());
    }

	public void onWorldLoad() {
		NotEnoughUpdates.INSTANCE.saveConfig();
		CrystalMetalDetectorSolver.initWorld();
	}

	public void onTick() {
		if (Minecraft.getInstance().level == null) return;
		if (Minecraft.getInstance().player == null) return;

		if (neu.hasSkyblockScoreboard()) {
			if (!preloadedItems) {
				preloadedItems = true;
				List<JsonObject> list = new ArrayList<>(neu.manager.getItemInformation().values());
				for (JsonObject json : list) {
					itemPreloader.submit(() -> {
						ItemStack stack = neu.manager.jsonToStack(json, true, false);
						if (stack.getItem() == Items.PLAYER_HEAD) toPreload.add(stack);
					});
				}
			} else if (!toPreload.isEmpty()) {
				ItemStack itemStack = toPreload.get(0);
				if (itemStack != null && itemStack.getItem() != null) {
					GameProfile gameprofile = null;
					if (itemStack.hasTag()) {
						CompoundTag nbttagcompound = itemStack.getTag();
						if (nbttagcompound.contains("SkullOwner", 10)) {
							gameprofile = NbtUtils.readGameProfile(nbttagcompound.getCompound("SkullOwner"));
						}
					}

					SkinManager skinManager = Minecraft.getInstance().getSkinManager();
					if (gameprofile != null) {
                        // Modern skin loading logic is different, keeping placeholder concept
					}
				}
				toPreload.remove(0);
			} else {
				itemPreloader.shutdown();
			}

			for (TextOverlay overlay : OverlayManager.textOverlays) {
				overlay.shouldUpdateFrequent = true;
			}
		}

		boolean longUpdate = false;
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastLongUpdate > 1000) {
			longUpdate = true;
			lastLongUpdate = currentTime;
		}
		if (!NotEnoughUpdates.INSTANCE.config.dungeons.slowDungeonBlocks) {
			DungeonBlocks.tick();
		}
		DungeonWin.tick();

		String containerName = null;
		if (Minecraft.getInstance().screen instanceof ContainerScreen) {
			ContainerScreen eventGui = (ContainerScreen) Minecraft.getInstance().screen;
            // Screen/Menu logic changed significantly in 1.20+
		}

		//MiningOverlay and TimersOverlay need real tick speed
		if (neu.hasSkyblockScoreboard()) {
			for (TextOverlay overlay : OverlayManager.textOverlays) {
				if (overlay instanceof TextTabOverlay) {
					TextTabOverlay skillOverlay = (TextTabOverlay) overlay;
					skillOverlay.realTick();
				}
			}
		}


		if (longUpdate) {

			if (!(Minecraft.getInstance().screen instanceof GuiItemRecipe)) {
				RecipeHistory.clear();
			}

			CrystalOverlay.tick();
			FairySouls.getInstance().tick();
			TabSkillInfoParser.parseSkillInfo();
			ItemCustomizeManager.tick();
			BackgroundBlur.markDirty();
			NPCRetexturing.getInstance().tick();
			StorageOverlay.getInstance().markDirty();
			CookieWarning.checkCookie();

			if (neu.hasSkyblockScoreboard()) {
				for (TextOverlay overlay : OverlayManager.textOverlays) {
					overlay.tick();
				}
			}

			NotEnoughUpdates.INSTANCE.overlay.redrawItems();

			NotEnoughUpdates.profileViewer.putNameUuid(
				Minecraft.getInstance().player.getName().getString(),
				Minecraft.getInstance().player.getUUID().toString().replace("-", "")
			);

			if (NotEnoughUpdates.INSTANCE.config.dungeons.slowDungeonBlocks) {
				DungeonBlocks.tick();
			}

			if (System.currentTimeMillis() - SBInfo.getInstance().joinedWorld > 500 &&
				System.currentTimeMillis() - SBInfo.getInstance().unloadedWorld > 500) {
				neu.updateSkyblockScoreboard();
			}
			CapeManager.getInstance().tick();

			if (neu.hasSkyblockScoreboard()) {
				SBInfo.getInstance().tick();
				lastSkyblockScoreboard = currentTime;
				if (!joinedSB) {
					joinedSB = true;

					if (NotEnoughUpdates.INSTANCE.config.notifications.doRamNotif) {
						long maxMemoryMB = Runtime.getRuntime().maxMemory() / 1024L / 1024L;
						if (maxMemoryMB > 4100) {
							NotificationHandler.displayNotification(Lists.newArrayList(
								ChatFormatting.GRAY + "Too much memory allocated!",
								String.format(
									ChatFormatting.DARK_GRAY + "NEU has detected %03dMB of memory allocated to Minecraft!",
									maxMemoryMB
								),
								ChatFormatting.GRAY + "It is recommended to allocated between 2-4GB of memory",
								ChatFormatting.GRAY + "More than 4GB MAY cause FPS issues, EVEN if you have 16GB+ available",
								ChatFormatting.GRAY + "For more information, visit #ram-info in " + Utils.getDiscordInvite(),
								"",
								ChatFormatting.GRAY + "Press X on your keyboard to close this notification"
							), false);
						}
					}

					if (!NotEnoughUpdates.INSTANCE.config.hidden.loadedModBefore) {
						NotEnoughUpdates.INSTANCE.config.hidden.loadedModBefore = true;
						if (Constants.MISC == null || !Constants.MISC.has("featureslist")) {
							Utils.showOutdatedRepoNotification("misc.json");
							Utils.addChatMessage(
								"" + ChatFormatting.GOLD + "To view the feature list after restarting type /neufeatures");
						} else {
							String url = Constants.MISC.get("featureslist").getAsString();
							Utils.addChatMessage("");
							Utils.addChatMessage(ChatFormatting.BLUE + "It seems this is your first time using NotEnoughUpdates.");
							Component clickTextFeatures = Component.literal(ChatFormatting.YELLOW +
								"Click this message if you would like to view a list of NotEnoughUpdate's Features.");
							// Utils.createClickStyle needs porting too
							Minecraft.getInstance().player.sendSystemMessage(clickTextFeatures);
						}
						Utils.addChatMessage("");
						Component clickTextHelp = Component.literal(ChatFormatting.YELLOW +
							"Click this message if you would like to view a list of NotEnoughUpdate's commands.");
						Minecraft.getInstance().player.sendSystemMessage(clickTextHelp);
						Utils.addChatMessage("");
					}
				}
			}
			if (currentTime - lastSkyblockScoreboard < 5 * 60 * 1000) { //5 minutes
				neu.manager.auctionManager.tick();
			}
		}
	}
}
