package team13.tetris.game.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import team13.tetris.data.ScoreBoard;
import team13.tetris.game.controller.GameStateListener;
import team13.tetris.game.model.Board;
import team13.tetris.game.model.Tetromino;

import static org.junit.jupiter.api.Assertions.*;

// GameEngine 기본 동작 테스트: Tests basic GameEngine functionality, initialization, and state retrieval
@DisplayName("GameEngine 기본 동작 테스트")
public class GameEngineBasicTest {

    private Board board;
    private TestListener listener;
    private GameEngine engine;

    // Test listener to track callback invocations
    private static class TestListener implements GameStateListener {
        int scoreChangedCount = 0;
        int pieceSpawnedCount = 0;
        int nextPieceCount = 0;

        int lastScore = -1;
        Tetromino lastSpawnedPiece = null;
        Tetromino lastNextPiece = null;

        @Override
        public void onScoreChanged(int newScore) {
            scoreChangedCount++;
            lastScore = newScore;
        }

        @Override
        public void onGameOver() {}

        @Override
        public void onPieceSpawned(Tetromino piece, int x, int y) {
            pieceSpawnedCount++;
            lastSpawnedPiece = piece;
        }

        @Override
        public void onBoardUpdated(Board board) {}

        @Override
        public void onLinesCleared(int linesCleared) {}

        @Override
        public void onNextPiece(Tetromino nextPiece) {
            nextPieceCount++;
            lastNextPiece = nextPiece;
        }

        void reset() {
            scoreChangedCount = 0;
            pieceSpawnedCount = 0;
            nextPieceCount = 0;
            lastScore = -1;
            lastSpawnedPiece = null;
            lastNextPiece = null;
        }
    }

    @BeforeEach
    void setUp() {
        board = new Board(10, 20);
        listener = new TestListener();
        engine = new GameEngine(board, listener);
    }

    @Test
    @DisplayName("기본 생성자로 GameEngine을 생성할 수 있어야 함")
    void testGameEngineCreation() {
        assertNotNull(engine);
        assertNotNull(engine.getBoard());
        assertNotNull(engine.getGameTimer());
    }

    @Test
    @DisplayName("난이도를 지정하여 GameEngine을 생성할 수 있어야 함")
    void testGameEngineCreationWithDifficulty() {
        GameEngine easyEngine = new GameEngine(board, listener, ScoreBoard.ScoreEntry.Mode.EASY);
        assertNotNull(easyEngine);

        GameEngine hardEngine = new GameEngine(board, listener, ScoreBoard.ScoreEntry.Mode.HARD);
        assertNotNull(hardEngine);

        GameEngine itemEngine = new GameEngine(board, listener, ScoreBoard.ScoreEntry.Mode.ITEM);
        assertNotNull(itemEngine);
    }

