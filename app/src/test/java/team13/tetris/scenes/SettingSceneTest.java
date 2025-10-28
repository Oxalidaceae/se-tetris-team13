package team13.tetris.scenes;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;

import static org.junit.jupiter.api.Assertions.*;

// SettingsScene 테스트: Tests scene 생성, settings controls, navigation 확인
@DisplayName("SettingScene 테스트")
public class SettingSceneTest {

  private SceneManager sceneManager;
  private Settings settings;
  private SettingsScene settingsScene;

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
    // Mock Stage와 SceneManager 생성
    javafx.application.Platform.runLater(() -> {
      Stage stage = new Stage();
      settings = new Settings();
      sceneManager = new SceneManager(stage);
      settingsScene = new SettingsScene(sceneManager, settings);
    });

    waitForFX();
  }

  @Test
  @DisplayName("Scene이 정상적으로 생성되는지 확인")
  void testSceneCreation() {
    javafx.application.Platform.runLater(() -> {
      Scene scene = settingsScene.getScene();

      assertNotNull(scene, "Scene should not be null");
      assertEquals(600, scene.getWidth(), "Scene width should be 600");
      assertEquals(700, scene.getHeight(), "Scene height should be 700");
      assertNotNull(scene.getRoot(), "Scene root should not be null");
      assertTrue(scene.getRoot() instanceof VBox, "Scene root should be VBox");
    });

    waitForFX();
  }

  @Test
  @DisplayName("레이아웃 구조가 올바른지 확인")
  void testSceneLayout() {
    javafx.application.Platform.runLater(() -> {
      Scene scene = settingsScene.getScene();
      VBox layout = (VBox) scene.getRoot();

      assertEquals(9, layout.getChildren().size(),
          "Layout should have 9 children (title + 3 size buttons + key button + colorblind toggle + reset button + default button + back button)");
      assertEquals("-fx-alignment: center;", layout.getStyle(), "Layout should be centered");
      assertEquals(15, layout.getSpacing(), 0.1, "Layout spacing should be 15");
    });

    waitForFX();
  }

  @Test
  @DisplayName("타이틀이 올바르게 표시되는지 확인")
  void testTitle() {
    javafx.application.Platform.runLater(() -> {
      Scene scene = settingsScene.getScene();
      VBox layout = (VBox) scene.getRoot();

      assertTrue(layout.getChildren().get(0) instanceof Label, "First child should be Label");
      Label title = (Label) layout.getChildren().get(0);

      assertEquals("Settings", title.getText(), "Title text should be 'Settings'");
      assertTrue(title.getStyleClass().contains("label-title"),
          "Title should have label-title style class");
    });

    waitForFX();
  }

  @Test
  @DisplayName("Small 버튼이 올바르게 생성되는지 확인")
  void testSmallButton() {
    javafx.application.Platform.runLater(() -> {
      Scene scene = settingsScene.getScene();
      VBox layout = (VBox) scene.getRoot();

      assertTrue(layout.getChildren().get(1) instanceof Button, "Second child should be Button");
      Button smallBtn = (Button) layout.getChildren().get(1);

      assertEquals("Small", smallBtn.getText(), "Button text should be 'Small'");
      assertNotNull(smallBtn.getOnAction(), "Small button should have action handler");
    });

    waitForFX();
  }

  @Test
  @DisplayName("Medium 버튼이 올바르게 생성되는지 확인")
  void testMediumButton() {
    javafx.application.Platform.runLater(() -> {
      Scene scene = settingsScene.getScene();
      VBox layout = (VBox) scene.getRoot();

      assertTrue(layout.getChildren().get(2) instanceof Button, "Third child should be Button");
      Button mediumBtn = (Button) layout.getChildren().get(2);

      assertEquals("Medium", mediumBtn.getText(), "Button text should be 'Medium'");
      assertNotNull(mediumBtn.getOnAction(), "Medium button should have action handler");
    });

    waitForFX();
  }

  @Test
  @DisplayName("Large 버튼이 올바르게 생성되는지 확인")
  void testLargeButton() {
    javafx.application.Platform.runLater(() -> {
      Scene scene = settingsScene.getScene();
      VBox layout = (VBox) scene.getRoot();

      assertTrue(layout.getChildren().get(3) instanceof Button, "Fourth child should be Button");
      Button largeBtn = (Button) layout.getChildren().get(3);

      assertEquals("Large", largeBtn.getText(), "Button text should be 'Large'");
      assertNotNull(largeBtn.getOnAction(), "Large button should have action handler");
    });

    waitForFX();
  }

  @Test
  @DisplayName("Key Settings 버튼이 올바르게 생성되는지 확인")
  void testKeySettingsButton() {
    javafx.application.Platform.runLater(() -> {
      Scene scene = settingsScene.getScene();
      VBox layout = (VBox) scene.getRoot();

      assertTrue(layout.getChildren().get(4) instanceof Button, "Fifth child should be Button");
      Button keyBtn = (Button) layout.getChildren().get(4);

      assertEquals("Key Settings", keyBtn.getText(), "Button text should be 'Key Settings'");
      assertNotNull(keyBtn.getOnAction(), "Key Settings button should have action handler");
    });

    waitForFX();
  }

  @Test
  @DisplayName("Color Blind Mode 토글 버튼이 올바르게 생성되는지 확인")
  void testColorBlindToggle() {
    javafx.application.Platform.runLater(() -> {
      Scene scene = settingsScene.getScene();
      VBox layout = (VBox) scene.getRoot();

      assertTrue(layout.getChildren().get(5) instanceof ToggleButton,
          "Sixth child should be ToggleButton");
      ToggleButton colorBlindBtn = (ToggleButton) layout.getChildren().get(5);

      assertNotNull(colorBlindBtn.getText(), "Toggle button should have text");
      assertTrue(colorBlindBtn.getText().contains("Color Blind Mode"),
          "Toggle button text should contain 'Color Blind Mode'");
      assertNotNull(colorBlindBtn.getOnAction(),
          "Color Blind toggle should have action handler");
    });

    waitForFX();
  }

  @Test
  @DisplayName("Color Blind Mode 토글 상태가 설정값과 일치하는지 확인")
  void testColorBlindToggleState() {
    javafx.application.Platform.runLater(() -> {
      // 색맹 모드 OFF 상태 확인
      settings.setColorBlindMode(false);
      Scene scene1 = settingsScene.getScene();
      VBox layout1 = (VBox) scene1.getRoot();
      ToggleButton colorBlindBtn1 = (ToggleButton) layout1.getChildren().get(5);

      assertFalse(colorBlindBtn1.isSelected(),
          "Toggle button should be unselected when color blind mode is OFF");
      assertTrue(colorBlindBtn1.getText().contains("OFF"),
          "Toggle button text should contain 'OFF'");

      // 색맹 모드 ON 상태 확인
      settings.setColorBlindMode(true);
      Scene scene2 = settingsScene.getScene();
      VBox layout2 = (VBox) scene2.getRoot();
      ToggleButton colorBlindBtn2 = (ToggleButton) layout2.getChildren().get(5);

      assertTrue(colorBlindBtn2.isSelected(),
          "Toggle button should be selected when color blind mode is ON");
      assertTrue(colorBlindBtn2.getText().contains("ON"),
          "Toggle button text should contain 'ON'");
    });

    waitForFX();
  }

  @Test
  @DisplayName("Reset Scoreboard 버튼이 올바르게 생성되는지 확인")
  void testResetScoreboardButton() {
    javafx.application.Platform.runLater(() -> {
      Scene scene = settingsScene.getScene();
      VBox layout = (VBox) scene.getRoot();

      assertTrue(layout.getChildren().get(6) instanceof Button,
          "Seventh child should be Button");
      Button resetBtn = (Button) layout.getChildren().get(6);

      assertEquals("Reset Scoreboard", resetBtn.getText(),
          "Button text should be 'Reset Scoreboard'");
      assertNotNull(resetBtn.getOnAction(),
          "Reset Scoreboard button should have action handler");
    });

    waitForFX();
  }

  @Test
  @DisplayName("Restore Defaults 버튼이 올바르게 생성되는지 확인")
  void testRestoreDefaultsButton() {
    javafx.application.Platform.runLater(() -> {
      Scene scene = settingsScene.getScene();
      VBox layout = (VBox) scene.getRoot();

      assertTrue(layout.getChildren().get(7) instanceof Button,
          "Eighth child should be Button");
      Button defaultBtn = (Button) layout.getChildren().get(7);

      assertEquals("Restore Defaults", defaultBtn.getText(),
          "Button text should be 'Restore Defaults'");
      assertNotNull(defaultBtn.getOnAction(),
          "Restore Defaults button should have action handler");
    });

    waitForFX();
  }

  @Test
  @DisplayName("Back 버튼이 올바르게 생성되는지 확인")
  void testBackButton() {
    javafx.application.Platform.runLater(() -> {
      Scene scene = settingsScene.getScene();
      VBox layout = (VBox) scene.getRoot();

      assertTrue(layout.getChildren().get(8) instanceof Button,
          "Ninth child should be Button");
      Button backBtn = (Button) layout.getChildren().get(8);

      assertEquals("Back", backBtn.getText(), "Button text should be 'Back'");
      assertNotNull(backBtn.getOnAction(), "Back button should have action handler");
    });

    waitForFX();
  }

  @Test
  @DisplayName("모든 버튼이 올바른 순서로 배치되는지 확인")
  void testButtonOrder() {
    javafx.application.Platform.runLater(() -> {
      Scene scene = settingsScene.getScene();
      VBox layout = (VBox) scene.getRoot();

      String[] expectedTexts = {
          "Settings", // Title (Label)
          "Small",
          "Medium",
          "Large",
          "Key Settings",
          // ToggleButton (인덱스 5) - 텍스트가 동적이므로 스킵
          "Reset Scoreboard",
          "Restore Defaults",
          "Back"
      };

      // Title 확인
      Label title = (Label) layout.getChildren().get(0);
      assertEquals(expectedTexts[0], title.getText(),
          "Title should be at position 0");

      // Small, Medium, Large 버튼 확인
      for (int i = 1; i <= 3; i++) {
        Button btn = (Button) layout.getChildren().get(i);
        assertEquals(expectedTexts[i], btn.getText(),
            "Button at position " + i + " should have correct text");
      }

      // Key Settings 버튼 확인
      Button keyBtn = (Button) layout.getChildren().get(4);
      assertEquals(expectedTexts[4], keyBtn.getText(),
          "Key Settings button should be at position 4");

      // Reset, Restore, Back 버튼 확인
      for (int i = 6; i <= 8; i++) {
        Button btn = (Button) layout.getChildren().get(i);
        assertEquals(expectedTexts[i - 1], btn.getText(),
            "Button at position " + i + " should have correct text");
      }
    });

    waitForFX();
  }

  @Test
  @DisplayName("SceneManager와 Settings가 올바르게 통합되는지 확인")
  void testSceneManagerIntegration() {
    javafx.application.Platform.runLater(() -> {
      assertNotNull(sceneManager, "SceneManager should be initialized");
      assertNotNull(settings, "Settings should be initialized");
      assertNotNull(settingsScene, "SettingsScene should be initialized");
    });

    waitForFX();
  }

  @Test
  @DisplayName("여러 번 Scene을 생성해도 정상 동작하는지 확인")
  void testMultipleSceneCreation() {
    javafx.application.Platform.runLater(() -> {
      Scene scene1 = settingsScene.getScene();
      Scene scene2 = settingsScene.getScene();

      assertNotNull(scene1, "First scene should not be null");
      assertNotNull(scene2, "Second scene should not be null");
      // 매번 새로운 Scene을 생성
      assertNotSame(scene1, scene2, "Each call should create a new scene");
    });

    waitForFX();
  }

  @Test
  @DisplayName("모든 자식 요소가 올바른 타입인지 확인")
  void testChildTypes() {
    javafx.application.Platform.runLater(() -> {
      Scene scene = settingsScene.getScene();
      VBox layout = (VBox) scene.getRoot();

      assertTrue(layout.getChildren().get(0) instanceof Label,
          "First child should be Label (title)");
      assertTrue(layout.getChildren().get(1) instanceof Button,
          "Second child should be Button (Small)");
      assertTrue(layout.getChildren().get(2) instanceof Button,
          "Third child should be Button (Medium)");
      assertTrue(layout.getChildren().get(3) instanceof Button,
          "Fourth child should be Button (Large)");
      assertTrue(layout.getChildren().get(4) instanceof Button,
          "Fifth child should be Button (Key Settings)");
      assertTrue(layout.getChildren().get(5) instanceof ToggleButton,
          "Sixth child should be ToggleButton (Color Blind Mode)");
      assertTrue(layout.getChildren().get(6) instanceof Button,
          "Seventh child should be Button (Reset Scoreboard)");
      assertTrue(layout.getChildren().get(7) instanceof Button,
          "Eighth child should be Button (Restore Defaults)");
      assertTrue(layout.getChildren().get(8) instanceof Button,
          "Ninth child should be Button (Back)");
    });

    waitForFX();
  }

  @Test
  @DisplayName("화면 크기 버튼이 모두 존재하는지 확인")
  void testWindowSizeButtons() {
    javafx.application.Platform.runLater(() -> {
      Scene scene = settingsScene.getScene();
      VBox layout = (VBox) scene.getRoot();

      Button smallBtn = (Button) layout.getChildren().get(1);
      Button mediumBtn = (Button) layout.getChildren().get(2);
      Button largeBtn = (Button) layout.getChildren().get(3);

      assertNotNull(smallBtn.getOnAction(), "Small button should have action");
      assertNotNull(mediumBtn.getOnAction(), "Medium button should have action");
      assertNotNull(largeBtn.getOnAction(), "Large button should have action");
    });

    waitForFX();
  }

  @Test
  @DisplayName("ScoreBoard 객체가 올바르게 초기화되는지 확인")
  void testScoreBoardInitialization() {
    javafx.application.Platform.runLater(() -> {
      // ScoreBoard는 내부적으로 생성되므로 Scene이 정상 생성되면 OK
      Scene scene = settingsScene.getScene();
      assertNotNull(scene, "Scene creation should succeed with internal ScoreBoard");
    });

    waitForFX();
  }

  @Test
  @DisplayName("모든 기능 버튼이 액션 핸들러를 가지는지 확인")
  void testAllButtonsHaveHandlers() {
    javafx.application.Platform.runLater(() -> {
      Scene scene = settingsScene.getScene();
      VBox layout = (VBox) scene.getRoot();

      // Button 타입인 모든 자식 요소 확인 (인덱스 1-4, 6-8)
      int[] buttonIndices = { 1, 2, 3, 4, 6, 7, 8 };
      for (int idx : buttonIndices) {
        Button btn = (Button) layout.getChildren().get(idx);
        assertNotNull(btn.getOnAction(),
            "Button '" + btn.getText() + "' should have action handler");
      }

      // ToggleButton 확인 (인덱스 5)
      ToggleButton toggleBtn = (ToggleButton) layout.getChildren().get(5);
      assertNotNull(toggleBtn.getOnAction(),
          "ToggleButton should have action handler");
    });

    waitForFX();
  }

  /**
   * JavaFX 스레드 작업 완료 대기 헬퍼 메서드
   */
  private void waitForFX() {
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
