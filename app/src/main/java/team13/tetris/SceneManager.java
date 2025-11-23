package team13.tetris;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import team13.tetris.config.Settings;
import team13.tetris.config.SettingsRepository;
import team13.tetris.data.ScoreBoard;
import team13.tetris.game.controller.CompositeGameStateListener;
import team13.tetris.game.controller.GameSceneController;
import team13.tetris.game.controller.NetworkGameController;
import team13.tetris.game.logic.GameEngine;
import team13.tetris.game.model.Board;
import team13.tetris.input.KeyInputHandler;
import team13.tetris.scenes.ConfirmScene;
import team13.tetris.scenes.DifficultySelectionScene;
import team13.tetris.scenes.GameOverScene;
import team13.tetris.scenes.GameScene;
import team13.tetris.scenes.HostOrJoinScene;
import team13.tetris.scenes.KeySettingsScene;
import team13.tetris.scenes.MainMenuScene;
import team13.tetris.scenes.ScoreboardScene;
import team13.tetris.scenes.SettingsScene;

public class SceneManager {
    private final Stage stage;
    private boolean colorBlindMode = false;
    private String windowSizeClass = "window-medium";
    private Scene previousScene = null;

    // 대전 모드 이전 창 크기 저장
    private double previousWidth = 0;
    private double previousHeight = 0;

    public SceneManager(Stage stage) {
        this.stage = stage;
    }

    public void showMainMenu(Settings settings) {
        // 대전 모드에서 나온 경우 창 크기 복원
        restoreWindowSize();
        changeScene(new MainMenuScene(this, settings).getScene());
    }

    public void showSettings(Settings settings) {
        changeScene(new SettingsScene(this, settings).getScene());
    }

    public void showScoreboard(Settings settings) {
        changeScene(new ScoreboardScene(this, settings).getScene());
    }

    public void showScoreboard(
            Settings settings, String name, int score, ScoreBoard.ScoreEntry.Mode difficulty) {
        ScoreboardScene scene = new ScoreboardScene(this, settings, name, score, difficulty);
        changeScene(scene.getScene());
    }

    public void showDifficultySelection(Settings settings) {
        changeScene(new DifficultySelectionScene(this, settings).getScene());
    }

    public void showGameModeSelection(Settings settings) {
        changeScene(new team13.tetris.scenes.GameModeSelectionScene(this, settings).getScene());
    }

    public void showSoloModeSelection(Settings settings) {
        changeScene(new team13.tetris.scenes.SoloModeSelectionScene(this, settings).getScene());
    }

    public void showMultiModeSelection(Settings settings) {
        changeScene(new team13.tetris.scenes.MultiModeSelectionScene(this, settings).getScene());
    }

    public void showLocalMultiModeSelection(Settings settings) {
        changeScene(
                new team13.tetris.scenes.LocalMultiModeSelectionScene(this, settings).getScene());
    }

    public void showHostOrJoin(Settings settings) {
        restoreWindowSize();
        changeScene(new HostOrJoinScene(this, settings).getScene());
    }

    public void showNetworkLobby(Settings settings, boolean isHost, String serverIP) {
        // "SMALL" 크기일 때만 창 크기를 10% 늘림
        if ("SMALL".equals(settings.getWindowSize())) {
            // 현재 창 크기 저장 (다른 특수 크기 모드가 아닐 때만)
            if (previousWidth == 0 && previousHeight == 0) {
                previousWidth = stage.getWidth();
                previousHeight = stage.getHeight();
                stage.setWidth(previousWidth * 1.1);
                stage.setHeight(previousHeight * 1.1);
            }
        }
        // windowSizeClass는 의도적으로 변경하지 않음 (UI 요소 크기 유지를 위해)

        NetworkGameController controller =
                new NetworkGameController(this, settings, isHost, serverIP);
        controller.initializeLobby();
    }

    public void showNotImplemented() {
        javafx.stage.Stage popup = new javafx.stage.Stage();
        popup.setTitle("Not Implemented");

        javafx.scene.control.Label message =
                new javafx.scene.control.Label("This feature is not yet implemented.");
        message.getStyleClass().add("label");

        javafx.scene.control.Button closeBtn = new javafx.scene.control.Button("Close");
        closeBtn.setOnAction(e -> popup.close());

        javafx.scene.layout.VBox layout = new javafx.scene.layout.VBox(15, message, closeBtn);
        layout.setStyle("-fx-alignment: center; -fx-padding: 30;");

        Scene scene = new Scene(layout, 300, 150);
        applyStylesheet(scene);
        popup.setScene(scene);
        popup.show();
    }

