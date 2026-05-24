package io.github.legentpc.neu21plus.client.dungeon;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.config.NeuConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DungeonMap {

    private static final Logger LOGGER = LoggerFactory.getLogger(DungeonMap.class);

    private static final DungeonMap INSTANCE = new DungeonMap();

    private static final int MAP_SIZE = 128;
    private static final int ROOM_SIZE = 16;
    private static final int DOOR_SIZE = 4;
    private static final int MAP_GRID_SIZE = 6;

    private static final Pattern DUNGEON_FLOOR_PATTERN = Pattern.compile(".+ ?(?:Master |M ?)Catacombs ?(?:Floor |F ?)([A-Z0-9]+)");
    private static final Pattern SCORE_PATTERN = Pattern.compile("Score: (\\d+)");
    private static final Pattern CLEARED_PATTERN = Pattern.compile("Cleared: (\\d+)%");

    public static DungeonMap getInstance() {
        return INSTANCE;
    }

    private DungeonRoom[][] rooms = new DungeonRoom[MAP_GRID_SIZE][MAP_GRID_SIZE];
    private String currentFloor = "";
    private int currentScore = 0;
    private int clearedPercentage = 0;
    private int totalSecrets = 0;
    private int foundSecrets = 0;
    private int totalKills = 0;
    private int requiredKills = 0;
    private long dungeonStartTime = -1;
    private long dungeonEndTime = -1;
    private boolean dungeonActive = false;
    private boolean mapDataLoaded = false;

    private int playerMapX = MAP_SIZE / 2;
    private int playerMapZ = MAP_SIZE / 2;
    private float playerRotation = 0f;

    private DungeonMap() {
        resetMap();
    }

    public void resetMap() {
        for (int x = 0; x < MAP_GRID_SIZE; x++) {
            for (int y = 0; y < MAP_GRID_SIZE; y++) {
                rooms[x][y] = new DungeonRoom(x, y);
            }
        }
        currentFloor = "";
        currentScore = 0;
        clearedPercentage = 0;
        totalSecrets = 0;
        foundSecrets = 0;
        totalKills = 0;
        requiredKills = 0;
        dungeonStartTime = -1;
        dungeonEndTime = -1;
        dungeonActive = false;
        mapDataLoaded = false;
    }

    public void onDungeonStart() {
        resetMap();
        dungeonStartTime = System.currentTimeMillis();
        dungeonActive = true;
        LOGGER.info("Dungeon started on floor {}", currentFloor);
    }

    public void onDungeonEnd() {
        dungeonEndTime = System.currentTimeMillis();
        dungeonActive = false;
    }

    public void tick() {
        if (!dungeonActive) return;

        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;

        parseScoreboardData(client);
        parseTabListData(client);
        updatePlayerPosition(client);
    }

    private void parseScoreboardData(Minecraft client) {
        try {
            Scoreboard scoreboard = client.level.getScoreboard();
            Objective objective = scoreboard.getDisplayObjective(net.minecraft.world.scores.DisplaySlot.SIDEBAR);
            if (objective == null) return;

            List<PlayerScoreEntry> scores = new ArrayList<>(scoreboard.listPlayerScores(objective));

            for (PlayerScoreEntry score : scores) {
                String line = score.display() != null ? score.display().getString() : score.owner();
                String cleaned = stripColorCodes(line).trim();
                parseScoreboardLine(cleaned);
            }
        } catch (Exception e) {
            LOGGER.debug("Failed to parse dungeon scoreboard", e);
        }
    }

    private void parseScoreboardLine(String line) {
        if (line.isEmpty()) return;

        Matcher floorMatcher = DUNGEON_FLOOR_PATTERN.matcher(line);
        if (floorMatcher.find()) {
            currentFloor = floorMatcher.group(1);
        }

        Matcher scoreMatcher = SCORE_PATTERN.matcher(line);
        if (scoreMatcher.find()) {
            try {
                currentScore = Integer.parseInt(scoreMatcher.group(1));
            } catch (NumberFormatException ignored) {
            }
        }

        Matcher clearedMatcher = CLEARED_PATTERN.matcher(line);
        if (clearedMatcher.find()) {
            try {
                clearedPercentage = Integer.parseInt(clearedMatcher.group(1));
            } catch (NumberFormatException ignored) {
            }
        }

        if (line.contains("Secrets")) {
            String[] parts = line.split("/");
            if (parts.length == 2) {
                try {
                    foundSecrets = Integer.parseInt(parts[0].replaceAll("\\D", ""));
                    totalSecrets = Integer.parseInt(parts[1].replaceAll("\\D", ""));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if (line.contains("Kills")) {
            String[] parts = line.split("/");
            if (parts.length == 2) {
                try {
                    totalKills = Integer.parseInt(parts[0].replaceAll("\\D", ""));
                    requiredKills = Integer.parseInt(parts[1].replaceAll("\\D", ""));
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }

    private void parseTabListData(Minecraft client) {
        try {
            Collection<PlayerInfo> entries = client.player.connection.getOnlinePlayers();
            for (PlayerInfo entry : entries) {
                Component displayName = entry.getTabListDisplayName();
                if (displayName == null) continue;

                String name = displayName.getString();
                String cleaned = stripColorCodes(name).trim();
                parseTabListEntry(cleaned);
            }
        } catch (Exception e) {
            LOGGER.debug("Failed to parse tab list for dungeon map", e);
        }
    }

    private void parseTabListEntry(String entry) {
        if (entry.isEmpty()) return;

        String[] lines = entry.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("Rooms:")) {
                String roomData = line.substring(6).trim();
                parseRoomData(roomData);
            }
        }
    }

    private void parseRoomData(String data) {
        String[] roomEntries = data.split(",");
        for (String roomEntry : roomEntries) {
            String[] parts = roomEntry.trim().split(":");
            if (parts.length >= 2) {
                try {
                    int roomIndex = Integer.parseInt(parts[0].trim());
                    int x = roomIndex % MAP_GRID_SIZE;
                    int y = roomIndex / MAP_GRID_SIZE;
                    if (x < MAP_GRID_SIZE && y < MAP_GRID_SIZE) {
                        char colorChar = parts[1].trim().charAt(0);
                        rooms[x][y].updateFromMapChar(colorChar);
                    }
                } catch (NumberFormatException | IndexOutOfBoundsException ignored) {
                }
            }
        }
    }

    private void updatePlayerPosition(Minecraft client) {
        double playerX = client.player.getX();
        double playerZ = client.player.getZ();
        float yaw = client.player.getYRot();

        playerMapX = (int) ((playerX % MAP_SIZE + MAP_SIZE) % MAP_SIZE);
        playerMapZ = (int) ((playerZ % MAP_SIZE + MAP_SIZE) % MAP_SIZE);
        playerRotation = yaw;
    }

    public void render(GuiGraphicsExtractor context, int x, int y, int size) {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config == null || !config.dungeons.dungeonMap) return;
        if (!dungeonActive) return;

        int roomPixelSize = size / MAP_GRID_SIZE;
        int doorPixelSize = Math.max(2, roomPixelSize / 4);

        context.fill(x - 2, y - 2, x + size + 2, y + size + 2, 0x80000000);

        for (int gx = 0; gx < MAP_GRID_SIZE; gx++) {
            for (int gy = 0; gy < MAP_GRID_SIZE; gy++) {
                DungeonRoom room = rooms[gx][gy];
                int roomX = x + gx * roomPixelSize;
                int roomY = y + gy * roomPixelSize;

                int roomColor = room.isCurrent() ? 0xFF55FF55 : room.getMapColor() | 0xFF000000;
                context.fill(roomX + 1, roomY + 1, roomX + roomPixelSize - 1, roomY + roomPixelSize - 1, roomColor);

                if (gx < MAP_GRID_SIZE - 1 && rooms[gx + 1][gy].getColor() != DungeonRoom.RoomColor.GRAY) {
                    context.fill(roomX + roomPixelSize - doorPixelSize / 2, roomY + roomPixelSize / 2 - doorPixelSize / 2,
                            roomX + roomPixelSize + doorPixelSize / 2, roomY + roomPixelSize / 2 + doorPixelSize / 2, 0xFF404040);
                }
                if (gy < MAP_GRID_SIZE - 1 && rooms[gx][gy + 1].getColor() != DungeonRoom.RoomColor.GRAY) {
                    context.fill(roomX + roomPixelSize / 2 - doorPixelSize / 2, roomY + roomPixelSize - doorPixelSize / 2,
                            roomX + roomPixelSize / 2 + doorPixelSize / 2, roomY + roomPixelSize + doorPixelSize / 2, 0xFF404040);
                }
            }
        }

        renderPlayerIndicator(context, x, y, size, roomPixelSize);

        Minecraft client = Minecraft.getInstance();
        if (currentFloor != null && !currentFloor.isEmpty()) {
            String floorText = "F" + currentFloor;
            context.text(client.font, floorText, x + 2, y + size + 2, 0xFFAAAAAA, true);
        }

        String scoreText = "Score: " + currentScore;
        context.text(client.font, scoreText, x + size - client.font.width(scoreText) - 2, y + size + 2, 0xFFAAAAAA, true);

        if (totalSecrets > 0) {
            String secretText = foundSecrets + "/" + totalSecrets + " S";
            context.text(client.font, secretText, x + 2, y + size + 2 + client.font.lineHeight + 1, 0xFFAAFFAA, true);
        }
    }

    private void renderPlayerIndicator(GuiGraphicsExtractor context, int mapX, int mapY, int mapSize, int roomPixelSize) {
        double relativeX = (double) playerMapX / MAP_SIZE;
        double relativeZ = (double) playerMapZ / MAP_SIZE;

        int indicatorX = mapX + (int) (relativeX * mapSize);
        int indicatorY = mapY + (int) (relativeZ * mapSize);

        float rad = (float) Math.toRadians(playerRotation);
        float arrowSize = Math.max(3, roomPixelSize / 4f);

        float tipX = indicatorX + (float) Math.sin(rad) * arrowSize;
        float tipY = indicatorY - (float) Math.cos(rad) * arrowSize;

        float leftX = indicatorX + (float) Math.sin(rad + 2.4f) * arrowSize * 0.6f;
        float leftY = indicatorY - (float) Math.cos(rad + 2.4f) * arrowSize * 0.6f;

        float rightX = indicatorX + (float) Math.sin(rad - 2.4f) * arrowSize * 0.6f;
        float rightY = indicatorY - (float) Math.cos(rad - 2.4f) * arrowSize * 0.6f;

        context.fill((int) tipX, (int) tipY, (int) tipX + 2, (int) tipY + 2, 0xFFFFFFFF);
        context.fill((int) leftX, (int) leftY, (int) leftX + 1, (int) leftY + 1, 0xFFFFFFFF);
        context.fill((int) rightX, (int) rightY, (int) rightX + 1, (int) rightY + 1, 0xFFFFFFFF);
    }

    public void setRoomFromMapData(int gridX, int gridY, char mapChar) {
        if (gridX >= 0 && gridX < MAP_GRID_SIZE && gridY >= 0 && gridY < MAP_GRID_SIZE) {
            rooms[gridX][gridY].updateFromMapChar(mapChar);
            mapDataLoaded = true;
        }
    }

    public void setCurrentRoom(int gridX, int gridY) {
        for (int x = 0; x < MAP_GRID_SIZE; x++) {
            for (int y = 0; y < MAP_GRID_SIZE; y++) {
                rooms[x][y].setCurrent(x == gridX && y == gridY);
            }
        }
    }

    @Nullable
    public DungeonRoom getRoom(int gridX, int gridY) {
        if (gridX < 0 || gridX >= MAP_GRID_SIZE || gridY < 0 || gridY >= MAP_GRID_SIZE) return null;
        return rooms[gridX][gridY];
    }

    public boolean isDungeonActive() {
        return dungeonActive;
    }

    public void setDungeonActive(boolean active) {
        this.dungeonActive = active;
    }

    public String getCurrentFloor() {
        return currentFloor;
    }

    public void setCurrentFloor(String floor) {
        this.currentFloor = floor;
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public int getClearedPercentage() {
        return clearedPercentage;
    }

    public int getFoundSecrets() {
        return foundSecrets;
    }

    public int getTotalSecrets() {
        return totalSecrets;
    }

    public long getDungeonStartTime() {
        return dungeonStartTime;
    }

    public long getDungeonElapsedTime() {
        if (dungeonStartTime < 0) return 0;
        long end = dungeonEndTime > 0 ? dungeonEndTime : System.currentTimeMillis();
        return end - dungeonStartTime;
    }

    private String stripColorCodes(String text) {
        return text.replaceAll("\u00a7[0-9a-fk-orA-FK-OR]", "");
    }
}
