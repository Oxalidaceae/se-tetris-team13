package team13.tetris.game;

import team13.tetris.ui.ScoreBoard;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Represents the current state of a Tetris game session
 */
enum GameState {
    READY,      // Game initialized, ready to start
    PLAYING,    // Game actively running
    PAUSED,     // Game paused
    GAME_OVER   // Game ended
}

/**
 * Main game manager class that handles the entire Tetris game
 * Manages game loop, scene transitions, and game state
 */
public class GameManager {
    
    // TODO: Game components to be implemented by other teams
    // private Board board;
    // private Block currentBlock;
    // private Block nextBlock;
    // private InputHandler inputHandler;
    // private Renderer renderer;
    
    private ScoreBoard scoreBoard;
    private Timer gameTimer;
    private GameState state;
    private AnimationTimer gameLoop;
    private Stage primaryStage;
    
    private int currentScore;
    private int linesCleared;
    private long lastDropTime;
    private int lastSpeedUpLevel; // Tracks difficulty level for speed progression
    
    /**
     * GameManager constructor
     * @param primaryStage Main stage
     */
    public GameManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.scoreBoard = new ScoreBoard();
        this.gameTimer = new Timer();
        this.state = GameState.READY;
        this.currentScore = 0;
        this.linesCleared = 0;
        this.lastDropTime = 0;
        this.lastSpeedUpLevel = 0;
        
        initializeGameLoop();
    }
    
    /**
     * Initialize the game loop
     */
    private void initializeGameLoop() {
        gameLoop = new AnimationTimer() {
            private long lastTime = 0;
            
            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }
                
                double deltaTime = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;
                
                if (state == GameState.PLAYING) {
                    updateGame(deltaTime);
                }
            }
        };
    }
    
    /**
     * Starts a new game (resets scores and begins game loop)
     */
    public void startGame() {
        state = GameState.PLAYING;
        currentScore = 0;
        linesCleared = 0;
        gameTimer.reset();
        lastDropTime = System.currentTimeMillis();
        lastSpeedUpLevel = 0;
        
        // TODO: Initialize game board and spawn first blocks
        // board.clear();
        // spawnNewBlock();
        
        gameLoop.start();
        System.out.println("Game started!");
    }
    
    /**
     * Update the game (called from game loop)
     * @param deltaTime Time elapsed since last frame (seconds)
     */
    private void updateGame(double deltaTime) {
        if (state != GameState.PLAYING) {
            return;
        }
        
        gameTimer.tick(deltaTime);
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastDropTime >= gameTimer.getInterval()) {
            dropCurrentBlock();
            lastDropTime = currentTime;
        }
        
        // TODO: Implement remaining game logic
        // - Block movement and collision detection
        // - Line clearing and game over checks
        // - Rendering updates
    }
    
    /**
     * Handles automatic block dropping based on current game speed
     */
    private void dropCurrentBlock() {
        // TODO: Implement block dropping logic
        // if (canMoveDown(currentBlock)) {
        //     currentBlock.moveDown();
        // } else {
        //     placeBlock(); checkLines(); spawnNewBlock();
        // }
        
        System.out.println("Block drop - Time: " + gameTimer.getFormattedTime() + 
                          ", Level: " + gameTimer.getCurrentLevel());
    }
    
    /**
     * Toggle game pause/resume
     */
    public void togglePause() {
        if (state == GameState.PLAYING) {
            state = GameState.PAUSED;
            System.out.println("Game paused.");
        } else if (state == GameState.PAUSED) {
            state = GameState.PLAYING;
            lastDropTime = System.currentTimeMillis(); // Reset time
            System.out.println("Game resumed.");
        }
    }
    
    /**
     * Ends current game session
     */
    public void endGame() {
        endGame(true);
    }
    
    /**
     * Ends current game session with optional UI display
     * @param showUI Whether to show the game over dialog
     */
    public void endGame(boolean showUI) {
        state = GameState.GAME_OVER;
        gameLoop.stop();
        
        System.out.println("Game Over! Final Score: " + currentScore);
        if (showUI) {
            showGameOverScene();
        }
    }
    
    /**
     * Displays game over dialog for score entry (FR6 requirement)
     */
    public void showGameOverScene() {
        scoreBoard.addScoreWithDialog(primaryStage, currentScore, this::showScoreboardScene);
    }
    
    /**
     * Displays scoreboard with Play Again option
     */
    private void showScoreboardScene() {
        Scene scoreScene = scoreBoard.createScoreScene(primaryStage, this::startGame);
        primaryStage.setScene(scoreScene);
        primaryStage.setTitle("Tetris - High Scores");
    }
    
    /**
     * Add points to current score
     * @param points Points to add
     */
    public void addScore(int points) {
        currentScore += points;
    }
    
    /**
     * Called when lines are cleared
     * @param lines Number of lines cleared
     */
    public void linesCleared(int lines) {
        this.linesCleared += lines;
        
        int currentDifficultyLevel = this.linesCleared / 10;
        if (currentDifficultyLevel > lastSpeedUpLevel) {
            gameTimer.increaseSpeed();
            lastSpeedUpLevel = currentDifficultyLevel;
            System.out.println("Difficulty increased! Lines: " + this.linesCleared + 
                             " - Speed: " + String.format("%.1f", gameTimer.getSpeedFactor()) + "x" +
                             " - Timer Level: " + gameTimer.getCurrentLevel());
        }
        
        // Standard Tetris scoring with level multiplier
        int points = 0;
        switch (lines) {
            case 1: points = 100 * gameTimer.getCurrentLevel(); break;
            case 2: points = 300 * gameTimer.getCurrentLevel(); break;
            case 3: points = 500 * gameTimer.getCurrentLevel(); break;
            case 4: points = 800 * gameTimer.getCurrentLevel(); break;
        }
        
        addScore(points);
        System.out.println(lines + " lines cleared! Points: " + points + " (Timer Level: " + gameTimer.getCurrentLevel() + ")");
    }
    
    public GameState getState() { return state; }
    public int getCurrentScore() { return currentScore; }
    public int getLinesCleared() { return linesCleared; }
    public Timer getGameTimer() { return gameTimer; }
    public ScoreBoard getScoreBoard() { return scoreBoard; }
    public boolean isGameRunning() { return state == GameState.PLAYING || state == GameState.PAUSED; }
    
    /**
     * @return Difficulty level based on lines cleared (every 10 lines = +1 level)
     */
    public int getDifficultyLevel() { return linesCleared / 10; }
    
    /**
     * @return Speed-based level from Timer (used for scoring calculations)
     */
    public int getSpeedLevel() { return gameTimer.getCurrentLevel(); }
}