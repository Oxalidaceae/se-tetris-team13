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

/**
 * 최소한의 게임 엔진으로, 조각 스폰, 이동, 회전, 라인 제거 기능을 제공합니다.
 * 전체 실시간 루프를 구현하지는 않으며, UI 루프나 스케줄러와 함께 사용하도록 설계되었습니다.
 */
public class GameEngine {
    private final Board board;
    private final GameStateListener listener;
    private Tetromino current;
    private Tetromino next;
    private int px, py;
    private final Random rnd = new Random();
    private int score = 0;
    private final Timer gameTimer; // 점수 계산을 위한 타이머
    private final ScoreBoard.ScoreEntry.Mode difficulty; // 난이도 정보

    // 자동 하강 간격(초) - 설정 가능
    private volatile double dropIntervalSeconds = 1.0;

    // 자동 하강을 위한 스케줄러
    private final Object schedulerLock = new Object();
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> autoDropFuture;

    public GameEngine(Board board, GameStateListener listener) {
        this(board, listener, ScoreBoard.ScoreEntry.Mode.NORMAL);
    }

    public GameEngine(Board board, GameStateListener listener, ScoreBoard.ScoreEntry.Mode difficulty) {
        this.board = board;
        this.listener = listener;
        this.difficulty = difficulty;
        this.gameTimer = new Timer(); // 점수 계산용 타이머 초기화
    }

    public void startNewGame() {
        board.clear();
        // prepare next bag
        next = randomPiece();
        spawnNext();
        listener.onScoreChanged(score);
        // restart automatic dropping when a new game starts
        stopAutoDrop();
        startAutoDrop();
    }

    /**
     * Roulette Wheel Selection을 사용하여 난이도별 가중치로 블록을 선택합니다.
     * EASY: I블록 12, 나머지 10 (I블록이 20% 더 자주)
     * NORMAL: 모두 10 (균등 분포)
     * HARD: I블록 8, 나머지 10 (I블록이 20% 덜 자주)
     */
    private Tetromino randomPiece() {
        // 난이도별 가중치 배열 [I, O, T, S, Z, J, L]
        int[] weights = getWeightsByDifficulty();

        // 총 가중치 계산
        int totalWeight = 0;
        for (int weight : weights) {
            totalWeight += weight;
        }

        // Roulette Wheel Selection
        int randomValue = rnd.nextInt(totalWeight);
        int cumulativeWeight = 0;

        for (int i = 0; i < weights.length; i++) {
            cumulativeWeight += weights[i];
            if (randomValue < cumulativeWeight) {
                return getPieceByIndex(i);
            }
        }

        // Fallback (should never reach here)
        return Tetromino.of(Tetromino.Kind.I);
    }

    /**
     * 난이도에 따른 블록 가중치 배열 반환
     * @return [I, O, T, S, Z, J, L] 순서의 가중치 배열
     */
    private int[] getWeightsByDifficulty() {
        switch (difficulty) {
            case EASY:
                // I블록 12, 나머지 10 (I블록이 20% 더 자주 등장)
                return new int[] { 12, 10, 10, 10, 10, 10, 10 };
            case HARD:
                // I블록 8, 나머지 10 (I블록이 20% 덜 등장)
                return new int[] { 8, 10, 10, 10, 10, 10, 10 };
            case NORMAL:
            default:
                // 모두 동일 가중치
                return new int[] { 10, 10, 10, 10, 10, 10, 10 };
        }
    }

    /**
     * 인덱스에 해당하는 Tetromino 반환
     * @param index 0:I, 1:O, 2:T, 3:S, 4:Z, 5:J, 6:L
     */
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

    private void spawnNext() {
        current = next != null ? next : randomPiece();
        next = randomPiece();
        px = (board.getWidth() - current.getWidth()) / 2;
        py = 0;
        if (!board.fits(current.getShape(), px, py)) {
            // 게임 오버: 알리고 자동 하강을 중지합니다
            listener.onGameOver();
            stopAutoDrop();
            return;
        }
        listener.onPieceSpawned(current, px, py);
        listener.onNextPiece(next);
        listener.onBoardUpdated(board);
    }

