package team13.tetris;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application{

    @Override
    public void start(Stage primaryStage) {
        AppConfig.initialize(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}