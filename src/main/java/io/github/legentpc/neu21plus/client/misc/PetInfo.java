package io.github.legentpc.neu21plus.client.misc;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.skyblock.SBInfo;
import io.github.legentpc.neu21plus.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PetInfo {

    private static final Logger LOGGER = LoggerFactory.getLogger(PetInfo.class);

    private static final PetInfo INSTANCE = new PetInfo();

    private static final Pattern PET_LEVEL_PATTERN = Pattern.compile(
            "\\[Lvl (\\d+)] (.+)"
    );
    private static final Pattern PET_XP_PATTERN = Pattern.compile(
            ".*XP: ([\\d,.]+)/(?<max>[\\d,.]+)"
    );
    private static final Pattern PET_ITEM_PATTERN = Pattern.compile(
            ".*Held Item: (.+)"
    );
    private static final Pattern PET_SITTER_PATTERN = Pattern.compile(
            ".*(?:Auto|Sitter|Pet Sitter).*"
    );

    public static PetInfo getInstance() {
        return INSTANCE;
    }

    public enum PetRarity {
        COMMON("Common", 0xFFAAAAAA, 100),
        UNCOMMON("Uncommon", 0xFF55FF55, 200),
        RARE("Rare", 0xFF5555FF, 300),
        EPIC("Epic", 0xFFAA00AA, 500),
        LEGENDARY("Legendary", 0xFFFFAA00, 1000),
        MYTHIC("Mythic", 0xFFFF55FF, 2000);

        private final String name;
        private final int color;
        private final int maxLevel;

        PetRarity(String name, int color, int maxLevel) {
            this.name = name;
            this.color = color;
            this.maxLevel = maxLevel;
        }

        public String getName() {
            return name;
        }

        public int getColor() {
            return color;
        }

        public int getMaxLevel() {
            return maxLevel;
        }

        public static PetRarity fromName(String name) {
            for (PetRarity rarity : values()) {
                if (rarity.name.equalsIgnoreCase(name)) return rarity;
            }
            return COMMON;
        }
    }

    private String activePetName = "";
    private int activePetLevel = 0;
    private PetRarity activePetRarity = PetRarity.COMMON;
    private double currentXp = 0;
    private double maxXp = 0;
    private String heldItem = "";
    private boolean petActive = false;

    private PetInfo() {
    }

    public void tick() {
        updatePetFromInventory();
    }

    private void updatePetFromInventory() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        SBInfo sbInfo = SBInfo.getInstance();
        if (!sbInfo.hasSkyblockScoreboard()) {
            petActive = false;
            return;
        }
    }

    public void onChatMessage(Component message) {
        String text = message.getString();
        String cleaned = TextUtils.stripColorCodes(text).trim();

        Matcher levelMatcher = PET_LEVEL_PATTERN.matcher(cleaned);
        if (levelMatcher.find()) {
            int parsedLevel = TextUtils.parseIntSafe(levelMatcher.group(1), -1);
            if (parsedLevel >= 0) {
                activePetLevel = parsedLevel;
                activePetName = levelMatcher.group(2).trim();
                petActive = true;
                LOGGER.debug("Pet detected: LVL {} {}", activePetLevel, activePetName);
            }
            return;
        }

        Matcher xpMatcher = PET_XP_PATTERN.matcher(cleaned);
        if (xpMatcher.find()) {
            String currentStr = xpMatcher.group(1).replace(",", "").replace(".", "");
            String maxStr = xpMatcher.group("max").replace(",", "").replace(".", "");
            currentXp = TextUtils.parseDoubleSafe(currentStr, currentXp);
            maxXp = TextUtils.parseDoubleSafe(maxStr, maxXp);
            return;
        }

        Matcher itemMatcher = PET_ITEM_PATTERN.matcher(cleaned);
        if (itemMatcher.find()) {
            heldItem = itemMatcher.group(1).trim();
        }
    }

    public void parsePetFromTooltip(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;

        String displayName = TextUtils.stripColorCodes(stack.getDisplayName().getString()).trim();

        Matcher levelMatcher = PET_LEVEL_PATTERN.matcher(displayName);
        if (levelMatcher.find()) {
            int parsedLevel = TextUtils.parseIntSafe(levelMatcher.group(1), -1);
            if (parsedLevel >= 0) {
                activePetLevel = parsedLevel;
                activePetName = levelMatcher.group(2).trim();
                petActive = true;

                for (Component line : stack.getTooltipLines(Item.TooltipContext.EMPTY, null, TooltipFlag.NORMAL)) {
                    String lineText = TextUtils.stripColorCodes(line.getString()).trim();

                    if (lineText.contains("COMMON") || lineText.contains("UNCOMMON")
                            || lineText.contains("RARE") || lineText.contains("EPIC")
                            || lineText.contains("LEGENDARY") || lineText.contains("MYTHIC")) {
                        for (PetRarity rarity : PetRarity.values()) {
                            if (lineText.toUpperCase().contains(rarity.name.toUpperCase())) {
                                activePetRarity = rarity;
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public double getLevelProgress() {
        if (maxXp <= 0) return 0;
        return Math.min(1.0, currentXp / maxXp);
    }

    public double calculatePetXpPerLevel(int level) {
        if (level <= 0) return 0;
        double baseXp = 100;
        double growthRate = 1.08;
        return baseXp * Math.pow(growthRate, level - 1);
    }

    public int getEstimatedMaxLevel() {
        if (activePetRarity == null) return 100;
        return activePetRarity.getMaxLevel();
    }

    public void render(GuiGraphicsExtractor context, int screenWidth, int screenHeight) {
        if (!petActive || activePetName.isEmpty()) return;

        Minecraft client = Minecraft.getInstance();
        int x = screenWidth - 120;
        int y = 4;

        context.fill(x - 2, y - 2, x + 120, y + 40, 0x80000000);
        context.outline(x - 2, y - 2, 122, 42, 0xFF555555);

        String petLabel = activePetName + " [Lvl " + activePetLevel + "]";
        context.text(client.font, petLabel, x + 2, y + 2, activePetRarity.getColor(), true);

        int barY = y + client.font.lineHeight + 6;
        int barWidth = 116;
        int barHeight = 6;

        context.fill(x, barY, x + barWidth, barY + barHeight, 0xFF333333);

        double progress = getLevelProgress();
        int filledWidth = (int) (barWidth * progress);
        int barColor = progress >= 0.9 ? 0xFF55FF55 : progress >= 0.5 ? 0xFFFFFF55 : 0xFF5555FF;
        context.fill(x, barY, x + filledWidth, barY + barHeight, barColor);

        String progressText = String.format("%.1f%%", progress * 100);
        context.text(client.font, progressText,
                x + barWidth / 2 - client.font.width(progressText) / 2,
                barY + barHeight + 2, 0xFFAAAAAA, false);

        if (!heldItem.isEmpty()) {
            String itemText = "Item: " + heldItem;
            context.text(client.font, itemText, x + 2, barY + barHeight + client.font.lineHeight + 4, 0xFFAAAAAA, true);
        }
    }

    public void reset() {
        activePetName = "";
        activePetLevel = 0;
        activePetRarity = PetRarity.COMMON;
        currentXp = 0;
        maxXp = 0;
        heldItem = "";
        petActive = false;
    }

    public String getActivePetName() {
        return activePetName;
    }

    public int getActivePetLevel() {
        return activePetLevel;
    }

    public PetRarity getActivePetRarity() {
        return activePetRarity;
    }

    public boolean isPetActive() {
        return petActive;
    }

    public String getHeldItem() {
        return heldItem;
    }

}
