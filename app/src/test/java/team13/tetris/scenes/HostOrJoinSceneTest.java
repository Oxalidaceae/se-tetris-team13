package team13.tetris.scenes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;

public class HostOrJoinSceneTest {

    private SceneManager mockManager;
    private Settings mockSettings;
    private HostOrJoinScene scene;

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

        // NPE 방지: getRecentIP() 이 null이 아니라 빈 문자열을 리턴하도록 설정
        when(mockSettings.getRecentIP()).thenReturn("");

        scene = new HostOrJoinScene(mockManager, mockSettings);
    }

    @Test
    void testGetScene() {
        var result = scene.getScene();
        assertNotNull(result);
        assertNotNull(result.getRoot());
    }

    @Test
    void testIsValidIPAddressWithValidIPv4() {
        assertTrue(scene.isValidIPAddress("192.168.1.1"));
        assertTrue(scene.isValidIPAddress("127.0.0.1"));
        assertTrue(scene.isValidIPAddress("255.255.255.255"));
        assertTrue(scene.isValidIPAddress("0.0.0.0"));
    }

    @Test
    void testIsValidIPAddressWithLocalhost() {
        assertTrue(scene.isValidIPAddress("localhost"));
        assertTrue(scene.isValidIPAddress("LOCALHOST"));
    }

    @Test
    void testIsValidIPAddressWithInvalidIPv4() {
        assertFalse(scene.isValidIPAddress("256.1.1.1"));
        assertFalse(scene.isValidIPAddress("192.168.1"));
        assertFalse(scene.isValidIPAddress("192.168.1.1.1"));
        assertFalse(scene.isValidIPAddress("abc.def.ghi.jkl"));
        assertFalse(scene.isValidIPAddress("-1.0.0.1"));
        assertFalse(scene.isValidIPAddress(""));
    }
}
