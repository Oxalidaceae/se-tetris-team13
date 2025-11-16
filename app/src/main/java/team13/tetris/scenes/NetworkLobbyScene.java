package team13.tetris.scenes;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;
import javafx.scene.text.TextAlignment;

// 대기 화면
public class NetworkLobbyScene {
    private final SceneManager manager;
    private final Settings settings;
    private final boolean isHost;

    private Scene scene;
    private final VBox root;
    
    private Label statusLabel;
    
    // Cancel 버튼 콜백
    private Runnable onCancelCallback;

    private Label myReadyLabel;
    private Label opponentReadyLabel;

    private Button readyButton;

    private Label gameModeLabel;
    private RadioButton normalModeButton;
    private RadioButton itemModeButton;
    
    private boolean myReady = false;
    private boolean opponentReady = false;
    
    public NetworkLobbyScene(SceneManager manager, Settings settings, boolean isHost) {
        this.manager = manager;
        this.settings = settings;
        this.isHost = isHost;
        
        root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("menu-root");
        root.setPadding(new Insets(40));
        
        Label titleLabel = new Label("Game Lobby");
        titleLabel.getStyleClass().add("label-title");
        titleLabel.setStyle("-fx-font-size: 36px;");

        statusLabel = new Label(/* 컨트롤러에서 글씨 설정*/);
        statusLabel.getStyleClass().add("label");
        statusLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: yellow;");
        statusLabel.setTextAlignment(TextAlignment.CENTER);
        
        // 게임 모드 선택 (서버만)
        VBox gameModeBox = new VBox(10);
        gameModeBox.setAlignment(Pos.CENTER);
        
        gameModeLabel = new Label("Select Game Mode:");
        gameModeLabel.getStyleClass().add("label");
        gameModeLabel.setStyle("-fx-font-size: 20px;");
        
        ToggleGroup modeGroup = new ToggleGroup();
        
        normalModeButton = new RadioButton("Normal Mode");
        normalModeButton.setToggleGroup(modeGroup);
        normalModeButton.setSelected(true);
        normalModeButton.getStyleClass().add("radio-button");
        normalModeButton.setStyle("-fx-font-size: 18px;");
        
        itemModeButton = new RadioButton("Item Mode");
        itemModeButton.setToggleGroup(modeGroup);
        itemModeButton.getStyleClass().add("radio-button");
        itemModeButton.setStyle("-fx-font-size: 18px;");
        
        if (isHost) {
            gameModeBox.getChildren().addAll(gameModeLabel, normalModeButton, itemModeButton);
        } else {
            // 클라이언트는 모드 표시만
            gameModeLabel.setText("Game Mode: Waiting...");
            normalModeButton.setVisible(false);
            itemModeButton.setVisible(false);
            gameModeBox.getChildren().add(gameModeLabel);
        }
        
        // 준비 상태
        VBox readyBox = new VBox(15);
        readyBox.setAlignment(Pos.CENTER);
        
        Label readyTitleLabel = new Label("Player Status:");
        readyTitleLabel.getStyleClass().add("label");
        readyTitleLabel.setStyle("-fx-font-size: 20px;");
        
        myReadyLabel = new Label((isHost ? "Host" : "Client") + ": Not Ready");
        myReadyLabel.getStyleClass().add("label");
        myReadyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: red;");
        
        opponentReadyLabel = new Label((isHost ? "Client" : "Host") + ": Not Ready");
        opponentReadyLabel.getStyleClass().add("label");
        opponentReadyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: red;");
        
        readyBox.getChildren().addAll(readyTitleLabel, myReadyLabel, opponentReadyLabel);
        
        // 준비 버튼
        readyButton = new Button("Ready");
        readyButton.getStyleClass().add("menu-button");
        readyButton.setStyle("-fx-font-size: 20px; -fx-min-width: 200px;");
        
        Button backButton = new Button("Cancel");
        backButton.getStyleClass().add("menu-button");
        backButton.setStyle("-fx-font-size: 20px; -fx-min-width: 200px;");
        
        backButton.setOnAction(e -> {
            if (onCancelCallback != null) {
                onCancelCallback.run();
            }
            manager.showMainMenu(settings);
        });
        
        HBox buttonBox = new HBox(20, readyButton, backButton);
        buttonBox.setAlignment(Pos.CENTER);
        
        root.getChildren().addAll(
            titleLabel,
            statusLabel,
            new Label(), // 간격
            gameModeBox,
            new Label(), // 간격
            readyBox,
            new Label(), // 간격
            buttonBox
        );
        
        scene = new Scene(root);
    }
    
    public Scene getScene() {
        return scene;
    }
    
    public Button getReadyButton() {
        return readyButton;
    }
    
    public boolean isItemMode() {
        return itemModeButton.isSelected();
    }
    
    public void setStatusText(String text) {
        Platform.runLater(() -> statusLabel.setText(text));
    }
    
    public void setGameMode(String mode) {
        Platform.runLater(() -> {
            if (!isHost) {
                gameModeLabel.setText("Game Mode: " + mode);
            }
        });
    }
    
    public void setMyReady(boolean ready) {
        this.myReady = ready;
        Platform.runLater(() -> {
            myReadyLabel.setText((isHost ? "Host" : "Client") + ": " + (ready ? "Ready!" : "Not Ready"));
            myReadyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: " + (ready ? "green" : "red") + ";");
            readyButton.setDisable(ready);
        });
    }
    
    public void setOpponentReady(boolean ready) {
        this.opponentReady = ready;
        Platform.runLater(() -> {
            opponentReadyLabel.setText((isHost ? "Client" : "Host") + ": " + (ready ? "Ready!" : "Not Ready"));
            opponentReadyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: " + (ready ? "green" : "red") + ";");
        });
    }
    
    public boolean areBothReady() {
        return myReady && opponentReady;
    }
    
    public void setOnCancelCallback(Runnable callback) {
        this.onCancelCallback = callback;
    }
}
