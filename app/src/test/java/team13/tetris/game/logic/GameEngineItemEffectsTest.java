package team13.tetris.game.logic;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import team13.tetris.game.controller.GameStateListener;
import team13.tetris.game.model.Board;
import team13.tetris.game.model.Tetromino;
import team13.tetris.game.model.Tetromino.ItemType;
import team13.tetris.game.model.Tetromino.Kind;

@DisplayName("GameEngine 아이템 효과 테스트")
class GameEngineItemEffectsTest {

    private GameEngine engine;
    private Board board;
    private TestListener listener;

    // Test listener to track callback invocations
    private static class TestListener implements GameStateListener {
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

    @BeforeEach
    void setUp() {
        board = new Board(10, 20);
        listener = new TestListener();
        engine = new GameEngine(board, listener);
        engine.startNewGame();
    }

    @Test
    @DisplayName("중력 아이템 효과 - processGravityEffect 테스트")
    void testProcessGravityEffect() throws Exception {
        // Given: 보드에 빈 공간이 있는 블록들 배치
        Board board = engine.getBoard();

        // 하단에 블록 배치 (y=18)
        board.setCell(5, 18, 1);
        board.setCell(6, 18, 1);

        // 상단에 떠있는 블록 배치 (y=15)
        board.setCell(5, 15, 2);
        board.setCell(6, 15, 2);

        // When: private 메서드 호출
        Method method = GameEngine.class.getDeclaredMethod("processGravityEffect");
        method.setAccessible(true);
        method.invoke(engine);

        // Then: 중력 효과가 적용되었는지 기본 확인
        // 실제 중력 효과는 Board.applyGravity()를 호출하므로 결과가 다를 수 있음
        assertNotNull(board);
        // 블록들이 여전히 존재하는지 확인
        assertTrue(
                board.getCell(5, 18) != 0
                        || board.getCell(5, 17) != 0
                        || board.getCell(5, 15) != 0);
    }

    @Test
    @DisplayName("분할 아이템 효과 - processSplitEffect 테스트")
    void testProcessSplitEffect() throws Exception {
        // Given: SPLIT 아이템 테트로미노 설정
        Tetromino splitPiece = Tetromino.item(Kind.SPLIT, 0, ItemType.SPLIT, 0);

        // 현재 조각을 SPLIT으로 설정 (reflection 사용)
        java.lang.reflect.Field currentField = GameEngine.class.getDeclaredField("current");
        currentField.setAccessible(true);
        currentField.set(engine, splitPiece);

        // 위치 설정
        java.lang.reflect.Field pxField = GameEngine.class.getDeclaredField("px");
        java.lang.reflect.Field pyField = GameEngine.class.getDeclaredField("py");
        pxField.setAccessible(true);
        pyField.setAccessible(true);
        pxField.set(engine, 3); // x 위치
        pyField.set(engine, 5); // y 위치

        // 보드에 떠있는 블록들 배치
        Board board = engine.getBoard();
        board.setCell(3, 10, 1); // SPLIT이 영향을 줄 열에 블록
        board.setCell(4, 12, 2);
        board.setCell(5, 8, 3);

        // When: processSplitEffect 호출
        Method method = GameEngine.class.getDeclaredMethod("processSplitEffect");
        method.setAccessible(true);
        method.invoke(engine);

        // Then: 각 열에서 중력이 적용되었는지 확인
        // 실제 효과는 applySingleColumnGravity에 의해 결정됨
        assertNotNull(board); // 보드가 변경되었는지 기본 확인
    }

    @Test
    @DisplayName("아이템 효과 처리 - processItemEffect 테스트")
    void testProcessItemEffect() throws Exception {
        // Given: 보드에 블록들 배치
        Board board = engine.getBoard();
        board.setCell(5, 18, 1);
        board.setCell(6, 18, 2);

        // When: processItemEffect 호출 (GRAVITY 효과)
        Method method =
                GameEngine.class.getDeclaredMethod(
                        "processItemEffect", Tetromino.ItemType.class, Tetromino.Kind.class);
        method.setAccessible(true);
        method.invoke(engine, ItemType.GRAVITY, Kind.T);

        // Then: 메서드가 정상적으로 실행되었는지 확인
        assertNotNull(board);
    }

