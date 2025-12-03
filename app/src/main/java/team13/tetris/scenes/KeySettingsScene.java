package team13.tetris.scenes;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;

public class KeySettingsScene {
    private final SceneManager manager;
    private final Settings settings;
    private String waitingForKey = null;

    // 모든 버튼들을 참조하기 위한 필드들
    private Button leftBtn, rightBtn, downBtn, rotateBtn, dropBtn, pauseBtn;
    private Button leftBtn2, rightBtn2, downBtn2, rotateBtn2, dropBtn2;

    public KeySettingsScene(SceneManager manager, Settings settings) {
        this.manager = manager;
        this.settings = settings;
    }

    public Scene getScene() {
        Label title = new Label("Key Settings");
        title.getStyleClass().add("label-title");

        // Player 1 키 설정 버튼들
        Label player1Title = new Label("Player 1 Controls");
        player1Title.getStyleClass().add("label");
        player1Title.setStyle("-fx-font-size: 16px; -fx-text-fill: #4CAF50;");

        leftBtn = new Button("Move Left: " + KeyCode.valueOf(settings.getKeyLeft()).getName());
        rightBtn = new Button("Move Right: " + KeyCode.valueOf(settings.getKeyRight()).getName());
        downBtn = new Button("Move Down: " + KeyCode.valueOf(settings.getKeyDown()).getName());
        rotateBtn = new Button("Rotate: " + KeyCode.valueOf(settings.getKeyRotate()).getName());
        dropBtn = new Button("Drop: " + KeyCode.valueOf(settings.getKeyDrop()).getName());
        pauseBtn = new Button("Pause: " + KeyCode.valueOf(settings.getPause()).getName());

        // Player 2 키 설정 버튼들
        Label player2Title = new Label("Player 2 Controls");
        player2Title.getStyleClass().add("label");
        player2Title.setStyle("-fx-font-size: 16px; -fx-text-fill: #FF5722;");

        leftBtn2 = new Button("Move Left: " + KeyCode.valueOf(settings.getKeyLeftP2()).getName());
        rightBtn2 =
                new Button("Move Right: " + KeyCode.valueOf(settings.getKeyRightP2()).getName());
        downBtn2 = new Button("Move Down: " + KeyCode.valueOf(settings.getKeyDownP2()).getName());
        rotateBtn2 = new Button("Rotate: " + KeyCode.valueOf(settings.getKeyRotateP2()).getName());
        dropBtn2 = new Button("Drop: " + KeyCode.valueOf(settings.getKeyDropP2()).getName());

        Button backBtn = new Button("Back");

        // Player 1 버튼 이벤트 핸들러
        leftBtn.setOnAction(
                e -> {
                    waitingForKey = "LEFT";
                    highlightSelectedButton(leftBtn);
                });
        rightBtn.setOnAction(
                e -> {
                    waitingForKey = "RIGHT";
                    highlightSelectedButton(rightBtn);
                });
        downBtn.setOnAction(
                e -> {
                    waitingForKey = "DOWN";
                    highlightSelectedButton(downBtn);
                });
        rotateBtn.setOnAction(
                e -> {
                    waitingForKey = "ROTATE";
                    highlightSelectedButton(rotateBtn);
                });
        dropBtn.setOnAction(
                e -> {
                    waitingForKey = "DROP";
                    highlightSelectedButton(dropBtn);
                });
        pauseBtn.setOnAction(
                e -> {
                    waitingForKey = "PAUSE";
                    highlightSelectedButton(pauseBtn);
                });

        // Player 2 버튼 이벤트 핸들러
        leftBtn2.setOnAction(
                e -> {
                    waitingForKey = "LEFT_P2";
                    highlightSelectedButton(leftBtn2);
                });
        rightBtn2.setOnAction(
                e -> {
                    waitingForKey = "RIGHT_P2";
                    highlightSelectedButton(rightBtn2);
                });
        downBtn2.setOnAction(
                e -> {
                    waitingForKey = "DOWN_P2";
                    highlightSelectedButton(downBtn2);
                });
        rotateBtn2.setOnAction(
                e -> {
                    waitingForKey = "ROTATE_P2";
                    highlightSelectedButton(rotateBtn2);
                });
        dropBtn2.setOnAction(
                e -> {
                    waitingForKey = "DROP_P2";
                    highlightSelectedButton(dropBtn2);
                });

        backBtn.setOnAction(e -> manager.showSettings(settings));

        // Player 1 컬럼
        VBox player1Column =
                new VBox(10, player1Title, leftBtn, rightBtn, downBtn, rotateBtn, dropBtn);
        player1Column.setStyle("-fx-alignment: center; -fx-padding: 20;");

        // Player 2 컬럼
        VBox player2Column =
                new VBox(10, player2Title, leftBtn2, rightBtn2, downBtn2, rotateBtn2, dropBtn2);
        player2Column.setStyle("-fx-alignment: center; -fx-padding: 20;");

        // 2열 레이아웃
        HBox twoColumnLayout = new HBox(30, player1Column, player2Column);
        twoColumnLayout.setStyle("-fx-alignment: center;");

        // 전체 레이아웃
        VBox layout = new VBox(20, title, twoColumnLayout, pauseBtn, backBtn);
        layout.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(layout, 600, 500);

        scene.addEventFilter(
                KeyEvent.KEY_PRESSED,
                event -> {
                    if (waitingForKey == null) return;
                    event.consume();

                    KeyCode key = event.getCode();

                    if (key == KeyCode.UNDEFINED
                            || key == KeyCode.WINDOWS
                            || key == KeyCode.META
                            || key == KeyCode.PRINTSCREEN
                            || key == KeyCode.CLEAR
                            || key == KeyCode.CAPS
                            || key == KeyCode.NUM_LOCK) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Invalid Key");
                        alert.setHeaderText(null);
                        alert.setContentText("사용할 수 없는 키입니다. 다른 키를 눌러주세요.");
                        alert.showAndWait();
                        waitingForKey = null;
                        clearAllHighlights(); // 하이라이트 제거
                        return;
                    }

                    if (settings.isKeyAlreadyUsed(key.toString())) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Duplicate Key");
                        alert.setHeaderText(null);
                        alert.setContentText("이미 다른 동작에 사용 중인 키입니다: " + key.getName());
                        alert.showAndWait();
                        waitingForKey = null;
                        clearAllHighlights(); // 하이라이트 제거
                        return;
                    }

                    switch (waitingForKey) {
                            // Player 1 키 설정
                        case "LEFT" -> {
                            settings.setKeyLeft(key.toString());
                            leftBtn.setText("Move Left: " + key.getName());
                        }
                        case "RIGHT" -> {
                            settings.setKeyRight(key.toString());
                            rightBtn.setText("Move Right: " + key.getName());
                        }
                        case "DOWN" -> {
                            settings.setKeyDown(key.toString());
                            downBtn.setText("Move Down: " + key.getName());
                        }
                        case "ROTATE" -> {
                            settings.setKeyRotate(key.toString());
                            rotateBtn.setText("Rotate: " + key.getName());
                        }
                        case "DROP" -> {
                            settings.setKeyDrop(key.toString());
                            dropBtn.setText("Drop: " + key.getName());
                        }
                        case "PAUSE" -> {
                            settings.setPause(key.toString());
                            pauseBtn.setText("Pause: " + key.getName());
                        }
                            // Player 2 키 설정
                        case "LEFT_P2" -> {
                            settings.setKeyLeftP2(key.toString());
                            leftBtn2.setText("Move Left: " + key.getName());
                        }
                        case "RIGHT_P2" -> {
                            settings.setKeyRightP2(key.toString());
                            rightBtn2.setText("Move Right: " + key.getName());
                        }
                        case "DOWN_P2" -> {
                            settings.setKeyDownP2(key.toString());
                            downBtn2.setText("Move Down: " + key.getName());
                        }
                        case "ROTATE_P2" -> {
                            settings.setKeyRotateP2(key.toString());
                            rotateBtn2.setText("Rotate: " + key.getName());
                        }
                        case "DROP_P2" -> {
                            settings.setKeyDropP2(key.toString());
                            dropBtn2.setText("Drop: " + key.getName());
                        }
                        default -> {}
                    }

                    waitingForKey = null;
                    clearAllHighlights(); // 키 입력 후 모든 하이라이트 제거
                });

        return scene;
    }

    // 선택된 버튼을 하이라이트 표시
    private void highlightSelectedButton(Button selectedButton) {
        clearAllHighlights(); // 먼저 모든 하이라이트 제거
        selectedButton.setStyle(
                "-fx-background-color: #FFD700; -fx-text-fill: #000000; -fx-font-weight: bold;");
    }

    // 모든 버튼의 하이라이트 제거
    private void clearAllHighlights() {
        String defaultStyle = "";

        // Player 1 버튼들
        leftBtn.setStyle(defaultStyle);
        rightBtn.setStyle(defaultStyle);
        downBtn.setStyle(defaultStyle);
        rotateBtn.setStyle(defaultStyle);
        dropBtn.setStyle(defaultStyle);
        pauseBtn.setStyle(defaultStyle);

        // Player 2 버튼들
        leftBtn2.setStyle(defaultStyle);
        rightBtn2.setStyle(defaultStyle);
        downBtn2.setStyle(defaultStyle);
        rotateBtn2.setStyle(defaultStyle);
        dropBtn2.setStyle(defaultStyle);
    }
}
