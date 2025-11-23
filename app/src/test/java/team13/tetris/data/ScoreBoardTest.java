package team13.tetris.data;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

// ScoreBoard 테스트 클래스: Tests score addition, sorting, persistence, and edge cases
@DisplayName("ScoreBoard 테스트")
public class ScoreBoardTest {

    private ScoreBoard scoreBoard;
    private String originalWorkingDir;

    @TempDir Path tempDir;

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws IOException {
        this.tempDir = tempDir;

        // Save original working directory
        originalWorkingDir = System.getProperty("user.dir");

        // Change working directory to temp directory BEFORE creating ScoreBoard
        System.setProperty("user.dir", tempDir.toString());

        // Create fresh ScoreBoard instance with test-specific file name
        // This prevents interference with actual game's scores.txt
        scoreBoard = new ScoreBoard("test_scores.txt");
    }

    @AfterEach
    void tearDown() {
        // Restore original working directory
        System.setProperty("user.dir", originalWorkingDir);

        // Clean up test-specific score files only
        File tempScoreFile = tempDir.resolve("test_scores.txt").toFile();
        if (tempScoreFile.exists()) tempScoreFile.delete();

        // Clean up test_scores.txt in current directory if it exists
        // (DO NOT delete scores.txt - that's the actual game data!)
        File currentTestScoreFile = new File("test_scores.txt");
        if (currentTestScoreFile.exists()) currentTestScoreFile.delete();
    }

    @Test
    @DisplayName("점수 추가: 단일 점수 추가 테스트")
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
    @DisplayName("점수 추가: 여러 점수 추가 및 정렬 테스트")
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
    @DisplayName("점수 정렬: 내림차순 자동 정렬 확인")
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
    @DisplayName("점수 초기화: 모든 점수 삭제")
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
    @DisplayName("ScoreEntry toString: 문자열 변환 형식 확인")
    void testScoreEntryToString() {
        ScoreBoard.ScoreEntry entry =
                new ScoreBoard.ScoreEntry("TestPlayer", 12345, ScoreBoard.ScoreEntry.Mode.NORMAL);
        assertEquals("TestPlayer: 12345 (NORMAL)", entry.toString());
    }

