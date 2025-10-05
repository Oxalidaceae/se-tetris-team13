package team13.tetris.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for Timer class
 */
public class TimerTest {
    
    private Timer timer;
    
    @BeforeEach
    void setUp() {
        timer = new Timer();
    }
    
    @Test
    void testInitialValues() {
        assertEquals(0.0, timer.getElapsedTime(), 0.001, "Initial elapsed time should be 0");
        assertEquals(1.0, timer.getSpeedFactor(), 0.001, "Initial speed factor should be 1.0");
        assertEquals(1000.0, timer.getInterval(), 0.001, "Initial interval should be 1000ms");
        assertEquals(1, timer.getCurrentLevel(), "Initial level should be 1");
        assertEquals("00:00", timer.getFormattedTime(), "Initial formatted time should be 00:00");
    }
    
    @Test
    void testTick() {
        timer.tick(1.5);
        assertEquals(1.5, timer.getElapsedTime(), 0.001, "Elapsed time should be 1.5 after tick(1.5)");
        
        timer.tick(2.3);
        assertEquals(3.8, timer.getElapsedTime(), 0.001, "Elapsed time should accumulate");
        
        timer.tick(0.2);
        assertEquals(4.0, timer.getElapsedTime(), 0.001, "Elapsed time should be 4.0");
    }
    
    @Test
    void testIncreaseSpeed() {
        // Test single increase
        timer.increaseSpeed();
        assertEquals(1.1, timer.getSpeedFactor(), 0.001, "Speed should increase by 0.1");
        assertEquals(1000.0 / 1.1, timer.getInterval(), 0.001, "Interval should decrease");
        assertEquals(1, timer.getCurrentLevel(), "Level should still be 1");
        
        // Test multiple increases
        for (int i = 0; i < 10; i++) {
            timer.increaseSpeed();
        }
        assertEquals(2.1, timer.getSpeedFactor(), 0.001, "Speed should be 2.1 after 11 increases");
        assertEquals(2, timer.getCurrentLevel(), "Level should be 2");
    }
    
    @Test
    void testMaxSpeedLimit() {
        // Set to near max speed
        timer.setSpeedFactor(9.9);
        timer.increaseSpeed();
        assertEquals(10.0, timer.getSpeedFactor(), 0.001, "Speed should reach max 10.0");
        
        // Try to exceed max
        timer.increaseSpeed();
        assertEquals(10.0, timer.getSpeedFactor(), 0.001, "Speed should not exceed max 10.0");
        assertEquals(100.0, timer.getInterval(), 0.001, "Min interval should be 100ms");
        assertEquals(10, timer.getCurrentLevel(), "Max level should be 10");
    }
    
    @Test
    void testSetSpeedFactor() {
        // Valid speed factor
        timer.setSpeedFactor(5.0);
        assertEquals(5.0, timer.getSpeedFactor(), 0.001, "Speed should be set to 5.0");
        assertEquals(200.0, timer.getInterval(), 0.001, "Interval should be 200ms");
        assertEquals(5, timer.getCurrentLevel(), "Level should be 5");
        
        // Invalid speed factors
        timer.setSpeedFactor(0.0);
        assertEquals(5.0, timer.getSpeedFactor(), 0.001, "Speed should not change for 0.0");
        
        timer.setSpeedFactor(-1.0);
        assertEquals(5.0, timer.getSpeedFactor(), 0.001, "Speed should not change for negative");
        
        timer.setSpeedFactor(15.0);
        assertEquals(5.0, timer.getSpeedFactor(), 0.001, "Speed should not change for > max");
    }
    
    @Test
    void testGetInterval() {
        // Test various speed factors
        timer.setSpeedFactor(1.0);
        assertEquals(1000.0, timer.getInterval(), 0.001, "Interval at 1x speed");
        
        timer.setSpeedFactor(2.0);
        assertEquals(500.0, timer.getInterval(), 0.001, "Interval at 2x speed");
        
        timer.setSpeedFactor(4.0);
        assertEquals(250.0, timer.getInterval(), 0.001, "Interval at 4x speed");
        
        timer.setSpeedFactor(10.0);
        assertEquals(100.0, timer.getInterval(), 0.001, "Interval at max speed");
    }
    
