package team13.tetris.view.scene;

import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import team13.tetris.model.data.ScoreBoard;
import team13.tetris.view.SceneManager;

public class ScoreboardScene {
    private final SceneManager manager;

    public ScoreboardScene(SceneManager manager) {
        this.manager = manager;
    }

    public Scene getScene() {
        Label title = new Label("Scoreboard");
        title.getStyleClass().add("label-title");

        ScoreBoard scoreBoard = manager.getScoreBoard();
        ListView<String> scoreList = new ListView<>();
        if (scoreBoard.getScores().isEmpty()) {
            scoreList.setItems(FXCollections.observableArrayList("No scores yet"));
        } else {
            scoreList.setItems(FXCollections.observableArrayList(
                scoreBoard.getScores().stream()
                    .map(entry -> String.format("%s - %d", entry.getName(), entry.getScore()))
                    .toList()
            ));
        }
        scoreList.setMaxHeight(300);

        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> manager.showMainMenu());

        VBox layout = new VBox(15, title, scoreList, backBtn);
        layout.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(layout, 600, 700);
        manager.enableArrowAsTab(scene);
        return scene;
    }
}
