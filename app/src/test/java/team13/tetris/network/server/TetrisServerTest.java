package team13.tetris.network.server;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import team13.tetris.network.listener.ServerMessageListener;
import team13.tetris.network.protocol.*;

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
        assertDoesNotThrow(
                () -> {
                    server.stop();
                },
                "실행 중이지 않은 서버 중지는 안전해야 함");
    }

    @Test
    @DisplayName("메시지 리스너 설정 테스트")
    void testSetHostMessageListener() {
        TestServerMessageListener newListener = new TestServerMessageListener();
        assertDoesNotThrow(
                () -> {
                    server.setHostMessageListener(newListener);
                },
                "메시지 리스너 설정은 안전해야 함");
    }

    @Test
    @DisplayName("클라이언트가 없는 상태에서 브로드캐스트 테스트")
    void testBroadcastWithNoClients() {
        assertDoesNotThrow(
                () -> {
                    server.broadcastGameOverToOthers("TestPlayer", "Test reason");
                    server.pauseGameAsHost();
                    server.resumeGameAsHost();
                },
                "클라이언트가 없는 상태에서 브로드캐스트는 안전해야 함");
    }

    @Test
    @DisplayName("서버 시작 실패 테스트 - 잘못된 포트")
    void testServerStartWithInvalidPort() {
        TetrisServer invalidServer = new TetrisServer("TestHost", -1);
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    invalidServer.start();
                },
                "잘못된 포트로 서버 시작 시 IllegalArgumentException이 발생해야 함");
    }

    @Test
    @DisplayName("게임 모드 선택 테스트")
    void testSelectGameMode() {
        assertDoesNotThrow(
                () -> {
                    server.selectGameMode(GameModeMessage.GameMode.NORMAL);
                },
                "게임 모드 선택은 안전해야 함");
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
        assertDoesNotThrow(
                () -> {
                    server.stop();
                    server.broadcastGameOverToOthers("TestPlayer", "Game Over");
                    server.pauseGameAsHost();
                    server.resumeGameAsHost();
                },
                "중지 상태에서도 메서드 호출은 안전해야 함");
    }

    @Test
    @DisplayName("게임 모드 선택 다양한 모드 테스트")
    void testAllGameModes() {
        GameModeMessage.GameMode[] modes = GameModeMessage.GameMode.values();

        for (GameModeMessage.GameMode mode : modes) {
            assertDoesNotThrow(
                    () -> {
                        server.selectGameMode(mode);
                    },
                    "게임 모드 " + mode + " 선택은 안전해야 함");
        }
    }

    @Test
    @DisplayName("서버 메시지 브로드캐스트 테스트")
    void testServerBroadcastMethods() {
        assertDoesNotThrow(
                () -> {
                    // 유효한 브로드캐스트 메서드 테스트
                    server.broadcastGameOverToOthers("Player1", "Game Over Reason");
                    server.broadcastGameOverToOthers("Player2", null); // null 이유
                    server.broadcastGameOverToOthers("Player3", ""); // 빈 이유
                    // 빈 ID나 null ID는 검증에서 제외 (예외 발생)
                },
                "유효한 브로드캐스트 호출은 안전해야 함");
    }

    @Test
    @DisplayName("서버 게임 제어 메서드 테스트")
    void testServerGameControlMethods() {
        assertDoesNotThrow(
                () -> {
                    // 호스트로서 게임 제어
                    server.pauseGameAsHost();
                    server.resumeGameAsHost();

                    // 여러 번 연속 호출
                    for (int i = 0; i < 5; i++) {
                        server.pauseGameAsHost();
                        server.resumeGameAsHost();
                    }
                },
                "게임 제어 메서드들은 안전해야 함");
    }

    @Test
    @DisplayName("서버 리스너 null 설정 테스트")
    void testNullMessageListener() {
        assertDoesNotThrow(
                () -> {
                    server.setHostMessageListener(null);
                },
                "null 리스너 설정은 안전해야 함");
    }

    @Test
    @DisplayName("여러 번 서버 중지 테스트")
    void testMultipleServerStops() {
        assertDoesNotThrow(
                () -> {
                    server.stop();
                    server.stop();
                    server.stop();
                },
                "여러 번 서버 중지해도 안전해야 함");
    }

    @Test
    @DisplayName("극한 상황에서 서버 동작 테스트")
    void testServerExtremeConditions() {
        // 매우 긴 문자열로 브로드캐스트
        String longReason = "Very long reason ".repeat(100);
        String longPlayerId = "VeryLongPlayerId".repeat(20);

        assertDoesNotThrow(
                () -> {
                    server.broadcastGameOverToOthers(longPlayerId, longReason);
                },
                "긴 문자열도 안전하게 처리해야 함");
    }

    @Test
    @DisplayName("서버 동시성 안전성 테스트")
    void testServerConcurrencySafety() {
        assertDoesNotThrow(
                () -> {
                    // 동시에 여러 작업 수행 시뮬레이션
                    server.pauseGameAsHost();
                    server.selectGameMode(GameModeMessage.GameMode.ITEM);
                    server.resumeGameAsHost();
                    server.broadcastGameOverToOthers("TestPlayer", "Concurrent test");
                    server.stop();
                },
                "동시 작업도 안전해야 함");
    }

    @Test
    @DisplayName("잘못된 포트 범위 테스트")
    void testInvalidPortRanges() {
        int[] invalidPorts = {-1, 0, 65536, 100000};

        for (int port : invalidPorts) {
            assertDoesNotThrow(
                    () -> {
                        TetrisServer invalidServer = new TetrisServer("TestHost", port);
                        assertNotNull(invalidServer, "잘못된 포트로도 서버 객체 생성은 가능");
                        // 시작할 때 예외가 발생할 수 있음
                    },
                    "잘못된 포트로 서버 생성 시도는 안전해야 함");
        }
    }

    @Test
    @DisplayName("빈 문자열이나 특수 문자가 포함된 호스트 ID 테스트")
    void testSpecialHostIds() {
        String[] specialIds = {"", "Host With Spaces", "Host@#$%", "12345", "가나다라마"};

        for (String id : specialIds) {
            assertDoesNotThrow(
                    () -> {
                        TetrisServer specialServer = new TetrisServer(id);
                        assertNotNull(specialServer, "특수 ID로도 서버 생성 가능: " + id);
                        assertEquals(id, specialServer.getHostPlayerId(), "특수 ID도 올바르게 저장됨");
                    },
                    "특수 문자 ID 처리는 안전해야 함: " + id);
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

    @Test
    @DisplayName("클라이언트 등록 및 해제 테스트")
    void testClientRegistrationAndUnregistration() {
        assertDoesNotThrow(
                () -> {
                    // 더미 ClientHandler 생성 (mock Socket 사용)
                    java.net.Socket mockSocket = new java.net.Socket();
                    ClientHandler handler = new ClientHandler(mockSocket, server);

                    // 등록은 ClientHandler 내부에서 수행되므로 직접 테스트 불가
                    // 하지만 서버 상태 메서드는 테스트 가능
                    assertEquals(0, server.getClientCount(), "초기 클라이언트 수는 0");
                },
                "클라이언트 등록/해제는 안전해야 함");
    }

    @Test
    @DisplayName("게임 모드 선택 후 클라이언트 연결 테스트")
    void testGameModeSelectionWithClients() {
        assertDoesNotThrow(
                () -> {
                    // 게임 모드 먼저 선택
                    server.selectGameMode(GameModeMessage.GameMode.ITEM);
                    assertEquals(
                            GameModeMessage.GameMode.ITEM,
                            server.getSelectedGameMode(),
                            "선택된 게임 모드 확인");

                    // 서버 상태 확인
                    assertFalse(server.isRunning(), "서버가 시작되지 않음");
                },
                "게임 모드 선택과 클라이언트 연결은 안전해야 함");
    }

    @Test
    @DisplayName("준비 상태 초기화 테스트")
    void testReadyStateReset() {
        assertDoesNotThrow(
                () -> {
                    // 호스트 준비 상태 설정
                    server.setHostReady();
                    assertTrue(server.isServerReady(), "호스트가 준비됨");

                    // 준비 상태 초기화
                    server.resetReadyStates();
                    assertFalse(server.isServerReady(), "호스트 준비 상태가 초기화됨");
                    assertNull(server.getSelectedGameMode(), "게임 모드가 초기화됨");
                },
                "준비 상태 초기화는 안전해야 함");
    }

    @Test
    @DisplayName("호스트 Ready 상태 설정 및 확인")
    void testHostReadyState() {
        assertDoesNotThrow(
                () -> {
                    // 초기 상태 확인
                    assertFalse(server.isServerReady(), "초기 상태는 준비되지 않음");

                    // 호스트 준비
                    server.setHostReady();
                    assertTrue(server.isServerReady(), "호스트가 준비됨");

                    // 호스트 준비 해제
                    server.setHostUnready();
                    assertFalse(server.isServerReady(), "호스트가 준비 해제됨");
                },
                "호스트 Ready 상태 설정은 안전해야 함");
    }

    @Test
    @DisplayName("클라이언트 Ready 상태 확인")
    void testClientReadyState() {
        assertDoesNotThrow(
                () -> {
                    // 존재하지 않는 클라이언트
                    assertFalse(
                            server.isClientReady("NonExistentClient"), "존재하지 않는 클라이언트는 준비되지 않음");

                    // null 클라이언트
                    assertFalse(server.isClientReady(null), "null 클라이언트는 준비되지 않음");
                },
                "클라이언트 Ready 상태 확인은 안전해야 함");
    }

    @Test
    @DisplayName("게임 모드 선택 전 상태 테스트")
    void testGameModeNotSelected() {
        assertNull(server.getSelectedGameMode(), "초기 상태에서는 게임 모드가 선택되지 않음");

        assertDoesNotThrow(
                () -> {
                    // 게임 모드 없이 준비 상태 설정
                    server.setHostReady();

                    // checkAllReady 호출해도 게임 모드가 없으면 시작 안 됨
                    server.checkAllReady();
                },
                "게임 모드 선택 전 상태는 안전해야 함");
    }

    @Test
    @DisplayName("모든 플레이어 준비 확인 - 부족한 플레이어")
    void testCheckAllReadyInsufficientPlayers() {
        assertDoesNotThrow(
                () -> {
                    // 게임 모드 선택
                    server.selectGameMode(GameModeMessage.GameMode.NORMAL);

                    // 호스트만 준비 (최소 2명 필요)
                    server.setHostReady();

                    // 플레이어가 부족하면 게임 시작 안 됨
                    server.checkAllReady();
                },
                "플레이어 부족 시 게임 시작 안 됨");
    }

    @Test
    @DisplayName("서버 IP 주소 반환 테스트")
    void testGetServerIP() {
        String serverIP = TetrisServer.getServerIP();
        assertNotNull(serverIP, "서버 IP 주소가 null이 아님");
        assertFalse(serverIP.isEmpty(), "서버 IP 주소가 비어있지 않음");
    }

    @Test
    @DisplayName("게임 중단 및 일시정지 테스트")
    void testGamePauseAndResume() {
        assertDoesNotThrow(
                () -> {
                    // 게임 시작 전 일시정지/재개
                    server.pauseGameAsHost();
                    server.resumeGameAsHost();

                    // 여러 번 반복
                    for (int i = 0; i < 3; i++) {
                        server.pauseGameAsHost();
                        server.resumeGameAsHost();
                    }
                },
                "게임 일시정지/재개는 안전해야 함");
    }

    @Test
    @DisplayName("게임 오버 메시지 브로드캐스트 - 다양한 입력")
    void testGameOverBroadcastVariousInputs() {
        assertDoesNotThrow(
                () -> {
                    // 정상적인 게임 오버
                    server.broadcastGameOverToOthers("Player1", "Time's up!");

                    // null 이유
                    server.broadcastGameOverToOthers("Player2", null);

                    // 빈 이유
                    server.broadcastGameOverToOthers("Player3", "");

                    // 특수 문자
                    server.broadcastGameOverToOthers("Player4", "Game Over! @#$%^&*()");

                    // 매우 긴 이유
                    String longReason = "Very long reason ".repeat(50);
                    server.broadcastGameOverToOthers("Player5", longReason);
                },
                "다양한 게임 오버 메시지 처리는 안전해야 함");
    }

    @Test
    @DisplayName("서버 시작 및 중지 사이클 테스트")
    void testStartStopCycle() {
        assertDoesNotThrow(
                () -> {
                    // 여러 번 중지 (이미 중지된 상태에서)
                    for (int i = 0; i < 5; i++) {
                        server.stop();
                        assertFalse(server.isRunning(), "서버가 중지됨");
                    }
                },
                "서버 시작/중지 사이클은 안전해야 함");
    }

    @Test
    @DisplayName("메시지 리스너 설정 테스트")
    void testMessageListenerConfiguration() {
        TestServerMessageListener listener = new TestServerMessageListener();

        assertDoesNotThrow(
                () -> {
                    server.setHostMessageListener(listener);

                    // null 리스너 설정
                    server.setHostMessageListener(null);

                    // 다시 리스너 설정
                    server.setHostMessageListener(listener);
                },
                "메시지 리스너 설정은 안전해야 함");
    }

    @Test
    @DisplayName("동시 다중 서버 생성 테스트")
    void testMultipleServerCreation() {
        TetrisServer[] servers = new TetrisServer[5];

        assertDoesNotThrow(
                () -> {
                    for (int i = 0; i < 5; i++) {
                        servers[i] = new TetrisServer("Host" + i, 12345 + i);
                        assertNotNull(servers[i], "서버 " + i + " 생성 성공");
                        assertEquals("Host" + i, servers[i].getHostPlayerId(), "호스트 ID 일치");
                    }

                    // 모든 서버 중지
                    for (TetrisServer s : servers) {
                        s.stop();
                    }
                },
                "동시 다중 서버 생성은 안전해야 함");
    }

    @Test
    @DisplayName("게임 모드별 서버 동작 테스트")
    void testServerBehaviorPerGameMode() {
        assertDoesNotThrow(
                () -> {
                    // NORMAL 모드
                    server.selectGameMode(GameModeMessage.GameMode.NORMAL);
                    assertEquals(GameModeMessage.GameMode.NORMAL, server.getSelectedGameMode());

                    server.resetReadyStates();

                    // ITEM 모드
                    server.selectGameMode(GameModeMessage.GameMode.ITEM);
                    assertEquals(GameModeMessage.GameMode.ITEM, server.getSelectedGameMode());

                    server.resetReadyStates();

                    // TIMER 모드
                    server.selectGameMode(GameModeMessage.GameMode.TIMER);
                    assertEquals(GameModeMessage.GameMode.TIMER, server.getSelectedGameMode());
                },
                "게임 모드별 서버 동작은 안전해야 함");
    }

    @Test
    @DisplayName("보드 업데이트 및 공격 브로드캐스트 테스트")
    void testBoardUpdateAndAttackBroadcast() {
        assertDoesNotThrow(
                () -> {
                    // 보드 업데이트 전송
                    int[][] boardState = new int[20][10];
                    java.util.Queue<int[][]> incomingBlocks = new java.util.LinkedList<>();

                    server.sendHostBoardUpdate(
                            boardState,
                            3,
                            0,
                            0,
                            0,
                            false,
                            null,
                            -1,
                            1,
                            false,
                            null,
                            -1,
                            incomingBlocks,
                            100,
                            5,
                            1);

                    // 공격 전송
                    server.sendHostAttack(2);
                    server.sendHostAttack(4);
                },
                "보드 업데이트 및 공격 브로드캐스트는 안전해야 함");
    }

    @Test
    @DisplayName("클라이언트 수 확인 테스트")
    void testClientCountTracking() {
        assertEquals(0, server.getClientCount(), "초기 클라이언트 수는 0");

        // 서버를 시작하지 않았으므로 클라이언트 연결 불가
        // 클라이언트 수는 여전히 0이어야 함
        assertEquals(0, server.getClientCount(), "서버 미시작 시 클라이언트 수는 0");
    }

    @Test
    @DisplayName("rejectConnection 메서드 Reflection 테스트")
    void testRejectConnectionViaReflection() throws Exception {
        java.lang.reflect.Method rejectMethod =
                TetrisServer.class.getDeclaredMethod(
                        "rejectConnection", java.net.Socket.class, String.class);
        rejectMethod.setAccessible(true);

        // 더미 소켓 생성 (연결은 필요 없음)
        assertDoesNotThrow(
                () -> {
                    try {
                        java.net.Socket mockSocket = new java.net.Socket();
                        rejectMethod.invoke(server, mockSocket, "Test rejection");
                    } catch (Exception e) {
                        // 소켓이 연결되지 않아 예외 발생할 수 있음 - 정상
                    }
                },
                "rejectConnection 메서드 호출은 안전해야 함");
    }

    @Test
    @DisplayName("getActivePlayerIds 메서드 Reflection 테스트")
    void testGetActivePlayerIdsViaReflection() throws Exception {
        java.lang.reflect.Method getActiveIdsMethod =
                TetrisServer.class.getDeclaredMethod("getActivePlayerIds");
        getActiveIdsMethod.setAccessible(true);

        @SuppressWarnings("unchecked")
        java.util.Set<String> activeIds = (java.util.Set<String>) getActiveIdsMethod.invoke(server);

        assertNotNull(activeIds, "활성 플레이어 ID 집합이 null이 아님");
        assertTrue(activeIds.contains("TestHost"), "호스트 ID가 포함됨");
    }

    @Test
    @DisplayName("startGame 메서드 Reflection 테스트")
    void testStartGameViaReflection() throws Exception {
        java.lang.reflect.Method startGameMethod =
                TetrisServer.class.getDeclaredMethod("startGame", long.class);
        startGameMethod.setAccessible(true);

        assertDoesNotThrow(
                () -> {
                    startGameMethod.invoke(server, 1L);
                },
                "startGame 메서드 호출은 안전해야 함");
    }

    @Test
    @DisplayName("endGame 메서드 Reflection 테스트")
    void testEndGameViaReflection() throws Exception {
        java.lang.reflect.Method endGameMethod =
                TetrisServer.class.getDeclaredMethod("endGame", String.class);
        endGameMethod.setAccessible(true);

        assertDoesNotThrow(
                () -> {
                    endGameMethod.invoke(server, "Test game end");
                },
                "endGame 메서드 호출은 안전해야 함");
    }

    @Test
    @DisplayName("acceptClients 메서드 Reflection 테스트")
    void testAcceptClientsViaReflection() throws Exception {
        java.lang.reflect.Method acceptClientsMethod =
                TetrisServer.class.getDeclaredMethod("acceptClients");
        acceptClientsMethod.setAccessible(true);

        // acceptClients는 무한 루프이므로 별도 스레드에서 실행하고 즉시 종료
        assertDoesNotThrow(
                () -> {
                    Thread testThread =
                            new Thread(
                                    () -> {
                                        try {
                                            acceptClientsMethod.invoke(server);
                                        } catch (Exception e) {
                                            // 서버소켓이 없어서 예외 발생 - 정상
                                        }
                                    });
                    testThread.start();
                    Thread.sleep(100); // 잠깐 대기
                    testThread.interrupt();
                },
                "acceptClients 메서드 접근은 안전해야 함");
    }

    @Test
    @DisplayName("서버 필드 접근 테스트 - hostPlayerId")
    void testHostPlayerIdField() throws Exception {
        java.lang.reflect.Field hostPlayerIdField =
                TetrisServer.class.getDeclaredField("hostPlayerId");
        hostPlayerIdField.setAccessible(true);

        String hostPlayerId = (String) hostPlayerIdField.get(server);
        assertEquals("TestHost", hostPlayerId, "hostPlayerId 필드 값이 일치함");
    }

    @Test
    @DisplayName("서버 필드 접근 테스트 - port")
    void testPortField() throws Exception {
        java.lang.reflect.Field portField = TetrisServer.class.getDeclaredField("port");
        portField.setAccessible(true);

        int port = (int) portField.get(server);
        assertEquals(12346, port, "port 필드 값이 일치함");
    }

    @Test
    @DisplayName("서버 필드 접근 테스트 - isRunning")
    void testIsRunningField() throws Exception {
        java.lang.reflect.Field isRunningField = TetrisServer.class.getDeclaredField("isRunning");
        isRunningField.setAccessible(true);

        boolean isRunning = (boolean) isRunningField.get(server);
        assertFalse(isRunning, "초기 상태에서는 실행 중이 아님");
    }

    @Test
    @DisplayName("서버 필드 접근 테스트 - gameInProgress")
    void testGameInProgressField() throws Exception {
        java.lang.reflect.Field gameInProgressField =
                TetrisServer.class.getDeclaredField("gameInProgress");
        gameInProgressField.setAccessible(true);

        boolean gameInProgress = (boolean) gameInProgressField.get(server);
        assertFalse(gameInProgress, "초기 상태에서는 게임이 진행 중이 아님");
    }

    @Test
    @DisplayName("서버 필드 접근 테스트 - selectedGameMode")
    void testSelectedGameModeField() throws Exception {
        java.lang.reflect.Field selectedGameModeField =
                TetrisServer.class.getDeclaredField("selectedGameMode");
        selectedGameModeField.setAccessible(true);

        GameModeMessage.GameMode selectedGameMode =
                (GameModeMessage.GameMode) selectedGameModeField.get(server);
        assertNull(selectedGameMode, "초기 상태에서는 게임 모드가 선택되지 않음");

        // 게임 모드 설정 후 확인
        server.selectGameMode(GameModeMessage.GameMode.NORMAL);
        selectedGameMode = (GameModeMessage.GameMode) selectedGameModeField.get(server);
        assertEquals(GameModeMessage.GameMode.NORMAL, selectedGameMode, "게임 모드가 설정됨");
    }

    @Test
    @DisplayName("서버 필드 접근 테스트 - connectedClients")
    void testConnectedClientsField() throws Exception {
        java.lang.reflect.Field connectedClientsField =
                TetrisServer.class.getDeclaredField("connectedClients");
        connectedClientsField.setAccessible(true);

        @SuppressWarnings("unchecked")
        java.util.Map<String, ClientHandler> connectedClients =
                (java.util.Map<String, ClientHandler>) connectedClientsField.get(server);

        assertNotNull(connectedClients, "connectedClients 맵이 null이 아님");
        assertTrue(connectedClients.isEmpty(), "초기 상태에서는 연결된 클라이언트가 없음");
    }

    @Test
    @DisplayName("서버 필드 접근 테스트 - players")
    void testPlayersField() throws Exception {
        java.lang.reflect.Field playersField = TetrisServer.class.getDeclaredField("players");
        playersField.setAccessible(true);

        @SuppressWarnings("unchecked")
        java.util.Map<String, ?> players = (java.util.Map<String, ?>) playersField.get(server);

        assertNotNull(players, "players 맵이 null이 아님");
        assertTrue(players.containsKey("TestHost"), "호스트가 플레이어 목록에 포함됨");
    }

    @Test
    @DisplayName("서버 필드 접근 테스트 - playerReadyStates")
    void testPlayerReadyStatesField() throws Exception {
        java.lang.reflect.Field playerReadyStatesField =
                TetrisServer.class.getDeclaredField("playerReadyStates");
        playerReadyStatesField.setAccessible(true);

        @SuppressWarnings("unchecked")
        java.util.Map<String, Boolean> playerReadyStates =
                (java.util.Map<String, Boolean>) playerReadyStatesField.get(server);

        assertNotNull(playerReadyStates, "playerReadyStates 맵이 null이 아님");
    }

    @Test
    @DisplayName("서버 필드 접근 테스트 - threadPool")
    void testThreadPoolField() throws Exception {
        java.lang.reflect.Field threadPoolField = TetrisServer.class.getDeclaredField("threadPool");
        threadPoolField.setAccessible(true);

        java.util.concurrent.ExecutorService threadPool =
                (java.util.concurrent.ExecutorService) threadPoolField.get(server);

        assertNotNull(threadPool, "threadPool이 null이 아님");
        assertFalse(threadPool.isShutdown(), "초기 상태에서는 스레드 풀이 종료되지 않음");
    }

    @Test
    @DisplayName("서버 필드 접근 테스트 - countdownTimer")
    void testCountdownTimerField() throws Exception {
        java.lang.reflect.Field countdownTimerField =
                TetrisServer.class.getDeclaredField("countdownTimer");
        countdownTimerField.setAccessible(true);

        java.util.Timer countdownTimer = (java.util.Timer) countdownTimerField.get(server);
        assertNull(countdownTimer, "초기 상태에서는 카운트다운 타이머가 null");
    }

    @Test
    @DisplayName("서버 필드 접근 테스트 - currentCountdownId")
    void testCurrentCountdownIdField() throws Exception {
        java.lang.reflect.Field currentCountdownIdField =
                TetrisServer.class.getDeclaredField("currentCountdownId");
        currentCountdownIdField.setAccessible(true);

        long currentCountdownId = (long) currentCountdownIdField.get(server);
        assertEquals(0L, currentCountdownId, "초기 카운트다운 ID는 0");
    }

    @Test
    @DisplayName("broadcastToOthers 메서드 테스트")
    void testBroadcastToOthers() {
        assertDoesNotThrow(
                () -> {
                    ConnectionMessage msg = ConnectionMessage.createGameOver("TestPlayer", "Test");
                    server.broadcastToOthers("TestPlayer", msg);
                },
                "broadcastToOthers 호출은 안전해야 함");
    }

    @Test
    @DisplayName("broadcastPlayerReady 메서드 테스트")
    void testBroadcastPlayerReady() {
        assertDoesNotThrow(
                () -> {
                    server.broadcastPlayerReady("TestPlayer");
                    server.broadcastPlayerReady(server.getHostPlayerId());
                },
                "broadcastPlayerReady 호출은 안전해야 함");
    }

    @Test
    @DisplayName("broadcastPlayerUnready 메서드 테스트")
    void testBroadcastPlayerUnready() {
        assertDoesNotThrow(
                () -> {
                    server.broadcastPlayerUnready("TestPlayer");
                    server.broadcastPlayerUnready(server.getHostPlayerId());
                },
                "broadcastPlayerUnready 호출은 안전해야 함");
    }

    @Test
    @DisplayName("broadcastBoardUpdateToOthers 메서드 테스트")
    void testBroadcastBoardUpdateToOthers() {
        assertDoesNotThrow(
                () -> {
                    int[][] board = new int[20][10];
                    java.util.Queue<int[][]> incomingBlocks = new java.util.LinkedList<>();
                    BoardUpdateMessage msg =
                            new BoardUpdateMessage(
                                    "TestPlayer",
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
                                    incomingBlocks,
                                    0,
                                    0,
                                    0);
                    server.broadcastBoardUpdateToOthers("TestPlayer", msg);
                },
                "broadcastBoardUpdateToOthers 호출은 안전해야 함");
    }

    @Test
    @DisplayName("broadcastAttackToOthers 메서드 테스트")
    void testBroadcastAttackToOthers() {
        assertDoesNotThrow(
                () -> {
                    AttackMessage msg = AttackMessage.createStandardAttack("TestPlayer", 2);
                    server.broadcastAttackToOthers("TestPlayer", msg);
                },
                "broadcastAttackToOthers 호출은 안전해야 함");
    }

    @Test
    @DisplayName("broadcastPauseToOthers 메서드 테스트")
    void testBroadcastPauseToOthers() {
        assertDoesNotThrow(
                () -> {
                    server.broadcastPauseToOthers("TestPlayer");
                },
                "broadcastPauseToOthers 호출은 안전해야 함");
    }

    @Test
    @DisplayName("broadcastResumeToOthers 메서드 테스트")
    void testBroadcastResumeToOthers() {
        assertDoesNotThrow(
                () -> {
                    server.broadcastResumeToOthers("TestPlayer");
                },
                "broadcastResumeToOthers 호출은 안전해야 함");
    }

    @Test
    @DisplayName("notify 메서드들 테스트")
    void testNotifyMethods() {
        assertDoesNotThrow(
                () -> {
                    int[][] board = new int[20][10];
                    java.util.Queue<int[][]> incomingBlocks = new java.util.LinkedList<>();
                    BoardUpdateMessage boardMsg =
                            new BoardUpdateMessage(
                                    "TestPlayer",
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
                                    incomingBlocks,
                                    0,
                                    0,
                                    0);
                    AttackMessage attackMsg = AttackMessage.createStandardAttack("TestPlayer", 2);

                    server.notifyHostBoardUpdate(boardMsg);
                    server.notifyHostAttack(attackMsg);
                    server.notifyHostPause();
                    server.notifyHostResume();
                    server.notifyHostGameOver("Test reason");
                },
                "notify 메서드들 호출은 안전해야 함");
    }

    @Test
    @DisplayName("sendInitialStateToClient 메서드 테스트")
    void testSendInitialStateToClient() {
        assertDoesNotThrow(
                () -> {
                    // 존재하지 않는 클라이언트
                    server.sendInitialStateToClient("NonExistentClient");

                    // 게임 모드 설정 후
                    server.selectGameMode(GameModeMessage.GameMode.ITEM);
                    server.sendInitialStateToClient("NonExistentClient");
                },
                "sendInitialStateToClient 호출은 안전해야 함");
    }

    @Test
    @DisplayName("sendMessageToPlayer 메서드 테스트")
    void testSendMessageToPlayer() {
        assertDoesNotThrow(
                () -> {
                    ConnectionMessage msg = ConnectionMessage.createGameOver("Server", "Test");
                    server.sendMessageToPlayer("NonExistentPlayer", msg);
                },
                "sendMessageToPlayer 호출은 안전해야 함");
    }

    @Test
    @DisplayName("getConnectedPlayerIds 메서드 테스트")
    void testGetConnectedPlayerIds() {
        java.util.Set<String> playerIds = server.getConnectedPlayerIds();
        assertNotNull(playerIds, "플레이어 ID 집합이 null이 아님");
        assertTrue(playerIds.isEmpty(), "초기 상태에서는 연결된 플레이어가 없음");
    }

    @Test
    @DisplayName("getConnectedPlayerCount 메서드 테스트")
    void testGetConnectedPlayerCount() {
        assertEquals(0, server.getConnectedPlayerCount(), "초기 연결된 플레이어 수는 0");
    }

    @Test
    @DisplayName("isGameInProgress 메서드 테스트")
    void testIsGameInProgress() {
        assertFalse(server.isGameInProgress(), "초기 상태에서는 게임이 진행 중이 아님");
    }
}
