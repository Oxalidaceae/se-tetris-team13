package team13.tetris.scenes;

import java.util.List;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;

/**
 * Squad PVP result scene showing final rankings.
 *
 * <p>Winner at top (large font), other players below (smaller).
 */
public class SquadResultScene {
    private final SceneManager manager;
    private final Settings settings;
    private final Scene scene;
    private final Map<Integer, String> playerIds; // index -> playerId mapping
    private Runnable onReturnToLobbyCallback;

    public SquadResultScene(
            SceneManager manager,
            Settings settings,
            List<String> rankings,
            Map<Integer, String> playerIds) {
        this.manager = manager;
        this.settings = settings;
        this.playerIds = playerIds;

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #1a1a1a;");

        // Title
        Label title = new Label("Squad PVP - Final Rankings");
        title.setStyle("-fx-font-size: 36px; -fx-text-fill: #ffffff; -fx-font-weight: bold;");

        // Winner (last player in elimination order)
        String winner = getDisplayName(rankings.get(rankings.size() - 1));
        Label winnerLabel = new Label("🏆 Winner: " + winner);
        winnerLabel.setStyle(
                "-fx-font-size: 38px; -fx-text-fill: #FFD700; -fx-font-weight: bold; -fx-padding: 20;");

        // Other rankings
        VBox rankingsBox = new VBox(10);
        rankingsBox.setAlignment(Pos.CENTER);

        for (int i = rankings.size() - 2; i >= 0; i--) {
            int place = i + 1;
            String player = getDisplayName(rankings.get(i));

            Label rankLabel = new Label(place + ". " + player);
            rankLabel.setStyle("-fx-font-size: 28px; -fx-text-fill: #cccccc;");
            rankingsBox.getChildren().add(rankLabel);
        }

        // Return to lobby button
        Button returnButton = new Button("Return to Lobby");
        returnButton.setStyle(
                "-fx-font-size: 18px; -fx-padding: 10 20; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        returnButton.setOnAction(
                e -> {
                    if (onReturnToLobbyCallback != null) {
                        onReturnToLobbyCallback.run();
                    } else {
                        manager.showMainMenu(settings);
                    }
                });

        root.getChildren().addAll(title, winnerLabel, rankingsBox, returnButton);

        scene = new Scene(root, 800, 600);
    }

    public Scene getScene() {
        return scene;
    }

    public void setOnReturnToLobbyCallback(Runnable callback) {
        this.onReturnToLobbyCallback = callback;
    }

    /**
     * Convert player ID to display name using playerIds map. Host (index 0) -> Host Client at index
     * 1 -> Client1 Client at index 2 -> Client2
     */
    private String getDisplayName(String playerId) {
        // Find the index of this player in playerIds map
        for (Map.Entry<Integer, String> entry : playerIds.entrySet()) {
            if (entry.getValue().equals(playerId)) {
                int index = entry.getKey();
                if (index == 0) {
                    return "Host";
                } else if (index == 1) {
                    return "Client1";
                } else if (index == 2) {
                    return "Client2";
                }
            }
        }
        // Fallback if not found in map
        return playerId.startsWith("Client-") ? "Client" : playerId;
    }
}
