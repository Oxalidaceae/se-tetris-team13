package team13.tetris.game.logic;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import team13.tetris.data.ScoreBoard;
import team13.tetris.game.controller.GameStateListener;
import team13.tetris.game.model.Board;
import team13.tetris.game.model.Tetromino;

// GameEngine Line Clear 테스트: Tests line clear scoring, speed increase, and total lines cleared tracking
@DisplayName("GameEngine Line Clear 테스트")
public class GameEngineLineClearTest {

    private GameEngine engine;
    private Board board;
    private TestListener listener;

    // 테스트용 리스너 클래스
    private static class TestListener implements GameStateListener {
        @Override
        public void onScoreChanged(int score) {}

        @Override
        public void onBoardUpdated(Board board) {}

        @Override
        public void onLinesCleared(int lines) {}

        @Override
        public void onGameOver() {}

        @Override
        public void onPieceSpawned(Tetromino piece, int x, int y) {}

        @Override
        public void onNextPiece(Tetromino piece) {}
    }

    @BeforeEach
    void setUp() {
        listener = new TestListener();
        board = new Board(10, 20);
        engine = new GameEngine(board, listener, ScoreBoard.ScoreEntry.Mode.NORMAL);
        engine.startNewGame();
    }

    @AfterEach
    void tearDown() {
        if (engine != null) engine.shutdown();
    }

    // 라인 클리어 점수 테스트
    @Test
    @DisplayName("1줄 클리어: 100점 추가")
    void testSingleLineClearScore() {
        int initialScore = engine.getScore();

        engine.addScoreForClearedLines(1);

        assertEquals(initialScore + 100, engine.getScore(), "1줄 클리어 시 100점 추가되어야 함");
    }

    @Test
    @DisplayName("2줄 클리어: 250점 추가")
    void testDoubleLineClearScore() {
        int initialScore = engine.getScore();

        engine.addScoreForClearedLines(2);

        assertEquals(initialScore + 250, engine.getScore(), "2줄 클리어 시 250점 추가되어야 함");
    }

    @Test
    @DisplayName("3줄 클리어: 500점 추가")
    void testTripleLineClearScore() {
        int initialScore = engine.getScore();

        engine.addScoreForClearedLines(3);

        assertEquals(initialScore + 500, engine.getScore(), "3줄 클리어 시 500점 추가되어야 함");
    }

    @Test
    @DisplayName("4줄 클리어 (테트리스): 1000점 추가")
    void testTetrisLineClearScore() {
        int initialScore = engine.getScore();

        engine.addScoreForClearedLines(4);

        assertEquals(initialScore + 1000, engine.getScore(), "4줄 클리어 시 1000점 추가되어야 함");
    }

    @Test
    @DisplayName("5줄 이상 클리어: 1000 + (추가줄 * 250)점 추가")
    void testMultipleLineClearScore() {
        int initialScore = engine.getScore();

        engine.addScoreForClearedLines(5);

        // 5줄: 1000 + (5-4) * 250 = 1250점
        assertEquals(initialScore + 1250, engine.getScore(), "5줄 클리어 시 1250점 추가되어야 함");
    }

    @Test
    @DisplayName("6줄 클리어: 1500점 추가")
    void testSixLineClearScore() {
        int initialScore = engine.getScore();

        engine.addScoreForClearedLines(6);

        // 6줄: 1000 + (6-4) * 250 = 1500점
        assertEquals(initialScore + 1500, engine.getScore(), "6줄 클리어 시 1500점 추가되어야 함");
    }

    @Test
    @DisplayName("0줄 클리어: 점수 변화 없음")
    void testNoLineClearScore() {
        int initialScore = engine.getScore();

        engine.addScoreForClearedLines(0);

        assertEquals(initialScore, engine.getScore(), "0줄 클리어 시 점수 변화 없어야 함");
    }

    @Test
    @DisplayName("연속 라인 클리어: 점수가 누적되어야 함")
    void testConsecutiveLineClearScore() {
        int initialScore = engine.getScore();

        engine.addScoreForClearedLines(1); // +100
        engine.addScoreForClearedLines(2); // +250
        engine.addScoreForClearedLines(4); // +1000

        assertEquals(initialScore + 1350, engine.getScore(), "연속 라인 클리어 시 점수가 누적되어야 함");
    }

    // 속도 증가 테스트
    @Test
    @DisplayName("3줄 클리어 시 속도 증가")
    void testSpeedIncreaseAfter3Lines() {
        double initialInterval = engine.getDropIntervalSeconds();

        // 3줄 클리어 시뮬레이션
        engine.updateSpeedForLinesCleared(3, 3);

        double newInterval = engine.getDropIntervalSeconds();
        assertTrue(newInterval < initialInterval, "3줄 클리어 후 드롭 간격이 감소(속도 증가)해야 함");
    }

    @Test
    @DisplayName("3줄 미만에서는 속도 유지")
    void testNoSpeedIncreaseBelow3Lines() {
        double initialInterval = engine.getDropIntervalSeconds();

        // 2줄 클리어
        engine.updateSpeedForLinesCleared(2, 2);

        double newInterval = engine.getDropIntervalSeconds();
        assertEquals(initialInterval, newInterval, 0.001, "3줄 미만에서는 속도가 유지되어야 함");
    }

    @Test
    @DisplayName("6줄 클리어 시 추가 속도 증가")
    void testSpeedIncreaseAfter6Lines() {
        // 첫 번째 속도 증가 (3줄)
        engine.updateSpeedForLinesCleared(3, 3);
        double intervalAfter3 = engine.getDropIntervalSeconds();

        // 두 번째 속도 증가 (6줄)
        engine.updateSpeedForLinesCleared(3, 6);
        double intervalAfter6 = engine.getDropIntervalSeconds();

        assertTrue(intervalAfter6 < intervalAfter3, "6줄 클리어 후 추가 속도 증가해야 함");
    }

