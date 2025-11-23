package team13.tetris.game.logic;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import team13.tetris.data.ScoreBoard;
import team13.tetris.game.controller.GameStateListener;
import team13.tetris.game.model.Board;
import team13.tetris.game.model.Tetromino;

// GameEngine 고스트 블록 테스트: Tests ghost block position calculation
@DisplayName("GameEngine 고스트 블록 테스트")
public class GameEngineGhostTest {

    private Board board;
    private GameEngine engine;
    private MockGameStateListener listener;

    private static class MockGameStateListener implements GameStateListener {
        @Override
        public void onBoardUpdated(Board board) {}

        @Override
        public void onPieceSpawned(Tetromino piece, int x, int y) {}

        @Override
        public void onGameOver() {}

        @Override
        public void onScoreChanged(int newScore) {}

        @Override
        public void onLinesCleared(int lines) {}

        @Override
        public void onNextPiece(Tetromino next) {}
    }

    @BeforeEach
    void setUp() {
        board = new Board(10, 20);
        listener = new MockGameStateListener();
        engine = new GameEngine(board, listener, ScoreBoard.ScoreEntry.Mode.NORMAL);
        engine.startNewGame();
    }

    @Test
    @DisplayName("빈 보드에서 고스트 Y 위치가 바닥까지 계산되는지 확인")
    void testGhostYOnEmptyBoard() {
        // given - 새 게임 시작 (현재 블록이 최상단에 스폰)
        int currentY = engine.getPieceY();

        // when
        int ghostY = engine.getGhostY();

        // then
        assertTrue(ghostY > currentY, "고스트 Y는 현재 블록보다 아래에 있어야 함");
        assertTrue(ghostY >= 0 && ghostY < 20, "고스트 Y는 보드 범위 내에 있어야 함");

        // 고스트 위치에서 블록이 배치 가능하고, 그 아래로는 불가능해야 함
        Tetromino current = engine.getCurrent();
        if (current != null) {
            assertTrue(
                    board.fits(current.getShape(), engine.getPieceX(), ghostY),
                    "고스트 위치에서 블록이 배치 가능해야 함");
            assertFalse(
                    board.fits(current.getShape(), engine.getPieceX(), ghostY + 1),
                    "고스트 위치 아래로는 배치 불가능해야 함");
        }
    }

    @Test
    @DisplayName("현재 블록이 없을 때 고스트 Y가 -1을 반환하는지 확인")
    void testGhostYWithNoCurrentPiece() {
        // given - 현재 블록을 null로 설정 (리플렉션 사용)
        try {
            java.lang.reflect.Field currentField = GameEngine.class.getDeclaredField("current");
            currentField.setAccessible(true);
            currentField.set(engine, null);
        } catch (Exception e) {
            fail("리플렉션으로 current 필드 설정 실패: " + e.getMessage());
        }

        // when
        int ghostY = engine.getGhostY();

        // then
        assertEquals(-1, ghostY, "현재 블록이 없으면 -1을 반환해야 함");
    }

    @Test
    @DisplayName("바닥에 블록이 있을 때 고스트 Y 위치가 올바르게 계산되는지 확인")
    void testGhostYWithObstacles() {
        // given - 바닥에 장애물 배치 (Y=18까지만 블록 존재)
        for (int x = 0; x < 10; x++) {
            board.setCell(x, 19, 1); // 바닥 한 줄 채움
        }

        // 현재 블록의 X 위치 확인
        int currentX = engine.getPieceX();

        // when
        int ghostY = engine.getGhostY();

        // then
        assertTrue(ghostY >= 0, "고스트 Y는 0 이상이어야 함");
        assertTrue(ghostY < 19, "고스트 Y는 바닥보다 위에 있어야 함");

        // 고스트 위치에서 블록이 배치 가능한지 확인
        Tetromino current = engine.getCurrent();
        if (current != null) {
            assertTrue(board.fits(current.getShape(), currentX, ghostY), "고스트 위치에서 블록이 배치 가능해야 함");
            assertFalse(
                    board.fits(current.getShape(), currentX, ghostY + 1), "고스트 위치 아래로는 배치 불가능해야 함");
        }
    }

