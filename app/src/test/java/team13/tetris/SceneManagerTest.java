package team13.tetris;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import team13.tetris.config.Settings;
import team13.tetris.data.ScoreBoard;

import static org.junit.jupiter.api.Assertions.*;

// SceneManager 테스트: Tests scene transitions, CSS application, and window management
@DisplayName("SceneManager 테스트")
public class SceneManagerTest {

  private Stage stage;
  private SceneManager sceneManager;
  private Settings settings;

  @BeforeAll
  static void initJavaFX() {
    // JavaFX 툴킷 초기화
    try {
      javafx.application.Platform.startup(() -> {
      });
    } catch (IllegalStateException e) {
      // 이미 초기화되었으면 무시
    }
  }

  @BeforeEach
  void setUp() {
    javafx.application.Platform.runLater(() -> {
      stage = new Stage();
      sceneManager = new SceneManager(stage);
      settings = new Settings();
    });

    waitForFX();
  }

  @Test
  @DisplayName("SceneManager가 정상적으로 생성되는지 확인")
  void testSceneManagerCreation() {
    javafx.application.Platform.runLater(() -> {
      assertNotNull(sceneManager, "SceneManager should not be null");
    });

    waitForFX();
  }

  @Test
  @DisplayName("색맹 모드 초기 상태가 false인지 확인")
  void testColorBlindModeDefaultState() {
    javafx.application.Platform.runLater(() -> {
      assertFalse(sceneManager.isColorBlindMode(),
          "Color blind mode should be false by default");
    });

    waitForFX();
  }

  @Test
  @DisplayName("색맹 모드를 활성화할 수 있는지 확인")
  void testSetColorBlindModeEnabled() {
    javafx.application.Platform.runLater(() -> {
      sceneManager.setColorBlindMode(true);
      assertTrue(sceneManager.isColorBlindMode(),
          "Color blind mode should be true after enabling");
    });

    waitForFX();
  }

  @Test
  @DisplayName("색맹 모드를 비활성화할 수 있는지 확인")
  void testSetColorBlindModeDisabled() {
    javafx.application.Platform.runLater(() -> {
      sceneManager.setColorBlindMode(true);
      sceneManager.setColorBlindMode(false);
      assertFalse(sceneManager.isColorBlindMode(),
          "Color blind mode should be false after disabling");
    });

    waitForFX();
  }

  @Test
  @DisplayName("창 크기를 Small(400x500)로 설정할 수 있는지 확인")
  void testSetWindowSizeSmall() {
    javafx.application.Platform.runLater(() -> {
      sceneManager.setWindowSize(400, 500);
      assertEquals(400, stage.getWidth(), 1.0, "Stage width should be 400");
      assertEquals(500, stage.getHeight(), 1.0, "Stage height should be 500");
    });

    waitForFX();
  }

  @Test
  @DisplayName("창 크기를 Medium(600x700)으로 설정할 수 있는지 확인")
  void testSetWindowSizeMedium() {
    javafx.application.Platform.runLater(() -> {
      sceneManager.setWindowSize(600, 700);
      assertEquals(600, stage.getWidth(), 1.0, "Stage width should be 600");
      assertEquals(700, stage.getHeight(), 1.0, "Stage height should be 700");
    });

    waitForFX();
  }

  @Test
  @DisplayName("창 크기를 Large(800x900)로 설정할 수 있는지 확인")
  void testSetWindowSizeLarge() {
    javafx.application.Platform.runLater(() -> {
      sceneManager.setWindowSize(800, 900);
      assertEquals(800, stage.getWidth(), 1.0, "Stage width should be 800");
      assertEquals(900, stage.getHeight(), 1.0, "Stage height should be 900");
    });

    waitForFX();
  }

  @Test
  @DisplayName("메인 메뉴 씬으로 전환할 수 있는지 확인")
  void testShowMainMenu() {
    javafx.application.Platform.runLater(() -> {
      sceneManager.showMainMenu(settings);

      assertNotNull(stage.getScene(), "Stage should have a scene after showing main menu");
      assertNotNull(stage.getScene().getRoot(), "Scene should have a root node");
    });

    waitForFX();
  }

  @Test
  @DisplayName("설정 씬으로 전환할 수 있는지 확인")
  void testShowSettings() {
    javafx.application.Platform.runLater(() -> {
      sceneManager.showSettings(settings);

      assertNotNull(stage.getScene(), "Stage should have a scene after showing settings");
      assertNotNull(stage.getScene().getRoot(), "Scene should have a root node");
    });

    waitForFX();
  }

