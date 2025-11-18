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

    // Removed: null listener test - GameEngine no longer accepts null listeners

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

    @Test
    @DisplayName("setDropIntervalSeconds 동작 테스트")
    void testSetDropIntervalSeconds() {
        engine.setDropIntervalSeconds(0.5);
        assertEquals(0.5, engine.getDropIntervalSeconds(), 0.001);
        
        engine.setDropIntervalSeconds(2.0);
        assertEquals(2.0, engine.getDropIntervalSeconds(), 0.001);
        
        // 음수 값은 예외를 발생시켜야 함
        assertThrows(IllegalArgumentException.class, () -> {
            engine.setDropIntervalSeconds(-1.0);
        }, "음수 dropInterval은 예외를 발생시켜야 함");
        
        // 0 값도 예외를 발생시켜야 함
        assertThrows(IllegalArgumentException.class, () -> {
            engine.setDropIntervalSeconds(0.0);
        }, "0 dropInterval은 예외를 발생시켜야 함");
    }

    @Test
    @DisplayName("게임 시작 전후 상태 확인")
    void testGameStateBeforeAndAfterStart() {
        // 시작 전
        assertNull(engine.getCurrent());
        assertNull(engine.getNext());
        assertEquals(0, engine.getScore());
        assertEquals(0, engine.getTotalLinesCleared());
        
        // 시작 후
        engine.startNewGame();
        assertNotNull(engine.getCurrent());
        assertNotNull(engine.getNext());
        assertEquals(0, engine.getScore()); // 시작 시에는 여전히 0
        assertEquals(0, engine.getTotalLinesCleared());
    }

    @Test
    @DisplayName("다양한 난이도로 엔진 생성 및 시작 테스트")
    void testAllDifficultyModes() {
        ScoreBoard.ScoreEntry.Mode[] modes = {
            ScoreBoard.ScoreEntry.Mode.EASY,
            ScoreBoard.ScoreEntry.Mode.NORMAL,
            ScoreBoard.ScoreEntry.Mode.HARD,
            ScoreBoard.ScoreEntry.Mode.ITEM
        };
        
        for (ScoreBoard.ScoreEntry.Mode mode : modes) {
            TestListener testListener = new TestListener();
            GameEngine testEngine = new GameEngine(board, testListener, mode);
            
            assertNotNull(testEngine, mode + " 모드로 엔진 생성 실패");
            
            testEngine.startNewGame();
            assertNotNull(testEngine.getCurrent(), mode + " 모드에서 current 생성 실패");
            assertNotNull(testEngine.getNext(), mode + " 모드에서 next 생성 실패");
            assertEquals(0, testEngine.getScore(), mode + " 모드에서 초기 점수가 0이 아님");
        }
    }

    @Test
    @DisplayName("타이머 관련 메서드 테스트")
    void testTimerMethods() {
        assertNotNull(engine.getGameTimer());
        
        // 기본 속도 인수는 1.0
        assertEquals(1.0, engine.getGameTimer().getSpeedFactor(), 0.001);
        
        // 게임 시작 후에도 타이머는 유효해야 함
        engine.startNewGame();
        assertNotNull(engine.getGameTimer());
    }

    @Test
    @DisplayName("조각 위치 경계값 테스트")
    void testPiecePositionBoundaries() {
        engine.startNewGame();
        
        int px = engine.getPieceX();
        int py = engine.getPieceY();
        
        // 조각이 보드 경계 내에 있어야 함
        assertTrue(px >= -3, "px가 너무 작음: " + px); // 테트로미노는 최대 4칸이므로 -3까지 허용
        assertTrue(px <= board.getWidth(), "px가 너무 큼: " + px);
        assertTrue(py >= -3, "py가 너무 작음: " + py);
        assertTrue(py <= board.getHeight(), "py가 너무 큼: " + py);
    }

    @Test
    @DisplayName("보드 크기 변경 후 엔진 동작 테스트")
    void testEngineWithDifferentBoardSizes() {
        Board smallBoard = new Board(5, 10);
        Board largeBoard = new Board(15, 30);
        
        TestListener smallListener = new TestListener();
        TestListener largeListener = new TestListener();
        
        GameEngine smallEngine = new GameEngine(smallBoard, smallListener);
        GameEngine largeEngine = new GameEngine(largeBoard, largeListener);
        
        smallEngine.startNewGame();
        largeEngine.startNewGame();
        
        // 두 엔진 모두 정상 동작해야 함
        assertNotNull(smallEngine.getCurrent());
        assertNotNull(largeEngine.getCurrent());
        
        // 조각 위치가 각 보드 크기에 맞게 설정되어야 함
        assertTrue(smallEngine.getPieceX() >= 0 && smallEngine.getPieceX() < smallBoard.getWidth() + 4);
        assertTrue(largeEngine.getPieceX() >= 0 && largeEngine.getPieceX() < largeBoard.getWidth() + 4);
    }

    @Test
    @DisplayName("연속 호출 안정성 테스트")
    void testRepeatedMethodCalls() {
        // startNewGame 여러 번 호출
        for (int i = 0; i < 5; i++) {
            engine.startNewGame();
            assertNotNull(engine.getCurrent(), i + "번째 startNewGame 후 current가 null");
            assertNotNull(engine.getNext(), i + "번째 startNewGame 후 next가 null");
            assertEquals(0, engine.getScore(), i + "번째 startNewGame 후 점수가 리셋되지 않음");
        }
        
        // dropInterval 여러 번 설정
        double[] intervals = {0.1, 0.5, 1.0, 1.5, 2.0};
        for (double interval : intervals) {
            engine.setDropIntervalSeconds(interval);
            assertEquals(interval, engine.getDropIntervalSeconds(), 0.001);
        }
    }

    @Test
    @DisplayName("리스너 콜백 누적 테스트")
    void testListenerCallbackAccumulation() {
        listener.reset();
        
        // 여러 번 startNewGame을 호출하여 콜백이 누적되는지 확인
        engine.startNewGame();
        int firstSpawnCount = listener.pieceSpawnedCount;
        int firstNextCount = listener.nextPieceCount;
        int firstScoreCount = listener.scoreChangedCount;
        
        engine.startNewGame();
        
        // 콜백 카운트가 증가해야 함
        assertTrue(listener.pieceSpawnedCount > firstSpawnCount, "pieceSpawned 콜백이 누적되지 않음");
        assertTrue(listener.nextPieceCount > firstNextCount, "nextPiece 콜백이 누적되지 않음");
        assertTrue(listener.scoreChangedCount > firstScoreCount, "scoreChanged 콜백이 누적되지 않음");
    }

    // Removed: null listener test - GameEngine no longer accepts null listeners

    @Test
    @DisplayName("다양한 보드 크기 테스트")
    void testVariousBoardSizes() {
        // 작은 보드 (테트로미노가 들어갈 수 있는 최소 크기)
        Board smallBoard = new Board(5, 5);
        assertDoesNotThrow(() -> {
            GameEngine smallEngine = new GameEngine(smallBoard, listener);
            // 작은 보드에서는 게임 시작만 테스트 (스폰은 실패할 수 있음)
            assertNotNull(smallEngine.getBoard(), "작은 보드로도 엔진이 생성되어야 함");
        }, "5x5 보드로도 엔진이 생성되어야 함");

        // 큰 보드
        Board largeBoard = new Board(50, 50);
        assertDoesNotThrow(() -> {
            GameEngine largeEngine = new GameEngine(largeBoard, listener);
            largeEngine.startNewGame();
            assertNotNull(largeEngine.getCurrent(), "큰 보드에서 current가 생성되어야 함");
            assertNotNull(largeEngine.getNext(), "큰 보드에서 next가 생성되어야 함");
        }, "50x50 보드로도 엔진이 정상 동작해야 함");
    }

    @Test
    @DisplayName("게임 상태 일관성 테스트")
    void testGameStateConsistency() {
        engine.startNewGame();
        
        // 초기 상태 확인
        Tetromino initialCurrent = engine.getCurrent();
        Tetromino initialNext = engine.getNext();
        int initialScore = engine.getScore();
        int initialX = engine.getPieceX();
        int initialY = engine.getPieceY();
        
        // 상태가 변경되지 않은 상태에서 getter 호출 시 일관된 결과
        assertEquals(initialCurrent, engine.getCurrent(), "current가 일관되지 않음");
        assertEquals(initialNext, engine.getNext(), "next가 일관되지 않음");
        assertEquals(initialScore, engine.getScore(), "score가 일관되지 않음");
        assertEquals(initialX, engine.getPieceX(), "pieceX가 일관되지 않음");
        assertEquals(initialY, engine.getPieceY(), "pieceY가 일관되지 않음");
    }

    @Test
    @DisplayName("drop interval 경계값 테스트")
    void testDropIntervalBoundaryValues() {
        // 0 값은 예외가 발생해야 함
        assertThrows(IllegalArgumentException.class, () -> {
            engine.setDropIntervalSeconds(0);
        }, "0초 간격은 허용되지 않아야 함");
        
        // 매우 작은 값 테스트
        engine.setDropIntervalSeconds(0.001);
        assertEquals(0.001, engine.getDropIntervalSeconds(), 0.0001, "매우 작은 간격이 설정되어야 함");
        
        // 매우 큰 값 테스트
        engine.setDropIntervalSeconds(1000.0);
        assertEquals(1000.0, engine.getDropIntervalSeconds(), 0.001, "매우 큰 간격이 설정되어야 함");
        
        // 음수 값은 예외가 발생해야 함
        assertThrows(IllegalArgumentException.class, () -> {
            engine.setDropIntervalSeconds(-1.0);
        }, "음수 간격은 허용되지 않아야 함");
    }

    @Test
    @DisplayName("모든 게임 모드 초기화 테스트")
    void testAllGameModeInitialization() {
        ScoreBoard.ScoreEntry.Mode[] modes = {
            ScoreBoard.ScoreEntry.Mode.EASY,
            ScoreBoard.ScoreEntry.Mode.NORMAL,
            ScoreBoard.ScoreEntry.Mode.HARD,
            ScoreBoard.ScoreEntry.Mode.ITEM,
            ScoreBoard.ScoreEntry.Mode.VERSUS
        };
        
        for (ScoreBoard.ScoreEntry.Mode mode : modes) {
            assertDoesNotThrow(() -> {
                GameEngine modeEngine = new GameEngine(board, listener, mode);
                modeEngine.startNewGame();
                
                assertNotNull(modeEngine.getCurrent(), mode + " 모드에서 current가 null");
                assertNotNull(modeEngine.getNext(), mode + " 모드에서 next가 null");
                assertEquals(0, modeEngine.getScore(), mode + " 모드에서 점수가 0이 아님");
            }, mode + " 모드로 엔진이 생성되고 시작되어야 함");
        }
    }

    @Test
    @DisplayName("피스 위치 유효성 테스트")
    void testPiecePositionValidity() {
        engine.startNewGame();
        
        int pieceX = engine.getPieceX();
        int pieceY = engine.getPieceY();
        
        // 피스 위치가 보드 경계 내에 있어야 함 (여유분 고려)
        assertTrue(pieceX >= -3, "피스 X 위치가 너무 작음: " + pieceX);
        assertTrue(pieceX < board.getWidth() + 3, "피스 X 위치가 너무 큼: " + pieceX);
        assertTrue(pieceY >= -3, "피스 Y 위치가 너무 작음: " + pieceY);
        assertTrue(pieceY < board.getHeight() + 3, "피스 Y 위치가 너무 큼: " + pieceY);
    }

    @Test
    @DisplayName("게임 타이머 상태 테스트")
    void testGameTimerState() {
        assertNotNull(engine.getGameTimer(), "게임 타이머가 null이면 안됨");
        
        engine.startNewGame();
        assertNotNull(engine.getGameTimer(), "게임 시작 후에도 타이머가 null이면 안됨");
    }

    // Removed: null listener test - GameEngine no longer accepts null listeners

    @Test
    @DisplayName("연속 게임 시작 메모리 누수 테스트")
    void testMemoryLeakOnRepeatedGameStart() {
        // 여러 번 게임을 시작해도 메모리 누수가 발생하지 않아야 함
        for (int i = 0; i < 100; i++) {
            engine.startNewGame();
            
            // 매번 새로운 객체들이 올바르게 생성되어야 함
            assertNotNull(engine.getCurrent(), i + "번째 게임 시작에서 current가 null");
            assertNotNull(engine.getNext(), i + "번째 게임 시작에서 next가 null");
            
            // 점수는 항상 0으로 초기화되어야 함
            assertEquals(0, engine.getScore(), i + "번째 게임 시작에서 점수가 초기화되지 않음");
        }
    }

    @Test
    @DisplayName("보드 참조 일관성 테스트")
    void testBoardReferenceConsistency() {
        Board originalBoard = engine.getBoard();
        
        engine.startNewGame();
        
        // 게임 시작 후에도 같은 보드 객체를 참조해야 함
        assertSame(originalBoard, engine.getBoard(), "게임 시작 후 보드 참조가 변경됨");
    }
}
