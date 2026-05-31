package io.github.legentpc.neu21plus.client.misc;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.client.notification.NotificationSystem;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.skyblock.SBInfo;
import io.github.legentpc.neu21plus.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiscFeatures {

    private static final Logger LOGGER = LoggerFactory.getLogger(MiscFeatures.class);

    private static final MiscFeatures INSTANCE = new MiscFeatures();

    private static final Pattern SKYBLOCK_LEVEL_PATTERN = Pattern.compile(
            ".*\\[(\\d+)].*"
    );
    private static final Pattern MAGICAL_SOUP_PATTERN = Pattern.compile(
            ".*(?:Magical Soup|Cookie Buff).*(?:activated|enabled|applied).*"
    );
    private static final Pattern GOD_POTION_PATTERN = Pattern.compile(
            ".*God Potion.*(?:activated|applied|enabled|consumed).*"
    );
    private static final Pattern BOOSTER_PATTERN = Pattern.compile(
            ".*(?:Booster Cookie|Cookie Buff).*(\\d+[hmd]).*"
    );
    private static final Pattern COLLECTION_PATTERN = Pattern.compile(
            ".*Collection: (.+?) - (\\d+(?:,\\d+)*)"
    );
    private static final Pattern SKILL_LEVEL_PATTERN = Pattern.compile(
            "\\s*(Farming|Mining|Combat|Foraging|Fishing|Enchanting|Alchemy|Taming|Carpentry|Runecrafting) (\\d+)"
    );

    public static MiscFeatures getInstance() {
        return INSTANCE;
    }

    private final SlayerTracker slayerTracker;
    private final FarmingFeatures farmingFeatures;
    private final PetInfo petInfo;
    private final AuctionHouseHelper auctionHelper;
    private final WardrobeFeatures wardrobeFeatures;

    private int skyblockLevel = 0;
    private boolean hasCookieBuff = false;
    private boolean hasGodPotion = false;
    private String cookieTimeRemaining = "";
    private int tickCount = 0;

    private final int[] skillLevels = new int[10];

    private MiscFeatures() {
        slayerTracker = SlayerTracker.getInstance();
        farmingFeatures = FarmingFeatures.getInstance();
        petInfo = PetInfo.getInstance();
        auctionHelper = AuctionHouseHelper.getInstance();
        wardrobeFeatures = WardrobeFeatures.getInstance();
    }

    public void tick() {
        tickCount++;

        slayerTracker.tick();
        farmingFeatures.tick();
        petInfo.tick();
        auctionHelper.tick();
        wardrobeFeatures.tick();

        if (tickCount % 100 == 0) {
            checkCookieBuffExpiry();
        }
    }

    private void checkCookieBuffExpiry() {
        if (hasCookieBuff && cookieTimeRemaining.isEmpty()) {
            return;
        }
    }

    public void onChatMessage(Component message) {
        String text = message.getString();
        String cleaned = TextUtils.stripColorCodes(text).trim();

        Matcher levelMatcher = SKYBLOCK_LEVEL_PATTERN.matcher(cleaned);
        if (levelMatcher.find()) {
            skyblockLevel = TextUtils.parseIntSafe(levelMatcher.group(1), skyblockLevel);
        }

        if (MAGICAL_SOUP_PATTERN.matcher(cleaned).find() || BOOSTER_PATTERN.matcher(cleaned).find()) {
            hasCookieBuff = true;
            Matcher timeMatcher = BOOSTER_PATTERN.matcher(cleaned);
            if (timeMatcher.find()) {
                cookieTimeRemaining = timeMatcher.group(1);
            }
        }

        if (GOD_POTION_PATTERN.matcher(cleaned).find()) {
            hasGodPotion = true;
        }

        Matcher skillMatcher = SKILL_LEVEL_PATTERN.matcher(cleaned);
        if (skillMatcher.find()) {
            String skillName = skillMatcher.group(1);
            int level = Integer.parseInt(skillMatcher.group(2));
            int index = getSkillIndex(skillName);
            if (index >= 0 && index < skillLevels.length) {
                skillLevels[index] = level;
            }
        }

        slayerTracker.onChatMessage(message);
        farmingFeatures.onChatMessage(message);
        petInfo.onChatMessage(message);
        wardrobeFeatures.onChatMessage(message);
    }

    private int getSkillIndex(String skill) {
        return switch (skill) {
            case "Farming" -> 0;
            case "Mining" -> 1;
            case "Combat" -> 2;
            case "Foraging" -> 3;
            case "Fishing" -> 4;
            case "Enchanting" -> 5;
            case "Alchemy" -> 6;
            case "Taming" -> 7;
            case "Carpentry" -> 8;
            case "Runecrafting" -> 9;
            default -> -1;
        };
    }

    public void render(GuiGraphicsExtractor context, int screenWidth, int screenHeight) {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config == null) return;

        Minecraft client = Minecraft.getInstance();
        SBInfo sbInfo = SBInfo.getInstance();
        if (!sbInfo.hasSkyblockScoreboard()) return;

        if (config.misc.slayerTracker) {
            slayerTracker.render(context, screenWidth, screenHeight);
        }

        if (config.misc.farmingOverlay) {
            farmingFeatures.render(context, screenWidth, screenHeight);
        }

        if (config.misc.petDisplay) {
            petInfo.render(context, screenWidth, screenHeight);
        }

        if (config.misc.auctionHelper) {
            auctionHelper.render(context, screenWidth, screenHeight);
        }

        if (config.misc.wardrobeStats) {
            wardrobeFeatures.render(context, screenWidth, screenHeight);
        }

        if (config.misc.cookieBuffTimer && (hasCookieBuff || hasGodPotion)) {
            renderBuffOverlay(context, client, screenWidth);
        }
    }

    private void renderBuffOverlay(GuiGraphicsExtractor context, Minecraft client, int screenWidth) {
        int x = screenWidth - 120;
        int y = 50;

        context.fill(x - 2, y - 2, x + 118, y + 28, 0x80000000);
        context.outline(x - 2, y - 2, 120, 30, 0xFF555555);

        if (hasCookieBuff) {
            String cookieText = "\u00a76Cookie Buff" + (!cookieTimeRemaining.isEmpty() ? " " + cookieTimeRemaining : "");
            context.text(client.font, cookieText, x + 2, y + 2, 0xFFFFAA00, true);
        }

        if (hasGodPotion) {
            String godText = "\u00a75God Potion Active";
            context.text(client.font, godText, x + 2, y + 2 + (hasCookieBuff ? client.font.lineHeight + 2 : 0), 0xFFAA00AA, true);
        }
    }

    public void reset() {
        slayerTracker.reset();
        farmingFeatures.reset();
        petInfo.reset();
        auctionHelper.reset();
        wardrobeFeatures.reset();

        skyblockLevel = 0;
        hasCookieBuff = false;
        hasGodPotion = false;
        cookieTimeRemaining = "";
        tickCount = 0;
    }

    public SlayerTracker getSlayerTracker() {
        return slayerTracker;
    }

    public FarmingFeatures getFarmingFeatures() {
        return farmingFeatures;
    }

    public PetInfo getPetInfo() {
        return petInfo;
    }

    public AuctionHouseHelper getAuctionHelper() {
        return auctionHelper;
    }

    public WardrobeFeatures getWardrobeFeatures() {
        return wardrobeFeatures;
    }

    public int getSkyblockLevel() {
        return skyblockLevel;
    }

    public boolean hasCookieBuff() {
        return hasCookieBuff;
    }

    public boolean hasGodPotion() {
        return hasGodPotion;
    }

    public int getSkillLevel(String skill) {
        int index = getSkillIndex(skill);
        if (index >= 0 && index < skillLevels.length) {
            return skillLevels[index];
        }
        return 0;
    }

}