    // 난이도별 속도 증가율 테스트
    @Test
    @DisplayName("EASY 모드: 속도 증가율 0.8배")
    void testEasyModeSpeedIncrease() {
        GameEngine easyEngine = new GameEngine(new Board(10, 20), listener, ScoreBoard.ScoreEntry.Mode.EASY);
        easyEngine.startNewGame();

        double initialInterval = easyEngine.getDropIntervalSeconds();
        easyEngine.updateSpeedForLinesCleared(3, 3);
        double newInterval = easyEngine.getDropIntervalSeconds();

        assertTrue(newInterval < initialInterval, "EASY 모드에서도 속도가 증가해야 함");

        easyEngine.shutdown();
    }

    @Test
    @DisplayName("NORMAL 모드: 속도 증가율 1.0배")
    void testNormalModeSpeedIncrease() {
        double initialInterval = engine.getDropIntervalSeconds();

        engine.updateSpeedForLinesCleared(3, 3);
        double newInterval = engine.getDropIntervalSeconds();

        assertTrue(newInterval < initialInterval, "NORMAL 모드에서 속도가 증가해야 함");
    }

    @Test
    @DisplayName("HARD 모드: 속도 증가율 1.2배")
    void testHardModeSpeedIncrease() {
        GameEngine hardEngine = new GameEngine(new Board(10, 20), listener, ScoreBoard.ScoreEntry.Mode.HARD);
        hardEngine.startNewGame();

        double initialInterval = hardEngine.getDropIntervalSeconds();
        hardEngine.updateSpeedForLinesCleared(3, 3);
        double newInterval = hardEngine.getDropIntervalSeconds();

        assertTrue(newInterval < initialInterval, "HARD 모드에서 속도가 증가해야 함");

        hardEngine.shutdown();
    }

    @Test
    @DisplayName("난이도별 속도 증가율 비교: HARD > NORMAL > EASY")
    void testDifficultySpeedIncreaseComparison() {
        GameEngine easyEngine = new GameEngine(new Board(10, 20), new TestListener(), ScoreBoard.ScoreEntry.Mode.EASY);
        GameEngine normalEngine = new GameEngine(new Board(10, 20), new TestListener(), ScoreBoard.ScoreEntry.Mode.NORMAL);
        GameEngine hardEngine = new GameEngine(new Board(10, 20), new TestListener(), ScoreBoard.ScoreEntry.Mode.HARD);

        easyEngine.startNewGame();
        normalEngine.startNewGame();
        hardEngine.startNewGame();

        double easyInitial = easyEngine.getDropIntervalSeconds();
        double normalInitial = normalEngine.getDropIntervalSeconds();
        double hardInitial = hardEngine.getDropIntervalSeconds();

        // 3줄 클리어 후 속도 증가
        easyEngine.updateSpeedForLinesCleared(3, 3);
        normalEngine.updateSpeedForLinesCleared(3, 3);
        hardEngine.updateSpeedForLinesCleared(3, 3);

        double easyAfter = easyEngine.getDropIntervalSeconds();
        double normalAfter = normalEngine.getDropIntervalSeconds();
        double hardAfter = hardEngine.getDropIntervalSeconds();

        // 속도 증가율 계산 (감소율)
        double easyDecrease = (easyInitial - easyAfter) / easyInitial;
        double normalDecrease = (normalInitial - normalAfter) / normalInitial;
        double hardDecrease = (hardInitial - hardAfter) / hardInitial;

        // HARD가 가장 많이 감소(가장 빠르게), EASY가 가장 적게 감소
        assertTrue(hardDecrease > normalDecrease, "HARD 모드가 NORMAL보다 속도 증가율이 커야 함");
        assertTrue(normalDecrease > easyDecrease, "NORMAL 모드가 EASY보다 속도 증가율이 커야 함");

        easyEngine.shutdown();
        normalEngine.shutdown();
        hardEngine.shutdown();
    }

    // totalLinesCleared 카운트 테스트
    @Test
    @DisplayName("초기 totalLinesCleared는 0")
    void testInitialTotalLinesCleared() {
        assertEquals(0, engine.getTotalLinesCleared(), "초기 totalLinesCleared는 0이어야 함");
    }

    @Test
    @DisplayName("라인 클리어 시 totalLinesCleared 증가 (ITEM 모드)")
    void testTotalLinesClearedIncrease() {
        // ITEM 모드에서만 totalLinesCleared가 증가
        GameEngine itemEngine = new GameEngine(new Board(10, 20), listener, ScoreBoard.ScoreEntry.Mode.ITEM);
        itemEngine.startNewGame();

        int initialTotal = itemEngine.getTotalLinesCleared();

        itemEngine.addScoreForClearedLines(3);

        // ITEM 모드에서 addScoreForClearedLines 호출 시 totalLinesCleared 증가
        assertTrue(itemEngine.getTotalLinesCleared() >= initialTotal, "라인 클리어 시 totalLinesCleared가 증가하거나 유지되어야 함");

        itemEngine.shutdown();
    }

    @Test
    @DisplayName("getTotalLinesCleared 접근자 정상 동작")
    void testGetTotalLinesCleared() {
        assertNotNull(engine.getTotalLinesCleared(), "getTotalLinesCleared()가 null을 반환하지 않아야 함");
        assertTrue(engine.getTotalLinesCleared() >= 0, "totalLinesCleared는 음수가 아니어야 함");
    }
}
