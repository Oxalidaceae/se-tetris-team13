package team13.tetris.game.logic;

import org.junit.jupiter.api.Test;
import team13.tetris.data.ScoreBoard;
import team13.tetris.game.controller.GameStateListener;
import team13.tetris.game.model.Board;
import team13.tetris.game.model.Tetromino;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;

// Tests for difficulty-based piece spawning with Roulette Wheel Selection
@DisplayName("GameEngine Difficulty-Based Piece Spawning Tests")
public class GameEngineDifficultyTest {

    private static class NoOpListener implements GameStateListener {
        @Override
        public void onScoreChanged(int newScore) {}

        @Override
        public void onGameOver() {}

        @Override
        public void onPieceSpawned(Tetromino piece, int x, int y) {}

        @Override
        public void onBoardUpdated(Board board) {}

        @Override
        public void onLinesCleared(int linesCleared) {}

        @Override
        public void onNextPiece(Tetromino nextPiece) {}
    }

    @Test
    @DisplayName("EASY 모드로 게임 엔진을 생성할 수 있어야 함")
    void testEasyModeEngineCreation() {
        Board board = new Board(10, 20);
        GameEngine easyEngine = new GameEngine(board, new NoOpListener(), ScoreBoard.ScoreEntry.Mode.EASY);

        // Verify the engine was created successfully with EASY mode
        assertNotNull(easyEngine);
    }

    @Test
    @DisplayName("HARD 모드로 게임 엔진을 생성할 수 있어야 함")
    void testHardModeEngineCreation() {
        Board board = new Board(10, 20);
        GameEngine hardEngine = new GameEngine(board, new NoOpListener(), ScoreBoard.ScoreEntry.Mode.HARD);

        assertNotNull(hardEngine);
    }

    @Test
    @DisplayName("NORMAL 모드로 게임 엔진을 생성할 수 있어야 함")
    void testNormalModeEngineCreation() {
        Board board = new Board(10, 20);
        GameEngine normalEngine = new GameEngine(board, new NoOpListener(), ScoreBoard.ScoreEntry.Mode.NORMAL);

        assertNotNull(normalEngine);
    }

    @Test
    @DisplayName("기본 생성자는 NORMAL 모드를 사용해야 함")
    void testDefaultConstructorUsesNormalMode() {
        Board board = new Board(10, 20);
        GameEngine defaultEngine = new GameEngine(board, new NoOpListener());

        // Should work without errors (uses NORMAL by default)
        assertNotNull(defaultEngine);
    }

    @Test
    @DisplayName("EASY 모드: I블록 확률 16.7% (가중치 12/72), 10000회 샘플링으로 ±5% 오차 검증")
    void testEasyModeBlockDistribution() {
        System.out.println("\n=== EASY Mode Block Distribution Test (10000 samples) ===");
        testBlockDistribution(ScoreBoard.ScoreEntry.Mode.EASY, 10000);
    }

    @Test
    @DisplayName("NORMAL 모드: 모든 블록 확률 14.3% (가중치 10/70), 10000회 샘플링으로 ±5% 오차 검증")
    void testNormalModeBlockDistribution() {
        System.out.println("\n=== NORMAL Mode Block Distribution Test (10000 samples) ===");
        testBlockDistribution(ScoreBoard.ScoreEntry.Mode.NORMAL, 10000);
    }

    @Test
    @DisplayName("HARD 모드: I블록 확률 11.8% (가중치 8/68), 10000회 샘플링으로 ±5% 오차 검증")
    void testHardModeBlockDistribution() {
        System.out.println("\n=== HARD Mode Block Distribution Test (10000 samples) ===");
        testBlockDistribution(ScoreBoard.ScoreEntry.Mode.HARD, 10000);
    }

