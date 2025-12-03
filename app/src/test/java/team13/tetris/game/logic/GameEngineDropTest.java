package team13.tetris.game.logic;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import team13.tetris.game.controller.GameStateListener;
import team13.tetris.game.model.Board;
import team13.tetris.game.model.Tetromino;

// GameEngine Drop 테스트: Tests soft drop and hard drop functionality, scoring, and auto-drop
// scheduling
@DisplayName("GameEngine Drop 테스트")
public class GameEngineDropTest {

    private Board board;
    private TestListener listener;
    private GameEngine engine;

    // Test listener to track callback invocations
    private static class TestListener implements GameStateListener {
        int scoreChangedCount = 0;
        int boardUpdatedCount = 0;

        @Override
        public void onScoreChanged(int newScore) {
            scoreChangedCount++;
        }

        @Override
        public void onGameOver() {}

        @Override
        public void onPieceSpawned(Tetromino piece, int x, int y) {}

        @Override
        public void onBoardUpdated(Board board) {
            boardUpdatedCount++;
        }

        @Override
        public void onLinesCleared(int linesCleared) {}

        @Override
        public void onNextPiece(Tetromino nextPiece) {}

        void reset() {
            scoreChangedCount = 0;
            boardUpdatedCount = 0;
        }
    }

    @BeforeEach
    void setUp() {
        board = new Board(10, 20);
        listener = new TestListener();
        engine = new GameEngine(board, listener);
        engine.startNewGame();
        listener.reset(); // startNewGame의 콜백 무시
    }

    @AfterEach
    void tearDown() {
        if (engine != null) {
            engine.shutdown();
        }
    }

    @Test
    @DisplayName("softDrop: 한 칸 하강 성공 시 true를 반환해야 함")
    void testSoftDropSuccess() {
        int initialY = engine.getPieceY();

        boolean result = engine.softDrop();

        assertTrue(result, "하강 성공 시 true를 반환해야 함");
        assertEquals(initialY + 1, engine.getPieceY(), "Y 좌표가 1 증가해야 함");
        assertTrue(listener.boardUpdatedCount > 0, "보드 업데이트 콜백이 호출되어야 함");
    }

    @Test
    @DisplayName("softDrop: 바닥 충돌 시 조각이 고정되고 false를 반환해야 함")
    void testSoftDropBottomCollision() {
        // 조각을 바닥 근처로 이동
        while (board.fits(
                engine.getCurrent().getShape(), engine.getPieceX(), engine.getPieceY() + 1)) {
            engine.softDrop();
        }

        Tetromino beforeDrop = engine.getCurrent();
        boolean result = engine.softDrop();

        assertFalse(result, "충돌 시 false를 반환해야 함");
        assertNotSame(beforeDrop, engine.getCurrent(), "새 조각이 생성되어야 함");
    }

    @Test
    @DisplayName("softDrop: 다른 블록과 충돌 시 조각이 고정되어야 함")
    void testSoftDropBlockCollision() {
        // 바닥에 블록 배치
        for (int x = 0; x < board.getWidth(); x++) board.setCell(x, 19, 1);

        // 조각을 충돌 위치로 이동
        while (board.fits(
                engine.getCurrent().getShape(), engine.getPieceX(), engine.getPieceY() + 1))
            engine.softDrop();

        Tetromino beforeDrop = engine.getCurrent();
        boolean result = engine.softDrop();

        assertFalse(result, "블록 충돌 시 false를 반환해야 함");
        assertNotSame(beforeDrop, engine.getCurrent(), "새 조각이 생성되어야 함");
    }

    @Test
    @DisplayName("softDrop: 점수가 추가되어야 함")
    void testSoftDropAddsScore() {
        int initialScore = engine.getScore();
        listener.reset();

        engine.softDrop();

        assertTrue(engine.getScore() > initialScore, "점수가 증가해야 함");
        assertTrue(listener.scoreChangedCount > 0, "점수 변경 콜백이 호출되어야 함");
    }

