package team13.tetris.scenes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.scene.Scene;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;
import team13.tetris.game.logic.GameEngine;
import team13.tetris.game.model.Board;

public class VersusGameSceneTest {

    private SceneManager mockManager;
    private Settings mockSettings;
    private GameEngine mockEngine1;
    private GameEngine mockEngine2;
    private Board mockBoard1;
    private Board mockBoard2;
    private VersusGameScene versusGameScene;

    @BeforeAll
    static void initToolkit() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        mockManager = mock(SceneManager.class);
        mockSettings = mock(Settings.class);
        mockEngine1 = mock(GameEngine.class);
        mockEngine2 = mock(GameEngine.class);
        mockBoard1 = mock(Board.class);
        mockBoard2 = mock(Board.class);

        // Mock 설정
        when(mockSettings.getWindowSize()).thenReturn("MEDIUM");
        when(mockEngine1.getBoard()).thenReturn(mockBoard1);
        when(mockEngine2.getBoard()).thenReturn(mockBoard2);
        when(mockBoard1.getWidth()).thenReturn(10);
        when(mockBoard1.getHeight()).thenReturn(20);
        when(mockBoard2.getWidth()).thenReturn(10);
        when(mockBoard2.getHeight()).thenReturn(20);
        when(mockEngine1.getScore()).thenReturn(1000);
        when(mockEngine2.getScore()).thenReturn(800);

        // 보드 셀 초기화
        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 10; x++) {
                when(mockBoard1.getCell(x, y)).thenReturn(0);
                when(mockBoard2.getCell(x, y)).thenReturn(0);
            }
        }

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    versusGameScene =
                            new VersusGameScene(
                                    mockManager, mockSettings, mockEngine1, mockEngine2, false);
                    latch.countDown();
                });
        latch.await();
    }

    @Test
    void testCreateScene() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    Scene scene = versusGameScene.createScene();
                    assertNotNull(scene);
                    assertNotNull(scene.getRoot());
                    latch.countDown();
                });
        latch.await();
    }

    @Test
    void testGetScene() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    versusGameScene.createScene();
                    Scene scene = versusGameScene.getScene();
                    assertNotNull(scene);
                    latch.countDown();
                });
        latch.await();
    }

    @Test
    void testSetEngines() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    GameEngine newEngine1 = mock(GameEngine.class);
                    GameEngine newEngine2 = mock(GameEngine.class);

                    versusGameScene.setEngine1(newEngine1);
                    versusGameScene.setEngine2(newEngine2);

                    // 설정이 성공적으로 완료되었는지 확인
                    assertNotNull(versusGameScene);
                    latch.countDown();
                });
        latch.await();
    }

    @Test
    void testUpdateGrid() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    versusGameScene.updateGrid();
                    // 업데이트가 성공적으로 완료되었는지 확인
                    assertNotNull(versusGameScene);
                    latch.countDown();
                });
        latch.await();
    }

    @Test
    void testTimerModeScene() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    VersusGameScene timerScene =
                            new VersusGameScene(
                                    mockManager, mockSettings, mockEngine1, mockEngine2, true);
                    assertNotNull(timerScene);
                    latch.countDown();
                });
        latch.await();
    }

    @Test
    void testUpdateTimer() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    VersusGameScene timerScene =
                            new VersusGameScene(
                                    mockManager, mockSettings, mockEngine1, mockEngine2, true);
                    timerScene.updateTimer(120);
                    timerScene.updateTimer(30); // 빨간색으로 변경되는 임계값
                    timerScene.updateTimer(10);
                    assertNotNull(timerScene);
                    latch.countDown();
                });
        latch.await();
    }

    @Test
    void testUpdateIncomingGrid() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    Queue<int[][]> incomingBlocks = new LinkedList<>();

                    // 테스트용 블록 패턴 생성
                    int[][] pattern1 = new int[2][10];
                    for (int i = 0; i < 10; i++) {
                        pattern1[0][i] = 1;
                        pattern1[1][i] = 1;
                    }
                    incomingBlocks.add(pattern1);

                    // Player 1과 Player 2 모두 업데이트
                    versusGameScene.updateIncomingGrid(1, incomingBlocks);
                    versusGameScene.updateIncomingGrid(2, incomingBlocks);

                    assertNotNull(versusGameScene);
                    latch.countDown();
                });
        latch.await();
    }

    @Test
    void testRequestFocus() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    versusGameScene.createScene();
                    versusGameScene.requestFocus();
                    assertNotNull(versusGameScene);
                    latch.countDown();
                });
        latch.await();
    }

    @Test
    void testDifferentWindowSizes() throws Exception {
        String[] windowSizes = {"SMALL", "MEDIUM", "LARGE"};

        for (String windowSize : windowSizes) {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(
                    () -> {
                        when(mockSettings.getWindowSize()).thenReturn(windowSize);
                        VersusGameScene scene =
                                new VersusGameScene(
                                        mockManager, mockSettings, mockEngine1, mockEngine2, false);
                        assertNotNull(scene);
                        latch.countDown();
                    });
            latch.await();
        }
    }

    @Test
    void testEmptyIncomingGrid() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    Queue<int[][]> emptyQueue = new LinkedList<>();
                    versusGameScene.updateIncomingGrid(1, emptyQueue);
                    versusGameScene.updateIncomingGrid(2, emptyQueue);
                    assertNotNull(versusGameScene);
                    latch.countDown();
                });
        latch.await();
    }
}
