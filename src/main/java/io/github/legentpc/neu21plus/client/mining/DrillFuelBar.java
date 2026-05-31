package io.github.legentpc.neu21plus.client.mining;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.skyblock.SBInfo;
import io.github.legentpc.neu21plus.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DrillFuelBar {

    private static final Logger LOGGER = LoggerFactory.getLogger(DrillFuelBar.class);

    private static final DrillFuelBar INSTANCE = new DrillFuelBar();

    private static final Pattern FUEL_PATTERN = Pattern.compile("Fuel: (\\d+)/(\\d+)");
    private static final Pattern FUEL_PERCENT_PATTERN = Pattern.compile("Fuel: (\\d+)%");

    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;
    private static final int BAR_Y_OFFSET = 32;

    public static DrillFuelBar getInstance() {
        return INSTANCE;
    }

    private int fuelCurrent = 0;
    private int fuelMax = 3000;
    private int fuelPercent = 100;
    private boolean holdingDrill = false;
    private boolean inDrillArea = false;
    private int tickCount = 0;
    private long lastFuelUpdate = -1;

    private DrillFuelBar() {
    }

    public void tick() {
        tickCount++;

        if (tickCount % 10 != 0) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        updateDrillDetection(client);
        updateAreaDetection();

        if (holdingDrill && inDrillArea) {
            scanHeldItemFuel(client);
        }
    }

    private void updateDrillDetection(Minecraft client) {
        ItemStack mainHand = client.player.getMainHandItem();
        ItemStack offHand = client.player.getOffhandItem();

        boolean wasHoldingDrill = holdingDrill;
        holdingDrill = isDrillItem(mainHand) || isDrillItem(offHand);

        if (holdingDrill && !wasHoldingDrill) {
            LOGGER.debug("Started holding a drill item");
        } else if (!holdingDrill && wasHoldingDrill) {
            fuelCurrent = 0;
            fuelMax = 3000;
            fuelPercent = 100;
        }
    }

    private boolean isDrillItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        String itemName = TextUtils.stripColorCodes(stack.getHoverName().getString()).toLowerCase();
        return itemName.contains("drill")
                || itemName.contains("pickonimbus")
                || itemName.contains("gemstone gauntlet")
                || itemName.contains("mithril drill")
                || itemName.contains("gemstone drill")
                || itemName.contains("titanium drill")
                || itemName.contains("divan's drill");
    }

    private void updateAreaDetection() {
        SBInfo sbInfo = SBInfo.getInstance();
        String location = sbInfo.getLocation();
        if (location == null) {
            inDrillArea = false;
            return;
        }

        String lower = location.toLowerCase();
        inDrillArea = lower.contains("crystal_hollows")
                || lower.contains("dwarven")
                || lower.contains("mines")
                || lower.contains("forge")
                || lower.contains("campfire");
    }

    private void scanHeldItemFuel(Minecraft client) {
        ItemStack mainHand = client.player.getMainHandItem();
        ItemStack drillStack = isDrillItem(mainHand) ? mainHand : client.player.getOffhandItem();

        if (drillStack.isEmpty()) return;

        net.minecraft.world.item.Item.TooltipContext tooltipContext = net.minecraft.world.item.Item.TooltipContext.EMPTY;
        net.minecraft.world.item.TooltipFlag tooltipFlag = client.options.advancedItemTooltips
                ? net.minecraft.world.item.TooltipFlag.ADVANCED
                : net.minecraft.world.item.TooltipFlag.NORMAL;

        List<net.minecraft.network.chat.Component> lore = drillStack.getTooltipLines(
                tooltipContext,
                client.player,
                tooltipFlag
        );

        boolean foundFuel = false;

        for (net.minecraft.network.chat.Component line : lore) {
            String lineText = TextUtils.stripColorCodes(line.getString()).trim();

            Matcher fuelMatcher = FUEL_PATTERN.matcher(lineText);
            if (fuelMatcher.find()) {
                fuelCurrent = TextUtils.parseIntSafe(fuelMatcher.group(1), fuelCurrent);
                fuelMax = TextUtils.parseIntSafe(fuelMatcher.group(2), fuelMax);
                if (fuelMax > 0) {
                    fuelPercent = (int) ((fuelCurrent * 100L) / fuelMax);
                }
                foundFuel = true;
                lastFuelUpdate = System.currentTimeMillis();
                break;
            }

            Matcher percentMatcher = FUEL_PERCENT_PATTERN.matcher(lineText);
            if (percentMatcher.find()) {
                fuelPercent = TextUtils.parseIntSafe(percentMatcher.group(1), fuelPercent);
                fuelCurrent = (fuelPercent * fuelMax) / 100;
                foundFuel = true;
                lastFuelUpdate = System.currentTimeMillis();
                break;
            }
        }

        if (!foundFuel) {
            tryFuelFromDataComponents(drillStack);
        }
    }

    private void tryFuelFromDataComponents(ItemStack stack) {
        var customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (customData == null) return;

        try {
            var tag = customData.copyTag();
            if (tag.contains("drill_fuel")) {
                fuelCurrent = tag.getIntOr("drill_fuel", 0);
                if (tag.contains("drill_fuel_max")) {
                    fuelMax = tag.getIntOr("drill_fuel_max", 3000);
                }
                if (fuelMax > 0) {
                    fuelPercent = (int) ((fuelCurrent * 100L) / fuelMax);
                }
                lastFuelUpdate = System.currentTimeMillis();
            }
        } catch (Exception e) {
            LOGGER.debug("Failed to read drill fuel from data components", e);
        }
    }

    public void render(GuiGraphicsExtractor context, int screenWidth, int screenHeight) {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config == null || !config.mining.drillFuelBar) return;
        if (!holdingDrill || !inDrillArea) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        int barX = (screenWidth - BAR_WIDTH) / 2;
        int barY = screenHeight - BAR_Y_OFFSET;

        context.fill(barX - 1, barY - 1, barX + BAR_WIDTH + 1, barY + BAR_HEIGHT + 1, 0xFF000000);

        context.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, 0xFF333333);

        int fuelWidth = (int) (BAR_WIDTH * (fuelPercent / 100.0));
        if (fuelWidth > 0) {
            int fuelColor = getFuelColor(fuelPercent);
            context.fill(barX, barY, barX + fuelWidth, barY + BAR_HEIGHT, fuelColor);

            if (fuelWidth > 2) {
                int highlightColor = getFuelHighlightColor(fuelPercent);
                context.fill(barX, barY, barX + fuelWidth, barY + 1, highlightColor);
            }
        }

        String fuelText = "Drill Fuel: " + fuelPercent + "%";
        int textWidth = client.font.width(fuelText);
        int textX = (screenWidth - textWidth) / 2;
        int textY = barY - client.font.lineHeight - 2;

        int textColor = getFuelTextColor(fuelPercent);
        context.text(client.font, fuelText, textX, textY, textColor, true);

        if (fuelMax > 0) {
            String detailText = fuelCurrent + "/" + fuelMax;
            int detailWidth = client.font.width(detailText);
            int detailX = (screenWidth - detailWidth) / 2;
            int detailY = barY + BAR_HEIGHT + 2;
            context.text(client.font, detailText, detailX, detailY, 0xFFAAAAAA, false);
        }
    }

    private int getFuelColor(int percent) {
        if (percent > 60) {
            return interpolateColor(0xFF55FF55, 0xFFFFFF55, (100 - percent) / 40f);
        } else if (percent > 25) {
            return interpolateColor(0xFFFFFF55, 0xFFFF5555, (60 - percent) / 35f);
        } else {
            if (percent <= 10 && tickCount % 20 < 10) {
                return 0xFFFF3333;
            }
            return 0xFFFF5555;
        }
    }

    private int getFuelHighlightColor(int percent) {
        if (percent > 60) {
            return 0xFF88FF88;
        } else if (percent > 25) {
            return 0xFFFFFF88;
        } else {
            return 0xFFFF8888;
        }
    }

    private int getFuelTextColor(int percent) {
        if (percent > 60) {
            return 0xFF55FF55;
        } else if (percent > 25) {
            return 0xFFFFFF55;
        } else {
            return 0xFFFF5555;
        }
    }

    private int interpolateColor(int color1, int color2, float factor) {
        factor = Math.max(0f, Math.min(1f, factor));
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        int r = (int) (r1 + (r2 - r1) * factor);
        int g = (int) (g1 + (g2 - g1) * factor);
        int b = (int) (b1 + (b2 - b1) * factor);
        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }

    public void reset() {
        fuelCurrent = 0;
        fuelMax = 3000;
        fuelPercent = 100;
        holdingDrill = false;
        inDrillArea = false;
        tickCount = 0;
        lastFuelUpdate = -1;
    }

    public boolean isHoldingDrill() {
        return holdingDrill;
    }

    public int getFuelPercent() {
        return fuelPercent;
    }

    public int getFuelCurrent() {
        return fuelCurrent;
    }

    public int getFuelMax() {
        return fuelMax;
    }

}