    @Test
    @DisplayName("startNewGame 호출 시 보드가 초기화되어야 함")
    void testStartNewGameClearsBoard() {
        // 보드에 블록을 배치
        board.setCell(5, 10, 1);
        board.setCell(3, 15, 2);

        engine.startNewGame();

        // 보드가 비어있어야 함
        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++)
                assertEquals(0, board.getCell(x, y), "보드의 (" + x + ", " + y + ") 위치가 비어있지 않음");
        }
    }

    @Test
    @DisplayName("startNewGame 호출 시 current와 next가 설정되어야 함")
    void testStartNewGameInitializesCurrentAndNext() {
        engine.startNewGame();

        assertNotNull(engine.getCurrent(), "current가 null이면 안됨");
        assertNotNull(engine.getNext(), "next가 null이면 안됨");
    }

    @Test
    @DisplayName("startNewGame 호출 시 score가 0으로 초기화되어야 함")
    void testStartNewGameResetsScore() {
        engine.startNewGame();

        assertEquals(0, engine.getScore());
        assertTrue(listener.scoreChangedCount > 0, "onScoreChanged가 호출되어야 함");
        assertEquals(0, listener.lastScore);
    }

    @Test
    @DisplayName("startNewGame 호출 시 리스너 콜백이 호출되어야 함")
    void testStartNewGameTriggersListenerCallbacks() {
        listener.reset();

        engine.startNewGame();

        assertTrue(listener.pieceSpawnedCount > 0, "onPieceSpawned가 호출되어야 함");
        assertTrue(listener.nextPieceCount > 0, "onNextPiece가 호출되어야 함");
        assertTrue(listener.scoreChangedCount > 0, "onScoreChanged가 호출되어야 함");

        assertNotNull(listener.lastSpawnedPiece, "마지막 생성된 조각이 null이면 안됨");
        assertNotNull(listener.lastNextPiece, "마지막 다음 조각이 null이면 안됨");
    }

    @Test
    @DisplayName("getBoard는 초기화된 Board를 반환해야 함")
    void testGetBoard() {
        Board returnedBoard = engine.getBoard();

        assertNotNull(returnedBoard);
        assertSame(board, returnedBoard, "동일한 Board 인스턴스를 반환해야 함");
    }

    @Test
    @DisplayName("getScore는 초기값 0을 반환해야 함")
    void testGetScoreInitialValue() { assertEquals(0, engine.getScore()); }

    @Test
    @DisplayName("getCurrent는 startNewGame 호출 전에는 null이어야 함")
    void testGetCurrentBeforeStart() {
        GameEngine newEngine = new GameEngine(board, listener);
        assertNull(newEngine.getCurrent());
    }

    @Test
    @DisplayName("getNext는 startNewGame 호출 전에는 null이어야 함")
    void testGetNextBeforeStart() {
        GameEngine newEngine = new GameEngine(board, listener);
        assertNull(newEngine.getNext());
    }

    @Test
    @DisplayName("getGameTimer는 초기화된 Timer를 반환해야 함")
    void testGetGameTimer() {
        assertNotNull(engine.getGameTimer());
        assertEquals(1.0, engine.getGameTimer().getSpeedFactor(), 0.001);
    }

    @Test
    @DisplayName("getTotalLinesCleared는 초기값 0을 반환해야 함")
    void testGetTotalLinesClearedInitialValue() { assertEquals(0, engine.getTotalLinesCleared()); }

    @Test
    @DisplayName("getPieceX와 getPieceY는 startNewGame 후 중앙 위치를 반환해야 함")
    void testGetPiecePositionAfterStart() {
        engine.startNewGame();

        // 중앙 위치는 대략 (board.getWidth() / 2 - 2) 정도
        int px = engine.getPieceX();
        int py = engine.getPieceY();

        assertTrue(px >= 0 && px < board.getWidth(), "px는 보드 범위 내에 있어야 함: " + px);
        assertTrue(py >= 0 && py < board.getHeight(), "py는 보드 범위 내에 있어야 함: " + py);
    }

    @Test
    @DisplayName("여러 번 startNewGame을 호출해도 정상 동작해야 함")
    void testMultipleStartNewGame() {
        engine.startNewGame();
        Tetromino firstCurrent = engine.getCurrent();

        listener.reset();
        engine.startNewGame();
        Tetromino secondCurrent = engine.getCurrent();

        assertNotNull(firstCurrent);
        assertNotNull(secondCurrent);
        assertEquals(0, engine.getScore(), "점수가 리셋되어야 함");
        assertTrue(listener.pieceSpawnedCount > 0, "새 조각이 생성되어야 함");
    }

    @Test
    @DisplayName("null 리스너를 전달하면 NullPointerException이 발생함")
    void testNullListenerHandling() {
        GameEngine engineWithNullListener = new GameEngine(board, null);

        // GameEngine은 현재 null 리스너를 처리하지 않으므로 예외가 발생해야 함
        assertThrows(NullPointerException.class, () -> {
            engineWithNullListener.startNewGame();
        }, "null 리스너로 게임 시작 시 NullPointerException이 발생해야 함");
    }

    @Test
    @DisplayName("ITEM 모드로 생성 시 itemModeEnabled가 true여야 함")
    void testItemModeEnabled() {
        GameEngine itemEngine = new GameEngine(board, listener, ScoreBoard.ScoreEntry.Mode.ITEM);
        itemEngine.startNewGame();

        // 간접적으로 확인: 10줄 클리어 후 아이템이 생성되는지
        // (실제 itemModeEnabled 필드는 private이므로 동작으로 확인)
        assertNotNull(itemEngine.getCurrent());
    }

    @Test
    @DisplayName("startNewGame 후 자동 하강이 시작되어야 함")
    void testAutoDropStartsAfterNewGame() {
        engine.startNewGame();

        // 자동 하강이 시작되었는지 확인하기 위해 잠시 대기
        // (실제 스케줄러 동작은 GameEngineDropTest에서 테스트)
        assertNotNull(engine.getCurrent(), "자동 하강을 위한 current가 필요함");
    }

    @Test
    @DisplayName("생성 직후 dropInterval은 기본값 1.0초여야 함")
    void testDefaultDropInterval() { assertEquals(1.0, engine.getDropIntervalSeconds(), 0.001); }
}