    @Test
    @DisplayName("파일 저장/로딩: 점수 영속성 테스트")
    void testSaveAndLoadScores() {
        // Add some scores
        scoreBoard.addScore("Alice", 1000, ScoreBoard.ScoreEntry.Mode.NORMAL);
        scoreBoard.addScore("Bob", 1500, ScoreBoard.ScoreEntry.Mode.NORMAL);
        scoreBoard.addScore("Charlie", 800, ScoreBoard.ScoreEntry.Mode.NORMAL);

        // Save scores
        scoreBoard.saveScores();

        // Create new ScoreBoard instance to test loading
        ScoreBoard newScoreBoard = new ScoreBoard("test_scores.txt");
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
    @DisplayName("파일 로딩: 존재하지 않는 파일 처리")
    void testLoadNonExistentFile() {
        // Ensure no test_scores.txt file exists
        File scoreFile = new File("test_scores.txt");
        if (scoreFile.exists()) scoreFile.delete();

        // Create new ScoreBoard - should handle missing file gracefully
        ScoreBoard newScoreBoard = new ScoreBoard("test_scores.txt");
        assertEquals(0, newScoreBoard.getScores().size());
    }

    @Test
    @DisplayName("엣지 케이스: 빈 플레이어 이름 처리")
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
    @DisplayName("엣지 케이스: 0점 및 음수 점수 처리")
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
    @DisplayName("엣지 케이스: 동일한 점수 여러 개 처리")
    void testDuplicateScores() {
        scoreBoard.addScore("Player1", 1000, ScoreBoard.ScoreEntry.Mode.NORMAL);
        scoreBoard.addScore("Player2", 1000, ScoreBoard.ScoreEntry.Mode.NORMAL);
        scoreBoard.addScore("Player3", 1000, ScoreBoard.ScoreEntry.Mode.NORMAL);

        List<ScoreBoard.ScoreEntry> scores = scoreBoard.getScores();
        assertEquals(3, scores.size());

        // All should have the same score
        for (ScoreBoard.ScoreEntry entry : scores) assertEquals(1000, entry.getScore());
    }

    @Test
    @DisplayName("마지막 추가 인덱스: 기본 동작 확인")
    void testGetLastAddedIndexBasic() {
        assertEquals(-1, scoreBoard.getLastAddedIndex());
        scoreBoard.addScore("Alice", 500, ScoreBoard.ScoreEntry.Mode.NORMAL);
        int idx = scoreBoard.getLastAddedIndex();
        // With only one element, it must be at index 0
        assertEquals(0, idx);
    }

    @Test
    @DisplayName("마지막 추가 인덱스: 정렬 후 위치 추적")
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
    @DisplayName("마지막 추가 인덱스: 초기화 후 상태")
    void testGetLastAddedIndexAfterReset() {
        scoreBoard.addScore("Alice", 1000, ScoreBoard.ScoreEntry.Mode.NORMAL);
        assertTrue(scoreBoard.getLastAddedIndex() >= 0);
        scoreBoard.resetScores();
        assertEquals(-1, scoreBoard.getLastAddedIndex());
    }

    @Test
    @DisplayName("마지막 추가 인덱스: 파일 재로드 후 상태")
    void testGetLastAddedIndexAfterReload() {
        scoreBoard.addScore("Alice", 1000, ScoreBoard.ScoreEntry.Mode.NORMAL);
        scoreBoard.saveScores();
        // New instance will load from file and should not track lastAddedEntry
        ScoreBoard newBoard = new ScoreBoard("test_scores.txt");
        assertEquals(-1, newBoard.getLastAddedIndex());
    }

    @Test
    @DisplayName("마지막 추가 엔트리: 가장 최근 추가된 항목 조회")
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
    @DisplayName("마지막 추가 엔트리: 초기화 후 상태")
    void testGetLastAddedEntryAfterReset() {
        scoreBoard.addScore("Alice", 1000, ScoreBoard.ScoreEntry.Mode.NORMAL);
        assertNotNull(scoreBoard.getLastAddedEntry());

        scoreBoard.resetScores();
        assertNull(scoreBoard.getLastAddedEntry());
    }

    @Test
    @DisplayName("마지막 추가 엔트리: 파일 재로드 후 상태")
    void testGetLastAddedEntryAfterReload() {
        scoreBoard.addScore("Alice", 1000, ScoreBoard.ScoreEntry.Mode.NORMAL);
        assertNotNull(scoreBoard.getLastAddedEntry());
        scoreBoard.saveScores();

        // New instance will load from file and should not track lastAddedEntry
        ScoreBoard newBoard = new ScoreBoard("test_scores.txt");
        assertNull(newBoard.getLastAddedEntry());
    }

    @Test
    @DisplayName("모드별 점수 조회: 필터링 기능 테스트")
    void testGetScoresByMode() {
        // Clear any existing scores
        scoreBoard.resetScores();

        // Add scores for different modes
        scoreBoard.addScore("NormalPlayer", 1000, ScoreBoard.ScoreEntry.Mode.NORMAL);
        scoreBoard.addScore("ItemPlayer", 1200, ScoreBoard.ScoreEntry.Mode.ITEM);

        // Test filtering by NORMAL mode
        List<ScoreBoard.ScoreEntry> normalScores =
                scoreBoard.getScoresByMode(ScoreBoard.ScoreEntry.Mode.NORMAL);
        assertEquals(1, normalScores.size());
        assertEquals("NormalPlayer", normalScores.get(0).getName());
        assertEquals(1000, normalScores.get(0).getScore());

        // Test filtering by ITEM mode
        List<ScoreBoard.ScoreEntry> itemScores =
                scoreBoard.getScoresByMode(ScoreBoard.ScoreEntry.Mode.ITEM);
        assertEquals(1, itemScores.size());
        assertEquals("ItemPlayer", itemScores.get(0).getName());
        assertEquals(1200, itemScores.get(0).getScore());

        // Test filtering by EASY mode (should be empty)
        List<ScoreBoard.ScoreEntry> easyScores =
                scoreBoard.getScoresByMode(ScoreBoard.ScoreEntry.Mode.EASY);
        assertEquals(0, easyScores.size());
    }

    @Test
    @DisplayName("게임 모드별 점수 조회: 올바른 필터링 및 정렬 확인")
    void testGetGameScores() {
        // Clear any existing scores
        scoreBoard.resetScores();

        // Add scores for different modes including ITEM
        scoreBoard.addScore("EasyPlayer", 800, ScoreBoard.ScoreEntry.Mode.EASY);
        scoreBoard.addScore("NormalPlayer", 1000, ScoreBoard.ScoreEntry.Mode.NORMAL);
        scoreBoard.addScore("HardPlayer", 1500, ScoreBoard.ScoreEntry.Mode.HARD);
        scoreBoard.addScore("ItemPlayer", 2000, ScoreBoard.ScoreEntry.Mode.ITEM);

        // Get game scores
        List<ScoreBoard.ScoreEntry> gameScores = scoreBoard.getGameScores();

        // Should have 4 entries (EASY, NORMAL, HARD, ITEM)
        assertEquals(4, gameScores.size());

        // Verify entries are sorted by score (descending)
        assertEquals("ItemPlayer", gameScores.get(0).getName());
        assertEquals(2000, gameScores.get(0).getScore());
        assertEquals(ScoreBoard.ScoreEntry.Mode.ITEM, gameScores.get(0).getMode());

        assertEquals("HardPlayer", gameScores.get(1).getName());
        assertEquals(1500, gameScores.get(1).getScore());
        assertEquals(ScoreBoard.ScoreEntry.Mode.HARD, gameScores.get(1).getMode());

        assertEquals("NormalPlayer", gameScores.get(2).getName());
        assertEquals(1000, gameScores.get(2).getScore());
        assertEquals(ScoreBoard.ScoreEntry.Mode.NORMAL, gameScores.get(2).getMode());

        assertEquals("EasyPlayer", gameScores.get(3).getName());
        assertEquals(800, gameScores.get(3).getScore());
        assertEquals(ScoreBoard.ScoreEntry.Mode.EASY, gameScores.get(3).getMode());
    }

    @Test
    @DisplayName("파일 로딩: 신버전 포맷 처리")
    void testLoadNewFormatFile() throws Exception {
        // Create new format file (name,score,mode)
        File scoreFile = new File("test_scores.txt");
        try (PrintWriter writer = new PrintWriter(new FileWriter(scoreFile))) {
            writer.println("Player1,1000,EASY");
            writer.println("Player2,2000,HARD");
            writer.println("Player3,1500,ITEM");
        }

        // Load scores
        ScoreBoard newBoard = new ScoreBoard("test_scores.txt");
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
    @DisplayName("파일 로딩: 손상된 데이터 처리")
    void testLoadCorruptedFile() throws Exception {
        // Create file with corrupted data
        File scoreFile = new File("test_scores.txt");
        try (PrintWriter writer = new PrintWriter(new FileWriter(scoreFile))) {
            writer.println("ValidPlayer,1000,NORMAL");
            writer.println("InvalidLine"); // Corrupted line
            writer.println("Player,NotANumber,EASY"); // Invalid score
            writer.println("Player,500,INVALID_MODE"); // Invalid mode
        }

        // Load scores - should handle errors gracefully
        ScoreBoard newBoard = new ScoreBoard("test_scores.txt");
        List<ScoreBoard.ScoreEntry> scores = newBoard.getScores();

        // Should only load valid entry
        assertEquals(1, scores.size());
        assertEquals("ValidPlayer", scores.get(0).getName());
    }

    @Test
    @DisplayName("모든 게임 모드 점수 추가 및 조회 테스트")
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
