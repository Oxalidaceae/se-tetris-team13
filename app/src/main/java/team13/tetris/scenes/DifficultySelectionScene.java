package team13.tetris.scenes;

import team13.tetris.SceneManager;
import team13.tetris.config.Settings;
import team13.tetris.data.ScoreBoard;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class DifficultySelectionScene {
    private final SceneManager manager;
    private final Settings settings;

    public DifficultySelectionScene(SceneManager manager, Settings settings) {
        this.manager = manager;
        this.settings = settings;
    }

    public Scene getScene() {
        Label title = new Label("Select Difficulty");
        title.getStyleClass().add("label-title");

        Button easyBtn = new Button("Easy");
        Button normalBtn = new Button("Normal");
        Button hardBtn = new Button("Hard");
        Button backBtn = new Button("Back");

        easyBtn.setOnAction(e -> manager.showGame(settings, ScoreBoard.ScoreEntry.Mode.EASY));
        normalBtn.setOnAction(e -> manager.showGame(settings, ScoreBoard.ScoreEntry.Mode.NORMAL));
        hardBtn.setOnAction(e -> manager.showGame(settings, ScoreBoard.ScoreEntry.Mode.HARD));
        backBtn.setOnAction(e -> manager.showSoloModeSelection(settings));

        VBox layout = new VBox(15, title, easyBtn, normalBtn, hardBtn, backBtn);
        layout.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(layout, 600, 700);
        manager.enableArrowAsTab(scene);

        return scene;
    }
}
