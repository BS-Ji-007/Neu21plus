package io.github.legentpc.neu21plus.client.mining;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.skyblock.SBInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiningFeatures {

    private static final Logger LOGGER = LoggerFactory.getLogger(MiningFeatures.class);

    private static final MiningFeatures INSTANCE = new MiningFeatures();

    private static final Pattern COMMISSION_COMPLETE_PATTERN = Pattern.compile(".+ (?:completed|finished) (?:a )?(?:mining )?commission!.*");
    private static final Pattern POWDER_FOUND_PATTERN = Pattern.compile(".*You found (\\d+[,.]?\\d*) (?:Mithril|Gemstone|Glacite) Powder.*");
    private static final Pattern TREASURE_FOUND_PATTERN = Pattern.compile(".*You (?:found|discovered) (?:a )?treasure.*");
    private static final Pattern PICKAXE_ABILITY_PATTERN = Pattern.compile(".*(?:Pickaxe|Drill) Ability.*(?:activated|ready|used).*");

    public static MiningFeatures getInstance() {
        return INSTANCE;
    }

    private final DrillFuelBar drillFuelBar;
    private final MetalDetectorSolver metalDetectorSolver;
    private final MiningOverlay miningOverlay;
    private final FossilSolver fossilSolver;

    private boolean inMiningArea = false;
    private int tickCount = 0;
    private long lastCommissionTime = -1;
    private long lastTreasureTime = -1;
    private long lastPickaxeAbilityTime = -1;
    private int commissionsCompleted = 0;
    private int treasuresFound = 0;
    private int totalPowderGained = 0;

    private MiningFeatures() {
        drillFuelBar = DrillFuelBar.getInstance();
        metalDetectorSolver = MetalDetectorSolver.getInstance();
        miningOverlay = MiningOverlay.getInstance();
        fossilSolver = FossilSolver.getInstance();
    }

    public void tick() {
        updateMiningDetection();

        if (!isInMiningArea()) return;

        tickCount++;

        drillFuelBar.tick();
        metalDetectorSolver.tick();
        miningOverlay.tick();
        fossilSolver.tick();

        if (tickCount % 20 == 0) {
            updateSubFeatures();
        }
    }

    private void updateMiningDetection() {
        SBInfo sbInfo = SBInfo.getInstance();
        String location = sbInfo.getLocation();

        boolean wasInMiningArea = inMiningArea;
        inMiningArea = isMiningLocation(location);

        if (inMiningArea && !wasInMiningArea) {
            onMiningAreaEnter();
        } else if (!inMiningArea && wasInMiningArea) {
            onMiningAreaExit();
        }
    }

    private boolean isMiningLocation(String location) {
        if (location == null) return false;
        String lower = location.toLowerCase();
        return lower.contains("mines")
                || lower.contains("crystal_hollows")
                || lower.contains("dwarven")
                || lower.contains("forge")
                || lower.contains("campfire")
                || lower.contains("crystal_hollows")
                || lower.contains("mining_")
                || lower.equals("mining")
                || lower.contains("dwarven_mines")
                || lower.contains("crystals");
    }

    private void onMiningAreaEnter() {
        LOGGER.info("Entered mining area");
        miningOverlay.onMiningAreaEnter();
    }

    private void onMiningAreaExit() {
        LOGGER.info("Exited mining area");
        reset();
    }

    private void updateSubFeatures() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;

        miningOverlay.parseScoreboard(client);
    }

    public void onChatMessage(Component message) {
        if (!isInMiningArea()) return;

        String text = message.getString();
        String cleaned = stripColorCodes(text).trim();

        Matcher commissionMatcher = COMMISSION_COMPLETE_PATTERN.matcher(cleaned);
        if (commissionMatcher.find()) {
            onCommissionComplete();
        }

        Matcher powderMatcher = POWDER_FOUND_PATTERN.matcher(cleaned);
        if (powderMatcher.find()) {
            try {
                String amountStr = powderMatcher.group(1).replace(",", "").replace(".", "");
                int amount = Integer.parseInt(amountStr);
                onPowderFound(amount, cleaned);
            } catch (NumberFormatException ignored) {
            }
        }

        Matcher treasureMatcher = TREASURE_FOUND_PATTERN.matcher(cleaned);
        if (treasureMatcher.find()) {
            onTreasureFound();
        }

        Matcher pickaxeMatcher = PICKAXE_ABILITY_PATTERN.matcher(cleaned);
        if (pickaxeMatcher.find()) {
            onPickaxeAbility();
        }

        metalDetectorSolver.onChatMessage(message);
        fossilSolver.onChatMessage(message);
    }

    private void onCommissionComplete() {
        commissionsCompleted++;
        lastCommissionTime = System.currentTimeMillis();
        LOGGER.debug("Mining commission completed (total: {})", commissionsCompleted);
    }

    private void onPowderFound(int amount, String context) {
        totalPowderGained += amount;
        if (context.contains("Mithril")) {
            miningOverlay.addMithrilPowder(amount);
        } else if (context.contains("Gemstone")) {
            miningOverlay.addGemstonePowder(amount);
        } else if (context.contains("Glacite")) {
            miningOverlay.addGlacitePowder(amount);
        }
    }

    private void onTreasureFound() {
        treasuresFound++;
        lastTreasureTime = System.currentTimeMillis();
        metalDetectorSolver.onTreasureFound();
        LOGGER.debug("Treasure found in mining area (total: {})", treasuresFound);
    }

    private void onPickaxeAbility() {
        lastPickaxeAbilityTime = System.currentTimeMillis();
    }

    public void reset() {
        inMiningArea = false;
        tickCount = 0;
        commissionsCompleted = 0;
        treasuresFound = 0;
        totalPowderGained = 0;
        lastCommissionTime = -1;
        lastTreasureTime = -1;
        lastPickaxeAbilityTime = -1;
        drillFuelBar.reset();
        metalDetectorSolver.reset();
        miningOverlay.reset();
        fossilSolver.reset();
    }

    public boolean isInMiningArea() {
        return inMiningArea;
    }

    public DrillFuelBar getDrillFuelBar() {
        return drillFuelBar;
    }

    public MetalDetectorSolver getMetalDetectorSolver() {
        return metalDetectorSolver;
    }

    public MiningOverlay getMiningOverlay() {
        return miningOverlay;
    }

    public FossilSolver getFossilSolver() {
        return fossilSolver;
    }

    public int getCommissionsCompleted() {
        return commissionsCompleted;
    }

    public int getTreasuresFound() {
        return treasuresFound;
    }

    public int getTotalPowderGained() {
        return totalPowderGained;
    }

    public long getTimeSinceLastCommission() {
        if (lastCommissionTime < 0) return -1;
        return System.currentTimeMillis() - lastCommissionTime;
    }

    public long getTimeSinceLastTreasure() {
        if (lastTreasureTime < 0) return -1;
        return System.currentTimeMillis() - lastTreasureTime;
    }

    public long getTimeSinceLastPickaxeAbility() {
        if (lastPickaxeAbilityTime < 0) return -1;
        return System.currentTimeMillis() - lastPickaxeAbilityTime;
    }

    private String stripColorCodes(String text) {
        return text.replaceAll("\u00a7[0-9a-fk-orA-FK-OR]", "");
    }
}
