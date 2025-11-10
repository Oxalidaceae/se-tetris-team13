package team13.tetris;

import javafx.application.Application;
import javafx.stage.Stage;
import team13.tetris.config.Settings;
import team13.tetris.config.SettingsRepository;

public class App extends Application {
    private SceneManager manager;
    private Settings settings;

    @Override
    public void start(Stage primaryStage) {
        settings = SettingsRepository.load();
        manager = new SceneManager(primaryStage);
        manager.showMainMenu(settings);
        manager.setColorBlindMode(settings.isColorBlindMode());

        switch (settings.getWindowSize()) {
            case "SMALL" -> manager.setWindowSize(400, 500);
            case "LARGE" -> manager.setWindowSize(800, 900);
            default -> manager.setWindowSize(600, 700);
        }

        primaryStage.setTitle("Tetris");
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            settings.setColorBlindMode(manager.isColorBlindMode());
            // 대전 모드 중이면 이전 크기 기준으로, 아니면 현재 크기 기준으로 저장
            if (manager.isInVersusMode()) {
                settings.setWindowSize(getCurrentWindowSize(manager.getPreviousWidth()));
            } else {
                settings.setWindowSize(getCurrentWindowSize(primaryStage.getWidth()));
            }
            SettingsRepository.save(settings);
        });
    }

    // OS별로 약간의 픽셀 차이가 존재하므로 범위로 판단
    private String getCurrentWindowSize(double width) {
        if (width <= 450) {
            return "SMALL";
        } else if (width >= 750) {
            return "LARGE";
        } else {
            return "MEDIUM";
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
