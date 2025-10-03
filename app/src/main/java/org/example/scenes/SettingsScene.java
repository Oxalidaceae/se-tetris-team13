package org.example.scenes;

import org.example.SceneManager;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;

public class SettingsScene {
    private final SceneManager manager;

    public SettingsScene(SceneManager manager) {
        this.manager = manager;
    }

    public Scene getScene() {
        // 타이틀
        Label title = new Label("Settings");
        title.getStyleClass().add("label-title");

        // 화면 크기 버튼
        Button smallBtn = new Button("Small");
        Button mediumBtn = new Button("Medium");
        Button largeBtn = new Button("Large");

        // 키 설정 버튼
        Button keyBtn = new Button("Key Settings");

        // 색맹 모드 토글 버튼
        ToggleButton colorBlindBtn = new ToggleButton("Color Blind Mode OFF");
        colorBlindBtn.setOnAction(e -> {
            boolean enabled = colorBlindBtn.isSelected();
            colorBlindBtn.setText(enabled ? "Color Blind Mode: ON" : "Color Blind Mode: OFF");
            
            manager.setColorBlindMode(enabled);
        });

        // 스코어보드 초기화
        Button resetBtn = new Button("Reset Scoreboard");

        // 기본 설정 복원
        Button defaultBtn = new Button("Restore Defaults");

        // 뒤로가기 버튼
        Button backBtn = new Button("Back");

        backBtn.setOnAction(e -> manager.changeScene(new MainMenuScene(manager).getScene()));

        VBox layout = new VBox(15, title, smallBtn, mediumBtn, largeBtn, keyBtn, colorBlindBtn, resetBtn, defaultBtn, backBtn);
        layout.setStyle("-fx-alignment: center;");
        
        Scene scene = new Scene(layout, 400, 500);

        scene.getStylesheets().add(
            getClass().getResource("/application.css").toExternalForm()
        );

        return scene;
    }
}
