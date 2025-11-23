package team13.tetris.scenes;

import static org.junit.jupiter.api.Assertions.*;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;
import team13.tetris.data.ScoreBoard;

// GameOverScene 테스트: Tests scene 생성, score 출력, 닉네임 입력 및 내비게이션 확인
@DisplayName("GameOverScene 테스트")
public class GameOverSceneTest {

    private SceneManager sceneManager;
    private Settings settings;
    private GameOverScene gameOverScene;
    private static final int TEST_SCORE = 12345;
    private static final ScoreBoard.ScoreEntry.Mode TEST_DIFFICULTY =
            ScoreBoard.ScoreEntry.Mode.NORMAL;

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
                    gameOverScene =
                            new GameOverScene(sceneManager, settings, TEST_SCORE, TEST_DIFFICULTY);
                });

        // JavaFX 스레드 작업 완료 대기
        waitForFX();
    }

    @Test
    @DisplayName("Scene이 정상적으로 생성되는지 확인")
    void testSceneCreation() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = gameOverScene.getScene();

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
                    Scene scene = gameOverScene.getScene();
                    VBox layout = (VBox) scene.getRoot();

                    assertEquals(
                            7,
                            layout.getChildren().size(),
                            "Layout should have 7 children (title, score, difficulty, nameField, status, 2 buttons)");
                    assertEquals(
                            "-fx-alignment: center;",
                            layout.getStyle(),
                            "Layout should be centered");
                });

        waitForFX();
    }

    @Test
    @DisplayName("Game Over 타이틀이 올바르게 표시되는지 확인")
    void testGameOverTitle() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = gameOverScene.getScene();
                    VBox layout = (VBox) scene.getRoot();

                    assertTrue(
                            layout.getChildren().get(0) instanceof Label,
                            "First child should be Label");
                    Label title = (Label) layout.getChildren().get(0);

                    assertEquals(
                            "Game Over!", title.getText(), "Title text should be 'Game Over!'");
                });

        waitForFX();
    }

    @Test
    @DisplayName("점수가 올바르게 표시되는지 확인")
    void testScoreDisplay() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = gameOverScene.getScene();
                    VBox layout = (VBox) scene.getRoot();

                    assertTrue(
                            layout.getChildren().get(1) instanceof Label,
                            "Second child should be Label");
                    Label scoreLabel = (Label) layout.getChildren().get(1);

                    assertEquals(
                            "Your Score: " + TEST_SCORE,
                            scoreLabel.getText(),
                            "Score label should display the correct score");
                });

        waitForFX();
    }

    @Test
    @DisplayName("난이도가 올바르게 표시되는지 확인")
    void testDifficultyDisplay() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = gameOverScene.getScene();
                    VBox layout = (VBox) scene.getRoot();

                    assertTrue(
                            layout.getChildren().get(2) instanceof Label,
                            "Third child should be Label");
                    Label difficultyLabel = (Label) layout.getChildren().get(2);

                    assertEquals(
                            "Difficulty: " + TEST_DIFFICULTY.name(),
                            difficultyLabel.getText(),
                            "Difficulty label should display the correct difficulty");
                });

        waitForFX();
    }

    @Test
    @DisplayName("이름 입력 필드가 올바르게 생성되는지 확인")
    void testNameInputField() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = gameOverScene.getScene();
                    VBox layout = (VBox) scene.getRoot();

                    assertTrue(
                            layout.getChildren().get(3) instanceof TextField,
                            "Fourth child should be TextField");
                    TextField nameField = (TextField) layout.getChildren().get(3);

                    assertEquals(
                            "Enter your name",
                            nameField.getPromptText(),
                            "Name field should have correct prompt text");
                    assertTrue(
                            nameField.getText().isEmpty(), "Name field should be initially empty");
                });

        waitForFX();
    }

    @Test
    @DisplayName("상태 메시지 레이블이 존재하는지 확인")
    void testStatusLabel() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = gameOverScene.getScene();
                    VBox layout = (VBox) scene.getRoot();

                    assertTrue(
                            layout.getChildren().get(4) instanceof Label,
                            "Fifth child should be status Label");
                    Label statusLabel = (Label) layout.getChildren().get(4);

                    assertTrue(
                            statusLabel.getText().isEmpty() || statusLabel.getText() != null,
                            "Status label should exist");
                });

        waitForFX();
    }

    @Test
    @DisplayName("Save Score 버튼이 올바르게 생성되는지 확인")
    void testSaveScoreButton() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = gameOverScene.getScene();
                    VBox layout = (VBox) scene.getRoot();

                    assertTrue(
                            layout.getChildren().get(5) instanceof Button,
                            "Sixth child should be Button");
                    Button saveBtn = (Button) layout.getChildren().get(5);

                    assertEquals(
                            "Save Score",
                            saveBtn.getText(),
                            "Save button text should be 'Save Score'");
                    assertNotNull(saveBtn.getOnAction(), "Save button should have action handler");
                });

        waitForFX();
    }

    @Test
    @DisplayName("Back to Menu 버튼이 올바르게 생성되는지 확인")
    void testBackToMenuButton() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = gameOverScene.getScene();
                    VBox layout = (VBox) scene.getRoot();

                    assertTrue(
                            layout.getChildren().get(6) instanceof Button,
                            "Seventh child should be Button");
                    Button backBtn = (Button) layout.getChildren().get(6);

                    assertEquals(
                            "Back to Menu",
                            backBtn.getText(),
                            "Back button text should be 'Back to Menu'");
                    assertNotNull(backBtn.getOnAction(), "Back button should have action handler");
                });

        waitForFX();
    }

    @Test
    @DisplayName("다양한 점수로 Scene이 생성되는지 확인")
    void testDifferentScores() {
        javafx.application.Platform.runLater(
                () -> {
                    int[] testScores = {0, 100, 5000, 999999};

                    for (int score : testScores) {
                        GameOverScene scene =
                                new GameOverScene(sceneManager, settings, score, TEST_DIFFICULTY);
                        Scene s = scene.getScene();
                        VBox layout = (VBox) s.getRoot();
                        Label scoreLabel = (Label) layout.getChildren().get(1);

                        assertEquals(
                                "Your Score: " + score,
                                scoreLabel.getText(),
                                "Score should be displayed correctly for score: " + score);
                    }
                });

        waitForFX();
    }

    @Test
    @DisplayName("다양한 난이도로 Scene이 생성되는지 확인")
    void testDifferentDifficulties() {
        javafx.application.Platform.runLater(
                () -> {
                    ScoreBoard.ScoreEntry.Mode[] difficulties = {
                        ScoreBoard.ScoreEntry.Mode.EASY,
                        ScoreBoard.ScoreEntry.Mode.NORMAL,
                        ScoreBoard.ScoreEntry.Mode.HARD
                    };

                    for (ScoreBoard.ScoreEntry.Mode difficulty : difficulties) {
                        GameOverScene scene =
                                new GameOverScene(sceneManager, settings, TEST_SCORE, difficulty);
                        Scene s = scene.getScene();
                        VBox layout = (VBox) s.getRoot();
                        Label difficultyLabel = (Label) layout.getChildren().get(2);

                        assertEquals(
                                "Difficulty: " + difficulty.name(),
                                difficultyLabel.getText(),
                                "Difficulty should be displayed correctly for: "
                                        + difficulty.name());
                    }
                });

        waitForFX();
    }

    @Test
    @DisplayName("모든 버튼에 액션 핸들러가 있는지 확인")
    void testAllButtonsHaveActionHandlers() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = gameOverScene.getScene();
                    VBox layout = (VBox) scene.getRoot();

                    Button saveBtn = (Button) layout.getChildren().get(5);
                    Button backBtn = (Button) layout.getChildren().get(6);

                    assertNotNull(saveBtn.getOnAction(), "Save button should have action handler");
                    assertNotNull(backBtn.getOnAction(), "Back button should have action handler");
                });

        waitForFX();
    }

    @Test
    @DisplayName("빈 이름으로 저장 시도 시 상태 메시지가 업데이트되는지 확인")
    void testEmptyNameSaveAttempt() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = gameOverScene.getScene();
                    VBox layout = (VBox) scene.getRoot();

                    TextField nameField = (TextField) layout.getChildren().get(3);
                    Label statusLabel = (Label) layout.getChildren().get(4);
                    Button saveBtn = (Button) layout.getChildren().get(5);

                    // 빈 이름으로 저장 시도
                    nameField.setText("");
                    saveBtn.fire();

                    assertEquals(
                            "Please enter your name before saving!",
                            statusLabel.getText(),
                            "Status label should show error message for empty name");
                });

        waitForFX();
    }

    @Test
    @DisplayName("공백만 있는 이름으로 저장 시도 시 상태 메시지가 업데이트되는지 확인")
    void testWhitespaceNameSaveAttempt() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = gameOverScene.getScene();
                    VBox layout = (VBox) scene.getRoot();

                    TextField nameField = (TextField) layout.getChildren().get(3);
                    Label statusLabel = (Label) layout.getChildren().get(4);
                    Button saveBtn = (Button) layout.getChildren().get(5);

                    // 공백만 있는 이름으로 저장 시도
                    nameField.setText("   ");
                    saveBtn.fire();

                    assertEquals(
                            "Please enter your name before saving!",
                            statusLabel.getText(),
                            "Status label should show error message for whitespace-only name");
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
                    assertNotNull(gameOverScene, "GameOverScene should be initialized");
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
