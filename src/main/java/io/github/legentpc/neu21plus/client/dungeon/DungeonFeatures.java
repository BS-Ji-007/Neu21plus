package io.github.legentpc.neu21plus.client.dungeon;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.skyblock.SBInfo;
import io.github.legentpc.neu21plus.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DungeonFeatures {

    private static final Logger LOGGER = LoggerFactory.getLogger(DungeonFeatures.class);

    private static final DungeonFeatures INSTANCE = new DungeonFeatures();

    private static final Pattern DUNGEON_START_PATTERN = Pattern.compile(".+ (?:entered|started) .*(?:Catacombs|Dungeon).*(?:Floor|floor) ([A-Z0-9]+)");
    private static final Pattern DUNGEON_END_PATTERN = Pattern.compile("(?:Dungeon Cleared|EXTRA STATS)|(?:(?:Master )?Catacombs .+ Floor .+ Cleared)");
    private static final Pattern DEATH_PATTERN = Pattern.compile(".+ was (?:killed|slain|died) by .+");
    private static final Pattern SECRET_PATTERN = Pattern.compile(".+found a Secret!");
    private static final Pattern PUZZLE_FAIL_PATTERN = Pattern.compile(".+failed the puzzle!");
    private static final Pattern PUZZLE_SOLVED_PATTERN = Pattern.compile(".+(?:solved|completed) the puzzle!");
    private static final Pattern TRAP_PATTERN = Pattern.compile(".+activated a trap!");
    private static final Pattern WITHER_PATTERN = Pattern.compile(".+Wither (?:has been defeated|door is now open|boss has been slain)");
    private static final Pattern BLOOD_PATTERN = Pattern.compile(".+Blood (?:Door|door) (?:opened|is now open)");
    private static final Pattern FLOOR_PATTERN = Pattern.compile("(?:(?:Master )?Catacombs )?(?:Floor |F)([A-Z0-9]+)");

    public static DungeonFeatures getInstance() {
        return INSTANCE;
    }

    private final DungeonMap dungeonMap;
    private final DungeonScore dungeonScore;
    private final PuzzleSolver puzzleSolver;
    private final DungeonWinMessage winMessage;

    private boolean inDungeon = false;
    private boolean dungeonStarted = false;
    private int tickCount = 0;

    private DungeonFeatures() {
        dungeonMap = DungeonMap.getInstance();
        dungeonScore = new DungeonScore();
        puzzleSolver = new PuzzleSolver();
        winMessage = DungeonWinMessage.getInstance();
    }

    public void tick() {
        if (!isInDungeon()) return;

        tickCount++;
        dungeonMap.tick();

        if (tickCount % 40 == 0 && puzzleSolver.getActivePuzzle() == PuzzleSolver.PuzzleType.BLAZE) {
            puzzleSolver.scanBlazeEntities();
        }

        updateDungeonDetection();
    }

    private void updateDungeonDetection() {
        SBInfo sbInfo = SBInfo.getInstance();
        String location = sbInfo.getLocation();

        boolean wasInDungeon = inDungeon;
        inDungeon = isDungeonLocation(location);

        if (inDungeon && !wasInDungeon) {
            onDungeonEnter();
        } else if (!inDungeon && wasInDungeon) {
            onDungeonExit();
        }

        sbInfo.isInDungeon = inDungeon;
    }

    private boolean isDungeonLocation(String location) {
        if (location == null) return false;
        return location.contains("dungeon") || location.contains("catacombs")
                || location.equals("master_mode") || location.startsWith("floor_");
    }

    private void onDungeonEnter() {
        LOGGER.info("Entered dungeon area");
    }

    private void onDungeonExit() {
        LOGGER.info("Exited dungeon area");
        reset();
    }

    public void onChatMessage(Component message) {
        if (!isInDungeon()) return;

        String text = message.getString();
        String cleaned = TextUtils.stripColorCodes(text).trim();

        Matcher startMatcher = DUNGEON_START_PATTERN.matcher(cleaned);
        if (startMatcher.find()) {
            String floor = startMatcher.group(1);
            onDungeonStart(floor);
            return;
        }

        Matcher endMatcher = DUNGEON_END_PATTERN.matcher(cleaned);
        if (endMatcher.find()) {
            onDungeonEnd();
        }

        Matcher deathMatcher = DEATH_PATTERN.matcher(cleaned);
        if (deathMatcher.find()) {
            dungeonScore.onDeath();
        }

        Matcher secretMatcher = SECRET_PATTERN.matcher(cleaned);
        if (secretMatcher.find()) {
            dungeonScore.onSecretFound();
        }

        Matcher puzzleFailMatcher = PUZZLE_FAIL_PATTERN.matcher(cleaned);
        if (puzzleFailMatcher.find()) {
            LOGGER.debug("Puzzle failed detected");
        }

        Matcher puzzleSolvedMatcher = PUZZLE_SOLVED_PATTERN.matcher(cleaned);
        if (puzzleSolvedMatcher.find()) {
            dungeonScore.onPuzzleSolved();
        }

        Matcher trapMatcher = TRAP_PATTERN.matcher(cleaned);
        if (trapMatcher.find()) {
            dungeonScore.onTrapCleared();
        }

        Matcher witherMatcher = WITHER_PATTERN.matcher(cleaned);
        if (witherMatcher.find()) {
            dungeonScore.onWitherKillsComplete();
        }

        Matcher bloodMatcher = BLOOD_PATTERN.matcher(cleaned);
        if (bloodMatcher.find()) {
            dungeonScore.onBloodCleared();
        }

        puzzleSolver.parseChatMessage(message);
        winMessage.parseChatMessage(message);
    }

    public void onChestTitleChanged(String title) {
        if (!isInDungeon()) return;
        puzzleSolver.parseChestTitle(title);
    }

    private void onDungeonStart(String floor) {
        dungeonStarted = true;
        dungeonMap.onDungeonStart();
        dungeonMap.setCurrentFloor(floor);
        dungeonScore.reset();
        puzzleSolver.reset();
        winMessage.reset();
        LOGGER.info("Dungeon run started on floor {}", floor);
    }

    private void onDungeonEnd() {
        dungeonMap.onDungeonEnd();
        dungeonStarted = false;
        LOGGER.info("Dungeon run ended. Score: {}, Grade: {}",
                dungeonScore.getTotalScore(), dungeonScore.getGrade().getDisplay());
    }

    public void reset() {
        inDungeon = false;
        dungeonStarted = false;
        dungeonMap.resetMap();
        dungeonScore.reset();
        puzzleSolver.reset();
        winMessage.reset();
        tickCount = 0;
    }

    public boolean isInDungeon() {
        return inDungeon;
    }

    public boolean isDungeonStarted() {
        return dungeonStarted;
    }

    public DungeonMap getDungeonMap() {
        return dungeonMap;
    }

    public DungeonScore getScore() {
        return dungeonScore;
    }

    public PuzzleSolver getPuzzleSolver() {
        return puzzleSolver;
    }

    public DungeonWinMessage getWinMessage() {
        return winMessage;
    }

}
