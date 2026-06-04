/*
 * Copyright (C) 2022-2026 NotEnoughUpdates contributors
 */

package io.github.moulberry.notenoughupdates.options;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.Config;
import io.github.notenoughupdates.moulconfig.Social;
import io.github.notenoughupdates.moulconfig.annotations.Category;
import io.github.notenoughupdates.moulconfig.gui.MoulConfigEditor;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.config.GuiPositionEditor;
import io.github.moulberry.notenoughupdates.core.config.Position;
import io.github.moulberry.notenoughupdates.dungeons.GuiDungeonMapEditor;
import io.github.moulberry.notenoughupdates.miscfeatures.FairySouls;
import io.github.moulberry.notenoughupdates.miscfeatures.HotmDesires;
import io.github.moulberry.notenoughupdates.miscfeatures.IQTest;
import io.github.moulberry.notenoughupdates.miscgui.GuiEnchantColour;
import io.github.moulberry.notenoughupdates.miscgui.GuiInvButtonEditor;
import io.github.moulberry.notenoughupdates.miscgui.NEUOverlayPlacements;
import io.github.moulberry.notenoughupdates.miscgui.customtodos.CustomTodo;
import io.github.moulberry.notenoughupdates.options.customtypes.NEUDebugFlag;
import io.github.moulberry.notenoughupdates.options.separatesections.*;
import io.github.moulberry.notenoughupdates.overlays.MiningOverlay;
import io.github.moulberry.notenoughupdates.overlays.OverlayManager;
import io.github.moulberry.notenoughupdates.overlays.TextOverlay;
import io.github.moulberry.notenoughupdates.util.NotificationHandler;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class NEUConfig extends Config {
	public void editOverlay() {
		final LinkedHashMap<TextOverlay, Position> overlayPositions = new LinkedHashMap<TextOverlay, Position>();
		for (TextOverlay overlay : OverlayManager.textOverlays) {
			overlayPositions.put(overlay, overlay.getPosition());
		}
		Screen savedGui = Minecraft.getInstance().screen;
		Minecraft.getInstance().setScreen(new GuiPositionEditor(overlayPositions, () -> {
		}, () -> NotEnoughUpdates.INSTANCE.openGui = savedGui));
	}

	public static Screen editOverlayForCommand() {
		final LinkedHashMap<TextOverlay, Position> overlayPositions = new LinkedHashMap<TextOverlay, Position>();
		for (TextOverlay overlay : OverlayManager.textOverlays) {
			overlayPositions.put(overlay, overlay.getPosition());
		}
		return new GuiPositionEditor(overlayPositions, () -> {
		}, () -> {
		});
	}

	@Override
	public void saveNow() {
		NotEnoughUpdates.INSTANCE.saveConfig();
	}

	private Social social(String name, String iconName, String link) {
		return new Social() {
			@Override
			public void onClick() {
				Utils.openUrl(link);
			}

			@Override
			public List<String> getTooltip() {
				return Arrays.asList(name, "§7Open " + link);
			}

			@Override
			public ResourceLocation getIcon() {
				return new ResourceLocation("notenoughupdates", "social/" + iconName + ".png");
			}
		};
	}

	@Override
	public List<Social> getSocials() {
		return Arrays.asList(
			social("Twitch", "twitch", "https://twitch.tv/moulberry2"),
			social("Patreon", "patreon", "https://patreon.com/moulberry"),
			social("YouTube", "youtube", "https://www.youtube.com/channel/UCPh-OKmRSS3IQi9p6YppLcw"),
			social("Twitter", "twitter", "https://twitter.com/moulberry/"),
			social("GitHub", "github", "https://github.com/NotEnoughUpdates/NotEnoughUpdates"),
			social("Discord", "discord", "https://" + Utils.getDiscordInvite())
		);
	}

	@Override
	public String getTitle() {
		return "§7NotEnoughUpdates " + NotEnoughUpdates.VERSION + " by §5Moulberry";
	}

	@Override
	public void executeRunnable(int runnableId) {
		String activeConfigCategory = null;
		if (Minecraft.getInstance().screen instanceof io.github.notenoughupdates.moulconfig.gui.GuiScreenElementWrapper) {
			io.github.notenoughupdates.moulconfig.gui.GuiScreenElementWrapper wrapper =
				(io.github.notenoughupdates.moulconfig.gui.GuiScreenElementWrapper) Minecraft.getInstance().screen;
			if (wrapper.element instanceof MoulConfigEditor) {
				activeConfigCategory = ((MoulConfigEditor) wrapper.element).getSelectedCategory();
			}
		}

		switch (runnableId) {
			case -1:
				return;
			case 0:
				Screen savedGui = Minecraft.getInstance().screen;
				NotEnoughUpdates.INSTANCE.openGui = new GuiDungeonMapEditor(() -> {
					NotEnoughUpdates.INSTANCE.openGui = savedGui;
				});
				return;
			case 1:
			case 4:
				editOverlay();
				return;
			case 6:
				NotEnoughUpdates.INSTANCE.openGui = new NEUOverlayPlacements();
				return;
			case 7:
				NotEnoughUpdates.INSTANCE.openGui = new GuiInvButtonEditor();
				return;
			case 8:
				NotEnoughUpdates.INSTANCE.openGui = new GuiEnchantColour();
				return;
			case 12:
				executeRunnableCommand("/dn");
				return;
			case 13:
				executeRunnableCommand("/pv");
				return;
			case 15:
				String command = NotEnoughUpdates.INSTANCE.config.misc.fariySoul ? "/neusouls on" : "/neusouls off";
				executeRunnableCommand(command);
				return;
			case 16:
				executeRunnableCommand("/neusouls clear");
				return;
			case 17:
				executeRunnableCommand("/neusouls unclear");
				return;
			case 20:
				FairySouls.getInstance().setTrackFairySouls(NotEnoughUpdates.INSTANCE.config.misc.trackFairySouls);
				return;
			case 21:
				NotEnoughUpdates.INSTANCE.overlay.updateSearch();
				return;
			case 22:
				NotEnoughUpdates.INSTANCE.manager
					.userFacingRepositoryReload()
					.thenAccept(strings ->
						NotificationHandler.displayNotification(strings, true, true));
				Minecraft.getInstance().setScreen(null);
				return;
			case 23:
				NotEnoughUpdates.INSTANCE.config.apiData.repoUser = "NotEnoughUpdates";
				NotEnoughUpdates.INSTANCE.config.apiData.repoName = "NotEnoughUpdates-REPO";
				NotEnoughUpdates.INSTANCE.config.apiData.repoBranch = "master";
				return;
			case 26:
				OverlayManager.powderGrindingOverlay.reset();
				return;
			case 27:
				IQTest.testIQ();
				return;
			case 28:
				executeRunnableCommand("/neuresetslotlocking");
				return;
			default:
				System.err.printf("Unknown runnableId = %d in category %s%n", runnableId, activeConfigCategory);
		}
	}

	private void executeRunnableCommand(String command) {
		if (Minecraft.getInstance().player == null) {
			System.err.println("Command (" + command + ") not executed since you are not in a world.");
			return;
		}
        // Command handling in Fabric needs porting
	}

	@Expose @Category(name = "About", desc = "") public About about = new About();
	@Expose @Category(name = "Misc", desc = "") public Misc misc = new Misc();
	@Expose @Category(name = "GUI Locations", desc = "") public LocationEdit locationedit = new LocationEdit();
	@Expose @Category(name = "Notifications", desc = "") public Notifications notifications = new Notifications();
	@Expose @Category(name = "Item List", desc = "") public Itemlist itemlist = new Itemlist();
	@Expose @Category(name = "Toolbar", desc = "") public Toolbar toolbar = new Toolbar();
	@Expose @Category(name = "Inventory Buttons", desc = "") public InventoryButtons inventoryButtons = new InventoryButtons();
	@Expose @Category(name = "Slot Locking", desc = "") public SlotLocking slotLocking = new SlotLocking();
	@Expose @Category(name = "Tooltip Tweaks", desc = "") public TooltipTweaks tooltipTweaks = new TooltipTweaks();
	@Expose @Category(name = "Item Overlays", desc = "") public ItemOverlays itemOverlays = new ItemOverlays();
	@Expose @Category(name = "Skill Overlays", desc = "") public SkillOverlays skillOverlays = new SkillOverlays();
	@Expose @Category(name = "Todo Overlays", desc = "") public MiscOverlays miscOverlays = new MiscOverlays();
	@Expose @Category(name = "Slayer Overlay", desc = "") public SlayerOverlay slayerOverlay = new SlayerOverlay();
	@Expose @Category(name = "Storage GUI", desc = "") public StorageGUI storageGUI = new StorageGUI();
	@Expose @Category(name = "Dungeons", desc = "") public Dungeons dungeons = new Dungeons();
	@Expose @Category(name = "Enchanting GUI/Solvers", desc = "") public Enchanting enchantingSolvers = new Enchanting();
	@Expose @Category(name = "Mining", desc = "") public Mining mining = new Mining();
	@Expose @Category(name = "Fishing", desc = "") public Fishing fishing = new Fishing();
	@Expose @Category(name = "Garden", desc = "") public Garden garden = new Garden();
	@Expose @Category(name = "Improved SB Menus", desc = "") public ImprovedSBMenu improvedSBMenu = new ImprovedSBMenu();
	@Expose @Category(name = "Equipment Hud", desc = "") public CustomArmour customArmour = new CustomArmour();
	@Expose @Category(name = "Calendar", desc = "") public Calendar calendar = new Calendar();
	@Expose @Category(name = "Trade Menu", desc = "") public TradeMenu tradeMenu = new TradeMenu();
	@Expose @Category(name = "Pet Overlay", desc = "") public PetOverlay petOverlay = new PetOverlay();
	@Expose @Category(name = "World Renderer", desc = "") public WorldConfig world = new WorldConfig();
	@Expose @Category(name = "AH Tweaks", desc = "") public AHTweaks ahTweaks = new AHTweaks();
	@Expose @Category(name = "Bazaar Tweaks", desc = "") public BazaarTweaks bazaarTweaks = new BazaarTweaks();
	@Expose @Category(name = "Recipe Tweaks", desc = "") public RecipeTweaks recipeTweaks = new RecipeTweaks();
	@Expose @Category(name = "Price Graph", desc = "") public AHGraph ahGraph = new AHGraph();
	@Expose @Category(name = "Wardrobe Keybinds", desc = "") public WardrobeKeybinds wardrobeKeybinds = new WardrobeKeybinds();
	@Expose @Category(name = "Accessory Bag Overlay", desc = "") public AccessoryBag accessoryBag = new AccessoryBag();
	@Expose @Category(name = "Museum", desc = "") public Museum museum = new Museum();
	@Expose @Category(name = "Profile Viewer", desc = "") public ProfileViewer profileViewer = new ProfileViewer();
	@Expose @Category(name = "Minion Helper", desc = "") public MinionHelper minionHelper = new MinionHelper();
	@Expose @Category(name = "Apis", desc = "") public ApiData apiData = new ApiData();

	@Expose public Hidden hidden = new Hidden();
	@Expose public DungeonMapConfig dungeonMap = new DungeonMapConfig();

	public static class Hidden {
		@Expose public List<CustomTodo> customTodos = new ArrayList<>();
		@Expose public HashMap<String, HiddenProfileSpecific> profileSpecific = new HashMap<>();
		@Expose public HashMap<String, HiddenLocationSpecific> locationSpecific = new HashMap<>();
		@Expose public List<InventoryButton> inventoryButtons = new ArrayList<>();
		@Expose public String overlaySearchBar = "";
		@Expose public String overlayQuickCommand = "";
		@Expose public boolean dev = false;
		@Expose public boolean loadedModBefore = false;
		@Expose public String selectedCape = null;
		@Expose public ArrayList<String> favourites = new ArrayList<>();
	}

	public static class HiddenProfileSpecific {
		@Expose public long godPotionDuration = 0L;
	}

	public static class HiddenLocationSpecific {
		@Expose public Map<String, Integer> commissionMaxes = new HashMap<>();
	}

	public static class InventoryButton {
		@Expose public int x;
		@Expose public int y;
		@Expose public String command;
		public boolean isActive() { return command != null && !command.trim().isEmpty(); }
	}
}
