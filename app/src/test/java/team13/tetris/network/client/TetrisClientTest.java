package team13.tetris.network.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import team13.tetris.network.listener.ClientMessageListener;
import team13.tetris.network.protocol.*;

import static org.junit.jupiter.api.Assertions.*;

class TetrisClientTest {

    private TetrisClient client;
    private TestClientMessageListener listener;

    @BeforeEach
    void setUp() {
        listener = new TestClientMessageListener();
        client = new TetrisClient("TestPlayer", "localhost", 12345);
        client.setMessageListener(listener);
    }

    @Test
    @DisplayName("클라이언트 생성 시 초기 상태가 올바른지 확인")
    void testClientInitialState() {
        assertFalse(client.isConnected(), "초기 상태에서는 연결되어 있지 않아야 함");
    }

    @Test
    @DisplayName("기본 호스트로 클라이언트 생성")
    void testClientWithDefaultHost() {
        TetrisClient defaultClient = new TetrisClient("TestPlayer");
        assertNotNull(defaultClient, "기본 호스트로 클라이언트 생성 가능");
        assertFalse(defaultClient.isConnected(), "초기 상태에서는 연결되어 있지 않아야 함");
    }

    @Test
    @DisplayName("호스트만 지정하여 클라이언트 생성")
    void testClientWithHostOnly() {
        TetrisClient hostOnlyClient = new TetrisClient("TestPlayer", "127.0.0.1");
        assertNotNull(hostOnlyClient, "호스트만 지정하여 클라이언트 생성 가능");
        assertFalse(hostOnlyClient.isConnected(), "초기 상태에서는 연결되어 있지 않아야 함");
    }

    @Test
    @DisplayName("연결되지 않은 상태에서 메시지 전송 시 false 반환")
    void testSendMessageWhenNotConnected() {
        ConnectionMessage message = ConnectionMessage.createPlayerReady("TestPlayer");
        boolean result = client.sendMessage(message);
        assertFalse(result, "연결되지 않은 상태에서 메시지 전송은 실패해야 함");
    }

    @Test
    @DisplayName("연결되지 않은 상태에서 disconnect 호출 시 안전하게 처리")
    void testDisconnectWhenNotConnected() {
        assertDoesNotThrow(() -> {
            client.disconnect();
        }, "연결되지 않은 상태에서 disconnect 호출은 안전해야 함");
    }

    @Test
    @DisplayName("메시지 리스너 설정 테스트")
    void testSetMessageListener() {
        TestClientMessageListener newListener = new TestClientMessageListener();
        assertDoesNotThrow(() -> {
            client.setMessageListener(newListener);
        }, "메시지 리스너 설정은 안전해야 함");
    }

    @Test
    @DisplayName("연결되지 않은 상태에서 각종 메시지 전송 메서드 테스트")
    void testMessageMethodsWhenNotConnected() {
        assertDoesNotThrow(() -> {
            assertFalse(client.pauseGame(), "연결되지 않은 상태에서 일시정지 메시지는 false 반환");
            assertFalse(client.resumeGame(), "연결되지 않은 상태에서 재개 메시지는 false 반환");
        }, "연결되지 않은 상태에서 메시지 전송 메서드들은 안전해야 함");
    }

    @Test
    @DisplayName("플레이어 ID 반환 테스트")
    void testGetPlayerId() {
        assertEquals("TestPlayer", client.getPlayerId(), "플레이어 ID가 올바르게 반환되어야 함");
    }

    @Test
    @DisplayName("게임 시작 상태 테스트")
    void testIsGameStarted() {
        assertFalse(client.isGameStarted(), "초기 상태에서는 게임이 시작되지 않아야 함");
    }

