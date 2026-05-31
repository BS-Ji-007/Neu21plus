package io.github.legentpc.neu21plus.config;

import io.github.notenoughupdates.moulconfig.Config;
import io.github.notenoughupdates.moulconfig.annotations.Category;
import io.github.notenoughupdates.moulconfig.common.text.StructuredText;
import io.github.notenoughupdates.moulconfig.managed.ManagedConfig;

import java.io.File;

public class NeuConfig extends Config {

    private static ManagedConfig<NeuConfig> managedConfig;

    public static final String DEFAULT_REPO_URL = "https://github.com/NotEnoughUpdates/NotEnoughUpdates-repo";
    public static final String DEFAULT_REPO_SOURCE = "NotEnoughUpdates/NotEnoughUpdates-repo";
    public static final String DEFAULT_REPO_BRANCH = "master";

    @Category(name = "General", desc = "General settings for Neu21+")
    public GeneralCategory general = new GeneralCategory();

    @Category(name = "Overlay", desc = "Overlay display settings")
    public OverlayCategory overlay = new OverlayCategory();

    @Category(name = "Profile Viewer", desc = "Profile Viewer settings")
    public ProfileViewerCategory profileViewer = new ProfileViewerCategory();

    @Category(name = "Storage", desc = "Storage viewer settings")
    public StorageCategory storage = new StorageCategory();

    @Category(name = "Accessories", desc = "Accessory bag and missing talisman settings")
    public AccessoryCategory accessories = new AccessoryCategory();

    @Category(name = "Fairy Souls", desc = "Fairy soul waypoint and tracker settings")
    public FairySoulCategory fairySouls = new FairySoulCategory();

    @Category(name = "Inventory", desc = "Inventory buttons and equipment comparison settings")
    public InventoryCategory inventory = new InventoryCategory();

    @Category(name = "Mining", desc = "Mining related features")
    public MiningCategory mining = new MiningCategory();

    @Category(name = "Dungeons", desc = "Dungeon related features")
    public DungeonsCategory dungeons = new DungeonsCategory();

    @Category(name = "Bazaar", desc = "Bazaar helper and market features")
    public BazaarCategory bazaar = new BazaarCategory();

    @Category(name = "Mayor", desc = "Mayor and election display settings")
    public MayorCategory mayor = new MayorCategory();

    @Category(name = "Display", desc = "Tab overlay and action bar display settings")
    public DisplayCategory display = new DisplayCategory();

    @Category(name = "Misc", desc = "Miscellaneous settings")
    public MiscCategory misc = new MiscCategory();

    @Override
    public StructuredText getTitle() {
        return StructuredText.of("\u00a7bNeu21+ Config");
    }

    public static ManagedConfig<NeuConfig> create(File configFile) {
        managedConfig = ManagedConfig.create(configFile, NeuConfig.class);
        return managedConfig;
    }

    public static ManagedConfig<NeuConfig> getManagedConfig() {
        return managedConfig;
    }

    public static void save() {
        if (managedConfig != null) {
            managedConfig.saveToFile();
        }
    }

    public static void reload() {
        if (managedConfig != null) {
            managedConfig.reloadFromFile();
        }
    }

    public void openConfigGui() {
        if (managedConfig != null) {
            managedConfig.openConfigGui();
        }
    }

    public String getRepoUrl() {
        return DEFAULT_REPO_URL;
    }

    public String getEffectiveRepoSource() {
        if (general.repoSource != null && !general.repoSource.isEmpty()) {
            return general.repoSource;
        }
        return DEFAULT_REPO_SOURCE;
    }

    public String getEffectiveRepoBranch() {
        if (general.repoBranch != null && !general.repoBranch.isEmpty()) {
            return general.repoBranch;
        }
        return DEFAULT_REPO_BRANCH;
    }

