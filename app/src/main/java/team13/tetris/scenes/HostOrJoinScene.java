package team13.tetris.scenes;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;
import team13.tetris.network.server.TetrisServer;

public class HostOrJoinScene {
    @SuppressWarnings("unused")
    private final SceneManager manager;

    @SuppressWarnings("unused")
    private final Settings settings;

    private Scene scene;
    private String selectedRole = "host"; // "host" or "client"

    public HostOrJoinScene(SceneManager manager, Settings settings) {
        this.manager = manager;
        this.settings = settings;

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("menu-root");
        root.setPadding(new Insets(40));

        Label titleLabel = new Label("P2P Multiplayer");
        titleLabel.getStyleClass().add("label-title");

        Label roleLabel = new Label("Select Role:");
        roleLabel.getStyleClass().add("label");

        Button hostButton = new Button("Host Server");
        hostButton.getStyleClass().addAll("button", "selected");

        Button joinButton = new Button("Join as Client");
        joinButton.getStyleClass().add("button");

        // Label to display host's IP
        Label ipDisplayLabel =
                new Label(
                        "Your IP address: "
                                + TetrisServer.getServerIP()
                                + "\n\nPlease let the client know this address.");
        ipDisplayLabel.getStyleClass().add("label");

        // Controls for joining a server
        Label ipInputLabel = new Label("Server IP Address:");
        ipInputLabel.getStyleClass().add("label");
        ipInputLabel.setVisible(false);

        Label recentIPLabel = new Label();
        recentIPLabel.getStyleClass().add("label");
        if (!settings.getRecentIP().isEmpty()) {
            recentIPLabel.setText("Recently connected IP Address: " + settings.getRecentIP());
            recentIPLabel.setVisible(false);
        }

        TextField ipTextField = new TextField();
        ipTextField.setPromptText("Enter IP address (e.g., 127.0.0.1)");
        ipTextField.getStyleClass().add("text-field");
        ipTextField.setMaxWidth(300);
        ipTextField.setVisible(false);

        // Event Handlers
        hostButton.setOnAction(
                e -> {
                    selectedRole = "host";
                    hostButton.getStyleClass().add("selected");
                    joinButton.getStyleClass().remove("selected");

                    recentIPLabel.setVisible(false);
                    ipInputLabel.setVisible(false);
                    ipTextField.setVisible(false);
                    ipDisplayLabel.setVisible(true);
                });

        joinButton.setOnAction(
                e -> {
                    selectedRole = "client";
                    joinButton.getStyleClass().add("selected");
                    hostButton.getStyleClass().remove("selected");

                    ipDisplayLabel.setVisible(false);
                    recentIPLabel.setVisible(true);
                    ipInputLabel.setVisible(true);
                    ipTextField.setVisible(true);
                    Platform.runLater(ipTextField::requestFocus);
                });

        Button continueButton = new Button("Continue");
        continueButton.getStyleClass().add("button");

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("button");

        ipTextField.setOnKeyPressed(
                event -> {
                    if (event.getCode() == KeyCode.UP) {
                        joinButton.requestFocus();
                        event.consume();
                    } else if (event.getCode() == KeyCode.DOWN) {
                        continueButton.requestFocus();
                        event.consume();
                    }
                });

        continueButton.setOnAction(
                e -> {
                    if ("host".equals(selectedRole)) {
                        manager.showNetworkLobby(settings, true, null);
                    } else {
                        String serverIP = ipTextField.getText().trim();

                        // IP 주소 유효성 검증
                        if (!isValidIPAddress(serverIP)) {
                            showErrorDialog(
                                    "Invalid IP Address",
                                    "Please enter a valid IP address (e.g., 192.168.1.1 or localhost)");
                            return;
                        }

                        manager.showNetworkLobby(settings, false, serverIP);
                    }
                });

        backButton.setOnAction(e -> manager.showMultiModeSelection(settings));

        HBox buttonBox = new HBox(20, continueButton, backButton);
        buttonBox.setAlignment(Pos.CENTER);

        root.getChildren()
                .addAll(
                        titleLabel,
                        new Label(),
                        roleLabel,
                        hostButton,
                        joinButton,
                        new Label(),
                        ipDisplayLabel, // IP for host
                        recentIPLabel, // Recent IP for client
                        ipInputLabel, // Label for client
                        ipTextField, // Input for client
                        buttonBox);

        scene = new Scene(root);
    }

    // IP 주소 유효성 검증
    public boolean isValidIPAddress(String ip) {
        // localhost 허용
        if (ip.equalsIgnoreCase("localhost")) {
            return true;
        }

        // IPv4 형식 검증: xxx.xxx.xxx.xxx (각 부분은 0-255)
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }

        try {
            for (String part : parts) {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // 에러 다이얼로그 표시
    private void showErrorDialog(String title, String message) {
        javafx.scene.control.Alert alert =
                new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Scene getScene() {
        return scene;
    }
}
