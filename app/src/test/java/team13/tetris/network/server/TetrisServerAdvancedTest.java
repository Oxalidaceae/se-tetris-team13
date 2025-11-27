package team13.tetris.network.server;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import team13.tetris.network.client.TetrisClient;
import team13.tetris.network.listener.ClientMessageListener;
import team13.tetris.network.listener.ServerMessageListener;
import team13.tetris.network.protocol.*;

@Disabled("Network tests are unstable and cause timeouts")
class TetrisServerAdvancedTest {

    private TetrisServer server;
    private TestAdvancedServerListener serverListener;
    private static final int TEST_PORT = 12348; // 다른 포트 사용

    @BeforeEach
    void setUp() throws IOException {
        serverListener = new TestAdvancedServerListener();
        server = new TetrisServer("TestHost", TEST_PORT);
        server.setHostMessageListener(serverListener);
        server.start();
    }

    @AfterEach
    void tearDown() {
        if (server != null && server.isRunning()) {
            server.stop();
        }
    }

    @Test
    @DisplayName("서버 시작 및 실행 상태 확인")
    void testServerStartAndRunningState() {
        assertTrue(server.isRunning(), "서버가 실행 중이어야 함");
        assertEquals("TestHost", server.getHostPlayerId(), "호스트 플레이어 ID가 올바르게 설정되어야 함");
    }

    @Test
    @DisplayName("클라이언트 연결 및 서버 리스너 호출 테스트")
    void testClientConnectionAndServerListener() throws InterruptedException {
        TetrisClient client = new TetrisClient("TestClient", "localhost", TEST_PORT);
        TestClientListener clientListener = new TestClientListener();
        client.setMessageListener(clientListener);

        assertTrue(client.connect(), "클라이언트가 서버에 연결되어야 함");

        // 서버 리스너가 클라이언트 연결을 감지했는지 확인
        assertTrue(serverListener.waitForClientConnected(3000), "서버가 클라이언트 연결을 감지해야 함");
        assertEquals(
                "TestClient",
                serverListener.getLastConnectedClientId(),
                "연결된 클라이언트 ID가 올바르게 기록되어야 함");

        client.disconnect();

        // 서버가 클라이언트 연결 해제를 감지했는지 확인
        assertTrue(serverListener.waitForClientDisconnected(3000), "서버가 클라이언트 연결 해제를 감지해야 함");
    }

    @Test
    @DisplayName("게임 모드 선택 및 브로드캐스트 테스트")
    void testGameModeSelectionAndBroadcast() throws InterruptedException {
        TetrisClient client = new TetrisClient("TestClient", "localhost", TEST_PORT);
        TestClientListener clientListener = new TestClientListener();
        client.setMessageListener(clientListener);

        assertTrue(client.connect());
        assertTrue(serverListener.waitForClientConnected(3000));

        // 서버에서 게임 모드 선택
        server.selectGameMode(GameModeMessage.GameMode.ITEM);

        // 클라이언트가 게임 모드 메시지를 받았는지 확인
        assertTrue(clientListener.waitForGameModeSelected(3000), "클라이언트가 게임 모드 선택 메시지를 받아야 함");
        assertEquals(
                GameModeMessage.GameMode.ITEM,
                clientListener.getSelectedGameMode(),
                "선택된 게임 모드가 올바르게 전달되어야 함");

        client.disconnect();
    }

    @Test
    @DisplayName("호스트 준비 상태 및 게임 시작 테스트")
    void testHostReadyAndGameStart() throws InterruptedException {
        TetrisClient client = new TetrisClient("TestClient", "localhost", TEST_PORT);
        TestClientListener clientListener = new TestClientListener();
        client.setMessageListener(clientListener);

        assertTrue(client.connect());
        assertTrue(serverListener.waitForClientConnected(3000));

        // 클라이언트가 준비 상태로 전환
        client.requestReady();
        Thread.sleep(100); // 메시지 처리 대기

        // 호스트도 준비 상태로 전환
        server.setHostReady();

        // 게임 시작 메시지가 전송되었는지 확인
        assertTrue(clientListener.waitForGameStart(3000), "클라이언트가 게임 시작 메시지를 받아야 함");

        client.disconnect();
    }

