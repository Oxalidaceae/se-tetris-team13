package team13.tetris.network.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.LinkedList;
import java.util.Queue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team13.tetris.network.listener.ClientMessageListener;
import team13.tetris.network.protocol.*;

@ExtendWith(MockitoExtension.class)
class TetrisClientAdvancedTest {

    @Mock private ClientMessageListener mockListener;

    private TetrisClient client;
    private static final int TEST_PORT = 12346;

    @BeforeEach
    void setUp() {
        client = new TetrisClient("TestClient", "localhost", TEST_PORT);
        client.setMessageListener(mockListener);
    }

    @Test
    @DisplayName("클라이언트 생성 및 초기 상태 테스트")
    void testClientCreationAndInitialState() {
        assertNotNull(client, "클라이언트가 생성되어야 함");
        assertFalse(client.isConnected(), "초기 연결 상태는 false여야 함");
        assertEquals("localhost:" + TEST_PORT, client.getServerAddress(), "서버 주소가 올바르게 설정되어야 함");
    }

    @Test
    @DisplayName("메시지 리스너 설정 테스트")
    void testMessageListenerSetup() {
        client.setMessageListener(mockListener);
        assertNotNull(client, "리스너 설정 후에도 클라이언트가 유효해야 함");

        client.setMessageListener(null);
        assertNotNull(client, "null 리스너 설정도 가능해야 함");
    }

    @Test
    @DisplayName("연결되지 않은 상태에서 메시지 전송 시도")
    void testSendMessageWithoutConnection() {
        assertFalse(client.isConnected(), "초기 상태는 연결되지 않음");

        // 연결되지 않은 상태에서 메시지 전송은 실패해야 함
        ConnectionMessage msg = ConnectionMessage.createPlayerReady("TestClient");
        assertFalse(client.sendMessage(msg), "연결 없이 메시지 전송은 실패해야 함");
    }

    @Test
    @DisplayName("연결되지 않은 상태에서 Ready 메시지 전송")
    void testReadyMessageWithoutConnection() {
        assertFalse(client.isConnected());
        assertFalse(client.requestReady(), "연결 없이 Ready 메시지 전송은 실패해야 함");
    }

    @Test
    @DisplayName("연결되지 않은 상태에서 Unready 메시지 전송")
    void testUnreadyMessageWithoutConnection() {
        assertFalse(client.isConnected());
        assertFalse(client.requestUnready(), "연결 없이 Unready 메시지 전송은 실패해야 함");
    }

    @Test
    @DisplayName("연결되지 않은 상태에서 보드 업데이트 전송")
    void testBoardUpdateWithoutConnection() {
        int[][] testBoard = new int[10][20];
        Queue<int[][]> incomingBlocks = new LinkedList<>();

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
                        2),
                "연결 없이 보드 업데이트 전송은 실패해야 함");
    }

    @Test
    @DisplayName("연결되지 않은 상태에서 공격 메시지 전송")
    void testAttackMessageWithoutConnection() {
        assertFalse(client.sendAttack("opponent", 4), "연결 없이 공격 메시지 전송은 실패해야 함");
    }

    @Test
    @DisplayName("연결되지 않은 상태에서 일시정지 메시지 전송")
    void testPauseMessageWithoutConnection() {
        assertFalse(client.pauseGame(), "연결 없이 일시정지 메시지 전송은 실패해야 함");
    }

    @Test
    @DisplayName("연결되지 않은 상태에서 재개 메시지 전송")
    void testResumeMessageWithoutConnection() {
        assertFalse(client.resumeGame(), "연결 없이 재개 메시지 전송은 실패해야 함");
    }

    @Test
    @DisplayName("연결되지 않은 상태에서 disconnect 호출")
    void testDisconnectWithoutConnection() {
        assertFalse(client.isConnected());
        assertDoesNotThrow(() -> client.disconnect(), "연결 없이 disconnect 호출은 안전해야 함");
        assertFalse(client.isConnected(), "여전히 연결되지 않은 상태여야 함");
    }

    @Test
    @DisplayName("서버 주소 정보 테스트")
    void testServerAddressInfo() {
        assertEquals("localhost:" + TEST_PORT, client.getServerAddress(), "서버 주소 정보가 올바르게 반환되어야 함");
    }

    @Test
    @DisplayName("다양한 플레이어 ID로 클라이언트 생성")
    void testDifferentPlayerIds() {
        TetrisClient client1 = new TetrisClient("Player1", "localhost", TEST_PORT);
        TetrisClient client2 = new TetrisClient("Player2", "127.0.0.1", TEST_PORT);
        TetrisClient client3 = new TetrisClient("", "localhost", TEST_PORT);

        assertNotNull(client1);
        assertNotNull(client2);
        assertNotNull(client3);
        assertFalse(client1.isConnected());
        assertFalse(client2.isConnected());
        assertFalse(client3.isConnected());
    }

    @Test
    @DisplayName("보드 업데이트 메시지 생성 테스트")
    void testBoardUpdateMessageCreation() {
        int[][] testBoard = new int[10][20];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 20; j++) {
                testBoard[i][j] = (i + j) % 7;
            }
        }

        Queue<int[][]> incomingBlocks = new LinkedList<>();

        // 연결되지 않은 상태에서는 false 반환
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
    @DisplayName("공격 메시지 유효성 테스트")
    void testAttackMessageValidation() {
        // 연결되지 않은 상태
        assertFalse(client.sendAttack("opponent", 0));
        assertFalse(client.sendAttack("opponent", -1));
        assertFalse(client.sendAttack("opponent", 1));
        assertFalse(client.sendAttack("opponent", 4));
        assertFalse(client.sendAttack(null, 2));
        assertFalse(client.sendAttack("", 2));
    }

    @Test
    @DisplayName("다양한 메시지 타입 전송 테스트")
    void testDifferentMessageTypes() {
        assertFalse(client.sendMessage(ConnectionMessage.createPlayerReady("TestClient")));
        assertFalse(client.sendMessage(ConnectionMessage.createPlayerUnready("TestClient")));
        assertFalse(client.sendMessage(new ConnectionMessage(MessageType.PAUSE, "TestClient", "")));
        assertFalse(
                client.sendMessage(new ConnectionMessage(MessageType.RESUME, "TestClient", "")));
    }

    @Test
    @DisplayName("클라이언트 재사용 테스트")
    void testClientReuse() {
        assertFalse(client.isConnected());

        // 첫 번째 disconnect
        client.disconnect();
        assertFalse(client.isConnected());

        // 두 번째 disconnect (안전해야 함)
        assertDoesNotThrow(() -> client.disconnect());
        assertFalse(client.isConnected());
    }

    @Test
    @DisplayName("메시지 전송 실패 처리 테스트")
    void testMessageSendingFailure() {
        // 연결되지 않은 상태에서 모든 메시지 전송은 실패해야 함
        assertFalse(client.requestReady());
        assertFalse(client.requestUnready());
        assertFalse(client.pauseGame());
        assertFalse(client.resumeGame());
    }

    @Test
    @DisplayName("클라이언트 정보 일관성 테스트")
    void testClientInfoConsistency() {
        String address1 = client.getServerAddress();
        String address2 = client.getServerAddress();

        assertEquals(address1, address2, "여러 번 호출해도 같은 주소를 반환해야 함");
        assertTrue(address1.contains("localhost"), "주소에 hostname이 포함되어야 함");
        assertTrue(address1.contains(String.valueOf(TEST_PORT)), "주소에 포트가 포함되어야 함");
    }
}