    @Test
    void testFormattedTime() {
        // Test various time formats
        timer.tick(0);
        assertEquals("00:00", timer.getFormattedTime(), "0 seconds");
        
        timer.tick(30);
        assertEquals("00:30", timer.getFormattedTime(), "30 seconds");
        
        timer.tick(35);
        assertEquals("01:05", timer.getFormattedTime(), "65 seconds (1:05)");
        
        timer.tick(3595);
        assertEquals("61:00", timer.getFormattedTime(), "3660 seconds (61:00)");
    }
    
    @Test
    void testGetCurrentLevel() {
        assertEquals(1, timer.getCurrentLevel(), "Level 1 at speed 1.0");
        
        timer.setSpeedFactor(1.9);
        assertEquals(1, timer.getCurrentLevel(), "Level 1 at speed 1.9");
        
        timer.setSpeedFactor(2.0);
        assertEquals(2, timer.getCurrentLevel(), "Level 2 at speed 2.0");
        
        timer.setSpeedFactor(2.9);
        assertEquals(2, timer.getCurrentLevel(), "Level 2 at speed 2.9");
        
        timer.setSpeedFactor(5.7);
        assertEquals(5, timer.getCurrentLevel(), "Level 5 at speed 5.7");
        
        timer.setSpeedFactor(10.0);
        assertEquals(10, timer.getCurrentLevel(), "Level 10 at max speed");
    }
    
    @Test
    void testReset() {
        // Change timer state
        timer.tick(120.5);
        timer.setSpeedFactor(7.3);
        
        // Verify changed state
        assertEquals(120.5, timer.getElapsedTime(), 0.001, "Time should be changed");
        assertEquals(7.3, timer.getSpeedFactor(), 0.001, "Speed should be changed");
        
        // Reset and verify
        timer.reset();
        assertEquals(0.0, timer.getElapsedTime(), 0.001, "Time should be reset to 0");
        assertEquals(1.0, timer.getSpeedFactor(), 0.001, "Speed should be reset to 1.0");
        assertEquals("00:00", timer.getFormattedTime(), "Formatted time should be reset");
        assertEquals(1, timer.getCurrentLevel(), "Level should be reset to 1");
    }
    
    @Test
    void testGameplayScenario() {
        // Simulate a game scenario
        Timer gameTimer = new Timer();
        int speedIncreases = 0;
        
        // Play for 2 minutes with speed increases
        for (int minute = 0; minute < 2; minute++) {
            for (int second = 0; second < 60; second++) {
                gameTimer.tick(1.0); // 1 second tick
                
                // Increase speed every 10 seconds (at 10, 20, 30, 40, 50 for each minute)
                if (second % 10 == 0 && second > 0) {
                    double oldSpeed = gameTimer.getSpeedFactor();
                    gameTimer.increaseSpeed();
                    if (gameTimer.getSpeedFactor() > oldSpeed) {
                        speedIncreases++;
                    }
                }
            }
        }
        
        assertEquals(120.0, gameTimer.getElapsedTime(), 0.001, "2 minutes elapsed");
        assertEquals("02:00", gameTimer.getFormattedTime(), "Formatted as 02:00");
        
        // Should have attempted 10 speed increases (5 per minute)
        // But actual increases depend on whether max speed was reached
        double expectedSpeed = Math.min(10.0, 1.0 + (speedIncreases * 0.1));
        assertEquals(expectedSpeed, gameTimer.getSpeedFactor(), 0.01, 
                    "Speed should be " + expectedSpeed + " after " + speedIncreases + " increases");
        
        assertTrue(gameTimer.getCurrentLevel() >= 1, "Should be at least level 1");
        assertTrue(gameTimer.getInterval() <= 1000.0, "Interval should be faster than or equal to initial");
    }
    
    @Test
    void testPrecisionAndEdgeCases() {
        // Test very small time increments
        for (int i = 0; i < 1000; i++) {
            timer.tick(0.001); // 1ms increments
        }
        assertEquals(1.0, timer.getElapsedTime(), 0.001, "1000 * 0.001 should equal 1.0");
        
        // Test very large time jump
        timer.reset();
        timer.tick(999999.0);
        assertTrue(timer.getElapsedTime() > 999998.0, "Should handle large time values");
        
        // Test speed factor precision
        timer.setSpeedFactor(3.14159);
        assertEquals(3.14159, timer.getSpeedFactor(), 0.00001, "Should maintain precision");
        assertEquals(1000.0 / 3.14159, timer.getInterval(), 0.001, "Interval calculation precision");
    }
}