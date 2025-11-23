package team13.tetris.scenes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;
import team13.tetris.network.protocol.GameModeMessage;

public class NetworkLobbySceneTest {

    private SceneManager mockManager;
    private Settings mockSettings;
    private NetworkLobbyScene hostScene;
    private NetworkLobbyScene clientScene;

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

        CountDownLatch latch = new CountDownLatch(2);
        Platform.runLater(
                () -> {
                    hostScene = new NetworkLobbyScene(mockManager, mockSettings, true);
                    latch.countDown();
                });
        Platform.runLater(
                () -> {
                    clientScene = new NetworkLobbyScene(mockManager, mockSettings, false);
                    latch.countDown();
                });
        latch.await();
    }

    @Test
    void testGetScene() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    assertNotNull(hostScene.getScene());
                    assertNotNull(clientScene.getScene());
                    latch.countDown();
                });
        latch.await();
    }

    @Test
    void testGetSelectedGameMode() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    assertEquals(GameModeMessage.GameMode.NORMAL, hostScene.getSelectedGameMode());
                    latch.countDown();
                });
        latch.await();
    }

    @Test
    void testSetMyReady() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    hostScene.setMyReady(true);
                    hostScene.setMyReady(false);
                    assertNotNull(hostScene);
                    latch.countDown();
                });
        latch.await();
    }

    @Test
    void testSetOpponentReady() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    hostScene.setOpponentReady(true);
                    hostScene.setOpponentReady(false);
                    assertNotNull(hostScene);
                    latch.countDown();
                });
        latch.await();
    }

    @Test
    void testAreBothReady() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    assertFalse(hostScene.areBothReady());

                    hostScene.setMyReady(true);
                    assertFalse(hostScene.areBothReady());

                    hostScene.setOpponentReady(true);
                    assertTrue(hostScene.areBothReady());
                    latch.countDown();
                });
        latch.await();
    }

    @Test
    void testSetStatusText() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    hostScene.setStatusText("Test Status");
                    assertNotNull(hostScene);
                    latch.countDown();
                });
        latch.await();
    }

    @Test
    void testSetGameMode() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    clientScene.setGameMode("Item Mode");
                    assertNotNull(clientScene);
                    latch.countDown();
                });
        latch.await();
    }

    @Test
    void testSetControlsDisabled() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    hostScene.setControlsDisabled(true);
                    hostScene.setControlsDisabled(false);
                    assertNotNull(hostScene);
                    latch.countDown();
                });
        latch.await();
    }
}
