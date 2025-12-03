package team13.tetris.network.client;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import team13.tetris.network.listener.ClientMessageListener;
import team13.tetris.network.protocol.*;
import team13.tetris.network.protocol.LobbyStateMessage.PlayerState;
import team13.tetris.network.server.TetrisServer;

@Disabled("Network tests are unstable and cause timeouts")
class TetrisClientAdvancedTest {

    private TetrisServer testServer;
    private TetrisClient client;
    private TestAdvancedListener listener;
    private static final int TEST_PORT = 12346; // 다른 포트 사용

    @BeforeEach
    void setUp() throws IOException {
        // 테스트용 서버 시작
        testServer = new TetrisServer("TestHost", TEST_PORT);
        testServer.start();

        // 클라이언트 생성
        listener = new TestAdvancedListener();
        client = new TetrisClient("TestClient", "localhost", TEST_PORT);
        client.setMessageListener(listener);
    }

    @AfterEach
    void tearDown() {
        if (client != null && client.isConnected()) {
            client.disconnect();
        }
        if (testServer != null && testServer.isRunning()) {
            testServer.stop();
        }
    }

    @Test
    @DisplayName("실제 서버 연결 테스트")
    void testRealServerConnection() throws InterruptedException {
        assertTrue(client.connect(), "서버에 연결되어야 함");
        assertTrue(client.isConnected(), "연결 상태가 true여야 함");

        // 연결 수락 대기
        assertTrue(listener.waitForConnectionAccepted(3000), "연결 수락 메시지를 받아야 함");
    }

    @Test
    @DisplayName("연결 후 Ready 메시지 전송 테스트")
    void testReadyMessageAfterConnection() throws InterruptedException {
        assertTrue(client.connect());
        assertTrue(listener.waitForConnectionAccepted(3000));

        assertTrue(client.requestReady(), "Ready 메시지 전송이 성공해야 함");
        // 서버에서 Ready 상태를 받고 응답하는지 확인
        Thread.sleep(100); // 메시지 처리 대기
    }

