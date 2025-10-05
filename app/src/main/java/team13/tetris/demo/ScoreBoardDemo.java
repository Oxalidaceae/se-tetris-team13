package team13.tetris.demo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import team13.tetris.ui.ScoreBoard;
import team13.tetris.ui.ScoreBoard.ScoreEntry;

import java.util.Random;

/**
 * Demo application to test ScoreBoard functionality
 * This is a simple JavaFX application that allows testing ScoreBoard features
 */
public class ScoreBoardDemo extends Application {
    
    private ScoreBoard scoreBoard;
    private Stage primaryStage;
    private TextField nameField;
    private TextField scoreField;
    private Label statusLabel;
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.scoreBoard = new ScoreBoard();
        
        primaryStage.setTitle("ScoreBoard Demo");
        
        Scene mainScene = createMainScene();
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }
    
    private Scene createMainScene() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f0f0f0;");
        
        // Title
        Label titleLabel = new Label("ScoreBoard Test Application");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Input section
        Label inputLabel = new Label("Add New Score:");
        inputLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        HBox inputBox = new HBox(10);
        nameField = new TextField();
        nameField.setPromptText("Player Name");
        nameField.setPrefWidth(150);
        
        scoreField = new TextField();
        scoreField.setPromptText("Score");
        scoreField.setPrefWidth(100);
        
        Button addButton = new Button("Add Score");
        addButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        addButton.setOnAction(e -> addScore());
        
        inputBox.getChildren().addAll(
            new Label("Name:"), nameField,
            new Label("Score:"), scoreField,
            addButton
        );
        
        // Action buttons
        HBox actionBox = new HBox(10);
        
        Button showScoresButton = new Button("Show High Scores");
        showScoresButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        showScoresButton.setOnAction(e -> showHighScores());
        
        Button resetButton = new Button("Reset All Scores");
        resetButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        resetButton.setOnAction(e -> resetScores());
        
        Button addRandomButton = new Button("Add Random Scores");
        addRandomButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold;");
        addRandomButton.setOnAction(e -> addRandomScores());
        
        Button testSaveLoadButton = new Button("Test Save/Load");
        testSaveLoadButton.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold;");
        testSaveLoadButton.setOnAction(e -> testSaveLoad());
        
        actionBox.getChildren().addAll(showScoresButton, resetButton, addRandomButton, testSaveLoadButton);
        
        // Status label
        statusLabel = new Label("Ready to test ScoreBoard functionality");
        statusLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #7f8c8d;");
        
        // Current scores display
        Label scoresLabel = new Label("Current Scores Count: 0");
        scoresLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #34495e;");
        
        Button refreshCountButton = new Button("Refresh Count");
        refreshCountButton.setOnAction(e -> {
            int count = scoreBoard.getScores().size();
            scoresLabel.setText("Current Scores Count: " + count);
            if (count > 0) {
                ScoreBoard.ScoreEntry topScore = scoreBoard.getScores().get(0);
                statusLabel.setText("Top Score: " + topScore.getName() + " - " + topScore.getScore());
            }
        });
        
        HBox countBox = new HBox(10);
        countBox.getChildren().addAll(scoresLabel, refreshCountButton);
        
        root.getChildren().addAll(
            titleLabel,
            inputLabel,
            inputBox,
            actionBox,
            countBox,
            statusLabel
        );
        
        return new Scene(root, 600, 300);
    }
    
    private void addScore() {
        try {
            String name = nameField.getText().trim();
            String scoreText = scoreField.getText().trim();
            
            if (name.isEmpty()) {
                statusLabel.setText("Please enter a player name");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            
            if (scoreText.isEmpty()) {
                statusLabel.setText("Please enter a score");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            
            int score = Integer.parseInt(scoreText);
            scoreBoard.addScore(name, score);
            
            nameField.clear();
            scoreField.clear();
            
            statusLabel.setText("Added score: " + name + " - " + score);
            statusLabel.setStyle("-fx-text-fill: green;");
            
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid score format. Please enter a number.");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    private void showHighScores() {
        Scene scoreScene = scoreBoard.createScoreScene(primaryStage, this::returnToMain);
        primaryStage.setScene(scoreScene);
        statusLabel.setText("Showing high scores scene");
        statusLabel.setStyle("-fx-text-fill: blue;");
    }
    
    private void returnToMain() {
        Scene mainScene = createMainScene();
        primaryStage.setScene(mainScene);
        statusLabel.setText("Returned from high scores");
        statusLabel.setStyle("-fx-text-fill: blue;");
    }
    
    private void resetScores() {
        scoreBoard.resetScores();
        statusLabel.setText("All scores have been reset");
        statusLabel.setStyle("-fx-text-fill: orange;");
    }
    
    private void addRandomScores() {
        String[] names = {"Alice", "Bob", "Charlie", "Diana", "Eve", "Frank", "Grace", "Henry"};
        Random random = new Random();
        
        for (int i = 0; i < 5; i++) {
            String name = names[random.nextInt(names.length)];
            int score = random.nextInt(10000) + 100; // Score between 100-10099
            scoreBoard.addScore(name, score);
        }
        
        statusLabel.setText("Added 5 random scores");
        statusLabel.setStyle("-fx-text-fill: green;");
    }
    
    private void testSaveLoad() {
        // Save current scores
        scoreBoard.saveScores();
        
        // Create a new ScoreBoard to test loading
        ScoreBoard testBoard = new ScoreBoard();
        int loadedCount = testBoard.getScores().size();
        
        statusLabel.setText("Save/Load test completed. Loaded " + loadedCount + " scores from file.");
        statusLabel.setStyle("-fx-text-fill: purple;");
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}