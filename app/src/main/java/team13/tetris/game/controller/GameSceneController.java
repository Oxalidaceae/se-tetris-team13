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
    }

    // ========== 일시정지 다이얼로그 ==========
    private void showPauseWindow() {
        Platform.runLater(() -> {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(gameScene.getScene().getWindow());

            Label resume = new Label("Resume");
            Label quit = new Label("Quit");
            resume.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8px;");
            quit.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8px;");
            
            if (gameOver) {
                // 게임 오버 시 Resume 비활성화
                resume.setStyle("-fx-text-fill: gray; -fx-font-size: 14px; -fx-padding: 8px;");
            }

            VBox box = new VBox(8, resume, quit);
            box.setStyle("-fx-background-color: black; -fx-padding: 12px;");
            box.setAlignment(Pos.CENTER);

            Scene dialogScene = new Scene(box);
            dialogScene.setOnKeyPressed(ev -> {
                if (ev.getCode() == KeyCode.UP || ev.getCode() == KeyCode.DOWN) {
                    // 선택 토글
                    boolean selectResume = resume.getStyle().contains("-fx-font-weight: bold");
                    if (selectResume) {
                        // quit으로 선택 변경
                        resume.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8px;");
                        quit.setStyle("-fx-text-fill: yellow; -fx-font-size: 14px; -fx-padding: 8px; -fx-font-weight: bold;");
                    } else {
                        resume.setStyle("-fx-text-fill: yellow; -fx-font-size: 14px; -fx-padding: 8px; -fx-font-weight: bold;");
                        quit.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8px;");
                    }
                } else if (ev.getCode() == KeyCode.ENTER) {
                    boolean resumeSelected = resume.getStyle().contains("-fx-font-weight: bold");
                    dialog.close();
                    paused = false;
                    if (resumeSelected && !gameOver) {
                        resume();
                    } else {
                        Platform.exit();
                    }
                }
            });

            // 기본적으로 resume 선택
            resume.setStyle("-fx-text-fill: yellow; -fx-font-size: 14px; -fx-padding: 8px; -fx-font-weight: bold;");

            dialog.setScene(dialogScene);
            dialog.setTitle("Paused");
            dialog.setWidth(200);
            dialog.setHeight(140);
            dialog.showAndWait();
        });
    }
}