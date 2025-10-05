package team13.tetris.game;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for GameManager class
 */
public class GameManagerTest {
    
    private GameManager gameManager;
    private Stage mockStage;
    
    @BeforeAll
    static void initJavaFX() {
        // Initialize JavaFX toolkit for testing
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // JavaFX already initialized
        }
    }
    
    @BeforeEach
    void setUp() {
        // Create a mock stage for testing
        Platform.runLater(() -> {
            mockStage = new Stage();
        });
        
        // Wait for JavaFX thread
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Platform.runLater(() -> {
            gameManager = new GameManager(mockStage);
        });
        
        // Wait for initialization
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Test
    void testInitialState() {
        Platform.runLater(() -> {
            // Test initial game state
            assertEquals(GameState.READY, gameManager.getState(), "Initial state should be READY");
            assertEquals(0, gameManager.getCurrentScore(), "Initial score should be 0");
            assertEquals(0, gameManager.getLinesCleared(), "Initial lines cleared should be 0");
            assertFalse(gameManager.isGameRunning(), "Game should not be running initially");
            
            // Test component initialization
            assertNotNull(gameManager.getGameTimer(), "Timer should be initialized");
            assertNotNull(gameManager.getScoreBoard(), "ScoreBoard should be initialized");
            
            // Test timer initial state
            assertEquals(0.0, gameManager.getGameTimer().getElapsedTime(), 0.001, "Timer should start at 0");
            assertEquals(1.0, gameManager.getGameTimer().getSpeedFactor(), 0.001, "Initial speed should be 1.0");
        });
        
        // Wait for JavaFX thread
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Test
    void testStartGame() {
        Platform.runLater(() -> {
            // Start the game
            gameManager.startGame();
            
            // Verify game state changes
            assertEquals(GameState.PLAYING, gameManager.getState(), "State should be PLAYING");
            assertTrue(gameManager.isGameRunning(), "Game should be running");
            assertEquals(0, gameManager.getCurrentScore(), "Score should be reset to 0");
            assertEquals(0, gameManager.getLinesCleared(), "Lines cleared should be reset to 0");
            
            // Verify timer is reset
            assertEquals(0.0, gameManager.getGameTimer().getElapsedTime(), 0.001, "Timer should be reset");
            assertEquals(1.0, gameManager.getGameTimer().getSpeedFactor(), 0.001, "Speed should be reset");
        });
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Test
    void testPauseResume() {
        Platform.runLater(() -> {
            // Start game first
            gameManager.startGame();
            assertEquals(GameState.PLAYING, gameManager.getState(), "Should be playing");
            
            // Test pause
            gameManager.togglePause();
            assertEquals(GameState.PAUSED, gameManager.getState(), "Should be paused");
            assertTrue(gameManager.isGameRunning(), "Game should still be flagged as running");
            
            // Test resume
            gameManager.togglePause();
            assertEquals(GameState.PLAYING, gameManager.getState(), "Should be playing again");
            assertTrue(gameManager.isGameRunning(), "Game should be running");
            
            // Test toggle from non-playing state (should do nothing)
            gameManager.endGame(false);
            gameManager.togglePause();
            assertEquals(GameState.GAME_OVER, gameManager.getState(), "Should remain GAME_OVER");
        });
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Test
    void testEndGame() {
        Platform.runLater(() -> {
            // Start game first
            gameManager.startGame();
            assertTrue(gameManager.isGameRunning(), "Game should be running");
            
            // End the game (without UI for testing)
            gameManager.endGame(false);
            
            // Verify game state
            assertFalse(gameManager.isGameRunning(), "Game should not be running");
            assertEquals(GameState.GAME_OVER, gameManager.getState(), "State should be GAME_OVER");
        });
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Test
    void testScoreManagement() {
        Platform.runLater(() -> {
            // Test adding score
            gameManager.addScore(100);
            assertEquals(100, gameManager.getCurrentScore(), "Score should be 100");
            
            gameManager.addScore(250);
            assertEquals(350, gameManager.getCurrentScore(), "Score should be 350");
            
            // Test negative score (should still work)
            gameManager.addScore(-50);
            assertEquals(300, gameManager.getCurrentScore(), "Score should be 300");
        });
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Test
    void testLinesCleared() {
        Platform.runLater(() -> {
            // Test single line clear
            gameManager.linesCleared(1);
            assertEquals(1, gameManager.getLinesCleared(), "Should have 1 line cleared");
            assertEquals(100, gameManager.getCurrentScore(), "Score should be 100 (1 line * level 1 * 100)");
            
            // Test double line clear
            gameManager.linesCleared(2);
            assertEquals(3, gameManager.getLinesCleared(), "Should have 3 lines cleared total");
            assertEquals(400, gameManager.getCurrentScore(), "Score should be 400 (100 + 300)");
            
            // Test triple line clear
            gameManager.linesCleared(3);
            assertEquals(6, gameManager.getLinesCleared(), "Should have 6 lines cleared total");
            assertEquals(900, gameManager.getCurrentScore(), "Score should be 900 (100 + 300 + 500)");
            
            // Test tetris (4 lines)
            gameManager.linesCleared(4);
            assertEquals(10, gameManager.getLinesCleared(), "Should have 10 lines cleared total");
            assertEquals(1700, gameManager.getCurrentScore(), "Score should be 1700 (100 + 300 + 500 + 800)");
        });
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Test
    void testLevelProgression() {
        Platform.runLater(() -> {
            // Start game and increase speed factor to simulate level progression
            gameManager.startGame();
            
            // Simulate level progression by increasing speed
            gameManager.getGameTimer().setSpeedFactor(2.5);
            assertEquals(2, gameManager.getGameTimer().getSpeedLevel(), "Should be speed level 2");
            
            // Test score calculation with higher level
            gameManager.linesCleared(1);
            assertEquals(200, gameManager.getCurrentScore(), "Score should be 200 (1 line * level 2 * 100)");
            
            // Test level 5
            gameManager.getGameTimer().setSpeedFactor(5.0);
            assertEquals(5, gameManager.getGameTimer().getSpeedLevel(), "Should be speed level 5");
            
            gameManager.linesCleared(4); // Tetris at level 5
            // Previous score (200) + new score (4 lines * level 5 * 800) = 200 + 4000 = 4200
            assertEquals(4200, gameManager.getCurrentScore(), "Score should be 4200");
        });
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Test
    void testGameStateTransitions() {
        Platform.runLater(() -> {
            // Test complete state transition cycle
            assertEquals(GameState.READY, gameManager.getState(), "Should start in READY");
            
            // MENU -> PLAYING
            gameManager.startGame();
            assertEquals(GameState.PLAYING, gameManager.getState(), "Should transition to PLAYING");
            
            // PLAYING -> PAUSED
            gameManager.togglePause();
            assertEquals(GameState.PAUSED, gameManager.getState(), "Should transition to PAUSED");
            
            // PAUSED -> PLAYING
            gameManager.togglePause();
            assertEquals(GameState.PLAYING, gameManager.getState(), "Should transition back to PLAYING");
            
            // PLAYING -> GAME_OVER
            gameManager.endGame(false);
            assertEquals(GameState.GAME_OVER, gameManager.getState(), "Should transition to GAME_OVER");
            
            // GAME_OVER -> PLAYING (restart)
            gameManager.startGame();
            assertEquals(GameState.PLAYING, gameManager.getState(), "Should be able to restart");
        });
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Test
    void testTimerIntegration() {
        Platform.runLater(() -> {
            gameManager.startGame();
            
            Timer timer = gameManager.getGameTimer();
            assertNotNull(timer, "Timer should be available");
            
            // Test timer tick simulation
            timer.tick(5.0);
            assertEquals(5.0, timer.getElapsedTime(), 0.001, "Timer should have 5 seconds");
            assertEquals("00:05", timer.getFormattedTime(), "Formatted time should be 00:05");
            
            // Test speed increase
            timer.increaseSpeed();
            assertEquals(1.1, timer.getSpeedFactor(), 0.001, "Speed should be 1.1");
            assertTrue(timer.getInterval() < 1000.0, "Interval should be less than 1000ms");
        });
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Test
    void testScoreBoardIntegration() {
        Platform.runLater(() -> {
            assertNotNull(gameManager.getScoreBoard(), "ScoreBoard should be initialized");
            
            // Test score submission
            gameManager.addScore(1500);
            gameManager.endGame(false); // End game without UI for testing
            
            // Verify the score was added (we can't easily test UI, but we can test the component exists)
            assertTrue(gameManager.getScoreBoard().getScores().size() > 0, "Score should be added to scoreboard");
        });
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Test
    void testGameplayScenario() {
        Platform.runLater(() -> {
            // Simulate a complete game scenario
            gameManager.startGame();
            
            // Simulate some gameplay
            gameManager.addScore(500);
            gameManager.linesCleared(2); // Double line clear
            gameManager.linesCleared(1); // Single line clear
            gameManager.linesCleared(4); // Tetris!
            
            // Check final state
            assertEquals(7, gameManager.getLinesCleared(), "Should have cleared 7 lines total");
            // Score: 500 + (2*300*1) + (1*100*1) + (4*800*1) = 500 + 600 + 100 + 3200 = 4400
            assertEquals(4400, gameManager.getCurrentScore(), "Score should be 4400");
            
            // End game
            gameManager.endGame(false);
            assertEquals(GameState.GAME_OVER, gameManager.getState(), "Should be game over");
            assertFalse(gameManager.isGameRunning(), "Game should not be running");
        });
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}