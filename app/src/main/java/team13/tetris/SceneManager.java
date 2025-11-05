package team13.tetris;

import team13.tetris.config.Settings;
import team13.tetris.config.SettingsRepository;
import team13.tetris.data.ScoreBoard;
import team13.tetris.game.controller.CompositeGameStateListener;
import team13.tetris.game.controller.GameSceneController;
import team13.tetris.game.logic.GameEngine;
import team13.tetris.game.model.Board;
import team13.tetris.input.KeyInputHandler;
import team13.tetris.scenes.DifficultySelectionScene;
import team13.tetris.scenes.ExitScene;
import team13.tetris.scenes.GameOverScene;
import team13.tetris.scenes.GameScene;
import team13.tetris.scenes.KeySettingsScene;
import team13.tetris.scenes.MainMenuScene;
import team13.tetris.scenes.ScoreboardScene;
import team13.tetris.scenes.SettingsScene;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class SceneManager {
    private final Stage stage;
    private boolean colorBlindMode = false;
    private String windowSizeClass = "window-medium";
    private Scene previousScene = null;

    public SceneManager(Stage stage) {
        this.stage = stage;
    }

    public void showMainMenu(Settings settings) {
        changeScene(new MainMenuScene(this, settings).getScene());
    }

    public void showSettings(Settings settings) {
        changeScene(new SettingsScene(this, settings).getScene());
    }

    public void showScoreboard(Settings settings) {
        changeScene(new ScoreboardScene(this, settings).getScene());
    }

    public void showScoreboard(
        Settings settings,
        String name,
        int score,
        ScoreBoard.ScoreEntry.Mode difficulty
    ) {
        ScoreboardScene scene = new ScoreboardScene(this, settings, name, score, difficulty);
        changeScene(scene.getScene());
    }

    public void showDifficultySelection(Settings settings) {
        changeScene(new DifficultySelectionScene(this, settings).getScene());
    }

    public void showGame(Settings settings, ScoreBoard.ScoreEntry.Mode difficulty) {
        Board board = new Board(10, 20);
        CompositeGameStateListener composite = new CompositeGameStateListener();
        GameEngine engine = new GameEngine(board, composite, difficulty);
        KeyInputHandler keyInputHandler = new KeyInputHandler(settings);
        GameScene gameScene = new GameScene(this, settings, engine, difficulty);
        GameSceneController gameController = new GameSceneController(
            gameScene,
            this,
            settings,
            keyInputHandler
        );

        gameController.setEngine(engine);
        composite.add(gameController);

        Scene scene = gameScene.createScene();

        gameController.attachToScene(scene);
        changeScene(scene);
        engine.startNewGame();
        gameScene.requestFocus();
    }

    public void showGameOver(
        Settings settings,
        int finalScore,
        ScoreBoard.ScoreEntry.Mode difficulty
    ) {
        changeScene(new GameOverScene(this, settings, finalScore, difficulty).getScene());
    }

    public void showKeySettings(Settings settings) {
        changeScene(new KeySettingsScene(this, settings).getScene());
    }

    public void showExitScene(Settings settings, Runnable onCancel) {
        previousScene = stage.getScene();
        changeScene(new ExitScene(this, settings, onCancel).getScene());
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
        stage.close();
    }

    public boolean isColorBlindMode() {
        return colorBlindMode;
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

        scene
            .getStylesheets()
            .add(getClass().getResource(cssPath).toExternalForm());
        applyWindowSizeClass(scene);
    }
    
    private void applyWindowSizeClass(Scene scene) {
        if (scene != null && scene.getRoot() != null) {
            scene
                .getRoot()
                .getStyleClass()
                .removeAll("window-small", "window-medium", "window-large");
            scene
                .getRoot()
                .getStyleClass()
                .add(windowSizeClass);
        }
    }

    public void enableArrowAsTab(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, new javafx.event.EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                Node focusedNode = scene.getFocusOwner();
                
                if (focusedNode == null) return;

                if (e.getCode() == KeyCode.DOWN || e.getCode() == KeyCode.RIGHT) {
                    e.consume();
                    focusedNode.fireEvent(new KeyEvent(
                        KeyEvent.KEY_PRESSED,
                        "",
                        "",
                        KeyCode.TAB,
                        false,
                        false,
                        false,
                        false
                    ));
                } else if (e.getCode() == KeyCode.UP || e.getCode() == KeyCode.LEFT) {
                    e.consume();
                    focusedNode.fireEvent(new KeyEvent(
                        KeyEvent.KEY_PRESSED,
                        "",
                        "",
                        KeyCode.TAB,
                        true,
                        false,
                        false,
                        false
                    ));
                }
            }
        });
    }
}
