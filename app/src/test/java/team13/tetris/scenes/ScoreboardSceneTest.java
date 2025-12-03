package team13.tetris.scenes;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;
import team13.tetris.data.ScoreBoard;

// ScoreboardScene 테스트: Tests scene 생성, score list 출력, navigation 확인
@DisplayName("ScoreboardScene 테스트")
public class ScoreboardSceneTest {

    private SceneManager sceneManager;
    private Settings settings;
    private ScoreboardScene scoreboardScene;

    @TempDir Path tempDir;

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
                    scoreboardScene = new ScoreboardScene(sceneManager, settings);
                });

        waitForFX();
    }

    @Test
    @DisplayName("Scene이 정상적으로 생성되는지 확인")
    void testSceneCreation() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = scoreboardScene.getScene();

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
                    Scene scene = scoreboardScene.getScene();
                    VBox layout = (VBox) scene.getRoot();

                    assertEquals(
                            3,
                            layout.getChildren().size(),
                            "Layout should have 3 children (title, score list, back button)");
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
                    Scene scene = scoreboardScene.getScene();
                    VBox layout = (VBox) scene.getRoot();

                    assertTrue(
                            layout.getChildren().get(0) instanceof Label,
                            "First child should be Label");
                    Label title = (Label) layout.getChildren().get(0);

                    assertEquals(
                            "Scoreboard", title.getText(), "Title text should be 'Scoreboard'");
                    assertTrue(
                            title.getStyleClass().contains("label-title"),
                            "Title should have label-title style class");
                });

        waitForFX();
    }

    @Test
    @DisplayName("점수 리스트가 생성되는지 확인")
    void testScoreListExists() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = scoreboardScene.getScene();
                    VBox layout = (VBox) scene.getRoot();

                    assertTrue(
                            layout.getChildren().get(1) instanceof ListView,
                            "Second child should be ListView");
                    @SuppressWarnings("unchecked")
                    ListView<String> scoreList = (ListView<String>) layout.getChildren().get(1);

                    assertNotNull(scoreList, "Score list should not be null");
                    assertEquals(
                            300,
                            scoreList.getMaxHeight(),
                            0.1,
                            "Score list max height should be 300");
                });

        waitForFX();
    }

    @Test
    @DisplayName("Back 버튼이 올바르게 생성되는지 확인")
    void testBackButton() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = scoreboardScene.getScene();
                    VBox layout = (VBox) scene.getRoot();

                    assertTrue(
                            layout.getChildren().get(2) instanceof Button,
                            "Third child should be Button");
                    Button backBtn = (Button) layout.getChildren().get(2);

                    assertEquals(
                            "Back to Main Menu",
                            backBtn.getText(),
                            "Back button text should be 'Back to Main Menu'");
                    assertNotNull(backBtn.getOnAction(), "Back button should have action handler");
                });

        waitForFX();
    }

    @Test
    @DisplayName("하이라이트 없이 생성자를 호출할 수 있는지 확인")
    void testConstructorWithoutHighlight() {
        javafx.application.Platform.runLater(
                () -> {
                    ScoreboardScene scene = new ScoreboardScene(sceneManager, settings);
                    assertNotNull(
                            scene,
                            "ScoreboardScene should be created without highlight parameters");
                    assertNotNull(scene.getScene(), "Scene should be created");
                });

        waitForFX();
    }

    @Test
    @DisplayName("하이라이트와 함께 생성자를 호출할 수 있는지 확인")
    void testConstructorWithHighlight() {
        javafx.application.Platform.runLater(
                () -> {
                    String testName = "TestPlayer";
                    Integer testScore = 5000;
                    ScoreBoard.ScoreEntry.Mode testMode = ScoreBoard.ScoreEntry.Mode.NORMAL;

                    ScoreboardScene scene =
                            new ScoreboardScene(
                                    sceneManager, settings, testName, testScore, testMode);

                    assertNotNull(
                            scene, "ScoreboardScene should be created with highlight parameters");
                    assertNotNull(scene.getScene(), "Scene should be created");
                });

        waitForFX();
    }

    @Test
    @DisplayName("점수 리스트 형식이 올바른지 확인")
    void testScoreListFormat() {
        javafx.application.Platform.runLater(
                () -> {
                    // 임시 디렉토리 내 파일로 ScoreBoard 생성 (테스트 후 자동 삭제)
                    String testFilePath = tempDir.resolve("test_scores.txt").toString();
                    ScoreBoard testBoard = new ScoreBoard(testFilePath);
                    testBoard.addScore("Player1", 1000, ScoreBoard.ScoreEntry.Mode.EASY);
                    testBoard.addScore("Player2", 2000, ScoreBoard.ScoreEntry.Mode.NORMAL);

                    Scene scene = scoreboardScene.getScene();
                    VBox layout = (VBox) scene.getRoot();
                    @SuppressWarnings("unchecked")
                    ListView<String> scoreList = (ListView<String>) layout.getChildren().get(1);

                    // 리스트가 초기화되었는지 확인
                    assertNotNull(scoreList.getItems(), "Score list items should not be null");

                    // 항목이 있다면 형식이 올바른지 확인
                    if (scoreList.getItems().size() > 0) {
                        String firstItem = scoreList.getItems().get(0);
                        assertTrue(
                                firstItem.matches("\\[\\w+\\] .+ : \\d+"),
                                "Score list item should match format '[MODE] Name : Score'");
                    }
                });

        waitForFX();
    }

    @Test
    @DisplayName("점수 리스트가 비어있을 때 정상 동작하는지 확인")
    void testEmptyScoreList() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = scoreboardScene.getScene();
                    VBox layout = (VBox) scene.getRoot();
                    @SuppressWarnings("unchecked")
                    ListView<String> scoreList = (ListView<String>) layout.getChildren().get(1);

                    assertNotNull(scoreList, "Score list should exist even when empty");
                    assertNotNull(scoreList.getItems(), "Score list items should not be null");
                });

        waitForFX();
    }

    @Test
    @DisplayName("레이아웃 간격이 올바르게 설정되는지 확인")
    void testLayoutSpacing() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = scoreboardScene.getScene();
                    VBox layout = (VBox) scene.getRoot();

                    assertEquals(15, layout.getSpacing(), 0.1, "Layout spacing should be 15");
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
                    assertNotNull(scoreboardScene, "ScoreboardScene should be initialized");
                });

        waitForFX();
    }

    @Test
    @DisplayName("여러 번 Scene을 생성해도 정상 동작하는지 확인")
    void testMultipleSceneCreation() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene1 = scoreboardScene.getScene();
                    Scene scene2 = scoreboardScene.getScene();

                    assertNotNull(scene1, "First scene should not be null");
                    assertNotNull(scene2, "Second scene should not be null");
                    // 매번 새로운 Scene을 생성
                    assertNotSame(scene1, scene2, "Each call should create a new scene");
                });

        waitForFX();
    }

    @Test
    @DisplayName("점수 리스트에 키보드 이벤트 핸들러가 있는지 확인")
    void testScoreListKeyboardHandler() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = scoreboardScene.getScene();
                    VBox layout = (VBox) scene.getRoot();
                    @SuppressWarnings("unchecked")
                    ListView<String> scoreList = (ListView<String>) layout.getChildren().get(1);

                    assertNotNull(
                            scoreList.getOnKeyPressed(),
                            "Score list should have key press handler for navigation");
                });

        waitForFX();
    }

    @Test
    @DisplayName("다양한 난이도 모드로 하이라이트를 생성할 수 있는지 확인")
    void testHighlightWithDifferentModes() {
        javafx.application.Platform.runLater(
                () -> {
                    ScoreBoard.ScoreEntry.Mode[] modes = {
                        ScoreBoard.ScoreEntry.Mode.EASY,
                        ScoreBoard.ScoreEntry.Mode.NORMAL,
                        ScoreBoard.ScoreEntry.Mode.HARD,
                        ScoreBoard.ScoreEntry.Mode.ITEM
                    };

                    for (ScoreBoard.ScoreEntry.Mode mode : modes) {
                        ScoreboardScene scene =
                                new ScoreboardScene(
                                        sceneManager, settings, "TestPlayer", 1000, mode);

                        assertNotNull(
                                scene.getScene(),
                                "Scene should be created with mode: " + mode.name());
                    }
                });

        waitForFX();
    }

    @Test
    @DisplayName("모든 자식 요소가 올바른 타입인지 확인")
    void testChildTypes() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = scoreboardScene.getScene();
                    VBox layout = (VBox) scene.getRoot();

                    assertTrue(
                            layout.getChildren().get(0) instanceof Label,
                            "First child should be Label");
                    assertTrue(
                            layout.getChildren().get(1) instanceof ListView,
                            "Second child should be ListView");
                    assertTrue(
                            layout.getChildren().get(2) instanceof Button,
                            "Third child should be Button");
                });

        waitForFX();
    }

    @Test
    @DisplayName("ScoreBoard 객체가 올바르게 초기화되는지 확인")
    void testScoreBoardInitialization() {
        javafx.application.Platform.runLater(
                () -> {
                    // ScoreBoard는 내부적으로 생성되므로 Scene이 정상 생성되면 OK
                    Scene scene = scoreboardScene.getScene();
                    assertNotNull(scene, "Scene creation should succeed with internal ScoreBoard");
                });

        waitForFX();
    }

    @Test
    @DisplayName("하이라이트 모드에서 Exit 버튼이 추가되는지 확인")
    void testExitButtonInHighlightMode() {
        javafx.application.Platform.runLater(
                () -> {
                    ScoreboardScene scene =
                            new ScoreboardScene(
                                    sceneManager,
                                    settings,
                                    "TestPlayer",
                                    1000,
                                    ScoreBoard.ScoreEntry.Mode.NORMAL);

                    Scene sceneObj = scene.getScene();
                    VBox layout = (VBox) sceneObj.getRoot();

                    assertEquals(
                            4,
                            layout.getChildren().size(),
                            "Layout should have 4 children with highlight (title, list, back, exit)");
                    assertTrue(
                            layout.getChildren().get(3) instanceof Button,
                            "Fourth child should be Exit button");

                    Button exitBtn = (Button) layout.getChildren().get(3);
                    assertEquals("Exit", exitBtn.getText(), "Exit button text should be 'Exit'");
                    assertNotNull(exitBtn.getOnAction(), "Exit button should have action handler");
                });

        waitForFX();
    }

    @Test
    @DisplayName("하이라이트 없는 모드에서는 Exit 버튼이 없는지 확인")
    void testNoExitButtonWithoutHighlight() {
        javafx.application.Platform.runLater(
                () -> {
                    Scene scene = scoreboardScene.getScene();
                    VBox layout = (VBox) scene.getRoot();

                    assertEquals(
                            3,
                            layout.getChildren().size(),
                            "Layout should have 3 children without highlight (title, list, back)");
                    // 네 번째 자식이 없어야 함
                    assertFalse(
                            layout.getChildren().size() > 3,
                            "Should not have Exit button without highlight parameters");
                });

        waitForFX();
    }

    @Test
    @DisplayName("부분 하이라이트 파라미터로 호출시 Exit 버튼이 없는지 확인")
    void testNoExitButtonWithPartialHighlight() {
        javafx.application.Platform.runLater(
                () -> {
                    // name만 null이 아닌 경우
                    ScoreboardScene scene1 =
                            new ScoreboardScene(sceneManager, settings, "TestPlayer", null, null);
                    VBox layout1 = (VBox) scene1.getScene().getRoot();
                    assertEquals(
                            3,
                            layout1.getChildren().size(),
                            "Should have 3 children when score is null");

                    // score만 null이 아닌 경우
                    ScoreboardScene scene2 =
                            new ScoreboardScene(sceneManager, settings, null, 1000, null);
                    VBox layout2 = (VBox) scene2.getScene().getRoot();
                    assertEquals(
                            3,
                            layout2.getChildren().size(),
                            "Should have 3 children when name is null");

                    // mode만 null이 아닌 경우
                    ScoreboardScene scene3 =
                            new ScoreboardScene(
                                    sceneManager,
                                    settings,
                                    null,
                                    null,
                                    ScoreBoard.ScoreEntry.Mode.NORMAL);
                    VBox layout3 = (VBox) scene3.getScene().getRoot();
                    assertEquals(
                            3,
                            layout3.getChildren().size(),
                            "Should have 3 children when name and score are null");
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
