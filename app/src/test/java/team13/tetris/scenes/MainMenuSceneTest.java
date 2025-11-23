package team13.tetris.scenes;

import static org.junit.jupiter.api.Assertions.*;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;

// MainMenuScene 테스트: Tests scene creation, menu buttons, and navigation
@DisplayName("MainMenuScene 테스트")
public class MainMenuSceneTest {

    private SceneManager sceneManager;
    private Settings settings;
    private MainMenuScene mainMenuScene;

    @BeforeAll
    static void initJavaFX() {
        // JavaFX 툴킷 초기화
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // 이미 초기화되었으면 무시
        }
    }

    @BeforeEach
    void setUp() {
        // Mock Stage와 SceneManager 생성
        javafx.application.Platform.runLater(
                () -> {
                    Stage stage = new Stage();
                    settings = new Settings();
                    sceneManager = new SceneManager(stage);
                    mainMenuScene = new MainMenuScene(sceneManager, settings);
                });

        waitForFX();
    }

    @Test
    @DisplayName("Scene이 정상적으로 생성되는지 확인")
    void testSceneCreation() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = mainMenuScene.getScene();

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
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = mainMenuScene.getScene();
                    VBox layout = (VBox) scene.getRoot();

                    assertEquals(
                            6,
                            layout.getChildren().size(),
                            "Layout should have 6 children (title + 5 buttons)");
                    assertEquals(
                            "-fx-alignment: center;",
                            layout.getStyle(),
                            "Layout should be centered");
                });

        waitForFX();
    }

    @Test
    @DisplayName("타이틀이 올바르게 표시되는지 확인")
    void testTitle() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = mainMenuScene.getScene();
                    VBox layout = (VBox) scene.getRoot();

                    assertTrue(
                            layout.getChildren().get(0) instanceof Label,
                            "First child should be Label");
                    Label title = (Label) layout.getChildren().get(0);

                    assertEquals("TETRIS", title.getText(), "Title text should be 'TETRIS'");
                    assertTrue(
                            title.getStyleClass().contains("label-title"),
                            "Title should have label-title style class");
                });

        waitForFX();
    }

    @Test
    @DisplayName("Start Game 버튼이 올바르게 생성되는지 확인")
    void testStartGameButton() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = mainMenuScene.getScene();
                    VBox layout = (VBox) scene.getRoot();

                    assertTrue(
                            layout.getChildren().get(1) instanceof Button,
                            "Second child should be Button");
                    Button startBtn = (Button) layout.getChildren().get(1);

                    assertEquals(
                            "Start Game",
                            startBtn.getText(),
                            "Start button text should be 'Start Game'");
                    assertNotNull(
                            startBtn.getOnAction(), "Start button should have action handler");
                });

        waitForFX();
    }

    @Test
    @DisplayName("Start Item Mode 버튼이 올바르게 생성되는지 확인")
    void testStartItemModeButton() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = mainMenuScene.getScene();
                    VBox layout = (VBox) scene.getRoot();

                    assertTrue(
                            layout.getChildren().get(2) instanceof Button,
                            "Third child should be Button");
                    Button itemModeBtn = (Button) layout.getChildren().get(2);

                    assertEquals(
                            "Start Item Mode",
                            itemModeBtn.getText(),
                            "Item mode button text should be 'Start Item Mode'");
                    assertNotNull(
                            itemModeBtn.getOnAction(),
                            "Item mode button should have action handler");
                });

        waitForFX();
    }

    @Test
    @DisplayName("Options 버튼이 올바르게 생성되는지 확인")
    void testOptionsButton() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = mainMenuScene.getScene();
                    VBox layout = (VBox) scene.getRoot();

                    assertTrue(
                            layout.getChildren().get(3) instanceof Button,
                            "Fourth child should be Button");
                    Button optionBtn = (Button) layout.getChildren().get(3);

                    assertEquals(
                            "Options",
                            optionBtn.getText(),
                            "Options button text should be 'Options'");
                    assertNotNull(
                            optionBtn.getOnAction(), "Options button should have action handler");
                });

        waitForFX();
    }

    @Test
    @DisplayName("Scoreboard 버튼이 올바르게 생성되는지 확인")
    void testScoreboardButton() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = mainMenuScene.getScene();
                    VBox layout = (VBox) scene.getRoot();

                    assertTrue(
                            layout.getChildren().get(4) instanceof Button,
                            "Fifth child should be Button");
                    Button scoreBtn = (Button) layout.getChildren().get(4);

                    assertEquals(
                            "Scoreboard",
                            scoreBtn.getText(),
                            "Scoreboard button text should be 'Scoreboard'");
                    assertNotNull(
                            scoreBtn.getOnAction(), "Scoreboard button should have action handler");
                });

        waitForFX();
    }

    @Test
    @DisplayName("Exit 버튼이 올바르게 생성되는지 확인")
    void testExitButton() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = mainMenuScene.getScene();
                    VBox layout = (VBox) scene.getRoot();

                    assertTrue(
                            layout.getChildren().get(5) instanceof Button,
                            "Sixth child should be Button");
                    Button exitBtn = (Button) layout.getChildren().get(5);

                    assertEquals("Exit", exitBtn.getText(), "Exit button text should be 'Exit'");
                    assertNotNull(exitBtn.getOnAction(), "Exit button should have action handler");
                });

        waitForFX();
    }

    @Test
    @DisplayName("모든 버튼이 존재하는지 확인")
    void testAllButtonsExist() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = mainMenuScene.getScene();
                    VBox layout = (VBox) scene.getRoot();

                    long buttonCount =
                            layout.getChildren().stream()
                                    .filter(node -> node instanceof Button)
                                    .count();

                    assertEquals(5, buttonCount, "Should have exactly 5 buttons");
                });

        waitForFX();
    }

    @Test
    @DisplayName("모든 버튼에 액션 핸들러가 있는지 확인")
    void testAllButtonsHaveActionHandlers() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = mainMenuScene.getScene();
                    VBox layout = (VBox) scene.getRoot();

                    Button startBtn = (Button) layout.getChildren().get(1);
                    Button itemModeBtn = (Button) layout.getChildren().get(2);
                    Button optionBtn = (Button) layout.getChildren().get(3);
                    Button scoreBtn = (Button) layout.getChildren().get(4);
                    Button exitBtn = (Button) layout.getChildren().get(5);

                    assertNotNull(
                            startBtn.getOnAction(), "Start button should have action handler");
                    assertNotNull(
                            itemModeBtn.getOnAction(),
                            "Item mode button should have action handler");
                    assertNotNull(
                            optionBtn.getOnAction(), "Options button should have action handler");
                    assertNotNull(
                            scoreBtn.getOnAction(), "Scoreboard button should have action handler");
                    assertNotNull(exitBtn.getOnAction(), "Exit button should have action handler");
                });

        waitForFX();
    }

    @Test
    @DisplayName("버튼 순서가 올바른지 확인")
    void testButtonOrder() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = mainMenuScene.getScene();
                    VBox layout = (VBox) scene.getRoot();

                    assertEquals(
                            "Start Game",
                            ((Button) layout.getChildren().get(1)).getText(),
                            "First button should be Start Game");
                    assertEquals(
                            "Start Item Mode",
                            ((Button) layout.getChildren().get(2)).getText(),
                            "Second button should be Start Item Mode");
                    assertEquals(
                            "Options",
                            ((Button) layout.getChildren().get(3)).getText(),
                            "Third button should be Options");
                    assertEquals(
                            "Scoreboard",
                            ((Button) layout.getChildren().get(4)).getText(),
                            "Fourth button should be Scoreboard");
                    assertEquals(
                            "Exit",
                            ((Button) layout.getChildren().get(5)).getText(),
                            "Fifth button should be Exit");
                });

        waitForFX();
    }

    @Test
    @DisplayName("SceneManager와의 통합이 정상적인지 확인")
    void testSceneManagerIntegration() {
        javafx.application.Platform.runLater(
                () -> {
                    assertNotNull(sceneManager, "SceneManager should be initialized");
                    assertNotNull(settings, "Settings should be initialized");
                    assertNotNull(mainMenuScene, "MainMenuScene should be initialized");
                });

        waitForFX();
    }

    @Test
    @DisplayName("여러 번 Scene을 생성해도 정상 동작하는지 확인")
    void testMultipleSceneCreation() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene1 = mainMenuScene.getScene();
                    Scene scene2 = mainMenuScene.getScene();

                    assertNotNull(scene1, "First scene should not be null");
                    assertNotNull(scene2, "Second scene should not be null");
                    // 매번 새로운 Scene을 생성
                    assertNotSame(scene1, scene2, "Each call should create a new scene");
                });

        waitForFX();
    }

    @Test
    @DisplayName("레이아웃 간격이 올바르게 설정되는지 확인")
    void testLayoutSpacing() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = mainMenuScene.getScene();
                    VBox layout = (VBox) scene.getRoot();

                    assertEquals(10, layout.getSpacing(), 0.1, "Layout spacing should be 10");
                });

        waitForFX();
    }

    @Test
    @DisplayName("모든 자식 요소가 올바른 타입인지 확인")
    void testChildTypes() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = mainMenuScene.getScene();
                    VBox layout = (VBox) scene.getRoot();

                    assertTrue(
                            layout.getChildren().get(0) instanceof Label,
                            "First child should be Label");

                    for (int i = 1; i <= 5; i++)
                        assertTrue(
                                layout.getChildren().get(i) instanceof Button,
                                "Child " + i + " should be Button");
                });

        waitForFX();
    }

    @Test
    @DisplayName("Settings 객체가 올바르게 주입되는지 확인")
    void testSettingsInjection() {
        javafx.application.Platform.runLater(
                () -> {
                    assertNotNull(settings, "Settings should not be null");
                    // Settings의 기본 속성들이 초기화되어 있는지 확인
                    assertNotNull(
                            settings.getKeyLeft(), "Settings should have key left configured");
                    assertNotNull(
                            settings.getKeyRight(), "Settings should have key right configured");
                });

        waitForFX();
    }

    // JavaFX 스레드 작업 완료 대기 헬퍼 메서드
    private void waitForFX() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
