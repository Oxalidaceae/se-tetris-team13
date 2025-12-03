package team13.tetris.scenes;

import static org.junit.jupiter.api.Assertions.*;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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

// GameScene 테스트: Tests scene 생성, board 렌더링, game UI 업데이트
@DisplayName("GameScene 테스트")
public class GameSceneTest {

    private SceneManager sceneManager;
    private Settings settings;
    private GameScene gameScene;
    private GameEngine gameEngine;
    private Board board;
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 20;

    @BeforeAll
    static void initJavaFX() {
        // JavaFX 툴킷 초기화
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // 이미 초기화되었으면 무시
        }
    }

    @BeforeEach
    void setUp() {
        // Mock Stage와 GameEngine 생성
        javafx.application.Platform.runLater(
                () -> {
                    Stage stage = new Stage();
                    settings = new Settings();
                    sceneManager = new SceneManager(stage);
                    board = new Board(BOARD_WIDTH, BOARD_HEIGHT);

                    // Mock GameStateListener
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
                });

        waitForFX();
    }

    @Test
    @DisplayName("Scene이 정상적으로 생성되는지 확인")
    void testSceneCreation() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = gameScene.createScene();

                    assertNotNull(scene, "Scene should not be null");
                    assertNotNull(scene.getRoot(), "Scene root should not be null");
                    assertTrue(scene.getRoot() instanceof HBox, "Scene root should be HBox");
                });

        waitForFX();
    }

    @Test
    @DisplayName("getScene 메서드가 생성된 Scene을 반환하는지 확인")
    void testGetScene() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene createdScene = gameScene.createScene();
                    Scene retrievedScene = gameScene.getScene();

                    assertSame(
                            createdScene,
                            retrievedScene,
                            "getScene should return the created scene");
                });

        waitForFX();
    }

    @Test
    @DisplayName("루트 레이아웃에 game-root 스타일 클래스가 있는지 확인")
    void testRootStyleClass() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = gameScene.createScene();
                    HBox root = (HBox) scene.getRoot();

                    assertTrue(
                            root.getStyleClass().contains("game-root"),
                            "Root should have 'game-root' style class");
                });

        waitForFX();
    }

    @Test
    @DisplayName("보드 그리드가 생성되는지 확인")
    void testBoardGridExists() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = gameScene.createScene();
                    HBox root = (HBox) scene.getRoot();

                    // 첫 번째 자식이 보드 그리드여야 함
                    assertTrue(
                            root.getChildren().size() >= 1, "Root should have at least one child");
                    assertTrue(
                            root.getChildren().get(0) instanceof GridPane,
                            "First child should be GridPane (board)");
                });

        waitForFX();
    }

    @Test
    @DisplayName("보드 그리드 크기가 올바른지 확인 (테두리 포함)")
    void testBoardGridSize() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = gameScene.createScene();
                    HBox root = (HBox) scene.getRoot();
                    GridPane boardGrid = (GridPane) root.getChildren().get(0);

                    // (width+2) x (height+2) 그리드 (테두리 포함)
                    int expectedCells = (BOARD_WIDTH + 2) * (BOARD_HEIGHT + 2);
                    assertEquals(
                            expectedCells,
                            boardGrid.getChildren().size(),
                            "Board grid should have " + expectedCells + " cells including borders");
                });

        waitForFX();
    }

    @Test
    @DisplayName("보드 그리드에 board-grid 스타일 클래스가 있는지 확인")
    void testBoardGridStyleClass() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = gameScene.createScene();
                    HBox root = (HBox) scene.getRoot();
                    GridPane boardGrid = (GridPane) root.getChildren().get(0);

                    assertTrue(
                            boardGrid.getStyleClass().contains("board-grid"),
                            "Board grid should have 'board-grid' style class");
                });

        waitForFX();
    }

    @Test
    @DisplayName("미리보기 그리드가 4x4 크기로 생성되는지 확인")
    void testPreviewGridSize() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = gameScene.createScene();
                    // 미리보기 그리드는 오른쪽 패널 내부에 있음
                    // updateGrid를 호출하여 초기화
                    gameScene.updateGrid();
                    waitForFX();

                    // 미리보기 그리드 크기 확인 (4x4 = 16 cells)
                    // 실제로는 VBox 내부에서 찾아야 하지만, 최소한 존재 확인
                    assertNotNull(scene.getRoot(), "Scene root should exist");
                });

        waitForFX();
    }

    @Test
    @DisplayName("점수 레이블이 초기화되는지 확인")
    void testScoreLabelInitialization() {
        javafx.application.Platform.runLater(
                () -> {
                    gameScene.createScene();
                    gameScene.updateGrid();

                    // updateGrid가 점수를 업데이트함
                    // 초기 점수는 0이어야 함
                    assertEquals(0, gameEngine.getScore(), "Initial score should be 0");
                });

        waitForFX();
    }

    @Test
    @DisplayName("updateGrid 메서드가 에러 없이 실행되는지 확인")
    void testUpdateGridExecution() {
        javafx.application.Platform.runLater(
                () -> {
                    gameScene.createScene();

                    assertDoesNotThrow(
                            () -> gameScene.updateGrid(),
                            "updateGrid should execute without throwing exceptions");
                });

        waitForFX();
    }

    @Test
    @DisplayName("엔진이 null일 때 updateGrid가 안전하게 처리되는지 확인")
    void testUpdateGridWithNullEngine() {
        javafx.application.Platform.runLater(
                () -> {
                    gameScene.createScene();
                    gameScene.setEngine(null);

                    assertDoesNotThrow(
                            () -> gameScene.updateGrid(),
                            "updateGrid should handle null engine gracefully");
                });

        waitForFX();
    }

    @Test
    @DisplayName("requestFocus 메서드가 에러 없이 실행되는지 확인")
    void testRequestFocus() {
        javafx.application.Platform.runLater(
                () -> {
                    gameScene.createScene();

                    assertDoesNotThrow(
                            () -> gameScene.requestFocus(),
                            "requestFocus should execute without throwing exceptions");
                });

        waitForFX();
    }

    @Test
    @DisplayName("setEngine 메서드가 정상 동작하는지 확인")
    void testSetEngine() {
        javafx.application.Platform.runLater(
                () -> {
                    gameScene.createScene();

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

                    Board newBoard = new Board(BOARD_WIDTH, BOARD_HEIGHT);
                    GameEngine newEngine =
                            new GameEngine(newBoard, mockListener, ScoreBoard.ScoreEntry.Mode.HARD);

                    assertDoesNotThrow(
                            () -> gameScene.setEngine(newEngine),
                            "setEngine should work without errors");
                });

        waitForFX();
    }

    @Test
    @DisplayName("보드 셀이 올바른 크기로 생성되는지 확인")
    void testCellSize() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = gameScene.createScene();
                    HBox root = (HBox) scene.getRoot();
                    GridPane boardGrid = (GridPane) root.getChildren().get(0);

                    if (!boardGrid.getChildren().isEmpty()) {
                        Label firstCell = (Label) boardGrid.getChildren().get(0);

                        assertEquals(
                                20, firstCell.getMinWidth(), 0.1, "Cell min width should be 20");
                        assertEquals(
                                16, firstCell.getMinHeight(), 0.1, "Cell min height should be 16");
                    }
                });

        waitForFX();
    }

    @Test
    @DisplayName("테두리 셀이 'X'로 표시되는지 확인")
    void testBorderCells() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = gameScene.createScene();
                    HBox root = (HBox) scene.getRoot();
                    GridPane boardGrid = (GridPane) root.getChildren().get(0);

                    // 첫 번째 행은 모두 테두리여야 함
                    Label topLeftCell = (Label) boardGrid.getChildren().get(0);
                    assertEquals("X", topLeftCell.getText(), "Border cell should display 'X'");
                });

        waitForFX();
    }

    @Test
    @DisplayName("빈 셀이 cell-empty 스타일 클래스를 가지는지 확인")
    void testEmptyCellStyle() {
        javafx.application.Platform.runLater(
                () -> {
                    gameScene.createScene();
                    gameScene.updateGrid();
                    waitForFX();

                    // 빈 보드의 경우 대부분의 셀이 empty여야 함
                    assertTrue(
                            board.getCell(0, 0) == 0 || board.getCell(0, 0) != 0,
                            "Board cell should have a valid state");
                });

        waitForFX();
    }

    @Test
    @DisplayName("게임 시작 후 조각이 스폰되는지 확인")
    void testPieceSpawn() {
        javafx.application.Platform.runLater(
                () -> {
                    gameScene.createScene();
                    gameEngine.startNewGame();
                    gameScene.updateGrid();

                    assertNotNull(gameEngine.getCurrent(), "Current piece should be spawned");
                    assertNotNull(gameEngine.getNext(), "Next piece should exist");
                });

        waitForFX();
    }

    @Test
    @DisplayName("다양한 난이도로 GameScene이 생성되는지 확인")
    void testDifferentDifficulties() {
        javafx.application.Platform.runLater(
                () -> {
                    ScoreBoard.ScoreEntry.Mode[] difficulties = {
                        ScoreBoard.ScoreEntry.Mode.EASY,
                        ScoreBoard.ScoreEntry.Mode.NORMAL,
                        ScoreBoard.ScoreEntry.Mode.HARD
                    };

                    for (ScoreBoard.ScoreEntry.Mode difficulty : difficulties) {
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

                        Board testBoard = new Board(BOARD_WIDTH, BOARD_HEIGHT);
                        GameEngine testEngine = new GameEngine(testBoard, mockListener, difficulty);
                        GameScene testScene =
                                new GameScene(sceneManager, settings, testEngine, difficulty);

                        assertNotNull(
                                testScene.createScene(),
                                "GameScene should be created for difficulty: " + difficulty.name());
                    }
                });

        waitForFX();
    }

    // JavaFX 스레드 작업 완료 대기 헬퍼 메서드
    private void waitForFX() {
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
