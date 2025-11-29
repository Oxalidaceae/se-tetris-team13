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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;
import team13.tetris.network.protocol.GameModeMessage;

// 대기 화면
public class NetworkLobbyScene {
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

    private Label myReadyLabel;
    private Label opponentReadyLabel;

    private Button readyButton;
    private Button backButton;

    private Label gameModeLabel;
    private Button normalModeButton;
    private Button itemModeButton;
    private Button timerModeButton;
    private GameModeMessage.GameMode selectedGameMode = GameModeMessage.GameMode.NORMAL;

    private boolean myReady = false;
    private boolean opponentReady = false;

    // 채팅 UI 컴포넌트
    private TextArea chatArea;
    private TextField chatInput;
    private Button chatSendButton;
    private Runnable onSendChatCallback;

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

        statusLabel = new Label(/* 컨트롤러에서 글씨 설정*/ );
        statusLabel.getStyleClass().add("label");
        statusLabel.setStyle("-fx-text-fill: yellow;");
        statusLabel.setTextAlignment(TextAlignment.CENTER);
        statusLabel.setWrapText(true);
        statusLabel.setMinHeight(Region.USE_PREF_SIZE);

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

        // 준비 상태
        VBox readyBox = new VBox(15);
        readyBox.setAlignment(Pos.CENTER);

        Label readyTitleLabel = new Label("Player Status:");
        readyTitleLabel.getStyleClass().add("label");

        myReadyLabel = new Label((isHost ? "Host" : "Client") + ": Not Ready");
        myReadyLabel.getStyleClass().add("label");
        myReadyLabel.setStyle("-fx-text-fill: red;");

        opponentReadyLabel = new Label((isHost ? "Client" : "Host") + ": Not Ready");
        opponentReadyLabel.getStyleClass().add("label");
        opponentReadyLabel.setStyle("-fx-text-fill: red;");

        readyBox.getChildren().addAll(readyTitleLabel, myReadyLabel, opponentReadyLabel);

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
        chatSendButton.prefWidthProperty().bind(chatInputBox.widthProperty().subtract(10).multiply(0.3));
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
        readyAndChatbox.getChildren().addAll(readyBox,chatBox);

        VBox.setVgrow(readyAndChatbox, Priority.ALWAYS);

        // 준비 버튼
        readyButton = new Button("Start");
        readyButton.getStyleClass().add("button");

        backButton = new Button("Back");
        backButton.getStyleClass().add("button");

        backButton.setOnAction(
                e -> {
                    if (onCancelCallback != null) {
                        onCancelCallback.run();
                    }
                    manager.showHostOrJoin(settings);
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
                    if (!isHost) {
                        gameModeLabel.setText("Game Mode: " + mode);
                    }
                });
    }

    public void setMyReady(boolean ready) {
        this.myReady = ready;
        Platform.runLater(
                () -> {
                    myReadyLabel.setText(
                            (isHost ? "Host" : "Client") + ": " + (ready ? "Ready!" : "Not Ready"));
                    myReadyLabel.setStyle("-fx-text-fill: " + (ready ? "green" : "red") + ";");
                    if (ready) {
                        readyButton.setText("Cancel Ready");
                        readyButton.getStyleClass().add("selected");
                    } else {
                        readyButton.setText("Start");
                        readyButton.getStyleClass().remove("selected");
                    }
                });
    }

    public void setOpponentReady(boolean ready) {
        this.opponentReady = ready;
        Platform.runLater(
                () -> {
                    opponentReadyLabel.setText(
                            (isHost ? "Client" : "Host") + ": " + (ready ? "Ready!" : "Not Ready"));
                    opponentReadyLabel.setStyle(
                            "-fx-text-fill: " + (ready ? "green" : "red") + ";");
                });
    }

    public boolean areBothReady() {
        return myReady && opponentReady;
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

    public void setBackButtonDisabled(boolean disabled) {
        Platform.runLater(() -> backButton.setDisable(disabled));
    }

}
