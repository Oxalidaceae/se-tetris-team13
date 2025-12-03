package team13.tetris.network.server;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team13.tetris.network.listener.ServerMessageListener;
import team13.tetris.network.protocol.GameModeMessage;

@ExtendWith(MockitoExtension.class)
class TetrisServerAdvancedTest {

    @Mock private ServerMessageListener mockListener;

    private TetrisServer server;
    private static final int TEST_PORT = 12348;

    @BeforeEach
    void setUp() throws IOException {
        server = new TetrisServer("TestHost", TEST_PORT);
        server.setHostMessageListener(mockListener);
    }

    @AfterEach
    void tearDown() {
        if (server != null && server.isRunning()) {
            server.stop();
        }
    }

    @Test
    @DisplayName("서버 생성 및 초기 상태 확인")
    void testServerCreationAndInitialState() {
        assertNotNull(server, "서버가 생성되어야 함");
        assertFalse(server.isRunning(), "초기 상태는 실행 중이 아니어야 함");
        assertEquals("TestHost", server.getHostPlayerId(), "호스트 플레이어 ID가 올바르게 설정되어야 함");
    }

    @Test
    @DisplayName("서버 시작 테스트")
    void testServerStart() throws IOException {
        assertFalse(server.isRunning());
        server.start();
        assertTrue(server.isRunning(), "서버 시작 후 실행 중이어야 함");
    }

    @Test
    @DisplayName("서버 중지 테스트")
    void testServerStop() throws IOException {
        server.start();
        assertTrue(server.isRunning());

        server.stop();
        assertFalse(server.isRunning(), "서버 중지 후 실행 중이 아니어야 함");
    }

    @Test
    @DisplayName("여러 번 서버 중지 호출")
    void testMultipleStopCalls() throws IOException {
        server.start();
        assertTrue(server.isRunning());

        server.stop();
        assertFalse(server.isRunning());

        assertDoesNotThrow(() -> server.stop(), "이미 중지된 서버 stop 호출은 안전해야 함");
        assertFalse(server.isRunning());
    }

    @Test
    @DisplayName("실행 중이지 않을 때 stop 호출")
    void testStopWithoutStart() {
        assertFalse(server.isRunning());
        assertDoesNotThrow(() -> server.stop(), "실행 중이 아닌 서버 stop 호출은 안전해야 함");
    }

    @Test
    @DisplayName("게임 모드 선택 테스트 - NORMAL")
    void testSelectNormalMode() throws IOException {
        server.start();
        assertDoesNotThrow(() -> server.selectGameMode(GameModeMessage.GameMode.NORMAL));
    }

    @Test
    @DisplayName("게임 모드 선택 테스트 - ITEM")
    void testSelectItemMode() throws IOException {
        server.start();
        assertDoesNotThrow(() -> server.selectGameMode(GameModeMessage.GameMode.ITEM));
    }

    @Test
    @DisplayName("게임 모드 선택 테스트 - TIMER")
    void testSelectTimerMode() throws IOException {
        server.start();
        assertDoesNotThrow(() -> server.selectGameMode(GameModeMessage.GameMode.TIMER));
    }

    @Test
    @DisplayName("호스트 준비 상태 설정 테스트")
    void testSetHostReady() throws IOException {
        server.start();
        assertDoesNotThrow(() -> server.setHostReady());
    }

    @Test
    @DisplayName("호스트 준비 해제 테스트")
    void testSetHostUnready() throws IOException {
        server.start();
        assertDoesNotThrow(() -> server.setHostUnready());
    }

    @Test
    @DisplayName("호스트 일시정지 테스트")
    void testPauseGameAsHost() throws IOException {
        server.start();
        assertDoesNotThrow(() -> server.pauseGameAsHost());
    }

    @Test
    @DisplayName("호스트 재개 테스트")
    void testResumeGameAsHost() throws IOException {
        server.start();
        assertDoesNotThrow(() -> server.resumeGameAsHost());
    }

    @Test
    @DisplayName("게임 오버 브로드캐스트 테스트")
    void testBroadcastGameOver() throws IOException {
        server.start();
        assertDoesNotThrow(() -> server.broadcastGameOverToOthers("TestHost", "Test Reason"));
    }

    @Test
    @DisplayName("준비 상태 리셋 테스트")
    void testResetReadyStates() throws IOException {
        server.start();
        assertDoesNotThrow(() -> server.resetReadyStates());
    }

    @Test
    @DisplayName("서버 실행 전 게임 모드 선택")
    void testSelectModeBeforeStart() {
        assertFalse(server.isRunning());
        assertDoesNotThrow(() -> server.selectGameMode(GameModeMessage.GameMode.NORMAL));
    }

    @Test
    @DisplayName("서버 실행 전 호스트 준비")
    void testSetHostReadyBeforeStart() {
        assertFalse(server.isRunning());
        assertDoesNotThrow(() -> server.setHostReady());
    }

    @Test
    @DisplayName("서버 실행 전 일시정지")
    void testPauseBeforeStart() {
        assertFalse(server.isRunning());
        assertDoesNotThrow(() -> server.pauseGameAsHost());
    }

    @Test
    @DisplayName("서버 리스너 설정 테스트")
    void testSetHostMessageListener() {
        server.setHostMessageListener(mockListener);
        assertNotNull(server, "리스너 설정 후에도 서버가 유효해야 함");

        server.setHostMessageListener(null);
        assertNotNull(server, "null 리스너 설정도 가능해야 함");
    }

    @Test
    @DisplayName("호스트 플레이어 ID 확인")
    void testGetHostPlayerId() {
        assertEquals("TestHost", server.getHostPlayerId());
    }

    @Test
    @DisplayName("다양한 포트로 서버 생성")
    void testDifferentPorts() throws IOException {
        TetrisServer server1 = new TetrisServer("Host1", 12345);
        TetrisServer server2 = new TetrisServer("Host2", 12346);
        TetrisServer server3 = new TetrisServer("Host3", 12347);

        assertNotNull(server1);
        assertNotNull(server2);
        assertNotNull(server3);
        assertFalse(server1.isRunning());
        assertFalse(server2.isRunning());
        assertFalse(server3.isRunning());
    }

    @Test
    @DisplayName("서버 상태 일관성 테스트")
    void testServerStateConsistency() throws IOException {
        assertFalse(server.isRunning());

        server.start();
        assertTrue(server.isRunning());
        assertTrue(server.isRunning(), "여러 번 호출해도 같은 결과");

        server.stop();
        assertFalse(server.isRunning());
        assertFalse(server.isRunning(), "여러 번 호출해도 같은 결과");
    }

    @Test
    @DisplayName("서버 메서드 체인 테스트")
    void testServerMethodChaining() throws IOException {
        server.start();

        assertDoesNotThrow(
                () -> {
                    server.selectGameMode(GameModeMessage.GameMode.ITEM);
                    server.setHostReady();
                    server.pauseGameAsHost();
                    server.resumeGameAsHost();
                    server.setHostUnready();
                },
                "여러 메서드를 연속으로 호출해도 안전해야 함");
    }

    @Test
    @DisplayName("서버 중지 후 메서드 호출 안전성")
    void testMethodCallsAfterStop() throws IOException {
        server.start();
        server.stop();

        assertDoesNotThrow(
                () -> {
                    server.selectGameMode(GameModeMessage.GameMode.NORMAL);
                    server.setHostReady();
                    server.pauseGameAsHost();
                    server.resumeGameAsHost();
                },
                "중지된 서버에 대한 호출도 안전해야 함");
    }
}
