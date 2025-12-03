package team13.tetris.scenes;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
    private Button backButton;

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

    // 채팅 UI 컴포넌트
    private TextArea chatArea;
    private TextField chatInput;
    private Button chatSendButton;
    private Runnable onSendChatCallback;

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

        // 채팅 UI
        VBox chatBox = new VBox(10);
        chatBox.setAlignment(Pos.CENTER);

        Label chatTitleLabel = new Label("Chat");
        chatTitleLabel.getStyleClass().add("label");

        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.getStyleClass().add("text-area");
        chatArea.setFocusTraversable(false);
        chatArea.setFocusTraversable(false);

        VBox.setVgrow(chatArea, Priority.ALWAYS);

        chatInput = new TextField();
        chatInput.setPromptText("Please enter a message");
        chatInput.getStyleClass().add("text-field");

        chatSendButton = new Button("Send");
        chatSendButton.getStyleClass().add("button");

        // Enter 키로 전송
        chatInput.setOnAction(e -> chatSendButton.fire());

        HBox chatInputBox = new HBox(10);
        chatInputBox.setAlignment(Pos.CENTER);

        // 채팅 입력창과 전송 버튼 크기 비율 조정
        chatInput.prefWidthProperty().bind(chatInputBox.widthProperty().subtract(10).multiply(0.7));
        chatSendButton
                .prefWidthProperty()
                .bind(chatInputBox.widthProperty().subtract(10).multiply(0.3));
        chatInputBox.getChildren().addAll(chatInput, chatSendButton);

        chatBox.getChildren().addAll(chatTitleLabel, chatArea, chatInputBox);

        // 준비 상태와 채팅 박스 가로 배치
        HBox readyAndChatbox = new HBox(20);
        readyAndChatbox.setAlignment(Pos.CENTER);

        readyBox.setMaxWidth(Double.MAX_VALUE);
        chatBox.setMaxWidth(Double.MAX_VALUE);

        readyBox.setPrefWidth(0);
        chatBox.setPrefWidth(0);

        HBox.setHgrow(readyBox, Priority.ALWAYS);
        HBox.setHgrow(chatBox, Priority.ALWAYS);
        readyAndChatbox.getChildren().addAll(readyBox, chatBox);

        VBox.setVgrow(readyAndChatbox, Priority.ALWAYS);

        // 준비 버튼
        if (isHost) {
            readyButton = new Button("Ready");
        } else {
            readyButton = new Button("Ready");
        }
        readyButton.getStyleClass().add("button");
        readyButton.setMinWidth(150);
        readyButton.setPrefWidth(150);
        readyButton.setMaxWidth(150);

        backButton = new Button("Back");
        backButton.getStyleClass().add("button");
        backButton.setMinWidth(150);
        backButton.setPrefWidth(150);
        backButton.setMaxWidth(150);

        backButton.setOnAction(
                e -> {
                    if (onCancelCallback != null) {
                        onCancelCallback.run();
                    }
                    manager.showHostOrJoin(settings, "squad");
                });

        HBox buttonBox = new HBox(20, readyButton, backButton);
        buttonBox.setAlignment(Pos.CENTER);

        root.getChildren()
                .addAll(
                        titleLabel,
                        statusLabel,
                        new Label(), // 간격
                        readyAndChatbox,
                        new Label(), // 간격
                        buttonBox);

        scene = new Scene(root);
        manager.enableArrowAsTab(scene);
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
                    if (!isHost && gameModeLabel != null) {
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
        if (normalModeButton != null) {
            normalModeButton.setDisable(disabled);
        }
        if (itemModeButton != null) {
            itemModeButton.setDisable(disabled);
        }
        if (timerModeButton != null) {
            timerModeButton.setDisable(disabled);
        }
    }

    public void setModeSelectionDisabled(boolean disabled) {
        if (normalModeButton != null) {
            normalModeButton.setDisable(disabled);
        }
        if (itemModeButton != null) {
            itemModeButton.setDisable(disabled);
        }
        if (timerModeButton != null) {
            timerModeButton.setDisable(disabled);
        }
    }

    // 채팅 메시지 추가
    public void appendChatMessage(String senderId, String message) {
        Platform.runLater(
                () -> {
                    chatArea.appendText(senderId + ": " + message + "\n");
                    // 자동 스크롤 (가장 최근 메시지로)
                    chatArea.positionCaret(chatArea.getLength() - 1);
                });
    }

    // 채팅 입력 필드 가져오기
    public String getChatInput() {
        return chatInput.getText();
    }

    // 채팅 입력 필드 초기화
    public void clearChatInput() {
        Platform.runLater(() -> chatInput.clear());
    }

    // 채팅 전송 버튼에 콜백 설정
    public void setOnSendChatCallback(Runnable callback) {
        this.onSendChatCallback = callback;
        chatSendButton.setOnAction(
                e -> {
                    if (onSendChatCallback != null) {
                        onSendChatCallback.run();
                    }
                });
    }

    // Back 버튼 비활성화
    public void setBackButtonDisabled(boolean disabled) {
        Platform.runLater(() -> backButton.setDisable(disabled));
    }
}
