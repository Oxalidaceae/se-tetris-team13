package team13.tetris.scenes;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GameModeSelectionSceneTest {
    
    private SceneManager mockManager;
    private Settings mockSettings;
    private GameModeSelectionScene scene;
    
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
        scene = new GameModeSelectionScene(mockManager, mockSettings);
    }
    
    @Test
    void testGetScene() {
        var result = scene.getScene();
        assertNotNull(result);
        assertEquals(600, result.getWidth());
        assertEquals(700, result.getHeight());
    }
}
