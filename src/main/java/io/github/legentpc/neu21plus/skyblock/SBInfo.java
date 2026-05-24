package io.github.legentpc.neu21plus.skyblock;

import com.google.gson.JsonObject;
import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.util.NeuManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SBInfo {

    private static final Logger LOGGER = LoggerFactory.getLogger(SBInfo.class);

    private static final SBInfo INSTANCE = new SBInfo();

    public static SBInfo getInstance() {
        return INSTANCE;
    }

    private static final Pattern JSON_BRACKET_PATTERN = Pattern.compile("^\\{.+}");
    private static final Pattern TIME_PATTERN = Pattern.compile(".+(am|pm)");
    private static final Pattern SKILLS_PREFIX = Pattern.compile("\\S+:");

    private @NotNull String scoreboardLocation = "";
    private @NotNull String lastScoreboardLocation = "";

    public String date = "";
    public String time = "";
    public String objective = "";
    public String slayer = "";
    public boolean stranded = false;
    public boolean bingo = false;

    private @Nullable String mode = null;

    private long lastManualLocRaw = -1;
    private long lastLocRaw = -1;
    public long joinedWorld = -1;
    private JsonObject locraw = null;
    public boolean isInDungeon = false;
    private String dungeonFloor = "";

    private static final Pattern DUNGEON_MODE_PATTERN = Pattern.compile("(?:master_mode|dungeon|catacombs)_?(?:floor_)?([A-Z0-9]+)?");

    private Map<String, Gamemode> gamemodes = new HashMap<>();
    private boolean areGamemodesLoaded = false;
    private int tickCount = 0;
    public String currentProfile = null;

    public String currentlyOpenChestName = "";
    public String lastOpenChestName = "";

    @Nullable
    public String getOpenChestName() {
        return currentlyOpenChestName;
    }

    public enum Gamemode {
        NORMAL("", ""),
        IRONMAN("Ironman", "\u26b2"),
        STRANDED("Stranded", "\u2600");

        private final String name;
        private final String emoji;

        Gamemode(String name, String emoji) {
            this.name = name;
            this.emoji = emoji;
        }

        public static Gamemode find(String type) {
            for (Gamemode gamemode : values()) {
                if (type.contains(gamemode.name)) {
                    return gamemode;
                }
            }
            return null;
        }

        public String getName() {
            return name;
        }

        public String getEmoji() {
            return emoji;
        }
    }

    public void onWorldLoad() {
        lastLocRaw = -1;
        locraw = null;
        this.setMode(null);
        joinedWorld = System.currentTimeMillis();
        currentlyOpenChestName = "";
        lastOpenChestName = "";
    }

    public void onSendChatMessage(String msg) {
        if (msg.trim().startsWith("/locraw") || msg.trim().startsWith("/locraw ")) {
            lastManualLocRaw = System.currentTimeMillis();
        }
    }

    public void onChatMessage(Component message) {
        String unformatted = message.getString();
        Matcher matcher = JSON_BRACKET_PATTERN.matcher(unformatted);
        if (matcher.find()) {
            try {
                NeuManager manager = Neu21PlusMod.getInstance().getManager();
                JsonObject obj = manager.getGson().fromJson(matcher.group(), JsonObject.class);
                if (obj != null && obj.has("server")) {
                    if (obj.has("gametype") && obj.has("mode") && obj.has("map")) {
                        locraw = obj;
                        setMode(locraw.get("mode").getAsString());
                    }
                }
            } catch (Exception e) {
                LOGGER.debug("Failed to parse locraw from chat", e);
            }
        }
    }

    public void tick() {
        tickCount++;
        if (tickCount % 10 != 0) return;

        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;

        updateScoreboardInfo(client);
    }

    private void updateScoreboardInfo(Minecraft client) {
        try {
            Scoreboard scoreboard = client.level.getScoreboard();
            Objective objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
            if (objective == null) return;

            List<PlayerScoreEntry> scores = new ArrayList<>(scoreboard.listPlayerScores(objective));
            scores.sort(java.util.Comparator.<PlayerScoreEntry>comparingInt(PlayerScoreEntry::value).reversed());

            for (PlayerScoreEntry entry : scores) {
                String line = entry.display() != null ? entry.display().getString() : entry.owner();
                String cleaned = stripColorCodes(line).trim();
                parseScoreboardLine(cleaned);
            }
        } catch (Exception e) {
            LOGGER.debug("Failed to read scoreboard", e);
        }
    }

    private void parseScoreboardLine(String line) {
        if (line.isEmpty()) return;

        String trimmed = line.trim();

        if (trimmed.contains("am") || trimmed.contains("pm")) {
            Matcher timeMatcher = TIME_PATTERN.matcher(trimmed);
            if (timeMatcher.matches()) {
                time = trimmed;
                return;
            }
        }

        if (trimmed.startsWith("Objective: ")) {
            objective = trimmed.substring(11);
            return;
        }

        if (trimmed.contains("Slayer")) {
            slayer = trimmed;
            return;
        }

        if (!trimmed.isEmpty() && !trimmed.equals(date) && !trimmed.equals(time)
                && !trimmed.equals(objective) && !trimmed.equals(slayer)) {

            if (!scoreboardLocation.equals(trimmed)) {
                lastScoreboardLocation = scoreboardLocation;
                scoreboardLocation = trimmed;
            }
        }
    }

    public boolean hasSkyblockScoreboard() {
        return !scoreboardLocation.isEmpty();
    }

    public boolean checkForSkyblockLocation() {
        if (!hasSkyblockScoreboard() || getLocation() == null) {
            return false;
        }
        return true;
    }

    @Nullable
    public String getLocation() {
        return mode;
    }

    public void setMode(String location) {
        location = location == null ? null : location.intern();
        this.mode = location;
        updateDungeonState();
    }

    private void updateDungeonState() {
        if (mode == null) {
            isInDungeon = false;
            dungeonFloor = "";
            return;
        }

        Matcher matcher = DUNGEON_MODE_PATTERN.matcher(mode);
        if (matcher.find()) {
            isInDungeon = true;
            dungeonFloor = matcher.group(1) != null ? matcher.group(1) : "";
        } else if (mode.contains("dungeon") || mode.contains("catacombs") || mode.equals("master_mode")) {
            isInDungeon = true;
        } else {
            isInDungeon = false;
            dungeonFloor = "";
        }
    }

    public String getDungeonFloor() {
        return dungeonFloor;
    }

    @NotNull
    public String getScoreboardLocation() {
        return scoreboardLocation;
    }

    public void resetScoreboardLocation() {
        this.scoreboardLocation = "";
    }

    @NotNull
    public String getLastScoreboardLocation() {
        return lastScoreboardLocation;
    }

    public Gamemode getCurrentMode() {
        return getGamemodeForProfile(currentProfile);
    }

    public Gamemode getGamemodeForProfile(String profile) {
        if (!areGamemodesLoaded) {
            loadGameModes();
        }
        return gamemodes.get(profile);
    }

    public Map<String, Gamemode> getAllGamemodes() {
        if (!areGamemodesLoaded) {
            loadGameModes();
        }
        return gamemodes;
    }

    public void saveGameModes() {
        try {
            NeuManager manager = Neu21PlusMod.getInstance().getManager();
            java.nio.file.Path profilesFile = manager.getConfigLocation().toPath().resolve("profiles.json");
            Files.write(profilesFile, manager.getGson().toJson(gamemodes).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            LOGGER.error("Failed to save game modes", e);
        }
    }

    public void loadGameModes() {
        try {
            NeuManager manager = Neu21PlusMod.getInstance().getManager();
            java.nio.file.Path profilesFile = manager.getConfigLocation().toPath().resolve("profiles.json");
            if (Files.exists(profilesFile)) {
                String content = new String(Files.readAllBytes(profilesFile), StandardCharsets.UTF_8);
                var type = new com.google.common.reflect.TypeToken<Map<String, Gamemode>>() {}.getType();
                gamemodes = manager.getGson().fromJson(content, type);
                if (gamemodes == null) {
                    gamemodes = new HashMap<>();
                }
            }
            areGamemodesLoaded = true;
        } catch (Exception e) {
            LOGGER.error("Failed to load game modes", e);
            gamemodes = new HashMap<>();
            areGamemodesLoaded = true;
        }
    }

    public JsonObject getLocraw() {
        return locraw;
    }

    private String stripColorCodes(String text) {
        return text.replaceAll("\u00a7[0-9a-fk-or]", "").replaceAll("\u00a7[0-9a-fk-orA-FK-OR]", "");
    }
}
