package team13.tetris.scenes;

import team13.tetris.SceneManager;
import team13.tetris.config.Settings;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class GameModeSelectionScene {
    private final SceneManager manager;
    private final Settings settings;

    public GameModeSelectionScene(SceneManager manager, Settings settings) {
        this.manager = manager;
        this.settings = settings;
    }

    public Scene getScene() {
        Label title = new Label("Game Mode");
        title.getStyleClass().add("label-title");

        Button soloBtn = new Button("Solo");
        Button multiBtn = new Button("Multi");
        Button backBtn = new Button("Back");

        soloBtn.setOnAction(e -> manager.showSoloModeSelection(settings));
        multiBtn.setOnAction(e -> manager.showMultiModeSelection(settings));
        backBtn.setOnAction(e -> manager.showMainMenu(settings));

        VBox layout = new VBox(15, title, soloBtn, multiBtn, backBtn);
        layout.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(layout, 600, 700);
        manager.enableArrowAsTab(scene);

        return scene;
    }
}