    public void showGame(Settings settings, ScoreBoard.ScoreEntry.Mode difficulty) {
        // 대전 모드인 경우 VersusGame으로 분기
        if (difficulty == ScoreBoard.ScoreEntry.Mode.VERSUS) {
            show2PGame(settings, false, false);
            return;
        }

        // 타이머 모드인 경우 VersusGame (타이머 활성화)
        if (difficulty == ScoreBoard.ScoreEntry.Mode.TIMER) {
            show2PGame(settings, true, false);
            return;
        }

        Board board = new Board(10, 20);
        CompositeGameStateListener composite = new CompositeGameStateListener();
        GameEngine engine = new GameEngine(board, composite, difficulty);
        KeyInputHandler keyInputHandler = new KeyInputHandler(settings);
        GameScene gameScene = new GameScene(this, settings, engine, difficulty);
        GameSceneController gameController =
                new GameSceneController(gameScene, this, settings, keyInputHandler);

        gameController.setEngine(engine);
        composite.add(gameController);

        Scene scene = gameScene.createScene();

        gameController.attachToScene(scene);
        changeScene(scene);
        engine.startNewGame();
        gameScene.requestFocus();
    }

    // 2P 대전 모드 (타이머 모드, 아이템 모드 옵션)
    public void show2PGame(Settings settings, boolean timerMode, boolean itemMode) {
        applyVersusWindowSize(settings);
        showVersusGame(settings, timerMode, itemMode);
    }

    public void applyVersusWindowSize(Settings settings) {
        // 현재 창 크기 저장
        previousWidth = stage.getWidth();
        previousHeight = stage.getHeight();

        // 창 크기를 대전 모드용으로 확장
        int versusWidth;
        int versusHeight;

        switch (settings.getWindowSize()) {
            case "SMALL" -> {
                versusWidth = 950;
                versusHeight = 500;
            }
            case "LARGE" -> {
                versusWidth = 1600;
                versusHeight = 900;
            }
            default -> { // MEDIUM
                versusWidth = 1200;
                versusHeight = 700;
            }
        }

        stage.setWidth(versusWidth);
        stage.setHeight(versusHeight);

        // SceneManager의 windowSizeClass 업데이트 (대전 모드용)
        switch (settings.getWindowSize()) {
            case "SMALL" -> windowSizeClass = "window-small";
            case "LARGE" -> windowSizeClass = "window-large";
            default -> windowSizeClass = "window-medium";
        }
    }

    private void showVersusGame(Settings settings, boolean timerMode, boolean itemMode) {
        // Player 1 설정 (아이템 모드 여부에 따라 Mode 설정)
        Board board1 = new Board(10, 20);
        CompositeGameStateListener composite1 = new CompositeGameStateListener();
        ScoreBoard.ScoreEntry.Mode mode;
        if (timerMode) {
            mode = ScoreBoard.ScoreEntry.Mode.TIMER;
        } else if (itemMode) {
            mode = ScoreBoard.ScoreEntry.Mode.ITEM;
        } else {
            mode = ScoreBoard.ScoreEntry.Mode.NORMAL;
        }
        GameEngine engine1 = new GameEngine(board1, composite1, mode);

        // Player 2 설정
        Board board2 = new Board(10, 20);
        CompositeGameStateListener composite2 = new CompositeGameStateListener();
        GameEngine engine2 = new GameEngine(board2, composite2, mode);

        // 대전 모드 Scene 생성
        team13.tetris.scenes.VersusGameScene versusScene =
                new team13.tetris.scenes.VersusGameScene(
                        this, settings, engine1, engine2, timerMode);

        // Controller 생성
        team13.tetris.game.controller.VersusGameController versusController =
                new team13.tetris.game.controller.VersusGameController(
                        versusScene, this, settings, engine1, engine2, timerMode, itemMode);

        composite1.add(versusController.getPlayer1Listener());
        composite2.add(versusController.getPlayer2Listener());

        Scene scene = versusScene.createScene();
        versusController.attachToScene(scene);
        changeScene(scene);
        engine1.startNewGame();
        engine2.startNewGame();
        versusScene.requestFocus();
    }

    public void showGameOver(
            Settings settings, int finalScore, ScoreBoard.ScoreEntry.Mode difficulty) {
        changeScene(new GameOverScene(this, settings, finalScore, difficulty).getScene());
    }

    public void showVersusGameOver(
            Settings settings,
            String winner,
            int winnerScore,
            int loserScore,
            boolean timerMode,
            boolean itemMode) {
        showVersusGameOver(
                settings,
                winner,
                winnerScore,
                loserScore,
                timerMode,
                itemMode,
                "Player 1",
                false,
                null);
    }

