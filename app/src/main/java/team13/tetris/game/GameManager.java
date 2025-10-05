package team13.tetris.game;

import team13.tetris.ui.ScoreBoard;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Game state enumeration
 */
enum GameState {
    MENU,       // Menu screen
    PLAYING,    // Game in progress
    PAUSED,     // Game paused
    GAME_OVER   // Game over
}

/**
 * Main game manager class that handles the entire Tetris game
 * Manages game loop, scene transitions, and game state
 */
public class GameManager {
    
    // Game components (to be implemented by other teams)
    // private Board board;                 // Game board
    // private Block currentBlock;          // Current falling block
    // private Block nextBlock;             // Next block to spawn
    // private InputHandler inputHandler;   // Input handler
    // private Renderer renderer;           // Game renderer
    
    private ScoreBoard scoreBoard;       // Score board
    private Timer gameTimer;             // Game timer
    private GameState state;             // Current game state
    private AnimationTimer gameLoop;     // Game loop
    private Stage primaryStage;          // Main stage
    
    // Game related variables
    private int currentScore;
    private int linesCleared;
    private long lastDropTime;
    private boolean gameRunning;
    private int lastSpeedUpLevel; // Track last difficulty level when speed was increased (lines/10)
    
    /**
     * GameManager constructor
     * @param primaryStage Main stage
     */
    public GameManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.scoreBoard = new ScoreBoard();
        this.gameTimer = new Timer();
        this.state = GameState.MENU;
        this.currentScore = 0;
        this.linesCleared = 0;
        this.lastDropTime = 0;
        this.gameRunning = false;
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
                
                double deltaTime = (now - lastTime) / 1_000_000_000.0; // Convert nanoseconds to seconds
                lastTime = now;
                
                if (state == GameState.PLAYING) {
                    updateGame(deltaTime);
                }
            }
        };
    }
    
    /**
     * Start the game
     */
    public void startGame() {
        state = GameState.PLAYING;
        gameRunning = true;
        currentScore = 0;
        linesCleared = 0;
        gameTimer.reset();
        lastDropTime = System.currentTimeMillis();
        lastSpeedUpLevel = 0;
        
        // TODO: Initialize board, generate first block, etc.
        // board.clear();
        // currentBlock = generateNewBlock();
        // nextBlock = generateNewBlock();
        
        gameLoop.start();
        System.out.println("Game started!");
    }
    
    /**
     * Update the game (called from game loop)
     * @param deltaTime Time elapsed since last frame (seconds)
     */
    private void updateGame(double deltaTime) {
        if (!gameRunning || state != GameState.PLAYING) {
            return;
        }
        
        // Update timer
        gameTimer.tick(deltaTime);
        
        // Handle automatic block dropping
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastDropTime >= gameTimer.getInterval()) {
            dropCurrentBlock();
            lastDropTime = currentTime;
        }
        
        // TODO: Game logic processing
        // - Update current block position
        // - Collision detection
        // - Line clearing check
        // - Game over condition check
        // - Rendering update
        
        // Level progression is now handled in linesCleared() method
    }
    
    /**
     * Drop the current block one row down
     */
    private void dropCurrentBlock() {
        // TODO: Implement actual block dropping logic
        // if (canMoveDown(currentBlock)) {
        //     currentBlock.moveDown();
        // } else {
        //     placeBlock();
        //     checkLines();
        //     spawnNewBlock();
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
     * End the game
     */
    public void endGame() {
        gameRunning = false;
        state = GameState.GAME_OVER;
        gameLoop.stop();
        
        System.out.println("Game Over! Final Score: " + currentScore);
        showGameOverScene();
    }
    
    /**
     * Show game over scene with high scores
     */
    public void showGameOverScene() {
        // Add score with player name input - Required by FR6
        scoreBoard.addScoreWithDialog(primaryStage, currentScore, this::showScoreboardScene);
    }
    
    /**
     * Show scoreboard scene after name input
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
        
        // Check for difficulty progression (every 10 lines cleared)
        int currentDifficultyLevel = this.linesCleared / 10;
        if (currentDifficultyLevel > lastSpeedUpLevel) {
            gameTimer.increaseSpeed();
            lastSpeedUpLevel = currentDifficultyLevel;
            System.out.println("Difficulty increased! Lines: " + this.linesCleared + 
                             " - Speed: " + String.format("%.1f", gameTimer.getSpeedFactor()) + "x" +
                             " - Timer Level: " + gameTimer.getCurrentLevel());
        }
        
        // Calculate points based on lines cleared (Tetris standard)
        // Using Timer's level (based on speed factor) for scoring
        int points = 0;
        switch (lines) {
            case 1: points = 100 * gameTimer.getCurrentLevel(); break;
            case 2: points = 300 * gameTimer.getCurrentLevel(); break;
            case 3: points = 500 * gameTimer.getCurrentLevel(); break;
            case 4: points = 800 * gameTimer.getCurrentLevel(); break; // Tetris!
        }
        
        addScore(points);
        System.out.println(lines + " lines cleared! Points: " + points + " (Timer Level: " + gameTimer.getCurrentLevel() + ")");
    }
    
    // Getter methods
    public GameState getState() { return state; }
    public int getCurrentScore() { return currentScore; }
    public int getLinesCleared() { return linesCleared; }
    public Timer getGameTimer() { return gameTimer; }
    public ScoreBoard getScoreBoard() { return scoreBoard; }
    public boolean isGameRunning() { return gameRunning; }
    
    /**
     * Get current difficulty level (based on lines cleared / 10)
     * This is different from Timer's getCurrentLevel() which is based on speed factor
     * @return Difficulty level (0-based)
     */
    public int getDifficultyLevel() { return linesCleared / 10; }
    
    /**
     * Get speed-based level from Timer (based on speed factor)
     * Used for scoring calculations
     * @return Speed level from Timer
     */
    public int getSpeedLevel() { return gameTimer.getCurrentLevel(); }
}