    public static class GeneralCategory {
        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Auto Update Repository",
                desc = "Automatically update the item repository from GitHub on startup")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean autoUpdateRepo = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Show Item Rarity",
                desc = "Show item rarity colors in the tooltip")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean showItemRarity = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Hypixel API Key",
                desc = "Your Hypixel API key for price data and profile viewer (/api new)")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText
        public String apiKey = "";

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Repository URL",
                desc = "NEU item data repository URL (always includes the official NEU repo)")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText
        public String repoUrl = DEFAULT_REPO_URL;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Repository Source",
                desc = "GitHub repo for item data (owner/repo format)")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText
        public String repoSource = DEFAULT_REPO_SOURCE;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Repository Branch",
                desc = "Branch to download from the repository")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText
        public String repoBranch = DEFAULT_REPO_BRANCH;
    }

    public static class OverlayCategory {
        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Show Overlay",
                desc = "Show the NEU overlay in inventory")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean showOverlay = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Overlay Scale",
                desc = "Scale of the overlay")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider(
                minValue = 0.5f, maxValue = 2.0f, minStep = 0.1f)
        public float overlayScale = 1.0f;
    }

    public static class ProfileViewerCategory {
        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Enable Profile Viewer",
                desc = "Allow viewing other players' SkyBlock profiles with /pv <name>")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean enabled = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Show Stats",
                desc = "Show player stats in profile viewer (health, defense, etc.)")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean showStats = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Show Skills",
                desc = "Show skill levels and XP in profile viewer")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean showSkills = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Show Inventory",
                desc = "Show player inventory items in profile viewer")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean showInventory = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Show Pets",
                desc = "Show player pets in profile viewer")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean showPets = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Show Collections",
                desc = "Show collection levels in profile viewer")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean showCollections = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Show Networth",
                desc = "Show estimated networth in profile viewer")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean showNetworth = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Cache Duration",
                desc = "How long to cache profile data (seconds)")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider(
                minValue = 30f, maxValue = 300f, minStep = 30f)
        public float cacheDurationSec = 120f;
    }

    public static class StorageCategory {
        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Enable Storage Viewer",
                desc = "Allow viewing storage from anywhere with /storage")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean enabled = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Show Storage Overlay",
                desc = "Show storage preview overlay on right side of screen")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean showOverlay = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Storage Scale",
                desc = "Scale of the storage viewer overlay")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider(
                minValue = 0.5f, maxValue = 2.0f, minStep = 0.1f)
        public float storageScale = 1.0f;
    }

    public static class AccessoryCategory {
        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Show Missing Accessories",
                desc = "Show which accessories/talismans you are missing")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean showMissing = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Show Upgrade Paths",
                desc = "Show talisman upgrade paths in tooltip")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean showUpgradePaths = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Highlight Missing",
                desc = "Highlight missing accessories in red in the overlay")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean highlightMissing = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Show Accessory Bag Overlay",
                desc = "Show accessory bag contents with missing slots when bag is open")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean showBagOverlay = true;
    }

    public static class FairySoulCategory {
        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Enable Fairy Soul Waypoints",
                desc = "Show fairy soul locations as waypoints")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean enabled = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Show Only Missing",
                desc = "Only show waypoints for fairy souls you have not collected")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean showOnlyMissing = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Waypoint Distance",
                desc = "Maximum distance to show fairy soul waypoints")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider(
                minValue = 10f, maxValue = 100f, minStep = 5f)
        public float waypointDistance = 50f;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Show Tracker Overlay",
                desc = "Show fairy soul count overlay (found/total per island)")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean showTracker = true;
    }

    public static class InventoryCategory {
        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Inventory Buttons",
                desc = "Show custom buttons in inventory for quick access")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean inventoryButtons = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Equipment Comparison",
                desc = "Show stat comparison when hovering over equipment")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean equipmentComparison = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Show Craft Cost",
                desc = "Show crafting cost vs auction/bazaar price")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean showCraftCost = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Show NPC Sell Price",
                desc = "Show NPC sell price in tooltip")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean showNpcSellPrice = true;
    }

    public static class MiningCategory {
        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Drill Fuel Bar",
                desc = "Show drill fuel bar overlay")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean drillFuelBar = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Metal Detector Solver",
                desc = "Show metal detector solver in Crystal Hollows")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean metalDetectorSolver = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Mining Overlay",
                desc = "Show mining info overlay with powder and commissions")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean miningOverlay = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Fossil Solver",
                desc = "Show fossil puzzle solver grid in Dwarven Mines")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean fossilSolver = true;
    }

    public static class DungeonsCategory {
        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Dungeon Map",
                desc = "Show dungeon map overlay")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean dungeonMap = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Map Scale",
                desc = "Scale of the dungeon map")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider(
                minValue = 0.5f, maxValue = 2.0f, minStep = 0.1f)
        public float dungeonMapScale = 1.0f;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Dungeon Win Message",
                desc = "Show custom dungeon win message")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean dungeonWinMessage = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Puzzle Solver",
                desc = "Show puzzle solutions in dungeons")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean puzzleSolver = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Blaze Solver Overlay",
                desc = "Show blaze puzzle markers on screen")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean blazeOverlay = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Score Calculation",
                desc = "Show live dungeon score and grade estimate")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean scoreDisplay = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Secret Tracker",
                desc = "Track found and remaining secrets in dungeon")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean secretTracker = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Death Counter",
                desc = "Show death counter in dungeon overlay")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean deathCounter = true;
    }

    public static class BazaarCategory {
        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Bazaar Helper",
                desc = "Show enhanced bazaar information and price alerts")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean bazaarHelper = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Show Sell Offer",
                desc = "Show best sell offer and buy order prices")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean showSellOffer = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Show Craft Profit",
                desc = "Show profit/loss for crafting items from bazaar materials")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean showCraftProfit = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Price Alert Threshold",
                desc = "Percentage change for price alerts (0 = disabled)")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider(
                minValue = 0f, maxValue = 50f, minStep = 5f)
        public float priceAlertThreshold = 10f;
    }

    public static class MayorCategory {
        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Show Mayor Info",
                desc = "Show current mayor and their perks in overlay")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean showMayor = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Show Election Info",
                desc = "Show ongoing election candidates and votes")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean showElection = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Mayor Overlay Position",
                desc = "Show mayor info overlay at top of screen")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean topPosition = true;
    }

    public static class DisplayCategory {
        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Custom Tab Overlay",
                desc = "Replace default tab list with custom SkyBlock info")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean customTab = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Action Bar Display",
                desc = "Show important info in action bar area (HP, defense, mana)")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean actionBarDisplay = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Show FPS",
                desc = "Show FPS counter in display overlay")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean showFps = false;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Show Ping",
                desc = "Show ping in display overlay")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean showPing = false;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Collection Display",
                desc = "Show collection progress overlay")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean collectionDisplay = true;
    }

    public static class MiscCategory {
        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Streamer Mode",
                desc = "Hide sensitive information from display")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean streamerMode = false;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Notification Sound",
                desc = "Play sound for notifications")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean notificationSound = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Slayer Tracker",
                desc = "Show slayer boss kill count, RNG meter, and boss status overlay")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean slayerTracker = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Farming Overlay",
                desc = "Show farming XP, pest counter, and contest info in garden")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean farmingOverlay = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Pet Display",
                desc = "Show active pet level, XP progress, and held item info")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean petDisplay = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Auction Helper",
                desc = "Highlight underpriced BIN auctions in Auction House")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean auctionHelper = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Wardrobe Stats",
                desc = "Show armor set stat comparison in Wardrobe menu")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean wardrobeStats = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Cookie Buff Timer",
                desc = "Show cookie buff and god potion status overlay")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean cookieBuffTimer = true;
    }
}