    @Test
    @DisplayName("블록이 더 이상 떨어질 수 없을 때 고스트 Y가 올바르게 계산되는지 확인")
    void testGhostYWhenAtBottom() {
        // given - 현재 블록 정보 저장
        Tetromino originalPiece = engine.getCurrent();
        int originalPieceId = originalPiece != null ? originalPiece.getId() : -1;

        // 블록을 한 칸씩 떨어뜨려서 착지 직전 위치까지 이동
        boolean canDrop = true;
        while (canDrop) {
            // 한 번 더 떨어질 수 있는지 미리 확인
            if (originalPiece != null
                    && !board.fits(
                            originalPiece.getShape(), engine.getPieceX(), engine.getPieceY() + 1)) {
                // 더 이상 떨어질 수 없는 상태 - 여기서 테스트
                break;
            }
            canDrop = engine.softDrop();

            // 블록이 바뀌었으면 (새로운 블록이 스폰되었으면) 테스트 중단
            Tetromino currentPiece = engine.getCurrent();
            if (currentPiece == null
                    || (originalPieceId != -1 && currentPiece.getId() != originalPieceId)) {
                System.out.println("새로운 블록이 스폰되어 테스트를 스킵합니다.");
                return; // 테스트 스킵
            }
        }

        // 현재 블록이 더 이상 떨어질 수 없는 상태에서
        int currentY = engine.getPieceY();

        // when
        int ghostY = engine.getGhostY();

        // then
        assertEquals(
                currentY,
                ghostY,
                "블록이 더 이상 떨어질 수 없을 때 고스트 Y는 현재 Y와 같아야 함 (현재Y="
                        + currentY
                        + ", 고스트Y="
                        + ghostY
                        + ")");

        // 현재 위치에서 블록이 배치 가능하고, 그 아래로는 불가능한지 확인
        Tetromino current = engine.getCurrent();
        if (current != null) {
            assertTrue(
                    board.fits(current.getShape(), engine.getPieceX(), currentY),
                    "현재 위치에서 블록이 배치 가능해야 함");
            assertFalse(
                    board.fits(current.getShape(), engine.getPieceX(), currentY + 1),
                    "현재 위치 아래로는 배치 불가능해야 함");
        }
    }

    @Test
    @DisplayName("I 블록의 고스트 위치가 올바르게 계산되는지 확인")
    void testGhostYWithIPiece() {
        // given - I 블록으로 테스트하기 위해 게임을 여러 번 재시작
        for (int attempt = 0; attempt < 50; attempt++) {
            engine.startNewGame();
            Tetromino current = engine.getCurrent();

            if (current != null && current.getKind() == Tetromino.Kind.I) {
                int currentY = engine.getPieceY();

                // when
                int ghostY = engine.getGhostY();

                // then
                assertTrue(ghostY >= currentY, "고스트 Y는 현재 Y 이상이어야 함");
                assertTrue(ghostY >= 0 && ghostY < 20, "고스트 Y는 보드 범위 내에 있어야 함");

                // I 블록의 형태를 고려한 검증
                assertTrue(
                        board.fits(current.getShape(), engine.getPieceX(), ghostY),
                        "I 블록이 고스트 위치에 배치 가능해야 함");
                return;
            }
        }

        // I 블록을 찾지 못한 경우 - 이는 정상적인 상황일 수 있음
        System.out.println("Warning: I 블록을 50번의 시도에서 찾지 못했습니다.");
    }

    @Test
    @DisplayName("회전된 블록의 고스트 위치가 올바르게 계산되는지 확인")
    void testGhostYWithRotatedPiece() {
        // given - 현재 블록 회전
        engine.rotateCW();

        int currentY = engine.getPieceY();
        Tetromino current = engine.getCurrent();

        // when
        int ghostY = engine.getGhostY();

        // then
        if (current != null) {
            assertTrue(ghostY >= currentY, "회전된 블록의 고스트 Y는 현재 Y 이상이어야 함");
            assertTrue(
                    board.fits(current.getShape(), engine.getPieceX(), ghostY),
                    "회전된 블록이 고스트 위치에 배치 가능해야 함");
        }
    }

