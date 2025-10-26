package team13.tetris.game.controller;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import team13.tetris.config.Settings;
import team13.tetris.game.logic.GameEngine;
import team13.tetris.game.model.Board;
import team13.tetris.game.model.Tetromino;
import team13.tetris.input.KeyInputHandler;
import team13.tetris.scenes.GameScene;

/**
 * GameScene의 비즈니스 로직과 게임 제어를 담당하는 컨트롤러
 * GameScene은 순수한 UI 렌더링만 담당하고, 이 클래스가 게임 로직을 처리합니다.
 */
public class GameSceneController implements GameStateListener, KeyInputHandler.KeyInputCallback, GameController {
    private final GameScene gameScene;
    private final Settings settings;
    private final KeyInputHandler keyInputHandler;
    private GameEngine engine;
    
    private boolean paused = false;
    private boolean gameOver = false;
    private int totalLinesCleared = 0; // 총 클리어된 라인 수 추적

    public GameSceneController(GameScene gameScene, Settings settings, KeyInputHandler keyInputHandler) {
        this.gameScene = gameScene;
        this.settings = settings;
        this.keyInputHandler = keyInputHandler;
    }

    public void setEngine(GameEngine engine) {
        this.engine = engine;
    }

    public void attachToScene(Scene scene) {
        keyInputHandler.attachToScene(scene, this);
    }

    // ========== GameController 인터페이스 구현 ==========
    @Override
    public void start() {
        if (engine != null) {
            engine.startAutoDrop();
        }
    }

    @Override
    public void pause() {
        if (engine != null && !paused && !gameOver) {
            paused = true;
            engine.stopAutoDrop();
            showPauseWindow();
        }
    }

    @Override
    public void resume() {
        if (engine != null && paused && !gameOver) {
            paused = false;
            engine.startAutoDrop();
        }
    }

    @Override
    public void moveLeft() {
        if (engine != null) {
            engine.moveLeft();
        }
    }

    @Override
    public void moveRight() {
        if (engine != null) {
            engine.moveRight();
        }
    }

    @Override
    public void softDrop() {
        if (engine != null) {
            engine.softDrop();
        }
    }

    @Override
    public void hardDrop() {
        if (engine != null) {
            engine.hardDrop();
        }
    }

    @Override
    public void rotateCW() {
        if (engine != null) {
            engine.rotateCW();
        }
    }

    // ========== KeyInputCallback 인터페이스 구현 ==========
    @Override
    public void onLeftPressed() {
        moveLeft();
    }

    @Override
    public void onRightPressed() {
        moveRight();
    }

    @Override
    public void onRotatePressed() {
        rotateCW();
    }

    @Override
    public void onDropPressed() {
        softDrop();
    }

    @Override
    public void onHardDropPressed() {
        hardDrop();
    }

    @Override
    public void onPausePressed() {
        pause();
    }

    @Override
    public void onEscPressed() {
        // ESC 키 처리 (필요시 구현)
    }

    // ========== GameStateListener 인터페이스 구현 ==========
    @Override
    public void onBoardUpdated(Board board) {
        gameScene.updateGrid();
    }

    @Override
    public void onPieceSpawned(Tetromino tetromino, int px, int py) {
        gameScene.updateGrid();
    }

    @Override
    public void onLinesCleared(int lines) {
        totalLinesCleared += lines;
        // GameEngine의 속도 업데이트 메서드 호출
        if (engine != null) {
            engine.updateSpeedForLinesCleared(lines, totalLinesCleared);
        }
        gameScene.updateGrid();
    }

    @Override
    public void onGameOver() {
        gameOver = true;
        gameScene.showGameOver();
    }

    @Override
    public void onNextPiece(Tetromino next) {
        gameScene.updateGrid();
    }

    @Override
    public void onScoreChanged(int score) {
        gameScene.updateGrid();
        // 아이템 모드일 때 라인 정보 업데이트
        if (engine != null) {
            gameScene.updateItemModeInfo(engine.getTotalLinesCleared());
        }
    }

    // ========== 일시정지 다이얼로그 ==========
    private void showPauseWindow() {
        Platform.runLater(() -> {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(gameScene.getScene().getWindow());

            Label resume = new Label("Resume");
            Label quit = new Label("Quit");

            // ✅ CSS 클래스 부여 (인라인 스타일 제거)
            resume.getStyleClass().add("pause-option");
            quit.getStyleClass().add("pause-option");

            VBox box = new VBox(8, resume, quit);
            box.getStyleClass().add("pause-box"); // 배경/패딩 등은 CSS에서
            box.setAlignment(Pos.CENTER);

            Scene dialogScene = new Scene(box);

            // ✅ 다이얼로그에도 기존 Scene의 스타일시트를 그대로 적용 (테마 연동)
            dialogScene.getStylesheets().addAll(gameScene.getScene().getStylesheets());

            // 선택 상태 관리
            final boolean resumeDisabled = gameOver; // 게임오버면 Resume 비활성화
            if (resumeDisabled) {
                resume.getStyleClass().add("disabled");
            }

            // 기본 선택: (게임오버면 Quit 선택, 아니면 Resume 선택)
            final int[] selected = new int[]{resumeDisabled ? 1 : 0};
            applySelection(resume, quit, selected[0]);

            dialogScene.setOnKeyPressed(ev -> {
                if (ev.getCode() == KeyCode.UP || ev.getCode() == KeyCode.DOWN) {
                    // 토글
                    if (resumeDisabled) {
                        selected[0] = 1; // Quit 고정
                    } else {
                        selected[0] = (selected[0] == 0) ? 1 : 0;
                    }
                    applySelection(resume, quit, selected[0]);
                } else if (ev.getCode() == KeyCode.ENTER) {
                    dialog.close();
                    if (selected[0] == 0 && !gameOver) {
                        resume(); // paused를 먼저 설정하지 말고 resume()에서 처리하도록
                    } else {
                        paused = false;
                        Platform.exit();
                    }
                } else if (ev.getCode() == KeyCode.ESCAPE) {
                    // ESC: 일단 다이얼로그 닫고 게임 재개(게임오버 아니면)
                    dialog.close();
                    if (!gameOver) {
                        resume(); // paused를 먼저 설정하지 말고 resume()에서 처리하도록
                    }
                }
            });

            dialog.setScene(dialogScene);
            dialog.setTitle("Paused");
            dialog.setWidth(220);
            dialog.setHeight(150);
            dialog.showAndWait();
        });
    }

    private void applySelection(Label resume, Label quit, int selectedIndex) {
        // selected 클래스 토글
        if (selectedIndex == 0) {
            resume.getStyleClass().remove("selected");
            quit.getStyleClass().remove("selected");
            resume.getStyleClass().add("selected");
        } else {
            resume.getStyleClass().remove("selected");
            quit.getStyleClass().remove("selected");
            quit.getStyleClass().add("selected");
        }
    }
}