    @Test
    @DisplayName("다양한 메시지 생성 및 전송 테스트")
    void testVariousMessageSending() {
        // AttackMessage 전송 테스트
        AttackMessage attackMsg = AttackMessage.createStandardAttack("TestPlayer", 4);
        assertFalse(client.sendMessage(attackMsg), "연결되지 않은 상태에서 공격 메시지 전송 실패");

        // GameModeMessage 전송 테스트
        GameModeMessage gameModeMsg = new GameModeMessage("testServer", GameModeMessage.GameMode.ITEM);
        assertFalse(client.sendMessage(gameModeMsg), "연결되지 않은 상태에서 게임 모드 메시지 전송 실패");

        // ConnectionMessage 전송 테스트
        ConnectionMessage connMsg = ConnectionMessage.createPlayerReady("TestPlayer");
        assertFalse(client.sendMessage(connMsg), "연결되지 않은 상태에서 연결 메시지 전송 실패");
    }

    @Test
    @DisplayName("클라이언트 상태 변경 시뮬레이션")
    void testClientStateSimulation() {
        // 다양한 상태에서 메서드 호출
        assertFalse(client.isConnected(), "초기 연결 상태 확인");
        assertFalse(client.isGameStarted(), "초기 게임 상태 확인");
        
        // null 메시지 전송 시도 (방어적 프로그래밍 테스트)
        assertDoesNotThrow(() -> {
            boolean result = client.sendMessage(null);
            assertFalse(result, "null 메시지 전송은 실패해야 함");
        }, "null 메시지 처리는 안전해야 함");
    }

    @Test
    @DisplayName("여러 번 disconnect 호출 테스트")
    void testMultipleDisconnects() {
        assertDoesNotThrow(() -> {
            client.disconnect();
            client.disconnect();
            client.disconnect();
        }, "여러 번 disconnect 호출해도 안전해야 함");
    }

    @Test
    @DisplayName("메시지 리스너 null 설정 테스트")
    void testNullMessageListener() {
        assertDoesNotThrow(() -> {
            client.setMessageListener(null);
        }, "null 리스너 설정은 안전해야 함");
    }

    @Test
    @DisplayName("다양한 플레이어 ID로 클라이언트 생성")
    void testVariousPlayerIds() {
        String[] testIds = {"Player1", "한글플레이어", "Player_123", "P", "VeryLongPlayerNameForTesting"};
        
        for (String playerId : testIds) {
            TetrisClient testClient = new TetrisClient(playerId);
            assertNotNull(testClient, "플레이어 ID: " + playerId + "로 클라이언트 생성 가능");
            assertEquals(playerId, testClient.getPlayerId(), "플레이어 ID가 올바르게 설정됨");
            assertFalse(testClient.isConnected(), "초기 상태는 연결되지 않음");
        }
    }

    @Test
    @DisplayName("다양한 호스트 주소로 클라이언트 생성")
    void testVariousHostAddresses() {
        String[] hostAddresses = {"localhost", "127.0.0.1", "192.168.1.1", "example.com"};
        
        for (String host : hostAddresses) {
            TetrisClient testClient = new TetrisClient("TestPlayer", host);
            assertNotNull(testClient, "호스트: " + host + "로 클라이언트 생성 가능");
            assertFalse(testClient.isConnected(), "초기 상태는 연결되지 않음");
        }
    }

    @Test
    @DisplayName("다양한 포트로 클라이언트 생성")
    void testVariousPorts() {
        int[] ports = {12345, 8080, 9999, 1234, 65535};
        
        for (int port : ports) {
            TetrisClient testClient = new TetrisClient("TestPlayer", "localhost", port);
            assertNotNull(testClient, "포트: " + port + "로 클라이언트 생성 가능");
            assertFalse(testClient.isConnected(), "초기 상태는 연결되지 않음");
        }
    }

