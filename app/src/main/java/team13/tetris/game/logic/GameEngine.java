package team13.tetris.game.logic;

import team13.tetris.data.ScoreBoard;
import team13.tetris.game.controller.GameStateListener;
import team13.tetris.game.model.Board;
import team13.tetris.game.model.Tetromino;
import team13.tetris.game.Timer;

import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GameEngine {
    private final Board board;
    private final GameStateListener listener;
    private Tetromino current;
    private Tetromino next;
    private int px, py;
    private final Random rnd = new Random();
    private int score = 0;
    private final Timer gameTimer;
    private final ScoreBoard.ScoreEntry.Mode difficulty;
    private int speedPerClearLines = 3;

    private volatile double dropIntervalSeconds = 1.0;

    private final Object schedulerLock = new Object();
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> autoDropFuture;

    private long lastDropTime = 0;
    private long pauseStartTime = 0;

    private int totalLinesCleared = 0;
    private boolean itemModeEnabled = false;
    private Tetromino nextItemPiece = null;

    private boolean weightCollisionDetected = false;
    private boolean isHardDrop = false;

    public GameEngine(Board board, GameStateListener listener) {
        this(board, listener, ScoreBoard.ScoreEntry.Mode.NORMAL);
    }

    public GameEngine(Board board, GameStateListener listener, ScoreBoard.ScoreEntry.Mode difficulty) {
        this.board = board;
        this.listener = listener;
        this.difficulty = difficulty;
        this.gameTimer = new Timer();
        this.itemModeEnabled = (difficulty == ScoreBoard.ScoreEntry.Mode.ITEM);
    }

    public void startNewGame() {
        board.clear();
        next = randomPiece();
        spawnNext();
        listener.onScoreChanged(score);
        stopAutoDrop();
        startAutoDrop();
    }

    // Roulette Wheel Selection으로 난이도별 가중치 적용
    // EASY: I블록 20%↑, NORMAL: 균등, HARD: I블록 20%↓
    private Tetromino randomPiece() {
        if (itemModeEnabled && nextItemPiece != null) {
            Tetromino itemPiece = nextItemPiece;
            nextItemPiece = null;
            return itemPiece;
        }

        int[] weights = getWeightsByDifficulty();

        int totalWeight = 0;
        for (int weight : weights)
            totalWeight += weight;

        int randomValue = rnd.nextInt(totalWeight);
        int cumulativeWeight = 0;

        for (int i = 0; i < weights.length; i++) {
            cumulativeWeight += weights[i];
            if (randomValue < cumulativeWeight) {
                return getPieceByIndex(i);
            }
        }

        return Tetromino.of(Tetromino.Kind.I);
    }

    private int[] getWeightsByDifficulty() {
        switch (difficulty) {
            case EASY:
                return new int[] { 12, 10, 10, 10, 10, 10, 10 };
            case HARD:
                return new int[] { 8, 10, 10, 10, 10, 10, 10 };
            case ITEM:
                return new int[] { 10, 10, 10, 10, 10, 10, 10 };
            case NORMAL:
            default:
                return new int[] { 10, 10, 10, 10, 10, 10, 10 };
        }
    }

    private Tetromino getPieceByIndex(int index) {
        switch (index) {
            case 0:
                return Tetromino.of(Tetromino.Kind.I);
            case 1:
                return Tetromino.of(Tetromino.Kind.O);
            case 2:
                return Tetromino.of(Tetromino.Kind.T);
            case 3:
                return Tetromino.of(Tetromino.Kind.S);
            case 4:
                return Tetromino.of(Tetromino.Kind.Z);
            case 5:
                return Tetromino.of(Tetromino.Kind.J);
            case 6:
                return Tetromino.of(Tetromino.Kind.L);
            default:
                return Tetromino.of(Tetromino.Kind.I);
        }
    }

    private Tetromino createItemPiece(Tetromino.Kind itemKind, Tetromino.Kind targetKind) {
        if (itemKind == Tetromino.Kind.COPY) {
            int copyBlockIndex = (int) (Math.random() * 4);
            Tetromino result = new Tetromino(targetKind, 0, copyBlockIndex);
            return result;
        } else if (itemKind == Tetromino.Kind.WEIGHT) {
            return new Tetromino(itemKind, 0, 0);
        } else if (itemKind == Tetromino.Kind.GRAVITY) {
            return new Tetromino(itemKind, 0, 0);
        } else if (itemKind == Tetromino.Kind.SPLIT) {
            return new Tetromino(itemKind, 0, 0);
        } else {
            return new Tetromino(itemKind, 0, 0);
        }
    }

    private void generateItemPiece() {
        if (!itemModeEnabled) return;

        int itemChoice = rnd.nextInt(5);

        if (itemChoice == 0) {
            Tetromino.Kind[] kinds = {
                    Tetromino.Kind.I, Tetromino.Kind.O, Tetromino.Kind.T,
                    Tetromino.Kind.S, Tetromino.Kind.Z, Tetromino.Kind.J, Tetromino.Kind.L
            };
            Tetromino.Kind targetKind = kinds[rnd.nextInt(kinds.length)];
            nextItemPiece = createItemPiece(Tetromino.Kind.COPY, targetKind);
        } else if (itemChoice == 1) {
            nextItemPiece = createItemPiece(Tetromino.Kind.WEIGHT, null);
        } else if (itemChoice == 2) {
            nextItemPiece = createItemPiece(Tetromino.Kind.GRAVITY, null);
        } else if (itemChoice == 3) {
            nextItemPiece = createItemPiece(Tetromino.Kind.SPLIT, null);
        } else {
            Tetromino.Kind[] kinds = {
                    Tetromino.Kind.I, Tetromino.Kind.O, Tetromino.Kind.T,
                    Tetromino.Kind.S, Tetromino.Kind.Z, Tetromino.Kind.J, Tetromino.Kind.L
            };
            Tetromino.Kind targetKind = kinds[rnd.nextInt(kinds.length)];
            nextItemPiece = createLineClearItemPiece(targetKind);
        }
    }

    private Tetromino createLineClearItemPiece(Tetromino.Kind targetKind) {
        int lineClearBlockIndex = (int) (Math.random() * 4);
        Tetromino result = new Tetromino(targetKind, 0, lineClearBlockIndex, Tetromino.ItemType.LINE_CLEAR);
        return result;
    }

    private void processItemEffect(Tetromino.ItemType itemType, Tetromino.Kind targetKind) {
        if (!itemModeEnabled) return;

        if (itemType == Tetromino.ItemType.COPY) {
            if (targetKind != null) {
                current = next != null ? next : randomPiece();
                next = new Tetromino(targetKind, 0);

                px = (board.getWidth() - current.getWidth()) / 2;
                py = 0;

                if (!board.fits(current.getShape(), px, py)) {
                    current = null;
                    javafx.application.Platform.runLater(() -> {
                        listener.onGameOver();
                    });
                    stopAutoDrop();
                    return;
                }

                listener.onPieceSpawned(current, px, py);
                listener.onNextPiece(next);
                listener.onBoardUpdated(board);
            }
        } else if (itemType == Tetromino.ItemType.GRAVITY) {
            processGravityEffect();
        } else if (itemType == Tetromino.ItemType.SPLIT) {
            processSplitEffect();
        } else if (itemType == Tetromino.ItemType.LINE_CLEAR) {
            processLineClearEffect();
        }
    }

    private void processWeightEffect() {
        if (current == null || !current.isItemPiece()) return;

        int[][] shape = current.getShape();
        int bottomRow = -1;
        java.util.Set<Integer> occupiedColumns = new java.util.HashSet<>();

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != 0) {
                    int boardX = px + c;
                    int boardY = py + r;

                    if (boardX >= 0 && boardX < board.getWidth() && boardY >= 0 && boardY < board.getHeight()) {
                        occupiedColumns.add(boardX);
                        bottomRow = Math.max(bottomRow, boardY);
                    }
                }
            }
        }

        java.util.List<Integer> rowsToDestroy = new java.util.ArrayList<>();
        for (int y = bottomRow + 1; y < board.getHeight(); y++) {
            boolean hasBlocksInRow = false;
            for (int col : occupiedColumns) {
                if (board.getCell(col, y) != 0) {
                    hasBlocksInRow = true;
                    break;
                }
            }
            if (hasBlocksInRow) rowsToDestroy.add(y);
        }

        destroyRowsSequentially(occupiedColumns, rowsToDestroy, 0);
    }

    private void destroyRowsSequentially(
        java.util.Set<Integer> columns,
        java.util.List<Integer> rows,
        int currentIndex
    ) {
        if (currentIndex >= rows.size()) {
            board.applyGravity();
            listener.onBoardUpdated(board);
            return;
        }

        int currentRow = rows.get(currentIndex);

        for (int col : columns) {
            if (board.getCell(col, currentRow) != 0) board.setCell(col, currentRow, 0);
        }

        listener.onBoardUpdated(board);

        java.util.Timer timer = new java.util.Timer();
        timer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                destroyRowsSequentially(columns, rows, currentIndex + 1);
                timer.cancel();
            }
        }, 100);
    }

    private void destroyLineDirectlyBelow() {
        if (current == null || !current.isItemPiece() || current.getKind() != Tetromino.Kind.WEIGHT) return;

        int[][] shape = current.getShape();
        int bottomRow = -1;
        java.util.Set<Integer> occupiedColumns = new java.util.HashSet<>();

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != 0) {
                    int boardX = px + c;
                    int boardY = py + r;

                    if (boardX >= 0 && boardX < board.getWidth() && boardY >= 0 && boardY < board.getHeight()) {
                        occupiedColumns.add(boardX);
                        bottomRow = Math.max(bottomRow, boardY);
                    }
                }
            }
        }

        int targetRow = bottomRow + 1;
        if (targetRow < board.getHeight()) {
            for (int col : occupiedColumns) {
                if (board.getCell(col, targetRow) != 0) board.setCell(col, targetRow, 0);
            }
        }
    }

    private void destroyAllBlocksBelow() {
        if (current == null || !current.isItemPiece() || current.getKind() != Tetromino.Kind.WEIGHT) return;

        int[][] shape = current.getShape();
        int bottomRow = -1;
        java.util.Set<Integer> occupiedColumns = new java.util.HashSet<>();

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != 0) {
                    int boardX = px + c;
                    int boardY = py + r;

                    if (boardX >= 0 && boardX < board.getWidth() && boardY >= 0 && boardY < board.getHeight()) {
                        occupiedColumns.add(boardX);
                        bottomRow = Math.max(bottomRow, boardY);
                    }
                }
            }
        }

        for (int y = bottomRow + 1; y < board.getHeight(); y++) {
            for (int col : occupiedColumns) {
                if (board.getCell(col, y) != 0) board.setCell(col, y, 0);
            }
        }
    }

    private void processGravityEffect() {
        board.applyGravity();
    }

    private void processSplitEffect() {
        if (current == null || !current.isItemPiece()) return;

        int[][] shape = current.getShape();
        java.util.Set<Integer> occupiedColumns = new java.util.HashSet<>();

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != 0) {
                    int worldCol = px + c;
                    if (worldCol >= 0 && worldCol < board.getWidth()) {
                        occupiedColumns.add(worldCol);
                    }
                }
            }
        }

        for (int col : occupiedColumns) applySingleColumnGravity(col);
    }

    private void applySingleColumnGravity(int col) {
        if (col < 0 || col >= board.getWidth()) return;

        int height = board.getHeight();
        java.util.List<Integer> splitBlocks = new java.util.ArrayList<>();

        for (int y = 0; y < height; y++) {
            int val = board.getCell(col, y);
            if (val >= 500 && val < 600) {
                splitBlocks.add(val);
                board.setCell(col, y, 0);
            }
        }

        for (int blockValue : splitBlocks) {
            int dropPosition = 0;

            while (dropPosition < height - 1 && board.getCell(col, dropPosition + 1) == 0) {
                dropPosition++;
            }

            board.setCell(col, dropPosition, blockValue);
        }
    }

    private void processLineClearEffect() {
        if (current == null || !current.isItemPiece()) return;

        int[][] shape = current.getShape();
        int lineClearBlockIndex = current.getLineClearBlockIndex();
        int blockCount = 0;
        int targetRow = -1;

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != 0) {
                    if (blockCount == lineClearBlockIndex) {
                        targetRow = py + r;
                        break;
                    }
                    blockCount++;
                }
            }
            if (targetRow != -1) break;
        }

        if (targetRow >= 0 && targetRow < board.getHeight()) {
            for (int r = targetRow; r > 0; r--) {
                for (int c = 0; c < board.getWidth(); c++) {
                    board.setCell(c, r, board.getCell(c, r - 1));
                }
            }

            for (int c = 0; c < board.getWidth(); c++) board.setCell(c, 0, 0);

            totalLinesCleared += 1;

            addScoreForClearedLines(1);
            updateSpeedForLinesCleared(1, totalLinesCleared);

            listener.onBoardUpdated(board);
        }
    }

    private Tetromino.Kind getKindFromId(int id) {
        for (Tetromino.Kind kind : Tetromino.Kind.values()) {
            if (kind.getId() == id) return kind;
        }
        return null;
    }

    private void spawnNext() {
        weightCollisionDetected = false;

        current = next != null ? next : randomPiece();

        if (itemModeEnabled && nextItemPiece != null) {
            next = nextItemPiece;
            nextItemPiece = null;
        } else {
            next = randomPiece();
        }

        px = (board.getWidth() - current.getWidth()) / 2;
        py = 0;

        if (!board.fits(current.getShape(), px, py)) {
            stopAutoDrop();
            current = null;

            javafx.application.Platform.runLater(() -> {
                listener.onGameOver();
            });

            return;
        }

        listener.onPieceSpawned(current, px, py);
        listener.onNextPiece(next);
        listener.onBoardUpdated(board);
    }

    public void startAutoDrop() {
        synchronized (schedulerLock) {
            if (scheduler == null || scheduler.isShutdown()) {
                scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                    Thread t = new Thread(r, "GameEngine-AutoDrop");
                    t.setDaemon(true);
                    return t;
                });
            }
            if (autoDropFuture != null && !autoDropFuture.isCancelled()) return;

            long periodMillis = Math.max(1L, (long) (dropIntervalSeconds * 1000.0));
            long initialDelay = periodMillis;
            long currentTime = System.currentTimeMillis();

            if (pauseStartTime > 0) {
                if (lastDropTime > 0) {
                    long pauseDuration = currentTime - pauseStartTime;
                    lastDropTime += pauseDuration;
                    long timeSinceLastDrop = currentTime - lastDropTime;
                    long remainingTime = periodMillis - (timeSinceLastDrop % periodMillis);
                    initialDelay = Math.max(1L, remainingTime);
                } else {
                    lastDropTime = currentTime;
                    initialDelay = periodMillis;
                }
                pauseStartTime = 0;
            } else {
                lastDropTime = currentTime;
            }

            autoDropFuture = scheduler.scheduleAtFixedRate(() -> {
                try {
                    lastDropTime = System.currentTimeMillis();
                    softDrop();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }, initialDelay, periodMillis, TimeUnit.MILLISECONDS);
        }
    }

    public void stopAutoDrop() {
        synchronized (schedulerLock) {
            pauseStartTime = System.currentTimeMillis();

            if (autoDropFuture != null) {
                autoDropFuture.cancel(false);
                autoDropFuture = null;
            }
        }
    }

    public void shutdown() {
        synchronized (schedulerLock) {
            if (autoDropFuture != null) {
                autoDropFuture.cancel(false);
                autoDropFuture = null;
            }
            if (scheduler != null && !scheduler.isShutdown()) {
                try {
                    scheduler.shutdownNow();
                } catch (Throwable ignored) {
                }
                scheduler = null;
            }
        }
    }

    public double getDropIntervalSeconds() {
        return dropIntervalSeconds;
    }

    public void setDropIntervalSeconds(double seconds) {
        if (seconds <= 0) throw new IllegalArgumentException("drop interval must be > 0");
        synchronized (schedulerLock) {
            this.dropIntervalSeconds = seconds;
            if (autoDropFuture != null && !autoDropFuture.isCancelled()) {
                autoDropFuture.cancel(false);
                long periodMillis = Math.max(1L, (long) (dropIntervalSeconds * 1000.0));
                autoDropFuture = scheduler.scheduleAtFixedRate(() -> {
                    try {
                        softDrop();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }, periodMillis, periodMillis, TimeUnit.MILLISECONDS);
            }
        }
    }

    public void moveLeft() {
        if (current == null) return;
        if (weightCollisionDetected) return;

        if (board.fits(current.getShape(), px - 1, py)) {
            px--;
            listener.onBoardUpdated(board);
        }
    }

    public void moveRight() {
        if (current == null) return;
        if (weightCollisionDetected) return;

        if (board.fits(current.getShape(), px + 1, py)) {
            px++;
            listener.onBoardUpdated(board);
        }
    }

    public void rotateCW() {
        if (current == null) return;
        if (current.isItemPiece() && current.getKind() == Tetromino.Kind.WEIGHT) return;

        Tetromino rotated = current.rotateClockwise();
        int[][] offsets = new int[][] { { 0, 0 }, { -1, 0 }, { 1, 0 }, { -2, 0 }, { 2, 0 }, { 0, -1 } };
        for (int[] off : offsets) {
            int nx = px + off[0];
            int ny = py + off[1];
            if (board.fits(rotated.getShape(), nx, ny)) {
                current = rotated;
                px = nx;
                py = ny;
                listener.onBoardUpdated(board);
                return;
            }
        }
    }

    public boolean softDrop() {
        if (current == null) return false;
        if (board.fits(current.getShape(), px, py + 1)) {
            py++;

            if (current.isItemPiece() && current.getKind() == Tetromino.Kind.WEIGHT && weightCollisionDetected) {
                destroyLineDirectlyBelow();
            }

            addDropScore(1);
            listener.onBoardUpdated(board);
            return true;
        } else {
            if (current.isItemPiece() && current.getKind() == Tetromino.Kind.WEIGHT && !weightCollisionDetected) {
                weightCollisionDetected = true;
                destroyLineDirectlyBelow();
                return false;
            }

            if (current.isItemPiece()) {
                String itemTypeStr = current.getItemType().name();
                int itemBlockIndex = -1;

                if (current.getItemType() == Tetromino.ItemType.COPY) {
                    itemBlockIndex = current.getCopyBlockIndex();
                } else if (current.getItemType() == Tetromino.ItemType.LINE_CLEAR) {
                    itemBlockIndex = current.getLineClearBlockIndex();
                } else {
                    itemBlockIndex = 0;
                }

                board.placeItemPiece(current.getShape(), px, py, current.getId(), itemBlockIndex, itemTypeStr);
            } else {
                board.placePiece(current.getShape(), px, py, current.getId());
            }
            handleLockedPiece();
            return false;
        }
    }

    public void hardDrop() {
        if (current == null)
            return;

        isHardDrop = true;

        int startY = py;

        if (current.isItemPiece() && current.getKind() == Tetromino.Kind.WEIGHT) {
            weightCollisionDetected = true;

            while (board.fits(current.getShape(), px, py + 1))
                py++;

            destroyAllBlocksBelow();

            while (board.fits(current.getShape(), px, py + 1))
                py++;
        } else {
            while (board.fits(current.getShape(), px, py + 1))
                py++;
        }

        int dropDistance = py - startY;

        if (dropDistance > 0) addHardDropScore(dropDistance);

        if (current.isItemPiece()) {
            String itemTypeStr = current.getItemType().name();
            int itemBlockIndex = -1;

            if (current.getItemType() == Tetromino.ItemType.COPY) {
                itemBlockIndex = current.getCopyBlockIndex();
            } else if (current.getItemType() == Tetromino.ItemType.LINE_CLEAR) {
                itemBlockIndex = current.getLineClearBlockIndex();
            } else {
                itemBlockIndex = 0;
            }

            board.placeItemPiece(current.getShape(), px, py, current.getId(), itemBlockIndex, itemTypeStr);
        } else {
            board.placePiece(current.getShape(), px, py, current.getId());
        }

        handleLockedPiece();

        isHardDrop = false;
    }

    private void handleLockedPiece() {
        if (current == null) return;

        if (itemModeEnabled && current != null && current.isItemPiece()) {
            Tetromino.Kind kind = current.getKind();
            Tetromino.ItemType itemType = current.getItemType();

            if (kind == Tetromino.Kind.GRAVITY) {
                processGravityEffect();
            } else if (kind == Tetromino.Kind.SPLIT) {
                processSplitEffect();
            } else if (itemType == Tetromino.ItemType.LINE_CLEAR) {
                processLineClearEffect();
            }
        }

        java.util.List<Integer> fullLines = board.getFullLineIndices();

        current = null;

        if (fullLines.isEmpty()) {
            spawnNext();
            return;
        }

        boolean hasItemBlockInFullLines = false;
        Tetromino.Kind itemPieceKind = null;
        Tetromino.ItemType detectedItemType = null;

        if (itemModeEnabled) {
            int[][] snapshot = board.snapshot();
            for (int row : fullLines) {
                for (int c = 0; c < snapshot[row].length; c++) {
                    if (snapshot[row][c] != 0) {
                    }
                    if (snapshot[row][c] >= 100 && snapshot[row][c] < 600) {
                        hasItemBlockInFullLines = true;
                        int originalId = snapshot[row][c] % 100;
                        itemPieceKind = getKindFromId(originalId);

                        if (snapshot[row][c] >= 100 && snapshot[row][c] < 200) {
                            detectedItemType = Tetromino.ItemType.COPY;
                        } else if (snapshot[row][c] >= 200 && snapshot[row][c] < 300) {
                            detectedItemType = Tetromino.ItemType.LINE_CLEAR;
                        } else if (snapshot[row][c] >= 300 && snapshot[row][c] < 400) {
                            detectedItemType = Tetromino.ItemType.WEIGHT;
                        } else if (snapshot[row][c] >= 400 && snapshot[row][c] < 500) {
                            detectedItemType = Tetromino.ItemType.GRAVITY;
                        } else if (snapshot[row][c] >= 500 && snapshot[row][c] < 600) {
                            detectedItemType = Tetromino.ItemType.SPLIT;
                        }

                        break;
                    }
                }
                if (hasItemBlockInFullLines) break;
            }
        }

        for (int row : fullLines) board.fillLineWith(row, -1);
        listener.onBoardUpdated(board);

        final boolean finalHasItemBlock = hasItemBlockInFullLines;
        final Tetromino.Kind finalItemPieceKind = itemPieceKind;
        final Tetromino.ItemType finalDetectedItemType = detectedItemType;

        java.util.Timer delayTimer = new java.util.Timer();
        delayTimer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                boolean copyEffectProcessed = false;

                if (itemModeEnabled && finalHasItemBlock && finalDetectedItemType != null) {
                    if (finalDetectedItemType == Tetromino.ItemType.COPY) copyEffectProcessed = true;
                    processItemEffect(finalDetectedItemType, finalItemPieceKind);
                }

                Runnable itemEffectCallback = () -> {};

                int cleared = board.clearFullLines(itemEffectCallback);
                if (cleared > 0) listener.onLinesCleared(cleared);
                if (cleared > 0) {
                    addScoreForClearedLines(cleared);
                    listener.onScoreChanged(score);
                }
                listener.onBoardUpdated(board);

                if (!copyEffectProcessed) spawnNext();
                delayTimer.cancel();
            }
        }, 150);
    }

    public void addScoreForClearedLines(int cleared) {
        switch (cleared) {
            case 1:
                score += 100;
                break;
            case 2:
                score += 250;
                break;
            case 3:
                score += 500;
                break;
            case 4:
                score += 1000;
                break;
            default:
                if (cleared > 4) score += 1000 + (cleared - 4) * 250;
                break;
        }

        if (itemModeEnabled && cleared > 0) {
            totalLinesCleared += cleared;

            int beforeClear = totalLinesCleared - cleared;
            int currentGroup = totalLinesCleared / 10;
            int previousGroup = beforeClear / 10;

            if (currentGroup > previousGroup) generateItemPiece();
        }
    }

    public void addDropScore(int dropDistance) {
        int dropPoints = gameTimer.calculateDropScore(dropDistance);
        score += dropPoints;
        listener.onScoreChanged(score);
    }

    public void addHardDropScore(int dropDistance) {
        int dropPoints = gameTimer.getHardDropScore(dropDistance);
        score += dropPoints;
        listener.onScoreChanged(score);
    }

    public Timer getGameTimer() {
        return gameTimer;
    }

    public void updateSpeedForLinesCleared(int clearedLines, int totalLinesCleared) {
        int newSpeedLevel = totalLinesCleared / speedPerClearLines;
        if (newSpeedLevel > (totalLinesCleared - clearedLines) / speedPerClearLines) {
            double speedMultiplier = getSpeedIncreaseMultiplier();
            gameTimer.increaseSpeed(speedMultiplier);
            double newInterval = gameTimer.getInterval() / 1000.0;
            setDropIntervalSeconds(newInterval);
        }
    }

    private double getSpeedIncreaseMultiplier() {
        switch (difficulty) {
            case EASY:
                return 0.8;
            case HARD:
                return 1.2;
            case NORMAL:
            default:
                return 1.0;
        }
    }

    public Tetromino getNext() {
        return next;
    }

    public int getScore() {
        return score;
    }

    public Board getBoard() {
        return board;
    }

    public Tetromino getCurrent() {
        return current;
    }

    public int getPieceX() {
        return px;
    }

    public int getPieceY() {
        return py;
    }

    public int getTotalLinesCleared() {
        return totalLinesCleared;
    }

    public Tetromino generateTestPiece() {
        return randomPiece();
    }
}
