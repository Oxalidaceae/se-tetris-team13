package team13.tetris.game.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import team13.tetris.game.model.Board;
import team13.tetris.game.model.Tetromino;

// CompositeGameStateListener 테스트: Tests listener management and event dispatching
@DisplayName("CompositeGameStateListener 테스트")
public class CompositeGameStateListenerTest {

    private CompositeGameStateListener composite;
    private TestListener listener1;
    private TestListener listener2;
    private TestListener listener3;

    // 테스트용 리스너 (메서드 호출 추적)
    private static class TestListener implements GameStateListener {
        int onBoardUpdatedCount = 0;
        int onPieceSpawnedCount = 0;
        int onLinesClearedCount = 0;
        int onGameOverCount = 0;
        int onNextPieceCount = 0;
        int onScoreChangedCount = 0;

        Board lastBoard = null;
        Tetromino lastSpawnedPiece = null;
        int lastSpawnX = -1;
        int lastSpawnY = -1;
        int lastLinesCleared = -1;
        Tetromino lastNextPiece = null;
        int lastScore = -1;

        boolean throwException = false;
        String exceptionMessage = "Test Exception";

        @Override
        public void onBoardUpdated(Board board) {
            onBoardUpdatedCount++;
            lastBoard = board;
            if (throwException) throw new RuntimeException(exceptionMessage);
        }

        @Override
        public void onPieceSpawned(Tetromino tetromino, int px, int py) {
            onPieceSpawnedCount++;
            lastSpawnedPiece = tetromino;
            lastSpawnX = px;
            lastSpawnY = py;
            if (throwException) throw new RuntimeException(exceptionMessage);
        }

        @Override
        public void onLinesCleared(int lines) {
            onLinesClearedCount++;
            lastLinesCleared = lines;
            if (throwException) throw new RuntimeException(exceptionMessage);
        }

        @Override
        public void onGameOver() {
            onGameOverCount++;
            if (throwException) throw new RuntimeException(exceptionMessage);
        }

        @Override
        public void onNextPiece(Tetromino next) {
            onNextPieceCount++;
            lastNextPiece = next;
            if (throwException) throw new RuntimeException(exceptionMessage);
        }

        @Override
        public void onScoreChanged(int score) {
            onScoreChangedCount++;
            lastScore = score;
            if (throwException) throw new RuntimeException(exceptionMessage);
        }
    }

    @BeforeEach
    void setUp() {
        composite = new CompositeGameStateListener();
        listener1 = new TestListener();
        listener2 = new TestListener();
        listener3 = new TestListener();
    }

    @AfterEach
    void tearDown() {
        composite = null;
        listener1 = null;
        listener2 = null;
        listener3 = null;
    }

    // 리스너 관리 테스트
    @Test
    @DisplayName("add: 리스너 추가 후 이벤트 전달됨")
    void testAddListener() {
        composite.add(listener1);

        composite.onScoreChanged(100);

        assertEquals(1, listener1.onScoreChangedCount);
        assertEquals(100, listener1.lastScore);
    }

    @Test
    @DisplayName("add: 여러 리스너 추가 후 모든 리스너에게 이벤트 전달")
    void testAddMultipleListeners() {
        composite.add(listener1);
        composite.add(listener2);
        composite.add(listener3);

        composite.onScoreChanged(500);

        assertEquals(1, listener1.onScoreChangedCount);
        assertEquals(1, listener2.onScoreChangedCount);
        assertEquals(1, listener3.onScoreChangedCount);
        assertEquals(500, listener1.lastScore);
        assertEquals(500, listener2.lastScore);
        assertEquals(500, listener3.lastScore);
    }

    @Test
    @DisplayName("add: null 리스너 추가 시 안전하게 무시됨")
    void testAddNullListener() {
        composite.add(null);
        composite.add(listener1);

        composite.onScoreChanged(200);

        assertEquals(1, listener1.onScoreChangedCount);
        assertEquals(200, listener1.lastScore);
    }

    @Test
    @DisplayName("remove: 리스너 제거 후 이벤트 전달되지 않음")
    void testRemoveListener() {
        composite.add(listener1);
        composite.add(listener2);

        composite.remove(listener1);
        composite.onScoreChanged(300);

        assertEquals(0, listener1.onScoreChangedCount);
        assertEquals(1, listener2.onScoreChangedCount);
        assertEquals(300, listener2.lastScore);
    }

    @Test
    @DisplayName("remove: 존재하지 않는 리스너 제거 시 예외 발생하지 않음")
    void testRemoveNonExistentListener() {
        composite.add(listener1);

        assertDoesNotThrow(() -> composite.remove(listener2));
        assertDoesNotThrow(() -> composite.remove(null));

        composite.onScoreChanged(400);
        assertEquals(1, listener1.onScoreChangedCount);
    }