    /**
     * 현재 설정된 하강 간격을 사용하여 자동 하강 스케줄러를 시작합니다.
     * 여러 번 호출해도 안전하며, 필요 시 스케줄러를 생성합니다.
     */
    public void startAutoDrop() {
        synchronized (schedulerLock) {
            if (scheduler == null || scheduler.isShutdown()) {
                scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                    Thread t = new Thread(r, "GameEngine-AutoDrop");
                    t.setDaemon(true);
                    return t;
                });
            }
            if (autoDropFuture != null && !autoDropFuture.isCancelled()) {
                // already running
                return;
            }
            long periodMillis = Math.max(1L, (long) (dropIntervalSeconds * 1000.0));
            autoDropFuture = scheduler.scheduleAtFixedRate(() -> {
                try {
                    // call softDrop on the engine thread; listener/UI will handle thread-safety
                    softDrop();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }, periodMillis, periodMillis, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 자동 하강 스케줄러를 중지하고 예약된 작업을 취소합니다.
     */
    public void stopAutoDrop() {
        synchronized (schedulerLock) {
            if (autoDropFuture != null) {
                autoDropFuture.cancel(false);
                autoDropFuture = null;
            }
            if (scheduler != null && !scheduler.isShutdown()) {
                try {
                    scheduler.shutdownNow();
                } catch (Throwable ignored) {}
                scheduler = null;
            }
        }
    }

    public double getDropIntervalSeconds() {
        return dropIntervalSeconds;
    }

    /**
     * 자동 하강 간격(초)을 설정합니다. 자동 하강이 동작 중이면 새로운 간격으로 재스케줄링됩니다.
     */
    public void setDropIntervalSeconds(double seconds) {
        if (seconds <= 0)
            throw new IllegalArgumentException("drop interval must be > 0");
        synchronized (schedulerLock) {
            this.dropIntervalSeconds = seconds;
            // if running, restart with new interval
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
        if (current == null)
            return;
        if (board.fits(current.getShape(), px - 1, py)) {
            px--;
            listener.onBoardUpdated(board);
        }
    }

    public void moveRight() {
        if (current == null)
            return;
        if (board.fits(current.getShape(), px + 1, py)) {
            px++;
            listener.onBoardUpdated(board);
        }
    }

    public void rotateCW() {
        if (current == null)
            return;
        Tetromino rotated = current.rotateClockwise();
        // try wall-kick offsets: prefer no-offset, then left/right small kicks, then
        // larger kicks, then upward kick
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
        if (current == null)
            return false;
        if (board.fits(current.getShape(), px, py + 1)) {
            py++;
            // 소프트 드롭 점수 추가 (한 칸 하강)
            addDropScore(1);
            listener.onBoardUpdated(board);
            return true;
        } else {
            // place
            board.placePiece(current.getShape(), px, py, current.getId());
            handleLockedPiece();
            return false;
        }
    }

    public void hardDrop() {
        if (current == null)
            return;
        int startY = py; // 시작 위치 기록
        while (board.fits(current.getShape(), px, py + 1))
            py++;
        int dropDistance = py - startY; // 떨어진 거리 계산

        // 하드 드롭 점수 추가 (거리 > 0일 때만)
        if (dropDistance > 0) {
            addHardDropScore(dropDistance);
        }

        board.placePiece(current.getShape(), px, py, current.getId());
        handleLockedPiece();
    }

    // Handles animation + scoring after the falling piece is fixed to the board.
    private void handleLockedPiece() {
        java.util.List<Integer> fullLines = board.getFullLineIndices();
        current = null; // 잠시 조작을 막고, 보드에는 고정된 조각만 남김

        if (fullLines.isEmpty()) {
            spawnNext();
            return;
        }

        for (int row : fullLines) {
            board.fillLineWith(row, -1); // 흰색 플래시 마커
        }
        listener.onBoardUpdated(board);

        // 잠깐(150ms) 보여준 뒤에 실제로 라인을 제거하고 다음 조각을 소환
        javafx.application.Platform.runLater(() -> {
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
                    javafx.util.Duration.millis(150));
            pause.setOnFinished(e -> {
                int cleared = board.clearFullLines();
                if (cleared > 0)
                    listener.onLinesCleared(cleared);
                if (cleared > 0) {
                    addScoreForClearedLines(cleared);
                    listener.onScoreChanged(score);
                }
                listener.onBoardUpdated(board);
                spawnNext();
            });
            pause.play();
        });
    }

    /**
     * 제거된 라인 수에 따른 점수 추가:
     * 1 -> 100, 2 -> 250, 3 -> 500, 4 -> 1000
     */
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
                if (cleared > 4)
                    score += 1000 + (cleared - 4) * 250;
                break; // graceful handling
        }
    }