    public void showVersusGameOver(
            Settings settings,
            String winner,
            int winnerScore,
            int loserScore,
            boolean timerMode,
            boolean itemMode,
            String currentPlayer,
            boolean isNetworkMode,
            Runnable onPlayAgain) {
        // 창 크기를 원래대로 복원
        restoreWindowSize();

        changeScene(
                new team13.tetris.scenes.VersusGameOverScene(
                                this,
                                settings,
                                winner,
                                winnerScore,
                                loserScore,
                                timerMode,
                                itemMode,
                                currentPlayer,
                                isNetworkMode,
                                onPlayAgain)
                        .getScene());
    }

    public void restoreWindowSize() {
        if (previousWidth > 0 && previousHeight > 0) {
            stage.setWidth(previousWidth);
            stage.setHeight(previousHeight);
            previousWidth = 0;
            previousHeight = 0;
        }
    }

    public void showKeySettings(Settings settings) {
        changeScene(new KeySettingsScene(this, settings).getScene());
    }

    public void showConfirmScene(
            Settings settings, String title, Runnable onConfirm, Runnable onCancel) {
        previousScene = stage.getScene();
        changeScene(new ConfirmScene(this, settings, title, onConfirm, onCancel).getScene());
    }

    public void restorePreviousScene() {
        if (previousScene != null) {
            stage.setScene(previousScene);
            previousScene = null;
        }
    }

    public void changeScene(Scene scene) {
        applyStylesheet(scene);
        stage.setScene(scene);
    }

    public void exitWithSave(Settings settings) {
        settings.setColorBlindMode(isColorBlindMode());
        SettingsRepository.save(settings);
        javafx.application.Platform.exit(); // JavaFX 애플리케이션 스레드 종료
        stage.close();
        System.exit(0); // JVM 강제 종료
    }

    public boolean isColorBlindMode() {
        return colorBlindMode;
    }

    // 대전 모드에서 확장된 창 크기가 아닌 원래 창 크기를 반환
    public double getOriginalWidth() {
        if (previousWidth > 0) {
            return previousWidth;
        }
        return stage.getWidth();
    }

    public void setColorBlindMode(boolean enabled) {
        this.colorBlindMode = enabled;
        applyStylesheet(stage.getScene());
    }

    public void setWindowSize(int width, int height) {
        stage.setWidth(width);
        stage.setHeight(height);

        if (width <= 400) {
            windowSizeClass = "window-small";
        } else if (width <= 600) {
            windowSizeClass = "window-medium";
        } else {
            windowSizeClass = "window-large";
        }

        if (stage.getScene() != null) applyWindowSizeClass(stage.getScene());
    }

    private void applyStylesheet(Scene scene) {
        scene.getStylesheets().clear();

        String cssPath = colorBlindMode ? "/colorblind.css" : "/application.css";

        scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
        applyWindowSizeClass(scene);
    }

    private void applyWindowSizeClass(Scene scene) {
        if (scene != null && scene.getRoot() != null) {
            scene.getRoot()
                    .getStyleClass()
                    .removeAll("window-small", "window-medium", "window-large");
            scene.getRoot().getStyleClass().add(windowSizeClass);
        }
    }

    public void enableArrowAsTab(Scene scene) {
        scene.addEventFilter(
                KeyEvent.KEY_PRESSED,
                new javafx.event.EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent e) {
                        Node focusedNode = scene.getFocusOwner();

                        if (focusedNode == null) return;

                        if (e.getCode() == KeyCode.DOWN || e.getCode() == KeyCode.RIGHT) {
                            e.consume();
                            focusedNode.fireEvent(
                                    new KeyEvent(
                                            KeyEvent.KEY_PRESSED,
                                            "",
                                            "",
                                            KeyCode.TAB,
                                            false,
                                            false,
                                            false,
                                            false));
                        } else if (e.getCode() == KeyCode.UP || e.getCode() == KeyCode.LEFT) {
                            e.consume();
                            focusedNode.fireEvent(
                                    new KeyEvent(
                                            KeyEvent.KEY_PRESSED,
                                            "",
                                            "",
                                            KeyCode.TAB,
                                            true,
                                            false,
                                            false,
                                            false));
                        }
                    }
                });
    }

    // 애플리케이션 종료 시 모든 리소스 정리
    public void cleanup() {
        javafx.application.Platform.exit(); // JavaFX 애플리케이션 스레드 종료
        System.exit(0);
    }
}
