package team13.tetris.scenes;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;

public class MultiModeSelectionScene {
    private final SceneManager manager;
    private final Settings settings;

    public MultiModeSelectionScene(SceneManager manager, Settings settings) {
        this.manager = manager;
        this.settings = settings;
    }

    public Scene getScene() {
        Label title = new Label("Multi Mode");
        title.getStyleClass().add("label-title");

        Button localMultiBtn = new Button("Local (1P/2P)");
        Button p2pBtn = new Button("Online (P2P)");
        Button backBtn = new Button("Back");

        localMultiBtn.setOnAction(e -> manager.showLocalMultiModeSelection(settings));
        p2pBtn.setOnAction(e -> manager.showHostOrJoin(settings));
        backBtn.setOnAction(e -> manager.showGameModeSelection(settings));

        VBox layout = new VBox(15, title, localMultiBtn, p2pBtn, backBtn);
        layout.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(layout, 600, 700);
        manager.enableArrowAsTab(scene);

        return scene;
    }
}
