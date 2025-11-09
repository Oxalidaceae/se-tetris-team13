package team13.tetris.scenes;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;

import static org.junit.jupiter.api.Assertions.*;

// DifficultySelectionScene 테스트: Tests scene 생성, button 기능 및 내비게이션 확인
@DisplayName("DifficultySelectionScene 테스트")
public class DifficultySelectionSceneTest {

    private SceneManager sceneManager;
    private Settings settings;
    private DifficultySelectionScene difficultyScene;

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
            difficultyScene = new DifficultySelectionScene(sceneManager, settings);
        });

        // JavaFX 스레드 작업 완료 대기
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    @DisplayName("Scene이 정상적으로 생성되는지 확인")
    void testSceneCreation() {
        javafx.application.Platform.runLater(() -> {
            Scene scene = difficultyScene.getScene();

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
            Scene scene = difficultyScene.getScene();
            VBox layout = (VBox) scene.getRoot();

            assertEquals(5, layout.getChildren().size(), "Layout should have 5 children (title + 4 buttons)");
            assertEquals("-fx-alignment: center;", layout.getStyle(), "Layout should be centered");
        });

        waitForFX();
    }

    @Test
    @DisplayName("타이틀 레이블이 올바르게 설정되는지 확인")
    void testTitleLabel() {
        javafx.application.Platform.runLater(() -> {
            Scene scene = difficultyScene.getScene();
            VBox layout = (VBox) scene.getRoot();

            assertTrue(layout.getChildren().get(0) instanceof Label, "First child should be Label");
            Label title = (Label) layout.getChildren().get(0);

            assertEquals("Select Difficulty", title.getText(), "Title text should be 'Select Difficulty'");
            assertTrue(title.getStyleClass().contains("label-title"), "Title should have label-title style class");
        });

        waitForFX();
    }

    @Test
    @DisplayName("Easy 버튼이 올바르게 생성되는지 확인")
    void testEasyButton() {
        javafx.application.Platform.runLater(() -> {
            Scene scene = difficultyScene.getScene();
            VBox layout = (VBox) scene.getRoot();

            assertTrue(layout.getChildren().get(1) instanceof Button, "Second child should be Button");
            Button easyBtn = (Button) layout.getChildren().get(1);

            assertEquals("Easy", easyBtn.getText(), "Easy button text should be 'Easy'");
            assertNotNull(easyBtn.getOnAction(), "Easy button should have action handler");
        });

        waitForFX();
    }

    @Test
    @DisplayName("Normal 버튼이 올바르게 생성되는지 확인")
    void testNormalButton() {
        javafx.application.Platform.runLater(() -> {
            Scene scene = difficultyScene.getScene();
            VBox layout = (VBox) scene.getRoot();

            assertTrue(layout.getChildren().get(2) instanceof Button, "Third child should be Button");
            Button normalBtn = (Button) layout.getChildren().get(2);

            assertEquals("Normal", normalBtn.getText(), "Normal button text should be 'Normal'");
            assertNotNull(normalBtn.getOnAction(), "Normal button should have action handler");
        });

        waitForFX();
    }

    @Test
    @DisplayName("Hard 버튼이 올바르게 생성되는지 확인")
    void testHardButton() {
        javafx.application.Platform.runLater(() -> {
            Scene scene = difficultyScene.getScene();
            VBox layout = (VBox) scene.getRoot();

            assertTrue(layout.getChildren().get(3) instanceof Button, "Fourth child should be Button");
            Button hardBtn = (Button) layout.getChildren().get(3);

            assertEquals("Hard", hardBtn.getText(), "Hard button text should be 'Hard'");
            assertNotNull(hardBtn.getOnAction(), "Hard button should have action handler");
        });

        waitForFX();
    }

    @Test
    @DisplayName("Back 버튼이 올바르게 생성되는지 확인")
    void testBackButton() {
        javafx.application.Platform.runLater(() -> {
            Scene scene = difficultyScene.getScene();
            VBox layout = (VBox) scene.getRoot();

            assertTrue(layout.getChildren().get(4) instanceof Button, "Fifth child should be Button");
            Button backBtn = (Button) layout.getChildren().get(4);

            assertEquals("Back", backBtn.getText(), "Back button text should be 'Back'");
            assertNotNull(backBtn.getOnAction(), "Back button should have action handler");
        });

        waitForFX();
    }

    @Test
    @DisplayName("모든 버튼이 존재하는지 확인")
    void testAllButtonsExist() {
        javafx.application.Platform.runLater(() -> {
            Scene scene = difficultyScene.getScene();
            VBox layout = (VBox) scene.getRoot();

            long buttonCount = layout.getChildren().stream()
                    .filter(node -> node instanceof Button)
                    .count();

            assertEquals(4, buttonCount, "Should have exactly 4 buttons");
        });

        waitForFX();
    }

    @Test
    @DisplayName("모든 버튼에 액션 핸들러가 있는지 확인")
    void testButtonActionHandlers() {
        javafx.application.Platform.runLater(() -> {
            Scene scene = difficultyScene.getScene();
            VBox layout = (VBox) scene.getRoot();

            Button easyBtn = (Button) layout.getChildren().get(1);
            Button normalBtn = (Button) layout.getChildren().get(2);
            Button hardBtn = (Button) layout.getChildren().get(3);
            Button backBtn = (Button) layout.getChildren().get(4);

            assertNotNull(easyBtn.getOnAction(), "Easy button should have action");
            assertNotNull(normalBtn.getOnAction(), "Normal button should have action");
            assertNotNull(hardBtn.getOnAction(), "Hard button should have action");
            assertNotNull(backBtn.getOnAction(), "Back button should have action");
        });

        waitForFX();
    }

    @Test
    @DisplayName("SceneManager와의 통합이 정상적인지 확인")
    void testSceneManagerIntegration() {
        javafx.application.Platform.runLater(() -> {
            assertNotNull(sceneManager, "SceneManager should be initialized");
            assertNotNull(settings, "Settings should be initialized");
            assertNotNull(difficultyScene, "DifficultySelectionScene should be initialized");
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
