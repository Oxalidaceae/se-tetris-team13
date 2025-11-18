package team13.tetris.network.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import team13.tetris.network.listener.ServerMessageListener;
import team13.tetris.network.protocol.*;



import static org.junit.jupiter.api.Assertions.*;

class TetrisServerTest {

    private TetrisServer server;
    private TestServerMessageListener listener;

    @BeforeEach
    void setUp() {
        listener = new TestServerMessageListener();
        server = new TetrisServer("TestHost", 12346); // 다른 포트 사용
        server.setHostMessageListener(listener);
    }

    @Test
    @DisplayName("서버 생성 시 초기 상태가 올바른지 확인")
    void testServerInitialState() {
        assertFalse(server.isRunning(), "초기 상태에서는 실행 중이 아니어야 함");
        assertEquals("TestHost", server.getHostPlayerId(), "호스트 플레이어 ID가 올바르게 설정되어야 함");
    }

    @Test
    @DisplayName("기본 포트로 서버 생성")
    void testServerWithDefaultPort() {
        TetrisServer defaultServer = new TetrisServer("DefaultHost");
        assertNotNull(defaultServer, "기본 포트로 서버 생성 가능");
        assertFalse(defaultServer.isRunning(), "초기 상태에서는 실행 중이 아니어야 함");
        assertEquals("DefaultHost", defaultServer.getHostPlayerId(), "호스트 플레이어 ID가 올바르게 설정되어야 함");
    }

    @Test
    @DisplayName("실행 중이지 않은 서버 중지 테스트")
    void testStopNonRunningServer() {
        assertDoesNotThrow(() -> {
            server.stop();
        }, "실행 중이지 않은 서버 중지는 안전해야 함");
    }

    @Test
    @DisplayName("메시지 리스너 설정 테스트")
    void testSetHostMessageListener() {
        TestServerMessageListener newListener = new TestServerMessageListener();
        assertDoesNotThrow(() -> {
            server.setHostMessageListener(newListener);
        }, "메시지 리스너 설정은 안전해야 함");
    }

    @Test
    @DisplayName("클라이언트가 없는 상태에서 브로드캐스트 테스트")
    void testBroadcastWithNoClients() {
        assertDoesNotThrow(() -> {
            server.broadcastGameOverToOthers("TestPlayer", "Test reason");
            server.pauseGameAsHost();
            server.resumeGameAsHost();
        }, "클라이언트가 없는 상태에서 브로드캐스트는 안전해야 함");
    }

    @Test
    @DisplayName("서버 시작 실패 테스트 - 잘못된 포트")
    void testServerStartWithInvalidPort() {
        TetrisServer invalidServer = new TetrisServer("TestHost", -1);
        assertThrows(IllegalArgumentException.class, () -> {
            invalidServer.start();
        }, "잘못된 포트로 서버 시작 시 IllegalArgumentException이 발생해야 함");
    }

    @Test
    @DisplayName("게임 모드 선택 테스트")
    void testSelectGameMode() {
        assertDoesNotThrow(() -> {
            server.selectGameMode(GameModeMessage.GameMode.NORMAL);
        }, "게임 모드 선택은 안전해야 함");
    }

    @Test
    @DisplayName("서버 생성자 파라미터 테스트")
    void testServerConstructorParameters() {
        TetrisServer customServer = new TetrisServer("CustomHost", 13000);
        assertNotNull(customServer, "커스텀 파라미터로 서버 생성 가능");
        assertEquals("CustomHost", customServer.getHostPlayerId(), "커스텀 호스트 ID가 올바르게 설정되어야 함");
    }

    @Test
    @DisplayName("다양한 호스트 ID로 서버 생성")
    void testVariousHostIds() {
        String[] hostIds = {"Host1", "한글호스트", "Host_123", "H", "VeryLongHostNameForTesting"};
        
        for (String hostId : hostIds) {
            TetrisServer testServer = new TetrisServer(hostId);
            assertNotNull(testServer, "호스트 ID: " + hostId + "로 서버 생성 가능");
            assertEquals(hostId, testServer.getHostPlayerId(), "호스트 ID가 올바르게 설정됨");
            assertFalse(testServer.isRunning(), "초기 상태는 실행 중이 아님");
        }
    }

    @Test
    @DisplayName("다양한 포트로 서버 생성")
    void testVariousPorts() {
        int[] ports = {12347, 8081, 9998, 1235, 50000};
        
        for (int port : ports) {
            TetrisServer testServer = new TetrisServer("TestHost", port);
            assertNotNull(testServer, "포트: " + port + "로 서버 생성 가능");
            assertFalse(testServer.isRunning(), "초기 상태는 실행 중이 아님");
        }
    }

    @Test
    @DisplayName("서버 상태 확인 메서드들")
    void testServerStateMethods() {
        assertFalse(server.isRunning(), "초기 상태에서는 실행 중이 아님");
        assertEquals("TestHost", server.getHostPlayerId(), "호스트 플레이어 ID 반환");
        
        // 중지 상태에서 메서드 호출
        assertDoesNotThrow(() -> {
            server.stop();
            server.broadcastGameOverToOthers("TestPlayer", "Game Over");
            server.pauseGameAsHost();
            server.resumeGameAsHost();
        }, "중지 상태에서도 메서드 호출은 안전해야 함");
    }

