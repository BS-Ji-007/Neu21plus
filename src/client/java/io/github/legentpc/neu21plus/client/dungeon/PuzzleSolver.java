package io.github.legentpc.neu21plus.client.dungeon;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.config.NeuConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PuzzleSolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(PuzzleSolver.class);

    private static final Pattern TRIVIA_QUESTION_PATTERN = Pattern.compile("(.+\\?)");
    private static final Pattern TRIVIA_ANSWER_PATTERN = Pattern.compile("([A-D])\\. (.+)");

    private static final Map<String, String[]> TRIVIA_SOLUTIONS = new HashMap<>();
    private static final Set<String> BLAZE_CORRECT_ORDER = new LinkedHashSet<>();

    private PuzzleType activePuzzle = PuzzleType.NONE;
    private String currentQuestion = "";
    private List<String> currentAnswers = new ArrayList<>();
    private String correctAnswer = "";
    private boolean puzzleSolved = false;
    private List<BlazeEntry> blazeEntries = new ArrayList<>();
    private int blazeTargetIndex = -1;
    private boolean blazeInitialized = false;

    static {
        TRIVIA_SOLUTIONS.put("What is the status of The Watcher?", new String[]{"Stalker"});
        TRIVIA_SOLUTIONS.put("What is the status of Bonzo?", new String[]{"New Necromancer"});
        TRIVIA_SOLUTIONS.put("What is the status of Scarf?", new String[]{"Apprentice Necromancer"});
        TRIVIA_SOLUTIONS.put("What is the status of The Professor?", new String[]{"Professor"});
        TRIVIA_SOLUTIONS.put("What is the status of Thorn?", new String[]{"Shaman Necromancer"});
        TRIVIA_SOLUTIONS.put("What is the status of Livid?", new String[]{"Master Necromancer"});
        TRIVIA_SOLUTIONS.put("What is the status of Sadan?", new String[]{"Master Necromancer"});
        TRIVIA_SOLUTIONS.put("What is the status of Maxor?", new String[]{"Young Wither"});
        TRIVIA_SOLUTIONS.put("What is the status of Goldor?", new String[]{"Wither Soldier"});
        TRIVIA_SOLUTIONS.put("What is the status of Storm?", new String[]{"Elementalist"});
        TRIVIA_SOLUTIONS.put("What is the status of Necron?", new String[]{"Wither Lord"});
        TRIVIA_SOLUTIONS.put("Which brother is on the Wither throne?", new String[]{"Necron"});
        TRIVIA_SOLUTIONS.put("How many total unique minions are there?", new String[]{"53"});
        TRIVIA_SOLUTIONS.put("How many portals does the End contain?", new String[]{"20"});
        TRIVIA_SOLUTIONS.put("What is the name of the parent of all Wither bosses?", new String[]{"Wither King"});
        TRIVIA_SOLUTIONS.put("What is the name of the parent of all Ender bosses?", new String[]{"Ender Lord"});
    }

    public PuzzleSolver() {
    }

    public void reset() {
        activePuzzle = PuzzleType.NONE;
        currentQuestion = "";
        currentAnswers.clear();
        correctAnswer = "";
        puzzleSolved = false;
        blazeEntries.clear();
        blazeTargetIndex = -1;
        blazeInitialized = false;
    }

    public void parseChatMessage(Component message) {
        String text = message.getString();
        String cleaned = stripColorCodes(text).trim();
        detectPuzzleType(cleaned);
    }

    public void parseChestTitle(String title) {
        if (title == null) return;
        String cleaned = stripColorCodes(title).trim();

        if (cleaned.contains("Trivia")) {
            activePuzzle = PuzzleType.TRIVIA;
            currentQuestion = "";
            currentAnswers.clear();
            correctAnswer = "";
            puzzleSolved = false;
        } else if (cleaned.contains("Blaze")) {
            activePuzzle = PuzzleType.BLAZE;
            blazeEntries.clear();
            blazeTargetIndex = 0;
            blazeInitialized = false;
            puzzleSolved = false;
        }
    }

    private void detectPuzzleType(String cleaned) {
        if (activePuzzle == PuzzleType.TRIVIA || cleaned.contains("?")) {
            processTriviaMessage(cleaned);
        }

        if (activePuzzle == PuzzleType.BLAZE) {
            processBlazeMessage(cleaned);
        }
    }

    private void processTriviaMessage(String text) {
        Matcher questionMatcher = TRIVIA_QUESTION_PATTERN.matcher(text);
        if (questionMatcher.find()) {
            currentQuestion = questionMatcher.group(1).trim();
            currentAnswers.clear();
            correctAnswer = "";
        }

        Matcher answerMatcher = TRIVIA_ANSWER_PATTERN.matcher(text);
        while (answerMatcher.find()) {
            String letter = answerMatcher.group(1);
            String answerText = answerMatcher.group(2).trim();
            currentAnswers.add(letter + ". " + answerText);

            if (!correctAnswer.isEmpty()) continue;

            for (Map.Entry<String, String[]> entry : TRIVIA_SOLUTIONS.entrySet()) {
                if (currentQuestion.contains(entry.getKey())) {
                    for (String solution : entry.getValue()) {
                        if (answerText.equalsIgnoreCase(solution) ||
                                answerText.toLowerCase().contains(solution.toLowerCase())) {
                            correctAnswer = letter + ". " + answerText;
                            activePuzzle = PuzzleType.TRIVIA;
                            puzzleSolved = true;
                            return;
                        }
                    }
                }
            }
        }
    }

    private void processBlazeMessage(String text) {
        if (text.contains("in order from") || text.contains("lowest") || text.contains("highest")) {
            blazeInitialized = true;
            blazeTargetIndex = 0;
        }

        if (text.contains("clicked the correct blaze") || text.contains("correct!")) {
            blazeTargetIndex++;
        }

        if (text.contains("wrong") || text.contains("incorrect")) {
            LOGGER.debug("Blaze puzzle wrong answer detected");
        }
    }

    public void scanBlazeEntities() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;

        blazeEntries.clear();

        for (Entity entity : ((net.minecraft.client.multiplayer.ClientLevel) client.level).entitiesForRendering()) {
            if (entity instanceof ArmorStand armorStand) {
                Component customName = armorStand.getCustomName();
                if (customName != null) {
                    String name = stripColorCodes(customName.getString()).trim();
                    try {
                        int health = Integer.parseInt(name.replaceAll("[^0-9]", ""));
                        blazeEntries.add(new BlazeEntry(
                                armorStand.getId(),
                                health,
                                (float) armorStand.getX(),
                                (float) armorStand.getY(),
                                (float) armorStand.getZ()
                        ));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        blazeEntries.sort(Comparator.comparingInt(BlazeEntry::health));
        blazeInitialized = true;
    }

    public void renderBlazeOverlay(GuiGraphicsExtractor context) {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config == null) return;

        if (activePuzzle != PuzzleType.BLAZE || !blazeInitialized) return;
        if (blazeTargetIndex < 0 || blazeTargetIndex >= blazeEntries.size()) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        BlazeEntry target = blazeEntries.get(blazeTargetIndex);

        for (BlazeEntry entry : blazeEntries) {
            float dx = entry.x - (float) client.player.getX();
            float dy = entry.y - (float) client.player.getY();
            float dz = entry.z - (float) client.player.getZ();
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (distance > 30) continue;

            if (entry == target) {
                renderBlazeMarker(context, entry, 0xFF00FF00, "CLICK");
            } else if (blazeEntries.indexOf(entry) < blazeTargetIndex) {
                renderBlazeMarker(context, entry, 0xFF666666, "DONE");
            } else {
                renderBlazeMarker(context, entry, 0xFFFF0000, (blazeEntries.indexOf(entry) + 1) + "");
            }
        }
    }

    private void renderBlazeMarker(GuiGraphicsExtractor context, BlazeEntry entry, int color, String label) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        float dx = entry.x - (float) client.player.getX();
        float dy = entry.y - (float) client.player.getY() + client.player.getEyeHeight();
        float dz = entry.z - (float) client.player.getZ();

        double distance = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) Math.toDegrees(Math.atan2(-dy, distance));

        float playerYaw = client.player.getYRot();
        float playerPitch = client.player.getXRot();

        float relYaw = yaw - playerYaw;
        float relPitch = pitch - playerPitch;

        while (relYaw > 180) relYaw -= 360;
        while (relYaw < -180) relYaw += 360;

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        int indicatorX = centerX + (int) (relYaw / 90f * centerX);
        int indicatorY = centerY + (int) (relPitch / 90f * centerY);

        indicatorX = Math.max(4, Math.min(screenWidth - 4, indicatorX));
        indicatorY = Math.max(4, Math.min(screenHeight - 4, indicatorY));

        context.fill(indicatorX - 3, indicatorY - 3, indicatorX + 3, indicatorY + 3, color);
        context.text(client.font, label, indicatorX - client.font.width(label) / 2, indicatorY - 12, color, true);
    }

    @Nullable
    public String getTriviaAnswer() {
        return correctAnswer;
    }

    public PuzzleType getActivePuzzle() {
        return activePuzzle;
    }

    public boolean isPuzzleSolved() {
        return puzzleSolved;
    }

    public String getCurrentQuestion() {
        return currentQuestion;
    }

    public List<String> getCurrentAnswers() {
        return Collections.unmodifiableList(currentAnswers);
    }

    public List<BlazeEntry> getBlazeEntries() {
        return Collections.unmodifiableList(blazeEntries);
    }

    public int getBlazeTargetIndex() {
        return blazeTargetIndex;
    }

    private String stripColorCodes(String text) {
        return text.replaceAll("\u00a7[0-9a-fk-orA-FK-OR]", "");
    }

    public enum PuzzleType {
        NONE,
        TRIVIA,
        BLAZE,
        CREEPER,
        WATER_BOARD,
        ICE_PATH,
        SILVERFISH,
        BOULDER,
        TELEPORT
    }

    public record BlazeEntry(int entityId, int health, float x, float y, float z) {
    }
}
