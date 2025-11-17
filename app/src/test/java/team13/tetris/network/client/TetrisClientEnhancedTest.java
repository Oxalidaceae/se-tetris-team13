package team13.tetris.network.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import team13.tetris.network.listener.ClientMessageListener;
import team13.tetris.network.protocol.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TetrisClientEnhancedTest {

    @Mock
    private ClientMessageListener mockListener;
    
    private TetrisClient client;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        client = new TetrisClient("TestPlayer", "localhost", 12346);
        client.setMessageListener(mockListener);
    }

    @Test
    @DisplayName("TetrisClient 생성자 매개변수 검증")
    void testTetrisClientConstructorParameters() {
        // 전체 매개변수 생성자
        TetrisClient fullClient = new TetrisClient("Player1", "192.168.1.1", 8080);
        assertNotNull(fullClient, "전체 매개변수로 클라이언트 생성 가능");
        assertFalse(fullClient.isConnected(), "초기 상태는 연결되지 않음");
        
        // 호스트만 지정
        TetrisClient hostClient = new TetrisClient("Player2", "example.com");
        assertNotNull(hostClient, "호스트만 지정해서 클라이언트 생성 가능");
        assertFalse(hostClient.isConnected(), "초기 상태는 연결되지 않음");
        
        // 플레이어 ID만 지정
        TetrisClient defaultClient = new TetrisClient("Player3", "localhost", 12345);
        assertNotNull(defaultClient, "플레이어 ID만으로 클라이언트 생성 가능");
        assertFalse(defaultClient.isConnected(), "초기 상태는 연결되지 않음");
    }

    @Test
    @DisplayName("다양한 플레이어 ID로 클라이언트 생성")
    void testVariousPlayerIds() {
        String[] playerIds = {
            "Player1",
            "한글플레이어",
            "Player_With_Underscore",
            "123456",
            "VeryLongPlayerNameForTesting",
            "",
            "P"
        };
        
        for (String playerId : playerIds) {
            assertDoesNotThrow(() -> {
                TetrisClient testClient = new TetrisClient(playerId, "localhost", 12345);
                assertNotNull(testClient, "다양한 플레이어 ID로 클라이언트 생성 가능: " + playerId);
                assertFalse(testClient.isConnected(), "초기 상태는 연결되지 않음");
            }, "플레이어 ID: " + playerId + "가 안전하게 처리되어야 함");
        }
    }

    @Test
    @DisplayName("다양한 서버 주소로 클라이언트 생성")
    void testVariousServerAddresses() {
        String[] serverAddresses = {
            "localhost",
            "127.0.0.1",
            "192.168.1.100",
            "example.com",
            "tetris-server.local",
            "",
            "very-long-hostname-that-might-cause-issues.example.com"
        };
        
        for (String address : serverAddresses) {
            assertDoesNotThrow(() -> {
                TetrisClient testClient = new TetrisClient("TestPlayer", address);
                assertNotNull(testClient, "다양한 서버 주소로 클라이언트 생성 가능: " + address);
                assertFalse(testClient.isConnected(), "초기 상태는 연결되지 않음");
            }, "서버 주소: " + address + "가 안전하게 처리되어야 함");
        }
    }

    @Test
    @DisplayName("다양한 포트 번호로 클라이언트 생성")
    void testVariousPortNumbers() {
        int[] portNumbers = {
            1024, 8080, 12345, 25000, 50000, 65535,
            0, -1, 70000, 999999
        };
        
        for (int port : portNumbers) {
            assertDoesNotThrow(() -> {
                TetrisClient testClient = new TetrisClient("TestPlayer", "localhost", port);
                assertNotNull(testClient, "다양한 포트 번호로 클라이언트 생성 가능: " + port);
                assertFalse(testClient.isConnected(), "초기 상태는 연결되지 않음");
            }, "포트 번호: " + port + "가 안전하게 처리되어야 함");
        }
    }

    @Test
    @DisplayName("메시지 리스너 설정 테스트")
    void testMessageListenerSetting() {
        // null 리스너 설정
        assertDoesNotThrow(() -> {
            client.setMessageListener(null);
        }, "null 리스너 설정이 안전해야 함");
        
        // 다른 리스너로 교체
        ClientMessageListener newListener = mock(ClientMessageListener.class);
        assertDoesNotThrow(() -> {
            client.setMessageListener(newListener);
        }, "리스너 교체가 안전해야 함");
        
        // 같은 리스너 재설정
        assertDoesNotThrow(() -> {
            client.setMessageListener(newListener);
        }, "같은 리스너 재설정이 안전해야 함");
    }

    @Test
    @DisplayName("연결되지 않은 상태에서 다양한 메서드 호출")
    void testMethodsWhenNotConnected() {
        // 연결 상태 확인
        assertFalse(client.isConnected(), "초기 상태는 연결되지 않음");
        
        // 메시지 전송 시도
        assertFalse(client.sendMessage(ConnectionMessage.createPlayerReady("TestPlayer")), 
                   "연결되지 않은 상태에서 메시지 전송 실패");
        
        // 준비 요청
        assertFalse(client.requestReady(), "연결되지 않은 상태에서 준비 요청 실패");
        
        // 게임 일시정지 요청
        assertFalse(client.pauseGame(), "연결되지 않은 상태에서 일시정지 요청 실패");
        
        // 게임 재개 요청
        assertFalse(client.resumeGame(), "연결되지 않은 상태에서 재개 요청 실패");
        
        // 보드 업데이트 전송 (연결되지 않은 상태에서는 실패 예상)
        assertFalse(client.sendBoardUpdate(new int[20][10], 5, 2, 1, 0, 2, new java.util.LinkedList<>(), 100, 0, 1), 
                   "연결되지 않은 상태에서 보드 업데이트 전송 실패");
        
        // 공격 메시지 전송 (일반 메시지로 전송)
        AttackMessage attack = new AttackMessage("TestPlayer", 2, 2);
        assertFalse(client.sendMessage(attack), "연결되지 않은 상태에서 공격 메시지 전송 실패");
        
        // 연결 해제 (이미 연결되지 않은 상태)
        assertDoesNotThrow(() -> {
            client.disconnect();
        }, "연결되지 않은 상태에서 연결 해제는 안전해야 함");
    }

    @Test
    @DisplayName("클라이언트 상태 일관성 테스트")
    void testClientStateConsistency() {
        // 초기 상태 확인
        assertFalse(client.isConnected(), "초기 상태는 연결되지 않음");
        
        // 연결 시도 (실패 예상)
        boolean connectResult = client.connect();
        assertFalse(connectResult, "서버가 없으므로 연결 실패 예상");
        assertFalse(client.isConnected(), "연결 실패 후에도 연결되지 않은 상태");
        
        // 연결 해제 후 상태 확인
        client.disconnect();
        assertFalse(client.isConnected(), "연결 해제 후 연결되지 않은 상태");
    }

    @Test
    @DisplayName("동시성 안전성 테스트")
    void testConcurrencySafety() {
        int threadCount = 5;
        Thread[] threads = new Thread[threadCount];
        
        assertDoesNotThrow(() -> {
            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    TetrisClient threadClient = new TetrisClient("Player" + index, "localhost", 12347 + index);
                    
                    // 동시에 여러 작업 수행
                    threadClient.setMessageListener(mock(ClientMessageListener.class));
                    threadClient.connect(); // 실패 예상
                    threadClient.requestReady(); // 실패 예상
                    threadClient.disconnect();
                });
                threads[i].start();
            }
            
            // 모든 스레드 완료 대기
            for (Thread thread : threads) {
                thread.join(2000);
            }
        }, "동시에 여러 클라이언트가 안전하게 동작해야 함");
    }

    @Test
    @DisplayName("메모리 효율성 테스트")
    void testMemoryEfficiency() {
        TetrisClient[] clients = new TetrisClient[50];
        
        assertDoesNotThrow(() -> {
            // 많은 클라이언트 생성
            for (int i = 0; i < clients.length; i++) {
                clients[i] = new TetrisClient("Player" + i, "localhost", 13000 + i);
                assertNotNull(clients[i], "메모리 효율적으로 클라이언트 생성: " + i);
                assertFalse(clients[i].isConnected(), "초기 상태는 연결되지 않음");
            }
            
            // 참조 해제
            for (int i = 0; i < clients.length; i++) {
                clients[i] = null;
            }
            
            // 가비지 컬렉션 힌트
            System.gc();
            Thread.sleep(100);
        }, "메모리 효율적으로 동작해야 함");
    }

    @Test
    @DisplayName("극한 상황에서의 안정성 테스트")
    void testExtremeConditions() {
        assertDoesNotThrow(() -> {
            // 매우 긴 문자열 사용
            String longPlayerId = "VeryLongPlayerName".repeat(50);
            String longHostName = "very-long-hostname".repeat(10) + ".com";
            
            TetrisClient extremeClient = new TetrisClient(longPlayerId, longHostName, 12348);
            assertNotNull(extremeClient, "극한 조건에서도 클라이언트 생성 가능");
            assertFalse(extremeClient.isConnected(), "초기 상태는 연결되지 않음");
            
            // 극한 조건에서 메서드 호출
            extremeClient.setMessageListener(mockListener);
            extremeClient.connect(); // 실패 예상
            extremeClient.disconnect();
        }, "극한 조건에서도 안전하게 동작해야 함");
    }

    @Test
    @DisplayName("null 매개변수 처리 테스트")
    void testNullParameterHandling() {
        assertDoesNotThrow(() -> {
            // null 플레이어 ID
            TetrisClient nullPlayerClient = new TetrisClient(null, "localhost", 12345);
            assertNotNull(nullPlayerClient, "null 플레이어 ID로도 클라이언트 생성 가능");
            
            // null 호스트
            TetrisClient nullHostClient = new TetrisClient("TestPlayer", null);
            assertNotNull(nullHostClient, "null 호스트로도 클라이언트 생성 가능");
            
            // 모두 null
            TetrisClient allNullClient = new TetrisClient(null, null, 12349);
            assertNotNull(allNullClient, "null 매개변수들로도 클라이언트 생성 가능");
        }, "null 매개변수가 안전하게 처리되어야 함");
    }

    @Test
    @DisplayName("클라이언트 인스턴스 독립성 테스트")
    void testClientInstanceIndependence() {
        // 여러 클라이언트 생성
        TetrisClient client1 = new TetrisClient("Player1", "localhost", 12350);
        TetrisClient client2 = new TetrisClient("Player2", "localhost", 12351);
        TetrisClient client3 = new TetrisClient("Player1", "localhost", 12350); // 같은 설정
        
        // 인스턴스 독립성 확인
        assertNotEquals(client1, client2, "다른 클라이언트 인스턴스는 독립적이어야 함");
        assertNotEquals(client1, client3, "같은 설정이라도 다른 인스턴스여야 함");
        assertNotEquals(client2, client3, "모든 인스턴스는 독립적이어야 함");
        
        // 각각 독립적으로 동작
        client1.setMessageListener(mock(ClientMessageListener.class));
        client2.setMessageListener(mock(ClientMessageListener.class));
        
        assertFalse(client1.isConnected(), "client1 초기 상태");
        assertFalse(client2.isConnected(), "client2 초기 상태");
        assertFalse(client3.isConnected(), "client3 초기 상태");
    }

    @Test
    @DisplayName("연결 시도 실패 처리 테스트")
    void testConnectionFailureHandling() {
        // 존재하지 않는 서버에 연결 시도
        TetrisClient unreachableClient = new TetrisClient("TestPlayer", "non-existent-server.invalid", 99999);
        
        assertDoesNotThrow(() -> {
            boolean result = unreachableClient.connect();
            assertFalse(result, "존재하지 않는 서버에 연결 시도는 실패해야 함");
            assertFalse(unreachableClient.isConnected(), "연결 실패 후 상태 확인");
        }, "연결 실패가 안전하게 처리되어야 함");
    }

    @Test
    @DisplayName("메시지 전송 실패 처리 테스트")
    void testMessageSendingFailure() {
        // 다양한 메시지 타입으로 전송 시도 (연결되지 않은 상태)
        ConnectionMessage readyMessage = ConnectionMessage.createPlayerReady("TestPlayer");
        assertFalse(client.sendMessage(readyMessage), "준비 메시지 전송 실패");
        
        ConnectionMessage disconnectMessage = new ConnectionMessage(MessageType.DISCONNECT, "TestPlayer", "Test disconnect");
        assertFalse(client.sendMessage(disconnectMessage), "연결 해제 메시지 전송 실패");
        
        // null 메시지 전송 시도
        assertFalse(client.sendMessage(null), "null 메시지 전송 실패");
    }
}