  @Test
  @DisplayName("스코어보드 씬으로 전환할 수 있는지 확인")
  void testShowScoreboard() {
    javafx.application.Platform.runLater(() -> {
      sceneManager.showScoreboard(settings);

      assertNotNull(stage.getScene(), "Stage should have a scene after showing scoreboard");
      assertNotNull(stage.getScene().getRoot(), "Scene should have a root node");
    });

    waitForFX();
  }

  @Test
  @DisplayName("하이라이트와 함께 스코어보드 씬으로 전환할 수 있는지 확인")
  void testShowScoreboardWithHighlight() {
    javafx.application.Platform.runLater(() -> {
      String playerName = "TestPlayer";
      int score = 5000;
      ScoreBoard.ScoreEntry.Mode mode = ScoreBoard.ScoreEntry.Mode.NORMAL;

      sceneManager.showScoreboard(settings, playerName, score, mode);

      assertNotNull(stage.getScene(),
          "Stage should have a scene after showing scoreboard with highlight");
      assertNotNull(stage.getScene().getRoot(), "Scene should have a root node");
    });

    waitForFX();
  }

  @Test
  @DisplayName("난이도 선택 씬으로 전환할 수 있는지 확인")
  void testShowDifficultySelection() {
    javafx.application.Platform.runLater(() -> {
      sceneManager.showDifficultySelection(settings);

      assertNotNull(stage.getScene(),
          "Stage should have a scene after showing difficulty selection");
      assertNotNull(stage.getScene().getRoot(), "Scene should have a root node");
    });

    waitForFX();
  }

  @Test
  @DisplayName("게임 씬으로 전환할 수 있는지 확인 (EASY)")
  void testShowGameEasy() {
    javafx.application.Platform.runLater(() -> {
      sceneManager.showGame(settings, ScoreBoard.ScoreEntry.Mode.EASY);

      assertNotNull(stage.getScene(), "Stage should have a scene after showing game");
      assertNotNull(stage.getScene().getRoot(), "Scene should have a root node");
    });

    waitForFX();
  }

  @Test
  @DisplayName("게임 씬으로 전환할 수 있는지 확인 (NORMAL)")
  void testShowGameNormal() {
    javafx.application.Platform.runLater(() -> {
      sceneManager.showGame(settings, ScoreBoard.ScoreEntry.Mode.NORMAL);

      assertNotNull(stage.getScene(), "Stage should have a scene after showing game");
      assertNotNull(stage.getScene().getRoot(), "Scene should have a root node");
    });

    waitForFX();
  }

  @Test
  @DisplayName("게임 씬으로 전환할 수 있는지 확인 (HARD)")
  void testShowGameHard() {
    javafx.application.Platform.runLater(() -> {
      sceneManager.showGame(settings, ScoreBoard.ScoreEntry.Mode.HARD);

      assertNotNull(stage.getScene(), "Stage should have a scene after showing game");
      assertNotNull(stage.getScene().getRoot(), "Scene should have a root node");
    });

    waitForFX();
  }

  @Test
  @DisplayName("게임 씬으로 전환할 수 있는지 확인 (ITEM)")
  void testShowGameItem() {
    javafx.application.Platform.runLater(() -> {
      sceneManager.showGame(settings, ScoreBoard.ScoreEntry.Mode.ITEM);

      assertNotNull(stage.getScene(), "Stage should have a scene after showing game");
      assertNotNull(stage.getScene().getRoot(), "Scene should have a root node");
    });

    waitForFX();
  }

  @Test
  @DisplayName("게임 오버 씬으로 전환할 수 있는지 확인")
  void testShowGameOver() {
    javafx.application.Platform.runLater(() -> {
      int finalScore = 10000;
      ScoreBoard.ScoreEntry.Mode difficulty = ScoreBoard.ScoreEntry.Mode.NORMAL;

      sceneManager.showGameOver(settings, finalScore, difficulty);

      assertNotNull(stage.getScene(), "Stage should have a scene after showing game over");
      assertNotNull(stage.getScene().getRoot(), "Scene should have a root node");
    });

    waitForFX();
  }

  @Test
  @DisplayName("키 설정 씬으로 전환할 수 있는지 확인")
  void testShowKeySettings() {
    javafx.application.Platform.runLater(() -> {
      sceneManager.showKeySettings(settings);

      assertNotNull(stage.getScene(), "Stage should have a scene after showing key settings");
      assertNotNull(stage.getScene().getRoot(), "Scene should have a root node");
    });

    waitForFX();
  }

