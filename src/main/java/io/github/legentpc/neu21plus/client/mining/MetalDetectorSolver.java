package io.github.legentpc.neu21plus.client.mining;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.config.NeuConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetalDetectorSolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetalDetectorSolver.class);

    private static final MetalDetectorSolver INSTANCE = new MetalDetectorSolver();

    private static final Pattern TREASURE_FOUND_PATTERN = Pattern.compile(".*You (?:found|discovered) (?:a )?treasure.*");
    private static final Pattern TREASURE_NEARBY_PATTERN = Pattern.compile(".*The treasure is (?:nearby|close|very close).*");
    private static final Pattern METAL_DETECTOR_PATTERN = Pattern.compile(".*Metal Detector.*");
    private static final Pattern CHEST_LOCATION_PATTERN = Pattern.compile(".*(?:chest|treasure) (?:is |at )?(?:at )?(-?\\d+)[, ]+(-?\\d+)[, ]+(-?\\d+).*");

    private static final double PROXIMITY_THRESHOLD = 2.0;
    private static final double NEARBY_THRESHOLD = 15.0;
    private static final double DETECTION_RANGE = 30.0;

    public static MetalDetectorSolver getInstance() {
        return INSTANCE;
    }

    private boolean active = false;
    private boolean treasureFound = false;
    private double targetX = 0;
    private double targetY = 0;
    private double targetZ = 0;
    private boolean hasTarget = false;
    private int tickCount = 0;
    private long lastProximityAlert = -1;
    private long lastDirectionUpdate = -1;

    private double lastPlayerX = 0;
    private double lastPlayerZ = 0;
    private String lastDirection = "";
    private double lastDistance = 0;
    private ProximityLevel proximityLevel = ProximityLevel.NONE;

    private List<ChestLocation> nearbyChests = new ArrayList<>();

    private enum ProximityLevel {
        NONE(0xFFAAAAAA),
        FAR(0xFFFFFF55),
        NEAR(0xFF55FFFF),
        CLOSE(0xFF55FF55),
        DIG_HERE(0xFF55FF55);

        private final int color;

        ProximityLevel(int color) {
            this.color = color;
        }

        public int getColor() {
            return color;
        }
    }

    public record ChestLocation(double x, double y, double z, long foundTime) {
    }

    private MetalDetectorSolver() {
    }

    public void tick() {
        tickCount++;

        if (!active) return;

        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;

        updatePlayerPosition(client);
        updateProximity(client);
        scanNearbyChests(client);

        if (tickCount % 5 == 0 && !hasTarget) {
            attemptAutoDetectTarget(client);
        }
    }

    private void updatePlayerPosition(Minecraft client) {
        lastPlayerX = client.player.getX();
        lastPlayerZ = client.player.getZ();
    }

    private void updateProximity(Minecraft client) {
        if (!hasTarget) {
            proximityLevel = ProximityLevel.NONE;
            return;
        }

        double dx = targetX - client.player.getX();
        double dy = targetY - client.player.getY();
        double dz = targetZ - client.player.getZ();
        lastDistance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (lastDistance <= PROXIMITY_THRESHOLD) {
            proximityLevel = ProximityLevel.DIG_HERE;
            lastProximityAlert = System.currentTimeMillis();
        } else if (lastDistance <= NEARBY_THRESHOLD) {
            proximityLevel = ProximityLevel.CLOSE;
        } else if (lastDistance <= DETECTION_RANGE) {
            proximityLevel = ProximityLevel.NEAR;
        } else {
            proximityLevel = ProximityLevel.FAR;
        }

        lastDirection = calculateDirection(client);
        lastDirectionUpdate = System.currentTimeMillis();
    }

    private void scanNearbyChests(Minecraft client) {
        if (tickCount % 40 != 0) return;

        nearbyChests.removeIf(chest -> System.currentTimeMillis() - chest.foundTime > 30000);

        AABB scanBox = new AABB(
                client.player.getX() - DETECTION_RANGE,
                client.player.getY() - DETECTION_RANGE,
                client.player.getZ() - DETECTION_RANGE,
                client.player.getX() + DETECTION_RANGE,
                client.player.getY() + DETECTION_RANGE,
                client.player.getZ() + DETECTION_RANGE
        );

        for (Entity entity : client.level.getEntities(client.player, scanBox)) {
            String name = entity.getName().getString().toLowerCase();
            if (name.contains("chest") || name.contains("treasure") || name.contains("loot")) {
                boolean alreadyKnown = false;
                for (ChestLocation chest : nearbyChests) {
                    if (Math.abs(chest.x - entity.getX()) < 2 && Math.abs(chest.z - entity.getZ()) < 2) {
                        alreadyKnown = true;
                        break;
                    }
                }
                if (!alreadyKnown) {
                    nearbyChests.add(new ChestLocation(
                            entity.getX(), entity.getY(), entity.getZ(), System.currentTimeMillis()
                    ));
                }
            }
        }
    }

    private void attemptAutoDetectTarget(Minecraft client) {
        if (!nearbyChests.isEmpty()) {
            ChestLocation nearest = null;
            double nearestDist = Double.MAX_VALUE;
            for (ChestLocation chest : nearbyChests) {
                double dx = chest.x - client.player.getX();
                double dz = chest.z - client.player.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearest = chest;
                }
            }
            if (nearest != null) {
                targetX = nearest.x;
                targetY = nearest.y;
                targetZ = nearest.z;
                hasTarget = true;
            }
        }
    }

    private String calculateDirection(Minecraft client) {
        if (!hasTarget) return "?";

        double dx = targetX - client.player.getX();
        double dz = targetZ - client.player.getZ();

        float yaw = client.player.getYRot();
        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));

        float relYaw = targetYaw - yaw;
        while (relYaw > 180) relYaw -= 360;
        while (relYaw < -180) relYaw += 360;

        if (relYaw > -22.5 && relYaw <= 22.5) return "S";
        if (relYaw > 22.5 && relYaw <= 67.5) return "SW";
        if (relYaw > 67.5 && relYaw <= 112.5) return "W";
        if (relYaw > 112.5 && relYaw <= 157.5) return "NW";
        if (relYaw > 157.5 || relYaw <= -157.5) return "N";
        if (relYaw > -157.5 && relYaw <= -112.5) return "NE";
        if (relYaw > -112.5 && relYaw <= -67.5) return "E";
        if (relYaw > -67.5 && relYaw <= -22.5) return "SE";

        return "?";
    }

    public void onChatMessage(Component message) {
        String text = message.getString();
        String cleaned = stripColorCodes(text).trim();

        Matcher detectorMatcher = METAL_DETECTOR_PATTERN.matcher(cleaned);
        if (detectorMatcher.find()) {
            active = true;
            LOGGER.debug("Metal detector puzzle detected");
        }

        Matcher nearbyMatcher = TREASURE_NEARBY_PATTERN.matcher(cleaned);
        if (nearbyMatcher.find() && active) {
            updateProximityFromChat(cleaned);
        }

        Matcher chestLocMatcher = CHEST_LOCATION_PATTERN.matcher(cleaned);
        if (chestLocMatcher.find() && active) {
            try {
                targetX = Double.parseDouble(chestLocMatcher.group(1));
                targetY = Double.parseDouble(chestLocMatcher.group(2));
                targetZ = Double.parseDouble(chestLocMatcher.group(3));
                hasTarget = true;
                LOGGER.info("Metal detector target set from chat: {}, {}, {}", targetX, targetY, targetZ);
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private void updateProximityFromChat(String text) {
        String lower = text.toLowerCase();
        if (lower.contains("very close")) {
            proximityLevel = ProximityLevel.CLOSE;
        } else if (lower.contains("close") || lower.contains("nearby")) {
            proximityLevel = ProximityLevel.NEAR;
        }
    }

    public void onTreasureFound() {
        treasureFound = true;
        active = false;
        hasTarget = false;
        proximityLevel = ProximityLevel.NONE;
        LOGGER.info("Treasure found! Metal detector solver deactivated");
    }

    public void render(GuiGraphicsExtractor context, int screenWidth, int screenHeight) {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config == null || !config.mining.metalDetectorSolver) return;
        if (!active) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        int panelWidth = 160;
        int panelHeight = 48;
        int panelX = (screenWidth - panelWidth) / 2;
        int panelY = screenHeight / 2 - 80;

        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0x90000000);

        int borderColor = proximityLevel.getColor();
        context.fill(panelX, panelY, panelX + panelWidth, panelY + 1, borderColor);
        context.fill(panelX, panelY + panelHeight - 1, panelX + panelWidth, panelY + panelHeight, borderColor);
        context.fill(panelX, panelY, panelX + 1, panelY + panelHeight, borderColor);
        context.fill(panelX + panelWidth - 1, panelY, panelX + panelWidth, panelY + panelHeight, borderColor);

        String titleText = "Metal Detector";
        int titleWidth = client.font.width(titleText);
        context.text(client.font, titleText, panelX + (panelWidth - titleWidth) / 2, panelY + 4, 0xFF55FFFF, true);

        if (proximityLevel == ProximityLevel.DIG_HERE) {
            String digText = "DIG HERE!";
            int digWidth = client.font.width(digText);
            boolean flash = tickCount % 20 < 10;
            int digColor = flash ? 0xFF55FF55 : 0xFF33AA33;
            context.text(client.font, digText, panelX + (panelWidth - digWidth) / 2, panelY + 18, digColor, true);

            String distText = String.format("%.1f blocks away", lastDistance);
            int distWidth = client.font.width(distText);
            context.text(client.font, distText, panelX + (panelWidth - distWidth) / 2, panelY + 32, 0xFFAAAAAA, false);
        } else if (hasTarget) {
            String dirText = "Direction: " + lastDirection;
            context.text(client.font, dirText, panelX + 8, panelY + 18, 0xFFFFFFFF, true);

            String distText = String.format("Distance: %.1f", lastDistance);
            context.text(client.font, distText, panelX + 8, panelY + 32, proximityLevel.getColor(), false);
        } else {
            String searchText = "Searching...";
            int searchWidth = client.font.width(searchText);
            context.text(client.font, searchText, panelX + (panelWidth - searchWidth) / 2, panelY + 24, 0xFFAAAAAA, false);
        }
    }

    public void reset() {
        active = false;
        treasureFound = false;
        targetX = 0;
        targetY = 0;
        targetZ = 0;
        hasTarget = false;
        tickCount = 0;
        lastProximityAlert = -1;
        lastDirectionUpdate = -1;
        lastPlayerX = 0;
        lastPlayerZ = 0;
        lastDirection = "";
        lastDistance = 0;
        proximityLevel = ProximityLevel.NONE;
        nearbyChests.clear();
    }

    public boolean isActive() {
        return active;
    }

    public boolean hasTarget() {
        return hasTarget;
    }

    public void setTarget(double x, double y, double z) {
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
        this.hasTarget = true;
    }

    public double getTargetX() {
        return targetX;
    }

    public double getTargetY() {
        return targetY;
    }

    public double getTargetZ() {
        return targetZ;
    }

    public double getLastDistance() {
        return lastDistance;
    }

    public String getLastDirection() {
        return lastDirection;
    }

    private String stripColorCodes(String text) {
        return text.replaceAll("\u00a7[0-9a-fk-orA-FK-OR]", "");
    }
}