    /**
     * 블록 하강에 따른 점수 추가 (10점 × 거리 × 속도 계수)
     * @param dropDistance 하강한 칸 수
     */
    public void addDropScore(int dropDistance) {
        int dropPoints = gameTimer.calculateDropScore(dropDistance);
        score += dropPoints;
        listener.onScoreChanged(score);
        // 소프트 드롭과 자동 드롭은 너무 빈번하므로 로그 비활성화
        // System.out.println("Drop score: " + dropPoints + " (Distance: " +
        // dropDistance + ", Speed: " + String.format("%.1f",
        // gameTimer.getSpeedFactor()) + "x)");
    }

    /**
     * 하드 드롭에 따른 점수 추가
     * @param dropDistance 하강한 칸 수
     */
    public void addHardDropScore(int dropDistance) {
        int dropPoints = gameTimer.getHardDropScore(dropDistance);
        score += dropPoints;
        listener.onScoreChanged(score);
    }

    /**
     * 게임 타이머 접근자 (속도 조정용)
     * @return 게임 타이머 인스턴스
     */
    public Timer getGameTimer() {
        return gameTimer;
    }

    /**
     * 라인 클리어 시 게임 속도 증가 (10줄마다)
     * Timer의 속도와 GameEngine의 드롭 간격을 동기화합니다.
     * 난이도에 따라 속도 증가율이 달라집니다:
     * - EASY: 20% 덜 증가 (0.8배)
     * - NORMAL: 기본 증가 (1.0배)
     * - HARD: 20% 더 증가 (1.2배)
     * @param clearedLines 이번에 클리어된 라인 수
     * @param totalLinesCleared 총 클리어된 라인 수
     */
    public void updateSpeedForLinesCleared(int clearedLines, int totalLinesCleared) {
        // 10줄마다 속도 증가
        int newSpeedLevel = totalLinesCleared / 10;
        if (newSpeedLevel > (totalLinesCleared - clearedLines) / 10) {
            // 난이도에 따른 속도 증가 배율 적용
            double speedMultiplier = getSpeedIncreaseMultiplier();
            gameTimer.increaseSpeed(speedMultiplier);
            // Timer의 새로운 속도로 드롭 간격 업데이트
            double newInterval = gameTimer.getInterval() / 1000.0; // milliseconds to seconds
            setDropIntervalSeconds(newInterval);
        }
    }

    /**
     * 난이도에 따른 속도 증가 배율을 반환합니다.
     * @return EASY: 0.8, NORMAL: 1.0, HARD: 1.2
     */
    private double getSpeedIncreaseMultiplier() {
        switch (difficulty) {
            case EASY:
                return 0.8; // 20% 덜 증가
            case HARD:
                return 1.2; // 20% 더 증가
            case NORMAL:
            default:
                return 1.0; // 기본 증가율
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

    /**
     * 테스트용: 난이도 기반 랜덤 피스를 생성합니다.
     * 이 메서드는 Roulette Wheel Selection 알고리즘을 테스트하기 위해 사용됩니다.
     */
    public Tetromino generateTestPiece() {
        return randomPiece();
    }
}
