package team13.tetris.data;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ScoreBoard {
    public static class ScoreEntry {
        public enum Mode {
            EASY, NORMAL, HARD, ITEM
        }

        private String name;
        private int score;
        private Mode mode;

        public ScoreEntry(String name, int score, Mode mode) {
            this.name = name;
            this.score = score;
            this.mode = mode;
        }

        public String getName() {
            return name;
        }

        public int getScore() {
            return score;
        }

        public Mode getMode() {
            return mode;
        }

        @Override
        public String toString() {
            return String.format("%s: %d (%s)", name, score, mode.name());
        }
    }

    private List<ScoreEntry> scores;
    private static final String DEFAULT_SCORE_FILE = "scores.txt";
    private String scoreFile;
    private ScoreEntry lastAddedEntry;

    public ScoreBoard() {
        this(DEFAULT_SCORE_FILE);
    }

    public ScoreBoard(String scoreFilePath) {
        this.scores = new ArrayList<>();
        this.scoreFile = scoreFilePath;
        loadScores();
    }

    public void addScore(String name, int score, ScoreEntry.Mode mode) {
        ScoreEntry entry = new ScoreEntry(name, score, mode);
        scores.add(entry);
        lastAddedEntry = entry;
        scores.sort(Comparator.comparingInt(ScoreEntry::getScore).reversed());
        saveScores();
    }

    public void resetScores() {
        scores.clear();
        lastAddedEntry = null;
        saveScores();
    }

    public void saveScores() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(scoreFile))) {
            for (ScoreEntry entry : scores) writer.println(entry.getName() + "," + entry.getScore() + "," + entry.getMode().name());
        } catch (IOException e) {
            System.err.println("Error saving scores: " + e.getMessage());
        }
    }

    public void loadScores() {
        File file = new File(scoreFile);
        
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String name = parts[0];
                int score = Integer.parseInt(parts[1]);
                ScoreEntry.Mode mode = ScoreEntry.Mode.valueOf(parts[2]);

                scores.add(new ScoreEntry(name, score, mode));
            }

            scores.sort(Comparator.comparingInt(ScoreEntry::getScore).reversed());
            lastAddedEntry = null;
        } catch (IOException | RuntimeException e) {
            System.err.println("Error loading scores: " + e.getMessage());
        }
    }

    public List<ScoreEntry> getScores() {
        return new ArrayList<>(scores);
    }

    public List<ScoreEntry> getScoresByMode(ScoreEntry.Mode mode) {
        List<ScoreEntry> filteredScores = new ArrayList<>();

        for (ScoreEntry entry : scores) if (entry.getMode() == mode) filteredScores.add(entry);

        filteredScores.sort(Comparator.comparingInt(ScoreEntry::getScore).reversed());

        return filteredScores;
    }

    public List<ScoreEntry> getGameScores() {
        List<ScoreEntry> gameScores = new ArrayList<>();

        for (ScoreEntry entry : scores) gameScores.add(entry);

        gameScores.sort(Comparator.comparingInt(ScoreEntry::getScore).reversed());

        return gameScores;
    }

    public int getLastAddedIndex() {
        if (lastAddedEntry == null) return -1;
        
        return scores.indexOf(lastAddedEntry);
    }

    public ScoreEntry getLastAddedEntry() {
        return lastAddedEntry;
    }
}
