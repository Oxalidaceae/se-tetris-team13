package org.example.scenes;

import org.example.SceneManager;
import org.example.config.Settings;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

public class KeySettingsScene {
    private final SceneManager manager;
    private final Settings settings;

    // 키 변경 중 어떤 항목을 수정 중인지 기억
    private String waitingForKey = null;

    public KeySettingsScene(SceneManager manager, Settings settings) {
        this.manager = manager;
        this.settings = settings;
    }

    public Scene getScene() {
        Label title = new Label("Key Settings");
        title.getStyleClass().add("label-title");

        // 각 키 설정 항목
        Button leftBtn = new Button("Move Left: " + settings.getKeyLeft());
        Button rightBtn = new Button("Move Right: " + settings.getKeyRight());
        Button rotateBtn = new Button("Rotate: " + settings.getKeyRotate());
        Button dropBtn = new Button("Drop: " + settings.getKeyDrop());

        // 키 변경 대기 모드
        leftBtn.setOnAction(e -> waitingForKey = "LEFT");
        rightBtn.setOnAction(e -> waitingForKey = "RIGHT");
        rotateBtn.setOnAction(e -> waitingForKey = "ROTATE");
        dropBtn.setOnAction(e -> waitingForKey = "DROP");

        // 현재 Scene에서 키 입력을 감지
        VBox layout = new VBox(15, title, leftBtn, rightBtn, rotateBtn, dropBtn);
        layout.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(layout, 400, 400);

        // 키 입력 감지 핸들러
        scene.setOnKeyPressed(event -> {
            if (waitingForKey == null) return;
            KeyCode key = event.getCode();

            switch (waitingForKey) {
                case "LEFT" -> {
                    settings.setKeyLeft(key.getName());
                    leftBtn.setText("Move Left: " + key.getName());
                }
                case "RIGHT" -> {
                    settings.setKeyRight(key.getName());
                    rightBtn.setText("Move Right: " + key.getName());
                }
                case "ROTATE" -> {
                    settings.setKeyRotate(key.getName());
                    rotateBtn.setText("Rotate: " + key.getName());
                }
                case "DROP" -> {
                    settings.setKeyDrop(key.getName());
                    dropBtn.setText("Drop: " + key.getName());
                }
            }

            waitingForKey = null; // 입력 완료 후 리셋
        });

        // 뒤로가기 버튼
        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> manager.showSettings(settings));

        layout.getChildren().add(backBtn);

        return scene;
    }
}