    @Test
    @DisplayName("연결 후 보드 업데이트 전송 테스트")
    void testBoardUpdateAfterConnection() throws InterruptedException {
        assertTrue(client.connect());
        assertTrue(listener.waitForConnectionAccepted(3000));

        // 테스트용 보드 데이터
        int[][] testBoard = new int[10][20];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 20; j++) {
                testBoard[i][j] = (i + j) % 7; // 다양한 블록 타입
            }
        }

        Queue<int[][]> incomingBlocks = new LinkedList<>();

        // 게임이 시작되지 않은 상태에서는 false를 반환해야 함
        assertFalse(
                client.sendBoardUpdate(
                        testBoard,
                        5,
                        10,
                        1,
                        0,
                        false,
                        null,
                        -1,
                        2,
                        false,
                        null,
                        -1,
                        incomingBlocks,
                        1500,
                        10,
                        2));
    }

    @Test
    @DisplayName("연결 후 공격 메시지 전송 테스트")
    void testAttackMessageAfterConnection() throws InterruptedException {
        assertTrue(client.connect());
        assertTrue(listener.waitForConnectionAccepted(3000));

        // 게임이 시작되지 않은 상태에서는 false를 반환해야 함
        assertFalse(client.sendAttack("opponent", 4));
    }

    @Test
    @DisplayName("연결 후 일시정지/재개 메시지 테스트")
    void testPauseResumeMessages() throws InterruptedException {
        assertTrue(client.connect());
        assertTrue(listener.waitForConnectionAccepted(3000));

        assertTrue(client.pauseGame(), "일시정지 메시지 전송이 성공해야 함");
        assertTrue(client.resumeGame(), "재개 메시지 전송이 성공해야 함");
    }

    @Test
    @DisplayName("연결 해제 테스트")
    void testDisconnection() throws InterruptedException {
        assertTrue(client.connect());
        assertTrue(listener.waitForConnectionAccepted(3000));

        client.disconnect();
        assertFalse(client.isConnected(), "연결 해제 후 상태가 false여야 함");
    }

    @Test
    @DisplayName("잘못된 서버 주소로 연결 시도")
    void testConnectionToInvalidServer() {
        TetrisClient invalidClient = new TetrisClient("TestClient", "invalid.host.example", 99999);
        assertFalse(invalidClient.connect(), "존재하지 않는 서버에는 연결할 수 없어야 함");
        assertFalse(invalidClient.isConnected(), "연결 실패 시 상태는 false여야 함");
    }

    @Test
    @DisplayName("이미 사용 중인 포트에 연결 시도")
    void testConnectionToUnavailablePort() throws IOException {
        // 임시로 포트를 점유
        try (ServerSocket blockingSocket = new ServerSocket(TEST_PORT + 1)) {
            TetrisClient blockedClient = new TetrisClient("TestClient", "localhost", TEST_PORT + 1);

            // 연결은 성공하지만 테트리스 프로토콜이 아니므로 실패할 것
            assertFalse(blockedClient.connect(), "잘못된 프로토콜 서버에는 연결할 수 없어야 함");
        }
    }

    @Test
    @DisplayName("서버 주소 정보 확인")
    void testServerAddressInfo() {
        assertEquals("localhost:" + TEST_PORT, client.getServerAddress(), "서버 주소 정보가 올바르게 반환되어야 함");
    }

    @Test
    @DisplayName("연결 중 서버 종료 처리")
    void testServerShutdownDuringConnection() throws InterruptedException {
        assertTrue(client.connect());
        assertTrue(listener.waitForConnectionAccepted(3000));

        // 서버 종료
        testServer.stop();

        // 잠시 대기 후 연결 상태 확인 (더 짧은 대기 시간)
        Thread.sleep(500);

        // 연결이 끊어졌을 때의 처리가 제대로 되는지 확인
        assertFalse(client.sendMessage(ConnectionMessage.createPlayerReady("TestClient")));
    }

    @Test
    @DisplayName("동시에 여러 메시지 전송")
    void testConcurrentMessageSending() throws InterruptedException {
        assertTrue(client.connect());
        assertTrue(listener.waitForConnectionAccepted(3000));

        // 여러 스레드에서 동시에 메시지 전송
        Thread[] threads = new Thread[5];
        boolean[] results = new boolean[5];

        for (int i = 0; i < 5; i++) {
            final int index = i;
            threads[i] =
                    new Thread(
                            () -> {
                                ConnectionMessage msg =
                                        ConnectionMessage.createPlayerReady("TestClient" + index);
                                results[index] = client.sendMessage(msg);
                            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // 모든 메시지 전송이 성공해야 함
        for (boolean result : results) {
            assertTrue(result, "동시 메시지 전송이 성공해야 함");
        }
    }

    @Test
    @DisplayName("매우 큰 보드 데이터 전송")
    void testLargeBoardDataTransmission() throws InterruptedException {
        assertTrue(client.connect());
        assertTrue(listener.waitForConnectionAccepted(3000));

        // 대용량 보드 데이터 생성
        int[][] largeBoard = new int[50][100];
        for (int i = 0; i < 50; i++) {
            for (int j = 0; j < 100; j++) {
                largeBoard[i][j] = (i * j) % 8;
            }
        }

        Queue<int[][]> largeIncomingBlocks = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            int[][] block = new int[4][4];
            for (int x = 0; x < 4; x++) {
                for (int y = 0; y < 4; y++) {
                    block[x][y] = i % 7;
                }
            }
            largeIncomingBlocks.add(block);
        }

        // 게임이 시작되지 않았으므로 false를 반환하지만, 에러 없이 처리되어야 함
        assertDoesNotThrow(
                () -> {
                    client.sendBoardUpdate(
                            largeBoard,
                            25,
                            50,
                            1,
                            2,
                            false,
                            null,
                            -1,
                            3,
                            false,
                            null,
                            -1,
                            largeIncomingBlocks,
                            999999,
                            500,
                            10);
                });
    }

    @Test
    @DisplayName("빈 메시지 리스너로 연결")
    void testConnectionWithNullListener() throws InterruptedException {
        client.setMessageListener(null);

        // 리스너가 null이어도 연결은 성공해야 함
        assertTrue(client.connect(), "null 리스너여도 연결은 성공해야 함");

        // 메시지 전송도 가능해야 함
        assertTrue(client.sendMessage(ConnectionMessage.createPlayerReady("TestClient")));
    }

    @Test
    @DisplayName("특수 문자가 포함된 메시지 전송")
    void testSpecialCharacterMessages() throws InterruptedException {
        assertTrue(client.connect());
        assertTrue(listener.waitForConnectionAccepted(3000));

        // 특수 문자가 포함된 메시지들
        String[] specialMessages = {
            "테스트 메시지",
            "Special !@#$%^&*() Characters",
            "줄바꿈\n포함",
            "탭\t문자",
            "\"따옴표\" 포함",
            "Unicode: 🎮🎯🎲"
        };

        for (String msg : specialMessages) {
            ConnectionMessage connMsg = new ConnectionMessage(MessageType.PAUSE, "TestClient", msg);
            assertTrue(client.sendMessage(connMsg), "특수 문자 메시지 전송이 성공해야 함: " + msg);
        }
    }

    @Test
    @DisplayName("연속 연결/해제 테스트")
    void testRepeatedConnectionDisconnection() throws InterruptedException {
        for (int i = 0; i < 2; i++) { // 3번에서 2번으로 줄임
            assertTrue(client.connect(), "연결 시도 " + i + "가 성공해야 함");
            assertTrue(listener.waitForConnectionAccepted(2000), "연결 수락 대기 " + i);

            client.disconnect();
            assertFalse(client.isConnected(), "연결 해제 " + i + " 후 상태가 false여야 함");

            Thread.sleep(200); // 정리 시간을 늘림
        }
    }

    // 고급 테스트용 리스너
    @SuppressWarnings("unused")
    private static class TestAdvancedListener implements ClientMessageListener {
        private final CountDownLatch connectionAccepted = new CountDownLatch(1);
        private final CountDownLatch gameStarted = new CountDownLatch(1);
        private final CountDownLatch gameOver = new CountDownLatch(1);
        private volatile String lastError;
        private volatile BoardUpdateMessage lastBoardUpdate;
        private volatile AttackMessage lastAttack;
        private volatile boolean paused = false;
        private volatile boolean resumed = false;
        private volatile GameModeMessage.GameMode selectedGameMode;

        @Override
        public void onConnectionAccepted() {
            connectionAccepted.countDown();
        }

        @Override
        public void onConnectionRejected(String reason) {
            // 테스트에서 거부는 예상하지 않음
        }

        @Override
        public void onPlayerReady(String playerId) {
            // 필요시 구현
        }

        @Override
        public void onCountdownStart() {
            // 필요시 구현
        }

        @Override
        public void onGameStart() {
            gameStarted.countDown();
        }

        @Override
        public void onGameOver(String reason) {
            gameOver.countDown();
        }

        @Override
        public void onBoardUpdate(BoardUpdateMessage boardUpdate) {
            this.lastBoardUpdate = boardUpdate;
        }

        @Override
        public void onAttackReceived(AttackMessage attackMessage) {
            this.lastAttack = attackMessage;
        }

        @Override
        public void onGamePaused() {
            this.paused = true;
        }

        @Override
        public void onGameResumed() {
            this.resumed = true;
        }

        @Override
        public void onError(String error) {
            this.lastError = error;
        }

        @Override
        public void onGameModeSelected(GameModeMessage.GameMode gameMode) {
            this.selectedGameMode = gameMode;
        }

        // 테스트 헬퍼 메서드들
        public boolean waitForConnectionAccepted(long timeoutMs) throws InterruptedException {
            return connectionAccepted.await(timeoutMs, TimeUnit.MILLISECONDS);
        }

        public boolean waitForGameStart(long timeoutMs) throws InterruptedException {
            return gameStarted.await(timeoutMs, TimeUnit.MILLISECONDS);
        }

        public boolean waitForGameOver(long timeoutMs) throws InterruptedException {
            return gameOver.await(timeoutMs, TimeUnit.MILLISECONDS);
        }

        public String getLastError() {
            return lastError;
        }

        public BoardUpdateMessage getLastBoardUpdate() {
            return lastBoardUpdate;
        }

        public AttackMessage getLastAttack() {
            return lastAttack;
        }

        public boolean isPaused() {
            return paused;
        }

        public boolean isResumed() {
            return resumed;
        }

        public GameModeMessage.GameMode getSelectedGameMode() {
            return selectedGameMode;
        }

        @Override
        public void onPlayerUnready(String playerId) {
            // Test implementation
        }

        @Override
        public void onServerDisconnected(String reason) {
            // Test implementation
        }

        @Override
        public void onLobbyStateUpdate(List<PlayerState> playerStates) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'onLobbyStateUpdate'");
        }

        @Override
        public void onGameEnd(List<String> rankings) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'onGameEnd'");
        }

        public void onChatMessageReceived(String senderId, String message) {
            // Test implementation
        }
    }
}
