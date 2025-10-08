package team13.tetris.view;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import team13.tetris.controller.GameSessionController;
import team13.tetris.model.data.ScoreBoard;
import team13.tetris.model.data.Settings;
import team13.tetris.model.repository.SettingsRepository;
import team13.tetris.view.scene.GameOverScene;
import team13.tetris.view.scene.GameScene;
import team13.tetris.view.scene.KeySettingsScene;
import team13.tetris.view.scene.MainMenuScene;
import team13.tetris.view.scene.ScoreboardScene;
import team13.tetris.view.scene.SettingsScene;

public class SceneManager {
    private final Stage stage;

    private Settings settings;
    private ScoreBoard scoreBoard;
    private GameSessionController gameSessionController;
    private boolean colorBlindMode = false;

    public SceneManager(Stage stage) {
        this.stage = stage;
    }

    public void configure(Settings settings, ScoreBoard scoreBoard, GameSessionController gameSessionController) {
        this.settings = settings;
        this.scoreBoard = scoreBoard;
        this.gameSessionController = gameSessionController;
    }

    public Settings getSettings() { return settings; }
    public ScoreBoard getScoreBoard() { return scoreBoard; }
    public GameSessionController getGameSessionController() { return gameSessionController; }

    public void showMainMenu() {
        ensureConfigured();
        gameSessionController.stop();
        changeScene(new MainMenuScene(this).getScene());
    }

    public void showSettings() {
        ensureConfigured();
        changeScene(new SettingsScene(this).getScene());
    }

    public void showScoreboard() {
        ensureConfigured();
        changeScene(new ScoreboardScene(this).getScene());
    }

    public void showKeySettings() {
        ensureConfigured();
        changeScene(new KeySettingsScene(this).getScene());
    }

    public void showGame() {
        ensureConfigured();
        gameSessionController.stop();
        GameScene view = new GameScene(this);
        Scene scene = view.buildScene(gameSessionController, gameSessionController.getKeyInputHandler());
        gameSessionController.bind(view);
        changeScene(scene);
        scene.getRoot().requestFocus();
        gameSessionController.start();
    }

    public void showGameOver(int finalScore) {
        ensureConfigured();
        if (gameSessionController != null) {
            gameSessionController.stop();
        }
        changeScene(new GameOverScene(this, finalScore).getScene());
    }

    public void changeScene(Scene scene) {
        applyStylesheet(scene);
        stage.setScene(scene);
    }

    public void exitWithSave() {
        ensureConfigured();
        settings.setColorBlindMode(isColorBlindMode());
        SettingsRepository.save(settings);
        if (gameSessionController != null) {
            gameSessionController.stop();
        }
        stage.close();
    }

    public boolean isColorBlindMode() { return colorBlindMode; }

    public void setColorBlindMode(boolean enabled) {
        this.colorBlindMode = enabled;
        Scene current = stage.getScene();
        if (current != null) {
            applyStylesheet(current);
        }
    }

    public void setWindowSize(int width, int height) {
        stage.setWidth(width);
        stage.setHeight(height);
    }

    private void applyStylesheet(Scene scene) {
        if (scene == null) return;
        scene.getStylesheets().clear();

    String cssPath = colorBlindMode ? "/css/colorblind.css" : "/css/application.css";
        java.net.URL resource = getClass().getResource(cssPath);
        if (resource != null) {
            scene.getStylesheets().add(resource.toExternalForm());
        } else {
            System.err.println("[SceneManager] Stylesheet not found: " + cssPath);
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
                        false, false, false, false
                    ));
                } else if (e.getCode() == KeyCode.UP || e.getCode() == KeyCode.LEFT) {
                    e.consume();
                    focusedNode.fireEvent(new KeyEvent(
                        KeyEvent.KEY_PRESSED,
                        "",
                        "",
                        KeyCode.TAB,
                        true, false, false, false
                    ));
                }
            }
        });
    }

    private void ensureConfigured() {
        if (settings == null || scoreBoard == null) {
            throw new IllegalStateException("SceneManager is not configured. Call configure() first.");
        }
    }
}
