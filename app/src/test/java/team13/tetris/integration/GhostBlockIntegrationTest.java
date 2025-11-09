package team13.tetris.integration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;
import team13.tetris.data.ScoreBoard;
import team13.tetris.game.controller.GameStateListener;
import team13.tetris.game.logic.GameEngine;
import team13.tetris.game.model.Board;
import team13.tetris.game.model.Tetromino;
import team13.tetris.scenes.GameScene;
import javafx.scene.Scene;
import javafx.stage.Stage;

import static org.junit.jupiter.api.Assertions.*;

// 고스트 블록 기능의 전체적인 통합 테스트: GameEngine과 GameScene이 올바르게 연동되어 작동하는지 확인
@DisplayName("고스트 블록 통합 테스트")
public class GhostBlockIntegrationTest {

    private SceneManager sceneManager;
    private Settings settings;
    private GameScene gameScene;
    private GameEngine gameEngine;
    private Board board;
    private MockGameStateListener listener;

    private static class MockGameStateListener implements GameStateListener {
        public boolean gameOver = false;

        @Override
        public void onBoardUpdated(Board board) {
            // Mock implementation
        }

        @Override
        public void onPieceSpawned(Tetromino piece, int x, int y) {
            // Mock implementation
        }

        @Override
        public void onGameOver() {
            this.gameOver = true;
        }

        @Override
        public void onScoreChanged(int newScore) {
            // Mock implementation
        }

        @Override
        public void onLinesCleared(int lines) {
            // Mock implementation
        }

        @Override
        public void onNextPiece(Tetromino next) {
            // Mock implementation
        }
    }

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
        javafx.application.Platform.runLater(() -> {
            Stage stage = new Stage();
            settings = new Settings();
            sceneManager = new SceneManager(stage);
            board = new Board(10, 20);
            listener = new MockGameStateListener();

            gameEngine = new GameEngine(board, listener, ScoreBoard.ScoreEntry.Mode.NORMAL);
            gameScene = new GameScene(sceneManager, settings, gameEngine, ScoreBoard.ScoreEntry.Mode.NORMAL);
            gameEngine.startNewGame();
        });

