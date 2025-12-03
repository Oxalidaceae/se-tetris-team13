package team13.tetris.game;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Timer 클래스 테스트: Tests time tracking, speed adjustments, formatted time output
@DisplayName("Timer 테스트")
public class TimerTest {

    private Timer timer;

    @BeforeEach
    void setUp() {
        timer = new Timer();
    }

    @Test
    @DisplayName("초기값이 올바르게 설정되는지 확인")
    void testInitialValues() {
        assertEquals(0.0, timer.getElapsedTime(), 0.001, "Initial elapsed time should be 0");
        assertEquals(1.0, timer.getSpeedFactor(), 0.001, "Initial speed factor should be 1.0");
        assertEquals(1000.0, timer.getInterval(), 0.001, "Initial interval should be 1000ms");
        assertEquals(1, timer.getSpeedLevel(), "Initial speed level should be 1");
        assertEquals("00:00", timer.getFormattedTime(), "Initial formatted time should be 00:00");
    }

    @Test
    @DisplayName("tick 메서드가 시간을 올바르게 누적하는지 확인")
    void testTick() {
        timer.tick(1.5);
        assertEquals(
                1.5, timer.getElapsedTime(), 0.001, "Elapsed time should be 1.5 after tick(1.5)");

        timer.tick(2.3);
        assertEquals(3.8, timer.getElapsedTime(), 0.001, "Elapsed time should accumulate");

        timer.tick(0.2);
        assertEquals(4.0, timer.getElapsedTime(), 0.001, "Elapsed time should be 4.0");
    }

    @Test
    @DisplayName("속도 증가가 올바르게 동작하는지 확인")
    void testIncreaseSpeed() {
        // Test single increase
        timer.increaseSpeed();
        assertEquals(1.1, timer.getSpeedFactor(), 0.001, "Speed should increase by 0.1");
        assertEquals(1000.0 / 1.1, timer.getInterval(), 0.001, "Interval should decrease");
        assertEquals(1, timer.getSpeedLevel(), "Speed level should still be 1");

        // Test multiple increases
        for (int i = 0; i < 10; i++) {
            timer.increaseSpeed();
        }
        assertEquals(2.1, timer.getSpeedFactor(), 0.001, "Speed should be 2.1 after 11 increases");
        assertEquals(2, timer.getSpeedLevel(), "Speed level should be 2");
    }

    @Test
    @DisplayName("최대 속도 제한이 올바르게 동작하는지 확인")
    void testMaxSpeedLimit() {
        // Set to near max speed
        timer.setSpeedFactor(9.9);
        timer.increaseSpeed();
        assertEquals(10.0, timer.getSpeedFactor(), 0.001, "Speed should reach max 10.0");

        // Try to exceed max
        timer.increaseSpeed();
        assertEquals(10.0, timer.getSpeedFactor(), 0.001, "Speed should not exceed max 10.0");
        assertEquals(100.0, timer.getInterval(), 0.001, "Min interval should be 100ms");
        assertEquals(10, timer.getSpeedLevel(), "Max speed level should be 10");
    }

    @Test
    @DisplayName("setSpeedFactor가 유효성 검증을 올바르게 수행하는지 확인")
    void testSetSpeedFactor() {
        // Valid speed factor
        timer.setSpeedFactor(5.0);
        assertEquals(5.0, timer.getSpeedFactor(), 0.001, "Speed should be set to 5.0");
        assertEquals(200.0, timer.getInterval(), 0.001, "Interval should be 200ms");
        assertEquals(5, timer.getSpeedLevel(), "Speed level should be 5");

        // Invalid speed factors
        timer.setSpeedFactor(0.0);
        assertEquals(5.0, timer.getSpeedFactor(), 0.001, "Speed should not change for 0.0");

        timer.setSpeedFactor(-1.0);
        assertEquals(5.0, timer.getSpeedFactor(), 0.001, "Speed should not change for negative");

        timer.setSpeedFactor(15.0);
        assertEquals(5.0, timer.getSpeedFactor(), 0.001, "Speed should not change for > max");
    }

    @Test
    @DisplayName("getInterval이 속도에 따라 올바른 간격을 계산하는지 확인")
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
    @DisplayName("시간이 MM:SS 형식으로 올바르게 포맷되는지 확인")
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
    @DisplayName("속도 레벨이 speedFactor에 따라 올바르게 계산되는지 확인")
    void testGetSpeedLevel() {
        assertEquals(1, timer.getSpeedLevel(), "Speed level 1 at speed 1.0");

        timer.setSpeedFactor(1.9);
        assertEquals(1, timer.getSpeedLevel(), "Speed level 1 at speed 1.9");

        timer.setSpeedFactor(2.0);
        assertEquals(2, timer.getSpeedLevel(), "Speed level 2 at speed 2.0");

        timer.setSpeedFactor(2.9);
        assertEquals(2, timer.getSpeedLevel(), "Speed level 2 at speed 2.9");

        timer.setSpeedFactor(5.7);
        assertEquals(5, timer.getSpeedLevel(), "Speed level 5 at speed 5.7");

        timer.setSpeedFactor(10.0);
        assertEquals(10, timer.getSpeedLevel(), "Speed level 10 at max speed");
    }

