package team13.tetris.network.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import team13.tetris.network.protocol.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class TestClientMessageListenerTest {

    private TestClientMessageListener listener;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errorStreamCaptor = new ByteArrayOutputStream();
    private final PrintStream originalSystemOut = System.out;
    private final PrintStream originalSystemErr = System.err;

    @BeforeEach
    void setUp() {
        listener = new TestClientMessageListener();
        System.setOut(new PrintStream(outputStreamCaptor));
        System.setErr(new PrintStream(errorStreamCaptor));
    }

    void tearDown() {
        System.setOut(originalSystemOut);
        System.setErr(originalSystemErr);
    }

    @Test
    @DisplayName("연결 수락 메시지 처리 테스트")
    void testOnConnectionAccepted() {
        // when
        listener.onConnectionAccepted();
        
        // then
        assertTrue(outputStreamCaptor.toString().contains("Successfully connected to server!"));
        tearDown();
    }

    @Test
    @DisplayName("연결 거부 메시지 처리 테스트")
    void testOnConnectionRejected() {
        // given
        String reason = "Server is full";
        
        // when
        listener.onConnectionRejected(reason);
        
        // then
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Connection rejected: " + reason));
        tearDown();
    }

    @Test
    @DisplayName("다양한 연결 거부 사유 처리 테스트")
    void testOnConnectionRejectedVariousReasons() {
        String[] reasons = {
            "Game already started",
            "Invalid player ID",
            "Server error",
            null,
            "",
            "Very long rejection reason that might cause issues with display"
        };
        
        for (String reason : reasons) {
            outputStreamCaptor.reset();
            
            assertDoesNotThrow(() -> {
                listener.onConnectionRejected(reason);
                String output = outputStreamCaptor.toString();
                assertTrue(output.contains("Connection rejected:"));
            }, "다양한 거부 사유가 안전하게 처리되어야 함: " + reason);
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
            "Player_With_Special_Chars@#$",
            "123456",
            "",
            null,
            "VeryLongPlayerNameThatMightCauseDisplayIssues"
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
        assertTrue(outputStreamCaptor.toString().contains("Game started! You can now play."));
        tearDown();
    }

    @Test
    @DisplayName("게임 종료 메시지 처리 테스트")
    void testOnGameOver() {
        // given
        String reason = "Player disconnected";
        
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
            "Time's up",
            "Player quit",
            "Network error",
            "Server shutdown",
            null,
            "",
            "Very detailed game over reason with lots of information"
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
        BoardUpdateMessage boardUpdate = new BoardUpdateMessage("Player1", new int[20][10], 5, 2, 
                                                                1, 0, 2, new java.util.LinkedList<>(), 1500, 5, 1);
        
        // when
        listener.onBoardUpdate(boardUpdate);
        
        // then
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Opponent board updated - Score: 1500"));
        tearDown();
    }

    @Test
    @DisplayName("다양한 점수로 보드 업데이트 메시지 처리 테스트")
    void testOnBoardUpdateVariousScores() {
        int[] scores = {0, 100, 1500, 10000, 999999, -1};
        
        for (int score : scores) {
            outputStreamCaptor.reset();
            
            assertDoesNotThrow(() -> {
                BoardUpdateMessage boardUpdate = new BoardUpdateMessage("TestPlayer", new int[20][10], 5, 2,
                                                                        1, 0, 2, new java.util.LinkedList<>(), score, 0, 1);
                listener.onBoardUpdate(boardUpdate);
                String output = outputStreamCaptor.toString();
                assertTrue(output.contains("Opponent board updated - Score: " + score));
            }, "다양한 점수가 안전하게 처리되어야 함: " + score);
        }
        tearDown();
    }

    @Test
    @DisplayName("공격 메시지 처리 테스트")
    void testOnAttackReceived() {
        // given
        AttackMessage attackMessage = new AttackMessage("Attacker", 3, 3);
        
        // when
        listener.onAttackReceived(attackMessage);
        
        // then
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Attack received: 3 lines!"));
        tearDown();
    }

    @Test
    @DisplayName("다양한 공격 라인 수로 공격 메시지 처리 테스트")
    void testOnAttackReceivedVariousLines() {
        int[] attackLines = {1, 2, 3, 4, 0, -1, 100};
        
        for (int lines : attackLines) {
            outputStreamCaptor.reset();
            
            assertDoesNotThrow(() -> {
                int validSourceLines = Math.max(1, Math.abs(lines));
                int validAttackLines = Math.max(0, lines);
                if (validSourceLines > 10) validSourceLines = 4;
                if (validAttackLines > 10) validAttackLines = 4;
                
                AttackMessage attackMessage = new AttackMessage("Attacker", validSourceLines, validAttackLines);
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
        assertTrue(outputStreamCaptor.toString().contains("[PAUSE]Game paused"));
        tearDown();
    }

    @Test
    @DisplayName("게임 재개 메시지 처리 테스트")
    void testOnGameResumed() {
        // when
        listener.onGameResumed();
        
        // then
        assertTrue(outputStreamCaptor.toString().contains("[PLAY]Game resumed"));
        tearDown();
    }

    @Test
    @DisplayName("오류 메시지 처리 테스트")
    void testOnError() {
        // given
        String error = "Network connection lost";
        
        // when
        listener.onError(error);
        
        // then
        String errorOutput = errorStreamCaptor.toString();
        assertTrue(errorOutput.contains("Error: " + error));
        tearDown();
    }

    @Test
    @DisplayName("다양한 오류 메시지 처리 테스트")
    void testOnErrorVariousMessages() {
        String[] errorMessages = {
            "Connection timeout",
            "Invalid message format",
            "Server overloaded",
            null,
            "",
            "Very detailed error message with technical information"
        };
        
        for (String error : errorMessages) {
            errorStreamCaptor.reset();
            
            assertDoesNotThrow(() -> {
                listener.onError(error);
                String errorOutput = errorStreamCaptor.toString();
                assertTrue(errorOutput.contains("Error:"));
            }, "다양한 오류 메시지가 안전하게 처리되어야 함: " + error);
        }
        tearDown();
    }

    @Test
    @DisplayName("게임 모드 선택 메시지 처리 테스트")
    void testOnGameModeSelected() {
        // given
        GameModeMessage.GameMode gameMode = GameModeMessage.GameMode.NORMAL;
        
        // when
        listener.onGameModeSelected(gameMode);
        
        // then
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Server selected game mode: " + gameMode));
        tearDown();
    }

    @Test
    @DisplayName("모든 게임 모드 선택 메시지 처리 테스트")
    void testOnGameModeSelectedAllModes() {
        GameModeMessage.GameMode[] gameModes = GameModeMessage.GameMode.values();
        
        for (GameModeMessage.GameMode mode : gameModes) {
            outputStreamCaptor.reset();
            
            assertDoesNotThrow(() -> {
                listener.onGameModeSelected(mode);
                String output = outputStreamCaptor.toString();
                assertTrue(output.contains("Server selected game mode: " + mode));
            }, "모든 게임 모드가 안전하게 처리되어야 함: " + mode);
        }
        tearDown();
    }

    @Test
    @DisplayName("연속적인 메시지 처리 테스트")
    void testSequentialMessageHandling() {
        // when
        listener.onConnectionAccepted();
        listener.onPlayerReady("Player1");
        listener.onGameStart();
        listener.onBoardUpdate(new BoardUpdateMessage("Player1", new int[20][10], 5, 2, 1, 0, 2, new java.util.LinkedList<>(), 500, 0, 1));
        listener.onAttackReceived(new AttackMessage("Player1", 2, 2));
        listener.onGamePaused();
        listener.onGameResumed();
        listener.onGameOver("Game completed");
        
        // then
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Successfully connected to server!"));
        assertTrue(output.contains("Player1 is ready!"));
        assertTrue(output.contains("Game started!"));
        assertTrue(output.contains("Opponent board updated - Score: 500"));
        assertTrue(output.contains("Attack received: 2 lines!"));
        assertTrue(output.contains("[PAUSE]Game paused"));
        assertTrue(output.contains("[PLAY]Game resumed"));
        assertTrue(output.contains("Game over: Game completed"));
        
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
        
        assertDoesNotThrow(() -> {
            listener.onGameModeSelected(null);
        }, "null GameMode가 안전하게 처리되어야 함");
        
        tearDown();
    }
}