package team13.tetris.scenes;

import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;

/**
 * Squad PVP Game Over Scene
 *
 * <p>Displays final rankings for 3 players: 1st, 2nd, 3rd
 */
public class SquadGameOverScene {
    private final SceneManager manager;
    private final Settings settings;
    private final List<String> rankings; // 1st, 2nd, 3rd
    private final String myPlayerId;

    public SquadGameOverScene(
            SceneManager manager, Settings settings, List<String> rankings, String myPlayerId) {
        this.manager = manager;
        this.settings = settings;
        this.rankings = rankings;
        this.myPlayerId = myPlayerId;
    }

    public Scene getScene() {
        Label titleLabel = new Label("Squad PVP - Game Over");
        titleLabel.getStyleClass().add("label-title");

        // Determine my placement
        int myPlace = -1;
        for (int i = 0; i < rankings.size(); i++) {
            if (rankings.get(i).equals(myPlayerId)) {
                myPlace = i + 1;
                break;
            }
        }

        // Result label
        Label resultLabel;
        if (myPlace == 1) {
            resultLabel = new Label("🥇 You Won!");
            resultLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 36px;"); // Gold
        } else if (myPlace == 2) {
            resultLabel = new Label("🥈 2nd Place");
            resultLabel.setStyle("-fx-text-fill: #C0C0C0; -fx-font-size: 32px;"); // Silver
        } else if (myPlace == 3) {
            resultLabel = new Label("🥉 3rd Place");
            resultLabel.setStyle("-fx-text-fill: #CD7F32; -fx-font-size: 28px;"); // Bronze
        } else {
            resultLabel = new Label("Game Finished");
            resultLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px;");
        }

        // Rankings display
        VBox rankingsBox = new VBox(15);
        rankingsBox.setAlignment(Pos.CENTER);

        Label rankingsTitle = new Label("Final Rankings:");
        rankingsTitle.getStyleClass().add("label");
        rankingsTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        rankingsBox.getChildren().add(rankingsTitle);

        for (int i = 0; i < rankings.size() && i < 3; i++) {
            String player = rankings.get(i);
            String place = (i == 0) ? "🥇 1st" : (i == 1) ? "🥈 2nd" : "🥉 3rd";
            Label rankLabel = new Label(place + ": " + player);
            rankLabel.getStyleClass().add("label");
            rankLabel.setStyle("-fx-font-size: 18px;");

            if (player.equals(myPlayerId)) {
                rankLabel.setStyle(
                        rankLabel.getStyle() + " -fx-font-weight: bold; -fx-text-fill: yellow;");
            }

            rankingsBox.getChildren().add(rankLabel);
        }

        // Buttons
        Button mainMenuButton = new Button("Main Menu");
        mainMenuButton.getStyleClass().add("button");
        mainMenuButton.setOnAction(e -> manager.showMainMenu(settings));

        VBox root = new VBox(30);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("menu-root");

        root.getChildren()
                .addAll(
                        titleLabel,
                        resultLabel,
                        new Label(), // Spacer
                        rankingsBox,
                        new Label(), // Spacer
                        mainMenuButton);

        Scene scene = new Scene(root);
        return scene;
    }
}
