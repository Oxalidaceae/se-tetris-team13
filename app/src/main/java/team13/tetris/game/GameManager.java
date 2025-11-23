package team13.tetris.game;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import team13.tetris.data.ScoreBoard;

enum GameState {
    READY,
    PLAYING,
    PAUSED,
    GAME_OVER
}

public class GameManager {
    private ScoreBoard scoreBoard;
    private Timer gameTimer;
    private GameState state;
    private ScheduledExecutorService gameLoop;
    private int currentScore;
    private int linesCleared;
    private long lastDropTime;
    private int lastDifficultyLevel;
    private long pauseStartTime;

    public GameManager() {
        this.scoreBoard = new ScoreBoard();
        this.gameTimer = new Timer();
        this.state = GameState.READY;
        this.currentScore = 0;
        this.linesCleared = 0;
        this.lastDropTime = 0;
        this.lastDifficultyLevel = 0;
        this.pauseStartTime = 0;
    }

    public void startGame() {
        if (gameLoop == null || gameLoop.isShutdown())
            gameLoop = Executors.newSingleThreadScheduledExecutor();

        state = GameState.PLAYING;
        currentScore = 0;
        linesCleared = 0;
        gameTimer.reset();
        lastDropTime = System.currentTimeMillis();
        lastDifficultyLevel = 0;
        startGameLoop();
        System.out.println("Game started!");
    }

    private void startGameLoop() {
        gameLoop.scheduleAtFixedRate(this::updateGame, 0, 16, TimeUnit.MILLISECONDS);
    }

    private void stopGameLoop() {
        if (gameLoop != null && !gameLoop.isShutdown()) gameLoop.shutdown();
    }

    private void updateGame() {
        if (state != GameState.PLAYING) return;

        gameTimer.tick(0.016);
        long currentTime = System.currentTimeMillis();
        long timeSinceLastDrop = currentTime - lastDropTime;
        long dropInterval = (long) gameTimer.getInterval();

        if (timeSinceLastDrop >= dropInterval) {
            dropCurrentBlock();
            lastDropTime = currentTime;
        }
    }

    private void dropCurrentBlock() {
        System.out.println(
                "Block drop - Time: "
                        + gameTimer.getFormattedTime()
                        + ", Speed Level: "
                        + gameTimer.getSpeedLevel());
    }

    public void togglePause() {
        if (state == GameState.PLAYING) {
            state = GameState.PAUSED;
            pauseStartTime = System.currentTimeMillis();
            System.out.println("Game paused.");
        } else if (state == GameState.PAUSED) {
            state = GameState.PLAYING;
            long currentTime = System.currentTimeMillis();
            long pauseDuration = currentTime - pauseStartTime;
            lastDropTime += pauseDuration;
            System.out.println("Game resumed.");
        }
    }

    public void endGame() {
        endGame(true);
    }

    public void endGame(boolean showUI) {
        state = GameState.GAME_OVER;
        stopGameLoop();
        System.out.println("Game Over! Final Score: " + currentScore);

        if (showUI) handleGameOver();
    }

    public void handleGameOver() {
        scoreBoard.addScore("Player", currentScore, ScoreBoard.ScoreEntry.Mode.NORMAL);
        System.out.println("Game Over! Final Score: " + currentScore);
        System.out.println("\n=== High Scores ===");
        scoreBoard.getScores().stream()
                .limit(10)
                .forEach(entry -> System.out.println(entry.getName() + ": " + entry.getScore()));
    }

    public void addScore(int points) {
        currentScore += points;
    }

    public void linesCleared(int lines) {
        this.linesCleared += lines;
        int difficultyLevel = this.linesCleared / 10;

        if (difficultyLevel > lastDifficultyLevel) {
            gameTimer.increaseSpeed();
            lastDifficultyLevel = difficultyLevel;
            System.out.println(
                    "Difficulty increased! Lines: "
                            + this.linesCleared
                            + " - Speed: "
                            + String.format("%.1f", gameTimer.getSpeedFactor())
                            + "x"
                            + " - Speed Level: "
                            + gameTimer.getSpeedLevel());
        }

        int points =
                switch (lines) {
                    case 1 -> 100 * gameTimer.getSpeedLevel();
                    case 2 -> 300 * gameTimer.getSpeedLevel();
                    case 3 -> 500 * gameTimer.getSpeedLevel();
                    case 4 -> 800 * gameTimer.getSpeedLevel();
                    default -> 0;
                };

        addScore(points);
        System.out.println(
                lines
                        + " lines cleared! Points: "
                        + points
                        + " (Speed Level: "
                        + gameTimer.getSpeedLevel()
                        + ")");
    }

    public void addSoftDropScore() {
        int points = gameTimer.getSoftDropScore();
        addScore(points);
    }

    public void addHardDropScore(int dropDistance) {
        int points = gameTimer.getHardDropScore(dropDistance);
        addScore(points);
        System.out.println(
                "Hard drop! Distance: "
                        + dropDistance
                        + " cells, Points: "
                        + points
                        + " (Speed: "
                        + String.format("%.1f", gameTimer.getSpeedFactor())
                        + "x)");
    }

    public void addAutoDropScore(int dropDistance) {
        int points = gameTimer.calculateDropScore(dropDistance);
        addScore(points);
    }

    public GameState getState() {
        return state;
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public int getLinesCleared() {
        return linesCleared;
    }

    public Timer getGameTimer() {
        return gameTimer;
    }

    public ScoreBoard getScoreBoard() {
        return scoreBoard;
    }

    public boolean isGameRunning() {
        return state == GameState.PLAYING || state == GameState.PAUSED;
    }

    public int getDifficultyLevel() {
        return linesCleared / 10;
    }

    public int getSpeedLevel() {
        return gameTimer.getSpeedLevel();
    }
}
