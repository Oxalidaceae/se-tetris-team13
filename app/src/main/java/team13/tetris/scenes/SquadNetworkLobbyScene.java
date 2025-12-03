package team13.tetris.scenes;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;
import team13.tetris.network.protocol.GameModeMessage;

// Squad PVP 대기 화면 (3명)
public class SquadNetworkLobbyScene {
    @SuppressWarnings("unused")
    private final SceneManager manager;

    @SuppressWarnings("unused")
    private final Settings settings;

    private final boolean isHost;

    private Scene scene;
    private final VBox root;

    private Label statusLabel;

    // Cancel 버튼 콜백
    private Runnable onCancelCallback;

    private Label hostReadyLabel;
    private Label client1ReadyLabel;
    private Label client2ReadyLabel;

    private Button readyButton;

    private Label gameModeLabel;
    private Button normalModeButton;
    private Button itemModeButton;
    private Button timerModeButton;
    private GameModeMessage.GameMode selectedGameMode = GameModeMessage.GameMode.NORMAL;

    private boolean hostReady = false;
    private boolean client1Ready = false;
    private boolean client2Ready = false;

    private boolean isClient1Connected = false;
    private boolean isClient2Connected = false;

    public SquadNetworkLobbyScene(SceneManager manager, Settings settings, boolean isHost) {
        this.manager = manager;
        this.settings = settings;
        this.isHost = isHost;

        root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("menu-root");
        root.setPadding(new Insets(40));

        Label titleLabel = new Label("Squad PVP Lobby (3 Players)");
        titleLabel.getStyleClass().add("label-title");

        statusLabel = new Label(/* 컨트롤러에서 글씨 설정*/ );
        statusLabel.getStyleClass().add("label");
        statusLabel.setStyle("-fx-text-fill: yellow;");
        statusLabel.setTextAlignment(TextAlignment.CENTER);

        // 게임 모드 선택 (서버만)
        VBox gameModeBox = new VBox(10);
        gameModeBox.setAlignment(Pos.CENTER);

        gameModeLabel = new Label("Select Game Mode:");
        gameModeLabel.getStyleClass().add("label");

        normalModeButton = new Button("Normal Mode");
        normalModeButton.getStyleClass().addAll("button", "selected");

        itemModeButton = new Button("Item Mode");
        itemModeButton.getStyleClass().add("button");

        timerModeButton = new Button("Timer Mode");
        timerModeButton.getStyleClass().add("button");

        normalModeButton.setOnAction(
                e -> {
                    selectedGameMode = GameModeMessage.GameMode.NORMAL;
                    normalModeButton.getStyleClass().add("selected");
                    itemModeButton.getStyleClass().remove("selected");
                    timerModeButton.getStyleClass().remove("selected");
                });
        itemModeButton.setOnAction(
                e -> {
                    selectedGameMode = GameModeMessage.GameMode.ITEM;
                    normalModeButton.getStyleClass().remove("selected");
                    itemModeButton.getStyleClass().add("selected");
                    timerModeButton.getStyleClass().remove("selected");
                });
        timerModeButton.setOnAction(
                e -> {
                    selectedGameMode = GameModeMessage.GameMode.TIMER;
                    normalModeButton.getStyleClass().remove("selected");
                    itemModeButton.getStyleClass().remove("selected");
                    timerModeButton.getStyleClass().add("selected");
                });

        HBox modeButtonBox = new HBox(10, normalModeButton, itemModeButton, timerModeButton);
        modeButtonBox.setAlignment(Pos.CENTER);

        if (isHost) {
            gameModeBox.getChildren().addAll(gameModeLabel, modeButtonBox);
        } else {
            // 클라이언트는 모드 표시만
            gameModeLabel.setText("Game Mode: Waiting...");
            gameModeBox.getChildren().add(gameModeLabel);
        }

        // 준비 상태 (3명)
        VBox readyBox = new VBox(15);
        readyBox.setAlignment(Pos.CENTER);

        Label readyTitleLabel = new Label("Player Status:");
        readyTitleLabel.getStyleClass().add("label");

        hostReadyLabel = new Label("Host: Not Ready");
        hostReadyLabel.getStyleClass().add("label");
        hostReadyLabel.setStyle("-fx-text-fill: red;");

        client1ReadyLabel = new Label("Client 1: Waiting...");
        client1ReadyLabel.getStyleClass().add("label");
        client1ReadyLabel.setStyle("-fx-text-fill: gray;");

        client2ReadyLabel = new Label("Client 2: Waiting...");
        client2ReadyLabel.getStyleClass().add("label");
        client2ReadyLabel.setStyle("-fx-text-fill: gray;");

        readyBox.getChildren()
                .addAll(readyTitleLabel, hostReadyLabel, client1ReadyLabel, client2ReadyLabel);

        // 준비 버튼
        if (isHost) {
            readyButton = new Button("Ready");
        } else {
            readyButton = new Button("Ready");
        }
        readyButton.getStyleClass().add("button");

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("button");

        backButton.setOnAction(
                e -> {
                    if (onCancelCallback != null) {
                        onCancelCallback.run();
                    }
                    manager.showMainMenu(settings);
                });

        HBox buttonBox = new HBox(20, readyButton, backButton);
        buttonBox.setAlignment(Pos.CENTER);

        root.getChildren()
                .addAll(
                        titleLabel,
                        statusLabel,
                        new Label(), // 간격
                        gameModeBox,
                        new Label(), // 간격
                        readyBox,
                        new Label(), // 간격
                        buttonBox);

        scene = new Scene(root);
    }