    // 이벤트 전달 테스트
    @Test
    @DisplayName("onBoardUpdated: 모든 리스너에게 보드 전달")
    void testOnBoardUpdated() {
        composite.add(listener1);
        composite.add(listener2);

        Board board = new Board(10, 20);
        composite.onBoardUpdated(board);

        assertEquals(1, listener1.onBoardUpdatedCount);
        assertEquals(1, listener2.onBoardUpdatedCount);
        assertSame(board, listener1.lastBoard);
        assertSame(board, listener2.lastBoard);
    }

    @Test
    @DisplayName("onPieceSpawned: 모든 리스너에게 조각과 위치 전달")
    void testOnPieceSpawned() {
        composite.add(listener1);
        composite.add(listener2);

        Tetromino piece = new Tetromino(Tetromino.Kind.T);
        composite.onPieceSpawned(piece, 3, 0);

        assertEquals(1, listener1.onPieceSpawnedCount);
        assertEquals(1, listener2.onPieceSpawnedCount);
        assertSame(piece, listener1.lastSpawnedPiece);
        assertSame(piece, listener2.lastSpawnedPiece);
        assertEquals(3, listener1.lastSpawnX);
        assertEquals(0, listener1.lastSpawnY);
        assertEquals(3, listener2.lastSpawnX);
        assertEquals(0, listener2.lastSpawnY);
    }

    @Test
    @DisplayName("onLinesCleared: 모든 리스너에게 라인 수 전달")
    void testOnLinesCleared() {
        composite.add(listener1);
        composite.add(listener2);

        composite.onLinesCleared(4);

        assertEquals(1, listener1.onLinesClearedCount);
        assertEquals(1, listener2.onLinesClearedCount);
        assertEquals(4, listener1.lastLinesCleared);
        assertEquals(4, listener2.lastLinesCleared);
    }

    @Test
    @DisplayName("onGameOver: 모든 리스너에게 게임오버 전달")
    void testOnGameOver() {
        composite.add(listener1);
        composite.add(listener2);

        composite.onGameOver();

        assertEquals(1, listener1.onGameOverCount);
        assertEquals(1, listener2.onGameOverCount);
    }

    @Test
    @DisplayName("onNextPiece: 모든 리스너에게 다음 조각 전달")
    void testOnNextPiece() {
        composite.add(listener1);
        composite.add(listener2);

        Tetromino nextPiece = new Tetromino(Tetromino.Kind.I);
        composite.onNextPiece(nextPiece);

        assertEquals(1, listener1.onNextPieceCount);
        assertEquals(1, listener2.onNextPieceCount);
        assertSame(nextPiece, listener1.lastNextPiece);
        assertSame(nextPiece, listener2.lastNextPiece);
    }

    @Test
    @DisplayName("onScoreChanged: 모든 리스너에게 점수 전달")
    void testOnScoreChanged() {
        composite.add(listener1);
        composite.add(listener2);

        composite.onScoreChanged(1500);

        assertEquals(1, listener1.onScoreChangedCount);
        assertEquals(1, listener2.onScoreChangedCount);
        assertEquals(1500, listener1.lastScore);
        assertEquals(1500, listener2.lastScore);
    }

