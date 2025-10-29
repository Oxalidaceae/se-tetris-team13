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

// GameScene의 비즈니스 로직과 게임 제어 담당
public class GameSceneController implements GameStateListener, KeyInputHandler.KeyInputCallback, GameController {
    private final GameScene gameScene;
    private final Settings settings;
    private final KeyInputHandler keyInputHandler;
    private GameEngine engine;
    private final SceneManager manager;

    private boolean paused = false;
    private boolean gameOver = false;
    private int totalLinesCleared = 0;
    private long lastHardDropTime = 0;

    public GameSceneController(
        GameScene gameScene,
        SceneManager manager,
        Settings settings,
        KeyInputHandler keyInputHandler
    ) {
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

    @Override
    public void start() {
        if (engine != null) engine.startAutoDrop();
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
        if (engine != null && !gameOver) engine.moveLeft();
    }

    @Override
    public void moveRight() {
        if (engine != null && !gameOver) engine.moveRight();
    }

    @Override
    public void softDrop() {
        if (engine != null && !gameOver) engine.softDrop();
    }

    @Override
    public void hardDrop() {
        if (engine != null && !gameOver) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastHardDropTime >= 100) {
                engine.hardDrop();
                lastHardDropTime = currentTime;
            }
        }
    }

    @Override
    public void rotateCW() {
        if (engine != null && !gameOver) engine.rotateCW();
    }

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
    public void onEscPressed() {}

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
        if (engine != null) engine.updateSpeedForLinesCleared(lines, totalLinesCleared);
        gameScene.updateGrid();
    }

    @Override
    public void onGameOver() {
        gameOver = true;
        paused = false;
        if (engine != null) engine.stopAutoDrop();
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

    private void showPauseWindow() {
        Platform.runLater(() -> {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(gameScene.getScene().getWindow());

            Label resume = new Label("Resume");
            Label quit = new Label("Quit");

            resume.getStyleClass().add("pause-option");
            quit.getStyleClass().add("pause-option");

            VBox box = new VBox(8, resume, quit);
            box.getStyleClass().add("pause-box");
            box.setAlignment(Pos.CENTER);

            Scene dialogScene = new Scene(box);
            dialogScene.getStylesheets().addAll(gameScene.getScene().getStylesheets());

            final boolean resumeDisabled = gameOver;
            if (resumeDisabled) resume.getStyleClass().add("disabled");

            final int[] selected = new int[] { resumeDisabled ? 1 : 0 };
            applySelection(resume, quit, selected[0]);

            dialogScene.setOnKeyPressed(ev -> {
                if (ev.getCode() == KeyCode.UP || ev.getCode() == KeyCode.DOWN) {
                    if (resumeDisabled) selected[0] = 1;
                    else selected[0] = (selected[0] == 0) ? 1 : 0;
                    applySelection(resume, quit, selected[0]);
                } else if (ev.getCode() == KeyCode.ENTER) {
                    dialog.close();
                    if (selected[0] == 0 && !gameOver) {
                        resume();
                    } else {
                        manager.showExitScene(settings, () -> {
                            manager.restorePreviousScene();
                            paused = true;
                            showPauseWindow();
                        });
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
        resume.getStyleClass().remove("selected");
        quit.getStyleClass().remove("selected");
        if (selectedIndex == 0) resume.getStyleClass().add("selected");
        else quit.getStyleClass().add("selected");
    }
}
