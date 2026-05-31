package io.github.legentpc.neu21plus.client.mining;

import io.github.legentpc.neu21plus.Neu21PlusMod;
import io.github.legentpc.neu21plus.config.NeuConfig;
import io.github.legentpc.neu21plus.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FossilSolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(FossilSolver.class);

    private static final FossilSolver INSTANCE = new FossilSolver();

    private static final Pattern FOSSIL_FOUND_PATTERN = Pattern.compile(".*You found (?:a )?fossil.*");
    private static final Pattern FOSSIL_START_PATTERN = Pattern.compile(".*(?:Fossil|fossil) (?:excavator|puzzle|search).*");
    private static final Pattern FOSSIL_DIG_PATTERN = Pattern.compile(".*(?:You dig|Dug|digging) (?:at |in )?(?:position |cell )?(-?\\d+)[, ]+(-?\\d+).*");
    private static final Pattern FOSSIL_RESULT_PATTERN = Pattern.compile(".*(?:found|contains) (?:a )?(fossil|nothing|empty|dirt).*");
    private static final Pattern FOSSIL_COMPLETE_PATTERN = Pattern.compile(".*(?:excavation|puzzle) (?:complete|finished|done).*");

    private static final int GRID_SIZE = 5;
    private static final int CELL_SIZE = 20;
    private static final int CELL_GAP = 1;

    public static FossilSolver getInstance() {
        return INSTANCE;
    }

    private CellState[][] grid = new CellState[GRID_SIZE][GRID_SIZE];
    private boolean puzzleActive = false;
    private boolean puzzleComplete = false;
    private int confirmedFossils = 0;
    private int totalDigs = 0;
    private int tickCount = 0;
    private long lastGridUpdate = -1;

    private int highlightedRow = -1;
    private int highlightedCol = -1;
    private long highlightTime = -1;

    private boolean[] rowClueUsed = new boolean[GRID_SIZE];
    private boolean[] colClueUsed = new boolean[GRID_SIZE];

    public enum CellState {
        UNKNOWN(0xFF555555, 0xFF333333),
        CONFIRMED(0xFF55FF55, 0xFF33AA33),
        ELIMINATED(0xFFFF5555, 0xFFAA3333),
        HIGHLIGHTED(0xFF55FFFF, 0xFF33AAAA);

        private final int fillColor;
        private final int borderColor;

        CellState(int fillColor, int borderColor) {
            this.fillColor = fillColor;
            this.borderColor = borderColor;
        }

        public int getFillColor() {
            return fillColor;
        }

        public int getBorderColor() {
            return borderColor;
        }
    }

    private FossilSolver() {
        resetGrid();
    }

    private void resetGrid() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                grid[row][col] = CellState.UNKNOWN;
            }
        }
        confirmedFossils = 0;
        totalDigs = 0;
        highlightedRow = -1;
        highlightedCol = -1;
        highlightTime = -1;
        for (int i = 0; i < GRID_SIZE; i++) {
            rowClueUsed[i] = false;
            colClueUsed[i] = false;
        }
    }

    public void tick() {
        tickCount++;

        if (!puzzleActive) return;

        if (highlightTime > 0 && System.currentTimeMillis() - highlightTime > 2000) {
            if (highlightedRow >= 0 && highlightedCol >= 0
                    && highlightedRow < GRID_SIZE && highlightedCol < GRID_SIZE) {
                if (grid[highlightedRow][highlightedCol] == CellState.HIGHLIGHTED) {
                    grid[highlightedRow][highlightedCol] = CellState.UNKNOWN;
                }
            }
            highlightedRow = -1;
            highlightedCol = -1;
            highlightTime = -1;
        }

        if (tickCount % 100 == 0 && puzzleActive && !puzzleComplete) {
            applyDeduction();
        }
    }

    public void onChatMessage(Component message) {
        String text = message.getString();
        String cleaned = TextUtils.stripColorCodes(text).trim();

        Matcher startMatcher = FOSSIL_START_PATTERN.matcher(cleaned);
        if (startMatcher.find()) {
            if (!puzzleActive) {
                puzzleActive = true;
                puzzleComplete = false;
                resetGrid();
                LOGGER.info("Fossil puzzle started");
            }
            return;
        }

        Matcher fossilMatcher = FOSSIL_FOUND_PATTERN.matcher(cleaned);
        if (fossilMatcher.find() && puzzleActive) {
            onFossilFound();
            return;
        }

        Matcher completeMatcher = FOSSIL_COMPLETE_PATTERN.matcher(cleaned);
        if (completeMatcher.find() && puzzleActive) {
            puzzleComplete = true;
            LOGGER.info("Fossil puzzle completed");
            return;
        }

        if (!puzzleActive) return;

        Matcher digMatcher = FOSSIL_DIG_PATTERN.matcher(cleaned);
        if (digMatcher.find()) {
            int row = TextUtils.parseIntSafe(digMatcher.group(1), 0);
            int col = TextUtils.parseIntSafe(digMatcher.group(2), 0);
            onDigAttempt(row, col);
            return;
        }

        Matcher resultMatcher = FOSSIL_RESULT_PATTERN.matcher(cleaned);
        if (resultMatcher.find()) {
            String result = resultMatcher.group(1).toLowerCase();
            if (result.contains("fossil")) {
                onFossilFound();
            } else if (result.contains("nothing") || result.contains("empty") || result.contains("dirt")) {
                onEmptyResult();
            }
        }
    }

    private void onDigAttempt(int row, int col) {
        int adjustedRow = adjustCoordinate(row);
        int adjustedCol = adjustCoordinate(col);

        if (adjustedRow >= 0 && adjustedRow < GRID_SIZE
                && adjustedCol >= 0 && adjustedCol < GRID_SIZE) {
            if (grid[adjustedRow][adjustedCol] == CellState.UNKNOWN) {
                grid[adjustedRow][adjustedCol] = CellState.HIGHLIGHTED;
                highlightedRow = adjustedRow;
                highlightedCol = adjustedCol;
                highlightTime = System.currentTimeMillis();
            }
            totalDigs++;
            lastGridUpdate = System.currentTimeMillis();
        }
    }

    private int adjustCoordinate(int coord) {
        if (coord >= 0 && coord < GRID_SIZE) return coord;
        if (coord >= 1 && coord <= GRID_SIZE) return coord - 1;
        return Math.abs(coord) % GRID_SIZE;
    }

    private void onFossilFound() {
        if (highlightedRow >= 0 && highlightedCol >= 0
                && highlightedRow < GRID_SIZE && highlightedCol < GRID_SIZE) {
            grid[highlightedRow][highlightedCol] = CellState.CONFIRMED;
            rowClueUsed[highlightedRow] = true;
            colClueUsed[highlightedCol] = true;
        }
        confirmedFossils++;
        highlightedRow = -1;
        highlightedCol = -1;
        highlightTime = -1;
        lastGridUpdate = System.currentTimeMillis();
        LOGGER.debug("Fossil found at grid position (total: {})", confirmedFossils);
    }

    private void onEmptyResult() {
        if (highlightedRow >= 0 && highlightedCol >= 0
                && highlightedRow < GRID_SIZE && highlightedCol < GRID_SIZE) {
            grid[highlightedRow][highlightedCol] = CellState.ELIMINATED;
        }
        highlightedRow = -1;
        highlightedCol = -1;
        highlightTime = -1;
        lastGridUpdate = System.currentTimeMillis();
    }

    private void applyDeduction() {
        for (int row = 0; row < GRID_SIZE; row++) {
            if (rowClueUsed[row]) {
                for (int col = 0; col < GRID_SIZE; col++) {
                    if (grid[row][col] == CellState.UNKNOWN) {
                        grid[row][col] = CellState.ELIMINATED;
                    }
                }
            }
        }

        for (int col = 0; col < GRID_SIZE; col++) {
            if (colClueUsed[col]) {
                for (int row = 0; row < GRID_SIZE; row++) {
                    if (grid[row][col] == CellState.UNKNOWN) {
                        grid[row][col] = CellState.ELIMINATED;
                    }
                }
            }
        }
    }

    public void setCellState(int row, int col, CellState state) {
        if (row < 0 || row >= GRID_SIZE || col < 0 || col >= GRID_SIZE) return;

        CellState previous = grid[row][col];
        grid[row][col] = state;

        if (state == CellState.CONFIRMED && previous != CellState.CONFIRMED) {
            confirmedFossils++;
            rowClueUsed[row] = true;
            colClueUsed[col] = true;
        } else if (previous == CellState.CONFIRMED && state != CellState.CONFIRMED) {
            confirmedFossils--;
        }

        lastGridUpdate = System.currentTimeMillis();
    }

    public void render(GuiGraphicsExtractor context, int screenWidth, int screenHeight) {
        NeuConfig config = Neu21PlusMod.getInstance().getConfig();
        if (config == null || !config.mining.fossilSolver) return;
        if (!puzzleActive) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        int totalGridSize = GRID_SIZE * CELL_SIZE + (GRID_SIZE + 1) * CELL_GAP;
        int gridStartX = (screenWidth - totalGridSize) / 2;
        int gridStartY = (screenHeight - totalGridSize) / 2 - 20;

        int titleHeight = client.font.lineHeight + 6;
        int panelWidth = totalGridSize + 8;
        int panelHeight = totalGridSize + titleHeight + 24;
        int panelX = gridStartX - 4;
        int panelY = gridStartY - titleHeight - 4;

        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0x90000000);
        context.fill(panelX, panelY, panelX + panelWidth, panelY + 1, 0xFF55FFFF);
        context.fill(panelX, panelY + panelHeight - 1, panelX + panelWidth, panelY + panelHeight, 0xFF55FFFF);

        String titleText = puzzleComplete ? "Fossil Puzzle Complete!" : "Fossil Solver";
        int titleWidth = client.font.width(titleText);
        int titleColor = puzzleComplete ? 0xFF55FF55 : 0xFF55FFFF;
        context.text(client.font, titleText, gridStartX + (totalGridSize - titleWidth) / 2, panelY + 3, titleColor, true);

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                int cellX = gridStartX + col * (CELL_SIZE + CELL_GAP) + CELL_GAP;
                int cellY = gridStartY + row * (CELL_SIZE + CELL_GAP) + CELL_GAP;

                CellState state = grid[row][col];

                context.fill(cellX, cellY, cellX + CELL_SIZE, cellY + CELL_SIZE, state.getFillColor());
                context.fill(cellX, cellY, cellX + CELL_SIZE, cellY + 1, state.getBorderColor());
                context.fill(cellX, cellY + CELL_SIZE - 1, cellX + CELL_SIZE, cellY + CELL_SIZE, state.getBorderColor());
                context.fill(cellX, cellY, cellX + 1, cellY + CELL_SIZE, state.getBorderColor());
                context.fill(cellX + CELL_SIZE - 1, cellY, cellX + CELL_SIZE, cellY + CELL_SIZE, state.getBorderColor());

                if (state == CellState.CONFIRMED) {
                    String symbol = "\u2714";
                    int symbolWidth = client.font.width(symbol);
                    context.text(client.font, symbol, cellX + (CELL_SIZE - symbolWidth) / 2,
                            cellY + (CELL_SIZE - client.font.lineHeight) / 2, 0xFFFFFFFF, true);
                } else if (state == CellState.ELIMINATED) {
                    String symbol = "\u2716";
                    int symbolWidth = client.font.width(symbol);
                    context.text(client.font, symbol, cellX + (CELL_SIZE - symbolWidth) / 2,
                            cellY + (CELL_SIZE - client.font.lineHeight) / 2, 0xFFAAAAAA, true);
                } else if (state == CellState.HIGHLIGHTED) {
                    String symbol = "?";
                    int symbolWidth = client.font.width(symbol);
                    context.text(client.font, symbol, cellX + (CELL_SIZE - symbolWidth) / 2,
                            cellY + (CELL_SIZE - client.font.lineHeight) / 2, 0xFFFFFFFF, true);
                }
            }
        }

        String infoText = "Fossils: " + confirmedFossils + "  Digs: " + totalDigs;
        int infoWidth = client.font.width(infoText);
        context.text(client.font, infoText, gridStartX + (totalGridSize - infoWidth) / 2,
                gridStartY + totalGridSize + 4, 0xFFAAAAAA, false);
    }

    public void reset() {
        puzzleActive = false;
        puzzleComplete = false;
        tickCount = 0;
        lastGridUpdate = -1;
        resetGrid();
    }

    public boolean isPuzzleActive() {
        return puzzleActive;
    }

    public boolean isPuzzleComplete() {
        return puzzleComplete;
    }

    public int getConfirmedFossils() {
        return confirmedFossils;
    }

    public int getTotalDigs() {
        return totalDigs;
    }

    public CellState getCellState(int row, int col) {
        if (row < 0 || row >= GRID_SIZE || col < 0 || col >= GRID_SIZE) return CellState.UNKNOWN;
        return grid[row][col];
    }

    public int getGridSize() {
        return GRID_SIZE;
    }

}
