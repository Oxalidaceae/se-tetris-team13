package team13.tetris.network.protocol;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
        ConnectionMessage message = ConnectionMessage.createConnectionRequest("TestPlayer", "Test message");
        
        assertNotNull(message, "메시지가 생성되어야 함");
        assertEquals(MessageType.CONNECTION_REQUEST, message.getType(), "메시지 타입이 CONNECTION_REQUEST여야 함");
        assertEquals("TestPlayer", message.getSenderId(), "발신자 ID가 올바르게 설정되어야 함");
    }

    @Test
    @DisplayName("연결 승인 메시지 생성 테스트")
    void testCreateConnectionAccepted() {
        ConnectionMessage message = ConnectionMessage.createConnectionAccepted("TestPlayer", "Welcome");
        
        assertNotNull(message, "메시지가 생성되어야 함");
        assertEquals(MessageType.CONNECTION_ACCEPTED, message.getType(), "메시지 타입이 CONNECTION_ACCEPTED여야 함");
        assertEquals("TestPlayer", message.getSenderId(), "발신자 ID가 올바르게 설정되어야 함");
    }

    @Test
    @DisplayName("연결 거절 메시지 생성 테스트")
    void testCreateConnectionRejected() {
        String reason = "Server is full";
        ConnectionMessage message = ConnectionMessage.createConnectionRejected("TestPlayer", reason);
        
        assertNotNull(message, "메시지가 생성되어야 함");
        assertEquals(MessageType.CONNECTION_REJECTED, message.getType(), "메시지 타입이 CONNECTION_REJECTED여야 함");
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
        ConnectionMessage message = new ConnectionMessage(MessageType.DISCONNECT, "TestPlayer", "Test message");
        
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
}