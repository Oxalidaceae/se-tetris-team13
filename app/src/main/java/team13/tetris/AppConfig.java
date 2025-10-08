package team13.tetris;

import javafx.stage.Stage;
import team13.tetris.controller.GameSessionController;
import team13.tetris.model.data.ScoreBoard;
import team13.tetris.model.data.Settings;
import team13.tetris.model.repository.SettingsRepository;
import team13.tetris.view.SceneManager;

public class AppConfig {
    public static void initialize(Stage stage) {
        Settings settings = SettingsRepository.load();
        ScoreBoard scoreBoard = new ScoreBoard();

        SceneManager sceneManager = new SceneManager(stage);
        GameSessionController gameSessionController = new GameSessionController(settings, scoreBoard, sceneManager);
        sceneManager.configure(settings, scoreBoard, gameSessionController);

        stage.setTitle("Team13 Tetris");
        applyWindowSize(sceneManager, settings.getWindowSize());
        sceneManager.setColorBlindMode(settings.isColorBlindMode());

        sceneManager.showMainMenu();
        stage.show();
    }

    private static void applyWindowSize(SceneManager manager, String size) {
        if (size == null) {
            manager.setWindowSize(600, 700);
            return;
        }
        switch (size.toUpperCase()) {
            case "SMALL" -> manager.setWindowSize(400, 500);
            case "LARGE" -> manager.setWindowSize(800, 900);
            default -> manager.setWindowSize(600, 700);
        }
    }
}
