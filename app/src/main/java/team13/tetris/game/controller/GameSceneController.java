package team13.tetris.game.controller;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import team13.tetris.SceneManager;
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
    private final SceneManager manager;
    
    private boolean paused = false;
    private boolean gameOver = false;
    private int totalLinesCleared = 0; // 총 클리어된 라인 수 추적
    private long lastHardDropTime = 0; // 마지막 하드드롭 시간

    public GameSceneController(GameScene gameScene, SceneManager manager, Settings settings, KeyInputHandler keyInputHandler) {
        this.gameScene = gameScene;
        this.settings = settings;
        this.keyInputHandler = keyInputHandler;
        this.manager = manager;
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
        if (engine != null && !gameOver) {
            engine.moveLeft();
        }
    }

    @Override
    public void moveRight() {
        if (engine != null && !gameOver) {
            engine.moveRight();
        }
    }

    @Override
    public void softDrop() {
        if (engine != null && !gameOver) {
            engine.softDrop();
        }
    }

    @Override
    public void hardDrop() {
        if (engine != null && !gameOver) {
            // 하드드롭 throttling: 100ms 간격으로 제한
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastHardDropTime >= 100) {
                engine.hardDrop();
                lastHardDropTime = currentTime;
            } 
        }
    }

    @Override
    public void rotateCW() {
        if (engine != null && !gameOver) {
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
        paused = false; // 일시정지 상태 해제
        
        // 엔진의 자동 하강도 확실히 중지
        if (engine != null) {
            engine.stopAutoDrop();
        }
        
        // 게임오버 화면 표시
        gameScene.showGameOver();
    }

    @Override
    public void onNextPiece(Tetromino next) {
        gameScene.updateGrid();
    }

    @Override
    public void onScoreChanged(int score) {
        gameScene.updateGrid();
        // 아이템 모드 줄 수 표시 제거로 인해 updateItemModeInfo 호출 비활성화
    }

    // ========== 일시정지 다이얼로그 ==========
    private void showPauseWindow() {
        Platform.runLater(() -> {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(gameScene.getScene().getWindow());

            Label resume = new Label("Resume");
            Label mainMenu = new Label("Main Menu");
            Label quit = new Label("Quit");

            // ✅ CSS 클래스 부여 (인라인 스타일 제거)
            resume.getStyleClass().add("pause-option");
            mainMenu.getStyleClass().add("pause-option");
            quit.getStyleClass().add("pause-option");

            VBox box = new VBox(8, resume, mainMenu, quit);
            box.getStyleClass().add("pause-box"); // 배경/패딩 등은 CSS에서
            box.setAlignment(Pos.CENTER);

            Scene dialogScene = new Scene(box);

            // ✅ 다이얼로그에도 기존 Scene의 스타일시트를 그대로 적용 (테마 연동)
            dialogScene.getStylesheets().addAll(gameScene.getScene().getStylesheets());

            

            // 기본 선택: (게임오버면 Quit 선택, 아니면 Resume 선택)
            final int[] selected = new int[]{0};
            applySelection(resume, mainMenu, quit, selected[0]);

            dialogScene.setOnKeyPressed(ev -> {
               if (ev.getCode() == KeyCode.UP) {
                    // Resume(0) <-> Main Menu(1) <-> Quit(2)
                    selected[0] = (selected[0] == 0) ? 0 : selected[0] - 1;
                    applySelection(resume, mainMenu, quit, selected[0]);
                } else if (ev.getCode() == KeyCode.DOWN) {
                    // Resume(0) <-> Main Menu(1) <-> Quit(2)
                    selected[0] = (selected[0] == 2) ? 2 : selected[0] + 1;
                    applySelection(resume, mainMenu, quit, selected[0]);
                } else if (ev.getCode() == KeyCode.ENTER) {
                    dialog.close();
                    if (selected[0] == 0) {
                        // Resume 선택
                        resume();
                    } else if (selected[0] == 1) {
                        // Main Menu 선택
                        manager.showConfirmScene(
                            settings,
                            "Return to Main Menu?",
                            () -> manager.showMainMenu(settings),
                            () -> {
                                manager.restorePreviousScene();
                                paused = true;
                                showPauseWindow();
                            }
                        );
                    } else {
                        // Quit 선택 - 확인 화면 표시
                        manager.showConfirmScene(
                            settings,
                            "Exit Game?",
                            () -> manager.exitWithSave(settings),
                            () -> {
                                manager.restorePreviousScene();
                                paused = true;
                                showPauseWindow();
                            }
                        );
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

    private void applySelection(Label resume, Label mainMenu, Label quit, int selectedIndex) {
        // 모든 선택 해제
        resume.getStyleClass().remove("selected");
        mainMenu.getStyleClass().remove("selected");
        quit.getStyleClass().remove("selected");
        
        // 선택된 항목에만 selected 클래스 추가
        switch (selectedIndex) {
            case 0 -> resume.getStyleClass().add("selected");
            case 1 -> mainMenu.getStyleClass().add("selected");
            case 2 -> quit.getStyleClass().add("selected");
        }
    }
}
