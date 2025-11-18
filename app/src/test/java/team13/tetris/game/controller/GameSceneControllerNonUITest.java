package team13.tetris.game.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import team13.tetris.SceneManager;
import team13.tetris.config.Settings;
import team13.tetris.game.logic.GameEngine;
import team13.tetris.game.model.Board;
import team13.tetris.input.KeyInputHandler;

class GameSceneControllerNonUITest {

    @Test
    @Disabled("JavaFX Toolkit initialization issues in headless environment")
    void testGameSceneControllerReflectionBasedTest() {
        // Test GameSceneController methods using reflection to avoid UI dependencies
        try {
            // Create a controller with null gameScene to test non-UI methods only
            GameSceneController controller = new GameSceneController(
                null, // gameScene
                new TestSceneManager(), 
                new TestSettings(), 
                new TestKeyInputHandler()
            );
            
            // Test setEngine method
            TestGameEngine testEngine = new TestGameEngine();
            controller.setEngine(testEngine);
            
            // Use reflection to verify engine was set
            Field engineField = GameSceneController.class.getDeclaredField("engine");
            engineField.setAccessible(true);
            GameEngine retrievedEngine = (GameEngine) engineField.get(controller);
            assertNotNull(retrievedEngine);
            
            // Test pause/resume state using reflection
            Field pausedField = GameSceneController.class.getDeclaredField("paused");
            pausedField.setAccessible(true);
            
            // Test pause
            controller.pause();
            boolean isPaused = (Boolean) pausedField.get(controller);
            assertTrue(isPaused);
            
            // Test resume
            controller.resume();
            isPaused = (Boolean) pausedField.get(controller);
            assertFalse(isPaused);
            
            // Test game over state
            Field gameOverField = GameSceneController.class.getDeclaredField("gameOver");
            gameOverField.setAccessible(true);
            
            // Initially should not be game over
            boolean isGameOver = (Boolean) gameOverField.get(controller);
            assertFalse(isGameOver);
            
        } catch (Exception e) {
            fail("Reflection-based test failed: " + e.getMessage());
        }
    }

    @Test
    @Disabled("JavaFX Toolkit initialization issues in headless environment")
    void testGameSceneControllerInputCallbacks() {
        // Test input callback methods
        GameSceneController controller = new GameSceneController(
            null, // gameScene
            new TestSceneManager(), 
            new TestSettings(), 
            new TestKeyInputHandler()
        );
        
        TestGameEngine testEngine = new TestGameEngine();
        controller.setEngine(testEngine);
        
        // Test input callbacks - these should work without UI
        assertDoesNotThrow(() -> controller.onLeftPressed());
        assertDoesNotThrow(() -> controller.onRightPressed());
        assertDoesNotThrow(() -> controller.onRotatePressed());
        assertDoesNotThrow(() -> controller.onDropPressed());
        assertDoesNotThrow(() -> controller.onHardDropPressed());
        assertDoesNotThrow(() -> controller.onPausePressed());
        
        // Verify engine methods were called
        assertTrue(testEngine.moveLeftCalled);
        assertTrue(testEngine.moveRightCalled);
        assertTrue(testEngine.rotateCWCalled);
        assertTrue(testEngine.softDropCalled);
        assertTrue(testEngine.hardDropCalled);
    }

    @Test
    @Disabled("JavaFX Toolkit initialization issues in headless environment")
    void testGameSceneControllerLinesCleared() {
        // Test lines cleared tracking
        try {
            GameSceneController controller = new GameSceneController(
                null, // gameScene
                new TestSceneManager(), 
                new TestSettings(), 
                new TestKeyInputHandler()
            );
            
            TestGameEngine testEngine = new TestGameEngine();
            controller.setEngine(testEngine);
            
            // Use reflection to access totalLinesCleared field
            Field linesClearedField = GameSceneController.class.getDeclaredField("totalLinesCleared");
            linesClearedField.setAccessible(true);
            
            // Initially should be 0
            int initialLines = (Integer) linesClearedField.get(controller);
            assertEquals(0, initialLines);
            
            // Simulate lines cleared event
            controller.onLinesCleared(3);
            
            // Should have updated totalLinesCleared
            int updatedLines = (Integer) linesClearedField.get(controller);
            assertEquals(3, updatedLines);
            
            // Clear more lines
            controller.onLinesCleared(2);
            updatedLines = (Integer) linesClearedField.get(controller);
            assertEquals(5, updatedLines);
            
        } catch (Exception e) {
            fail("Lines cleared test failed: " + e.getMessage());
        }
    }
    
    static class TestSceneManager extends SceneManager {
        public TestSceneManager() {
            super(null);
        }
        
        @Override
        public void showConfirmScene(Settings settings, String message, 
                                   Runnable onConfirm, Runnable onCancel) {}
        
        @Override
        public void showMainMenu(Settings settings) {}
        
        @Override
        public void restorePreviousScene() {}
        
        @Override
        public void exitWithSave(Settings settings) {}
    }
    
    static class TestSettings extends Settings {
        @Override
        public String getKeyLeft() { return "LEFT"; }
        
        @Override
        public String getKeyRight() { return "RIGHT"; }
        
        @Override
        public String getKeyDown() { return "DOWN"; }
        
        @Override
        public String getKeyRotate() { return "UP"; }
        
        @Override
        public String getKeyDrop() { return "SPACE"; }
        
        @Override
        public String getPause() { return "P"; }
    }
    
    static class TestKeyInputHandler extends KeyInputHandler {
        public TestKeyInputHandler() {
            super(new TestSettings());
        }
        
        @Override
        public void attachToScene(javafx.scene.Scene scene, 
                                KeyInputHandler.KeyInputCallback callback) {}
    }
    
    static class TestGameEngine extends GameEngine {
        public boolean moveLeftCalled = false;
        public boolean moveRightCalled = false;
        public boolean rotateCWCalled = false;
        public boolean softDropCalled = false;
        public boolean hardDropCalled = false;
        
        public TestGameEngine() {
            super(new Board(10, 20), null);
        }
        
        @Override
        public void moveLeft() {
            moveLeftCalled = true;
        }
        
        @Override
        public void moveRight() {
            moveRightCalled = true;
        }
        
        @Override
        public boolean softDrop() {
            softDropCalled = true;
            return false;
        }
        
        @Override
        public void rotateCW() {
            rotateCWCalled = true;
        }
        
        @Override
        public void hardDrop() {
            hardDropCalled = true;
        }
        
        @Override
        public void startAutoDrop() {}
        
        @Override
        public void stopAutoDrop() {}
        
        @Override
        public void updateSpeedForLinesCleared(int lines, int totalLines) {}
    }
}