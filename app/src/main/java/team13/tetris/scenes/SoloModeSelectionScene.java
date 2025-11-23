package team13.tetris.scenes;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;

public class SoloModeSelectionScene {
    private final SceneManager manager;
    private final Settings settings;

    public SoloModeSelectionScene(SceneManager manager, Settings settings) {
        this.manager = manager;
        this.settings = settings;
    }

    public Scene getScene() {
        Label title = new Label("Solo Mode");
        title.getStyleClass().add("label-title");

        Button normalModeBtn = new Button("Normal");
        Button itemModeBtn = new Button("Item Mode");
        Button backBtn = new Button("Back");

        normalModeBtn.setOnAction(e -> manager.showDifficultySelection(settings));
        itemModeBtn.setOnAction(
                e ->
                        manager.showGame(
                                settings, team13.tetris.data.ScoreBoard.ScoreEntry.Mode.ITEM));
        backBtn.setOnAction(e -> manager.showGameModeSelection(settings));

        VBox layout = new VBox(15, title, normalModeBtn, itemModeBtn, backBtn);
        layout.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(layout, 600, 700);
        manager.enableArrowAsTab(scene);

        return scene;
    }
}
