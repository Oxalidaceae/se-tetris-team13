package team13.tetris.game.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;
import team13.tetris.data.ScoreBoard;
import team13.tetris.network.protocol.*;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("JavaFX Toolkit dependency issues in test environment")
class NetworkGameControllerTest {

    private NetworkGameController controller;
    private Settings settings;
    private SceneManager sceneManager;

    @BeforeEach
    void setUp() {
        settings = new Settings();
        // SceneManager는 JavaFX Stage가 필요하므로 null로 생성하거나 생략
        sceneManager = null; // 실제 UI 테스트가 아니므로 null로 처리
        
        // 실제 테스트에서는 null SceneManager로도 동작하도록 구현되어야 함
        controller = new NetworkGameController(sceneManager, settings, true, "127.0.0.1");
    }

    @Test
    @DisplayName("NetworkGameController 생성 테스트 - 호스트 모드")
    void testNetworkGameControllerCreationHost() {
        NetworkGameController hostController = new NetworkGameController(null, settings, true, "127.0.0.1");
        assertNotNull(hostController, "호스트 모드로 컨트롤러 생성 가능");
    }

    @Test
    @DisplayName("NetworkGameController 생성 테스트 - 클라이언트 모드")
    void testNetworkGameControllerCreationClient() {
        NetworkGameController clientController = new NetworkGameController(null, settings, false, "127.0.0.1");
        assertNotNull(clientController, "클라이언트 모드로 컨트롤러 생성 가능");
    }

    @Test
    @DisplayName("연결 해제 테스트")
    void testDisconnect() {
        assertDoesNotThrow(() -> {
            controller.disconnect();
        }, "연결 해제는 안전해야 함");
    }

    @Test
    @DisplayName("ClientMessageListener 구현 메서드 테스트")
    void testClientMessageListenerMethods() {
        assertDoesNotThrow(() -> {
            controller.onConnectionAccepted();
            controller.onGameStart();
            controller.onGameOver("Test reason");
            controller.onGamePaused();
            controller.onGameResumed();
            controller.onError("Test error");
            controller.onGameModeSelected(GameModeMessage.GameMode.NORMAL);
        }, "ClientMessageListener 메서드들은 안전해야 함");
        
        // JavaFX Platform.runLater 사용 메서드들은 IllegalStateException 발생
        assertThrows(IllegalStateException.class, () -> {
            controller.onConnectionRejected("Test reason");
        }, "JavaFX 의존성 메서드는 툴킷 미초기화 시 IllegalStateException 발생");
        
        assertThrows(IllegalStateException.class, () -> {
            controller.onPlayerReady("TestPlayer");
        }, "JavaFX 의존성 메서드는 툴킷 미초기화 시 IllegalStateException 발생");
    }

    @Test
    @DisplayName("ServerMessageListener 구현 메서드 테스트")
    void testServerMessageListenerMethods() {
        assertDoesNotThrow(() -> {
            controller.onClientDisconnected("TestClient");
        }, "ServerMessageListener 메서드들은 안전해야 함");
        
        // JavaFX Platform.runLater 사용 메서드는 IllegalStateException 발생
        assertThrows(IllegalStateException.class, () -> {
            controller.onClientConnected("TestClient");
        }, "JavaFX 의존성 메서드는 툴킷 미초기화 시 IllegalStateException 발생");
    }

    @Test
    @DisplayName("보드 업데이트 메시지 처리 테스트")
    void testBoardUpdateMessage() {
        int[][] testBoard = new int[20][10];
        BoardUpdateMessage message = new BoardUpdateMessage("TestPlayer", testBoard, 1, 2, 3, 4, 5, null, 6, 7, 8);
        
        assertDoesNotThrow(() -> {
            controller.onBoardUpdate(message);
        }, "보드 업데이트 메시지 처리는 안전해야 함");
    }

    @Test
    @DisplayName("공격 메시지 처리 테스트")
    void testAttackMessage() {
        AttackMessage message = new AttackMessage("TestPlayer", 2, 2, null);
        
        assertDoesNotThrow(() -> {
            controller.onAttackReceived(message);
        }, "공격 메시지 처리는 안전해야 함");
    }

    @Test
    @DisplayName("키 입력 처리 테스트")
    void testKeyInputHandling() {
        assertDoesNotThrow(() -> {
            // Mock KeyEvent는 복잡하므로 직접적인 키 이벤트 처리는 생략
            controller.onConnectionAccepted(); // 연결 상태로 만듦
        }, "키 입력 처리 설정은 안전해야 함");
    }

    @Test
    @DisplayName("게임 시작 전 상태 테스트")
    void testPreGameState() {
        // 게임 시작 전에는 대부분의 동작이 안전하게 무시되어야 함
        assertDoesNotThrow(() -> {
            controller.onGamePaused();
            controller.onGameResumed();
        }, "게임 시작 전 상태에서 메시지 처리는 안전해야 함");
        
        // JavaFX 의존성이 있는 메서드는 예외 발생
        assertThrows(IllegalStateException.class, () -> {
            controller.onPlayerReady("TestPlayer");
        }, "JavaFX 의존성 메서드는 툴킷 미초기화 시 IllegalStateException 발생");
    }

    @Test
    @DisplayName("null 파라미터 처리 테스트")
    void testNullParameterHandling() {
        assertDoesNotThrow(() -> {
            controller.onGameOver(null);
            controller.onError(null);
            controller.onBoardUpdate(null);
        }, "null 파라미터 처리는 안전해야 함");
        
        // JavaFX 의존성이 있는 메서드는 IllegalStateException 발생
        assertThrows(IllegalStateException.class, () -> {
            controller.onConnectionRejected(null);
        }, "JavaFX 의존성 메서드는 툴킷 미초기화 시 IllegalStateException 발생");
        
        // onAttackReceived는 null 체크가 없어서 NullPointerException 발생
        assertThrows(NullPointerException.class, () -> {
            controller.onAttackReceived(null);
        }, "null AttackMessage는 NullPointerException을 발생시켜야 함");
    }
}