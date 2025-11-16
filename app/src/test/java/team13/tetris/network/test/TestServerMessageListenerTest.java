package team13.tetris.network.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import team13.tetris.network.protocol.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class TestServerMessageListenerTest {

    private TestServerMessageListener listener;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final PrintStream originalSystemOut = System.out;

    @BeforeEach
    void setUp() {
        listener = new TestServerMessageListener();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    void tearDown() {
        System.setOut(originalSystemOut);
    }

    @Test
    @DisplayName("클라이언트 연결 메시지 처리 테스트")
    void testOnClientConnected() {
        // given
        String clientId = "TestClient";
        
        // when
        listener.onClientConnected(clientId);
        
        // then
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Client connected: " + clientId));
        tearDown();
    }

    @Test
    @DisplayName("다양한 클라이언트 ID로 연결 메시지 처리 테스트")
    void testOnClientConnectedVariousIds() {
        String[] clientIds = {
            "Client1",
            "한글클라이언트",
            "Client_With_Special_Chars@#$",
            "123456",
            "",
            null,
            "VeryLongClientNameThatMightCauseDisplayIssues"
        };
        
        for (String clientId : clientIds) {
            outputStreamCaptor.reset();
            
            assertDoesNotThrow(() -> {
                listener.onClientConnected(clientId);
                String output = outputStreamCaptor.toString();
                assertTrue(output.contains("Client connected:"));
            }, "다양한 클라이언트 ID가 안전하게 처리되어야 함: " + clientId);
        }
        tearDown();
    }

    @Test
    @DisplayName("클라이언트 연결 해제 메시지 처리 테스트")
    void testOnClientDisconnected() {
        // given
        String clientId = "TestClient";
        
        // when
        listener.onClientDisconnected(clientId);
        
        // then
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Client disconnected: " + clientId));
        tearDown();
    }

    @Test
    @DisplayName("다양한 클라이언트 ID로 연결 해제 메시지 처리 테스트")
    void testOnClientDisconnectedVariousIds() {
        String[] clientIds = {
            "Client1",
            "DisconnectedClient",
            "Client_Timeout",
            "",
            null,
            "ClientWithNetworkIssues"
        };
        
        for (String clientId : clientIds) {
            outputStreamCaptor.reset();
            
            assertDoesNotThrow(() -> {
                listener.onClientDisconnected(clientId);
                String output = outputStreamCaptor.toString();
                assertTrue(output.contains("Client disconnected:"));
            }, "다양한 클라이언트 ID가 안전하게 처리되어야 함: " + clientId);
        }
        tearDown();
    }

    @Test
    @DisplayName("플레이어 준비 상태 메시지 처리 테스트")
    void testOnPlayerReady() {
        // given
        String playerId = "TestPlayer";
        
        // when
        listener.onPlayerReady(playerId);
        
        // then
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains(playerId + " is ready!"));
        tearDown();
    }

    @Test
    @DisplayName("다양한 플레이어 ID로 준비 상태 메시지 처리 테스트")
    void testOnPlayerReadyVariousIds() {
        String[] playerIds = {
            "Player1",
            "한글플레이어",
            "Player_Ready",
            "123",
            "",
            null
        };
        
        for (String playerId : playerIds) {
            outputStreamCaptor.reset();
            
            assertDoesNotThrow(() -> {
                listener.onPlayerReady(playerId);
                String output = outputStreamCaptor.toString();
                assertTrue(output.contains(" is ready!"));
            }, "다양한 플레이어 ID가 안전하게 처리되어야 함: " + playerId);
        }
        tearDown();
    }

    @Test
    @DisplayName("게임 시작 메시지 처리 테스트")
    void testOnGameStart() {
        // when
        listener.onGameStart();
        
        // then
        assertTrue(outputStreamCaptor.toString().contains("Game started! You can now play as host."));
        tearDown();
    }

    @Test
    @DisplayName("게임 종료 메시지 처리 테스트")
    void testOnGameOver() {
        // given
        String reason = "Client disconnected";
        
        // when
        listener.onGameOver(reason);
        
        // then
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Game over: " + reason));
        tearDown();
    }

    @Test
    @DisplayName("다양한 게임 종료 사유 처리 테스트")
    void testOnGameOverVariousReasons() {
        String[] reasons = {
            "Host disconnected",
            "Network error",
            "Game completed",
            "Time limit exceeded",
            null,
            ""
        };
        
        for (String reason : reasons) {
            outputStreamCaptor.reset();
            
            assertDoesNotThrow(() -> {
                listener.onGameOver(reason);
                String output = outputStreamCaptor.toString();
                assertTrue(output.contains("Game over:"));
            }, "다양한 게임 종료 사유가 안전하게 처리되어야 함: " + reason);
        }
        tearDown();
    }

    @Test
    @DisplayName("보드 업데이트 메시지 처리 테스트")
    void testOnBoardUpdate() {
        // given
        BoardUpdateMessage boardUpdate = new BoardUpdateMessage("Client1", new int[20][10], 5, 2,
                                                                1, 0, 2, new java.util.LinkedList<>(), 2000, 5, 1);
        
        // when
        listener.onBoardUpdate(boardUpdate);
        
        // then
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Client board updated - Score: 2000"));
        tearDown();
    }

    @Test
    @DisplayName("다양한 점수로 보드 업데이트 메시지 처리 테스트")
    void testOnBoardUpdateVariousScores() {
        int[] scores = {0, 50, 1000, 5000, 99999, -1};
        
        for (int score : scores) {
            outputStreamCaptor.reset();
            
            assertDoesNotThrow(() -> {
                BoardUpdateMessage boardUpdate = new BoardUpdateMessage("Client", new int[20][10], 5, 2,
                                                                        1, 0, 2, new java.util.LinkedList<>(), score, 0, 1);
                listener.onBoardUpdate(boardUpdate);
                String output = outputStreamCaptor.toString();
                assertTrue(output.contains("Client board updated - Score: " + score));
            }, "다양한 점수가 안전하게 처리되어야 함: " + score);
        }
        tearDown();
    }

    @Test
    @DisplayName("공격 메시지 처리 테스트")
    void testOnAttackReceived() {
        // given
        AttackMessage attackMessage = new AttackMessage("Client1", 4, 4);
        
        // when
        listener.onAttackReceived(attackMessage);
        
        // then
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Attack received: 4 lines!"));
        tearDown();
    }

    @Test
    @DisplayName("다양한 공격 라인 수로 공격 메시지 처리 테스트")
    void testOnAttackReceivedVariousLines() {
        int[] attackLines = {1, 2, 3, 4, 0, 10, -1};
        
        for (int lines : attackLines) {
            outputStreamCaptor.reset();
            
            assertDoesNotThrow(() -> {
                int validSourceLines = Math.max(1, Math.abs(lines));
                int validAttackLines = Math.max(0, lines);
                if (validSourceLines > 10) validSourceLines = 4;
                if (validAttackLines > 10) validAttackLines = 4;
                
                AttackMessage attackMessage = new AttackMessage("Client", validSourceLines, validAttackLines);
                listener.onAttackReceived(attackMessage);
                String output = outputStreamCaptor.toString();
                assertTrue(output.contains("Attack received: " + validAttackLines + " lines!"));
            }, "다양한 공격 라인 수가 안전하게 처리되어야 함: " + lines);
        }
        tearDown();
    }

    @Test
    @DisplayName("게임 일시정지 메시지 처리 테스트")
    void testOnGamePaused() {
        // when
        listener.onGamePaused();
        
        // then
        assertTrue(outputStreamCaptor.toString().contains("[PAUSE]Client paused the game"));
        tearDown();
    }

    @Test
    @DisplayName("게임 재개 메시지 처리 테스트")
    void testOnGameResumed() {
        // when
        listener.onGameResumed();
        
        // then
        assertTrue(outputStreamCaptor.toString().contains("[PLAY]Client resumed the game"));
        tearDown();
    }

    @Test
    @DisplayName("연속적인 메시지 처리 테스트")
    void testSequentialMessageHandling() {
        // when
        listener.onClientConnected("TestClient");
        listener.onPlayerReady("TestClient");
        listener.onGameStart();
        listener.onBoardUpdate(new BoardUpdateMessage("TestClient", new int[20][10], 5, 2, 1, 0, 2, new java.util.LinkedList<>(), 750, 0, 1));
        listener.onAttackReceived(new AttackMessage("TestClient", 3, 3));
        listener.onGamePaused();
        listener.onGameResumed();
        listener.onGameOver("Client won");
        listener.onClientDisconnected("TestClient");
        
        // then
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Client connected: TestClient"));
        assertTrue(output.contains("TestClient is ready!"));
        assertTrue(output.contains("Game started! You can now play as host."));
        assertTrue(output.contains("Client board updated - Score: 750"));
        assertTrue(output.contains("Attack received: 3 lines!"));
        assertTrue(output.contains("[PAUSE]Client paused the game"));
        assertTrue(output.contains("[PLAY]Client resumed the game"));
        assertTrue(output.contains("Game over: Client won"));
        assertTrue(output.contains("Client disconnected: TestClient"));
        
        tearDown();
    }

    @Test
    @DisplayName("null 메시지 객체 처리 테스트")
    void testNullMessageObjects() {
        // when & then
        assertDoesNotThrow(() -> {
            listener.onBoardUpdate(null);
        }, "null BoardUpdateMessage가 안전하게 처리되어야 함");
        
        assertDoesNotThrow(() -> {
            listener.onAttackReceived(null);
        }, "null AttackMessage가 안전하게 처리되어야 함");
        
        tearDown();
    }

    @Test
    @DisplayName("다중 클라이언트 연결/해제 시나리오 테스트")
    void testMultipleClientScenarios() {
        // when
        listener.onClientConnected("Client1");
        listener.onClientConnected("Client2");
        listener.onPlayerReady("Client1");
        listener.onPlayerReady("Client2");
        listener.onClientDisconnected("Client1");
        listener.onClientDisconnected("Client2");
        
        // then
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Client connected: Client1"));
        assertTrue(output.contains("Client connected: Client2"));
        assertTrue(output.contains("Client1 is ready!"));
        assertTrue(output.contains("Client2 is ready!"));
        assertTrue(output.contains("Client disconnected: Client1"));
        assertTrue(output.contains("Client disconnected: Client2"));
        
        tearDown();
    }

    @Test
    @DisplayName("극한 상황에서의 안정성 테스트")
    void testExtremeConditions() {
        // given
        String longClientId = "VeryLongClientIdThatExceedsNormalLimits".repeat(10);
        String longReason = "Very detailed game over reason with lots of information".repeat(5);
        
        // when & then
        assertDoesNotThrow(() -> {
            listener.onClientConnected(longClientId);
            listener.onGameOver(longReason);
            listener.onClientDisconnected(longClientId);
        }, "극한 조건에서도 안전하게 처리되어야 함");
        
        tearDown();
    }
}