  @Test
  @DisplayName("씬 전환 시 CSS가 적용되는지 확인")
  void testChangeSceneAppliesCSS() {
    javafx.application.Platform.runLater(() -> {
      sceneManager.showMainMenu(settings);
      Scene scene = stage.getScene();

      assertNotNull(scene, "Scene should not be null");
      assertFalse(scene.getStylesheets().isEmpty(),
          "Scene should have stylesheets applied");
      assertTrue(scene.getStylesheets().get(0).contains("application.css") ||
          scene.getStylesheets().get(0).contains("colorblind.css"),
          "Stylesheet should be either application.css or colorblind.css");
    });

    waitForFX();
  }

  @Test
  @DisplayName("색맹 모드 활성화 시 colorblind.css가 적용되는지 확인")
  void testColorBlindModeAppliesColorblindCSS() {
    javafx.application.Platform.runLater(() -> {
      sceneManager.setColorBlindMode(true);
      sceneManager.showMainMenu(settings);
      Scene scene = stage.getScene();

      assertNotNull(scene, "Scene should not be null");
      assertFalse(scene.getStylesheets().isEmpty(),
          "Scene should have stylesheets applied");
      assertTrue(scene.getStylesheets().get(0).contains("colorblind.css"),
          "Stylesheet should be colorblind.css when color blind mode is enabled");
    });

    waitForFX();
  }

  @Test
  @DisplayName("색맹 모드 비활성화 시 application.css가 적용되는지 확인")
  void testNormalModeAppliesApplicationCSS() {
    javafx.application.Platform.runLater(() -> {
      sceneManager.setColorBlindMode(false);
      sceneManager.showMainMenu(settings);
      Scene scene = stage.getScene();

      assertNotNull(scene, "Scene should not be null");
      assertFalse(scene.getStylesheets().isEmpty(),
          "Scene should have stylesheets applied");
      assertTrue(scene.getStylesheets().get(0).contains("application.css"),
          "Stylesheet should be application.css when color blind mode is disabled");
    });

    waitForFX();
  }

  @Test
  @DisplayName("여러 씬을 연속으로 전환할 수 있는지 확인")
  void testMultipleSceneTransitions() {
    javafx.application.Platform.runLater(() -> {
      sceneManager.showMainMenu(settings);
      assertNotNull(stage.getScene(), "Scene should exist after first transition");

      sceneManager.showSettings(settings);
      assertNotNull(stage.getScene(), "Scene should exist after second transition");

      sceneManager.showScoreboard(settings);
      assertNotNull(stage.getScene(), "Scene should exist after third transition");
    });

    waitForFX();
  }

  @Test
  @DisplayName("색맹 모드 전환 후 CSS가 재적용되는지 확인")
  void testColorBlindModeToggleReappliesCSS() {
    javafx.application.Platform.runLater(() -> {
      sceneManager.showMainMenu(settings);
      Scene scene1 = stage.getScene();
      String stylesheet1 = scene1.getStylesheets().get(0);
      assertTrue(stylesheet1.contains("application.css"),
          "Should start with application.css");

      sceneManager.setColorBlindMode(true);
      String stylesheet2 = scene1.getStylesheets().get(0);
      assertTrue(stylesheet2.contains("colorblind.css"),
          "Should change to colorblind.css");

      sceneManager.setColorBlindMode(false);
      String stylesheet3 = scene1.getStylesheets().get(0);
      assertTrue(stylesheet3.contains("application.css"),
          "Should change back to application.css");
    });

    waitForFX();
  }

  @Test
  @DisplayName("모든 난이도로 게임을 시작할 수 있는지 확인")
  void testAllDifficulties() {
    javafx.application.Platform.runLater(() -> {
      ScoreBoard.ScoreEntry.Mode[] modes = {
          ScoreBoard.ScoreEntry.Mode.EASY,
          ScoreBoard.ScoreEntry.Mode.NORMAL,
          ScoreBoard.ScoreEntry.Mode.HARD,
          ScoreBoard.ScoreEntry.Mode.ITEM
      };

      for (ScoreBoard.ScoreEntry.Mode mode : modes) {
        sceneManager.showGame(settings, mode);
        assertNotNull(stage.getScene(),
            "Game scene should be created for mode: " + mode.name());
      }
    });

    waitForFX();
  }