    @Test
    @DisplayName("reset이 타이머를 초기 상태로 되돌리는지 확인")
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
        assertEquals(1, timer.getSpeedLevel(), "Speed level should be reset to 1");
    }

    @Test
    @DisplayName("게임 플레이 시나리오를 시뮬레이션하여 전체 동작을 확인")
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
        assertEquals(
                expectedSpeed,
                gameTimer.getSpeedFactor(),
                0.01,
                "Speed should be " + expectedSpeed + " after " + speedIncreases + " increases");

        assertTrue(gameTimer.getSpeedLevel() >= 1, "Should be at least speed level 1");
        assertTrue(
                gameTimer.getInterval() <= 1000.0,
                "Interval should be faster than or equal to initial");
    }

    @Test
    @DisplayName("정밀도와 엣지 케이스를 올바르게 처리하는지 확인")
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
        assertEquals(
                1000.0 / 3.14159, timer.getInterval(), 0.001, "Interval calculation precision");
    }

    // ============================================
    // 누락된 기능 테스트 추가
    // ============================================

    @Test
    @DisplayName("multiplier 파라미터를 사용한 속도 증가가 올바르게 동작하는지 확인")
    void testIncreaseSpeedWithMultiplier() {
        // Test with multiplier 0.5 (slower increase)
        timer.increaseSpeed(0.5);
        assertEquals(
                1.05, timer.getSpeedFactor(), 0.001, "Speed should increase by 0.1 * 0.5 = 0.05");

        // Test with multiplier 2.0 (faster increase)
        timer.reset();
        timer.increaseSpeed(2.0);
        assertEquals(
                1.2, timer.getSpeedFactor(), 0.001, "Speed should increase by 0.1 * 2.0 = 0.2");

        // Test with multiplier 0 (no increase)
        timer.reset();
        timer.increaseSpeed(0.0);
        assertEquals(
                1.0, timer.getSpeedFactor(), 0.001, "Speed should not increase with 0 multiplier");

        // Test negative multiplier (decrease)
        timer.reset();
        timer.increaseSpeed(-1.0);
        assertEquals(
                0.9,
                timer.getSpeedFactor(),
                0.001,
                "Speed should decrease with negative multiplier");
    }

    @Test
    @DisplayName("multiplier 사용 시에도 최대 속도 제한이 적용되는지 확인")
    void testIncreaseSpeedWithMultiplierMaxLimit() {
        // Set near max speed
        timer.setSpeedFactor(9.8);

        // Try to increase beyond max with multiplier
        timer.increaseSpeed(5.0); // Would increase by 0.5
        assertTrue(
                timer.getSpeedFactor() <= 10.0,
                "Speed should not exceed max even with large multiplier");
    }

    @Test
    @DisplayName("드롭 거리에 따른 점수 계산이 올바른지 확인")
    void testCalculateDropScore() {
        // Test at base speed (1.0x)
        assertEquals(10, timer.calculateDropScore(1), "1 cell drop at 1x speed = 10 points");
        assertEquals(50, timer.calculateDropScore(5), "5 cell drop at 1x speed = 50 points");
        assertEquals(0, timer.calculateDropScore(0), "0 cell drop = 0 points");

        // Test at higher speed (2.0x)
        timer.setSpeedFactor(2.0);
        assertEquals(20, timer.calculateDropScore(1), "1 cell drop at 2x speed = 20 points");
        assertEquals(100, timer.calculateDropScore(5), "5 cell drop at 2x speed = 100 points");

        // Test at max speed (10.0x)
        timer.setSpeedFactor(10.0);
        assertEquals(100, timer.calculateDropScore(1), "1 cell drop at 10x speed = 100 points");
        assertEquals(1000, timer.calculateDropScore(10), "10 cell drop at 10x speed = 1000 points");
    }

    @Test
    @DisplayName("소프트 드롭 점수가 속도에 따라 올바르게 계산되는지 확인")
    void testGetSoftDropScore() {
        // Test at base speed
        assertEquals(10, timer.getSoftDropScore(), "Soft drop at 1x speed = 10 points");

        // Test at 3x speed
        timer.setSpeedFactor(3.0);
        assertEquals(30, timer.getSoftDropScore(), "Soft drop at 3x speed = 30 points");

        // Test at max speed
        timer.setSpeedFactor(10.0);
        assertEquals(100, timer.getSoftDropScore(), "Soft drop at 10x speed = 100 points");
    }

    @Test
    @DisplayName("하드 드롭 점수가 거리와 속도에 따라 올바르게 계산되는지 확인")
    void testGetHardDropScore() {
        // Test various drop distances at base speed
        assertEquals(10, timer.getHardDropScore(1), "Hard drop 1 cell at 1x speed = 10 points");
        assertEquals(
                100, timer.getHardDropScore(10), "Hard drop 10 cells at 1x speed = 100 points");
        assertEquals(
                200, timer.getHardDropScore(20), "Hard drop 20 cells at 1x speed = 200 points");
        assertEquals(0, timer.getHardDropScore(0), "Hard drop 0 cells = 0 points");

        // Test at higher speed
        timer.setSpeedFactor(5.0);
        assertEquals(50, timer.getHardDropScore(1), "Hard drop 1 cell at 5x speed = 50 points");
        assertEquals(
                500, timer.getHardDropScore(10), "Hard drop 10 cells at 5x speed = 500 points");
    }

    @Test
    @DisplayName("소수점 속도에서 점수 계산이 올바른지 확인")
    void testScoreCalculationWithFractionalSpeed() {
        // Test with fractional speed factor
        timer.setSpeedFactor(2.5);
        assertEquals(25, timer.calculateDropScore(1), "1 cell at 2.5x speed = 25 points");
        assertEquals(250, timer.calculateDropScore(10), "10 cells at 2.5x speed = 250 points");

        timer.setSpeedFactor(1.5);
        assertEquals(15, timer.calculateDropScore(1), "1 cell at 1.5x speed = 15 points");
    }

    @Test
    @DisplayName("음수 드롭 거리 처리를 확인 (엣지 케이스)")
    void testNegativeDropDistance() {
        // Edge case: what happens with negative distance?
        int score = timer.calculateDropScore(-5);
        assertEquals(
                -50,
                score,
                "Negative drop distance should give negative score (or should it be validated?)");
    }
}
