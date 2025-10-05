package team13.tetris.ui;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * ScoreBoard class for managing Tetris high scores
 * Provides functionality for score storage, retrieval, and UI display
 * Supports file persistence and JavaFX integration
 */
public class ScoreBoard {
    
    /**
     * Represents a single score entry with player name and score
     */
    public static class ScoreEntry {
        private String name;
        private int score;
        
        public ScoreEntry(String name, int score) {
            this.name = name;
            this.score = score;
        }
        
        public String getName() { return name; }
        public int getScore() { return score; }
        
        @Override
        public String toString() {
            return String.format("%s: %d", name, score);
        }
    }
    
    private List<ScoreEntry> scores;
    private static final String SCORE_FILE = "scores.txt";
    private ScoreEntry lastAddedEntry; // Tracks most recently added entry for UI highlighting
    
    public ScoreBoard() {
        this.scores = new ArrayList<>();
        loadScores();
    }
    
    /**
     * Add a new score
     * @param name Player name
     * @param score Score
     */
    public void addScore(String name, int score) {
        ScoreEntry entry = new ScoreEntry(name, score);
        scores.add(entry);
        lastAddedEntry = entry;
        scores.sort(Comparator.comparingInt(ScoreEntry::getScore).reversed());
        saveScores();
    }
    
    /**
     * Reset all scores
     */
    public void resetScores() {
        scores.clear();
        lastAddedEntry = null;
        saveScores();
    }
    
    /**
     * Creates a JavaFX Scene displaying the scoreboard with Play Again and Exit buttons
     * @param stage Main stage for scene display
     * @param onRetry Callback executed when Play Again button is clicked
     * @return Complete scoreboard Scene ready for display
     */
    public Scene createScoreScene(Stage stage, Runnable onRetry) {
        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 20; -fx-alignment: center;");
        
        Label titleLabel = new Label("High Scores");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        ListView<ScoreEntry> scoreListView = new ListView<>();
        scoreListView.getItems().addAll(scores);
        scoreListView.setPrefHeight(300);
        
        if (lastAddedEntry != null) {
            int idx = scores.indexOf(lastAddedEntry);
            if (idx >= 0) {
                scoreListView.getSelectionModel().select(idx);
                scoreListView.scrollTo(idx);
            }
        }
        
        Button retryButton = new Button("Play Again");
        retryButton.setOnAction(e -> onRetry.run());
        
        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> stage.close());
        
        root.getChildren().addAll(titleLabel, scoreListView, retryButton, exitButton);
        
        return new Scene(root, 400, 500);
    }
    
    /**
     * Persists current scores to CSV file
     */
    public void saveScores() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SCORE_FILE))) {
            for (ScoreEntry entry : scores) {
                writer.println(entry.getName() + "," + entry.getScore());
            }
        } catch (IOException e) {
            System.err.println("Error saving scores: " + e.getMessage());
        }
    }
    
    /**
     * Loads scores from CSV file on initialization
     */
    public void loadScores() {
        File file = new File(SCORE_FILE);
        if (!file.exists()) {
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String name = parts[0];
                    int score = Integer.parseInt(parts[1]);
                    scores.add(new ScoreEntry(name, score));
                }
            }
            scores.sort(Comparator.comparingInt(ScoreEntry::getScore).reversed());
            lastAddedEntry = null; // Reset since loaded entries aren't "newly added"
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading scores: " + e.getMessage());
        }
    }
    
    /**
     * @return Defensive copy of current scores list
     */
    public List<ScoreEntry> getScores() {
        return new ArrayList<>(scores);
    }

    /**
     * @return Index of last added entry in sorted list, or -1 if none tracked
     */
    public int getLastAddedIndex() {
        if (lastAddedEntry == null) return -1;
        return scores.indexOf(lastAddedEntry);
    }

    /**
     * @return Most recently added entry instance, or null if none tracked
     */
    public ScoreEntry getLastAddedEntry() {
        return lastAddedEntry;
    }
    
    /**
     * Add score with player name input dialog - Required by FR6
     * Shows dialog to input player name, then adds score and shows scoreboard
     */
    public void addScoreWithDialog(Stage parentStage, int score, Runnable onComplete) {
        TextInputDialog dialog = new TextInputDialog("Player");
        dialog.setTitle("High Score!");
        dialog.setHeaderText("Congratulations! You achieved a high score!");
        dialog.setContentText("Enter your name:");
        
        // Only set owner if parentStage is valid
        if (parentStage != null && parentStage.getScene() != null) {
            dialog.initOwner(parentStage);
        }
        
        Optional<String> result = dialog.showAndWait();
        String playerName = result.orElse("Anonymous");
        
        // Ensure name is not empty
        if (playerName.trim().isEmpty()) {
            playerName = "Anonymous";
        }
        
        addScore(playerName, score);
        onComplete.run();
    }
}
