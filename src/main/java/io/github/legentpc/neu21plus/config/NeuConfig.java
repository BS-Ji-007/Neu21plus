package io.github.legentpc.neu21plus.config;

import io.github.notenoughupdates.moulconfig.Config;
import io.github.notenoughupdates.moulconfig.annotations.Category;
import io.github.notenoughupdates.moulconfig.common.text.StructuredText;
import io.github.notenoughupdates.moulconfig.managed.ManagedConfig;

import java.io.File;

public class NeuConfig extends Config {

    private static ManagedConfig<NeuConfig> managedConfig;

    @Category(name = "General", desc = "General settings for Neu21+")
    public GeneralCategory general = new GeneralCategory();

    @Category(name = "Overlay", desc = "Overlay display settings")
    public OverlayCategory overlay = new OverlayCategory();

    @Category(name = "Mining", desc = "Mining related features")
    public MiningCategory mining = new MiningCategory();

    @Category(name = "Dungeons", desc = "Dungeon related features")
    public DungeonsCategory dungeons = new DungeonsCategory();

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

    public void openConfigGui() {
        if (managedConfig != null) {
            managedConfig.openConfigGui();
        }
    }

    public static class GeneralCategory {
        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Auto Update Repository",
                desc = "Automatically update the item repository on startup")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean autoUpdateRepo = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Show Item Rarity",
                desc = "Show item rarity colors in the tooltip")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
        public boolean showItemRarity = true;

        @io.github.notenoughupdates.moulconfig.annotations.ConfigOption(
                name = "Hypixel API Key",
                desc = "Your Hypixel API key for price data (/api new)")
        @io.github.notenoughupdates.moulconfig.annotations.ConfigEditorText
        public String apiKey = "";
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