    private void testBlockDistribution(ScoreBoard.ScoreEntry.Mode difficulty, int samples) {
        Board board = new Board(10, 20);
        GameEngine engine = new GameEngine(board, new NoOpListener(), difficulty);

        // Count each piece type
        int[] counts = new int[7]; // I, O, T, S, Z, J, L

        for (int i = 0; i < samples; i++) {
            Tetromino piece = engine.generateTestPiece();
            Tetromino.Kind kind = piece.getKind();
            if (kind != null) {
                switch (kind) {
                    case I:
                        counts[0]++;
                        break;
                    case O:
                        counts[1]++;
                        break;
                    case T:
                        counts[2]++;
                        break;
                    case S:
                        counts[3]++;
                        break;
                    case Z:
                        counts[4]++;
                        break;
                    case J:
                        counts[5]++;
                        break;
                    case L:
                        counts[6]++;
                        break;
                    default:
                        // 아이템 블록 (COPY, WEIGHT, GRAVITY, SPLIT, LINE_CLEAR)은 카운트하지 않음
                        break;
                }
            }
        }

        // Calculate expected weights
        int[] weights = getExpectedWeights(difficulty);
        int totalWeight = 0;
        for (int w : weights)
            totalWeight += w;

        // Print results
        String[] pieceNames = { "I", "O", "T", "S", "Z", "J", "L" };
        System.out.println("Piece | Count | Actual% | Expected% | Difference");
        System.out.println("------|-------|---------|-----------|------------");

        for (int i = 0; i < 7; i++) {
            double actualPercent = (counts[i] * 100.0) / samples;
            double expectedPercent = (weights[i] * 100.0) / totalWeight;
            double difference = actualPercent - expectedPercent;

            System.out.printf("  %s   | %4d  | %6.2f%% |  %6.2f%%  | %+6.2f%%\n",
                    pieceNames[i], counts[i], actualPercent, expectedPercent, difference);
        }

        System.out.println("------|-------|---------|-----------|------------");
        System.out.printf("Total | %4d  | 100.00%% |  100.00%%  |  0.00%%\n\n", samples);

        // Verify I-block is within reasonable range (5% margin for statistical
        // variance)
        double iBlockPercent = (counts[0] * 100.0) / samples;
        double expectedIPercent = (weights[0] * 100.0) / totalWeight;

        assertTrue(Math.abs(iBlockPercent - expectedIPercent) < 5.0,
                String.format("I-block distribution deviation too large: %.2f%% vs expected %.2f%%",
                        iBlockPercent, expectedIPercent));
    }

    private int[] getExpectedWeights(ScoreBoard.ScoreEntry.Mode difficulty) {
        switch (difficulty) {
            case EASY:
                return new int[] { 12, 10, 10, 10, 10, 10, 10 }; // I gets +20% (12/10)
            case HARD:
                return new int[] { 8, 10, 10, 10, 10, 10, 10 }; // I gets -20% (8/10)
            default: // NORMAL
                return new int[] { 10, 10, 10, 10, 10, 10, 10 }; // All equal
        }
    }

    @Test
    @DisplayName("난이도별 속도 증가율 검증: EASY 0.8배, NORMAL 1.0배, HARD 1.2배")
    void testSpeedIncreaseByDifficulty() {
        System.out.println("\n=== Speed Increase Test by Difficulty ===");

        // Test each difficulty mode
        testSpeedIncrease(ScoreBoard.ScoreEntry.Mode.EASY, 0.8, "EASY (20% slower)");
        testSpeedIncrease(ScoreBoard.ScoreEntry.Mode.NORMAL, 1.0, "NORMAL (baseline)");
        testSpeedIncrease(ScoreBoard.ScoreEntry.Mode.HARD, 1.2, "HARD (20% faster)");
    }

    private void testSpeedIncrease(ScoreBoard.ScoreEntry.Mode difficulty, double expectedMultiplier, String label) {
        Board board = new Board(10, 20);
        GameEngine engine = new GameEngine(board, new NoOpListener(), difficulty);

        double initialSpeed = engine.getGameTimer().getSpeedFactor();
        System.out.printf("\n%s:\n", label);
        System.out.printf("  Initial speed: %.2f\n", initialSpeed);

        // Simulate clearing 10 lines (triggers one speed increase)
        engine.updateSpeedForLinesCleared(10, 10);

        double afterFirstIncrease = engine.getGameTimer().getSpeedFactor();
        double actualIncrease = afterFirstIncrease - initialSpeed;
        double expectedIncrease = 0.1 * expectedMultiplier; // SPEED_INCREMENT = 0.1

        System.out.printf("  After 10 lines: %.2f (increase: %.3f)\n", afterFirstIncrease, actualIncrease);
        System.out.printf("  Expected increase: %.3f\n", expectedIncrease);
        System.out.printf("  Multiplier applied: %.2fx\n", expectedMultiplier);

        // Allow small floating-point error margin
        assertEquals(expectedIncrease, actualIncrease, 0.001,
                String.format("%s mode speed increase incorrect: expected %.3f, got %.3f",
                        difficulty, expectedIncrease, actualIncrease));

        // Test cumulative effect after multiple increases
        engine.updateSpeedForLinesCleared(10, 20);
        engine.updateSpeedForLinesCleared(10, 30);

        double afterThreeIncreases = engine.getGameTimer().getSpeedFactor();
        double totalIncrease = afterThreeIncreases - initialSpeed;
        double expectedTotalIncrease = 0.1 * expectedMultiplier * 3;

        System.out.printf("  After 30 lines: %.2f (total increase: %.3f)\n", afterThreeIncreases, totalIncrease);
        System.out.printf("  Expected total increase: %.3f\n", expectedTotalIncrease);

        assertEquals(expectedTotalIncrease, totalIncrease, 0.001,
                String.format("%s mode cumulative speed increase incorrect", difficulty));
    }
}