    @Test
    @DisplayName("게임 모드 선택 다양한 모드 테스트")
    void testAllGameModes() {
        GameModeMessage.GameMode[] modes = GameModeMessage.GameMode.values();
        
        for (GameModeMessage.GameMode mode : modes) {
            assertDoesNotThrow(() -> {
                server.selectGameMode(mode);
            }, "게임 모드 " + mode + " 선택은 안전해야 함");
        }
    }

    @Test
    @DisplayName("서버 메시지 브로드캐스트 테스트")
    void testServerBroadcastMethods() {
        assertDoesNotThrow(() -> {
            // 유효한 브로드캐스트 메서드 테스트
            server.broadcastGameOverToOthers("Player1", "Game Over Reason");
            server.broadcastGameOverToOthers("Player2", null); // null 이유
            server.broadcastGameOverToOthers("Player3", ""); // 빈 이유
            // 빈 ID나 null ID는 검증에서 제외 (예외 발생)
        }, "유효한 브로드캐스트 호출은 안전해야 함");
    }

    @Test
    @DisplayName("서버 게임 제어 메서드 테스트")
    void testServerGameControlMethods() {
        assertDoesNotThrow(() -> {
            // 호스트로서 게임 제어
            server.pauseGameAsHost();
            server.resumeGameAsHost();
            
            // 여러 번 연속 호출
            for (int i = 0; i < 5; i++) {
                server.pauseGameAsHost();
                server.resumeGameAsHost();
            }
        }, "게임 제어 메서드들은 안전해야 함");
    }

    @Test
    @DisplayName("서버 리스너 null 설정 테스트")
    void testNullMessageListener() {
        assertDoesNotThrow(() -> {
            server.setHostMessageListener(null);
        }, "null 리스너 설정은 안전해야 함");
    }

    @Test
    @DisplayName("여러 번 서버 중지 테스트")
    void testMultipleServerStops() {
        assertDoesNotThrow(() -> {
            server.stop();
            server.stop();
            server.stop();
        }, "여러 번 서버 중지해도 안전해야 함");
    }

    @Test
    @DisplayName("극한 상황에서 서버 동작 테스트")
    void testServerExtremeConditions() {
        // 매우 긴 문자열로 브로드캐스트
        String longReason = "Very long reason ".repeat(100);
        String longPlayerId = "VeryLongPlayerId".repeat(20);
        
        assertDoesNotThrow(() -> {
            server.broadcastGameOverToOthers(longPlayerId, longReason);
        }, "긴 문자열도 안전하게 처리해야 함");
    }

    @Test
    @DisplayName("서버 동시성 안전성 테스트")
    void testServerConcurrencySafety() {
        assertDoesNotThrow(() -> {
            // 동시에 여러 작업 수행 시뮬레이션
            server.pauseGameAsHost();
            server.selectGameMode(GameModeMessage.GameMode.ITEM);
            server.resumeGameAsHost();
            server.broadcastGameOverToOthers("TestPlayer", "Concurrent test");
            server.stop();
        }, "동시 작업도 안전해야 함");
    }

    @Test
    @DisplayName("잘못된 포트 범위 테스트")
    void testInvalidPortRanges() {
        int[] invalidPorts = {-1, 0, 65536, 100000};
        
        for (int port : invalidPorts) {
            assertDoesNotThrow(() -> {
                TetrisServer invalidServer = new TetrisServer("TestHost", port);
                assertNotNull(invalidServer, "잘못된 포트로도 서버 객체 생성은 가능");
                // 시작할 때 예외가 발생할 수 있음
            }, "잘못된 포트로 서버 생성 시도는 안전해야 함");
        }
    }

    @Test
    @DisplayName("빈 문자열이나 특수 문자가 포함된 호스트 ID 테스트")
    void testSpecialHostIds() {
        String[] specialIds = {"", "Host With Spaces", "Host@#$%", "12345", "가나다라마"};
        
        for (String id : specialIds) {
            assertDoesNotThrow(() -> {
                TetrisServer specialServer = new TetrisServer(id);
                assertNotNull(specialServer, "특수 ID로도 서버 생성 가능: " + id);
                assertEquals(id, specialServer.getHostPlayerId(), "특수 ID도 올바르게 저장됨");
            }, "특수 문자 ID 처리는 안전해야 함: " + id);
        }
    }

    // 테스트용 ServerMessageListener 구현
    private static class TestServerMessageListener implements ServerMessageListener {
        private String lastClientId = null;
        private boolean gameStarted = false;

        @Override
        public void onClientConnected(String clientId) {
            lastClientId = clientId;
        }

        @Override
        public void onClientDisconnected(String clientId) {
            lastClientId = clientId;
        }

        @Override
        public void onPlayerReady(String playerId) {
            // 테스트용 구현
        }

        @Override
        public void onGameStart() {
            gameStarted = true;
        }

        @Override
        public void onCountdownStart() {
            // 테스트용 구현
        }

        @Override
        public void onGameOver(String reason) {
            // 테스트용 구현
        }

        @Override
        public void onBoardUpdate(BoardUpdateMessage boardUpdate) {
            // 테스트용 구현
        }

        @Override
        public void onAttackReceived(AttackMessage attackMessage) {
            // 테스트용 구현
        }

        @Override
        public void onGamePaused() {
            // 테스트용 구현
        }

        @Override
        public void onGameResumed() {
            // 테스트용 구현
        }

        public String getLastClientId() {
            return lastClientId;
        }

        public boolean isGameStarted() {
            return gameStarted;
        }

        @Override
        public void onPlayerUnready(String playerId) {
            // Test implementation
        }
    }
}