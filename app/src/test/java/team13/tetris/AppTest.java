package team13.tetris;

import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

// App class 테스트: Tests basic application initialization and lifecycle
@DisplayName("App 테스트")
public class AppTest {

  @BeforeAll
  static void initJavaFX() {
    // JavaFX 툴킷 초기화
    try {
      // headless 모드에서는 초기화하지 않음
      if (!Boolean.getBoolean("java.awt.headless")) {
        javafx.application.Platform.startup(() -> {});
      }
    } catch (IllegalStateException e) {
      // 이미 초기화되었으면 무시
    }
  }

  @Test
  @DisplayName("App 인스턴스를 생성할 수 있는지 확인")
  void testAppInstantiation() {
    assertDoesNotThrow(() -> {
      App app = new App();
      assertNotNull(app, "App instance should not be null");
    });
  }

  @Test
  @DisplayName("App의 start 메서드가 Stage를 받아 실행될 수 있는지 확인")
  void testAppStart() {
    javafx.application.Platform.runLater(() -> {
      App app = new App();
      Stage stage = new Stage();

      assertDoesNotThrow(() -> {
        app.start(stage);
      }, "App.start() should not throw exception");

      assertNotNull(stage.getScene(), "Stage should have a scene after start");
      assertEquals("Tetris", stage.getTitle(), "Stage title should be 'Tetris'");
    });

    waitForFX();
  }

  @Test
  @DisplayName("App이 설정을 로드하고 초기 화면을 표시하는지 확인")
  void testAppInitialization() {
    javafx.application.Platform.runLater(() -> {
      App app = new App();
      Stage stage = new Stage();

      app.start(stage);

      assertNotNull(stage.getScene(), "Scene should be initialized");
      assertNotNull(stage.getScene().getRoot(), "Scene should have root node");
      assertFalse(stage.getScene().getStylesheets().isEmpty(),
          "Scene should have stylesheets applied");
    });

    waitForFX();
  }

  @Test
  @DisplayName("App이 창 크기를 올바르게 설정하는지 확인 (기본값)")
  void testDefaultWindowSize() {
    javafx.application.Platform.runLater(() -> {
      App app = new App();
      Stage stage = new Stage();

      app.start(stage);

      // 기본값은 MEDIUM (600x700)
      assertTrue(stage.getWidth() >= 550 && stage.getWidth() <= 650,
          "Default width should be around 600 (MEDIUM)");
      assertTrue(stage.getHeight() >= 650 && stage.getHeight() <= 750,
          "Default height should be around 700 (MEDIUM)");
    });

    waitForFX();
  }

  @Test
  @DisplayName("App이 Stage 타이틀을 설정하는지 확인")
  void testStageTitle() {
    javafx.application.Platform.runLater(() -> {
      App app = new App();
      Stage stage = new Stage();

      app.start(stage);

      assertEquals("Tetris", stage.getTitle(),
          "Stage title should be set to 'Tetris'");
    });

    waitForFX();
  }

  @Test
  @DisplayName("App이 여러 번 생성될 수 있는지 확인")
  void testMultipleAppInstances() {
    assertDoesNotThrow(() -> {
      App app1 = new App();
      App app2 = new App();

      assertNotNull(app1, "First app instance should not be null");
      assertNotNull(app2, "Second app instance should not be null");
      assertNotSame(app1, app2, "Each instance should be different");
    });
  }

  @Test
  @DisplayName("App.start()가 SceneManager를 초기화하는지 확인")
  void testSceneManagerInitialization() {
    javafx.application.Platform.runLater(() -> {
      App app = new App();
      Stage stage = new Stage();

      app.start(stage);

      // SceneManager가 정상 동작하면 Scene이 설정됨
      assertNotNull(stage.getScene(),
          "SceneManager should initialize and set a scene");
      assertNotNull(stage.getScene().getRoot(),
          "Scene should have a root node from SceneManager");
    });

    waitForFX();
  }

  @Test
  @DisplayName("App이 Settings를 로드하는지 확인")
  void testSettingsLoading() {
    javafx.application.Platform.runLater(() -> {
      App app = new App();
      Stage stage = new Stage();

      // Settings 로딩 중 예외가 발생하지 않아야 함
      assertDoesNotThrow(() -> {
        app.start(stage);
      }, "Settings loading should not throw exception");

      assertNotNull(stage.getScene(), "Scene should be created after loading settings");
    });

    waitForFX();
  }

  @Test
  @DisplayName("App이 CSS 스타일시트를 적용하는지 확인")
  void testStylesheetApplication() {
    javafx.application.Platform.runLater(() -> {
      App app = new App();
      Stage stage = new Stage();

      app.start(stage);

      assertNotNull(stage.getScene(), "Scene should exist");
      assertFalse(stage.getScene().getStylesheets().isEmpty(),
          "Stylesheets should be applied to the scene");

      String stylesheet = stage.getScene().getStylesheets().get(0);
      assertTrue(stylesheet.contains("application.css") ||
          stylesheet.contains("colorblind.css"),
          "Stylesheet should be either application.css or colorblind.css");
    });

    waitForFX();
  }

  @Test
  @DisplayName("main 메서드가 존재하는지 확인")
  void testMainMethodExists() {
    assertDoesNotThrow(() -> {
      App.class.getMethod("main", String[].class);
    }, "App should have a main method");
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
