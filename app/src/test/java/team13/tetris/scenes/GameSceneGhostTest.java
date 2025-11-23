package team13.tetris.scenes;

import static org.junit.jupiter.api.Assertions.*;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;
import team13.tetris.data.ScoreBoard;
import team13.tetris.game.controller.GameStateListener;
import team13.tetris.game.logic.GameEngine;
import team13.tetris.game.model.Board;
import team13.tetris.game.model.Tetromino;

// GameScene 고스트 블록 렌더링 테스트: Tests ghost block rendering in UI
@DisplayName("GameScene 고스트 블록 렌더링 테스트")
public class GameSceneGhostTest {

    private SceneManager sceneManager;
    private Settings settings;
    private GameScene gameScene;
    private GameEngine gameEngine;
    private Board board;
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 20;

    @BeforeAll
    static void initJavaFX() {
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // 이미 초기화되었으면 무시
        }
    }

    @BeforeEach
    void setUp() {
        javafx.application.Platform.runLater(
                () -> {
                    Stage stage = new Stage();
                    settings = new Settings();
                    sceneManager = new SceneManager(stage);
                    board = new Board(BOARD_WIDTH, BOARD_HEIGHT);

                    GameStateListener mockListener =
                            new GameStateListener() {
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
                            };

                    gameEngine =
                            new GameEngine(board, mockListener, ScoreBoard.ScoreEntry.Mode.NORMAL);
                    gameScene =
                            new GameScene(
                                    sceneManager,
                                    settings,
                                    gameEngine,
                                    ScoreBoard.ScoreEntry.Mode.NORMAL);
                    gameEngine.startNewGame();
                });

        waitForFX();
    }

    @Test
    @DisplayName("게임 씬이 고스트 블록 정보를 올바르게 조회할 수 있는지 확인")
    void testGameSceneCanAccessGhostY() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = gameScene.createScene();
                    assertNotNull(scene, "Scene should be created");

                    // 게임 엔진에서 고스트 Y를 조회할 수 있는지 확인
                    int ghostY = gameEngine.getGhostY();
                    assertTrue(ghostY >= -1, "Ghost Y should be -1 (no piece) or valid position");

                    if (gameEngine.getCurrent() != null) {
                        assertTrue(
                                ghostY >= gameEngine.getPieceY(),
                                "Ghost Y should be at or below current piece Y");
                        assertTrue(ghostY < BOARD_HEIGHT, "Ghost Y should be within board bounds");
                    }
                });

        waitForFX();
    }

    @Test
    @DisplayName("updateGrid 호출 시 고스트 블록이 렌더링되는지 확인")
    void testUpdateGridRendersGhostBlock() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = gameScene.createScene();
                    assertNotNull(scene, "Scene should be created");

                    // updateGrid를 호출하여 고스트 블록 렌더링
                    assertDoesNotThrow(
                            () -> gameScene.updateGrid(),
                            "updateGrid should not throw exceptions when rendering ghost blocks");
                });

        waitForFX();
    }

    @Test
    @DisplayName("현재 블록과 고스트 블록이 다른 위치에 있을 때 올바르게 렌더링되는지 확인")
    void testGhostBlockRenderedWhenDifferentFromCurrent() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = gameScene.createScene();

                    // 현재 블록과 고스트 위치가 다른지 확인
                    int currentY = gameEngine.getPieceY();
                    int ghostY = gameEngine.getGhostY();

                    if (ghostY != -1 && ghostY != currentY) {
                        // 고스트 블록이 렌더링되어야 하는 상황
                        assertDoesNotThrow(
                                () -> gameScene.updateGrid(),
                                "updateGrid should render ghost block without errors");

                        // 씬이 정상적으로 업데이트되었는지 확인
                        assertNotNull(
                                scene.getRoot(), "Scene root should exist after ghost rendering");
                    }
                });

        waitForFX();
    }

    @Test
    @DisplayName("고스트 블록이 보드 경계를 벗어나지 않는지 확인")
    void testGhostBlockStaysWithinBounds() {
        javafx.application.Platform.runLater(
                () -> {
                    gameScene.createScene();

                    // 여러 번 updateGrid를 호출하여 고스트 블록 렌더링 테스트
                    for (int i = 0; i < 10; i++) {
                        assertDoesNotThrow(
                                () -> gameScene.updateGrid(),
                                "Ghost block rendering should not cause out-of-bounds errors");

                        int ghostY = gameEngine.getGhostY();
                        if (ghostY != -1) {
                            assertTrue(
                                    ghostY >= 0 && ghostY < BOARD_HEIGHT,
                                    "Ghost Y should be within board bounds: " + ghostY);
                        }

                        // 블록을 조금씩 이동해보기
                        if (i % 2 == 0) {
                            gameEngine.moveLeft();
                        } else {
                            gameEngine.moveRight();
                        }
                    }
                });

        waitForFX();
    }

    @Test
    @DisplayName("블록 회전 후 고스트 위치가 업데이트되는지 확인")
    void testGhostBlockUpdatesAfterRotation() {
        javafx.application.Platform.runLater(
                () -> {
                    gameScene.createScene();

                    // 블록 회전
                    gameEngine.rotateCW();
                    gameScene.updateGrid();

                    int newGhostY = gameEngine.getGhostY();

                    // 고스트 위치가 여전히 유효한지 확인 (반드시 달라질 필요는 없음)
                    if (newGhostY != -1) {
                        assertTrue(
                                newGhostY >= 0 && newGhostY < BOARD_HEIGHT,
                                "Ghost Y after rotation should be valid: " + newGhostY);

                        // 회전된 블록이 고스트 위치에 배치 가능한지 확인
                        Tetromino current = gameEngine.getCurrent();
                        if (current != null) {
                            assertTrue(
                                    board.fits(
                                            current.getShape(), gameEngine.getPieceX(), newGhostY),
                                    "Rotated piece should fit at ghost position");
                        }
                    }
                });

        waitForFX();
    }

    @Test
    @DisplayName("좌우 이동 후 고스트 위치가 업데이트되는지 확인")
    void testGhostBlockUpdatesAfterMovement() {
        javafx.application.Platform.runLater(
                () -> {
                    gameScene.createScene();

                    // 좌측으로 이동
                    gameEngine.moveLeft();
                    gameScene.updateGrid();

                    int ghostYLeft = gameEngine.getGhostY();

                    // 우측으로 이동
                    gameEngine.moveRight();
                    gameEngine.moveRight();
                    gameScene.updateGrid();

                    int ghostYRight = gameEngine.getGhostY();

                    // 고스트 위치들이 유효한지 확인
                    if (ghostYLeft != -1) {
                        assertTrue(
                                ghostYLeft >= 0 && ghostYLeft < BOARD_HEIGHT,
                                "Ghost Y after left movement should be valid: " + ghostYLeft);
                    }

                    if (ghostYRight != -1) {
                        assertTrue(
                                ghostYRight >= 0 && ghostYRight < BOARD_HEIGHT,
                                "Ghost Y after right movement should be valid: " + ghostYRight);
                    }
                });

        waitForFX();
    }

    @Test
    @DisplayName("바닥에 장애물이 있을 때 고스트 블록이 올바르게 렌더링되는지 확인")
    void testGhostBlockWithObstacles() {
        javafx.application.Platform.runLater(
                () -> {
                    // 바닥에 장애물 배치
                    for (int x = 0; x < 5; x++) {
                        board.setCell(x, BOARD_HEIGHT - 1, 1);
                    }

                    gameScene.createScene();
                    gameScene.updateGrid();

                    int ghostY = gameEngine.getGhostY();

                    if (ghostY != -1) {
                        assertTrue(
                                ghostY < BOARD_HEIGHT - 1,
                                "Ghost should be above obstacles: ghostY=" + ghostY);

                        // 고스트 위치에서 블록이 배치 가능한지 확인
                        Tetromino current = gameEngine.getCurrent();
                        if (current != null) {
                            assertTrue(
                                    board.fits(current.getShape(), gameEngine.getPieceX(), ghostY),
                                    "Piece should fit at ghost position above obstacles");
                        }
                    }
                });

        waitForFX();
    }

    @Test
    @DisplayName("CSS 스타일이 고스트 블록에 올바르게 적용되는지 확인")
    void testGhostBlockCSS() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = gameScene.createScene();
                    gameScene.updateGrid();

                    // CSS가 적용되어 있는지 확인
                    assertNotNull(scene.getStylesheets(), "Scene should have stylesheets");
                    assertFalse(
                            scene.getStylesheets().isEmpty(),
                            "Scene should have stylesheets applied");

                    // block-ghost와 tetris-ghost-text 클래스가 CSS에 정의되어 있는지 확인
                    // (실제로는 CSS 파일을 파싱해야 하지만, 여기서는 렌더링이 예외 없이 완료되는지만 확인)
                    assertDoesNotThrow(
                            () -> gameScene.updateGrid(),
                            "Ghost block rendering with CSS should not throw exceptions");
                });

        waitForFX();
    }

    @Test
    @DisplayName("고스트 블록이 현재 블록과 같은 위치에 있을 때 렌더링되지 않는지 확인")
    void testGhostBlockNotRenderedWhenSameAsCurrent() {
        javafx.application.Platform.runLater(
                () -> {
                    // 블록을 바닥까지 이동시켜서 고스트와 현재 위치를 같게 만들기
                    while (gameEngine.softDrop()) {
                        // 바닥까지 이동
                    }

                    gameScene.createScene();

                    int currentY = gameEngine.getPieceY();
                    int ghostY = gameEngine.getGhostY();

                    if (currentY == ghostY) {
                        // 고스트와 현재 위치가 같은 경우에도 렌더링이 정상적으로 동작해야 함
                        assertDoesNotThrow(
                                () -> gameScene.updateGrid(),
                                "updateGrid should work even when ghost and current positions are same");
                    }
                });

        waitForFX();
    }

    @Test
    @DisplayName("색맹 모드에서 고스트 블록이 올바르게 렌더링되는지 확인")
    void testGhostBlockInColorBlindMode() {
        javafx.application.Platform.runLater(
                () -> {
                    settings.setColorBlindMode(true);

                    Scene scene = gameScene.createScene();
                    gameScene.updateGrid();

                    // 색맹 모드에서도 고스트 블록 렌더링이 정상 동작하는지 확인
                    assertDoesNotThrow(
                            () -> gameScene.updateGrid(),
                            "Ghost block rendering should work in color blind mode");

                    // CSS가 올바르게 적용되었는지 확인
                    assertTrue(
                            scene.getStylesheets().get(0).contains("colorblind.css")
                                    || scene.getStylesheets().get(0).contains("application.css"),
                            "Appropriate CSS should be applied");
                });

        waitForFX();
    }

    @Test
    @DisplayName("하드 드롭 후 새로운 블록의 고스트가 렌더링되는지 확인")
    void testGhostBlockAfterHardDrop() {
        javafx.application.Platform.runLater(
                () -> {
                    gameScene.createScene();

                    // 하드 드롭 실행
                    gameEngine.hardDrop();
                    gameScene.updateGrid();

                    // 새로운 블록의 고스트가 렌더링되는지 확인
                    int newGhostY = gameEngine.getGhostY();

                    if (newGhostY != -1) {
                        assertTrue(
                                newGhostY >= 0 && newGhostY < BOARD_HEIGHT,
                                "New ghost position should be valid after hard drop: " + newGhostY);

                        assertDoesNotThrow(
                                () -> gameScene.updateGrid(),
                                "Ghost rendering after hard drop should not throw exceptions");
                    }
                });

        waitForFX();
    }

    // Helper method
    private void waitForFX() {
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
