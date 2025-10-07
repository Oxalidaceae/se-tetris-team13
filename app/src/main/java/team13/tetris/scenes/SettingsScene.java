package team13.tetris.scenes;

import team13.tetris.SceneManager;
import team13.tetris.config.Settings;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;

public class SettingsScene {
    private final SceneManager manager;
    private final Settings settings;

    public SettingsScene(SceneManager manager, Settings settings) {
        this.manager = manager;
        this.settings = settings;
    }

    public Scene getScene() {
        // 타이틀
        Label title = new Label("Settings");
        title.getStyleClass().add("label-title");

        // 화면 크기 버튼
        Button smallBtn = new Button("Small");
        Button mediumBtn = new Button("Medium");
        Button largeBtn = new Button("Large");

        smallBtn.setOnAction(e -> {
            manager.setWindowSize(400, 500);
            settings.setWindowSize("SMALL");
        });
        mediumBtn.setOnAction(e -> {
            manager.setWindowSize(600, 700);
            settings.setWindowSize("MEDIUM");
        });
        largeBtn.setOnAction(e -> {
            manager.setWindowSize(800, 900);
            settings.setWindowSize("LARGE");
        });

        // 키 설정 버튼
        Button keyBtn = new Button("Key Settings");
        keyBtn.setOnAction(e -> manager.showKeySettings(settings));

        // 색맹 모드 토글 버튼
        ToggleButton colorBlindBtn = new ToggleButton();

        // 초기 상태 설정
        boolean isColorBlind = settings.isColorBlindMode();
        colorBlindBtn.setSelected(isColorBlind);
        colorBlindBtn.setText(isColorBlind ? "Color Blind Mode: ON" : "Color Blind Mode: OFF");

        // 토글 버튼 클릭 시 상태 변경
        colorBlindBtn.setOnAction(e -> {
            boolean newState = colorBlindBtn.isSelected();
            colorBlindBtn.setText(newState ? "Color Blind Mode: ON" : "Color Blind Mode: OFF");

            manager.setColorBlindMode(newState);
            settings.setColorBlindMode(newState);
        });

        // 스코어보드 초기화
        Button resetBtn = new Button("Reset Scoreboard");
        resetBtn.setOnAction(e -> {
            System.out.println("Scoreboard reset requested (TODO)");
        });

        // 기본 설정 복원
        Button defaultBtn = new Button("Restore Defaults");
        defaultBtn.setOnAction(e -> {
            manager.setColorBlindMode(false);
            manager.setWindowSize(600, 700);
            settings.setColorBlindMode(false);
            settings.setWindowSize("MEDIUM");
            colorBlindBtn.setSelected(false);
            colorBlindBtn.setText("Color Blind Mode: OFF");
            settings.restoreDefaultKeys();
        });

        // 뒤로가기 버튼
        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> manager.showMainMenu(settings));

        VBox layout = new VBox(15, title, smallBtn, mediumBtn, largeBtn, keyBtn, colorBlindBtn, resetBtn, defaultBtn, backBtn);
        layout.setStyle("-fx-alignment: center;");
        
        Scene scene = new Scene(layout, 600, 700);

        manager.enableArrowAsTab(scene);

        return scene;
    }
}
