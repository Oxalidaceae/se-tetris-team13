package team13.tetris.scenes;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;

// 서버 또는 클라이언트 접속 선택
public class HostOrJoinScene {
    private final SceneManager manager;
    private final Settings settings;
    private Scene scene;
    
    public HostOrJoinScene(SceneManager manager, Settings settings) {
        this.manager = manager;
        this.settings = settings;
        
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("menu-root");
        root.setPadding(new Insets(40));
        
        Label titleLabel = new Label("P2P Multiplayer");
        titleLabel.getStyleClass().add("label-title");
        titleLabel.setStyle("-fx-font-size: 36px;");
        
        // 역할 선택
        Label roleLabel = new Label("Select Role:");
        roleLabel.getStyleClass().add("label");
        roleLabel.setStyle("-fx-font-size: 20px;");
        
        ToggleGroup roleGroup = new ToggleGroup();
        
        RadioButton serverButton = new RadioButton("Host Server");
        serverButton.setToggleGroup(roleGroup);
        serverButton.setSelected(true);
        serverButton.getStyleClass().add("radio-button");
        serverButton.setStyle("-fx-font-size: 18px;");
        
        RadioButton clientButton = new RadioButton("Join as Client");
        clientButton.setToggleGroup(roleGroup);
        clientButton.getStyleClass().add("radio-button");
        clientButton.setStyle("-fx-font-size: 18px;");
        
        // IP 주소 입력 (클라이언트용)
        Label ipLabel = new Label("Server IP Address:");
        ipLabel.getStyleClass().add("label");
        ipLabel.setVisible(false);
        
        TextField ipTextField = new TextField();
        ipTextField.setPromptText("Enter IP address (e.g., 127.0.0.1)"); 
        ipTextField.setMaxWidth(300);
        ipTextField.setVisible(false);
        
        // 클라이언트 선택 시 IP 입력 표시
        clientButton.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            ipLabel.setVisible(isNowSelected);
            ipTextField.setVisible(isNowSelected);
        });
        
        // 버튼들
        Button continueButton = new Button("Continue");
        continueButton.getStyleClass().add("menu-button");
        continueButton.setStyle("-fx-font-size: 20px; -fx-min-width: 200px;");
        
        Button backButton = new Button("Back");
        backButton.getStyleClass().add("menu-button");
        backButton.setStyle("-fx-font-size: 20px; -fx-min-width: 200px;");
        
        continueButton.setOnAction(e -> {
            if (serverButton.isSelected()) {
                // 서버 호스트로 시작
                manager.showNetworkLobby(settings, true, null);
            } else {
                // 클라이언트로 접속
                String serverIP = ipTextField.getText().trim();

                // IP 주소 유효성 검증
                if (!isValidIPAddress(serverIP)) {
                    showErrorDialog("Invalid IP Address", 
                        "Please enter a valid IP address (e.g., 192.168.1.1 or localhost)");
                    return;
                }
                
                manager.showNetworkLobby(settings, false, serverIP);
            }
        });
        
        backButton.setOnAction(e -> manager.showMultiModeSelection(settings));
        
        HBox buttonBox = new HBox(20, continueButton, backButton);
        buttonBox.setAlignment(Pos.CENTER);
        
        root.getChildren().addAll(
            titleLabel,
            new Label(), // 간격
            roleLabel,
            serverButton,
            clientButton,
            new Label(), // 간격
            ipLabel,
            ipTextField,
            new Label(), // 간격
            buttonBox
        );
        
        scene = new Scene(root);
    }
    
    public Scene getScene() {
        return scene;
    }
    
    // IP 주소 유효성 검증
    private boolean isValidIPAddress(String ip) {
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
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
