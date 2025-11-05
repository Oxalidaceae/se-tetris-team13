package team13.tetris.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.*;

// GameManager 테스트: Tests game state transitions, scoring, difficulty progression, timer and scoreboard integration
@DisplayName("GameManager 테스트")
public class GameManagerTest {

    private GameManager gameManager;

    @TempDir
    Path tempDir;

    private File originalScoresFile;
    private File backupScoresFile;

    @BeforeEach
    void setUp() throws IOException {
        // scores.txt 백업
        originalScoresFile = new File("scores.txt");
        if (originalScoresFile.exists()) {
            backupScoresFile = new File(tempDir.toFile(), "scores_backup.txt");
            Files.copy(originalScoresFile.toPath(), backupScoresFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        gameManager = new GameManager();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (gameManager != null && gameManager.isGameRunning()) gameManager.endGame(false);

        // scores.txt 복원
        if (backupScoresFile != null && backupScoresFile.exists()) {
            Files.copy(backupScoresFile.toPath(), originalScoresFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } else if (originalScoresFile != null && originalScoresFile.exists()) {
            // 백업이 없었다면 테스트로 생성된 파일 삭제
            originalScoresFile.delete();
        }
    }

    @Test
    @DisplayName("초기 상태가 올바르게 설정되는지 확인")
    void testInitialState() {
        assertEquals(GameState.READY, gameManager.getState());
        assertEquals(0, gameManager.getCurrentScore());
        assertEquals(0, gameManager.getLinesCleared());
        assertFalse(gameManager.isGameRunning());
        assertNotNull(gameManager.getGameTimer());
        assertNotNull(gameManager.getScoreBoard());
    }

    @Test
    @DisplayName("게임 시작이 올바르게 동작하는지 확인")
    void testStartGame() {
        gameManager.startGame();
        assertEquals(GameState.PLAYING, gameManager.getState());
        assertTrue(gameManager.isGameRunning());
        assertEquals(0, gameManager.getCurrentScore());
        assertEquals(0, gameManager.getLinesCleared());
    }

    @Test
    @DisplayName("일시정지와 재개가 올바르게 동작하는지 확인")
    void testPauseResume() {
        gameManager.startGame();
        assertEquals(GameState.PLAYING, gameManager.getState());

        gameManager.togglePause();
        assertEquals(GameState.PAUSED, gameManager.getState());
        assertTrue(gameManager.isGameRunning()); // Paused is still "running"

        gameManager.togglePause();
        assertEquals(GameState.PLAYING, gameManager.getState());
        assertTrue(gameManager.isGameRunning());
    }

    @Test
    @DisplayName("게임 종료가 올바르게 동작하는지 확인")
    void testEndGame() {
        gameManager.startGame();
        assertTrue(gameManager.isGameRunning());

        gameManager.endGame(false);
        assertEquals(GameState.GAME_OVER, gameManager.getState());
        assertFalse(gameManager.isGameRunning());
    }

    @Test
    @DisplayName("점수 추가가 올바르게 동작하는지 확인")
    void testScoring() {
        gameManager.addScore(100);
        assertEquals(100, gameManager.getCurrentScore());

        gameManager.addScore(50);
        assertEquals(150, gameManager.getCurrentScore());
    }

    @Test
    @DisplayName("라인 클리어 시 점수와 라인 수가 올바르게 업데이트되는지 확인")
    void testLinesClearedScoring() {
        // Test single line clear
        gameManager.linesCleared(1);
        assertEquals(1, gameManager.getLinesCleared());
        assertTrue(gameManager.getCurrentScore() > 0);

        int scoreAfterOne = gameManager.getCurrentScore();

        // Test tetris (4 lines)
        gameManager.linesCleared(4);
        assertEquals(5, gameManager.getLinesCleared());
        assertTrue(gameManager.getCurrentScore() > scoreAfterOne);
    }

    @Test
    @DisplayName("난이도가 10라인마다 증가하는지 확인")
    void testDifficultyProgression() {
        assertEquals(0, gameManager.getDifficultyLevel());

        // Clear 10 lines to increase difficulty
        gameManager.linesCleared(10);
        assertEquals(1, gameManager.getDifficultyLevel());

        // Clear 10 more lines
        gameManager.linesCleared(10);
        assertEquals(2, gameManager.getDifficultyLevel());
    }

    @Test
    @DisplayName("라인 클리어에 따라 속도가 점진적으로 증가하는지 확인")
    void testSpeedProgression() {
        gameManager.startGame(); // Start game to initialize lastDifficultyLevel

        Timer timer = gameManager.getGameTimer();
        double initialSpeed = timer.getSpeedFactor();

        // Test speed factor increases progressively
        gameManager.linesCleared(10); // First difficulty increase
        assertTrue(timer.getSpeedFactor() > initialSpeed,
                "Speed factor should increase after 10 lines. Initial: " + initialSpeed + ", Current: " + timer.getSpeedFactor());

        double speedAfter10 = timer.getSpeedFactor();
        gameManager.linesCleared(10); // Second difficulty increase (total 20 lines)
        assertTrue(timer.getSpeedFactor() > speedAfter10,
                "Speed factor should increase after 20 lines. After 10: " + speedAfter10 + ", Current: " + timer.getSpeedFactor());

        // Test that speed level eventually increases (need speedFactor >= 2.0)
        // Clear lines until speed level increases
        // Clear 8 more sets of 10 (total 100 lines)
        for (int i = 0; i < 8; i++) gameManager.linesCleared(10);

        assertTrue(timer.getSpeedFactor() >= 2.0, "Speed factor should be >= 2.0 after 100 lines. Current: " + timer.getSpeedFactor());
        assertTrue(timer.getSpeedLevel() >= 2, "Speed level should be >= 2 after 100 lines. Current: " + timer.getSpeedLevel());
    }

    @Test
    @DisplayName("Timer 통합이 올바르게 동작하는지 확인")
    void testTimerIntegration() {
        Timer timer = gameManager.getGameTimer();

        // Test timer tick
        timer.tick(1.0);
        assertEquals(1.0, timer.getElapsedTime(), 0.001);

        // Test timer reset
        timer.reset();
        assertEquals(0.0, timer.getElapsedTime(), 0.001);
    }

    @Test
    @DisplayName("ScoreBoard 통합이 올바르게 동작하는지 확인")
    void testScoreBoardIntegration() {
        assertNotNull(gameManager.getScoreBoard());

        // Test game over adds score to scoreboard
        gameManager.startGame();
        gameManager.addScore(500);
        gameManager.endGame(true); // This should add score

        assertTrue(gameManager.getScoreBoard().getScores().size() > 0);
    }

    @Test
    @DisplayName("게임 상태 전환이 올바르게 동작하는지 확인")
    void testGameStateTransitions() {
        // Test complete lifecycle
        assertEquals(GameState.READY, gameManager.getState());

        gameManager.startGame();
        assertEquals(GameState.PLAYING, gameManager.getState());

        gameManager.togglePause();
        assertEquals(GameState.PAUSED, gameManager.getState());

        gameManager.togglePause();
        assertEquals(GameState.PLAYING, gameManager.getState());

        gameManager.endGame(false);
        assertEquals(GameState.GAME_OVER, gameManager.getState());
    }

    @Test
    @DisplayName("여러 게임을 연속으로 실행할 수 있는지 확인")
    void testMultipleGames() {
        // First game
        gameManager.startGame();
        gameManager.addScore(100);
        gameManager.endGame(false);

        // Start new game - should reset
        gameManager.startGame();
        assertEquals(0, gameManager.getCurrentScore());
        assertEquals(GameState.PLAYING, gameManager.getState());
    }

    @Test
    @DisplayName("표준 테트리스 점수 체계가 올바르게 적용되는지 확인")
    void testLineScoring() {
        // Test standard Tetris scoring
        int speedLevel = gameManager.getSpeedLevel();

        // Single line
        gameManager.linesCleared(1);
        int expectedSingle = 100 * speedLevel;
        assertTrue(gameManager.getCurrentScore() >= expectedSingle);

        int scoreAfterSingle = gameManager.getCurrentScore();

        // Double line
        gameManager.linesCleared(2);
        int expectedDouble = 300 * speedLevel;
        assertTrue(gameManager.getCurrentScore() >= scoreAfterSingle + expectedDouble);

        int scoreAfterDouble = gameManager.getCurrentScore();

        // Triple line
        gameManager.linesCleared(3);
        int expectedTriple = 500 * speedLevel;
        assertTrue(gameManager.getCurrentScore() >= scoreAfterDouble + expectedTriple);

        int scoreAfterTriple = gameManager.getCurrentScore();

        // Tetris (4 lines)
        gameManager.linesCleared(4);
        int expectedTetris = 800 * speedLevel;
        assertTrue(gameManager.getCurrentScore() >= scoreAfterTriple + expectedTetris);
    }

    @Test
    @DisplayName("게임 루프 상태 처리가 올바르게 동작하는지 확인")
    void testGameLoopStateHandling() throws InterruptedException {
        // Start game and let it run briefly
        gameManager.startGame();
        Thread.sleep(50); // Let game loop run

        // Pause should stop updates
        gameManager.togglePause();
        assertEquals(GameState.PAUSED, gameManager.getState());

        // Resume should continue
        gameManager.togglePause();
        assertEquals(GameState.PLAYING, gameManager.getState());

        // End game should stop loop
        gameManager.endGame(false);
        assertEquals(GameState.GAME_OVER, gameManager.getState());
    }

    @Test
    @DisplayName("소프트 드롭 점수가 올바르게 추가되는지 확인")
    void testAddSoftDropScore() {
        // given
        gameManager.startGame();
        int initialScore = gameManager.getCurrentScore();

        // when
        gameManager.addSoftDropScore();

        // then
        assertTrue(gameManager.getCurrentScore() > initialScore, "소프트 드롭 후 점수가 증가해야 함");

        // 기본 속도(1x)에서 소프트 드롭은 10점
        assertEquals(initialScore + 10, gameManager.getCurrentScore(), "기본 속도에서 소프트 드롭은 10점");
    }

    @Test
    @DisplayName("하드 드롭 점수가 거리에 따라 올바르게 계산되는지 확인")
    void testAddHardDropScore() {
        // given
        gameManager.startGame();
        int initialScore = gameManager.getCurrentScore();

        // when - 5칸 하드 드롭
        gameManager.addHardDropScore(5);

        // then
        // 기본 속도(1x)에서 하드 드롭: 10 * 거리 * speedFactor = 10 * 5 * 1 = 50점
        assertEquals(initialScore + 50, gameManager.getCurrentScore(), "5칸 하드 드롭은 50점");

        // when - 10칸 하드 드롭
        initialScore = gameManager.getCurrentScore();
        gameManager.addHardDropScore(10);

        // then
        assertEquals(initialScore + 100, gameManager.getCurrentScore(), "10칸 하드 드롭은 100점");
    }

    @Test
    @DisplayName("자동 드롭 점수가 올바르게 추가되는지 확인")
    void testAddAutoDropScore() {
        // given
        gameManager.startGame();
        int initialScore = gameManager.getCurrentScore();

        // when
        gameManager.addAutoDropScore(1);

        // then
        assertEquals(initialScore + 10, gameManager.getCurrentScore(), "1칸 자동 드롭은 10점");
    }

    @Test
    @DisplayName("속도 증가 후 드롭 점수가 올바르게 배율 적용되는지 확인")
    void testDropScoreWithSpeedMultiplier() {
        // given
        gameManager.startGame();

        // 속도를 2배로 증가 (10라인 클리어)
        gameManager.linesCleared(10);

        int initialScore = gameManager.getCurrentScore();

        // when
        gameManager.addSoftDropScore();

        // then
        int addedScore = gameManager.getCurrentScore() - initialScore;
        assertTrue(addedScore > 10, "속도 증가 후 점수가 더 많이 추가되어야 함");
    }

    @Test
    @DisplayName("게임 종료 시 스코어보드에 점수가 추가되는지 확인")
    void testGameOverAddsToScoreBoard() {
        // given
        int initialScoreCount = gameManager.getScoreBoard().getScores().size();

        gameManager.startGame();
        gameManager.addScore(1000);

        // when
        gameManager.endGame(true); // showUI = true

        // then
        int finalScoreCount = gameManager.getScoreBoard().getScores().size();
        assertEquals(initialScoreCount + 1, finalScoreCount, "스코어보드에 점수가 추가되어야 함");

        // 마지막 추가된 점수가 1000인지 확인
        var scores = gameManager.getScoreBoard().getScores();
        assertTrue(scores.stream().anyMatch(entry -> entry.getScore() == 1000), "1000점이 스코어보드에 기록되어야 함");
    }

    @Test
    @DisplayName("게임 종료 시 showUI=false면 스코어보드에 추가하지 않는지 확인")
    void testGameOverWithoutUI() {
        // given
        int initialScoreCount = gameManager.getScoreBoard().getScores().size();

        gameManager.startGame();
        gameManager.addScore(500);

        // when
        gameManager.endGame(false); // showUI = false

        // then
        int finalScoreCount = gameManager.getScoreBoard().getScores().size();
        assertEquals(initialScoreCount, finalScoreCount, "showUI=false일 때는 스코어보드에 추가하지 않아야 함");
    }

    @Test
    @DisplayName("getSpeedLevel이 타이머의 속도 레벨을 반환하는지 확인")
    void testGetSpeedLevel() {
        // given
        gameManager.startGame();

        // when
        int speedLevel = gameManager.getSpeedLevel();

        // then
        assertEquals(gameManager.getGameTimer().getSpeedLevel(), speedLevel, "getSpeedLevel은 타이머의 속도 레벨을 반환해야 함");
        assertEquals(1, speedLevel, "초기 속도 레벨은 1");

        // 속도 증가 후
        gameManager.linesCleared(10);
        assertTrue(gameManager.getSpeedLevel() >= 1, "라인 클리어 후 속도 레벨 유지 또는 증가");
    }

    @Test
    @DisplayName("라인 클리어 점수가 속도 레벨에 비례하는지 확인")
    void testLineScoringWithSpeedLevel() {
        // given
        gameManager.startGame();

        // 초기 속도 레벨(1)에서 1라인 클리어
        gameManager.linesCleared(1);
        int scoreAtSpeedLevel1 = gameManager.getCurrentScore();
        assertEquals(100, scoreAtSpeedLevel1, "속도 레벨 1에서 1라인 = 100점");

        // 속도를 증가시킨 후 (10라인 더 클리어)
        gameManager.linesCleared(10);
        int scoreBeforeNextLine = gameManager.getCurrentScore();

        // 속도 증가 후 1라인 클리어
        gameManager.linesCleared(1);
        int addedScore = gameManager.getCurrentScore() - scoreBeforeNextLine;

        // 속도 레벨에 따라 점수가 배율 적용되어야 함
        int currentSpeedLevel = gameManager.getSpeedLevel();
        assertEquals(100 * currentSpeedLevel, addedScore, "속도 레벨 " + currentSpeedLevel + "에서 1라인 점수");
    }

    @Test
    @DisplayName("READY 상태에서 togglePause를 호출해도 상태가 변하지 않는지 확인")
    void testTogglePauseInReadyState() {
        // given - READY 상태
        assertEquals(GameState.READY, gameManager.getState());

        // when
        gameManager.togglePause();

        // then - 상태 변화 없음
        assertEquals(GameState.READY, gameManager.getState(), "READY 상태에서는 togglePause가 동작하지 않아야 함");
    }

    @Test
    @DisplayName("GAME_OVER 상태에서 togglePause를 호출해도 상태가 변하지 않는지 확인")
    void testTogglePauseInGameOverState() {
        // given - GAME_OVER 상태
        gameManager.startGame();
        gameManager.endGame(false);
        assertEquals(GameState.GAME_OVER, gameManager.getState());

        // when
        gameManager.togglePause();

        // then - 상태 변화 없음
        assertEquals(GameState.GAME_OVER, gameManager.getState(), "GAME_OVER 상태에서는 togglePause가 동작하지 않아야 함");
    }

    @Test
    @DisplayName("0라인 클리어 시 점수가 추가되지 않는지 확인")
    void testZeroLinesCleared() {
        // given
        gameManager.startGame();
        int initialScore = gameManager.getCurrentScore();

        // when
        gameManager.linesCleared(0);

        // then
        assertEquals(initialScore, gameManager.getCurrentScore(), "0라인 클리어 시 점수 변화 없음");
        assertEquals(0, gameManager.getLinesCleared(), "라인 수도 변화 없음");
    }

    @Test
    @DisplayName("5라인 이상 클리어 시 점수가 추가되지 않는지 확인 (유효하지 않은 입력)")
    void testInvalidLinesCleared() {
        // given
        gameManager.startGame();
        int initialScore = gameManager.getCurrentScore();

        // when
        gameManager.linesCleared(5); // 테트리스는 최대 4라인

        // then
        assertEquals(initialScore, gameManager.getCurrentScore(), "5라인 이상은 유효하지 않으므로 점수 변화 없음");
    }
}
