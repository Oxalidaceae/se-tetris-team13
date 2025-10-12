package team13.tetris.game.logic;

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

    // 자동 하강 간격(초) - 설정 가능
    private volatile double dropIntervalSeconds = 1.0;

    // 자동 하강을 위한 스케줄러
    private final Object schedulerLock = new Object();
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> autoDropFuture;

    public GameEngine(Board board, GameStateListener listener) {
        this.board = board;
        this.listener = listener;
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

    private Tetromino randomPiece() {
        // Use full 7-piece set from Tetromino.Kind
        int choice = rnd.nextInt(7);
        switch (choice) {
            case 0: return Tetromino.of(Tetromino.Kind.I);
            case 1: return Tetromino.of(Tetromino.Kind.O);
            case 2: return Tetromino.of(Tetromino.Kind.T);
            case 3: return Tetromino.of(Tetromino.Kind.S);
            case 4: return Tetromino.of(Tetromino.Kind.Z);
            case 5: return Tetromino.of(Tetromino.Kind.J);
            default: return Tetromino.of(Tetromino.Kind.L);
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

    public double getDropIntervalSeconds() { return dropIntervalSeconds; }

    /**
     * 자동 하강 간격(초)을 설정합니다. 자동 하강이 동작 중이면 새로운 간격으로 재스케줄링됩니다.
     */
    public void setDropIntervalSeconds(double seconds) {
        if (seconds <= 0) throw new IllegalArgumentException("drop interval must be > 0");
        synchronized (schedulerLock) {
            this.dropIntervalSeconds = seconds;
            // if running, restart with new interval
            if (autoDropFuture != null && !autoDropFuture.isCancelled()) {
                autoDropFuture.cancel(false);
                long periodMillis = Math.max(1L, (long) (dropIntervalSeconds * 1000.0));
                autoDropFuture = scheduler.scheduleAtFixedRate(() -> {
                    try { softDrop(); } catch (Throwable t) { t.printStackTrace(); }
                }, periodMillis, periodMillis, TimeUnit.MILLISECONDS);
            }
        }
    }

    public void moveLeft() {
        if (current == null) return;
        if (board.fits(current.getShape(), px - 1, py)) {
            px--;
            listener.onBoardUpdated(board);
        }
    }

    public void moveRight() {
        if (current == null) return;
        if (board.fits(current.getShape(), px + 1, py)) {
            px++;
            listener.onBoardUpdated(board);
        }
    }

    public void rotateCW() {
        if (current == null) return;
        Tetromino rotated = current.rotateClockwise();
        // try wall-kick offsets: prefer no-offset, then left/right small kicks, then larger kicks, then upward kick
        int[][] offsets = new int[][] { {0,0}, {-1,0}, {1,0}, {-2,0}, {2,0}, {0,-1} };
        for (int[] off : offsets) {
            int nx = px + off[0];
            int ny = py + off[1];
            if (board.fits(rotated.getShape(), nx, ny)) {
                current = rotated;
                px = nx; py = ny;
                listener.onBoardUpdated(board);
                return;
            }
        }
    }

    

    public boolean softDrop() {
        if (current == null) return false;
        if (board.fits(current.getShape(), px, py + 1)) {
            py++;
            // 소프트 드롭 점수 추가 (한 칸 하강)
            addDropScore(1);
            listener.onBoardUpdated(board);
            return true;
        } else {
            // place
            board.placePiece(current.getShape(), px, py, current.getId());
            int cleared = board.clearLinesAndReturnCount();
            if (cleared > 0) listener.onLinesCleared(cleared);
            if (cleared > 0) {
                addScoreForClearedLines(cleared);
                listener.onScoreChanged(score);
            }
            spawnNext();
            return false;
        }
    }

    public void hardDrop() {
        if (current == null) return;
        int startY = py; // 시작 위치 기록
        while (board.fits(current.getShape(), px, py + 1)) py++;
        int dropDistance = py - startY; // 떨어진 거리 계산
        
        // 하드 드롭 점수 추가 (거리 > 0일 때만)
        if (dropDistance > 0) {
            addHardDropScore(dropDistance);
        }
        
        board.placePiece(current.getShape(), px, py, current.getId());
        int cleared = board.clearLinesAndReturnCount();
        if (cleared > 0) listener.onLinesCleared(cleared);
        if (cleared > 0) {
            addScoreForClearedLines(cleared);
            listener.onScoreChanged(score);
        }
        spawnNext();
    }

    /**
     * 제거된 라인 수에 따른 점수 추가:
     * 1 -> 100, 2 -> 250, 3 -> 500, 4 -> 1000
     */
    public void addScoreForClearedLines(int cleared) {
        switch (cleared) {
            case 1: score += 100; break;
            case 2: score += 250; break;
            case 3: score += 500; break;
            case 4: score += 1000; break;
            default: if (cleared > 4) score += 1000 + (cleared - 4) * 250; break; // graceful handling
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
        // System.out.println("Drop score: " + dropPoints + " (Distance: " + dropDistance + ", Speed: " + String.format("%.1f", gameTimer.getSpeedFactor()) + "x)");
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
     * @param clearedLines 이번에 클리어된 라인 수
     * @param totalLinesCleared 총 클리어된 라인 수
     */
    public void updateSpeedForLinesCleared(int clearedLines, int totalLinesCleared) {
        // 10줄마다 속도 증가
        int newSpeedLevel = totalLinesCleared / 10;
        if (newSpeedLevel > (totalLinesCleared - clearedLines) / 10) {
            gameTimer.increaseSpeed();
            // Timer의 새로운 속도로 드롭 간격 업데이트
            double newInterval = gameTimer.getInterval() / 1000.0; // milliseconds to seconds
            setDropIntervalSeconds(newInterval);
        }
    }

    public Tetromino getNext() { return next; }
    public int getScore() { return score; }

    public Board getBoard() { return board; }
    public Tetromino getCurrent() { return current; }
    public int getPieceX() { return px; }
    public int getPieceY() { return py; }
}
