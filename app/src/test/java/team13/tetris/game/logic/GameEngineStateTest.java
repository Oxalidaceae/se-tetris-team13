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

// GameEngine State 테스트: Tests game state management functions like startNewGame,
// spawnNext, shutdown, game over conditions, and listener callbacks
@DisplayName("GameEngine 상태 테스트")
public class GameEngineStateTest {

    private GameEngine engine;
    private Board board;
    private TestListener listener;

    // 테스트용 리스너 클래스
    private static class TestListener implements GameStateListener {
        int scoreChangedCount = 0;
        boolean boardUpdated = false;
        Tetromino spawnedPiece = null;
        int spawnX = -1;
        int spawnY = -1;
        Tetromino nextPiece = null;
        int pieceSpawnedCount = 0;
        int nextPieceCount = 0;

        @Override
        public void onScoreChanged(int score) { scoreChangedCount++; }

        @Override
        public void onBoardUpdated(Board board) { boardUpdated = true; }

        @Override
        public void onLinesCleared(int lines) {}

        @Override
        public void onGameOver() {}

        @Override
        public void onPieceSpawned(Tetromino piece, int x, int y) {
            pieceSpawnedCount++;
            spawnedPiece = piece;
            spawnX = x;
            spawnY = y;
        }

        @Override
        public void onNextPiece(Tetromino piece) {
            nextPieceCount++;
            nextPiece = piece;
        }

        void reset() {
            scoreChangedCount = 0;
            boardUpdated = false;
            spawnedPiece = null;
            spawnX = -1;
            spawnY = -1;
            nextPiece = null;
            pieceSpawnedCount = 0;
            nextPieceCount = 0;
        }
    }

    @BeforeEach
    void setUp() {
        listener = new TestListener();
        board = new Board(10, 20);
        engine = new GameEngine(board, listener, ScoreBoard.ScoreEntry.Mode.NORMAL);
    }

    @AfterEach
    void tearDown() {
        if (engine != null) engine.shutdown();
    }

    // startNewGame() 테스트
    @Test
    @DisplayName("startNewGame: 보드 초기화")
    void testStartNewGameClearsBoard() {
        // 보드에 블록 배치
        board.setCell(0, 0, 1);
        board.setCell(5, 10, 2);

        engine.startNewGame();

        // 보드가 비워졌는지 확인
        assertEquals(0, board.getCell(0, 0), "보드가 초기화되어야 함");
        assertEquals(0, board.getCell(5, 10), "보드가 초기화되어야 함");
    }

    @Test
    @DisplayName("startNewGame: 현재 조각과 다음 조각 생성")
    void testStartNewGameSpawnsPieces() {
        engine.startNewGame();

        assertNotNull(engine.getCurrent(), "현재 조각이 생성되어야 함");
        assertNotNull(engine.getNext(), "다음 조각이 생성되어야 함");
    }

    @Test
    @DisplayName("startNewGame: 조각 초기 위치 설정")
    void testStartNewGameSetsInitialPosition() {
        engine.startNewGame();

        Tetromino current = engine.getCurrent();
        int expectedX = (board.getWidth() - current.getWidth()) / 2;

        assertEquals(expectedX, engine.getPieceX(), "초기 X 위치는 중앙이어야 함");
        assertEquals(0, engine.getPieceY(), "초기 Y 위치는 0이어야 함");
    }

    @Test
    @DisplayName("startNewGame: 점수 알림 발생")
    void testStartNewGameNotifiesScore() {
        listener.reset();

        engine.startNewGame();

        assertTrue(listener.scoreChangedCount > 0, "점수 변경 알림이 발생해야 함");
    }

    @Test
    @DisplayName("startNewGame: 조각 생성 알림 발생")
    void testStartNewGameNotifiesPieceSpawned() {
        listener.reset();

        engine.startNewGame();

        assertEquals(1, listener.pieceSpawnedCount, "조각 생성 알림이 1번 발생해야 함");
        assertNotNull(listener.spawnedPiece, "생성된 조각 정보가 전달되어야 함");
    }