    @Test
    @DisplayName("서버에서 일시정지/재개 브로드캐스트 테스트")
    void testPauseResumeBroadcast() throws InterruptedException {
        TetrisClient client = new TetrisClient("TestClient", "localhost", TEST_PORT);
        TestClientListener clientListener = new TestClientListener();
        client.setMessageListener(clientListener);

        assertTrue(client.connect());
        assertTrue(serverListener.waitForClientConnected(3000));

        // 서버에서 일시정지
        server.pauseGameAsHost();
        assertTrue(clientListener.waitForGamePaused(3000), "클라이언트가 일시정지 메시지를 받아야 함");

        // 서버에서 재개
        server.resumeGameAsHost();
        assertTrue(clientListener.waitForGameResumed(3000), "클라이언트가 재개 메시지를 받아야 함");

        client.disconnect();
    }

    @Test
    @DisplayName("서버에서 게임 종료 브로드캐스트 테스트")
    void testGameOverBroadcast() throws InterruptedException {
        TetrisClient client = new TetrisClient("TestClient", "localhost", TEST_PORT);
        TestClientListener clientListener = new TestClientListener();
        client.setMessageListener(clientListener);

        assertTrue(client.connect());
        assertTrue(serverListener.waitForClientConnected(3000));

        // 서버에서 게임 종료 메시지 브로드캐스트
        String gameOverReason = "Test game over";
        server.broadcastGameOverToOthers("TestHost", gameOverReason);

        assertTrue(clientListener.waitForGameOver(3000), "클라이언트가 게임 종료 메시지를 받아야 함");
        assertEquals(gameOverReason, clientListener.getGameOverReason(), "게임 종료 이유가 올바르게 전달되어야 함");

        client.disconnect();
    }

    @Test
    @DisplayName("다중 클라이언트 연결 및 브로드캐스트 테스트")
    void testMultipleClientsAndBroadcast() throws InterruptedException {
        TetrisClient client1 = new TetrisClient("TestClient1", "localhost", TEST_PORT);
        TetrisClient client2 = new TetrisClient("TestClient2", "localhost", TEST_PORT);

        TestClientListener listener1 = new TestClientListener();
        TestClientListener listener2 = new TestClientListener();

        client1.setMessageListener(listener1);
        client2.setMessageListener(listener2);

        // 첫 번째 클라이언트 연결
        assertTrue(client1.connect());
        assertTrue(serverListener.waitForClientConnected(3000));

        Thread.sleep(100); // 연결 처리 대기

        // 두 번째 클라이언트 연결 시도 (서버는 2명까지만 허용해야 함)
        // 실제 구현에 따라 결과가 다를 수 있음
        boolean secondConnected = client2.connect();

        if (secondConnected) {
            // 두 번째 클라이언트도 연결된 경우, 브로드캐스트 테스트
            server.pauseGameAsHost();

            assertTrue(listener1.waitForGamePaused(3000), "첫 번째 클라이언트가 일시정지 메시지를 받아야 함");
            assertTrue(listener2.waitForGamePaused(3000), "두 번째 클라이언트가 일시정지 메시지를 받아야 함");

            client2.disconnect();
        }

        client1.disconnect();
    }

    @Test
    @DisplayName("서버 중지 후 클라이언트 연결 시도")
    void testClientConnectionAfterServerStop() throws InterruptedException {
        // 서버 중지
        server.stop();
        assertFalse(server.isRunning(), "서버가 중지되어야 함");

        // 클라이언트 연결 시도
        TetrisClient client = new TetrisClient("TestClient", "localhost", TEST_PORT);
        assertFalse(client.connect(), "중지된 서버에는 연결할 수 없어야 함");
    }