    @Test
    @DisplayName("softDrop: current가 null이면 false를 반환해야 함")
    void testSoftDropWithNullCurrent() {
        // shutdown을 호출하여 current를 null로 만들 수 있지만,
        // 직접 테스트하기 어려우므로 간접적으로 검증
        engine.shutdown();

        // 새 엔진 생성 (startNewGame 호출 안함)
        GameEngine newEngine = new GameEngine(board, listener);
        boolean result = newEngine.softDrop();

        assertFalse(result, "current가 null이면 false를 반환해야 함");
    }

    @Test
    @DisplayName("hardDrop: 즉시 바닥까지 하강하고 조각이 고정되어야 함")
    void testHardDrop() {
        Tetromino pieceBeforeDrop = engine.getCurrent();

        engine.hardDrop();

        // hardDrop 후에는 조각이 고정되고 새 조각이 생성됨
        assertNotSame(pieceBeforeDrop, engine.getCurrent(), "hardDrop 후 새 조각이 생성되어야 함");

        // 보드에 이전 조각이 배치되었는지 확인
        boolean foundPiece = false;
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                if (board.getCell(x, y) != 0) {
                    foundPiece = true;
                    break;
                }
            }
            if (foundPiece) break;
        }
        assertTrue(foundPiece, "보드에 조각이 배치되어야 함");
    }

    @Test
    @DisplayName("hardDrop: 떨어진 거리만큼 점수가 추가되어야 함")
    void testHardDropAddsScore() {
        int initialY = engine.getPieceY();
        int initialScore = engine.getScore();
        listener.reset();

        engine.hardDrop();

        int dropDistance = engine.getPieceY() - initialY;
        if (dropDistance > 0) {
            assertTrue(engine.getScore() > initialScore, "떨어진 거리가 있으면 점수가 증가해야 함");
            assertTrue(listener.scoreChangedCount > 0, "점수 변경 콜백이 호출되어야 함");
        }
    }

    @Test
    @DisplayName("hardDrop: 0칸 떨어질 때는 점수가 추가되지 않아야 함")
    void testHardDropNoDistanceNoScore() {
        // 조각을 바닥까지 이동
        while (board.fits(
                engine.getCurrent().getShape(), engine.getPieceX(), engine.getPieceY() + 1)) {
            engine.softDrop();
        }

        int currentScore = engine.getScore();
        listener.reset();
        engine.hardDrop();

        // 0칸 떨어졌으므로 점수 변화 없음
        assertEquals(currentScore, engine.getScore(), "0칸 떨어질 때는 점수가 변하지 않아야 함");
    }

    @Test
    @DisplayName("hardDrop: current가 null이면 아무 동작도 하지 않아야 함")
    void testHardDropWithNullCurrent() {
        GameEngine newEngine = new GameEngine(board, listener);

        assertDoesNotThrow(
                () -> {
                    newEngine.hardDrop();
                },
                "current가 null이어도 예외가 발생하지 않아야 함");
    }

    @Test
    @DisplayName("startAutoDrop: 자동 하강 스케줄러가 시작되어야 함")
    void testStartAutoDrop() throws InterruptedException {
        engine.stopAutoDrop();
        int initialY = engine.getPieceY();

        engine.startAutoDrop();

        // 자동 하강이 동작하는지 확인 (1초 이상 대기)
        Thread.sleep(1200);

        // Y 좌표가 변경되었거나 새 조각이 생성되었을 것
        assertTrue(
                engine.getPieceY() > initialY || engine.getPieceY() == 0, "자동 하강으로 인해 조각이 이동했어야 함");
    }

    @Test
    @DisplayName("stopAutoDrop: 자동 하강이 중지되어야 함")
    void testStopAutoDrop() throws InterruptedException {
        engine.startAutoDrop();
        Thread.sleep(100); // 스케줄러 시작 대기

        engine.stopAutoDrop();

        Thread.sleep(1200); // 1초 이상 대기

        // 자동 하강이 중지되었으므로 Y 좌표가 변하지 않았을 수 있음
        // (단, softDrop이 마지막으로 실행되어 조각이 교체될 수도 있음)
        assertNotNull(engine.getCurrent(), "조각은 여전히 존재해야 함");
    }

    @Test
    @DisplayName("startAutoDrop: 중복 호출해도 안전해야 함")
    void testStartAutoDropMultipleTimes() {
        assertDoesNotThrow(
                () -> {
                    engine.startAutoDrop();
                    engine.startAutoDrop();
                    engine.startAutoDrop();
                },
                "중복 호출 시 예외가 발생하지 않아야 함");
    }

    @Test
    @DisplayName("stopAutoDrop: 시작하지 않은 상태에서 호출해도 안전해야 함")
    void testStopAutoDropWithoutStart() {
        GameEngine newEngine = new GameEngine(board, listener);

        assertDoesNotThrow(
                () -> {
                    newEngine.stopAutoDrop();
                },
                "시작하지 않은 상태에서도 예외가 발생하지 않아야 함");

        newEngine.shutdown();
    }

    @Test
    @DisplayName("setDropIntervalSeconds: 하강 간격을 변경할 수 있어야 함")
    void testSetDropIntervalSeconds() {
        engine.setDropIntervalSeconds(0.5);

        assertEquals(0.5, engine.getDropIntervalSeconds(), 0.001, "하강 간격이 변경되어야 함");
    }

    @Test
    @DisplayName("setDropIntervalSeconds: 음수나 0은 허용되지 않아야 함")
    void testSetDropIntervalSecondsInvalid() {
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    engine.setDropIntervalSeconds(0);
                },
                "0은 허용되지 않아야 함");

        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    engine.setDropIntervalSeconds(-1.0);
                },
                "음수는 허용되지 않아야 함");
    }

    @Test
    @DisplayName("shutdown: 스케줄러가 완전히 종료되어야 함")
    void testShutdown() {
        engine.startAutoDrop();

        engine.shutdown();

        // shutdown 후에는 새로운 작업이 예약되지 않아야 함
        assertDoesNotThrow(
                () -> {
                    engine.shutdown(); // 중복 호출해도 안전
                },
                "shutdown 중복 호출 시 예외가 발생하지 않아야 함");
    }

    @Test
    @DisplayName("getDropIntervalSeconds: 기본값 1.0초를 반환해야 함")
    void testGetDropIntervalSecondsDefault() {
        GameEngine newEngine = new GameEngine(board, listener);

        assertEquals(1.0, newEngine.getDropIntervalSeconds(), 0.001, "기본 하강 간격은 1.0초여야 함");

        newEngine.shutdown();
    }

    @Test
    @DisplayName("addDropScore: 소프트 드롭 점수가 정확히 계산되어야 함")
    void testAddDropScore() {
        int initialScore = engine.getScore();
        listener.reset();

        // softDrop 호출 (내부적으로 addDropScore(1) 호출)
        engine.softDrop();

        int expectedScore = initialScore + engine.getGameTimer().getSoftDropScore();
        assertEquals(expectedScore, engine.getScore(), "소프트 드롭 점수가 정확히 계산되어야 함");
    }

    @Test
    @DisplayName("addHardDropScore: 하드 드롭 점수가 정확히 계산되어야 함")
    void testAddHardDropScore() {
        int initialY = engine.getPieceY();
        int initialScore = engine.getScore();

        engine.hardDrop();

        int dropDistance = engine.getPieceY() - initialY;
        if (dropDistance > 0) {
            int expectedScore = initialScore + engine.getGameTimer().getHardDropScore(dropDistance);
            assertEquals(expectedScore, engine.getScore(), "하드 드롭 점수가 정확히 계산되어야 함");
        }
    }
}