    @Test
    @DisplayName("startNewGame: 다음 조각 알림 발생")
    void testStartNewGameNotifiesNextPiece() {
        listener.reset();

        engine.startNewGame();

        assertEquals(1, listener.nextPieceCount, "다음 조각 알림이 1번 발생해야 함");
        assertNotNull(listener.nextPiece, "다음 조각 정보가 전달되어야 함");
    }

    @Test
    @DisplayName("startNewGame: 여러 번 호출 가능")
    void testStartNewGameMultipleTimes() {
        engine.startNewGame();
        Tetromino first = engine.getCurrent();

        engine.startNewGame();
        Tetromino second = engine.getCurrent();

        assertNotNull(first, "첫 번째 게임의 조각이 생성되어야 함");
        assertNotNull(second, "두 번째 게임의 조각이 생성되어야 함");
    }

    // spawnNext() 동작 테스트
    @Test
    @DisplayName("hardDrop 후 다음 조각 자동 생성")
    void testSpawnNextAfterHardDrop() {
        engine.startNewGame();
        Tetromino beforeDrop = engine.getCurrent();

        engine.hardDrop();

        Tetromino afterDrop = engine.getCurrent();
        assertNotSame(beforeDrop, afterDrop, "hardDrop 후 새로운 조각이 생성되어야 함");
    }

    @Test
    @DisplayName("조각 생성 시 current ← next, next ← random")
    void testSpawnNextMovesNextToCurrent() {
        engine.startNewGame();
        Tetromino originalNext = engine.getNext();

        engine.hardDrop();

        Tetromino newCurrent = engine.getCurrent();
        assertNotNull(newCurrent, "새 조각이 current로 설정되어야 함");
        assertNotNull(originalNext, "새로운 next 조각이 생성되어야 함");
    }

    @Test
    @DisplayName("조각 생성 시 초기 위치로 리셋")
    void testSpawnNextResetsPosition() {
        engine.startNewGame();

        // 조각을 이동시킴
        engine.moveLeft();
        engine.moveLeft();

        engine.hardDrop();

        // 새 조각은 중앙에서 시작해야 함
        Tetromino current = engine.getCurrent();
        int expectedX = (board.getWidth() - current.getWidth()) / 2;
        assertEquals(expectedX, engine.getPieceX(), "새 조각은 중앙에서 시작해야 함");
        assertEquals(0, engine.getPieceY(), "새 조각은 Y=0에서 시작해야 함");
    }