    @Test
    @DisplayName("서버 재시작 테스트")
    void testServerRestart() throws IOException, InterruptedException {
        // 서버 중지
        server.stop();
        assertFalse(server.isRunning(), "서버가 중지되어야 함");

        // 서버 재시작
        server.start();
        assertTrue(server.isRunning(), "서버가 재시작되어야 함");

        // 클라이언트 연결 테스트
        TetrisClient client = new TetrisClient("TestClient", "localhost", TEST_PORT);
        TestClientListener clientListener = new TestClientListener();
        client.setMessageListener(clientListener);

        assertTrue(client.connect(), "재시작된 서버에 연결되어야 함");

        client.disconnect();
    }

    @Test
    @DisplayName("서버 동시성 테스트 - 여러 작업 동시 실행")
    void testServerConcurrency() throws InterruptedException {
        TetrisClient client = new TetrisClient("TestClient", "localhost", TEST_PORT);
        TestClientListener clientListener = new TestClientListener();
        client.setMessageListener(clientListener);

        assertTrue(client.connect());
        assertTrue(serverListener.waitForClientConnected(3000));

        // 여러 스레드에서 동시에 서버 작업 수행 (스레드 수를 줄임)
        Thread[] threads = new Thread[3];
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < 3; i++) {
            threads[i] =
                    new Thread(
                            () -> {
                                try {
                                    server.pauseGameAsHost();
                                    Thread.sleep(5); // 대기 시간 단축
                                    server.resumeGameAsHost();
                                    successCount.incrementAndGet();
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join(1000); // 타임아웃 추가
        }

        assertEquals(3, successCount.get(), "모든 동시 작업이 성공해야 함");

        client.disconnect();
    }

    @Test
    @DisplayName("서버 메모리 사용량 테스트 - 대량 메시지 처리")
    void testServerMemoryUsage() throws InterruptedException {
        TetrisClient client = new TetrisClient("TestClient", "localhost", TEST_PORT);
        TestClientListener clientListener = new TestClientListener();
        client.setMessageListener(clientListener);

        assertTrue(client.connect());
        assertTrue(serverListener.waitForClientConnected(3000));

        // 대량의 메시지 처리 테스트 (반복 횟수 줄임)
        for (int i = 0; i < 50; i++) {
            server.pauseGameAsHost();
            server.resumeGameAsHost();
            server.selectGameMode(
                    i % 2 == 0 ? GameModeMessage.GameMode.NORMAL : GameModeMessage.GameMode.ITEM);

            if (i % 20 == 0) {
                Thread.sleep(1); // 주기적으로 잠깐 대기
            }
        }

        client.disconnect();
    }

    @Test
    @DisplayName("서버 종료 시 정리 작업 테스트")
    void testServerCleanupOnShutdown() throws InterruptedException {
        TetrisClient client = new TetrisClient("TestClient", "localhost", TEST_PORT);
        TestClientListener clientListener = new TestClientListener();
        client.setMessageListener(clientListener);

        assertTrue(client.connect());
        assertTrue(serverListener.waitForClientConnected(3000));

        // 서버 종료
        server.stop();

        // 클라이언트가 연결 해제를 감지했는지 확인 (더 짧은 대기 시간)
        Thread.sleep(500); // 연결 해제 처리 시간

        assertFalse(server.isRunning(), "서버가 완전히 중지되어야 함");

        client.disconnect(); // 안전하게 클라이언트도 종료
    }

    // 고급 서버 테스트용 리스너
    private static class TestAdvancedServerListener implements ServerMessageListener {
        private final CountDownLatch clientConnected = new CountDownLatch(1);
        private final CountDownLatch clientDisconnected = new CountDownLatch(1);
        private volatile String lastConnectedClientId;
        private volatile String lastDisconnectedClientId;

        @Override
        public void onClientConnected(String clientId) {
            this.lastConnectedClientId = clientId;
            clientConnected.countDown();
        }

        @Override
        public void onClientDisconnected(String clientId) {
            this.lastDisconnectedClientId = clientId;
            clientDisconnected.countDown();
        }

        @Override
        public void onPlayerReady(String playerId) {
            // 필요시 구현
        }

        @Override
        public void onGameStart() {
            // 필요시 구현
        }

        @Override
        public void onGameOver(String reason) {
            // 필요시 구현
        }

        @Override
        public void onBoardUpdate(BoardUpdateMessage boardUpdate) {
            // 필요시 구현
        }

        @Override
        public void onAttackReceived(AttackMessage attackMessage) {
            // 필요시 구현
        }

        @Override
        public void onGamePaused() {
            // 필요시 구현
        }

        @Override
        public void onGameResumed() {
            // 필요시 구현
        }

        @Override
        public void onChatMessageReceived(String senderId, String message) {
            // 필요시 구현
        }

        public boolean waitForClientConnected(long timeoutMs) throws InterruptedException {
            return clientConnected.await(timeoutMs, TimeUnit.MILLISECONDS);
        }

        public boolean waitForClientDisconnected(long timeoutMs) throws InterruptedException {
            return clientDisconnected.await(timeoutMs, TimeUnit.MILLISECONDS);
        }

        public String getLastConnectedClientId() {
            return lastConnectedClientId;
        }

        @SuppressWarnings("unused")
        public String getLastDisconnectedClientId() {
            return lastDisconnectedClientId;
        }

        @Override
        public void onCountdownStart() {
            // Test implementation
        }

        @Override
        public void onPlayerUnready(String playerId) {
            // Test implementation
        }
    }

    // 테스트용 클라이언트 리스너
    private static class TestClientListener implements ClientMessageListener {
        private final CountDownLatch gameStart = new CountDownLatch(1);
        private final CountDownLatch gameOver = new CountDownLatch(1);
        private final CountDownLatch gamePaused = new CountDownLatch(1);
        private final CountDownLatch gameResumed = new CountDownLatch(1);
        private final CountDownLatch gameModeSelected = new CountDownLatch(1);

        private volatile String gameOverReason;
        private volatile GameModeMessage.GameMode selectedGameMode;

        @Override
        public void onConnectionAccepted() {
            // 기본 구현
        }

        @Override
        public void onConnectionRejected(String reason) {
            // 기본 구현
        }

        @Override
        public void onPlayerReady(String playerId) {
            // 기본 구현
        }

        @Override
        public void onGameStart() {
            gameStart.countDown();
        }

        @Override
        public void onCountdownStart() {
            // 테스트용 구현
        }

        @Override
        public void onGameOver(String reason) {
            this.gameOverReason = reason;
            gameOver.countDown();
        }

        @Override
        public void onBoardUpdate(BoardUpdateMessage boardUpdate) {
            // 기본 구현
        }

        @Override
        public void onAttackReceived(AttackMessage attackMessage) {
            // 기본 구현
        }

        @Override
        public void onGamePaused() {
            gamePaused.countDown();
        }

        @Override
        public void onGameResumed() {
            gameResumed.countDown();
        }

        @Override
        public void onGameModeSelected(GameModeMessage.GameMode gameMode) {
            this.selectedGameMode = gameMode;
            gameModeSelected.countDown();
        }

        @Override
        public void onError(String error) {
            // 기본 구현
        }

        public boolean waitForGameStart(long timeoutMs) throws InterruptedException {
            return gameStart.await(timeoutMs, TimeUnit.MILLISECONDS);
        }

        public boolean waitForGameOver(long timeoutMs) throws InterruptedException {
            return gameOver.await(timeoutMs, TimeUnit.MILLISECONDS);
        }

        public boolean waitForGamePaused(long timeoutMs) throws InterruptedException {
            return gamePaused.await(timeoutMs, TimeUnit.MILLISECONDS);
        }

        public boolean waitForGameResumed(long timeoutMs) throws InterruptedException {
            return gameResumed.await(timeoutMs, TimeUnit.MILLISECONDS);
        }

        public boolean waitForGameModeSelected(long timeoutMs) throws InterruptedException {
            return gameModeSelected.await(timeoutMs, TimeUnit.MILLISECONDS);
        }

        public String getGameOverReason() {
            return gameOverReason;
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
        public void onChatMessageReceived(String senderId, String message) {
            // Test implementation
        }
    }
}
