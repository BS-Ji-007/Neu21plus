package io.github.legentpc.neu21plus.client.accessory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.itemrepo.ItemRepo;
import io.github.legentpc.neu21plus.skyblock.SBInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AccessoryHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessoryHelper.class);

    private static final AccessoryHelper INSTANCE = new AccessoryHelper();

    public static AccessoryHelper getInstance() {
        return INSTANCE;
    }

    private final Set<String> allAccessories = new HashSet<>();
    private final Set<String> ownedAccessories = new HashSet<>();
    private final Map<String, String> upgradePaths = new HashMap<>();
    private boolean loaded = false;

    private AccessoryHelper() {
    }

    public void loadAccessories() {
        allAccessories.clear();
        upgradePaths.clear();

        ItemRepo repo = ItemRepo.getInstance();
        if (!repo.isLoaded()) return;

        for (String itemId : repo.getItemMap().keySet()) {
            if (isAccessory(itemId)) {
                allAccessories.add(itemId);
            }
        }

        buildUpgradePaths();
        loaded = true;
    }

    private boolean isAccessory(String itemId) {
        String lower = itemId.toLowerCase();
        return lower.contains("talisman") || lower.contains("ring") || lower.contains("artifact")
                || lower.contains("relic") || lower.contains("hegemony") || lower.contains("artifact")
                || lower.contains("accessory") || isKnownAccessory(itemId);
    }

    private boolean isKnownAccessory(String itemId) {
        String[] knownAccessories = {
                "WOLF_TALISMAN", "WOLF_RING", "WOLF_ARTIFACT",
                "SPIDER_TALISMAN", "SPIDER_RING", "SPIDER_ARTIFACT",
                "ZOMBIE_TALISMAN", "ZOMBIE_RING", "ZOMBIE_ARTIFACT",
                "ENDERMITE_TALISMAN", "ENDERMITE_RING", "ENDERMITE_ARTIFACT",
                "BAT_TALISMAN", "BAT_RING", "BAT_ARTIFACT",
                "CRYSTAL_TALISMAN", "CRYSTAL_RING", "CRYSTAL_ARTIFACT",
                "SCARF_TALISMAN", "SCARF_RING", "SCARF_ARTIFACT",
                "GRAVEL_TALISMAN", "GRAVEL_RING", "GRAVEL_ARTIFACT",
                "MAGMA_TALISMAN", "MAGMA_RING", "MAGMA_ARTIFACT",
                "SEA_CREATURE_TALISMAN", "FARMER_ORB", "HUNTER_TALISMAN",
                "TARANTULA_TALISMAN", "CRYPT_GHOOL_TALISMAN", "ZOMBIE_SOLDIER_CUTLAS",
                "RED_CLAW_TALISMAN", "RED_CLAW_RING", "RED_CLAW_ARTIFACT",
                "HEGEMONY_ARTIFACT", "JERRY_TALISMAN", "JERRY_RING", "JERRY_ARTIFACT",
                "BINGO_TALISMAN", "PERSONAL_COMPACTOR", "PERSONAL_DELETOR",
                "CAMPFIRE_TALISMAN", "CAMPFIRE_RING", "CAMPFIRE_ARTIFACT",
                "POCKET_ESPRESSO_MACHINE", "POPCORN_STATION", "SPEED_TALISMAN",
                "SPEED_RING", "SPEED_ARTIFACT", "FIRE_TALISMAN", "FIRE_RING",
                "FIRE_ARTIFACT", "PIGMAN_TALISMAN", "PIGMAN_RING", "PIGMAN_ARTIFACT",
                "ICE_TALISMAN", "ICE_RING", "ICE_ARTIFACT", "SEARING_STONE"
        };

        for (String known : knownAccessories) {
            if (itemId.equals(known)) return true;
        }
        return false;
    }

    private void buildUpgradePaths() {
        String[][] paths = {
                {"WOLF_TALISMAN", "WOLF_RING", "WOLF_ARTIFACT"},
                {"SPIDER_TALISMAN", "SPIDER_RING", "SPIDER_ARTIFACT"},
                {"ZOMBIE_TALISMAN", "ZOMBIE_RING", "ZOMBIE_ARTIFACT"},
                {"ENDERMITE_TALISMAN", "ENDERMITE_RING", "ENDERMITE_ARTIFACT"},
                {"BAT_TALISMAN", "BAT_RING", "BAT_ARTIFACT"},
                {"CRYSTAL_TALISMAN", "CRYSTAL_RING", "CRYSTAL_ARTIFACT"},
                {"SCARF_TALISMAN", "SCARF_RING", "SCARF_ARTIFACT"},
                {"GRAVEL_TALISMAN", "GRAVEL_RING", "GRAVEL_ARTIFACT"},
                {"MAGMA_TALISMAN", "MAGMA_RING", "MAGMA_ARTIFACT"},
                {"SPEED_TALISMAN", "SPEED_RING", "SPEED_ARTIFACT"},
                {"FIRE_TALISMAN", "FIRE_RING", "FIRE_ARTIFACT"},
                {"PIGMAN_TALISMAN", "PIGMAN_RING", "PIGMAN_ARTIFACT"},
                {"ICE_TALISMAN", "ICE_RING", "ICE_ARTIFACT"},
                {"RED_CLAW_TALISMAN", "RED_CLAW_RING", "RED_CLAW_ARTIFACT"},
                {"JERRY_TALISMAN", "JERRY_RING", "JERRY_ARTIFACT"},
                {"CAMPFIRE_TALISMAN", "CAMPFIRE_RING", "CAMPFIRE_ARTIFACT"}
        };

        for (String[] path : paths) {
            for (int i = 0; i < path.length - 1; i++) {
                upgradePaths.put(path[i], path[i + 1]);
            }
        }
    }

    public void scanPlayerAccessories() {
        ownedAccessories.clear();

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        for (int i = 0; i < client.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = client.player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            String internalName = resolveInternalName(stack);
            if (internalName != null && allAccessories.contains(internalName)) {
                ownedAccessories.add(internalName);
            }
        }

        for (net.minecraft.world.entity.EquipmentSlot slot : net.minecraft.world.entity.EquipmentSlot.values()) {
            ItemStack stack = client.player.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            String internalName = resolveInternalName(stack);
            if (internalName != null && allAccessories.contains(internalName)) {
                ownedAccessories.add(internalName);
            }
        }
    }

    private String resolveInternalName(ItemStack stack) {
        var customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (customData != null) {
            var tag = customData.copyTag();
            String id = tag.getStringOr("id", "");
            if (!id.isEmpty()) {
                return id;
            }
        }

        String displayName = stack.getHoverName().getString();
        displayName = displayName.replaceAll("\u00a7[0-9a-fk-orA-FK-OR]", "");

        ItemRepo repo = ItemRepo.getInstance();
        for (String itemId : allAccessories) {
            String itemDisplayName = repo.getDisplayName(itemId);
            if (itemDisplayName != null) {
                String clean = itemDisplayName.replaceAll("\u00a7[0-9a-fk-orA-FK-OR]", "");
                if (clean.equalsIgnoreCase(displayName)) {
                    return itemId;
                }
            }
        }

        return null;
    }

    public List<String> getMissingAccessories() {
        List<String> missing = new ArrayList<>();
        Set<String> upgradedFrom = new HashSet<>();

        for (String owned : ownedAccessories) {
            String current = owned;
            while (upgradePaths.containsKey(current)) {
                current = upgradePaths.get(current);
                upgradedFrom.add(current);
            }
        }

        for (String accessory : allAccessories) {
            if (!ownedAccessories.contains(accessory) && !upgradedFrom.contains(accessory)) {
                boolean hasBetter = false;
                String current = accessory;
                while (upgradePaths.containsKey(current)) {
                    current = upgradePaths.get(current);
                    if (ownedAccessories.contains(current)) {
                        hasBetter = true;
                        break;
                    }
                }

                if (!hasBetter) {
                    boolean hasUpgrade = false;
                    for (String owned : ownedAccessories) {
                        String check = owned;
                        while (upgradePaths.containsKey(check)) {
                            check = upgradePaths.get(check);
                            if (check.equals(accessory)) {
                                hasUpgrade = true;
                                break;
                            }
                        }
                        if (hasUpgrade) break;
                    }

                    if (!hasUpgrade) {
                        missing.add(accessory);
                    }
                }
            }
        }

        return missing;
    }

    public List<String> getUpgradeableAccessories() {
        List<String> upgradeable = new ArrayList<>();

        for (String owned : ownedAccessories) {
            if (upgradePaths.containsKey(owned)) {
                String upgrade = upgradePaths.get(owned);
                if (!ownedAccessories.contains(upgrade)) {
                    upgradeable.add(owned);
                }
            }
        }

        return upgradeable;
    }

    public void render(GuiGraphicsExtractor context, int screenWidth, int screenHeight) {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config == null || !config.accessories.showBagOverlay) return;

        SBInfo sbInfo = SBInfo.getInstance();
        if (!sbInfo.hasSkyblockScoreboard()) return;

        String openChest = sbInfo.getOpenChestName();
        if (openChest == null || !openChest.contains("Accessory Bag")) return;

        if (!loaded) {
            loadAccessories();
        }

        Minecraft client = Minecraft.getInstance();

        List<String> missing = getMissingAccessories();
        if (missing.isEmpty()) return;

        int x = screenWidth - 160;
        int y = 10;
        int lineH = client.font.lineHeight + 2;

        int panelHeight = Math.min(missing.size(), 10) * lineH + 24;
        context.fill(x - 4, y - 4, x + 156, y + panelHeight, 0x80000000);
        context.outline(x - 4, y - 4, 160, panelHeight, 0xFF555555);

        context.text(client.font, "\u00a7cMissing Accessories:", x, y, 0xFFFF5555, true);
        y += lineH + 4;

        int count = 0;
        for (String itemId : missing) {
            if (count >= 10) {
                context.text(client.font, "\u00a77... and " + (missing.size() - 10) + " more", x, y, 0xFFAAAAAA, false);
                break;
            }

            ItemRepo repo = ItemRepo.getInstance();
            String displayName = repo.getDisplayName(itemId);
            String name = displayName != null ? displayName : itemId;
            name = name.replaceAll("\u00a7[0-9a-fk-orA-FK-OR]", "");

            String upgrade = upgradePaths.get(itemId);
            String text = "\u00a7c- " + name;
            if (upgrade != null && config.accessories.showUpgradePaths) {
                String upgradeName = repo.getDisplayName(upgrade);
                String cleanUpgrade = upgradeName != null ? upgradeName.replaceAll("\u00a7[0-9a-fk-orA-FK-OR]", "") : upgrade;
                text += " \u00a77\u2192 \u00a7a" + cleanUpgrade;
            }

            context.text(client.font, text, x, y, 0xFFAAAAAA, false);
            y += lineH;
            count++;
        }
    }

    public void reset() {
        ownedAccessories.clear();
        loaded = false;
    }

    public Set<String> getOwnedAccessories() {
        return ownedAccessories;
    }

    public Set<String> getAllAccessories() {
        return allAccessories;
    }

    public Map<String, String> getUpgradePaths() {
        return upgradePaths;
    }

    public boolean isLoaded() {
        return loaded;
    }
}