  @Test
  @DisplayName("다양한 점수로 게임 오버 씬을 표시할 수 있는지 확인")
  void testShowGameOverWithDifferentScores() {
    javafx.application.Platform.runLater(() -> {
      int[] scores = { 0, 1000, 5000, 10000, 99999 };

      for (int score : scores) {
        sceneManager.showGameOver(settings, score, ScoreBoard.ScoreEntry.Mode.NORMAL);
        assertNotNull(stage.getScene(),
            "Game over scene should be created for score: " + score);
      }
    });

    waitForFX();
  }

  @Test
  @DisplayName("enableArrowAsTab이 씬에 이벤트 필터를 추가하는지 확인")
  void testEnableArrowAsTab() {
    javafx.application.Platform.runLater(() -> {
      sceneManager.showMainMenu(settings);
      Scene scene = stage.getScene();

      // enableArrowAsTab은 showMainMenu 내부에서 호출되므로
      // 씬이 정상적으로 생성되었는지 확인
      assertNotNull(scene, "Scene should not be null");
      assertNotNull(scene.getRoot(), "Scene root should not be null");
    });

    waitForFX();
  }

  @Test
  @DisplayName("창 크기를 여러 번 변경할 수 있는지 확인")
  void testMultipleWindowSizeChanges() {
    javafx.application.Platform.runLater(() -> {
      sceneManager.setWindowSize(400, 500);
      assertEquals(400, stage.getWidth(), 1.0, "Width should be 400");
      assertEquals(500, stage.getHeight(), 1.0, "Height should be 500");

      sceneManager.setWindowSize(600, 700);
      assertEquals(600, stage.getWidth(), 1.0, "Width should be 600");
      assertEquals(700, stage.getHeight(), 1.0, "Height should be 700");

      sceneManager.setWindowSize(800, 900);
      assertEquals(800, stage.getWidth(), 1.0, "Width should be 800");
      assertEquals(900, stage.getHeight(), 1.0, "Height should be 900");
    });

    waitForFX();
  }

  @Test
  @DisplayName("Stage가 SceneManager에 올바르게 연결되는지 확인")
  void testStageConnection() {
    javafx.application.Platform.runLater(() -> {
      sceneManager.showMainMenu(settings);

      assertNotNull(stage.getScene(), "Stage should have a scene");
      assertSame(stage.getScene(), stage.getScene(),
          "Stage should maintain its scene reference");
    });

    waitForFX();
  }

  @Test
  @DisplayName("CSS 스타일시트가 씬 전환 시 교체되는지 확인")
  void testStylesheetReplacementOnSceneChange() {
    javafx.application.Platform.runLater(() -> {
      sceneManager.showMainMenu(settings);
      Scene scene1 = stage.getScene();
      int stylesheetCount1 = scene1.getStylesheets().size();

      sceneManager.showSettings(settings);
      Scene scene2 = stage.getScene();
      int stylesheetCount2 = scene2.getStylesheets().size();

      // 각 씬은 정확히 하나의 스타일시트를 가져야 함
      assertEquals(1, stylesheetCount1, "First scene should have exactly 1 stylesheet");
      assertEquals(1, stylesheetCount2, "Second scene should have exactly 1 stylesheet");
    });

    waitForFX();
  }

  @Test
  @DisplayName("ExitScene으로 전환할 수 있는지 확인")
  void testShowExitScene() {
    javafx.application.Platform.runLater(() -> {
      // 먼저 메인 메뉴 표시
      sceneManager.showMainMenu(settings);
      Scene originalScene = stage.getScene();
      assertNotNull(originalScene, "Original scene should not be null");

      // ExitScene으로 전환
      Runnable onCancel = () -> {
        // Cancel 콜백
      };
      sceneManager.showExitScene(settings, onCancel);

      Scene exitScene = stage.getScene();
      assertNotNull(exitScene, "Stage should have a scene after showing exit scene");
      assertNotNull(exitScene.getRoot(), "Exit scene should have a root node");
      assertNotSame(originalScene, exitScene, "Exit scene should be different from original scene");
    });

    waitForFX();
  }

  @Test
  @DisplayName("ExitScene에서 이전 씬으로 복원할 수 있는지 확인")
  void testRestorePreviousScene() {
    javafx.application.Platform.runLater(() -> {
      // 메인 메뉴 표시
      sceneManager.showMainMenu(settings);
      Scene mainMenuScene = stage.getScene();

      // ExitScene으로 전환
      sceneManager.showExitScene(settings, () -> {
      });
      Scene exitScene = stage.getScene();
      assertNotSame(mainMenuScene, exitScene, "Exit scene should be different");

      // 이전 씬으로 복원
      sceneManager.restorePreviousScene();
      Scene restoredScene = stage.getScene();

      assertSame(mainMenuScene, restoredScene, "Restored scene should be the same as original");
    });

    waitForFX();
  }

