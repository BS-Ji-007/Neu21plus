package io.github.legentpc.neu21plus.client.misc;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.skyblock.SBInfo;
import io.github.legentpc.neu21plus.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FarmingFeatures {

    private static final Logger LOGGER = LoggerFactory.getLogger(FarmingFeatures.class);

    private static final FarmingFeatures INSTANCE = new FarmingFeatures();

    private static final Pattern FARMING_XP_PATTERN = Pattern.compile(
            "\\s*\\+(\\d+(?:,\\d+)*) Farming XP"
    );
    private static final Pattern CROP_MILESTONE_PATTERN = Pattern.compile(
            ".+?(?:Wheat|Carrot|Potato|Pumpkin|Melon|Mushroom|Cactus|Sugar Cane|Nether Wart|Cocoa Beans) .+?(\\d+)"
    );
    private static final Pattern PEST_PATTERN = Pattern.compile(
            ".+?(?:Pest|pest).+?(?:spawned|appeared|detected)"
    );
    private static final Pattern CONTEST_PATTERN = Pattern.compile(
            ".+?Jacob's Farming Contest.+?starts? in (\\d+)"
    );
    private static final Pattern CONTEST_CROP_PATTERN = Pattern.compile(
            ".*Contest Crop: (.+)"
    );

    public static FarmingFeatures getInstance() {
        return INSTANCE;
    }

    public enum CropType {
        WHEAT("Wheat", 0xFFAAAA00),
        CARROT("Carrot", 0xFFFF6600),
        POTATO("Potato", 0xFFAA8800),
        PUMPKIN("Pumpkin", 0xFFFF8800),
        MELON("Melon", 0xFF55AA55),
        MUSHROOM("Mushroom", 0xFF885588),
        CACTUS("Cactus", 0xFF558800),
        SUGAR_CANE("Sugar Cane", 0xFFCCFFCC),
        NETHER_WART("Nether Wart", 0xFFAA0000),
        COCOA_BEANS("Cocoa Beans", 0xFF885533);

        private final String name;
        private final int color;

        CropType(String name, int color) {
            this.name = name;
            this.color = color;
        }

        public String getName() {
            return name;
        }

        public int getColor() {
            return color;
        }

        public static CropType fromName(String name) {
            for (CropType type : values()) {
                if (type.name.equalsIgnoreCase(name)) return type;
            }
            return null;
        }
    }

    private final Map<CropType, Integer> cropMilestones = new HashMap<>();
    private final Map<CropType, Integer> contestScores = new HashMap<>();

    private int totalFarmingXp = 0;
    private int pestsDetected = 0;
    private long lastPestTime = -1;
    private boolean inContest = false;
    private String contestCrop = "";
    private int contestStartMinutes = -1;
    private boolean inFarmingArea = false;
    private int tickCount = 0;

    private FarmingFeatures() {
    }

    public void tick() {
        tickCount++;

        updateFarmingDetection();

        if (!inFarmingArea) return;

        if (tickCount % 100 == 0) {
            updateContestState();
        }
    }

    private void updateFarmingDetection() {
        SBInfo sbInfo = SBInfo.getInstance();
        String location = sbInfo.getLocation();

        boolean wasInFarmingArea = inFarmingArea;
        inFarmingArea = isFarmingLocation(location);

        if (inFarmingArea && !wasInFarmingArea) {
            LOGGER.debug("Entered farming area");
        } else if (!inFarmingArea && wasInFarmingArea) {
            LOGGER.debug("Exited farming area");
        }
    }

    private boolean isFarmingLocation(String location) {
        if (location == null) return false;
        String lower = location.toLowerCase();
        return lower.contains("garden")
                || lower.contains("farming_")
                || lower.equals("farming")
                || lower.contains("barn");
    }

    private void updateContestState() {
        if (contestStartMinutes > 0) {
            contestStartMinutes--;
            if (contestStartMinutes <= 0) {
                inContest = true;
                contestStartMinutes = -1;
            }
        }
    }

    public void onChatMessage(Component message) {
        String text = message.getString();
        String cleaned = TextUtils.stripColorCodes(text).trim();

        Matcher xpMatcher = FARMING_XP_PATTERN.matcher(cleaned);
        if (xpMatcher.find()) {
            totalFarmingXp += TextUtils.parseIntSafe(xpMatcher.group(1), 0);
        }

        Matcher pestMatcher = PEST_PATTERN.matcher(cleaned);
        if (pestMatcher.find()) {
            pestsDetected++;
            lastPestTime = System.currentTimeMillis();
            LOGGER.debug("Pest detected (total: {})", pestsDetected);
        }

        Matcher contestMatcher = CONTEST_PATTERN.matcher(cleaned);
        if (contestMatcher.find()) {
            contestStartMinutes = TextUtils.parseIntSafe(contestMatcher.group(1), contestStartMinutes);
            LOGGER.debug("Farming contest starts in {} minutes", contestStartMinutes);
        }

        Matcher cropMatcher = CONTEST_CROP_PATTERN.matcher(cleaned);
        if (cropMatcher.find()) {
            contestCrop = cropMatcher.group(1).trim();
        }

        Matcher milestoneMatcher = CROP_MILESTONE_PATTERN.matcher(cleaned);
        if (milestoneMatcher.find()) {
            String raw = cleaned;
            for (CropType crop : CropType.values()) {
                if (raw.contains(crop.getName())) {
                    int level = TextUtils.parseIntSafe(milestoneMatcher.group(1), 0);
                    cropMilestones.put(crop, level);
                    break;
                }
            }
        }
    }

    public void render(GuiGraphicsExtractor context, int screenWidth, int screenHeight) {
        if (!inFarmingArea) return;

        Minecraft client = Minecraft.getInstance();
        int x = 4;
        int y = screenHeight - 100;

        context.text(client.font, "\u00a7aFarming", x, y, 0xFF55FF55, true);

        String xpText = "XP: " + TextUtils.formatNumber(totalFarmingXp);
        context.text(client.font, xpText, x, y + client.font.lineHeight + 2, 0xFFAAAAAA, true);

        if (pestsDetected > 0) {
            String pestText = "Pests: " + pestsDetected;
            int pestColor = lastPestTime > 0 && System.currentTimeMillis() - lastPestTime < 10000
                    ? 0xFFFF5555 : 0xFFAAAAAA;
            context.text(client.font, pestText, x, y + (client.font.lineHeight + 2) * 2, pestColor, true);
        }

        if (inContest && !contestCrop.isEmpty()) {
            String contestText = "Contest: " + contestCrop;
            context.text(client.font, contestText, x, y + (client.font.lineHeight + 2) * 3, 0xFFFFAA00, true);
        }

        if (contestStartMinutes > 0) {
            String countdownText = "Contest in: " + contestStartMinutes + "m";
            context.text(client.font, countdownText, x, y + (client.font.lineHeight + 2) * 3, 0xFFFFFF55, true);
        }
    }

    public void reset() {
        totalFarmingXp = 0;
        pestsDetected = 0;
        lastPestTime = -1;
        inContest = false;
        contestCrop = "";
        contestStartMinutes = -1;
        inFarmingArea = false;
        tickCount = 0;
        cropMilestones.clear();
        contestScores.clear();
    }

    public boolean isInFarmingArea() {
        return inFarmingArea;
    }

    public int getTotalFarmingXp() {
        return totalFarmingXp;
    }

    public int getPestsDetected() {
        return pestsDetected;
    }

    public boolean isInContest() {
        return inContest;
    }

    public String getContestCrop() {
        return contestCrop;
    }

    public int getCropMilestone(CropType crop) {
        return cropMilestones.getOrDefault(crop, 0);
    }

}
