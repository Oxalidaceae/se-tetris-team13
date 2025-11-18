package team13.tetris.game.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import team13.tetris.config.Settings;
import team13.tetris.network.protocol.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class NetworkGameControllerLogicTest {

    private NetworkGameController hostController;
    private NetworkGameController clientController;
    private Settings settings;

    @BeforeEach
    void setUp() {
        settings = new Settings();
        hostController = new NetworkGameController(null, settings, true, "127.0.0.1");
        clientController = new NetworkGameController(null, settings, false, "192.168.1.100");
    }

    @Test
    @DisplayName("컨트롤러 생성 및 기본 상태 테스트")
    void testControllerCreationAndInitialState() {
        assertNotNull(hostController, "호스트 컨트롤러가 생성되어야 함");
        assertNotNull(clientController, "클라이언트 컨트롤러가 생성되어야 함");
        
        // 초기 상태에서는 JavaFX에 의존하지 않는 메서드들이 안전하게 동작해야 함
        assertDoesNotThrow(() -> {
            hostController.disconnect();
            clientController.disconnect();
        }, "초기 상태에서 disconnect는 안전해야 함");
    }

    @Test
    @DisplayName("게임 상태 변경 이벤트 처리 테스트")
    void testGameStateEventHandling() {
        // 연결 수락
        assertDoesNotThrow(() -> {
            hostController.onConnectionAccepted();
            clientController.onConnectionAccepted();
        }, "연결 수락 이벤트 처리는 안전해야 함");

        // 게임 시작
        assertDoesNotThrow(() -> {
            hostController.onGameStart();
            clientController.onGameStart();
        }, "게임 시작 이벤트 처리는 안전해야 함");

        // 게임 일시정지/재개
        assertDoesNotThrow(() -> {
            hostController.onGamePaused();
            hostController.onGameResumed();
            clientController.onGamePaused();
            clientController.onGameResumed();
        }, "게임 일시정지/재개 이벤트 처리는 안전해야 함");

        // 게임 종료
        assertDoesNotThrow(() -> {
            hostController.onGameOver("Host game over");
            clientController.onGameOver("Client game over");
        }, "게임 종료 이벤트 처리는 안전해야 함");
    }

    @Test
    @DisplayName("네트워크 메시지 처리 테스트")
    void testNetworkMessageHandling() {
        // 에러 메시지 처리
        assertDoesNotThrow(() -> {
            hostController.onError("Test error message");
            clientController.onError("Network connection failed");
        }, "에러 메시지 처리는 안전해야 함");

        // 게임 모드 선택 메시지 처리
        assertDoesNotThrow(() -> {
            hostController.onGameModeSelected(GameModeMessage.GameMode.NORMAL);
            hostController.onGameModeSelected(GameModeMessage.GameMode.ITEM);
            clientController.onGameModeSelected(GameModeMessage.GameMode.NORMAL);
            clientController.onGameModeSelected(GameModeMessage.GameMode.ITEM);
        }, "게임 모드 선택 메시지 처리는 안전해야 함");

        // 서버 메시지 리스너 이벤트 (호스트만)
        assertDoesNotThrow(() -> {
            hostController.onClientDisconnected("TestClient");
        }, "클라이언트 연결 해제 이벤트 처리는 안전해야 함");
    }

    @Test
    @DisplayName("보드 업데이트 메시지 처리 테스트")
    void testBoardUpdateMessageHandling() {
        // 다양한 보드 상태 메시지 테스트
        int[][] testBoard1 = createTestBoard(10, 20, 0);
        int[][] testBoard2 = createTestBoard(10, 20, 1);
        int[][] largeBoard = createTestBoard(50, 100, 2);

        BoardUpdateMessage message1 = new BoardUpdateMessage(
            "Player1", testBoard1, 5, 10, 1, 0, 2, null, 1000, 5, 1
        );
        BoardUpdateMessage message2 = new BoardUpdateMessage(
            "Player2", testBoard2, 3, 8, 2, 1, 1, null, 2500, 12, 2
        );
        BoardUpdateMessage largeMessage = new BoardUpdateMessage(
            "Player3", largeBoard, 25, 50, 3, 2, 4, null, 50000, 100, 10
        );

        assertDoesNotThrow(() -> {
            hostController.onBoardUpdate(message1);
            hostController.onBoardUpdate(message2);
            hostController.onBoardUpdate(largeMessage);
            
            clientController.onBoardUpdate(message1);
            clientController.onBoardUpdate(message2);
            clientController.onBoardUpdate(largeMessage);
        }, "다양한 보드 업데이트 메시지 처리는 안전해야 함");
    }

    @Test
    @DisplayName("공격 메시지 처리 테스트")
    void testAttackMessageHandling() {
        // 다양한 공격 메시지 테스트
        AttackMessage singleLineAttack = new AttackMessage("Attacker1", 1, 1);
        AttackMessage multiLineAttack = new AttackMessage("Attacker2", 4, 4);
        AttackMessage maxLineAttack = new AttackMessage("Attacker3", 10, 10);
        AttackMessage specialAttack = AttackMessage.createStandardAttack("Attacker4", 2);

        assertDoesNotThrow(() -> {
            hostController.onAttackReceived(singleLineAttack);
            hostController.onAttackReceived(multiLineAttack);
            hostController.onAttackReceived(maxLineAttack);
            hostController.onAttackReceived(specialAttack);
            
            clientController.onAttackReceived(singleLineAttack);
            clientController.onAttackReceived(multiLineAttack);
            clientController.onAttackReceived(maxLineAttack);
            clientController.onAttackReceived(specialAttack);
        }, "다양한 공격 메시지 처리는 안전해야 함");
    }

    @Test
    @DisplayName("극한 상황 메시지 처리 테스트")
    void testExtremeMessageHandling() {
        // 빈 메시지들
        assertDoesNotThrow(() -> {
            hostController.onGameOver("");
            hostController.onError("");
            clientController.onGameOver("");
            clientController.onError("");
        }, "빈 문자열 메시지 처리는 안전해야 함");

        // 매우 긴 메시지들
        String longMessage = "Very long message ".repeat(1000);
        assertDoesNotThrow(() -> {
            hostController.onGameOver(longMessage);
            hostController.onError(longMessage);
            clientController.onGameOver(longMessage);
            clientController.onError(longMessage);
        }, "긴 메시지 처리는 안전해야 함");

        // 특수 문자가 포함된 메시지들
        String specialMessage = "Special characters: 🎮🎯🎲 가나다라마바사 !@#$%^&*()";
        assertDoesNotThrow(() -> {
            hostController.onGameOver(specialMessage);
            hostController.onError(specialMessage);
            clientController.onGameOver(specialMessage);
            clientController.onError(specialMessage);
        }, "특수 문자 메시지 처리는 안전해야 함");
    }

    @Test
    @DisplayName("동시성 테스트 - 여러 메시지 동시 처리")
    void testConcurrentMessageHandling() throws InterruptedException {
        int threadCount = 5; // 스레드 수 줄임
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            new Thread(() -> {
                try {
                    // 다양한 메시지를 동시에 처리
                    hostController.onConnectionAccepted();
                    hostController.onGameStart();
                    hostController.onGamePaused();
                    hostController.onGameResumed();
                    hostController.onGameOver("Thread " + index + " game over");
                    
                    AttackMessage attack = new AttackMessage("Thread" + index, index % 4 + 1, index % 4 + 1);
                    hostController.onAttackReceived(attack);
                    
                    hostController.onError("Thread " + index + " error");
                    hostController.onGameModeSelected(
                        index % 2 == 0 ? GameModeMessage.GameMode.NORMAL : GameModeMessage.GameMode.ITEM
                    );
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        
        assertTrue(latch.await(3, TimeUnit.SECONDS), "모든 동시 메시지 처리가 완료되어야 함");
    }

    @Test
    @DisplayName("null 값 처리 테스트")
    void testNullValueHandling() {
        // null 메시지 처리
        assertDoesNotThrow(() -> {
            hostController.onGameOver(null);
            hostController.onError(null);
            clientController.onGameOver(null);
            clientController.onError(null);
        }, "null 문자열 메시지 처리는 안전해야 함");

        // null 보드 업데이트 처리
        assertDoesNotThrow(() -> {
            hostController.onBoardUpdate(null);
            clientController.onBoardUpdate(null);
        }, "null 보드 업데이트 메시지 처리는 안전해야 함");

        // null 공격 메시지 처리 (NullPointerException 예상)
        assertThrows(NullPointerException.class, () -> {
            hostController.onAttackReceived(null);
        }, "null 공격 메시지는 NullPointerException을 발생시켜야 함");

        assertThrows(NullPointerException.class, () -> {
            clientController.onAttackReceived(null);
        }, "null 공격 메시지는 NullPointerException을 발생시켜야 함");

        // null 게임 모드 처리
        assertDoesNotThrow(() -> {
            hostController.onGameModeSelected(null);
            clientController.onGameModeSelected(null);
        }, "null 게임 모드 메시지 처리는 안전해야 함");
    }

    @Test
    @DisplayName("연속 이벤트 처리 테스트")
    void testSequentialEventHandling() {
        // 게임 시작부터 종료까지의 시나리오
        assertDoesNotThrow(() -> {
            // 연결
            hostController.onConnectionAccepted();
            clientController.onConnectionAccepted();
            
            // 게임 모드 선택
            hostController.onGameModeSelected(GameModeMessage.GameMode.ITEM);
            clientController.onGameModeSelected(GameModeMessage.GameMode.ITEM);
            
            // 게임 시작
            hostController.onGameStart();
            clientController.onGameStart();
            
            // 게임 진행 (보드 업데이트와 공격 반복)
            for (int i = 0; i < 10; i++) {
                int[][] board = createTestBoard(10, 20, i);
                BoardUpdateMessage boardMsg = new BoardUpdateMessage(
                    "Player" + i, board, i % 10, i % 20, i % 7, i % 4, (i + 1) % 7, null, i * 100, i, i / 2
                );
                hostController.onBoardUpdate(boardMsg);
                clientController.onBoardUpdate(boardMsg);
                
                if (i % 3 == 0) {
                    AttackMessage attackMsg = new AttackMessage("Attacker" + i, i % 4 + 1, i % 4 + 1);
                    hostController.onAttackReceived(attackMsg);
                    clientController.onAttackReceived(attackMsg);
                }
            }
            
            // 일시정지/재개
            hostController.onGamePaused();
            clientController.onGamePaused();
            
            hostController.onGameResumed();
            clientController.onGameResumed();
            
            // 게임 종료
            hostController.onGameOver("Game completed");
            clientController.onGameOver("Game completed");
            
        }, "게임 전체 시나리오가 안전하게 처리되어야 함");
    }

    @Test
    @DisplayName("메모리 효율성 테스트 - 대량 메시지 처리")
    void testMemoryEfficiency() {
        assertDoesNotThrow(() -> {
            // 대량의 보드 업데이트 처리 (반복 횟수 줄임)
            for (int i = 0; i < 200; i++) {
                int[][] board = createTestBoard(10, 20, i % 8);
                BoardUpdateMessage msg = new BoardUpdateMessage(
                    "Player" + (i % 10), board, i % 10, i % 20, i % 7, i % 4, (i + 1) % 7, null, i, i / 10, i / 100
                );
                hostController.onBoardUpdate(msg);
                
                if (i % 50 == 0) {
                    // 주기적으로 가비지 컬렉션 힌트
                    System.gc();
                }
            }
            
            // 대량의 공격 메시지 처리 (반복 횟수 줄임)
            for (int i = 0; i < 100; i++) {
                AttackMessage attackMsg = new AttackMessage("Attacker" + (i % 5), i % 4 + 1, i % 4 + 1);
                hostController.onAttackReceived(attackMsg);
            }
            
        }, "대량 메시지 처리가 안전하게 완료되어야 함");
    }

    @Test
    @DisplayName("다양한 IP 주소와 서버 설정 테스트")
    void testVariousServerSettings() {
        String[] testIPs = {"127.0.0.1", "192.168.1.1", "10.0.0.1", "localhost", "example.com"};
        
        for (String ip : testIPs) {
            assertDoesNotThrow(() -> {
                NetworkGameController testHost = new NetworkGameController(null, settings, true, ip);
                NetworkGameController testClient = new NetworkGameController(null, settings, false, ip);
                
                assertNotNull(testHost, "호스트 컨트롤러가 IP " + ip + "로 생성되어야 함");
                assertNotNull(testClient, "클라이언트 컨트롤러가 IP " + ip + "로 생성되어야 함");
                
                // 기본 동작 테스트
                testHost.onConnectionAccepted();
                testClient.onConnectionAccepted();
                
                testHost.disconnect();
                testClient.disconnect();
                
            }, "IP 주소 " + ip + "로 컨트롤러 생성 및 동작은 안전해야 함");
        }
    }

    // 테스트용 보드 생성 헬퍼 메서드
    private int[][] createTestBoard(int width, int height, int pattern) {
        int[][] board = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                board[x][y] = (x + y + pattern) % 8; // 0-7 범위의 다양한 값
            }
        }
        return board;
    }
}