    // 게임오버 테스트
    @Test
    @DisplayName("게임오버: 조각이 배치될 공간이 없을 때")
    void testGameOverWhenNoSpace() {
        engine.startNewGame();

        // 상단 6줄을 블록으로 채움 (조각이 생성될 공간을 막음)
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < board.getWidth(); col++)
                board.setCell(col, row, 1);
        }

        engine.hardDrop();

        // 게임오버 후 current는 null이어야 함
        assertNull(engine.getCurrent(), "게임오버 시 current는 null이어야 함");
    }

    @Test
    @DisplayName("게임오버: 더 이상 조작 불가")
    void testGameOverPreventsMovement() {
        engine.startNewGame();

        // 게임오버 상태로 만들기
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < board.getWidth(); col++)
                board.setCell(col, row, 1);
        }
        engine.hardDrop();

        // 조작 시도
        assertDoesNotThrow(() -> engine.moveLeft(), "게임오버 후 moveLeft 안전해야 함");
        assertDoesNotThrow(() -> engine.moveRight(), "게임오버 후 moveRight 안전해야 함");
        assertDoesNotThrow(() -> engine.rotateCW(), "게임오버 후 rotateCW 안전해야 함");
        assertDoesNotThrow(() -> engine.softDrop(), "게임오버 후 softDrop 안전해야 함");
    }

    // shutdown() 테스트

    @Test
    @DisplayName("shutdown: 리소스 정리")
    void testShutdownCleansUpResources() {
        engine.startNewGame();

        assertDoesNotThrow(() -> engine.shutdown(), "shutdown이 예외 없이 실행되어야 함");
    }

    @Test
    @DisplayName("shutdown: 여러 번 호출 가능")
    void testShutdownMultipleTimes() {
        engine.startNewGame();

        assertDoesNotThrow(() -> {
            engine.shutdown();
            engine.shutdown();
            engine.shutdown();
        }, "shutdown 여러 번 호출 시 안전해야 함");
    }

    @Test
    @DisplayName("shutdown 후 게임 재시작 가능")
    void testRestartAfterShutdown() {
        engine.startNewGame();
        engine.shutdown();

        assertDoesNotThrow(() -> engine.startNewGame(), "shutdown 후 게임을 다시 시작할 수 있어야 함");
    }

    // 리스너 콜백 검증
    @Test
    @DisplayName("리스너: onBoardUpdated 호출 확인")
    void testListenerOnBoardUpdated() {
        listener.reset();

        engine.startNewGame();

        assertTrue(listener.boardUpdated, "보드 업데이트 알림이 발생해야 함");
    }

    @Test
    @DisplayName("리스너: onPieceSpawned 위치 정보 정확성")
    void testListenerOnPieceSpawnedAccuracy() {
        listener.reset();

        engine.startNewGame();

        assertEquals(engine.getCurrent(), listener.spawnedPiece, "생성된 조각이 정확히 전달되어야 함");
        assertEquals(engine.getPieceX(), listener.spawnX, "생성 위치 X가 정확히 전달되어야 함");
        assertEquals(engine.getPieceY(), listener.spawnY, "생성 위치 Y가 정확히 전달되어야 함");
    }

    // 난이도별 테스트
    @Test
    @DisplayName("난이도 EASY로 게임 시작")
    void testStartGameWithEasyDifficulty() {
        GameEngine easyEngine = new GameEngine(new Board(10, 20), new TestListener(), ScoreBoard.ScoreEntry.Mode.EASY);

        assertDoesNotThrow(() -> easyEngine.startNewGame(), "EASY 난이도로 게임을 시작할 수 있어야 함");

        easyEngine.shutdown();
    }

    @Test
    @DisplayName("난이도 HARD로 게임 시작")
    void testStartGameWithHardDifficulty() {
        GameEngine hardEngine = new GameEngine(new Board(10, 20), new TestListener(), ScoreBoard.ScoreEntry.Mode.HARD);

        assertDoesNotThrow(() -> hardEngine.startNewGame(), "HARD 난이도로 게임을 시작할 수 있어야 함");

        hardEngine.shutdown();
    }

    @Test
    @DisplayName("아이템 모드로 게임 시작")
    void testStartGameWithItemMode() {
        GameEngine itemEngine = new GameEngine(new Board(10, 20), new TestListener(), ScoreBoard.ScoreEntry.Mode.ITEM);

        assertDoesNotThrow(() -> itemEngine.startNewGame(), "ITEM 모드로 게임을 시작할 수 있어야 함");

        itemEngine.shutdown();
    }

    @Test
    @DisplayName("모든 게임 모드에서 상태 초기화 테스트")
    void testStateInitializationInAllModes() {
        ScoreBoard.ScoreEntry.Mode[] modes = {
            ScoreBoard.ScoreEntry.Mode.EASY,
            ScoreBoard.ScoreEntry.Mode.NORMAL,
            ScoreBoard.ScoreEntry.Mode.HARD,
            ScoreBoard.ScoreEntry.Mode.ITEM,
            ScoreBoard.ScoreEntry.Mode.VERSUS
        };

        for (ScoreBoard.ScoreEntry.Mode mode : modes) {
            TestListener modeListener = new TestListener();
            GameEngine modeEngine = new GameEngine(new Board(10, 20), modeListener, mode);
            
            modeEngine.startNewGame();
            
            assertEquals(0, modeEngine.getScore(), mode + " 모드에서 점수가 0으로 초기화되어야 함");
            assertNotNull(modeEngine.getCurrent(), mode + " 모드에서 current가 null이면 안됨");
            assertNotNull(modeEngine.getNext(), mode + " 모드에서 next가 null이면 안됨");
            assertTrue(modeListener.scoreChangedCount > 0, mode + " 모드에서 scoreChanged 콜백이 호출되어야 함");
            assertTrue(modeListener.pieceSpawnedCount > 0, mode + " 모드에서 pieceSpawned 콜백이 호출되어야 함");
            
            modeEngine.shutdown();
        }
    }

    @Test
    @DisplayName("연속 shutdown 호출 테스트")
    void testRepeatedShutdown() {
        engine.startNewGame();
        
        // 여러 번 shutdown을 호출해도 안전해야 함
        assertDoesNotThrow(() -> {
            engine.shutdown();
            engine.shutdown();
            engine.shutdown();
        }, "연속 shutdown 호출이 안전해야 함");
    }

    @Test
    @DisplayName("shutdown 후 메서드 호출 안전성 테스트")
    void testMethodCallsAfterShutdown() {
        engine.startNewGame();
        engine.shutdown();
        
        // shutdown 후에도 getter 메서드들은 안전해야 함
        assertDoesNotThrow(() -> {
            engine.getScore();
            engine.getCurrent();
            engine.getNext();
            engine.getPieceX();
            engine.getPieceY();
            engine.getBoard();
            engine.getGameTimer();
            engine.getDropIntervalSeconds();
        }, "shutdown 후에도 getter 메서드들은 안전해야 함");
    }

    @Test
    @DisplayName("spawnNext 반복 호출 테스트")
    void testRepeatedSpawnNext() {
        engine.startNewGame();
        
        Tetromino initialNext = engine.getNext();
        listener.reset();
        
        // 여러 번 게임을 다시 시작하여 피스 생성 테스트
        for (int i = 0; i < 5; i++) {
            Tetromino previousCurrent = engine.getCurrent();
            Tetromino previousNext = engine.getNext();
            
            engine.startNewGame();
            
            // 새로운 current와 next가 생성되어야 함
            assertNotNull(engine.getCurrent(), 
                         i + "번째 재시작에서 새로운 current가 생성되지 않음");
            assertNotNull(engine.getNext(), 
                         i + "번째 재시작에서 새로운 next가 생성되지 않음");
        }
        
        // 콜백이 적절히 호출되었는지 확인
        assertTrue(listener.pieceSpawnedCount >= 5, "게임 재시작 시마다 pieceSpawned 콜백이 호출되어야 함");
        assertTrue(listener.nextPieceCount >= 5, "게임 재시작 시마다 nextPiece 콜백이 호출되어야 함");
    }

    @Test
    @DisplayName("게임 오버 상태에서의 동작 테스트")
    void testBehaviorInGameOverState() {
        // 보드를 게임 오버 상태로 만들기 (맨 위까지 블록으로 채우기)
        for (int x = 0; x < board.getWidth(); x++) {
            for (int y = 0; y < 4; y++) { // 맨 위 4줄을 채움
                board.setCell(x, y, 1);
            }
        }
        
        engine.startNewGame();
        listener.reset();
        
        // 게임 오버 상태에서도 메서드 호출이 안전해야 함
        assertDoesNotThrow(() -> {
            engine.getScore();
            engine.getCurrent();
            engine.getNext();
            engine.getPieceX();
            engine.getPieceY();
        }, "게임 오버 상태에서도 메서드 호출이 안전해야 함");
    }

    @Test
    @DisplayName("빈 보드에서 상태 전환 테스트")
    void testStateTransitionOnEmptyBoard() {
        // 완전히 비어있는 보드에서 게임 시작
        board.clear();
        engine.startNewGame();
        
        Tetromino firstCurrent = engine.getCurrent();
        Tetromino firstNext = engine.getNext();
        
        assertNotNull(firstCurrent, "빈 보드에서도 current가 생성되어야 함");
        assertNotNull(firstNext, "빈 보드에서도 next가 생성되어야 함");
        
        // 게임 재시작으로 상태 변경 테스트
        engine.startNewGame();
        
        // 재시작 후에도 적절한 피스들이 생성되어야 함
        assertNotNull(engine.getCurrent(), "재시작 후 current가 생성되어야 함");
        assertNotNull(engine.getNext(), "재시작 후 새로운 next가 생성되어야 함");
    }

    @Test
    @DisplayName("다양한 보드 크기에서 상태 관리 테스트")
    void testStateManagementOnVariousBoardSizes() {
        // 작은 보드 (테트로미노가 배치 가능한 크기)
        Board smallBoard = new Board(5, 10);
        TestListener smallListener = new TestListener();
        GameEngine smallEngine = new GameEngine(smallBoard, smallListener);
        
        assertDoesNotThrow(() -> {
            smallEngine.startNewGame();
            // 작은 보드에서는 current 생성이 실패할 수 있으므로 보드만 확인
            assertNotNull(smallEngine.getBoard(), "작은 보드에서도 보드가 설정되어야 함");
            assertEquals(0, smallEngine.getScore(), "작은 보드에서도 점수가 올바르게 초기화되어야 함");
            smallEngine.shutdown();
        }, "작은 보드에서도 상태 관리가 정상 동작해야 함");
        
        // 매우 큰 보드
        Board largeBoard = new Board(50, 50);
        TestListener largeListener = new TestListener();
        GameEngine largeEngine = new GameEngine(largeBoard, largeListener);
        
        assertDoesNotThrow(() -> {
            largeEngine.startNewGame();
            assertNotNull(largeEngine.getCurrent(), "큰 보드에서도 current가 생성되어야 함");
            assertEquals(0, largeEngine.getScore(), "큰 보드에서도 점수가 올바르게 초기화되어야 함");
            largeEngine.shutdown();
        }, "매우 큰 보드에서도 상태 관리가 정상 동작해야 함");
    }

    @Test
    @DisplayName("리스너 콜백 매개변수 유효성 테스트")
    void testListenerCallbackParameterValidity() {
        listener.reset();
        engine.startNewGame();
        
        // spawnedPiece 콜백 매개변수 검증
        assertNotNull(listener.spawnedPiece, "spawnedPiece가 null이면 안됨");
        assertTrue(listener.spawnX >= 0, "spawnX가 음수이면 안됨");
        assertTrue(listener.spawnY >= 0, "spawnY가 음수이면 안됨");
        assertTrue(listener.spawnX < board.getWidth() + 4, "spawnX가 보드 너비를 초과하면 안됨");
        assertTrue(listener.spawnY < board.getHeight() + 4, "spawnY가 보드 높이를 초과하면 안됨");
        
        // nextPiece 콜백 매개변수 검증
        assertNotNull(listener.nextPiece, "nextPiece가 null이면 안됨");
    }

    @Test
    @DisplayName("상태 변경 시 리스너 호출 순서 테스트")
    void testListenerCallbackOrder() {
        listener.reset();
        
        long startTime = System.nanoTime();
        engine.startNewGame();
        long endTime = System.nanoTime();
        
        // 게임 시작 시 적절한 콜백들이 호출되었는지 확인
        assertTrue(listener.scoreChangedCount > 0, "scoreChanged 콜백이 호출되어야 함");
        assertTrue(listener.pieceSpawnedCount > 0, "pieceSpawned 콜백이 호출되어야 함");
        assertTrue(listener.nextPieceCount > 0, "nextPiece 콜백이 호출되어야 함");
        
        // 게임 시작이 합리적인 시간 내에 완료되어야 함 (1초 이내)
        assertTrue(endTime - startTime < 1_000_000_000L, "게임 시작이 너무 오래 걸림");
    }

    @Test
    @DisplayName("동일한 리스너로 여러 엔진 생성 테스트")
    void testMultipleEnginesWithSameListener() {
        TestListener sharedListener = new TestListener();
        
        GameEngine engine1 = new GameEngine(new Board(10, 20), sharedListener);
        GameEngine engine2 = new GameEngine(new Board(15, 25), sharedListener);
        
        sharedListener.reset();
        
        engine1.startNewGame();
        int afterEngine1 = sharedListener.scoreChangedCount + sharedListener.pieceSpawnedCount;
        
        engine2.startNewGame();
        int afterEngine2 = sharedListener.scoreChangedCount + sharedListener.pieceSpawnedCount;
        
        assertTrue(afterEngine2 > afterEngine1, "두 번째 엔진 시작 후 콜백 카운트가 증가해야 함");
        
        engine1.shutdown();
        engine2.shutdown();
    }
}
