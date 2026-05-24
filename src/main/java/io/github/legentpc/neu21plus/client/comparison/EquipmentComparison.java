package io.github.legentpc.neu21plus.client.comparison;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.skyblock.SBInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EquipmentComparison {

    private static final Logger LOGGER = LoggerFactory.getLogger(EquipmentComparison.class);

    private static final EquipmentComparison INSTANCE = new EquipmentComparison();

    private static final Pattern STAT_PATTERN = Pattern.compile(
            "\u00a7[0-9a-fk-orA-FK-OR]?(?:Health|Defense|Speed|Strength|Intelligence|Crit Chance|Crit Damage|" +
                    "Bonus Attack Speed|Sea Creature Chance|Magic Find|Pet Luck|True Defense|Ferocity|Ability Damage|(" +
                    "Mining Speed|Mining Fortune|Farming Fortune|Foraging Fortune)): ([+-]?\\d+)"
    );

    public static EquipmentComparison getInstance() {
        return INSTANCE;
    }

    private EquipmentComparison() {
    }

    public Map<String, Integer> parseItemStats(ItemStack stack) {
        Map<String, Integer> stats = new HashMap<>();
        if (stack.isEmpty()) return stats;

        Minecraft client = Minecraft.getInstance();
        List<Component> tooltip = stack.getTooltipLines(net.minecraft.world.item.Item.TooltipContext.EMPTY, null, net.minecraft.world.item.TooltipFlag.NORMAL);

        for (Component line : tooltip) {
            String text = line.getString();
            parseStatLine(text, stats);
        }

        return stats;
    }

    private void parseStatLine(String text, Map<String, Integer> stats) {
        String[] statNames = {"Health", "Defense", "Speed", "Strength", "Intelligence",
                "Crit Chance", "Crit Damage", "Bonus Attack Speed", "Sea Creature Chance",
                "Magic Find", "Pet Luck", "True Defense", "Ferocity", "Ability Damage",
                "Mining Speed", "Mining Fortune", "Farming Fortune", "Foraging Fortune"};

        for (String stat : statNames) {
            int idx = text.indexOf(stat);
            if (idx >= 0) {
                String after = text.substring(idx + stat.length()).trim();
                Matcher numMatcher = Pattern.compile("([+-]?\\d+)").matcher(after);
                if (numMatcher.find()) {
                    try {
                        int value = Integer.parseInt(numMatcher.group(1));
                        stats.merge(stat, value, Integer::sum);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
    }

    public Map<String, Integer> getDifference(Map<String, Integer> newStats, Map<String, Integer> currentStats) {
        Map<String, Integer> diff = new HashMap<>();

        for (Map.Entry<String, Integer> entry : newStats.entrySet()) {
            int currentVal = currentStats.getOrDefault(entry.getKey(), 0);
            int difference = entry.getValue() - currentVal;
            if (difference != 0) {
                diff.put(entry.getKey(), difference);
            }
        }

        for (Map.Entry<String, Integer> entry : currentStats.entrySet()) {
            if (!newStats.containsKey(entry.getKey()) && entry.getValue() != 0) {
                diff.put(entry.getKey(), -entry.getValue());
            }
        }

        return diff;
    }

    public void renderComparison(GuiGraphicsExtractor context, ItemStack hoveredItem, int mouseX, int mouseY) {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config == null || !config.inventory.equipmentComparison) return;

        SBInfo sbInfo = SBInfo.getInstance();
        if (!sbInfo.hasSkyblockScoreboard()) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        Map<String, Integer> newItemStats = parseItemStats(hoveredItem);
        if (newItemStats.isEmpty()) return;

        EquipmentSlot slot = getEquipmentSlot(hoveredItem);
        if (slot == null) return;

        ItemStack currentItem = client.player.getItemBySlot(slot);
        Map<String, Integer> currentStats = parseItemStats(currentItem);

        Map<String, Integer> diff = getDifference(newItemStats, currentStats);
        if (diff.isEmpty()) return;

        int x = mouseX + 12;
        int y = mouseY - 12;
        int width = 130;
        int lineHeight = client.font.lineHeight + 1;
        int height = diff.size() * lineHeight + 16;

        if (x + width > client.getWindow().getGuiScaledWidth()) {
            x = mouseX - width - 12;
        }
        if (y + height > client.getWindow().getGuiScaledHeight()) {
            y = client.getWindow().getGuiScaledHeight() - height - 4;
        }
        if (y < 4) y = 4;

        context.fill(x - 2, y - 2, x + width + 2, y + height + 2, 0xE0101010);
        context.outline(x - 2, y - 2, width + 4, height + 4, 0xFF555555);

        context.text(client.font, "\u00a77Stat Difference:", x, y, 0xFFAAAAAA, false);
        y += lineHeight + 4;

        for (Map.Entry<String, Integer> entry : diff.entrySet()) {
            int value = entry.getValue();
            String prefix = value > 0 ? "\u00a7a+" : "\u00a7c";
            String text = "\u00a77" + entry.getKey() + ": " + prefix + value;
            context.text(client.font, text, x, y, 0xFFAAAAAA, false);
            y += lineHeight;
        }
    }

    private EquipmentSlot getEquipmentSlot(ItemStack stack) {
        if (stack.isEmpty()) return null;

        String itemId = stack.getItem().toString().toLowerCase();
        if (itemId.contains("helmet") || itemId.contains("hat") || itemId.contains("hood")) {
            return EquipmentSlot.HEAD;
        }
        if (itemId.contains("chestplate") || itemId.contains("tunic") || itemId.contains("robe")) {
            return EquipmentSlot.CHEST;
        }
        if (itemId.contains("leggings") || itemId.contains("pants") || itemId.contains("trousers")) {
            return EquipmentSlot.LEGS;
        }
        if (itemId.contains("boots") || itemId.contains("shoes") || itemId.contains("slippers")) {
            return EquipmentSlot.FEET;
        }

        return null;
    }
}
