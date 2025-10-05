package team13.tetris.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * Test class for ScoreBoard functionality
 */
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
        scoreBoard.addScore("Alice", 1000);
        
        List<ScoreBoard.ScoreEntry> scores = scoreBoard.getScores();
        assertEquals(1, scores.size());
        assertEquals("Alice", scores.get(0).getName());
        assertEquals(1000, scores.get(0).getScore());
    }
    
    @Test
    void testAddMultipleScores() {
        // Test adding multiple scores
        scoreBoard.addScore("Alice", 1000);
        scoreBoard.addScore("Bob", 1500);
        scoreBoard.addScore("Charlie", 800);
        
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
        scoreBoard.addScore("Low", 100);
        scoreBoard.addScore("High", 2000);
        scoreBoard.addScore("Medium", 1000);
        
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
        scoreBoard.addScore("Alice", 1000);
        scoreBoard.addScore("Bob", 1500);
        
        assertEquals(2, scoreBoard.getScores().size());
        
        // Reset scores
        scoreBoard.resetScores();
        
        assertEquals(0, scoreBoard.getScores().size());
    }
    
    @Test
    void testScoreEntryToString() {
        ScoreBoard.ScoreEntry entry = new ScoreBoard.ScoreEntry("TestPlayer", 12345);
        assertEquals("TestPlayer: 12345", entry.toString());
    }
    
    @Test
    void testSaveAndLoadScores() {
        // Add some scores
        scoreBoard.addScore("Alice", 1000);
        scoreBoard.addScore("Bob", 1500);
        scoreBoard.addScore("Charlie", 800);
        
        // Save scores
        scoreBoard.saveScores();
        
        // Create new ScoreBoard instance to test loading
        ScoreBoard newScoreBoard = new ScoreBoard();
        List<ScoreBoard.ScoreEntry> loadedScores = newScoreBoard.getScores();
        
        // Verify loaded scores
        assertEquals(3, loadedScores.size());
        assertEquals("Bob", loadedScores.get(0).getName());
        assertEquals(1500, loadedScores.get(0).getScore());
        assertEquals("Alice", loadedScores.get(1).getName());
        assertEquals(1000, loadedScores.get(1).getScore());
        assertEquals("Charlie", loadedScores.get(2).getName());
        assertEquals(800, loadedScores.get(2).getScore());
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
        scoreBoard.addScore("", 1000);
        scoreBoard.addScore("ValidName", 500);
        
        List<ScoreBoard.ScoreEntry> scores = scoreBoard.getScores();
        assertEquals(2, scores.size());
        
        // Should still work with empty name
        assertEquals("", scores.get(0).getName());
        assertEquals(1000, scores.get(0).getScore());
    }
    
    @Test
    void testZeroAndNegativeScores() {
        scoreBoard.addScore("Zero", 0);
        scoreBoard.addScore("Negative", -100);
        scoreBoard.addScore("Positive", 100);
        
        List<ScoreBoard.ScoreEntry> scores = scoreBoard.getScores();
        assertEquals(3, scores.size());
        
        // Should be sorted correctly including negative scores
        assertEquals(100, scores.get(0).getScore());
        assertEquals(0, scores.get(1).getScore());
        assertEquals(-100, scores.get(2).getScore());
    }
    
    @Test
    void testDuplicateScores() {
        scoreBoard.addScore("Player1", 1000);
        scoreBoard.addScore("Player2", 1000);
        scoreBoard.addScore("Player3", 1000);
        
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
        scoreBoard.addScore("Alice", 500);
        int idx = scoreBoard.getLastAddedIndex();
        // With only one element, it must be at index 0
        assertEquals(0, idx);
    }

    @Test
    void testGetLastAddedIndexWithSorting() {
        scoreBoard.addScore("Low", 100);
        scoreBoard.addScore("High", 1000);
        // Last added is High(1000) which should be at index 0 due to sorting
        assertEquals(0, scoreBoard.getLastAddedIndex());
        scoreBoard.addScore("Mid", 500);
        // Last added is Mid(500) which should be at index 1 now
        assertEquals(1, scoreBoard.getLastAddedIndex());
    }

    @Test
    void testGetLastAddedIndexAfterReset() {
        scoreBoard.addScore("Alice", 1000);
        assertTrue(scoreBoard.getLastAddedIndex() >= 0);
        scoreBoard.resetScores();
        assertEquals(-1, scoreBoard.getLastAddedIndex());
    }

    @Test
    void testGetLastAddedIndexAfterReload() {
        scoreBoard.addScore("Alice", 1000);
        scoreBoard.saveScores();
        // New instance will load from file and should not track lastAddedEntry
        ScoreBoard newBoard = new ScoreBoard();
        assertEquals(-1, newBoard.getLastAddedIndex());
    }
}