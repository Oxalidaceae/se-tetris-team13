package team13.tetris.game;

import team13.tetris.data.ScoreBoard;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

enum GameState {
    READY, PLAYING, PAUSED, GAME_OVER
}

/**
 * Main game manager - handles Tetris game logic without UI dependencies.
 * Uses ScheduledExecutorService for 60 FPS game loop.
 */
public class GameManager {
    // Core components
    private ScoreBoard scoreBoard;
    private Timer gameTimer;
    private GameState state;
    private ScheduledExecutorService gameLoop;
    
    // Game state
    private int currentScore;
    private int linesCleared;
    private long lastDropTime;
    private int lastDifficultyLevel;
    
    // TODO: Game components to be implemented by other teams
    // private Board board, currentBlock, nextBlock;
    // private InputHandler inputHandler;
    
    /**
     * GameManager constructor for pure game logic management.
     */
    public GameManager() {
        this.scoreBoard = new ScoreBoard();
        this.gameTimer = new Timer();
        this.state = GameState.READY;
        this.currentScore = 0;
        this.linesCleared = 0;
        this.lastDropTime = 0;
        this.lastDifficultyLevel = 0;
        // Executor will be created when game starts
    }
    
    /** Starts new game (resets state and begins 60 FPS loop) */
    public void startGame() {
        // Create new game loop if needed (for multiple games)
        if (gameLoop == null || gameLoop.isShutdown()) {
            gameLoop = Executors.newSingleThreadScheduledExecutor();
        }
        
        state = GameState.PLAYING;
        currentScore = 0;
        linesCleared = 0;
        gameTimer.reset();
        lastDropTime = System.currentTimeMillis();
        lastDifficultyLevel = 0;
        // TODO: Initialize game board and spawn first blocks
        startGameLoop();
        System.out.println("Game started!");
    }
    
    /** Starts 60 FPS game loop */
    private void startGameLoop() {
        gameLoop.scheduleAtFixedRate(this::updateGame, 0, 16, TimeUnit.MILLISECONDS);
    }
    
    /** Stops game loop */
    private void stopGameLoop() {
        if (gameLoop != null && !gameLoop.isShutdown()) {
            gameLoop.shutdown();
        }
    }

    /** Main game update (called every 16ms) */
    private void updateGame() {
        if (state != GameState.PLAYING) return;
        
        gameTimer.tick(0.016); // Fixed 16ms delta time
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastDropTime >= gameTimer.getInterval()) {
            dropCurrentBlock();
            lastDropTime = currentTime;
        }
        // TODO: Block movement, collision detection, line clearing
    }
    
    /** Handles automatic block dropping */
    private void dropCurrentBlock() {
        // TODO: Implement block dropping logic
        System.out.println("Block drop - Time: " + gameTimer.getFormattedTime() + 
                          ", Speed Level: " + gameTimer.getSpeedLevel());
    }
    
    /** Toggle pause/resume */
    public void togglePause() {
        if (state == GameState.PLAYING) {
            state = GameState.PAUSED;
            System.out.println("Game paused.");
        } else if (state == GameState.PAUSED) {
            state = GameState.PLAYING;
            lastDropTime = System.currentTimeMillis();
            System.out.println("Game resumed.");
        }
    }
    
    /** Ends current game session */
    public void endGame() {
        endGame(true);
    }
    
    /** Ends game session with optional score display */
    public void endGame(boolean showUI) {
        state = GameState.GAME_OVER;
        stopGameLoop();
        System.out.println("Game Over! Final Score: " + currentScore);
        if (showUI) handleGameOver();
    }
    
    /** Handles game over - adds score and shows leaderboard */
    public void handleGameOver() {
        scoreBoard.addScore("Player", currentScore, ScoreBoard.ScoreEntry.Mode.NORMAL);
        System.out.println("Game Over! Final Score: " + currentScore);
        System.out.println("\n=== High Scores ===");
        scoreBoard.getScores().stream()
            .limit(10)
            .forEach(entry -> System.out.println(entry.getName() + ": " + entry.getScore()));
    }
    
    /** Add points to current score */
    public void addScore(int points) {
        currentScore += points;
    }
    
    /** Process line clearing and update difficulty/score */
    public void linesCleared(int lines) {
        this.linesCleared += lines;
        
        // Check difficulty increase (every 10 lines)
        int difficultyLevel = this.linesCleared / 10;
        if (difficultyLevel > lastDifficultyLevel) {
            gameTimer.increaseSpeed();
            lastDifficultyLevel = difficultyLevel;
            System.out.println("Difficulty increased! Lines: " + this.linesCleared + 
                             " - Speed: " + String.format("%.1f", gameTimer.getSpeedFactor()) + "x" +
                             " - Speed Level: " + gameTimer.getSpeedLevel());
        }
        
        // Standard Tetris scoring
        int points = switch (lines) {
            case 1 -> 100 * gameTimer.getSpeedLevel();
            case 2 -> 300 * gameTimer.getSpeedLevel();
            case 3 -> 500 * gameTimer.getSpeedLevel();
            case 4 -> 800 * gameTimer.getSpeedLevel();
            default -> 0;
        };
        
        addScore(points);
        System.out.println(lines + " lines cleared! Points: " + points + " (Speed Level: " + gameTimer.getSpeedLevel() + ")");
    }
    
    /**
     * Add points for soft drop (player manually drops block one cell)
     */
    public void addSoftDropScore() {
        int points = gameTimer.getSoftDropScore();
        addScore(points);
        // System.out.println("Soft drop! Points: " + points + " (Speed: " + String.format("%.1f", gameTimer.getSpeedFactor()) + "x)");
    }
    
    /**
     * Add points for hard drop based on distance
     * @param dropDistance Number of cells the block dropped
     */
    public void addHardDropScore(int dropDistance) {
        int points = gameTimer.getHardDropScore(dropDistance);
        addScore(points);
        System.out.println("Hard drop! Distance: " + dropDistance + " cells, Points: " + points + " (Speed: " + String.format("%.1f", gameTimer.getSpeedFactor()) + "x)");
    }
    
    /**
     * Add points for automatic drop (when block falls naturally)
     * @param dropDistance Number of cells the block dropped automatically
     */
    public void addAutoDropScore(int dropDistance) {
        int points = gameTimer.calculateDropScore(dropDistance);
        addScore(points);
        // Uncomment below for debugging automatic drops
        // System.out.println("Auto drop! Distance: " + dropDistance + " cells, Points: " + points);
    }
    
    // Getters
    public GameState getState() { return state; }
    public int getCurrentScore() { return currentScore; }
    public int getLinesCleared() { return linesCleared; }
    public Timer getGameTimer() { return gameTimer; }
    public ScoreBoard getScoreBoard() { return scoreBoard; }
    public boolean isGameRunning() { return state == GameState.PLAYING || state == GameState.PAUSED; }
    public int getDifficultyLevel() { return linesCleared / 10; }
    public int getSpeedLevel() { return gameTimer.getSpeedLevel(); }
}