    public Scene getScene() {
        return scene;
    }

    public Button getReadyButton() {
        return readyButton;
    }

    public GameModeMessage.GameMode getSelectedGameMode() {
        return selectedGameMode;
    }

    public void setStatusText(String text) {
        Platform.runLater(() -> statusLabel.setText(text));
    }

    public void setGameMode(String mode) {
        Platform.runLater(
                () -> {
                    if (!isHost) {
                        gameModeLabel.setText("Game Mode: " + mode);
                    }
                });
    }

    // 플레이어별 Ready 상태 설정
    public void setHostReady(boolean ready) {
        this.hostReady = ready;
        Platform.runLater(
                () -> {
                    hostReadyLabel.setText("Host: " + (ready ? "Ready!" : "Not Ready"));
                    hostReadyLabel.setStyle("-fx-text-fill: " + (ready ? "green" : "red") + ";");
                    if (isHost) {
                        if (ready) {
                            readyButton.setText("Cancel Ready");
                            readyButton.getStyleClass().add("selected");
                        } else {
                            readyButton.setText("Start");
                            readyButton.getStyleClass().remove("selected");
                        }
                    }
                });
    }

    public void setClient1Ready(boolean ready) {
        this.client1Ready = ready;
        Platform.runLater(
                () -> {
                    if (isClient1Connected) {
                        client1ReadyLabel.setText("Client 1: " + (ready ? "Ready!" : "Not Ready"));
                        client1ReadyLabel.setStyle(
                                "-fx-text-fill: " + (ready ? "green" : "red") + ";");
                    }
                });
    }

    public void setClient2Ready(boolean ready) {
        this.client2Ready = ready;
        Platform.runLater(
                () -> {
                    if (isClient2Connected) {
                        client2ReadyLabel.setText("Client 2: " + (ready ? "Ready!" : "Not Ready"));
                        client2ReadyLabel.setStyle(
                                "-fx-text-fill: " + (ready ? "green" : "red") + ";");
                    }
                });
    }

    // 클라이언트 연결 상태 설정
    public void setClient1Connected(boolean connected) {
        this.isClient1Connected = connected;
        Platform.runLater(
                () -> {
                    if (connected) {
                        client1ReadyLabel.setText("Client 1: Not Ready");
                        client1ReadyLabel.setStyle("-fx-text-fill: red;");
                    } else {
                        client1ReadyLabel.setText("Client 1: Waiting...");
                        client1ReadyLabel.setStyle("-fx-text-fill: gray;");
                        client1Ready = false;
                    }
                });
    }

    public void setClient2Connected(boolean connected) {
        this.isClient2Connected = connected;
        Platform.runLater(
                () -> {
                    if (connected) {
                        client2ReadyLabel.setText("Client 2: Not Ready");
                        client2ReadyLabel.setStyle("-fx-text-fill: red;");
                    } else {
                        client2ReadyLabel.setText("Client 2: Waiting...");
                        client2ReadyLabel.setStyle("-fx-text-fill: gray;");
                        client2Ready = false;
                    }
                });
    }

    // 모든 플레이어가 준비되었는지 확인
    public boolean areAllReady() {
        return hostReady
                && client1Ready
                && isClient1Connected
                && client2Ready
                && isClient2Connected;
    }

    public void setOnCancelCallback(Runnable callback) {
        this.onCancelCallback = callback;
    }

    public void setControlsDisabled(boolean disabled) {
        readyButton.setDisable(disabled);
        normalModeButton.setDisable(disabled);
        itemModeButton.setDisable(disabled);
        timerModeButton.setDisable(disabled);
    }

    public void setModeSelectionDisabled(boolean disabled) {
        normalModeButton.setDisable(disabled);
        itemModeButton.setDisable(disabled);
        timerModeButton.setDisable(disabled);
    }

    public void setBackButtonDisabled(boolean disabled) {
        // Back button is not stored as a field, so this is a no-op for now
        // If needed, store backButton reference in the constructor
    }
}
