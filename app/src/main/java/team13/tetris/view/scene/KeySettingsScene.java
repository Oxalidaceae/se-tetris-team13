package team13.tetris.view.scene;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import team13.tetris.model.data.Settings;
import team13.tetris.view.SceneManager;

public class KeySettingsScene {
    private final SceneManager manager;

    private String waitingForKey = null;

    public KeySettingsScene(SceneManager manager) {
        this.manager = manager;
    }

    public Scene getScene() {
        Settings settings = manager.getSettings();
        Label title = new Label("Key Settings");
        title.getStyleClass().add("label-title");

        // 각 키 설정 항목
        Button leftBtn = new Button("Move Left: " + settings.getKeyLeft());
        Button rightBtn = new Button("Move Right: " + settings.getKeyRight());
        Button downBtn = new Button("Move Down: " + settings.getKeyDown());
        Button rotateBtn = new Button("Rotate: " + settings.getKeyRotate());
        Button dropBtn = new Button("Drop: " + settings.getKeyDrop());
        Button pauseBtn = new Button("Pause: " + settings.getPause());
        Button backBtn = new Button("Back");
        Button exitBtn = new Button("Exit: " + settings.getExit());

        // 키 변경 대기 모드
    leftBtn.setOnAction(e -> waitingForKey = "LEFT");
    rightBtn.setOnAction(e -> waitingForKey = "RIGHT");
    downBtn.setOnAction(e -> waitingForKey = "DOWN");
    rotateBtn.setOnAction(e -> waitingForKey = "ROTATE");
    dropBtn.setOnAction(e -> waitingForKey = "DROP");
    pauseBtn.setOnAction(e -> waitingForKey = "PAUSE");
    exitBtn.setOnAction(e -> waitingForKey = "EXIT");
    backBtn.setOnAction(e -> manager.showSettings());

        // 현재 Scene에서 키 입력을 감지
        VBox layout = new VBox(15, title, leftBtn, rightBtn, downBtn, rotateBtn, dropBtn, pauseBtn, exitBtn, backBtn);
        layout.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(layout, 400, 400);

        // 키 입력 감지 핸들러
        scene.setOnKeyPressed(event -> {
            if (waitingForKey == null) return;
            event.consume();

            KeyCode key = event.getCode();
            switch (waitingForKey) {
                case "LEFT" -> {
                    settings.setKeyLeft(key.getName());
                    leftBtn.setText("Move Left: " + key.getName());
                    break;
                }
                case "RIGHT" -> {
                    settings.setKeyRight(key.getName());
                    rightBtn.setText("Move Right: " + key.getName());
                    break;
                }
                case "DOWN" -> {
                    settings.setKeyDown(key.getName());
                    downBtn.setText("Move Down: " + key.getName());
                    break;
                }
                case "ROTATE" -> {
                    settings.setKeyRotate(key.getName());
                    rotateBtn.setText("Rotate: " + key.getName());
                    break;
                }
                case "DROP" -> {
                    settings.setKeyDrop(key.getName());
                    dropBtn.setText("Drop: " + key.getName());
                    break;
                }
                case "PAUSE" -> {
                    settings.setPause(key.getName());
                    pauseBtn.setText("Pause: " + key.getName());
                    break;
                }
                case "EXIT" -> {
                    settings.setExit(key.getName());
                    exitBtn.setText("Exit: " + key.getName());
                    break;
                }
            }

            waitingForKey = null; // 입력 완료 후 리셋
            
        });

        manager.enableArrowAsTab(scene);
        return scene;
    }
}