        waitForFX();
    }

    @Test
    @DisplayName("게임 시작 후 고스트 블록이 올바르게 계산되고 렌더링되는지 확인")
    void testGhostBlockAfterGameStart() {
        javafx.application.Platform.runLater(() -> {
            // given - 새 게임이 시작된 상태
            Scene scene = gameScene.createScene();
            assertNotNull(scene, "게임 씬이 생성되어야 함");

            // when - 고스트 위치 계산 및 화면 업데이트
            int ghostY = gameEngine.getGhostY();
            assertDoesNotThrow(() -> gameScene.updateGrid(), 
                "updateGrid가 예외 없이 실행되어야 함");

            // then - 고스트 위치가 유효해야 함
            assertTrue(ghostY >= 0, "고스트 Y가 유효해야 함: " + ghostY);
            assertTrue(ghostY < 20, "고스트 Y가 보드 범위 내에 있어야 함: " + ghostY);
            assertTrue(ghostY >= gameEngine.getPieceY(), 
                "고스트 Y가 현재 블록 Y 이상이어야 함");

            // 현재 블록이 고스트 위치에 배치 가능한지 확인
            Tetromino current = gameEngine.getCurrent();
            if (current != null) {
                assertTrue(board.fits(current.getShape(), gameEngine.getPieceX(), ghostY),
                    "현재 블록이 고스트 위치에 배치 가능해야 함");
            }
        });

        waitForFX();
    }

    @Test
    @DisplayName("블록 이동 시 고스트 위치가 실시간으로 업데이트되는지 확인")
    void testGhostBlockUpdatesWithMovement() {
        javafx.application.Platform.runLater(() -> {
            gameScene.createScene();
            
            // when - 블록을 좌우로 이동
            gameEngine.moveLeft();
            gameScene.updateGrid();
            int ghostYAfterLeft = gameEngine.getGhostY();
            
            gameEngine.moveRight();
            gameEngine.moveRight();
            gameScene.updateGrid();
            int ghostYAfterRight = gameEngine.getGhostY();
            
            // then - 고스트 위치가 여전히 유효해야 함
            assertTrue(ghostYAfterLeft >= 0, "좌측 이동 후 고스트 Y 유효: " + ghostYAfterLeft);
            assertTrue(ghostYAfterRight >= 0, "우측 이동 후 고스트 Y 유효: " + ghostYAfterRight);
            
            // 모든 위치에서 렌더링이 정상 동작해야 함
            assertDoesNotThrow(() -> gameScene.updateGrid(),
                "이동 후 렌더링이 예외 없이 실행되어야 함");
        });

        waitForFX();
    }

    @Test
    @DisplayName("블록 회전 시 고스트 위치가 적절히 업데이트되는지 확인")
    void testGhostBlockUpdatesWithRotation() {
        javafx.application.Platform.runLater(() -> {
            gameScene.createScene();
            
            // when - 블록 회전
            gameEngine.rotateCW();
            gameScene.updateGrid();
            
            int ghostYAfterRotation = gameEngine.getGhostY();
            
            // then - 회전 후에도 고스트 위치가 유효해야 함
            if (ghostYAfterRotation != -1) {
                assertTrue(ghostYAfterRotation >= 0 && ghostYAfterRotation < 20,
                    "회전 후 고스트 Y가 보드 범위 내에 있어야 함: " + ghostYAfterRotation);
                
                // 회전된 블록이 고스트 위치에 배치 가능한지 확인
                Tetromino current = gameEngine.getCurrent();
                if (current != null) {
                    assertTrue(board.fits(current.getShape(), gameEngine.getPieceX(), ghostYAfterRotation),
                        "회전된 블록이 고스트 위치에 배치 가능해야 함");
                }
            }
            
            // 렌더링이 정상 동작해야 함
            assertDoesNotThrow(() -> gameScene.updateGrid(),
                "회전 후 렌더링이 예외 없이 실행되어야 함");
        });

        waitForFX();
    }

    @Test
    @DisplayName("색맹 모드에서도 고스트 블록이 정상 동작하는지 확인")
    void testGhostBlockInColorBlindMode() {
        javafx.application.Platform.runLater(() -> {
            // given - 색맹 모드 활성화
            settings.setColorBlindMode(true);
            
            Scene scene = gameScene.createScene();
            
            // when - 고스트 블록 렌더링
            assertDoesNotThrow(() -> gameScene.updateGrid(),
                "색맹 모드에서 고스트 블록 렌더링이 예외 없이 실행되어야 함");
            
            // then - 고스트 위치가 여전히 유효해야 함
            int ghostY = gameEngine.getGhostY();
            if (ghostY != -1) {
                assertTrue(ghostY >= 0 && ghostY < 20,
                    "색맹 모드에서도 고스트 Y가 유효해야 함: " + ghostY);
            }
            
            // CSS가 적절히 적용되었는지 확인
            assertTrue(scene.getStylesheets().get(0).contains("colorblind.css") ||
                       scene.getStylesheets().get(0).contains("application.css"),
                "적절한 CSS가 적용되어야 함");
        });

        waitForFX();
    }

    @Test
    @DisplayName("복잡한 시나리오에서 고스트 블록이 안정적으로 동작하는지 확인")
    void testGhostBlockInComplexScenario() {
        javafx.application.Platform.runLater(() -> {
            gameScene.createScene();
            
            // given - 복잡한 보드 상태 생성 (일부 라인에 블록 배치)
            for (int y = 17; y < 20; y++) {
                for (int x = 0; x < 7; x++) {
                    board.setCell(x, y, 1);
                }
            }
            
            // when - 여러 동작을 연속으로 수행
            for (int i = 0; i < 5; i++) {
                // 이동, 회전, 렌더링을 반복
                if (i % 2 == 0) {
                    gameEngine.moveLeft();
                } else {
                    gameEngine.moveRight();
                }
                
                if (i % 3 == 0) {
                    gameEngine.rotateCW();
                }
                
                // 각 단계에서 고스트 블록이 정상 동작하는지 확인
                assertDoesNotThrow(() -> gameScene.updateGrid(),
                    "복잡한 시나리오 " + i + "단계에서 렌더링이 예외 없이 실행되어야 함");
                
                int ghostY = gameEngine.getGhostY();
                if (ghostY != -1) {
                    assertTrue(ghostY >= 0 && ghostY < 20,
                        "복잡한 시나리오에서 고스트 Y가 유효해야 함: " + ghostY);
                }
            }
        });

        waitForFX();
    }

    @Test
    @DisplayName("하드 드롭 후 새로운 블록의 고스트가 즉시 업데이트되는지 확인")
    void testGhostBlockAfterHardDrop() {
        javafx.application.Platform.runLater(() -> {
            gameScene.createScene();
            
            // given - 초기 블록 정보
            Tetromino originalPiece = gameEngine.getCurrent();
            
            // when - 하드 드롭 실행
            gameEngine.hardDrop();
            gameScene.updateGrid();
            
            // then - 새로운 블록의 고스트가 정상 계산되는지 확인
            Tetromino newPiece = gameEngine.getCurrent();
            if (newPiece != null && originalPiece != null && 
                newPiece.getId() != originalPiece.getId()) {
                
                int newGhostY = gameEngine.getGhostY();
                assertTrue(newGhostY >= 0, "새로운 블록의 고스트 Y가 유효해야 함: " + newGhostY);
                
                assertDoesNotThrow(() -> gameScene.updateGrid(),
                    "하드 드롭 후 렌더링이 예외 없이 실행되어야 함");
            }
        });

        waitForFX();
    }

    @Test
    @DisplayName("게임 오버 상황에서 고스트 블록 처리가 안전한지 확인")
    void testGhostBlockSafetyOnGameOver() {
        javafx.application.Platform.runLater(() -> {
            gameScene.createScene();
            
            // given - 보드를 거의 채워서 게임 오버에 가깝게 만들기
            for (int y = 1; y < 20; y++) {
                for (int x = 0; x < 10; x++) {
                    if (y < 19 || x < 5) { // 마지막 줄 일부만 비워둠
                        board.setCell(x, y, 1);
                    }
                }
            }
            
            // when - 여러 번 하드 드롭하여 게임 오버 유도
            for (int i = 0; i < 10; i++) {
                if (gameEngine.getCurrent() != null) {
                    // 고스트 블록 계산이 안전하게 동작하는지 확인
                    assertDoesNotThrow(() -> {
                        gameEngine.getGhostY();
                        gameScene.updateGrid();
                    }, "게임 오버 근처에서도 고스트 블록 처리가 안전해야 함");
                    
                    gameEngine.hardDrop();
                }
                
                if (listener.gameOver) {
                    break;
                }
            }
            
            // then - 게임 오버 후에도 고스트 블록 호출이 안전해야 함
            assertDoesNotThrow(() -> {
                gameEngine.getGhostY();
                gameScene.updateGrid();
            }, "게임 오버 후에도 고스트 블록 처리가 안전해야 함");
        });

        waitForFX();
    }

    // Helper method
    private void waitForFX() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}