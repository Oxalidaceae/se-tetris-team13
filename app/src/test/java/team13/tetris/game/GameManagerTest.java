package team13.tetris.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for GameManager - pure game logic without UI dependencies
 */
public class GameManagerTest {
    
    private GameManager gameManager;
    
    @BeforeEach
    void setUp() {
        gameManager = new GameManager();
    }
    
    @AfterEach
    void tearDown() {
        if (gameManager != null && gameManager.isGameRunning()) {
            gameManager.endGame(false);
        }
    }
    
    @Test
    void testInitialState() {
        assertEquals(GameState.READY, gameManager.getState());
        assertEquals(0, gameManager.getCurrentScore());
        assertEquals(0, gameManager.getLinesCleared());
        assertFalse(gameManager.isGameRunning());
        assertNotNull(gameManager.getGameTimer());
        assertNotNull(gameManager.getScoreBoard());
    }
    
    @Test
    void testStartGame() {
        gameManager.startGame();
        assertEquals(GameState.PLAYING, gameManager.getState());
        assertTrue(gameManager.isGameRunning());
        assertEquals(0, gameManager.getCurrentScore());
        assertEquals(0, gameManager.getLinesCleared());
    }
    
    @Test
    void testPauseResume() {
        gameManager.startGame();
        assertEquals(GameState.PLAYING, gameManager.getState());
        
        gameManager.togglePause();
        assertEquals(GameState.PAUSED, gameManager.getState());
        assertTrue(gameManager.isGameRunning()); // Paused is still "running"
        
        gameManager.togglePause();
        assertEquals(GameState.PLAYING, gameManager.getState());
        assertTrue(gameManager.isGameRunning());
    }
    
    @Test
    void testEndGame() {
        gameManager.startGame();
        assertTrue(gameManager.isGameRunning());
        
        gameManager.endGame(false);
        assertEquals(GameState.GAME_OVER, gameManager.getState());
        assertFalse(gameManager.isGameRunning());
    }
    
    @Test
    void testScoring() {
        gameManager.addScore(100);
        assertEquals(100, gameManager.getCurrentScore());
        
        gameManager.addScore(50);
        assertEquals(150, gameManager.getCurrentScore());
    }
    
    @Test
    void testLinesClearedScoring() {
        // Test single line clear
        gameManager.linesCleared(1);
        assertEquals(1, gameManager.getLinesCleared());
        assertTrue(gameManager.getCurrentScore() > 0);
        
        int scoreAfterOne = gameManager.getCurrentScore();
        
        // Test tetris (4 lines)
        gameManager.linesCleared(4);
        assertEquals(5, gameManager.getLinesCleared());
        assertTrue(gameManager.getCurrentScore() > scoreAfterOne);
    }
    
    @Test
    void testDifficultyProgression() {
        assertEquals(0, gameManager.getDifficultyLevel());
        
        // Clear 10 lines to increase difficulty
        gameManager.linesCleared(10);
        assertEquals(1, gameManager.getDifficultyLevel());
        
        // Clear 10 more lines
        gameManager.linesCleared(10);
        assertEquals(2, gameManager.getDifficultyLevel());
    }
    
    @Test
    void testSpeedProgression() {
        gameManager.startGame(); // Start game to initialize lastDifficultyLevel
        
        Timer timer = gameManager.getGameTimer();
        double initialSpeed = timer.getSpeedFactor();
        
        // Test speed factor increases progressively
        gameManager.linesCleared(10); // First difficulty increase
        assertTrue(timer.getSpeedFactor() > initialSpeed, 
                   "Speed factor should increase after 10 lines. Initial: " + initialSpeed + ", Current: " + timer.getSpeedFactor());
        
        double speedAfter10 = timer.getSpeedFactor();
        gameManager.linesCleared(10); // Second difficulty increase (total 20 lines)
        assertTrue(timer.getSpeedFactor() > speedAfter10,
                   "Speed factor should increase after 20 lines. After 10: " + speedAfter10 + ", Current: " + timer.getSpeedFactor());
        
        // Test that speed level eventually increases (need speedFactor >= 2.0)
        // Clear lines until speed level increases 
        for (int i = 0; i < 8; i++) { // Clear 8 more sets of 10 (total 100 lines)
            gameManager.linesCleared(10);
        }
        
        assertTrue(timer.getSpeedFactor() >= 2.0,
                   "Speed factor should be >= 2.0 after 100 lines. Current: " + timer.getSpeedFactor());
        assertTrue(timer.getSpeedLevel() >= 2,
                   "Speed level should be >= 2 after 100 lines. Current: " + timer.getSpeedLevel());
    }
    
