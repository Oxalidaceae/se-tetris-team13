package team13.tetris.scenes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;
import team13.tetris.game.logic.GameEngine;
import team13.tetris.game.model.Board;

public class NetworkGameSceneTest {

    private SceneManager mockManager;
    private Settings mockSettings;
    private GameEngine mockEngine;
    private Board mockBoard;
    private NetworkGameScene scene;

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
        mockEngine = mock(GameEngine.class);
        mockBoard = mock(Board.class);

        when(mockSettings.getWindowSize()).thenReturn("MEDIUM");
        when(mockSettings.isColorBlindMode()).thenReturn(false);
        when(mockEngine.getBoard()).thenReturn(mockBoard);
        when(mockBoard.getWidth()).thenReturn(10);
        when(mockBoard.getHeight()).thenReturn(20);
        when(mockEngine.getScore()).thenReturn(0);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    scene =
                            new NetworkGameScene(
                                    mockManager,
                                    mockSettings,
                                    mockEngine,
                                    "LocalPlayer",
                                    "RemotePlayer",
                                    false);
                    latch.countDown();
                });
        latch.await();
    }

    @Test
    void testGetScene() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    var result = scene.getScene();
                    assertNotNull(result);
                    assertNotNull(result.getRoot());
                    latch.countDown();
                });
        latch.await();
    }

    @Test
    void testUpdateTimer() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    scene.updateTimer(60);
                    scene.updateTimer(30); // Red warning
                    scene.updateTimer(120); // Back to normal
                    latch.countDown();
                });
        latch.await();
    }

    @Test
    void testUpdateRemoteBoardState() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    int[][] board = new int[20][10];
                    scene.updateRemoteBoardState(
                            board,
                            5,
                            10,
                            1,
                            0,
                            false,
                            null,
                            -1,
                            2,
                            false,
                            null,
                            -1,
                            new LinkedList<>(),
                            1000,
                            5);
                    assertNotNull(scene);
                    latch.countDown();
                });
        latch.await();
    }

    @Test
    void testGetOpponentScore() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    assertEquals(0, scene.getOpponentScore());

                    int[][] board = new int[20][10];
                    scene.updateRemoteBoardState(
                            board,
                            0,
                            0,
                            0,
                            0,
                            false,
                            null,
                            -1,
                            0,
                            false,
                            null,
                            -1,
                            new LinkedList<>(),
                            500,
                            0);
                    assertEquals(500, scene.getOpponentScore());
                    latch.countDown();
                });
        latch.await();
    }

    @Test
    void testUpdateLocalGrid() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    scene.updateLocalGrid();
                    assertNotNull(scene);
                    latch.countDown();
                });
        latch.await();
    }
}