    @Test
    @DisplayName("좌우로 이동한 블록의 고스트 위치가 올바르게 계산되는지 확인")
    void testGhostYAfterHorizontalMovement() {
        // given - 블록을 좌우로 이동
        engine.moveLeft();
        int ghostYAfterLeft = engine.getGhostY();

        engine.moveRight();
        engine.moveRight();
        int ghostYAfterRight = engine.getGhostY();

        // then
        assertTrue(ghostYAfterLeft >= 0, "좌측 이동 후 고스트 Y는 유효해야 함");
        assertTrue(ghostYAfterRight >= 0, "우측 이동 후 고스트 Y는 유효해야 함");

        // X 위치가 바뀌어도 고스트 계산이 정상적으로 작동해야 함
        Tetromino current = engine.getCurrent();
        if (current != null) {
            assertTrue(
                    board.fits(current.getShape(), engine.getPieceX(), ghostYAfterRight),
                    "이동 후 고스트 위치에서 블록 배치가 가능해야 함");
        }
    }

    @Test
    @DisplayName("복잡한 보드 상태에서 고스트 위치가 올바르게 계산되는지 확인")
    void testGhostYWithComplexBoard() {
        // given - 복잡한 보드 상태 생성 (계단 모양)
        for (int y = 15; y < 20; y++) {
            for (int x = 0; x < y - 14; x++) {
                board.setCell(x, y, 1);
            }
        }

        // 블록을 특정 위치로 이동
        for (int i = 0; i < 5 && engine.getPieceX() < 5; i++) {
            engine.moveRight();
        }

        // when
        int ghostY = engine.getGhostY();

        // then
        assertTrue(ghostY >= 0, "복잡한 보드에서도 유효한 고스트 Y를 계산해야 함");

        Tetromino current = engine.getCurrent();
        if (current != null) {
            assertTrue(
                    board.fits(current.getShape(), engine.getPieceX(), ghostY),
                    "복잡한 보드에서 고스트 위치에 블록 배치가 가능해야 함");

            if (ghostY < 19) {
                assertFalse(
                        board.fits(current.getShape(), engine.getPieceX(), ghostY + 1),
                        "고스트 위치 아래로는 배치 불가능해야 함");
            }
        }
    }

    @Test
    @DisplayName("하드 드롭 후 고스트 위치가 업데이트되는지 확인")
    void testGhostYAfterHardDrop() {
        // given - 하드 드롭 실행
        engine.hardDrop();

        // 새로운 블록이 스폰되었으므로 고스트 Y가 업데이트되어야 함
        int newGhostY = engine.getGhostY();

        // then
        assertNotEquals(-1, newGhostY, "하드 드롭 후에도 유효한 고스트 Y가 있어야 함");

        // 새로운 블록의 고스트 위치 검증
        Tetromino newCurrent = engine.getCurrent();
        if (newCurrent != null) {
            assertTrue(
                    board.fits(newCurrent.getShape(), engine.getPieceX(), newGhostY),
                    "새로운 블록의 고스트 위치에서 배치가 가능해야 함");
        }
    }

    @Test
    @DisplayName("여러 종류의 테트로미노에서 고스트 위치가 올바르게 계산되는지 확인")
    void testGhostYForAllTetrominoTypes() {
        // given - 여러 번 게임을 재시작하여 다양한 블록 테스트
        java.util.Set<Tetromino.Kind> testedKinds = new java.util.HashSet<>();

        for (int attempt = 0; attempt < 100 && testedKinds.size() < 7; attempt++) {
            engine.startNewGame();
            Tetromino current = engine.getCurrent();

            if (current != null && !testedKinds.contains(current.getKind())) {
                testedKinds.add(current.getKind());

                // when
                int ghostY = engine.getGhostY();

                // then
                assertTrue(
                        ghostY >= engine.getPieceY(),
                        current.getKind() + " 블록의 고스트 Y는 현재 Y 이상이어야 함");
                assertTrue(
                        ghostY >= 0 && ghostY < 20,
                        current.getKind() + " 블록의 고스트 Y는 보드 범위 내에 있어야 함");
                assertTrue(
                        board.fits(current.getShape(), engine.getPieceX(), ghostY),
                        current.getKind() + " 블록이 고스트 위치에 배치 가능해야 함");
            }
        }

        assertTrue(
                testedKinds.size() >= 3,
                "최소 3가지 이상의 테트로미노 종류를 테스트해야 함 (테스트된 종류: " + testedKinds.size() + ")");
    }
}
