package team13.tetris.game.controller;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import team13.tetris.config.Settings;
import team13.tetris.game.logic.GameEngine;
import team13.tetris.game.model.Board;

class VersusGameControllerNonUITest {

    @Test
    void testVersusGameControllerReflectionMethods() throws Exception {
        // Create minimal controller without UI dependencies
        TestVersusGameController controller = new TestVersusGameController();
        
        // Test shouldProcessKey method via reflection
        java.lang.reflect.Method shouldProcessKeyMethod = VersusGameController.class
            .getDeclaredMethod("shouldProcessKey", java.util.Map.class, Object.class, long.class);
        shouldProcessKeyMethod.setAccessible(true);
        
        java.util.Map<String, Long> keyMap = new java.util.HashMap<>();
        long currentTime = System.currentTimeMillis();
        
        // Test key not pressed
        Boolean result = (Boolean) shouldProcessKeyMethod.invoke(controller, keyMap, "LEFT", currentTime);
        assertFalse(result);
        
        // Test key just pressed (within initial delay)
        keyMap.put("LEFT", currentTime - 100);
        result = (Boolean) shouldProcessKeyMethod.invoke(controller, keyMap, "LEFT", currentTime);
        assertFalse(result);
        
        // Test key pressed long enough (after initial delay)
        keyMap.put("LEFT", currentTime - 600);
        result = (Boolean) shouldProcessKeyMethod.invoke(controller, keyMap, "LEFT", currentTime);
        assertTrue(result);
    }

    @Test
    void testCreateAttackPatternMethod() throws Exception {
        TestVersusGameController controller = new TestVersusGameController();
        TestGameEngine engine = new TestGameEngine();
        
        // Test createAttackPattern method via reflection
        java.lang.reflect.Method method = VersusGameController.class
            .getDeclaredMethod("createAttackPattern", int.class, GameEngine.class);
        method.setAccessible(true);
        
        int[][] pattern = (int[][]) method.invoke(controller, 2, engine);
        
        assertNotNull(pattern);
        assertEquals(2, pattern.length);
        assertEquals(10, pattern[0].length);
        
        // Each row should have exactly one hole (0) and rest should be grey blocks (1000)
        for (int r = 0; r < 2; r++) {
            int holes = 0;
            int greyBlocks = 0;
            for (int c = 0; c < 10; c++) {
                if (pattern[r][c] == 0) holes++;
                else if (pattern[r][c] == 1000) greyBlocks++;
            }
            assertEquals(1, holes, "Each row should have exactly one hole");
            assertEquals(9, greyBlocks, "Each row should have 9 grey blocks");
        }
    }

    @Test
    void testAddIncomingBlockToBoardMethod() throws Exception {
        TestVersusGameController controller = new TestVersusGameController();
        TestGameEngine engine = new TestGameEngine();
        
        // Test addIncomingBlockToBoard method via reflection
        java.lang.reflect.Method method = VersusGameController.class
            .getDeclaredMethod("addIncomingBlockToBoard", GameEngine.class, int[][].class);
        method.setAccessible(true);
        
        int[][] pattern = {{1000, 0, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000}};
        
        // This should not throw any exceptions
        assertDoesNotThrow(() -> {
            try {
                method.invoke(controller, engine, pattern);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testUpdateSharedSpeedMethod() throws Exception {
        TestVersusGameController controller = new TestVersusGameController();
        
        // Test updateSharedSpeed method via reflection
        java.lang.reflect.Method method = VersusGameController.class
            .getDeclaredMethod("updateSharedSpeed", int.class);
        method.setAccessible(true);
        
        // This should not throw any exceptions
        assertDoesNotThrow(() -> {
            try {
                method.invoke(controller, 5);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testProcessInputsMethod() throws Exception {
        TestVersusGameController controller = new TestVersusGameController();
        
        // Test processInputs method via reflection
        java.lang.reflect.Method method = VersusGameController.class
            .getDeclaredMethod("processInputs");
        method.setAccessible(true);
        
        // This should not throw any exceptions
        assertDoesNotThrow(() -> {
            try {
                method.invoke(controller);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Minimal test implementations without UI dependencies
    static class TestVersusGameController extends VersusGameController {
        public TestVersusGameController() {
            super(null, null, new TestSettings(), 
                  new TestGameEngine(), new TestGameEngine(), false, false);
        }
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
        
        @Override
        public String getKeyLeftP2() { return "A"; }
        
        @Override
        public String getKeyRightP2() { return "D"; }
        
        @Override
        public String getKeyDownP2() { return "S"; }
        
        @Override
        public String getKeyRotateP2() { return "W"; }
        
        @Override
        public String getKeyDropP2() { return "Q"; }
    }
    
    static class TestGameEngine extends GameEngine {
        private Board board = new Board(10, 20);
        
        public TestGameEngine() {
            super(new Board(10, 20), null);
        }
        
        @Override
        public Board getBoard() { return board; }
        
        @Override
        public int getScore() { return 1000; }
        
        @Override
        public void moveLeft() {}
        
        @Override
        public void moveRight() {}
        
        @Override
        public boolean softDrop() { return false; }
        
        @Override
        public void rotateCW() {}
        
        @Override
        public void hardDrop() {}
        
        @Override
        public void startAutoDrop() {}
        
        @Override
        public void stopAutoDrop() {}
        
        @Override
        public boolean isLastClearByGravityOrSplit() { return true; }
        
        @Override
        public java.util.List<int[]> getLastLockedCells() {
            return java.util.Arrays.asList(new int[]{3, 19}, new int[]{4, 19});
        }
        
        @Override 
        public java.util.List<Integer> getClearedLineIndices() {
            return java.util.Arrays.asList(19);
        }
    }
}