package team13.tetris.scenes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;

public class MultiModeSelectionSceneTest {

    private SceneManager mockManager;
    private Settings mockSettings;
    private MultiModeSelectionScene scene;

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
        scene = new MultiModeSelectionScene(mockManager, mockSettings);
    }

    @Test
    void testGetScene() {
        var result = scene.getScene();
        assertNotNull(result);
        assertNotNull(result.getRoot());
    }
}
