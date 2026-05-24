package io.github.legentpc.neu21plus.client.misc;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.skyblock.SBInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SlayerTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlayerTracker.class);

    private static final SlayerTracker INSTANCE = new SlayerTracker();

    private static final Pattern SLAYER_QUEST_START = Pattern.compile(
            "\\s*(Spider|Zombie|Wolf|Enderman|Blaze|Vampire) Slayer LVL (\\d)"
    );
    private static final Pattern SLAYER_BOSS_SPAWN = Pattern.compile(
            ".+?(Revenant|Tarantula|Sven|Voidgloom|Inferno|Bloodfiend) .+?has spawned!"
    );
    private static final Pattern SLAYER_BOSS_SLAIN = Pattern.compile(
            ".+?(?:BOSS|boss) (?:SLAIN|slain|defeated).+?(?:Revenant|Tarantula|Sven|Voidgloom|Inferno|Bloodfiend)"
    );
    private static final Pattern SLAYER_RNG_METER = Pattern.compile(
            ".*RNG Meter:.*?(\\d+\\.?\\d*)%?"
    );
    private static final Pattern SLAYER_XP_DROP = Pattern.compile(
            "\\s*\\+(\\d+(?:,\\d+)*) (?:Spider|Zombie|Wolf|Enderman|Blaze) XP"
    );

    public static SlayerTracker getInstance() {
        return INSTANCE;
    }

    public enum SlayerType {
        ZOMBIE("Zombie", "Revenant", 0xFFFF5555),
        SPIDER("Spider", "Tarantula", 0xFF5555FF),
        WOLF("Wolf", "Sven", 0xFF55FFFF),
        ENDERMAN("Enderman", "Voidgloom", 0xFFAA00AA),
        BLAZE("Blaze", "Inferno", 0xFFFFAA00),
        VAMPIRE("Vampire", "Bloodfiend", 0xFFFF55FF);

        private final String name;
        private final String bossName;
        private final int color;

        SlayerType(String name, String bossName, int color) {
            this.name = name;
            this.bossName = bossName;
            this.color = color;
        }

        public String getName() {
            return name;
        }

        public String getBossName() {
            return bossName;
        }

        public int getColor() {
            return color;
        }

        public static SlayerType fromName(String name) {
            for (SlayerType type : values()) {
                if (type.name.equalsIgnoreCase(name)) return type;
            }
            return null;
        }

        public static SlayerType fromBossName(String bossName) {
            for (SlayerType type : values()) {
                if (type.bossName.equalsIgnoreCase(bossName)) return type;
            }
            return null;
        }
    }

    private SlayerType activeSlayer = null;
    private int slayerLevel = 0;
    private int bossKills = 0;
    private int totalXpGained = 0;
    private float rngMeterPercent = 0.0f;
    private long lastBossKillTime = -1;
    private boolean bossAlive = false;
    private long slayerStartTime = -1;

    private SlayerTracker() {
    }

    public void tick() {
        if (activeSlayer != null && bossAlive) {
            checkBossTimeout();
        }
    }

    public void onChatMessage(Component message) {
        String text = message.getString();
        String cleaned = stripColorCodes(text).trim();

        Matcher questMatcher = SLAYER_QUEST_START.matcher(cleaned);
        if (questMatcher.find()) {
            String typeStr = questMatcher.group(1);
            int level = Integer.parseInt(questMatcher.group(2));
            onSlayerQuestStart(typeStr, level);
            return;
        }

        Matcher spawnMatcher = SLAYER_BOSS_SPAWN.matcher(cleaned);
        if (spawnMatcher.find()) {
            String bossName = spawnMatcher.group(1);
            onBossSpawn(bossName);
            return;
        }

        Matcher slainMatcher = SLAYER_BOSS_SLAIN.matcher(cleaned);
        if (slainMatcher.find()) {
            onBossSlain();
            return;
        }

        Matcher rngMatcher = SLAYER_RNG_METER.matcher(cleaned);
        if (rngMatcher.find()) {
            try {
                rngMeterPercent = Float.parseFloat(rngMatcher.group(1));
            } catch (NumberFormatException ignored) {
            }
            return;
        }

        Matcher xpMatcher = SLAYER_XP_DROP.matcher(cleaned);
        if (xpMatcher.find()) {
            try {
                String amountStr = xpMatcher.group(1).replace(",", "");
                int amount = Integer.parseInt(amountStr);
                totalXpGained += amount;
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private void onSlayerQuestStart(String typeStr, int level) {
        SlayerType type = SlayerType.fromName(typeStr);
        if (type != null) {
            activeSlayer = type;
            slayerLevel = level;
            bossAlive = false;
            slayerStartTime = System.currentTimeMillis();
            LOGGER.debug("Slayer quest started: {} LVL {}", typeStr, level);
        }
    }

    private void onBossSpawn(String bossName) {
        SlayerType type = SlayerType.fromBossName(bossName);
        if (type != null) {
            activeSlayer = type;
            bossAlive = true;
            LOGGER.debug("Slayer boss spawned: {}", bossName);
        }
    }

    private void onBossSlain() {
        if (activeSlayer != null) {
            bossKills++;
            lastBossKillTime = System.currentTimeMillis();
            bossAlive = false;
            LOGGER.debug("Slayer boss slain (total: {})", bossKills);
        }
    }

    private void checkBossTimeout() {
        if (slayerStartTime > 0 && System.currentTimeMillis() - slayerStartTime > 300000) {
            bossAlive = false;
            slayerStartTime = -1;
        }
    }

    public void render(GuiGraphicsExtractor context, int screenWidth, int screenHeight) {
        if (activeSlayer == null) return;

        Minecraft client = Minecraft.getInstance();
        int x = 4;
        int y = screenHeight - 60;

        String slayerName = activeSlayer.getName() + " LVL " + slayerLevel;
        context.text(client.font, slayerName, x, y, activeSlayer.getColor(), true);

        String killsText = "Kills: " + bossKills;
        context.text(client.font, killsText, x, y + client.font.lineHeight + 2, 0xFFAAAAAA, true);

        if (rngMeterPercent > 0) {
            String rngText = "RNG: " + String.format("%.1f", rngMeterPercent) + "%";
            context.text(client.font, rngText, x, y + (client.font.lineHeight + 2) * 2, 0xFF55FF55, true);

            int barWidth = 80;
            int barHeight = 3;
            int barY = y + (client.font.lineHeight + 2) * 3;
            context.fill(x, barY, x + barWidth, barY + barHeight, 0xFF333333);
            int filledWidth = (int) (barWidth * rngMeterPercent / 100.0f);
            context.fill(x, barY, x + filledWidth, barY + barHeight, 0xFF55FF55);
        }

        if (bossAlive) {
            String bossText = "\u00a7cBOSS ALIVE!";
            context.text(client.font, bossText, x + 90, y, 0xFFFF5555, true);
        }
    }

    public void reset() {
        activeSlayer = null;
        slayerLevel = 0;
        bossKills = 0;
        totalXpGained = 0;
        rngMeterPercent = 0.0f;
        lastBossKillTime = -1;
        bossAlive = false;
        slayerStartTime = -1;
    }

    public SlayerType getActiveSlayer() {
        return activeSlayer;
    }

    public int getBossKills() {
        return bossKills;
    }

    public int getTotalXpGained() {
        return totalXpGained;
    }

    public float getRngMeterPercent() {
        return rngMeterPercent;
    }

    public boolean isBossAlive() {
        return bossAlive;
    }

    private String stripColorCodes(String text) {
        return text.replaceAll("\u00a7[0-9a-fk-orA-FK-OR]", "");
    }
}
