package io.github.legentpc.neu21plus.client.misc;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.skyblock.SBInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WardrobeFeatures {

    private static final Logger LOGGER = LoggerFactory.getLogger(WardrobeFeatures.class);

    private static final WardrobeFeatures INSTANCE = new WardrobeFeatures();

    private static final Pattern WARDROBE_SLOT_PATTERN = Pattern.compile(
            ".*Wardrobe Slot #(\\d+).*"
    );
    private static final Pattern HEALTH_PATTERN = Pattern.compile("\\s*([+-]?\\d+)\\s+\u2764 Health");
    private static final Pattern DEFENSE_PATTERN = Pattern.compile("\\s*([+-]?\\d+)\\s+Defense");
    private static final Pattern SPEED_PATTERN = Pattern.compile("\\s*([+-]?\\d+)\\s+Speed");
    private static final Pattern STRENGTH_PATTERN = Pattern.compile("\\s*([+-]?\\d+)\\s+Strength");

    public static WardrobeFeatures getInstance() {
        return INSTANCE;
    }

    public static class ArmorSet {
        String name;
        int slotNumber;
        int health;
        int defense;
        int speed;
        int strength;
        int intelligence;
        int magicFind;

        ArmorSet(String name, int slotNumber) {
            this.name = name;
            this.slotNumber = slotNumber;
        }

        public int getTotalStats() {
            return health + defense + speed + strength + intelligence + magicFind;
        }
    }

    private final Map<Integer, ArmorSet> savedSets = new HashMap<>();
    private boolean inWardrobe = false;
    private int activeSet = -1;

    private final List<ItemStack> currentArmor = new ArrayList<>();

    private WardrobeFeatures() {
    }

    public void tick() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        SBInfo sbInfo = SBInfo.getInstance();
        String chestName = sbInfo.currentlyOpenChestName;
        boolean wasInWardrobe = inWardrobe;
        inWardrobe = chestName != null && chestName.contains("Wardrobe");

        if (inWardrobe && !wasInWardrobe) {
            LOGGER.debug("Opened wardrobe");
        }

        if (!inWardrobe && wasInWardrobe) {
            LOGGER.debug("Closed wardrobe");
        }

        if (!inWardrobe) {
            updateCurrentArmor(client);
        }
    }

    private void updateCurrentArmor(Minecraft client) {
        currentArmor.clear();
        EquipmentSlot[] armorSlots = {
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        };
        for (EquipmentSlot slot : armorSlots) {
            ItemStack stack = client.player.getItemBySlot(slot);
            if (stack != null && !stack.isEmpty()) {
                currentArmor.add(stack.copy());
            }
        }
    }

    public void onChatMessage(Component message) {
        String text = message.getString();
        String cleaned = stripColorCodes(text).trim();

        Matcher slotMatcher = WARDROBE_SLOT_PATTERN.matcher(cleaned);
        if (slotMatcher.find()) {
            try {
                int slot = Integer.parseInt(slotMatcher.group(1));
                activeSet = slot;
                LOGGER.debug("Active wardrobe slot: {}", slot);
            } catch (NumberFormatException ignored) {
            }
        }
    }

    public void parseWardrobeSlot(ItemStack stack, int slotNumber) {
        if (stack == null || stack.isEmpty()) return;

        String name = stripColorCodes(stack.getDisplayName().getString()).trim();
        ArmorSet set = new ArmorSet(name, slotNumber);

        for (Component line : stack.getTooltipLines(Item.TooltipContext.EMPTY, null, TooltipFlag.NORMAL)) {
            String lineText = stripColorCodes(line.getString()).trim();

            Matcher healthMatcher = HEALTH_PATTERN.matcher(lineText);
            if (healthMatcher.find()) {
                set.health = Integer.parseInt(healthMatcher.group(1));
                continue;
            }

            Matcher defenseMatcher = DEFENSE_PATTERN.matcher(lineText);
            if (defenseMatcher.find()) {
                set.defense = Integer.parseInt(defenseMatcher.group(1));
                continue;
            }

            Matcher speedMatcher = SPEED_PATTERN.matcher(lineText);
            if (speedMatcher.find()) {
                set.speed = Integer.parseInt(speedMatcher.group(1));
                continue;
            }

            Matcher strengthMatcher = STRENGTH_PATTERN.matcher(lineText);
            if (strengthMatcher.find()) {
                set.strength = Integer.parseInt(strengthMatcher.group(1));
            }
        }

        savedSets.put(slotNumber, set);
        LOGGER.debug("Parsed wardrobe set {}: {} (HP:{} DEF:{} SPD:{} STR:{})",
                slotNumber, name, set.health, set.defense, set.speed, set.strength);
    }

    public void render(GuiGraphicsExtractor context, int screenWidth, int screenHeight) {
        if (!inWardrobe || savedSets.isEmpty()) return;

        Minecraft client = Minecraft.getInstance();
        int x = screenWidth - 140;
        int y = 4;

        int boxHeight = 16 + savedSets.size() * 24;
        context.fill(x - 2, y - 2, x + 138, y + boxHeight, 0x80000000);
        context.outline(x - 2, y - 2, 140, boxHeight + 2, 0xFF5555FF);

        context.text(client.font, "\u00a79Wardrobe Stats", x + 2, y + 2, 0xFF5555FF, true);

        int setY = y + 16;
        for (Map.Entry<Integer, ArmorSet> entry : savedSets.entrySet()) {
            ArmorSet set = entry.getValue();
            boolean isActive = entry.getKey() == activeSet;

            String prefix = isActive ? "\u00a7a> " : "\u00a77  ";
            String nameText = prefix + "#" + entry.getKey() + " " + set.name;
            context.text(client.font, nameText, x + 2, setY, isActive ? 0xFF55FF55 : 0xFFAAAAAA, false);

            String statsText = "  \u00a7c" + set.health + "\u00a77/\u00a79" + set.defense
                    + "\u00a77/\u00a7a" + set.speed + "\u00a77/\u00a76" + set.strength;
            context.text(client.font, statsText, x + 2, setY + 11, 0xFFAAAAAA, false);

            setY += 24;
        }
    }

    public void reset() {
        savedSets.clear();
        inWardrobe = false;
        activeSet = -1;
        currentArmor.clear();
    }

    public boolean isInWardrobe() {
        return inWardrobe;
    }

    public int getActiveSet() {
        return activeSet;
    }

    public Map<Integer, ArmorSet> getSavedSets() {
        return savedSets;
    }

    public List<ItemStack> getCurrentArmor() {
        return currentArmor;
    }

    private String stripColorCodes(String text) {
        return text.replaceAll("\u00a7[0-9a-fk-orA-FK-OR]", "");
    }
}
