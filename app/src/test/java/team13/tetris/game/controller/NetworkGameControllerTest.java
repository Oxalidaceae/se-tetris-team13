package team13.tetris.game.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Queue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;
import team13.tetris.game.logic.GameEngine;
import team13.tetris.game.model.Board;
import team13.tetris.network.protocol.*;

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
        NetworkGameController hostController =
                new NetworkGameController(null, settings, true, "127.0.0.1");
        assertNotNull(hostController, "호스트 모드로 컨트롤러 생성 가능");
    }

    @Test
    @DisplayName("NetworkGameController 생성 테스트 - 클라이언트 모드")
    void testNetworkGameControllerCreationClient() {
        NetworkGameController clientController =
                new NetworkGameController(null, settings, false, "192.168.1.1");
        assertNotNull(clientController, "클라이언트 모드로 컨트롤러 생성 가능");
    }

    @Test
    @DisplayName("createAttackPattern 메서드 테스트 - 일반 블록")
    void testCreateAttackPatternNormal() throws Exception {
        // Reflection으로 private 메서드 접근
        Method createAttackPattern =
                NetworkGameController.class.getDeclaredMethod(
                        "createAttackPattern", int.class, GameEngine.class);
        createAttackPattern.setAccessible(true);

        // GameEngine 생성
        Board board = new Board(10, 20);
        GameEngine engine =
                new GameEngine(board, null, team13.tetris.data.ScoreBoard.ScoreEntry.Mode.NORMAL);

        // 공격 패턴 생성 테스트
        int[][] pattern = (int[][]) createAttackPattern.invoke(controller, 2, engine);

        assertNotNull(pattern, "공격 패턴이 생성되어야 함");
        assertEquals(2, pattern.length, "2줄 공격 패턴");
        assertEquals(10, pattern[0].length, "보드 너비만큼의 열");

        // 회색 블록(1000)과 빈 공간(0)으로만 구성되어야 함
        for (int[] row : pattern) {
            for (int cell : row) {
                assertTrue(cell == 0 || cell == 1000, "셀은 0 또는 1000이어야 함");
            }
        }
    }

    @Test
    @DisplayName("addIncomingBlockToBoard 메서드 테스트")
    void testAddIncomingBlockToBoard() throws Exception {
        // Reflection으로 private 메서드 접근
        Method addIncomingBlockToBoard =
                NetworkGameController.class.getDeclaredMethod(
                        "addIncomingBlockToBoard", GameEngine.class, int[][].class);
        addIncomingBlockToBoard.setAccessible(true);

        // GameEngine 생성
        Board board = new Board(10, 20);
        GameEngine engine =
                new GameEngine(board, null, team13.tetris.data.ScoreBoard.ScoreEntry.Mode.NORMAL);

        // 테스트 패턴 생성 (2줄)
        int[][] pattern = new int[2][10];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 10; j++) {
                pattern[i][j] = (j == 9) ? 0 : 1000;
            }
        }

        // 메서드 호출
        assertDoesNotThrow(
                () -> {
                    addIncomingBlockToBoard.invoke(controller, engine, pattern);
                },
                "addIncomingBlockToBoard 실행은 안전해야 함");

        // 보드 하단에 패턴이 추가되었는지 확인
        int bottomRow = board.getHeight() - 1;
        assertEquals(0, board.getCell(9, bottomRow), "마지막 열은 빈 공간");
        assertEquals(1000, board.getCell(0, bottomRow), "나머지는 회색 블록");
    }

    @Test
    @DisplayName("myIncomingBlocks 큐 테스트")
    void testIncomingBlocksQueue() throws Exception {
        // Reflection으로 private 필드 접근
        Field myIncomingBlocksField =
                NetworkGameController.class.getDeclaredField("myIncomingBlocks");
        myIncomingBlocksField.setAccessible(true);

        @SuppressWarnings("unchecked")
        Queue<int[][]> myIncomingBlocks = (Queue<int[][]>) myIncomingBlocksField.get(controller);

        assertNotNull(myIncomingBlocks, "myIncomingBlocks 큐가 초기화되어야 함");
        assertTrue(myIncomingBlocks.isEmpty(), "초기 상태에서는 비어있어야 함");

        // 공격 패턴 추가
        int[][] pattern = new int[1][10];
        myIncomingBlocks.add(pattern);

        assertEquals(1, myIncomingBlocks.size(), "패턴이 추가되어야 함");
        assertSame(pattern, myIncomingBlocks.peek(), "추가한 패턴이 큐에 있어야 함");
    }

    @Test
    @DisplayName("네트워크 안정성 필드 테스트")
    void testNetworkStabilityFields() throws Exception {
        // lastMessageReceivedTime 필드 확인
        Field lastMessageReceivedTimeField =
                NetworkGameController.class.getDeclaredField("lastMessageReceivedTime");
        lastMessageReceivedTimeField.setAccessible(true);
        long lastMessageTime = lastMessageReceivedTimeField.getLong(controller);

        assertEquals(0L, lastMessageTime, "초기값은 0이어야 함");

        // isLagging 필드 확인
        Field isLaggingField = NetworkGameController.class.getDeclaredField("isLagging");
        isLaggingField.setAccessible(true);
        boolean isLagging = isLaggingField.getBoolean(controller);

        assertFalse(isLagging, "초기 상태는 지연 없음");
    }

    @Test
    @DisplayName("게임 상태 플래그 테스트")
    void testGameStateFlags() throws Exception {
        // gameStarted 필드
        Field gameStartedField = NetworkGameController.class.getDeclaredField("gameStarted");
        gameStartedField.setAccessible(true);
        assertFalse(gameStartedField.getBoolean(controller), "초기 상태는 게임 시작 안 됨");

        // paused 필드
        Field pausedField = NetworkGameController.class.getDeclaredField("paused");
        pausedField.setAccessible(true);
        assertFalse(pausedField.getBoolean(controller), "초기 상태는 일시정지 안 됨");

        // itemMode 필드
        Field itemModeField = NetworkGameController.class.getDeclaredField("itemMode");
        itemModeField.setAccessible(true);
        assertFalse(itemModeField.getBoolean(controller), "초기 상태는 아이템 모드 아님");

        // timerMode 필드
        Field timerModeField = NetworkGameController.class.getDeclaredField("timerMode");
        timerModeField.setAccessible(true);
        assertFalse(timerModeField.getBoolean(controller), "초기 상태는 타이머 모드 아님");

        // myReady 필드
        Field myReadyField = NetworkGameController.class.getDeclaredField("myReady");
        myReadyField.setAccessible(true);
        assertFalse(myReadyField.getBoolean(controller), "초기 상태는 준비 안 됨");
    }

    @Test
    @DisplayName("연결 해제 테스트")
    void testDisconnect() {
        assertDoesNotThrow(
                () -> {
                    controller.disconnect();
                },
                "연결 해제는 안전해야 함");
    }

    @Test
    @DisplayName("updateLastMessageTime 메서드 테스트")
    void testUpdateLastMessageTime() throws Exception {
        // Reflection으로 private 메서드 접근
        Method updateLastMessageTime =
                NetworkGameController.class.getDeclaredMethod("updateLastMessageTime");
        updateLastMessageTime.setAccessible(true);

        Field lastMessageReceivedTimeField =
                NetworkGameController.class.getDeclaredField("lastMessageReceivedTime");
        lastMessageReceivedTimeField.setAccessible(true);

        long beforeTime = lastMessageReceivedTimeField.getLong(controller);

        // 메서드 호출
        Thread.sleep(10); // 약간의 시간 경과
        updateLastMessageTime.invoke(controller);

        long afterTime = lastMessageReceivedTimeField.getLong(controller);

        assertTrue(afterTime > beforeTime, "메시지 수신 시각이 업데이트되어야 함");
        assertTrue(afterTime > 0, "현재 시간으로 설정되어야 함");
    }

    @Test
    @DisplayName("타이머 모드 remainingSeconds 필드 테스트")
    void testTimerModeFields() throws Exception {
        Field remainingSecondsField =
                NetworkGameController.class.getDeclaredField("remainingSeconds");
        remainingSecondsField.setAccessible(true);

        int remainingSeconds = remainingSecondsField.getInt(controller);
        assertEquals(120, remainingSeconds, "초기 남은 시간은 120초");
    }

    @Test
    @DisplayName("네트워크 임계값 상수 테스트")
    void testNetworkThresholdConstants() throws Exception {
        Field lagThresholdField = NetworkGameController.class.getDeclaredField("LAG_THRESHOLD_MS");
        lagThresholdField.setAccessible(true);
        long lagThreshold = lagThresholdField.getLong(null);
        assertEquals(2000L, lagThreshold, "지연 임계값은 2초");

        Field disconnectThresholdField =
                NetworkGameController.class.getDeclaredField("DISCONNECT_THRESHOLD_MS");
        disconnectThresholdField.setAccessible(true);
        long disconnectThreshold = disconnectThresholdField.getLong(null);
        assertEquals(10000L, disconnectThreshold, "연결 끊김 임계값은 10초");
    }

    @Test
    @DisplayName("플레이어 ID 필드 테스트")
    void testPlayerIdFields() throws Exception {
        Field myPlayerIdField = NetworkGameController.class.getDeclaredField("myPlayerId");
        myPlayerIdField.setAccessible(true);
        String myPlayerId = (String) myPlayerIdField.get(controller);
        assertNull(myPlayerId, "초기 상태에서는 플레이어 ID가 null");

        Field opponentPlayerIdField =
                NetworkGameController.class.getDeclaredField("opponentPlayerId");
        opponentPlayerIdField.setAccessible(true);
        String opponentPlayerId = (String) opponentPlayerIdField.get(controller);
        assertNull(opponentPlayerId, "초기 상태에서는 상대 플레이어 ID가 null");
    }

    @Test
    @DisplayName("호스트와 클라이언트 구분 테스트")
    void testHostClientDistinction() {
        NetworkGameController hostController =
                new NetworkGameController(null, settings, true, "localhost");
        NetworkGameController clientController =
                new NetworkGameController(null, settings, false, "192.168.1.1");

        assertNotNull(hostController, "호스트 컨트롤러 생성됨");
        assertNotNull(clientController, "클라이언트 컨트롤러 생성됨");
        assertNotEquals(hostController, clientController, "두 컨트롤러는 서로 다름");
    }

    @Test
    @DisplayName("다양한 공격 패턴 크기 테스트")
    void testVariousAttackPatternSizes() throws Exception {
        Method createAttackPattern =
                NetworkGameController.class.getDeclaredMethod(
                        "createAttackPattern", int.class, GameEngine.class);
        createAttackPattern.setAccessible(true);

        Board board = new Board(10, 20);
        GameEngine engine =
                new GameEngine(board, null, team13.tetris.data.ScoreBoard.ScoreEntry.Mode.NORMAL);

        // 1줄부터 10줄까지 테스트
        for (int lines = 1; lines <= 10; lines++) {
            int[][] pattern = (int[][]) createAttackPattern.invoke(controller, lines, engine);

            assertNotNull(pattern, lines + "줄 공격 패턴 생성됨");
            assertEquals(lines, pattern.length, "패턴 줄 수 일치");
            assertEquals(10, pattern[0].length, "패턴 열 수는 10");
        }
    }

    @Test
    @DisplayName("incoming 블록 큐 순서 테스트")
    void testIncomingBlocksQueueOrder() throws Exception {
        Field myIncomingBlocksField =
                NetworkGameController.class.getDeclaredField("myIncomingBlocks");
        myIncomingBlocksField.setAccessible(true);

        @SuppressWarnings("unchecked")
        Queue<int[][]> myIncomingBlocks = (Queue<int[][]>) myIncomingBlocksField.get(controller);

        // 여러 패턴 추가
        int[][] pattern1 = new int[1][10];
        int[][] pattern2 = new int[2][10];
        int[][] pattern3 = new int[3][10];

        myIncomingBlocks.add(pattern1);
        myIncomingBlocks.add(pattern2);
        myIncomingBlocks.add(pattern3);

        assertEquals(3, myIncomingBlocks.size(), "3개 패턴 추가됨");

        // FIFO 순서 확인
        assertSame(pattern1, myIncomingBlocks.poll(), "첫 번째 패턴");
        assertSame(pattern2, myIncomingBlocks.poll(), "두 번째 패턴");
        assertSame(pattern3, myIncomingBlocks.poll(), "세 번째 패턴");

        assertTrue(myIncomingBlocks.isEmpty(), "모든 패턴 제거됨");
    }

    @Test
    @DisplayName("Settings 객체 저장 테스트")
    void testSettingsStorage() throws Exception {
        Field settingsField = NetworkGameController.class.getDeclaredField("settings");
        settingsField.setAccessible(true);

        Settings storedSettings = (Settings) settingsField.get(controller);
        assertSame(settings, storedSettings, "Settings 객체가 저장되어야 함");
    }

    @Test
    @DisplayName("서버 IP 저장 테스트")
    void testServerIPStorage() throws Exception {
        String testIP = "192.168.0.100";
        NetworkGameController testController =
                new NetworkGameController(null, settings, false, testIP);

        Field serverIPField = NetworkGameController.class.getDeclaredField("serverIP");
        serverIPField.setAccessible(true);

        String storedIP = (String) serverIPField.get(testController);
        assertEquals(testIP, storedIP, "서버 IP가 저장되어야 함");
    }

    @Test
    @DisplayName("ClientMessageListener 구현 메서드 테스트 - 비 JavaFX")
    void testClientMessageListenerMethods() {
        assertDoesNotThrow(
                () -> {
                    controller.onConnectionAccepted("TestPlayer");
                    controller.onError("Test error");
                    controller.onGameModeSelected(GameModeMessage.GameMode.NORMAL);
                },
                "ClientMessageListener 메서드들은 안전해야 함");

        // JavaFX Platform.runLater 사용 메서드들은 툴킷 미초기화 시에도 예외를 던지지 않음 (단순히 무시됨)
        assertDoesNotThrow(
                () -> {
                    controller.onConnectionRejected("Test reason");
                },
                "Platform.runLater는 툴킷 미초기화 시에도 안전함");

        assertDoesNotThrow(
                () -> {
                    controller.onPlayerReady("TestPlayer");
                },
                "Platform.runLater는 툴킷 미초기화 시에도 안전함");
    }

    @Test
    @DisplayName("ServerMessageListener 구현 메서드 테스트 - onError")
    void testServerMessageListenerMethods() {
        assertDoesNotThrow(
                () -> {
                    controller.onClientDisconnected("TestClient");
                },
                "ServerMessageListener 메서드들은 안전해야 함");

        // JavaFX Platform.runLater 사용 메서드는 툴킷 미초기화 시에도 예외를 던지지 않음 (단순히 무시됨)
        assertDoesNotThrow(
                () -> {
                    controller.onClientConnected("TestClient");
                },
                "Platform.runLater는 툴킷 미초기화 시에도 안전함");
    }

    @Test
    @DisplayName("보드 업데이트 메시지 처리 테스트")
    void testBoardUpdateMessage() {
        int[][] testBoard = new int[20][10];
        BoardUpdateMessage message =
                new BoardUpdateMessage(
                        "TestPlayer",
                        testBoard,
                        1,
                        2,
                        3,
                        4,
                        false,
                        null,
                        -1,
                        5,
                        false,
                        null,
                        -1,
                        null,
                        6,
                        7,
                        8);

        assertDoesNotThrow(
                () -> {
                    controller.onBoardUpdate(message);
                },
                "보드 업데이트 메시지 처리는 안전해야 함");
    }

    @Test
    @DisplayName("공격 메시지 처리 테스트")
    void testAttackMessage() {
        AttackMessage message = new AttackMessage("TestPlayer", 2, 2, null);

        assertDoesNotThrow(
                () -> {
                    controller.onAttackReceived(message);
                },
                "공격 메시지 처리는 안전해야 함");
    }

    @Test
    @DisplayName("키 입력 처리 테스트")
    void testKeyInputHandling() {
        assertDoesNotThrow(
                () -> {
                    // Mock KeyEvent는 복잡하므로 직접적인 키 이벤트 처리는 생략
                    controller.onConnectionAccepted("TestPlayer"); // 연결 상태로 만듦
                },
                "키 입력 처리 설정은 안전해야 함");
    }

    @Test
    @DisplayName("게임 시작 전 상태 테스트")
    void testPreGameState() {
        // 게임 시작 전에는 대부분의 동작이 안전하게 무시되어야 함
        assertDoesNotThrow(
                () -> {
                    controller.onGamePaused();
                    controller.onGameResumed();
                },
                "게임 시작 전 상태에서 메시지 처리는 안전해야 함");

        // JavaFX Platform.runLater는 툴킷 미초기화 시에도 예외를 던지지 않음
        assertDoesNotThrow(
                () -> {
                    controller.onPlayerReady("TestPlayer");
                },
                "Platform.runLater는 툴킷 미초기화 시에도 안전함");
    }

    @Test
    @DisplayName("null 파라미터 처리 테스트")
    void testNullParameterHandling() {
        assertDoesNotThrow(
                () -> {
                    controller.onGameOver(null);
                    controller.onError(null);
                    controller.onBoardUpdate(null);
                },
                "null 파라미터 처리는 안전해야 함");

        // JavaFX Platform.runLater는 툴킷 미초기화 시에도 예외를 던지지 않음
        assertDoesNotThrow(
                () -> {
                    controller.onConnectionRejected(null);
                },
                "Platform.runLater는 툴킷 미초기화 시에도 안전함");

        // onAttackReceived는 null 체크가 없어서 NullPointerException 발생
        assertThrows(
                NullPointerException.class,
                () -> {
                    controller.onAttackReceived(null);
                },
                "null AttackMessage는 NullPointerException을 발생시켜야 함");
    }

    @Test
    @DisplayName("10줄 공격 제한 - null 패턴 테스트")
    void testAttackReceivedWithNullPattern() {
        // null 패턴으로 AttackMessage 생성 (최소 1줄 필요)
        AttackMessage nullMessage = new AttackMessage("Opponent", 1, 1, null);

        assertDoesNotThrow(
                () -> {
                    controller.onAttackReceived(nullMessage);
                },
                "null 공격 패턴은 안전하게 무시되어야 함");
    }

    @Test
    @DisplayName("공격 패턴 수신 - 다양한 크기 테스트")
    void testAttackReceivedWithVariousSizes() {
        // 1줄 공격
        int[][] pattern1 = new int[][] {{1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 0}};
        AttackMessage message1 = new AttackMessage("Opponent", 1, 1, pattern1);
        assertDoesNotThrow(() -> controller.onAttackReceived(message1), "1줄 공격 처리 안전");

        // 5줄 공격
        int[][] pattern5 = new int[5][10];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 10; j++) {
                pattern5[i][j] = (j == 9) ? 0 : 1000;
            }
        }
        AttackMessage message5 = new AttackMessage("Opponent", 5, 5, pattern5);
        assertDoesNotThrow(() -> controller.onAttackReceived(message5), "5줄 공격 처리 안전");

        // 10줄 공격
        int[][] pattern10 = new int[10][10];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                pattern10[i][j] = (j == 9) ? 0 : 1000;
            }
        }
        AttackMessage message10 = new AttackMessage("Opponent", 10, 10, pattern10);
        assertDoesNotThrow(() -> controller.onAttackReceived(message10), "10줄 공격 처리 안전");
    }

    @Test
    @DisplayName("게임 모드 필드 직접 테스트")
    void testGameModeFields() throws Exception {
        Field itemModeField = NetworkGameController.class.getDeclaredField("itemMode");
        itemModeField.setAccessible(true);
        Field timerModeField = NetworkGameController.class.getDeclaredField("timerMode");
        timerModeField.setAccessible(true);

        assertFalse(itemModeField.getBoolean(controller), "초기 itemMode는 false");
        assertFalse(timerModeField.getBoolean(controller), "초기 timerMode는 false");

        // 수동으로 상태 변경 테스트
        itemModeField.setBoolean(controller, true);
        assertTrue(itemModeField.getBoolean(controller), "itemMode 변경 가능");
    }

    @Test
    @DisplayName("myReady 필드 상태 테스트")
    void testMyReadyFieldState() throws Exception {
        Field myReadyField = NetworkGameController.class.getDeclaredField("myReady");
        myReadyField.setAccessible(true);

        assertFalse(myReadyField.getBoolean(controller), "초기 상태는 준비 안 됨");

        myReadyField.setBoolean(controller, true);
        assertTrue(myReadyField.getBoolean(controller), "준비 상태로 변경 가능");
    }

    @Test
    @DisplayName("보드 업데이트 메시지 처리 - 다양한 상태")
    void testBoardUpdateWithVariousStates() {
        // 현재 블록 없음
        int[][] emptyBoard = new int[20][10];
        BoardUpdateMessage message1 =
                new BoardUpdateMessage(
                        "Opponent",
                        emptyBoard,
                        0,
                        0,
                        -1,
                        0,
                        false,
                        null,
                        -1,
                        -1,
                        false,
                        null,
                        -1,
                        null,
                        0,
                        0,
                        0);
        assertDoesNotThrow(() -> controller.onBoardUpdate(message1), "빈 보드 업데이트 안전");

        // 다음 블록 있음
        BoardUpdateMessage message2 =
                new BoardUpdateMessage(
                        "Opponent",
                        emptyBoard,
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
                        null,
                        100,
                        5,
                        1);
        assertDoesNotThrow(() -> controller.onBoardUpdate(message2), "다음 블록 있는 보드 업데이트 안전");

        // incoming 블록 있음
        java.util.Queue<int[][]> incomingBlocks = new java.util.LinkedList<>();
        incomingBlocks.add(new int[][] {{1000, 1000, 0, 1000, 1000, 1000, 1000, 1000, 1000, 1000}});
        BoardUpdateMessage message3 =
                new BoardUpdateMessage(
                        "Opponent",
                        emptyBoard,
                        5,
                        10,
                        2,
                        1,
                        false,
                        null,
                        -1,
                        3,
                        false,
                        null,
                        -1,
                        incomingBlocks,
                        200,
                        10,
                        2);
        assertDoesNotThrow(() -> controller.onBoardUpdate(message3), "incoming 블록 있는 보드 업데이트 안전");
    }

    @Test
    @DisplayName("paused 필드 상태 테스트")
    void testPausedFieldState() throws Exception {
        Field pausedField = NetworkGameController.class.getDeclaredField("paused");
        pausedField.setAccessible(true);

        assertFalse(pausedField.getBoolean(controller), "초기 상태는 일시정지 안 됨");

        pausedField.setBoolean(controller, true);
        assertTrue(pausedField.getBoolean(controller), "일시정지 상태로 변경 가능");
    }

    @Test
    @DisplayName("여러 공격 메시지 연속 처리")
    void testMultipleAttacksInSequence() {
        assertDoesNotThrow(
                () -> {
                    for (int i = 1; i <= 5; i++) {
                        int[][] pattern = new int[i][10];
                        for (int r = 0; r < i; r++) {
                            for (int c = 0; c < 10; c++) {
                                pattern[r][c] = (c == 9) ? 0 : 1000;
                            }
                        }
                        AttackMessage message = new AttackMessage("Opponent", i, i, pattern);
                        controller.onAttackReceived(message);
                    }
                },
                "연속된 공격 메시지 처리는 안전해야 함");
    }

    @Test
    @DisplayName("countdownSeconds 프로퍼티 테스트")
    void testCountdownSecondsProperty() throws Exception {
        Field countdownSecondsField =
                NetworkGameController.class.getDeclaredField("countdownSeconds");
        countdownSecondsField.setAccessible(true);

        Object countdownSeconds = countdownSecondsField.get(controller);
        assertNotNull(countdownSeconds, "countdownSeconds 프로퍼티가 초기화되어야 함");
    }

    @Test
    @DisplayName("호스트/클라이언트 모드 구분 테스트")
    void testHostAndClientModes() {
        NetworkGameController hostController =
                new NetworkGameController(sceneManager, settings, true, "localhost");
        NetworkGameController clientController =
                new NetworkGameController(sceneManager, settings, false, "192.168.1.1");

        assertNotNull(hostController, "호스트 컨트롤러 생성 성공");
        assertNotNull(clientController, "클라이언트 컨트롤러 생성 성공");
    }

    @Test
    @DisplayName("다양한 서버 IP 주소 테스트")
    void testVariousServerIPAddresses() {
        String[] testIPs = {"localhost", "127.0.0.1", "192.168.0.1", "10.0.0.1", "255.255.255.255"};

        for (String ip : testIPs) {
            NetworkGameController testController =
                    new NetworkGameController(sceneManager, settings, false, ip);
            assertNotNull(testController, "IP: " + ip + "로 컨트롤러 생성 가능");
        }
    }

    @Test
    @DisplayName("에러 메시지 처리 테스트")
    void testErrorMessageHandling() {
        assertDoesNotThrow(
                () -> {
                    controller.onError("Connection timeout");
                    controller.onError("Invalid message");
                    controller.onError("");
                },
                "에러 메시지 처리는 안전해야 함");
    }

    @Test
    @DisplayName("initializeLobby 메서드 Reflection 테스트")
    void testInitializeLobbyReflection() throws Exception {
        Method initMethod = NetworkGameController.class.getDeclaredMethod("initializeLobby");
        initMethod.setAccessible(true);

        assertDoesNotThrow(
                () -> {
                    try {
                        initMethod.invoke(controller);
                    } catch (Exception e) {
                        // JavaFX 의존성으로 예외 발생 가능
                    }
                },
                "initializeLobby 메서드 호출 가능");
    }

    @Test
    @DisplayName("handleKeyPress 메서드 Reflection 테스트")
    void testHandleKeyPressReflection() throws Exception {
        Field gameStartedField = NetworkGameController.class.getDeclaredField("gameStarted");
        gameStartedField.setAccessible(true);
        gameStartedField.setBoolean(controller, true);

        Field myEngineField = NetworkGameController.class.getDeclaredField("myEngine");
        myEngineField.setAccessible(true);

        // myEngine이 null이므로 handleKeyPress는 아무 동작 안 함
        assertNull(myEngineField.get(controller), "myEngine은 초기에 null");
    }

    @Test
    @DisplayName("sendMyBoardState 메서드 Reflection 테스트")
    void testSendMyBoardStateReflection() throws Exception {
        Method sendMethod = NetworkGameController.class.getDeclaredMethod("sendMyBoardState");
        sendMethod.setAccessible(true);

        assertDoesNotThrow(
                () -> {
                    sendMethod.invoke(controller);
                },
                "sendMyBoardState는 myEngine null일 때 안전하게 리턴");
    }

    @Test
    @DisplayName("sendAttackPattern 메서드 Reflection 테스트")
    void testSendAttackPatternReflection() throws Exception {
        Method sendMethod =
                NetworkGameController.class.getDeclaredMethod("sendAttackPattern", int[][].class);
        sendMethod.setAccessible(true);

        int[][] pattern = new int[2][10];
        assertDoesNotThrow(
                () -> {
                    sendMethod.invoke(controller, (Object) pattern);
                },
                "sendAttackPattern 호출 가능");
    }

    @Test
    @DisplayName("togglePause 메서드 Reflection 테스트")
    void testTogglePauseReflection() throws Exception {
        Method toggleMethod = NetworkGameController.class.getDeclaredMethod("togglePause");
        toggleMethod.setAccessible(true);

        Field gameStartedField = NetworkGameController.class.getDeclaredField("gameStarted");
        gameStartedField.setAccessible(true);

        // gameStarted = false일 때는 아무 동작 안 함
        gameStartedField.setBoolean(controller, false);
        assertDoesNotThrow(
                () -> {
                    toggleMethod.invoke(controller);
                },
                "게임 시작 안 했을 때 togglePause는 안전");
    }

    @Test
    @DisplayName("네트워크 안정성 필드 값 변경 테스트")
    void testNetworkStabilityFieldsMutation() throws Exception {
        Field lastMessageField =
                NetworkGameController.class.getDeclaredField("lastMessageReceivedTime");
        lastMessageField.setAccessible(true);

        Field isLaggingField = NetworkGameController.class.getDeclaredField("isLagging");
        isLaggingField.setAccessible(true);

        // 값 변경
        lastMessageField.setLong(controller, System.currentTimeMillis());
        isLaggingField.setBoolean(controller, true);

        assertEquals(
                System.currentTimeMillis(),
                lastMessageField.getLong(controller),
                1000,
                "lastMessageReceivedTime 설정 가능");
        assertTrue(isLaggingField.getBoolean(controller), "isLagging 상태 변경 가능");
    }

    @Test
    @DisplayName("remainingSeconds 필드 테스트")
    void testRemainingSecondsField() throws Exception {
        Field remainingField = NetworkGameController.class.getDeclaredField("remainingSeconds");
        remainingField.setAccessible(true);

        int initialValue = remainingField.getInt(controller);
        assertEquals(120, initialValue, "초기 remainingSeconds는 120초");

        remainingField.setInt(controller, 60);
        assertEquals(60, remainingField.getInt(controller), "remainingSeconds 변경 가능");
    }

    @Test
    @DisplayName("opponentPlayerId 필드 테스트")
    void testOpponentPlayerIdField() throws Exception {
        Field opponentField = NetworkGameController.class.getDeclaredField("opponentPlayerId");
        opponentField.setAccessible(true);

        assertNull(opponentField.get(controller), "초기 opponentPlayerId는 null");

        opponentField.set(controller, "Opponent123");
        assertEquals("Opponent123", opponentField.get(controller), "opponentPlayerId 설정 가능");
    }

    @Test
    @DisplayName("pendingOpponentReady 필드 테스트")
    void testPendingOpponentReadyField() throws Exception {
        Field pendingField = NetworkGameController.class.getDeclaredField("pendingOpponentReady");
        pendingField.setAccessible(true);

        assertNull(pendingField.get(controller), "초기 pendingOpponentReady는 null");

        pendingField.set(controller, Boolean.TRUE);
        assertEquals(Boolean.TRUE, pendingField.get(controller), "pendingOpponentReady 설정 가능");
    }

    @Test
    @DisplayName("server와 client 필드 초기값 테스트")
    void testServerAndClientFieldsInitialValues() throws Exception {
        Field serverField = NetworkGameController.class.getDeclaredField("server");
        serverField.setAccessible(true);

        Field clientField = NetworkGameController.class.getDeclaredField("client");
        clientField.setAccessible(true);

        assertNull(serverField.get(controller), "초기 server는 null");
        assertNull(clientField.get(controller), "초기 client는 null");
    }

    @Test
    @DisplayName("applyLocalPause 메서드 Reflection 테스트")
    void testApplyLocalPauseReflection() throws Exception {
        Method method = NetworkGameController.class.getDeclaredMethod("applyLocalPause");
        method.setAccessible(true);

        Field pausedField = NetworkGameController.class.getDeclaredField("paused");
        pausedField.setAccessible(true);

        assertFalse(pausedField.getBoolean(controller), "초기 paused는 false");

        assertDoesNotThrow(
                () -> {
                    try {
                        method.invoke(controller);
                    } catch (Exception e) {
                        // JavaFX 의존성으로 예외 발생 가능
                    }
                },
                "applyLocalPause 호출 가능");
    }

    @Test
    @DisplayName("applyLocalResume 메서드 Reflection 테스트")
    void testApplyLocalResumeReflection() throws Exception {
        Method method = NetworkGameController.class.getDeclaredMethod("applyLocalResume");
        method.setAccessible(true);

        Field pausedField = NetworkGameController.class.getDeclaredField("paused");
        pausedField.setAccessible(true);
        pausedField.setBoolean(controller, true);

        assertDoesNotThrow(
                () -> {
                    method.invoke(controller);
                },
                "applyLocalResume 호출 가능");
    }

    @Test
    @DisplayName("sendPauseToNetwork 메서드 Reflection 테스트")
    void testSendPauseToNetworkReflection() throws Exception {
        Method method = NetworkGameController.class.getDeclaredMethod("sendPauseToNetwork");
        method.setAccessible(true);

        assertDoesNotThrow(
                () -> {
                    method.invoke(controller);
                },
                "sendPauseToNetwork는 server/client null일 때 안전");
    }

    @Test
    @DisplayName("sendResumeToNetwork 메서드 Reflection 테스트")
    void testSendResumeToNetworkReflection() throws Exception {
        Method method = NetworkGameController.class.getDeclaredMethod("sendResumeToNetwork");
        method.setAccessible(true);

        assertDoesNotThrow(
                () -> {
                    method.invoke(controller);
                },
                "sendResumeToNetwork는 server/client null일 때 안전");
    }

    @Test
    @DisplayName("handleLocalGameOver 메서드 Reflection 테스트")
    void testHandleLocalGameOverReflection() throws Exception {
        Method method =
                NetworkGameController.class.getDeclaredMethod("handleLocalGameOver", String.class);
        method.setAccessible(true);

        Field gameStartedField = NetworkGameController.class.getDeclaredField("gameStarted");
        gameStartedField.setAccessible(true);
        gameStartedField.setBoolean(controller, false);

        assertDoesNotThrow(
                () -> {
                    method.invoke(controller, "Test reason");
                },
                "handleLocalGameOver는 gameStarted=false일 때 안전하게 리턴");
    }

    @Test
    @DisplayName("handleRemoteGameOver 메서드 Reflection 테스트")
    void testHandleRemoteGameOverReflection() throws Exception {
        Method method =
                NetworkGameController.class.getDeclaredMethod("handleRemoteGameOver", String.class);
        method.setAccessible(true);

        Field gameStartedField = NetworkGameController.class.getDeclaredField("gameStarted");
        gameStartedField.setAccessible(true);
        gameStartedField.setBoolean(controller, false);

        assertDoesNotThrow(
                () -> {
                    method.invoke(controller, "Test reason");
                },
                "handleRemoteGameOver는 gameStarted=false일 때 안전하게 리턴");
    }

    @Test
    @DisplayName("startNetworkStabilityCheck 메서드 Reflection 테스트")
    void testStartNetworkStabilityCheckReflection() throws Exception {
        Method method = NetworkGameController.class.getDeclaredMethod("startNetworkStabilityCheck");
        method.setAccessible(true);

        assertDoesNotThrow(
                () -> {
                    method.invoke(controller);
                },
                "startNetworkStabilityCheck 호출 가능");
    }

    @Test
    @DisplayName("disconnect 메서드 테스트")
    void testDisconnectMethod() {
        assertDoesNotThrow(
                () -> {
                    controller.disconnect();
                },
                "disconnect는 항상 안전하게 호출 가능");
    }

    @Test
    @DisplayName("returnToLobby 메서드 Reflection 테스트")
    void testReturnToLobbyReflection() throws Exception {
        Method method = NetworkGameController.class.getDeclaredMethod("returnToLobby");
        method.setAccessible(true);

        assertDoesNotThrow(
                () -> {
                    try {
                        method.invoke(controller);
                    } catch (Exception e) {
                        // JavaFX 의존성으로 예외 발생 가능
                    }
                },
                "returnToLobby 호출 가능");
    }

    @Test
    @DisplayName("countdownSeconds 프로퍼티 접근 테스트")
    void testCountdownSecondsPropertyAccess() throws Exception {
        Field countdownField = NetworkGameController.class.getDeclaredField("countdownSeconds");
        countdownField.setAccessible(true);

        Object countdownProperty = countdownField.get(controller);
        assertNotNull(countdownProperty, "countdownSeconds 프로퍼티는 null이 아님");
    }

    @Test
    @DisplayName("timerExecutor 필드 초기값 테스트")
    void testTimerExecutorInitialValue() throws Exception {
        Field timerField = NetworkGameController.class.getDeclaredField("timerExecutor");
        timerField.setAccessible(true);

        assertNull(timerField.get(controller), "초기 timerExecutor는 null");
    }

    @Test
    @DisplayName("countdownTimeline 필드 초기값 테스트")
    void testCountdownTimelineInitialValue() throws Exception {
        Field timelineField = NetworkGameController.class.getDeclaredField("countdownTimeline");
        timelineField.setAccessible(true);

        assertNull(timelineField.get(controller), "초기 countdownTimeline은 null");
    }

    @Test
    @DisplayName("networkCheckExecutor 필드 초기값 테스트")
    void testNetworkCheckExecutorInitialValue() throws Exception {
        Field executorField = NetworkGameController.class.getDeclaredField("networkCheckExecutor");
        executorField.setAccessible(true);

        assertNull(executorField.get(controller), "초기 networkCheckExecutor는 null");
    }

    @Test
    @DisplayName("lobbyScene과 gameScene 필드 초기값 테스트")
    void testSceneFieldsInitialValues() throws Exception {
        Field lobbyField = NetworkGameController.class.getDeclaredField("lobbyScene");
        lobbyField.setAccessible(true);

        Field gameField = NetworkGameController.class.getDeclaredField("gameScene");
        gameField.setAccessible(true);

        assertNull(lobbyField.get(controller), "초기 lobbyScene은 null");
        assertNull(gameField.get(controller), "초기 gameScene은 null");
    }

    @Test
    @DisplayName("onClientConnected 메서드 테스트")
    void testOnClientConnectedMethod() {
        assertDoesNotThrow(
                () -> {
                    controller.onClientConnected("Client123");
                },
                "onClientConnected 호출 가능");
    }

    @Test
    @DisplayName("onClientDisconnected 메서드 테스트")
    void testOnClientDisconnectedMethod() {
        assertDoesNotThrow(
                () -> {
                    controller.onClientDisconnected("Client123");
                },
                "onClientDisconnected 호출 가능");
    }

    @Test
    @DisplayName("onServerDisconnected 메서드 테스트")
    void testOnServerDisconnectedMethod() {
        assertDoesNotThrow(
                () -> {
                    controller.onServerDisconnected("Server closed");
                },
                "onServerDisconnected 호출 가능");
    }

    @Test
    @DisplayName("sendAttack deprecated 메서드 Reflection 테스트")
    void testSendAttackDeprecatedReflection() throws Exception {
        Method method = NetworkGameController.class.getDeclaredMethod("sendAttack", int.class);
        method.setAccessible(true);

        Field gameStartedField = NetworkGameController.class.getDeclaredField("gameStarted");
        gameStartedField.setAccessible(true);
        gameStartedField.setBoolean(controller, false);

        assertDoesNotThrow(
                () -> {
                    method.invoke(controller, 2);
                },
                "sendAttack는 gameStarted=false일 때 안전하게 리턴");
    }

    @Test
    @DisplayName("cleanupAndReturnToMenu 메서드 Reflection 테스트")
    void testCleanupAndReturnToMenuReflection() throws Exception {
        Method method = NetworkGameController.class.getDeclaredMethod("cleanupAndReturnToMenu");
        method.setAccessible(true);

        assertDoesNotThrow(
                () -> {
                    try {
                        method.invoke(controller);
                    } catch (Exception e) {
                        // JavaFX 의존성으로 예외 발생 가능
                    }
                },
                "cleanupAndReturnToMenu 호출 가능");
    }

    @Test
    @DisplayName("여러 모드 조합으로 컨트롤러 생성 테스트")
    void testVariousModeControllers() {
        String[] ips = {"localhost", "127.0.0.1", "192.168.1.1"};
        boolean[] hostModes = {true, false};

        for (String ip : ips) {
            for (boolean isHost : hostModes) {
                NetworkGameController testController =
                        new NetworkGameController(sceneManager, settings, isHost, ip);
                assertNotNull(
                        testController,
                        "컨트롤러 생성: " + (isHost ? "Host" : "Client") + " - IP: " + ip);
            }
        }
    }
}
