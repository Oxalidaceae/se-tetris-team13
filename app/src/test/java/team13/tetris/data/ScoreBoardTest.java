package team13.tetris.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;

// Test class for ScoreBoard functionality
public class ScoreBoardTest {

    private ScoreBoard scoreBoard;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // Change working directory to temp directory for isolated testing
        System.setProperty("user.dir", tempDir.toString());
        scoreBoard = new ScoreBoard();
    }

    @AfterEach
    void tearDown() {
        // Clean up any created files
        File scoreFile = new File("scores.txt");
        if (scoreFile.exists()) {
            scoreFile.delete();
        }
    }

    @Test
    void testAddScore() {
        // Test adding a single score
        scoreBoard.addScore("Alice", 1000, ScoreBoard.ScoreEntry.Mode.NORMAL);

        List<ScoreBoard.ScoreEntry> scores = scoreBoard.getScores();
        assertEquals(1, scores.size());
        assertEquals("Alice", scores.get(0).getName());
        assertEquals(1000, scores.get(0).getScore());
        assertEquals(ScoreBoard.ScoreEntry.Mode.NORMAL, scores.get(0).getMode());
    }

    @Test
    void testAddMultipleScores() {
        // Test adding multiple scores
        scoreBoard.addScore("Alice", 1000, ScoreBoard.ScoreEntry.Mode.NORMAL);
        scoreBoard.addScore("Bob", 1500, ScoreBoard.ScoreEntry.Mode.NORMAL);
        scoreBoard.addScore("Charlie", 800, ScoreBoard.ScoreEntry.Mode.NORMAL);

        List<ScoreBoard.ScoreEntry> scores = scoreBoard.getScores();
        assertEquals(3, scores.size());

        // Should be sorted by score in descending order
        assertEquals("Bob", scores.get(0).getName());
        assertEquals(1500, scores.get(0).getScore());
        assertEquals("Alice", scores.get(1).getName());
        assertEquals(1000, scores.get(1).getScore());
        assertEquals("Charlie", scores.get(2).getName());
        assertEquals(800, scores.get(2).getScore());
    }

    @Test
    void testScoreSorting() {
        // Test that scores are automatically sorted
        scoreBoard.addScore("Low", 100, ScoreBoard.ScoreEntry.Mode.NORMAL);
        scoreBoard.addScore("High", 2000, ScoreBoard.ScoreEntry.Mode.NORMAL);
        scoreBoard.addScore("Medium", 1000, ScoreBoard.ScoreEntry.Mode.NORMAL);

        List<ScoreBoard.ScoreEntry> scores = scoreBoard.getScores();

        // Verify descending order
        assertTrue(scores.get(0).getScore() >= scores.get(1).getScore());
        assertTrue(scores.get(1).getScore() >= scores.get(2).getScore());

        assertEquals(2000, scores.get(0).getScore());
        assertEquals(1000, scores.get(1).getScore());
        assertEquals(100, scores.get(2).getScore());
    }

    @Test
    void testResetScores() {
        // Add some scores
        scoreBoard.addScore("Alice", 1000, ScoreBoard.ScoreEntry.Mode.NORMAL);
        scoreBoard.addScore("Bob", 1500, ScoreBoard.ScoreEntry.Mode.NORMAL);

        assertEquals(2, scoreBoard.getScores().size());

        // Reset scores
        scoreBoard.resetScores();

        assertEquals(0, scoreBoard.getScores().size());
    }

    @Test
    void testScoreEntryToString() {
        ScoreBoard.ScoreEntry entry = new ScoreBoard.ScoreEntry("TestPlayer", 12345, ScoreBoard.ScoreEntry.Mode.NORMAL);
        assertEquals("TestPlayer: 12345 (NORMAL)", entry.toString());
    }

    @Test
    void testSaveAndLoadScores() {
        // Add some scores
        scoreBoard.addScore("Alice", 1000, ScoreBoard.ScoreEntry.Mode.NORMAL);
        scoreBoard.addScore("Bob", 1500, ScoreBoard.ScoreEntry.Mode.NORMAL);
        scoreBoard.addScore("Charlie", 800, ScoreBoard.ScoreEntry.Mode.NORMAL);

        // Save scores
        scoreBoard.saveScores();

        // Create new ScoreBoard instance to test loading
        ScoreBoard newScoreBoard = new ScoreBoard();
        List<ScoreBoard.ScoreEntry> loadedScores = newScoreBoard.getScores();

        // Verify loaded scores
        assertEquals(3, loadedScores.size());
        assertEquals("Bob", loadedScores.get(0).getName());
        assertEquals(1500, loadedScores.get(0).getScore());
        assertEquals(ScoreBoard.ScoreEntry.Mode.NORMAL, loadedScores.get(0).getMode());
        assertEquals("Alice", loadedScores.get(1).getName());
        assertEquals(1000, loadedScores.get(1).getScore());
        assertEquals(ScoreBoard.ScoreEntry.Mode.NORMAL, loadedScores.get(1).getMode());
        assertEquals("Charlie", loadedScores.get(2).getName());
        assertEquals(800, loadedScores.get(2).getScore());
        assertEquals(ScoreBoard.ScoreEntry.Mode.NORMAL, loadedScores.get(2).getMode());
    }

    @Test
    void testLoadNonExistentFile() {
        // Ensure no scores.txt file exists
        File scoreFile = new File("scores.txt");
        if (scoreFile.exists()) {
            scoreFile.delete();
        }

        // Create new ScoreBoard - should handle missing file gracefully
        ScoreBoard newScoreBoard = new ScoreBoard();
        assertEquals(0, newScoreBoard.getScores().size());
    }

    @Test
    void testEmptyPlayerName() {
        scoreBoard.addScore("", 1000, ScoreBoard.ScoreEntry.Mode.NORMAL);
        scoreBoard.addScore("ValidName", 500, ScoreBoard.ScoreEntry.Mode.NORMAL);

        List<ScoreBoard.ScoreEntry> scores = scoreBoard.getScores();
        assertEquals(2, scores.size());

        // Should still work with empty name
        assertEquals("", scores.get(0).getName());
        assertEquals(1000, scores.get(0).getScore());
    }

    @Test
    void testZeroAndNegativeScores() {
        scoreBoard.addScore("Zero", 0, ScoreBoard.ScoreEntry.Mode.NORMAL);
        scoreBoard.addScore("Negative", -100, ScoreBoard.ScoreEntry.Mode.NORMAL);
        scoreBoard.addScore("Positive", 100, ScoreBoard.ScoreEntry.Mode.NORMAL);

        List<ScoreBoard.ScoreEntry> scores = scoreBoard.getScores();
        assertEquals(3, scores.size());

        // Should be sorted correctly including negative scores
        assertEquals(100, scores.get(0).getScore());
        assertEquals(0, scores.get(1).getScore());
        assertEquals(-100, scores.get(2).getScore());
    }

    @Test
    void testDuplicateScores() {
        scoreBoard.addScore("Player1", 1000, ScoreBoard.ScoreEntry.Mode.NORMAL);
        scoreBoard.addScore("Player2", 1000, ScoreBoard.ScoreEntry.Mode.NORMAL);
        scoreBoard.addScore("Player3", 1000, ScoreBoard.ScoreEntry.Mode.NORMAL);

        List<ScoreBoard.ScoreEntry> scores = scoreBoard.getScores();
        assertEquals(3, scores.size());

        // All should have the same score
        for (ScoreBoard.ScoreEntry entry : scores) {
            assertEquals(1000, entry.getScore());
        }
    }

    @Test
    void testGetLastAddedIndexBasic() {
        assertEquals(-1, scoreBoard.getLastAddedIndex());
        scoreBoard.addScore("Alice", 500, ScoreBoard.ScoreEntry.Mode.NORMAL);
        int idx = scoreBoard.getLastAddedIndex();
        // With only one element, it must be at index 0
        assertEquals(0, idx);
    }

    @Test
    void testGetLastAddedIndexWithSorting() {
        scoreBoard.addScore("Low", 100, ScoreBoard.ScoreEntry.Mode.NORMAL);
        scoreBoard.addScore("High", 1000, ScoreBoard.ScoreEntry.Mode.NORMAL);
        // Last added is High(1000) which should be at index 0 due to sorting
        assertEquals(0, scoreBoard.getLastAddedIndex());
        scoreBoard.addScore("Mid", 500, ScoreBoard.ScoreEntry.Mode.NORMAL);
        // Last added is Mid(500) which should be at index 1 now
        assertEquals(1, scoreBoard.getLastAddedIndex());
    }

    @Test
    void testGetLastAddedIndexAfterReset() {
        scoreBoard.addScore("Alice", 1000, ScoreBoard.ScoreEntry.Mode.NORMAL);
        assertTrue(scoreBoard.getLastAddedIndex() >= 0);
        scoreBoard.resetScores();
        assertEquals(-1, scoreBoard.getLastAddedIndex());
    }

    @Test
    void testGetLastAddedIndexAfterReload() {
        scoreBoard.addScore("Alice", 1000, ScoreBoard.ScoreEntry.Mode.NORMAL);
        scoreBoard.saveScores();
        // New instance will load from file and should not track lastAddedEntry
        ScoreBoard newBoard = new ScoreBoard();
        assertEquals(-1, newBoard.getLastAddedIndex());
    }

    @Test
    void testGetLastAddedEntry() {
        // Initially should be null
        assertNull(scoreBoard.getLastAddedEntry());

        // Add first score
        scoreBoard.addScore("Alice", 500, ScoreBoard.ScoreEntry.Mode.NORMAL);
        ScoreBoard.ScoreEntry lastEntry = scoreBoard.getLastAddedEntry();
        assertNotNull(lastEntry);
        assertEquals("Alice", lastEntry.getName());
        assertEquals(500, lastEntry.getScore());
        assertEquals(ScoreBoard.ScoreEntry.Mode.NORMAL, lastEntry.getMode());

        // Add higher score - should become new last added entry
        scoreBoard.addScore("Bob", 1000, ScoreBoard.ScoreEntry.Mode.HARD);
        lastEntry = scoreBoard.getLastAddedEntry();
        assertNotNull(lastEntry);
        assertEquals("Bob", lastEntry.getName());
        assertEquals(1000, lastEntry.getScore());
        assertEquals(ScoreBoard.ScoreEntry.Mode.HARD, lastEntry.getMode());
    }

    @Test
    void testGetLastAddedEntryAfterReset() {
        scoreBoard.addScore("Alice", 1000, ScoreBoard.ScoreEntry.Mode.NORMAL);
        assertNotNull(scoreBoard.getLastAddedEntry());

        scoreBoard.resetScores();
        assertNull(scoreBoard.getLastAddedEntry());
    }

    @Test
    void testGetLastAddedEntryAfterReload() {
        scoreBoard.addScore("Alice", 1000, ScoreBoard.ScoreEntry.Mode.NORMAL);
        assertNotNull(scoreBoard.getLastAddedEntry());
        scoreBoard.saveScores();

        // New instance will load from file and should not track lastAddedEntry
        ScoreBoard newBoard = new ScoreBoard();
        assertNull(newBoard.getLastAddedEntry());
    }

    @Test
    void testGetScoresByMode() {
        // Clear any existing scores
        scoreBoard.resetScores();

        // Add scores for different modes
        scoreBoard.addScore("NormalPlayer", 1000, ScoreBoard.ScoreEntry.Mode.NORMAL);
        scoreBoard.addScore("ItemPlayer", 1200, ScoreBoard.ScoreEntry.Mode.ITEM);

        // Test filtering by NORMAL mode
        List<ScoreBoard.ScoreEntry> normalScores = scoreBoard.getScoresByMode(ScoreBoard.ScoreEntry.Mode.NORMAL);
        assertEquals(1, normalScores.size());
        assertEquals("NormalPlayer", normalScores.get(0).getName());
        assertEquals(1000, normalScores.get(0).getScore());

        // Test filtering by ITEM mode
        List<ScoreBoard.ScoreEntry> itemScores = scoreBoard.getScoresByMode(ScoreBoard.ScoreEntry.Mode.ITEM);
        assertEquals(1, itemScores.size());
        assertEquals("ItemPlayer", itemScores.get(0).getName());
        assertEquals(1200, itemScores.get(0).getScore());

        // Test filtering by EASY mode (should be empty)
        List<ScoreBoard.ScoreEntry> easyScores = scoreBoard.getScoresByMode(ScoreBoard.ScoreEntry.Mode.EASY);
        assertEquals(0, easyScores.size());
    }

    @Test
    void testGetGameScores() {
        // Clear any existing scores
        scoreBoard.resetScores();

        // Add scores for different modes including ITEM
        scoreBoard.addScore("EasyPlayer", 800, ScoreBoard.ScoreEntry.Mode.EASY);
        scoreBoard.addScore("NormalPlayer", 1000, ScoreBoard.ScoreEntry.Mode.NORMAL);
        scoreBoard.addScore("HardPlayer", 1500, ScoreBoard.ScoreEntry.Mode.HARD);
        scoreBoard.addScore("ItemPlayer", 2000, ScoreBoard.ScoreEntry.Mode.ITEM);

        // Get normal game scores (should exclude ITEM)
        List<ScoreBoard.ScoreEntry> normalGameScores = scoreBoard.getGameScores();

        // Should only have 3 entries (EASY, NORMAL, HARD)
        assertEquals(3, normalGameScores.size());

        // Verify ITEM mode is not included
        for (ScoreBoard.ScoreEntry entry : normalGameScores) {
            assertNotEquals(ScoreBoard.ScoreEntry.Mode.ITEM, entry.getMode());
        }
        
        // Verify entries are sorted by score (descending)
        assertEquals("HardPlayer", normalGameScores.get(0).getName());
        assertEquals(1500, normalGameScores.get(0).getScore());
        assertEquals("NormalPlayer", normalGameScores.get(1).getName());
        assertEquals(1000, normalGameScores.get(1).getScore());
        assertEquals("EasyPlayer", normalGameScores.get(2).getName());
        assertEquals(800, normalGameScores.get(2).getScore());
    }

    @Test
    void testLoadOldFormatFile() throws Exception {
        // Create old format file (name,score without mode)
        File scoreFile = new File("scores.txt");
        try (PrintWriter writer = new PrintWriter(new FileWriter(scoreFile))) {
            writer.println("OldPlayer1,1000");
            writer.println("OldPlayer2,2000");
        }

        // Load scores - should default to NORMAL mode
        ScoreBoard newBoard = new ScoreBoard();
        List<ScoreBoard.ScoreEntry> scores = newBoard.getScores();

        assertEquals(2, scores.size());
        assertEquals("OldPlayer2", scores.get(0).getName());
        assertEquals(2000, scores.get(0).getScore());
        assertEquals(ScoreBoard.ScoreEntry.Mode.NORMAL, scores.get(0).getMode());
        assertEquals("OldPlayer1", scores.get(1).getName());
        assertEquals(1000, scores.get(1).getScore());
        assertEquals(ScoreBoard.ScoreEntry.Mode.NORMAL, scores.get(1).getMode());
    }

    @Test
    void testLoadNewFormatFile() throws Exception {
        // Create new format file (name,score,mode)
        File scoreFile = new File("scores.txt");
        try (PrintWriter writer = new PrintWriter(new FileWriter(scoreFile))) {
            writer.println("Player1,1000,EASY");
            writer.println("Player2,2000,HARD");
            writer.println("Player3,1500,ITEM");
        }

        // Load scores
        ScoreBoard newBoard = new ScoreBoard();
        List<ScoreBoard.ScoreEntry> scores = newBoard.getScores();

        assertEquals(3, scores.size());
        assertEquals("Player2", scores.get(0).getName());
        assertEquals(ScoreBoard.ScoreEntry.Mode.HARD, scores.get(0).getMode());
        assertEquals("Player3", scores.get(1).getName());
        assertEquals(ScoreBoard.ScoreEntry.Mode.ITEM, scores.get(1).getMode());
        assertEquals("Player1", scores.get(2).getName());
        assertEquals(ScoreBoard.ScoreEntry.Mode.EASY, scores.get(2).getMode());
    }

    @Test
    void testLoadMixedFormatFile() throws Exception {
        // Create mixed format file (some old, some new)
        File scoreFile = new File("scores.txt");
        try (PrintWriter writer = new PrintWriter(new FileWriter(scoreFile))) {
            writer.println("OldPlayer,1000"); // Old format
            writer.println("NewPlayer,2000,HARD"); // New format
        }

        // Load scores
        ScoreBoard newBoard = new ScoreBoard();
        List<ScoreBoard.ScoreEntry> scores = newBoard.getScores();

        assertEquals(2, scores.size());
        assertEquals("NewPlayer", scores.get(0).getName());
        assertEquals(ScoreBoard.ScoreEntry.Mode.HARD, scores.get(0).getMode());
        assertEquals("OldPlayer", scores.get(1).getName());
        assertEquals(ScoreBoard.ScoreEntry.Mode.NORMAL, scores.get(1).getMode()); // Default
    }

    @Test
    void testLoadCorruptedFile() throws Exception {
        // Create file with corrupted data
        File scoreFile = new File("scores.txt");
        try (PrintWriter writer = new PrintWriter(new FileWriter(scoreFile))) {
            writer.println("ValidPlayer,1000,NORMAL");
            writer.println("InvalidLine"); // Corrupted line
            writer.println("Player,NotANumber,EASY"); // Invalid score
            writer.println("Player,500,INVALID_MODE"); // Invalid mode
        }

        // Load scores - should handle errors gracefully
        ScoreBoard newBoard = new ScoreBoard();
        List<ScoreBoard.ScoreEntry> scores = newBoard.getScores();

        // Should only load valid entry
        assertEquals(1, scores.size());
        assertEquals("ValidPlayer", scores.get(0).getName());
    }

    @Test
    void testAllGameModes() {
        // Test all 4 game modes
        scoreBoard.addScore("EasyPlayer", 100, ScoreBoard.ScoreEntry.Mode.EASY);
        scoreBoard.addScore("NormalPlayer", 200, ScoreBoard.ScoreEntry.Mode.NORMAL);
        scoreBoard.addScore("HardPlayer", 300, ScoreBoard.ScoreEntry.Mode.HARD);
        scoreBoard.addScore("ItemPlayer", 400, ScoreBoard.ScoreEntry.Mode.ITEM);

        List<ScoreBoard.ScoreEntry> scores = scoreBoard.getScores();
        assertEquals(4, scores.size());

        // Verify all modes are present
        assertEquals(ScoreBoard.ScoreEntry.Mode.ITEM, scores.get(0).getMode());
        assertEquals(ScoreBoard.ScoreEntry.Mode.HARD, scores.get(1).getMode());
        assertEquals(ScoreBoard.ScoreEntry.Mode.NORMAL, scores.get(2).getMode());
        assertEquals(ScoreBoard.ScoreEntry.Mode.EASY, scores.get(3).getMode());
    }
}