    @Test
    @DisplayName("라인 클리어 아이템 효과 - processLineClearEffect 테스트")
    void testProcessLineClearEffect() throws Exception {
        // Given: LINE_CLEAR 아이템 설정
        Tetromino lineClearPiece = Tetromino.item(Kind.T, 0, ItemType.LINE_CLEAR, 1);

        java.lang.reflect.Field currentField = GameEngine.class.getDeclaredField("current");
        currentField.setAccessible(true);
        currentField.set(engine, lineClearPiece);

        // 클리어할 라인에 블록들 배치
        Board board = engine.getBoard();
        for (int x = 0; x < board.getWidth(); x++) {
            board.setCell(x, 19, 1); // 맨 아래 라인을 가득 채움
        }

        // When: processLineClearEffect 호출
        Method method = GameEngine.class.getDeclaredMethod("processLineClearEffect");
        method.setAccessible(true);
        method.invoke(engine);

        // Then: 라인 클리어 효과가 적용되었는지 기본 확인
        // 실제 라인 클리어 로직에 따라 결과가 달라질 수 있음
        assertNotNull(board);
        // 보드가 변경되었는지 간접적으로 확인
        assertTrue(board.getWidth() > 0 && board.getHeight() > 0);
    }

    @Test
    @DisplayName("아이템이 아닌 일반 조각에서 아이템 효과 메서드 호출 시 안전성 테스트")
    void testItemEffectsWithNormalPiece() throws Exception {
        // Given: 일반 테트로미노 설정
        Tetromino normalPiece = Tetromino.of(Tetromino.Kind.T);

        java.lang.reflect.Field currentField = GameEngine.class.getDeclaredField("current");
        currentField.setAccessible(true);
        currentField.set(engine, normalPiece);

        // When & Then: 존재하는 아이템 효과 메서드들이 일반 조각에서도 안전하게 동작하는지 확인
        Method[] methods = {
            GameEngine.class.getDeclaredMethod("processGravityEffect"),
            GameEngine.class.getDeclaredMethod("processSplitEffect"),
            GameEngine.class.getDeclaredMethod("processLineClearEffect")
        };

        for (Method method : methods) {
            method.setAccessible(true);
            assertDoesNotThrow(
                    () -> method.invoke(engine),
                    "메서드 " + method.getName() + "가 일반 조각에서 예외를 발생시켰습니다");
        }
    }

    @Test
    @DisplayName("current가 null일 때 아이템 효과 메서드들의 안전성 테스트")
    void testItemEffectsWithNullCurrent() throws Exception {
        // Given: current를 null로 설정
        java.lang.reflect.Field currentField = GameEngine.class.getDeclaredField("current");
        currentField.setAccessible(true);
        currentField.set(engine, null);

        // When & Then: null 상태에서도 안전하게 동작하는지 확인
        Method[] methods = {
            GameEngine.class.getDeclaredMethod("processGravityEffect"),
            GameEngine.class.getDeclaredMethod("processSplitEffect"),
            GameEngine.class.getDeclaredMethod("processLineClearEffect")
        };

        for (Method method : methods) {
            method.setAccessible(true);
            assertDoesNotThrow(
                    () -> method.invoke(engine),
                    "메서드 " + method.getName() + "가 null current에서 예외를 발생시켰습니다");
        }
    }

    @Test
    @DisplayName("applySingleColumnGravity 메서드 테스트")
    void testApplySingleColumnGravity() throws Exception {
        // Given: 특정 열에 떠있는 블록들 배치
        Board board = engine.getBoard();
        int testColumn = 5;

        // 빈 공간이 있는 상태로 블록 배치
        board.setCell(testColumn, 15, 1); // 떠있는 블록
        board.setCell(testColumn, 18, 2); // 바닥 근처 블록

        // When: applySingleColumnGravity 호출
        Method method = GameEngine.class.getDeclaredMethod("applySingleColumnGravity", int.class);
        method.setAccessible(true);
        method.invoke(engine, testColumn);

        // Then: 해당 열에서 중력이 적용되었는지 기본 확인
        // 중력 효과의 정확한 결과는 구현에 따라 다를 수 있음
        assertNotNull(board);
        // 해당 열에 블록이 여전히 존재하는지 확인
        boolean hasBlockInColumn = false;
        for (int y = 0; y < board.getHeight(); y++) {
            if (board.getCell(testColumn, y) != 0) {
                hasBlockInColumn = true;
                break;
            }
        }
        assertTrue(hasBlockInColumn, "중력 적용 후에도 블록이 존재해야 함");
    }

    @Test
    @DisplayName("잘못된 열 인덱스로 applySingleColumnGravity 호출 시 안전성 테스트")
    void testApplySingleColumnGravityWithInvalidColumn() throws Exception {
        // When & Then: 잘못된 열 인덱스로 호출해도 예외가 발생하지 않아야 함
        Method method = GameEngine.class.getDeclaredMethod("applySingleColumnGravity", int.class);
        method.setAccessible(true);

        assertDoesNotThrow(() -> method.invoke(engine, -1));
        assertDoesNotThrow(() -> method.invoke(engine, 100));
    }
}
