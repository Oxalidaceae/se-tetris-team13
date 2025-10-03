package org.example;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {
    private final Stage stage;  
    private boolean colorBlindMode = false;    // 색맹 모드 상태 변수 

    public SceneManager(Stage stage) {
        this.stage = stage;
    }

    public void setColorBlindMode(boolean enabled) {
        this.colorBlindMode = enabled;
        applyStylesheet(stage.getScene());
    }

    public void changeScene(Scene scene) {
        applyStylesheet(scene);
        stage.setScene(scene);
    }

    private void applyStylesheet(Scene scene) {
        scene.getStylesheets().clear();
        if (colorBlindMode) {
            scene.getStylesheets().add(
                getClass().getResource("/colorblind.css").toExternalForm()
            );
        } 
        else {
            scene.getStylesheets().add(
                getClass().getResource("/application.css").toExternalForm()
            );
        }
    }

}
