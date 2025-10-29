package team13.tetris;

import javafx.application.Application;
import javafx.stage.Stage;
import team13.tetris.config.Settings;
import team13.tetris.config.SettingsRepository;

// Tetris 게임 진입점
public class App extends Application {
    private SceneManager manager;
    private Settings settings;

    @Override
    public void start(Stage primaryStage) {
        // 설정 로드 및 초기화
        settings = SettingsRepository.load();
        manager = new SceneManager(primaryStage);
        manager.showMainMenu(settings);

        // 저장된 설정 적용
        manager.setColorBlindMode(settings.isColorBlindMode());

        switch (settings.getWindowSize()) {
            case "SMALL":
                manager.setWindowSize(400, 500);
                break;
            case "LARGE":
                manager.setWindowSize(800, 900);
                break;
            default:
                manager.setWindowSize(600, 700);
                break;
        }

        primaryStage.setTitle("Tetris");
        primaryStage.show();

        // 종료 시 설정 저장
        primaryStage.setOnCloseRequest(event -> {
            settings.setColorBlindMode(manager.isColorBlindMode());
            settings.setWindowSize(getCurrentWindowSize(primaryStage));
            SettingsRepository.save(settings);
        });
    }

    // 현재 창 크기를 설정 문자열로 변환
    private String getCurrentWindowSize(Stage stage) {
        double width = stage.getWidth();
        if (width <= 450) return "SMALL";
        else if (width >= 750) return "LARGE";
        else return "MEDIUM";
    }

    public static void main(String[] args) {
        launch(args);
    }
}
