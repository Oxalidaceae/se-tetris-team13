package team13.tetris.scenes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;

public class LocalMultiModeSelectionSceneTest {

    private SceneManager mockManager;
    private Settings mockSettings;
    private LocalMultiModeSelectionScene scene;

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
        scene = new LocalMultiModeSelectionScene(mockManager, mockSettings);
    }

    @Test
    void testGetScene() {
        var result = scene.getScene();
        assertNotNull(result);
        assertNotNull(result.getRoot());
    }
}