    @Test
    void testTimerIntegration() {
        Timer timer = gameManager.getGameTimer();
        
        // Test timer tick
        timer.tick(1.0);
        assertEquals(1.0, timer.getElapsedTime(), 0.001);
        
        // Test timer reset
        timer.reset();
        assertEquals(0.0, timer.getElapsedTime(), 0.001);
    }
    
    @Test
    void testScoreBoardIntegration() {
        assertNotNull(gameManager.getScoreBoard());
        
        // Test game over adds score to scoreboard
        gameManager.startGame();
        gameManager.addScore(500);
        gameManager.endGame(true); // This should add score
        
        assertTrue(gameManager.getScoreBoard().getScores().size() > 0);
    }
    
    @Test
    void testGameStateTransitions() {
        // Test complete lifecycle
        assertEquals(GameState.READY, gameManager.getState());
        
        gameManager.startGame();
        assertEquals(GameState.PLAYING, gameManager.getState());
        
        gameManager.togglePause();
        assertEquals(GameState.PAUSED, gameManager.getState());
        
        gameManager.togglePause();
        assertEquals(GameState.PLAYING, gameManager.getState());
        
        gameManager.endGame(false);
        assertEquals(GameState.GAME_OVER, gameManager.getState());
    }
    
    @Test
    void testMultipleGames() {
        // First game
        gameManager.startGame();
        gameManager.addScore(100);
        gameManager.endGame(false);
        
        // Start new game - should reset
        gameManager.startGame();
        assertEquals(0, gameManager.getCurrentScore());
        assertEquals(GameState.PLAYING, gameManager.getState());
    }
    
    @Test
    void testLineScoring() {
        // Test standard Tetris scoring
        int speedLevel = gameManager.getSpeedLevel();
        
        // Single line
        gameManager.linesCleared(1);
        int expectedSingle = 100 * speedLevel;
        assertTrue(gameManager.getCurrentScore() >= expectedSingle);
        
        int scoreAfterSingle = gameManager.getCurrentScore();
        
        // Double line
        gameManager.linesCleared(2);
        int expectedDouble = 300 * speedLevel;
        assertTrue(gameManager.getCurrentScore() >= scoreAfterSingle + expectedDouble);
        
        int scoreAfterDouble = gameManager.getCurrentScore();
        
        // Triple line
        gameManager.linesCleared(3);
        int expectedTriple = 500 * speedLevel;
        assertTrue(gameManager.getCurrentScore() >= scoreAfterDouble + expectedTriple);
        
        int scoreAfterTriple = gameManager.getCurrentScore();
        
        // Tetris (4 lines)
        gameManager.linesCleared(4);
        int expectedTetris = 800 * speedLevel;
        assertTrue(gameManager.getCurrentScore() >= scoreAfterTriple + expectedTetris);
    }
    
    @Test
    void testGameLoopStateHandling() throws InterruptedException {
        // Start game and let it run briefly
        gameManager.startGame();
        Thread.sleep(50); // Let game loop run
        
        // Pause should stop updates
        gameManager.togglePause();
        assertEquals(GameState.PAUSED, gameManager.getState());
        
        // Resume should continue
        gameManager.togglePause();
        assertEquals(GameState.PLAYING, gameManager.getState());
        
        // End game should stop loop
        gameManager.endGame(false);
        assertEquals(GameState.GAME_OVER, gameManager.getState());
    }
}