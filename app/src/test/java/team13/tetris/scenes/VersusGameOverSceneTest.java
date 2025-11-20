package team13.tetris.scenes;

import javafx.application.Platform;
import javafx.scene.Scene;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VersusGameOverSceneTest {
    
    private SceneManager mockManager;
    private Settings mockSettings;
    
    @BeforeAll
    static void initToolkit() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }
    
    @BeforeEach
    void setUp() {
        mockManager = mock(SceneManager.class);
        mockSettings = mock(Settings.class);
    }
    
    @Test
    void testLocalModeScene() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            VersusGameOverScene scene = new VersusGameOverScene(
                mockManager, mockSettings, "Player 1", 1000, 800, false, false
            );
            
            Scene result = scene.getScene();
            assertNotNull(result);
            assertNotNull(result.getRoot());
            latch.countDown();
        });
        latch.await();
    }
    
    @Test
    void testNetworkModeWinScene() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Runnable mockCallback = mock(Runnable.class);
        
        Platform.runLater(() -> {
            VersusGameOverScene scene = new VersusGameOverScene(
                mockManager, mockSettings, "You", 1200, 900, 
                true, true, "Player 1", true, mockCallback
            );
            
            Scene result = scene.getScene();
            assertNotNull(result);
            assertNotNull(result.getRoot());
            latch.countDown();
        });
        latch.await();
    }
    
    @Test
    void testNetworkModeLoseScene() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Runnable mockCallback = mock(Runnable.class);
        
        Platform.runLater(() -> {
            VersusGameOverScene scene = new VersusGameOverScene(
                mockManager, mockSettings, "Opponent", 900, 1200, 
                false, true, "Player 1", true, mockCallback
            );
            
            Scene result = scene.getScene();
            assertNotNull(result);
            assertNotNull(result.getRoot());
            latch.countDown();
        });
        latch.await();
    }
    
    @Test
    void testNetworkModeDrawScene() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Runnable mockCallback = mock(Runnable.class);
        
        Platform.runLater(() -> {
            VersusGameOverScene scene = new VersusGameOverScene(
                mockManager, mockSettings, "Draw", 1000, 1000, 
                true, false, "Player 1", true, mockCallback
            );
            
            Scene result = scene.getScene();
            assertNotNull(result);
            assertNotNull(result.getRoot());
            latch.countDown();
        });
        latch.await();
    }
    
    @Test
    void testTimerModeScene() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            VersusGameOverScene scene = new VersusGameOverScene(
                mockManager, mockSettings, "Player 2", 1500, 1200, 
                true, false
            );
            
            Scene result = scene.getScene();
            assertNotNull(result);
            assertNotNull(result.getRoot());
            latch.countDown();
        });
        latch.await();
    }
    
    @Test
    void testItemModeScene() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            VersusGameOverScene scene = new VersusGameOverScene(
                mockManager, mockSettings, "Player 1", 2000, 1800, 
                false, true
            );
            
            Scene result = scene.getScene();
            assertNotNull(result);
            assertNotNull(result.getRoot());
            latch.countDown();
        });
        latch.await();
    }
    
    @Test
    void testDefaultConstructor() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            VersusGameOverScene scene = new VersusGameOverScene(
                mockManager, mockSettings, "Player 2", 1100, 900, 
                false, false
            );
            
            Scene result = scene.getScene();
            assertNotNull(result);
            assertNotNull(result.getRoot());
            latch.countDown();
        });
        latch.await();
    }
    
    @Test
    void testNullCurrentPlayerHandling() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            VersusGameOverScene scene = new VersusGameOverScene(
                mockManager, mockSettings, "Player 1", 1000, 800, 
                false, false, null, false, null
            );
            
            Scene result = scene.getScene();
            assertNotNull(result);
            assertNotNull(result.getRoot());
            latch.countDown();
        });
        latch.await();
    }
}
