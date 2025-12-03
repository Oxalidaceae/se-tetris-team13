package team13.tetris.network.protocol;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConnectionMessageTest {

    @Test
    @DisplayName("플레이어 준비 메시지 생성 테스트")
    void testCreatePlayerReady() {
        ConnectionMessage message = ConnectionMessage.createPlayerReady("TestPlayer");

        assertNotNull(message, "메시지가 생성되어야 함");
        assertEquals(MessageType.PLAYER_READY, message.getType(), "메시지 타입이 PLAYER_READY여야 함");
        assertEquals("TestPlayer", message.getSenderId(), "발신자 ID가 올바르게 설정되어야 함");
    }

    @Test
    @DisplayName("연결 요청 메시지 생성 테스트")
    void testCreateConnectionRequest() {
        ConnectionMessage message =
                ConnectionMessage.createConnectionRequest("TestPlayer", "Test message");

        assertNotNull(message, "메시지가 생성되어야 함");
        assertEquals(
                MessageType.CONNECTION_REQUEST,
                message.getType(),
                "메시지 타입이 CONNECTION_REQUEST여야 함");
        assertEquals("TestPlayer", message.getSenderId(), "발신자 ID가 올바르게 설정되어야 함");
    }

    @Test
    @DisplayName("연결 승인 메시지 생성 테스트")
    void testCreateConnectionAccepted() {
        ConnectionMessage message =
                ConnectionMessage.createConnectionAccepted("TestPlayer", "Welcome");

        assertNotNull(message, "메시지가 생성되어야 함");
        assertEquals(
                MessageType.CONNECTION_ACCEPTED,
                message.getType(),
                "메시지 타입이 CONNECTION_ACCEPTED여야 함");
        assertEquals("TestPlayer", message.getSenderId(), "발신자 ID가 올바르게 설정되어야 함");
    }

    @Test
    @DisplayName("연결 거절 메시지 생성 테스트")
    void testCreateConnectionRejected() {
        String reason = "Server is full";
        ConnectionMessage message =
                ConnectionMessage.createConnectionRejected("TestPlayer", reason);

        assertNotNull(message, "메시지가 생성되어야 함");
        assertEquals(
                MessageType.CONNECTION_REJECTED,
                message.getType(),
                "메시지 타입이 CONNECTION_REJECTED여야 함");
        assertEquals("TestPlayer", message.getSenderId(), "발신자 ID가 올바르게 설정되어야 함");
        assertEquals(reason, message.getMessage(), "거절 사유가 올바르게 설정되어야 함");
    }

    @Test
    @DisplayName("게임 시작 메시지 생성 테스트")
    void testCreateGameStart() {
        ConnectionMessage message = ConnectionMessage.createGameStart("Host");

        assertNotNull(message, "메시지가 생성되어야 함");
        assertEquals(MessageType.GAME_START, message.getType(), "메시지 타입이 GAME_START여야 함");
        assertEquals("Host", message.getSenderId(), "발신자 ID가 올바르게 설정되어야 함");
    }

    @Test
    @DisplayName("게임 종료 메시지 생성 테스트")
    void testCreateGameOver() {
        String reason = "Player disconnected";
        ConnectionMessage message = ConnectionMessage.createGameOver("TestPlayer", reason);

        assertNotNull(message, "메시지가 생성되어야 함");
        assertEquals(MessageType.GAME_OVER, message.getType(), "메시지 타입이 GAME_OVER여야 함");
        assertEquals("TestPlayer", message.getSenderId(), "발신자 ID가 올바르게 설정되어야 함");
        assertEquals(reason, message.getMessage(), "게임 종료 사유가 올바르게 설정되어야 함");
    }

    @Test
    @DisplayName("기본 생성자 테스트")
    void testDefaultConstructor() {
        ConnectionMessage message =
                new ConnectionMessage(MessageType.DISCONNECT, "TestPlayer", "Test message");

        assertNotNull(message, "메시지가 생성되어야 함");
        assertEquals(MessageType.DISCONNECT, message.getType(), "메시지 타입이 올바르게 설정되어야 함");
        assertEquals("TestPlayer", message.getSenderId(), "발신자 ID가 올바르게 설정되어야 함");
        assertEquals("Test message", message.getMessage(), "메시지 내용이 올바르게 설정되어야 함");
    }

    @Test
    @DisplayName("toString 메서드 테스트")
    void testToString() {
        ConnectionMessage message = ConnectionMessage.createPlayerReady("TestPlayer");
        String toString = message.toString();

        assertNotNull(toString, "toString 결과가 null이 아니어야 함");
        assertTrue(toString.contains("PLAYER_READY"), "toString에 메시지 타입이 포함되어야 함");
        assertTrue(toString.contains("TestPlayer"), "toString에 발신자 ID가 포함되어야 함");
    }

    @Test
    @DisplayName("카운트다운 시작 메시지 생성 테스트")
    void testCreateCountdownStart() {
        ConnectionMessage message = ConnectionMessage.createCountdownStart("Server");

        assertNotNull(message, "메시지가 생성되어야 함");
        assertEquals(MessageType.COUNTDOWN_START, message.getType(), "메시지 타입이 COUNTDOWN_START여야 함");
        assertEquals("Server", message.getSenderId(), "발신자 ID가 올바르게 설정되어야 함");
        assertEquals("Countdown starting!", message.getMessage(), "메시지 내용이 올바르게 설정되어야 함");
    }

    @Test
    @DisplayName("게임 일시정지 메시지 생성 테스트 - 사유 있음")
    void testCreateGamePauseWithReason() {
        String reason = "Player requested pause";
        ConnectionMessage message = ConnectionMessage.createGamePause("Player1", reason);

        assertNotNull(message, "메시지가 생성되어야 함");
        assertEquals(MessageType.PAUSE, message.getType(), "메시지 타입이 PAUSE여야 함");
        assertEquals("Player1", message.getSenderId(), "발신자 ID가 올바르게 설정되어야 함");
        assertEquals(reason, message.getMessage(), "일시정지 사유가 올바르게 설정되어야 함");
    }

    @Test
    @DisplayName("게임 일시정지 메시지 생성 테스트 - 사유 없음")
    void testCreateGamePauseWithoutReason() {
        ConnectionMessage message = ConnectionMessage.createGamePause("Player1", null);

        assertNotNull(message, "메시지가 생성되어야 함");
        assertEquals(MessageType.PAUSE, message.getType(), "메시지 타입이 PAUSE여야 함");
        assertEquals("Game paused", message.getMessage(), "기본 일시정지 메시지가 설정되어야 함");
    }

    @Test
    @DisplayName("게임 재개 메시지 생성 테스트")
    void testCreateGameResume() {
        ConnectionMessage message = ConnectionMessage.createGameResume("Server");

        assertNotNull(message, "메시지가 생성되어야 함");
        assertEquals(MessageType.RESUME, message.getType(), "메시지 타입이 RESUME이어야 함");
        assertEquals("Server", message.getSenderId(), "발신자 ID가 올바르게 설정되어야 함");
        assertEquals("Game resumed", message.getMessage(), "메시지 내용이 올바르게 설정되어야 함");
    }

    @Test
    @DisplayName("대상 플레이어 ID 가져오기 테스트")
    void testGetTargetPlayerId() {
        ConnectionMessage message = ConnectionMessage.createConnectionAccepted("Server", "Player1");

        assertEquals("Player1", message.getTargetPlayerId(), "대상 플레이어 ID가 올바르게 반환되어야 함");
    }

    @Test
    @DisplayName("대상 플레이어 존재 여부 테스트 - 존재함")
    void testHasTargetWithTarget() {
        ConnectionMessage message = ConnectionMessage.createConnectionAccepted("Server", "Player1");

        assertTrue(message.hasTarget(), "대상 플레이어가 존재하는 경우 true를 반환해야 함");
    }

    @Test
    @DisplayName("대상 플레이어 존재 여부 테스트 - 존재하지 않음")
    void testHasTargetWithoutTarget() {
        ConnectionMessage message = ConnectionMessage.createPlayerReady("Player1");

        assertFalse(message.hasTarget(), "대상 플레이어가 없는 경우 false를 반환해야 함");
    }

    @Test
    @DisplayName("대상 플레이어 존재 여부 테스트 - 빈 문자열")
    void testHasTargetWithEmptyString() {
        ConnectionMessage message =
                new ConnectionMessage(MessageType.CONNECTION_ACCEPTED, "Server", "Welcome", "   ");

        assertFalse(message.hasTarget(), "대상 플레이어 ID가 공백인 경우 false를 반환해야 함");
    }

    @Test
    @DisplayName("연결 요청 메시지 판별 테스트")
    void testIsConnectionRequest() {
        ConnectionMessage requestMsg =
                ConnectionMessage.createConnectionRequest("Player1", "PlayerName");
        ConnectionMessage otherMsg = ConnectionMessage.createPlayerReady("Player1");

        assertTrue(requestMsg.isConnectionRequest(), "CONNECTION_REQUEST 타입의 메시지는 true를 반환해야 함");
        assertFalse(otherMsg.isConnectionRequest(), "다른 타입의 메시지는 false를 반환해야 함");
    }

    @Test
    @DisplayName("연결 응답 메시지 판별 테스트")
    void testIsConnectionResponse() {
        ConnectionMessage acceptedMsg =
                ConnectionMessage.createConnectionAccepted("Server", "Player1");
        ConnectionMessage rejectedMsg =
                ConnectionMessage.createConnectionRejected("Server", "Server is full");
        ConnectionMessage disconnectMsg =
                new ConnectionMessage(MessageType.DISCONNECT, "Player1", "Bye");
        ConnectionMessage otherMsg = ConnectionMessage.createGameStart("Server");

        assertTrue(acceptedMsg.isConnectionResponse(), "CONNECTION_ACCEPTED는 true를 반환해야 함");
        assertTrue(rejectedMsg.isConnectionResponse(), "CONNECTION_REJECTED는 true를 반환해야 함");
        assertTrue(disconnectMsg.isConnectionResponse(), "DISCONNECT는 true를 반환해야 함");
        assertFalse(otherMsg.isConnectionResponse(), "다른 타입의 메시지는 false를 반환해야 함");
    }

    @Test
    @DisplayName("게임 제어 메시지 판별 테스트")
    void testIsGameControl() {
        ConnectionMessage gameStartMsg = ConnectionMessage.createGameStart("Server");
        ConnectionMessage pauseMsg = ConnectionMessage.createGamePause("Player1", "pause");
        ConnectionMessage resumeMsg = ConnectionMessage.createGameResume("Server");
        ConnectionMessage gameOverMsg = ConnectionMessage.createGameOver("Server", "Game ended");
        ConnectionMessage otherMsg = ConnectionMessage.createPlayerReady("Player1");

        assertTrue(gameStartMsg.isGameControl(), "GAME_START는 true를 반환해야 함");
        assertTrue(pauseMsg.isGameControl(), "PAUSE는 true를 반환해야 함");
        assertTrue(resumeMsg.isGameControl(), "RESUME은 true를 반환해야 함");
        assertTrue(gameOverMsg.isGameControl(), "GAME_OVER는 true를 반환해야 함");
        assertFalse(otherMsg.isGameControl(), "다른 타입의 메시지는 false를 반환해야 함");
    }

    @Test
    @DisplayName("플레이어 준비 해제 메시지 생성 테스트")
    void testCreatePlayerUnready() {
        ConnectionMessage message = ConnectionMessage.createPlayerUnready("Player1");

        assertNotNull(message, "메시지가 생성되어야 함");
        assertEquals(MessageType.PLAYER_UNREADY, message.getType(), "메시지 타입이 PLAYER_UNREADY여야 함");
        assertEquals("Player1", message.getSenderId(), "발신자 ID가 올바르게 설정되어야 함");
        assertTrue(message.getMessage().contains("Player1"), "메시지에 플레이어 ID가 포함되어야 함");
    }

    @Test
    @DisplayName("toString에 대상 플레이어 포함 테스트")
    void testToStringWithTarget() {
        ConnectionMessage message = ConnectionMessage.createConnectionAccepted("Server", "Player1");
        String toString = message.toString();

        assertTrue(toString.contains("Player1"), "toString에 대상 플레이어 ID가 포함되어야 함");
        assertTrue(toString.contains("target"), "toString에 'target' 필드가 표시되어야 함");
    }

    @Test
    @DisplayName("잘못된 메시지 타입으로 생성 시 예외 발생 테스트")
    void testInvalidMessageType() {
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    new ConnectionMessage(MessageType.BOARD_UPDATE, "Player1", "Invalid");
                },
                "잘못된 메시지 타입으로 생성 시 IllegalArgumentException이 발생해야 함");
    }

    @Test
    @DisplayName("null 메시지로 생성 테스트")
    void testNullMessage() {
        ConnectionMessage message = new ConnectionMessage(MessageType.GAME_START, "Server", null);

        assertNotNull(message.getMessage(), "null 메시지는 빈 문자열로 변환되어야 함");
        assertEquals("", message.getMessage(), "null 메시지는 빈 문자열이어야 함");
    }

    @Test
    @DisplayName("게임 종료 메시지 생성 테스트 - 사유 없음")
    void testCreateGameOverWithoutReason() {
        ConnectionMessage message = ConnectionMessage.createGameOver("Server", null);

        assertNotNull(message, "메시지가 생성되어야 함");
        assertEquals(MessageType.GAME_OVER, message.getType(), "메시지 타입이 GAME_OVER여야 함");
        assertEquals("Game ended", message.getMessage(), "기본 게임 종료 메시지가 설정되어야 함");
    }
}
