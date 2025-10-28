package team13.tetris.scenes;

import team13.tetris.SceneManager;
import team13.tetris.config.Settings;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
        Button leftBtn = new Button("Move Left: " + KeyCode.valueOf(settings.getKeyLeft()).getName());
        Button rightBtn = new Button("Move Right: " + KeyCode.valueOf(settings.getKeyRight()).getName());
        Button downBtn = new Button("Move Down: " + KeyCode.valueOf(settings.getKeyDown()).getName());
        Button rotateBtn = new Button("Rotate: " + KeyCode.valueOf(settings.getKeyRotate()).getName());
        Button dropBtn = new Button("Drop: " + KeyCode.valueOf(settings.getKeyDrop()).getName());
        Button pauseBtn = new Button("Pause: " + KeyCode.valueOf(settings.getPause()).getName());
        Button backBtn = new Button("Back");

        // 키 변경 대기 모드
        leftBtn.setOnAction(e -> waitingForKey = "LEFT");
        rightBtn.setOnAction(e -> waitingForKey = "RIGHT");
        downBtn.setOnAction(e -> waitingForKey = "DOWN");
        rotateBtn.setOnAction(e -> waitingForKey = "ROTATE");
        dropBtn.setOnAction(e -> waitingForKey = "DROP");
        pauseBtn.setOnAction(e -> waitingForKey = "PAUSE");
        backBtn.setOnAction(e -> manager.showSettings(settings));

        // 현재 Scene에서 키 입력을 감지
        VBox layout = new VBox(15, title, leftBtn, rightBtn, downBtn, rotateBtn, dropBtn, pauseBtn, backBtn);
        layout.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(layout, 400, 400);

        // 키 입력 감지 핸들러
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (waitingForKey == null) return;
            event.consume();

            KeyCode key = event.getCode();

            // undefined 키, windows 키, os 종속 키 방지
            if(key == KeyCode.UNDEFINED || key == KeyCode.WINDOWS 
            || key == KeyCode.META || key == KeyCode.PRINTSCREEN || key == KeyCode.CLEAR 
            || key == KeyCode.CAPS || key == KeyCode.NUM_LOCK) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Invalid Key");
                alert.setHeaderText(null);
                alert.setContentText("사용할 수 없는 키입니다. 다른 키를 눌러주세요.");
                alert.showAndWait();
                waitingForKey = null;
                return;
            }

            
            // 중복 키 체크
            if(settings.isKeyAlreadyUsed(key.toString())) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Duplicate Key");
                alert.setHeaderText(null);
                alert.setContentText("이미 다른 동작에 사용 중인 키입니다: " + key.getName());
                alert.showAndWait();
                waitingForKey = null;
                return;
            }

            switch (waitingForKey) {
                case "LEFT" -> {
                    settings.setKeyLeft(key.toString());
                    leftBtn.setText("Move Left: " + key.getName());
                    break;
                }
                case "RIGHT" -> {
                    settings.setKeyRight(key.toString());
                    rightBtn.setText("Move Right: " + key.getName());
                    break;
                }
                case "DOWN" -> {
                    settings.setKeyDown(key.toString());
                    downBtn.setText("Move Down: " + key.getName());
                    break;
                }
                case "ROTATE" -> {
                    settings.setKeyRotate(key.toString());
                    rotateBtn.setText("Rotate: " + key.getName());
                    break;
                }
                case "DROP" -> {
                    settings.setKeyDrop(key.toString());
                    dropBtn.setText("Drop: " + key.getName());
                    break;
                }
                case "PAUSE" -> {
                    settings.setPause(key.toString());
                    pauseBtn.setText("Pause: " + key.getName());
                    break;
                }
            }

            waitingForKey = null; // 입력 완료 후 리셋
            
        });

        return scene;
    }
}
