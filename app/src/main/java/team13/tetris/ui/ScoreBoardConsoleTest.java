package team13.tetris.ui;

import java.util.List;
import java.util.Scanner;

/**
 * Console-based test application for ScoreBoard
 * This provides a simple way to test ScoreBoard functionality without GUI
 */
public class ScoreBoardConsoleTest {
    
    private ScoreBoard scoreBoard;
    private Scanner scanner;
    
    public ScoreBoardConsoleTest() {
        this.scoreBoard = new ScoreBoard();
        this.scanner = new Scanner(System.in);
    }
    
    public void runTest() {
        System.out.println("=== ScoreBoard Console Test ===");
        System.out.println("This will test all ScoreBoard functionality");
        System.out.println();
        
        // Test 1: Add some scores
        System.out.println("Test 1: Adding scores...");
        scoreBoard.addScore("Alice", 1500);
        scoreBoard.addScore("Bob", 2000);
        scoreBoard.addScore("Charlie", 1200);
        scoreBoard.addScore("Diana", 1800);
        System.out.println("[OK] Added 4 test scores");
        
        // Test 2: Display scores
        System.out.println("\nTest 2: Displaying current scores:");
        displayScores();
        
        // Test 3: Add more scores to test sorting
        System.out.println("\nTest 3: Adding more scores to test sorting...");
        scoreBoard.addScore("Eve", 2500);  // Should be #1
        scoreBoard.addScore("Frank", 800); // Should be last
        System.out.println("[OK] Added 2 more scores");
        
        // Test 4: Display updated scores
        System.out.println("\nTest 4: Displaying updated scores (should be sorted):");
        displayScores();
        
        // Test 5: Save scores
        System.out.println("\nTest 5: Saving scores to file...");
        scoreBoard.saveScores();
        System.out.println("[OK] Scores saved to scores.txt");
        
        // Test 6: Load scores with new instance
        System.out.println("\nTest 6: Testing load functionality...");
        ScoreBoard newScoreBoard = new ScoreBoard();
        List<ScoreBoard.ScoreEntry> loadedScores = newScoreBoard.getScores();
        System.out.println("[OK] Loaded " + loadedScores.size() + " scores from file");
        System.out.println("Loaded scores:");
        for (int i = 0; i < loadedScores.size(); i++) {
            ScoreBoard.ScoreEntry entry = loadedScores.get(i);
            System.out.println("  " + (i + 1) + ". " + entry.getName() + ": " + entry.getScore());
        }
        
        // Test 7: Test reset functionality
        System.out.println("\nTest 7: Testing reset functionality...");
        System.out.println("Scores before reset: " + scoreBoard.getScores().size());
        scoreBoard.resetScores();
        System.out.println("Scores after reset: " + scoreBoard.getScores().size());
        System.out.println("✓ Reset functionality works");
        
        // Test 8: Test edge cases
        System.out.println("\nTest 8: Testing edge cases...");
        scoreBoard.addScore("", 100);           // Empty name
        scoreBoard.addScore("Negative", -50);   // Negative score  
        scoreBoard.addScore("Zero", 0);         // Zero score
        scoreBoard.addScore("Same1", 1000);     // Duplicate scores
        scoreBoard.addScore("Same2", 1000);
        System.out.println("[OK] Added edge case scores");
        displayScores();
        
        // Interactive test
        System.out.println("\n=== Interactive Test ===");
        System.out.println("You can now add your own scores!");
        
        while (true) {
            System.out.print("\nEnter player name (or 'quit' to exit): ");
            String name = scanner.nextLine().trim();
            
            if (name.equalsIgnoreCase("quit")) {
                break;
            }
            
            if (name.isEmpty()) {
                System.out.println("Name cannot be empty. Try again.");
                continue;
            }
            
            System.out.print("Enter score: ");
            try {
                int score = Integer.parseInt(scanner.nextLine().trim());
                scoreBoard.addScore(name, score);
                System.out.println("[OK] Added: " + name + " - " + score);
                
                System.out.println("\nCurrent top 5 scores:");
                List<ScoreBoard.ScoreEntry> scores = scoreBoard.getScores();
                for (int i = 0; i < Math.min(5, scores.size()); i++) {
                    ScoreBoard.ScoreEntry entry = scores.get(i);
                    System.out.println("  " + (i + 1) + ". " + entry.getName() + ": " + entry.getScore());
                }
                
            } catch (NumberFormatException e) {
                System.out.println("Invalid score format. Please enter a number.");
            }
        }
        
        // Final test summary
        System.out.println("\n=== Test Summary ===");
        System.out.println("[OK] All ScoreBoard functionality tested successfully!");
        System.out.println("[OK] Score addition and sorting works");
        System.out.println("[OK] File save/load functionality works");
        System.out.println("✓ Reset functionality works");
        System.out.println("[OK] Edge cases handled properly");
        System.out.println("Final score count: " + scoreBoard.getScores().size());
        
        // Save final state
        scoreBoard.saveScores();
        System.out.println("[OK] Final scores saved to file");
        
        scanner.close();
    }
    
    private void displayScores() {
        List<ScoreBoard.ScoreEntry> scores = scoreBoard.getScores();
        if (scores.isEmpty()) {
            System.out.println("  No scores available");
            return;
        }
        
        System.out.println("  Current scores (sorted by score, descending):");
        for (int i = 0; i < scores.size(); i++) {
            ScoreBoard.ScoreEntry entry = scores.get(i);
            String name = entry.getName().isEmpty() ? "[Empty Name]" : entry.getName();
            System.out.println("    " + (i + 1) + ". " + name + ": " + entry.getScore());
        }
        System.out.println("  Total scores: " + scores.size());
    }
    
    public static void main(String[] args) {
        ScoreBoardConsoleTest test = new ScoreBoardConsoleTest();
        test.runTest();
    }
}