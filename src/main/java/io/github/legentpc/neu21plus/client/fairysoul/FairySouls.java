package io.github.legentpc.neu21plus.client.fairysoul;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.itemrepo.Constants;
import io.github.legentpc.neu21plus.skyblock.SBInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FairySouls {

    private static final Logger LOGGER = LoggerFactory.getLogger(FairySouls.class);

    private static final FairySouls INSTANCE = new FairySouls();

    public static FairySouls getInstance() {
        return INSTANCE;
    }

    private final List<FairySoulLocation> allSoulLocations = new ArrayList<>();
    private final Set<String> collectedSouls = new HashSet<>();
    private final Map<String, Integer> soulsPerIsland = new HashMap<>();
    private final Map<String, Integer> collectedPerIsland = new HashMap<>();
    private boolean locationsLoaded = false;

    private FairySouls() {
    }

    public void loadSoulLocations() {
        allSoulLocations.clear();
        soulsPerIsland.clear();

        Constants constants = Constants.getInstance();
        JsonObject fairyData = constants.fairysouls;
        if (fairyData == null) {
            LOGGER.warn("Fairy soul data not available from constants");
            return;
        }

        try {
            if (fairyData.has("soul_locations")) {
                JsonArray locations = fairyData.getAsJsonArray("soul_locations");
                for (JsonElement elem : locations) {
                    JsonObject loc = elem.getAsJsonObject();
                    FairySoulLocation soul = new FairySoulLocation();
                    soul.island = loc.has("island") ? loc.get("island").getAsString() : "Unknown";
                    soul.x = loc.has("x") ? loc.get("x").getAsDouble() : 0;
                    soul.y = loc.has("y") ? loc.get("y").getAsDouble() : 0;
                    soul.z = loc.has("z") ? loc.get("z").getAsDouble() : 0;
                    soul.id = loc.has("id") ? loc.get("id").getAsString() : soul.island + "_" + allSoulLocations.size();
                    allSoulLocations.add(soul);

                    soulsPerIsland.merge(soul.island, 1, Integer::sum);
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Failed to parse fairy soul locations", e);
        }

        if (allSoulLocations.isEmpty()) {
            loadDefaultLocations();
        }

        locationsLoaded = true;
        LOGGER.info("Loaded {} fairy soul locations across {} islands", allSoulLocations.size(), soulsPerIsland.size());
    }

    private void loadDefaultLocations() {
        addSoulLocation("Hub", -2, 72, -70);
        addSoulLocation("Hub", 16, 69, -53);
        addSoulLocation("Hub", -30, 71, -45);
        addSoulLocation("Hub", 25, 74, -30);
        addSoulLocation("Hub", -15, 68, -20);
        addSoulLocation("Hub", 10, 72, -90);
        addSoulLocation("Hub", -40, 80, -60);
        addSoulLocation("Hub", 35, 69, -75);

        addSoulLocation("Spider's Den", -95, 82, -165);
        addSoulLocation("Spider's Den", -80, 76, -140);
        addSoulLocation("Spider's Den", -105, 90, -150);
        addSoulLocation("Spider's Den", -70, 85, -180);

        addSoulLocation("Blazing Fortress", -45, 72, -55);
        addSoulLocation("Blazing Fortress", -60, 80, -40);
        addSoulLocation("Blazing Fortress", -35, 68, -70);

        addSoulLocation("The End", -45, 55, -85);
        addSoulLocation("The End", -60, 62, -70);
        addSoulLocation("The End", -35, 50, -95);

        addSoulLocation("Crystal Hollows", 200, 100, 200);
        addSoulLocation("Crystal Hollows", 220, 95, 180);
        addSoulLocation("Crystal Hollows", 180, 105, 220);
        addSoulLocation("Crystal Hollows", 240, 90, 190);

        addSoulLocation("Dwarven Mines", -50, 200, -50);
        addSoulLocation("Dwarven Mines", -30, 195, -70);
        addSoulLocation("Dwarven Mines", -70, 205, -30);
    }

    private void addSoulLocation(String island, double x, double y, double z) {
        FairySoulLocation soul = new FairySoulLocation();
        soul.island = island;
        soul.x = x;
        soul.y = y;
        soul.z = z;
        soul.id = island + "_" + allSoulLocations.size();
        allSoulLocations.add(soul);
        soulsPerIsland.merge(island, 1, Integer::sum);
    }

    public void tick() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        if (!locationsLoaded) {
            loadSoulLocations();
        }

        Vec3 playerPos = client.player.position();
        for (FairySoulLocation soul : allSoulLocations) {
            if (collectedSouls.contains(soul.id)) continue;

            double dist = playerPos.distanceTo(new Vec3(soul.x, soul.y, soul.z));
            if (dist < 2.0) {
                collectedSouls.add(soul.id);
                collectedPerIsland.merge(soul.island, 1, Integer::sum);
            }
        }
    }

    public void onChatMessage(net.minecraft.network.chat.Component message) {
        String text = message.getString();
        String cleaned = text.replaceAll("\u00a7[0-9a-fk-orA-FK-OR]", "").trim();

        if (cleaned.contains("FAIRY SOUL!") || cleaned.contains("You found a Fairy Soul!")) {
            SBInfo sbInfo = SBInfo.getInstance();
            String location = sbInfo.getLocation();
            if (location != null && !location.isEmpty()) {
                collectedPerIsland.merge(location, 1, Integer::sum);
            }
        }
    }

    public List<FairySoulLocation> getNearbySouls(Vec3 playerPos, double maxDistance) {
        List<FairySoulLocation> nearby = new ArrayList<>();
        SBInfo sbInfo = SBInfo.getInstance();
        String currentIsland = sbInfo.getLocation();

        for (FairySoulLocation soul : allSoulLocations) {
            if (collectedSouls.contains(soul.id)) continue;

            if (currentIsland != null && !currentIsland.equalsIgnoreCase(soul.island)) continue;

            double dist = playerPos.distanceTo(new Vec3(soul.x, soul.y, soul.z));
            if (dist <= maxDistance) {
                nearby.add(soul);
            }
        }

        nearby.sort((a, b) -> {
            double distA = playerPos.distanceTo(new Vec3(a.x, a.y, a.z));
            double distB = playerPos.distanceTo(new Vec3(b.x, b.y, b.z));
            return Double.compare(distA, distB);
        });

        return nearby;
    }

    public void renderWaypoints(GuiGraphicsExtractor context, float partialTicks) {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config == null || !config.fairySouls.enabled) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;

        SBInfo sbInfo = SBInfo.getInstance();
        if (!sbInfo.hasSkyblockScoreboard()) return;

        Vec3 playerPos = client.player.position();
        float maxDist = config.fairySouls.waypointDistance;

        List<FairySoulLocation> nearby = getNearbySouls(playerPos, maxDist);
        for (FairySoulLocation soul : nearby) {
            renderWaypoint(context, client, soul, playerPos, partialTicks);
        }
    }

    private void renderWaypoint(GuiGraphicsExtractor context, Minecraft client,
                                FairySoulLocation soul, Vec3 playerPos, float partialTicks) {
        Vec3 soulPos = new Vec3(soul.x, soul.y + 1.5, soul.z);
        Vec3 viewVec = client.player.getViewVector(partialTicks);
        Vec3 toSoul = soulPos.subtract(playerPos);

        double dist = playerPos.distanceTo(soulPos);
        if (dist < 1) return;

        toSoul = toSoul.normalize();
        double dot = viewVec.dot(toSoul);
        if (dot < 0) return;

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        net.minecraft.world.phys.Vec3 eyePos = client.player.getEyePosition(partialTicks);
        net.minecraft.world.phys.Vec3 lookVec = client.player.getLookAngle();
        net.minecraft.world.phys.Vec3 targetVec = new net.minecraft.world.phys.Vec3(
                soul.x - eyePos.x, soul.y + 1.5 - eyePos.y, soul.z - eyePos.z);

        double targetDist = targetVec.length();
        targetVec = targetVec.normalize();

        double angle = Math.acos(lookVec.dot(targetVec));
        if (angle > Math.PI / 3) return;

        int screenX = screenWidth / 2 + (int) (Math.atan2(
                targetVec.x * lookVec.z - targetVec.z * lookVec.x,
                targetVec.x * lookVec.x + targetVec.z * lookVec.z
        ) * screenWidth / 3);

        int screenY = screenHeight / 2 - (int) (targetVec.y * screenHeight / 3);

        String text = "\u00a7d\u2726 " + (int) dist + "m";
        context.text(client.font, text, screenX - client.font.width(text) / 2, screenY, 0xFFFF55FF, true);
    }

    public void renderTracker(GuiGraphicsExtractor context, int screenWidth, int screenHeight) {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config == null || !config.fairySouls.showTracker) return;

        SBInfo sbInfo = SBInfo.getInstance();
        if (!sbInfo.hasSkyblockScoreboard()) return;

        Minecraft client = Minecraft.getInstance();

        int x = screenWidth - 140;
        int y = screenHeight - 80;

        int totalFound = collectedSouls.size();
        int totalAll = allSoulLocations.size();

        context.fill(x - 2, y - 2, x + 136, y + 20, 0x80000000);
        context.outline(x - 2, y - 2, 138, 22, 0xFF555555);

        String trackerText = "\u00a7dFairy Souls: \u00a7f" + totalFound + "/" + totalAll;
        context.text(client.font, trackerText, x + 2, y + 2, 0xFFFF55FF, true);

        String currentIsland = sbInfo.getLocation();
        if (currentIsland != null && soulsPerIsland.containsKey(currentIsland)) {
            int islandTotal = soulsPerIsland.get(currentIsland);
            int islandFound = collectedPerIsland.getOrDefault(currentIsland, 0);
            String islandText = "\u00a77" + currentIsland + ": \u00a7f" + islandFound + "/" + islandTotal;
            context.text(client.font, islandText, x + 2, y + 12, 0xFFAAAAAA, false);
        }
    }

    public void markCollected(String soulId) {
        collectedSouls.add(soulId);
        for (FairySoulLocation soul : allSoulLocations) {
            if (soul.id.equals(soulId)) {
                collectedPerIsland.merge(soul.island, 1, Integer::sum);
                break;
            }
        }
    }

    public void reset() {
        collectedSouls.clear();
        collectedPerIsland.clear();
    }

    public int getTotalCollected() {
        return collectedSouls.size();
    }

    public int getTotalSouls() {
        return allSoulLocations.size();
    }

    public static class FairySoulLocation {
        public String island;
        public double x;
        public double y;
        public double z;
        public String id;
    }
}