    // 예외 처리 테스트
    @Test
    @DisplayName("예외 발생 리스너가 있어도 다른 리스너들은 계속 실행됨")
    void testExceptionHandling() {
        composite.add(listener1);
        composite.add(listener2); // 예외 발생 예정
        composite.add(listener3);

        // listener2에서 예외 발생하도록 설정
        listener2.throwException = true;

        // System.err 캡처 (printStackTrace 출력 확인)
        ByteArrayOutputStream errStream = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errStream));

        try {
            composite.onScoreChanged(750);

            // listener1과 listener3는 정상 실행됨
            assertEquals(1, listener1.onScoreChangedCount);
            assertEquals(1, listener3.onScoreChangedCount);
            assertEquals(750, listener1.lastScore);
            assertEquals(750, listener3.lastScore);

            // listener2도 예외 발생 전까지는 실행됨
            assertEquals(1, listener2.onScoreChangedCount);
            assertEquals(750, listener2.lastScore);

            // printStackTrace가 호출되었는지 확인
            String errorOutput = errStream.toString();
            assertTrue(errorOutput.contains("Test Exception"));

        } finally {
            System.setErr(originalErr);
        }
    }

    @Test
    @DisplayName("모든 리스너에서 예외 발생해도 전체 처리는 완료됨")
    void testAllListenersThrowException() {
        composite.add(listener1);
        composite.add(listener2);
        composite.add(listener3);

        // 모든 리스너에서 예외 발생하도록 설정
        listener1.throwException = true;
        listener2.throwException = true;
        listener3.throwException = true;

        ByteArrayOutputStream errStream = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errStream));

        try {
            // 예외 발생해도 composite 메서드는 정상 완료됨
            assertDoesNotThrow(() -> composite.onGameOver());

            // 모든 리스너가 호출됨
            assertEquals(1, listener1.onGameOverCount);
            assertEquals(1, listener2.onGameOverCount);
            assertEquals(1, listener3.onGameOverCount);

            // 세 번의 printStackTrace 출력 확인
            String errorOutput = errStream.toString();
            long exceptionCount =
                    errorOutput.lines().filter(line -> line.contains("Test Exception")).count();
            assertEquals(3, exceptionCount);

        } finally {
            System.setErr(originalErr);
        }
    }

    // 엣지 케이스 테스트

    @Test
    @DisplayName("리스너가 없을 때 이벤트 호출해도 예외 발생하지 않음")
    void testEmptyListeners() {
        assertDoesNotThrow(() -> composite.onBoardUpdated(new Board(10, 20)));
        assertDoesNotThrow(() -> composite.onPieceSpawned(new Tetromino(Tetromino.Kind.O), 4, 1));
        assertDoesNotThrow(() -> composite.onLinesCleared(2));
        assertDoesNotThrow(() -> composite.onGameOver());
        assertDoesNotThrow(() -> composite.onNextPiece(new Tetromino(Tetromino.Kind.S)));
        assertDoesNotThrow(() -> composite.onScoreChanged(1000));
    }

    @Test
    @DisplayName("동일한 리스너 중복 추가 가능")
    void testDuplicateListeners() {
        composite.add(listener1);
        composite.add(listener1); // 중복 추가

        composite.onScoreChanged(600);

        // 두 번 호출됨
        assertEquals(2, listener1.onScoreChangedCount);
    }

    @Test
    @DisplayName("동시성 테스트: 이벤트 전달 중 리스너 추가/제거")
    void testConcurrentModification() {
        composite.add(listener1);
        composite.add(listener2);

        // CopyOnWriteArrayList 사용으로 동시 수정 안전
        assertDoesNotThrow(
                () -> {
                    composite.add(listener3); // 이벤트 전달 중 추가
                    composite.onScoreChanged(800);
                    composite.remove(listener1); // 이벤트 전달 중 제거
                });

        // listener3는 추가 후 이벤트를 받음
        assertEquals(1, listener3.onScoreChangedCount);
        assertEquals(800, listener3.lastScore);
    }

    @Test
    @DisplayName("리스너 순서대로 이벤트 전달됨")
    void testListenerOrder() {
        // 호출 순서를 기록하는 리스너
        StringBuilder callOrder = new StringBuilder();

        GameStateListener orderedListener1 =
                new GameStateListener() {
                    @Override
                    public void onBoardUpdated(Board board) {}

                    @Override
                    public void onPieceSpawned(Tetromino tetromino, int px, int py) {}

                    @Override
                    public void onLinesCleared(int lines) {}

                    @Override
                    public void onGameOver() {}

                    @Override
                    public void onNextPiece(Tetromino next) {}

                    @Override
                    public void onScoreChanged(int score) {
                        callOrder.append("1");
                    }
                };

        GameStateListener orderedListener2 =
                new GameStateListener() {
                    @Override
                    public void onBoardUpdated(Board board) {}

                    @Override
                    public void onPieceSpawned(Tetromino tetromino, int px, int py) {}

                    @Override
                    public void onLinesCleared(int lines) {}

                    @Override
                    public void onGameOver() {}

                    @Override
                    public void onNextPiece(Tetromino next) {}

                    @Override
                    public void onScoreChanged(int score) {
                        callOrder.append("2");
                    }
                };

        GameStateListener orderedListener3 =
                new GameStateListener() {
                    @Override
                    public void onBoardUpdated(Board board) {}

                    @Override
                    public void onPieceSpawned(Tetromino tetromino, int px, int py) {}

                    @Override
                    public void onLinesCleared(int lines) {}

                    @Override
                    public void onGameOver() {}

                    @Override
                    public void onNextPiece(Tetromino next) {}

                    @Override
                    public void onScoreChanged(int score) {
                        callOrder.append("3");
                    }
                };

        composite.add(orderedListener1);
        composite.add(orderedListener2);
        composite.add(orderedListener3);

        composite.onScoreChanged(900);

        assertEquals("123", callOrder.toString());
    }
}