    @Test
    @DisplayName("게임 액션 메서드들 테스트")
    void testGameActionMethods() {
        // Ready 메시지
        assertFalse(client.requestReady(), "연결되지 않은 상태에서 requestReady는 false");
        
        // 게임 제어 메시지들
        assertFalse(client.pauseGame(), "연결되지 않은 상태에서 pauseGame은 false");
        assertFalse(client.resumeGame(), "연결되지 않은 상태에서 resumeGame은 false");
        
        // 공격 메시지
        assertFalse(client.sendAttack("opponent", 4), "연결되지 않은 상태에서 sendAttack은 false");
        
        // 보드 업데이트 (모든 필수 매개변수 포함)
        int[][] testBoard = new int[10][20];
        java.util.Queue<int[][]> emptyQueue = new java.util.LinkedList<>();
        assertFalse(client.sendBoardUpdate(testBoard, 5, 2, 1, 0, 2, emptyQueue, 1000, 5, 1), "연결되지 않은 상태에서 sendBoardUpdate는 false");
    }

    @Test
    @DisplayName("극한 상황 테스트")
    void testExtremeConditions() {
        // 매우 큰 보드로 메시지 생성
        int[][] largeBoard = new int[50][100];
        for (int i = 0; i < 50; i++) {
            for (int j = 0; j < 100; j++) {
                largeBoard[i][j] = (i + j) % 8; // 다양한 값으로 채움
            }
        }
        
        assertDoesNotThrow(() -> {
            java.util.Queue<int[][]> largeQueue = new java.util.LinkedList<>();
            assertFalse(client.sendBoardUpdate(largeBoard, 0, 0, 1, 0, 2, largeQueue, Integer.MAX_VALUE, Integer.MAX_VALUE, 10), "큰 보드 메시지도 안전하게 처리");
        }, "큰 데이터 처리도 안전해야 함");
        
        // 유효 범위 내 공격 메시지
        assertDoesNotThrow(() -> {
            AttackMessage bigAttack = AttackMessage.createStandardAttack("TestPlayer", 10);
            assertFalse(client.sendMessage(bigAttack), "큰 공격 메시지도 안전하게 처리");
        }, "극한 공격 메시지도 안전해야 함");
    }

    @Test
    @DisplayName("연속 메시지 전송 테스트")
    void testContinuousMessageSending() {
        for (int i = 0; i < 100; i++) {
            ConnectionMessage msg = ConnectionMessage.createPlayerReady("Player" + i);
            assertFalse(client.sendMessage(msg), "연속 메시지 전송 " + i + "번째 실패");
        }
    }

    @Test
    @DisplayName("빈 문자열이나 특수 문자가 포함된 플레이어 ID 테스트")
    void testSpecialPlayerIds() {
        String[] specialIds = {"", " ", "Player With Spaces", "Player@#$%", "12345", "가나다라마"};
        
        for (String id : specialIds) {
            assertDoesNotThrow(() -> {
                TetrisClient specialClient = new TetrisClient(id);
                assertNotNull(specialClient, "특수 ID로도 클라이언트 생성 가능: " + id);
                assertEquals(id, specialClient.getPlayerId(), "특수 ID도 올바르게 저장됨");
            }, "특수 문자 ID 처리는 안전해야 함: " + id);
        }
    }

    // 테스트용 ClientMessageListener 구현
    private static class TestClientMessageListener implements ClientMessageListener {
        private boolean connectionAccepted = false;
        private String rejectionReason = null;
        private boolean gameStarted = false;
        private String lastError = null;

        @Override
        public void onConnectionAccepted() {
            connectionAccepted = true;
        }

        @Override
        public void onConnectionRejected(String reason) {
            rejectionReason = reason;
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

        @Override
        public void onError(String error) {
            lastError = error;
        }

        @Override
        public void onGameModeSelected(GameModeMessage.GameMode gameMode) {
            // 테스트용 구현
        }

        public boolean isConnectionAccepted() {
            return connectionAccepted;
        }

        public String getRejectionReason() {
            return rejectionReason;
        }

        public boolean isGameStarted() {
            return gameStarted;
        }

        public String getLastError() {
            return lastError;
        }
    }
}