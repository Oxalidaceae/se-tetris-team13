package team13.tetris.data;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * ScoreBoard class for managing Tetris high scores.
 * Provides functionality for score storage, retrieval, and file persistence.
 * Pure data management class without UI dependencies.
 */
public class ScoreBoard {

    /**
     * Represents a single score entry with player name and score.
     */
    public static class ScoreEntry {
        private String name;
        private int score;
        
        public ScoreEntry(String name, int score) {
            this.name = name;
            this.score = score;
        }
        
        public String getName() {
            return name;
        }

        public int getScore() {
            return score;
        }

        @Override
        public String toString() {
            return String.format("%s: %d", name, score);
        }
    }

    // Fields
    private List<ScoreEntry> scores;
    private static final String SCORE_FILE = "scores.txt";
    private ScoreEntry lastAddedEntry; // Tracks most recently added entry

    // Constructor
    public ScoreBoard() {
        this.scores = new ArrayList<>();
        loadScores();
    }
    
    /**
     * Adds a new score entry to the scoreboard.
     * Automatically sorts scores in descending order and saves to file.
     *
     * @param name  Player name
     * @param score Player score
     */
    public void addScore(String name, int score) {
        ScoreEntry entry = new ScoreEntry(name, score);
        scores.add(entry);
        lastAddedEntry = entry;
        scores.sort(Comparator.comparingInt(ScoreEntry::getScore).reversed());
        saveScores();
    }

    /**
     * Clears all scores from the scoreboard and saves the empty state to file.
     */
    public void resetScores() {
        scores.clear();
        lastAddedEntry = null;
        saveScores();
    }

    /**
     * Saves current scores to CSV file.
     * Format: "playerName,score" per line.
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
     * Loads scores from CSV file during initialization.
     * Creates empty scoreboard if file doesn't exist or has format errors.
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
     * Returns a defensive copy of the current scores list.
     *
     * @return List of score entries sorted in descending order
     */
    public List<ScoreEntry> getScores() {
        return new ArrayList<>(scores);
    }

    /**
     * Returns the index of the last added entry in the sorted list.
     *
     * @return Index of last added entry, or -1 if none tracked
     */
    public int getLastAddedIndex() {
        if (lastAddedEntry == null) {
            return -1;
        }
        return scores.indexOf(lastAddedEntry);
    }

    /**
     * Returns the most recently added score entry.
     *
     * @return Last added entry, or null if none tracked
     */
    public ScoreEntry getLastAddedEntry() {
        return lastAddedEntry;
    }
}