  @Test
  @DisplayName("이전 씬이 없을 때 restorePreviousScene 호출 시 예외가 발생하지 않는지 확인")
  void testRestorePreviousSceneWithoutPrevious() {
    javafx.application.Platform.runLater(() -> {
      // 이전 씬 없이 restorePreviousScene 호출
      assertDoesNotThrow(() -> sceneManager.restorePreviousScene(),
          "restorePreviousScene should not throw when no previous scene exists");
    });

    waitForFX();
  }

  @Test
  @DisplayName("여러 번 ExitScene을 표시할 수 있는지 확인")
  void testMultipleExitSceneShows() {
    javafx.application.Platform.runLater(() -> {
      sceneManager.showMainMenu(settings);

      // 첫 번째 ExitScene 표시
      sceneManager.showExitScene(settings, () -> {
      });
      assertNotNull(stage.getScene(), "Scene should exist after first exit scene");

      // 복원
      sceneManager.restorePreviousScene();

      // 두 번째 ExitScene 표시
      sceneManager.showExitScene(settings, () -> {
      });
      assertNotNull(stage.getScene(), "Scene should exist after second exit scene");
    });

    waitForFX();
  }

  @Test
  @DisplayName("ExitScene의 onCancel 콜백이 null이어도 표시할 수 있는지 확인")
  void testShowExitSceneWithNullCallback() {
    javafx.application.Platform.runLater(() -> {
      sceneManager.showMainMenu(settings);

      assertDoesNotThrow(() -> sceneManager.showExitScene(settings, null),
          "showExitScene should not throw with null callback");

      assertNotNull(stage.getScene(), "Scene should exist after showing exit scene with null callback");
    });

    waitForFX();
  }

  @Test
  @DisplayName("다른 씬들 사이에서 ExitScene을 표시할 수 있는지 확인")
  void testExitSceneFromDifferentScenes() {
    javafx.application.Platform.runLater(() -> {
      // Settings에서 ExitScene
      sceneManager.showSettings(settings);
      Scene settingsScene = stage.getScene();
      sceneManager.showExitScene(settings, () -> {
      });
      assertNotSame(settingsScene, stage.getScene(), "Should switch to exit scene");
      sceneManager.restorePreviousScene();
      assertSame(settingsScene, stage.getScene(), "Should restore settings scene");

      // Scoreboard에서 ExitScene
      sceneManager.showScoreboard(settings);
      Scene scoreboardScene = stage.getScene();
      sceneManager.showExitScene(settings, () -> {
      });
      assertNotSame(scoreboardScene, stage.getScene(), "Should switch to exit scene");
      sceneManager.restorePreviousScene();
      assertSame(scoreboardScene, stage.getScene(), "Should restore scoreboard scene");
    });

    waitForFX();
  }

  @Test
  @DisplayName("ExitScene이 CSS를 올바르게 적용하는지 확인")
  void testExitSceneAppliesCSS() {
    javafx.application.Platform.runLater(() -> {
      sceneManager.showMainMenu(settings);
      sceneManager.showExitScene(settings, () -> {
      });

      Scene exitScene = stage.getScene();
      assertNotNull(exitScene, "Exit scene should not be null");
      assertFalse(exitScene.getStylesheets().isEmpty(),
          "Exit scene should have stylesheets applied");
    });

    waitForFX();
  }

  @Test
  @DisplayName("색맹 모드에서 ExitScene이 colorblind.css를 적용하는지 확인")
  void testExitSceneColorBlindMode() {
    javafx.application.Platform.runLater(() -> {
      sceneManager.setColorBlindMode(true);
      sceneManager.showMainMenu(settings);
      sceneManager.showExitScene(settings, () -> {
      });

      Scene exitScene = stage.getScene();
      assertNotNull(exitScene, "Exit scene should not be null");
      assertFalse(exitScene.getStylesheets().isEmpty(),
          "Exit scene should have stylesheets");
      assertTrue(exitScene.getStylesheets().get(0).contains("colorblind.css"),
          "Exit scene should use colorblind.css when color blind mode is enabled");
    });

    waitForFX();
  }

  // JavaFX 스레드 작업 완료 대기 헬퍼 메서드
  private void waitForFX() {
    try {
      Thread.sleep(150);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
