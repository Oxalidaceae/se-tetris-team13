package team13.tetris.network.protocol;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GameModeMessageTest {

    @Test
    @DisplayName("NORMAL 게임 모드 메시지 생성 테스트")
    void testCreateNormalGameMode() {
        GameModeMessage message = new GameModeMessage("Host", GameModeMessage.GameMode.NORMAL);

        assertNotNull(message, "메시지가 생성되어야 함");
        assertEquals(
                MessageType.GAME_MODE_SELECTED,
                message.getType(),
                "메시지 타입이 GAME_MODE_SELECTED여야 함");
        assertEquals("Host", message.getSenderId(), "발신자 ID가 올바르게 설정되어야 함");
        assertEquals(GameModeMessage.GameMode.NORMAL, message.getGameMode(), "게임 모드가 NORMAL이어야 함");
    }

    @Test
    @DisplayName("ITEM 게임 모드 메시지 생성 테스트")
    void testCreateItemGameMode() {
        GameModeMessage message = new GameModeMessage("Host", GameModeMessage.GameMode.ITEM);

        assertNotNull(message, "메시지가 생성되어야 함");
        assertEquals(
                MessageType.GAME_MODE_SELECTED,
                message.getType(),
                "메시지 타입이 GAME_MODE_SELECTED여야 함");
        assertEquals("Host", message.getSenderId(), "발신자 ID가 올바르게 설정되어야 함");
        assertEquals(GameModeMessage.GameMode.ITEM, message.getGameMode(), "게임 모드가 ITEM이어야 함");
    }

    @Test
    @DisplayName("GameMode enum 값 테스트")
    void testGameModeEnum() {
        assertEquals(3, GameModeMessage.GameMode.values().length, "GameMode enum은 3개의 값을 가져야 함");
        assertNotNull(GameModeMessage.GameMode.NORMAL, "NORMAL 모드가 존재해야 함");
        assertNotNull(GameModeMessage.GameMode.ITEM, "ITEM 모드가 존재해야 함");
        assertNotNull(GameModeMessage.GameMode.TIMER, "TIMER 모드가 존재해야 함");
    }

    @Test
    @DisplayName("toString 메서드 테스트")
    void testToString() {
        GameModeMessage message = new GameModeMessage("Host", GameModeMessage.GameMode.NORMAL);
        String toString = message.toString();

        assertNotNull(toString, "toString 결과가 null이 아니어야 함");
        assertTrue(toString.contains("NORMAL"), "toString에 게임 모드가 포함되어야 함");
        assertTrue(toString.contains("Host"), "toString에 발신자 ID가 포함되어야 함");
    }

    @Test
    @DisplayName("isNormalMode 메서드 테스트")
    void testIsNormalMode() {
        GameModeMessage normalMsg = new GameModeMessage("Host", GameModeMessage.GameMode.NORMAL);
        GameModeMessage itemMsg = new GameModeMessage("Host", GameModeMessage.GameMode.ITEM);

        assertTrue(normalMsg.isNormalMode(), "NORMAL 모드에서 isNormalMode()는 true를 반환해야 함");
        assertFalse(itemMsg.isNormalMode(), "다른 모드에서 isNormalMode()는 false를 반환해야 함");
    }

    @Test
    @DisplayName("isItemMode 메서드 테스트")
    void testIsItemMode() {
        GameModeMessage itemMsg = new GameModeMessage("Host", GameModeMessage.GameMode.ITEM);
        GameModeMessage normalMsg = new GameModeMessage("Host", GameModeMessage.GameMode.NORMAL);

        assertTrue(itemMsg.isItemMode(), "ITEM 모드에서 isItemMode()는 true를 반환해야 함");
        assertFalse(normalMsg.isItemMode(), "다른 모드에서 isItemMode()는 false를 반환해야 함");
    }

    @Test
    @DisplayName("isTimerMode 메서드 테스트")
    void testIsTimerMode() {
        GameModeMessage timerMsg = new GameModeMessage("Host", GameModeMessage.GameMode.TIMER);
        GameModeMessage normalMsg = new GameModeMessage("Host", GameModeMessage.GameMode.NORMAL);

        assertTrue(timerMsg.isTimerMode(), "TIMER 모드에서 isTimerMode()는 true를 반환해야 함");
        assertFalse(normalMsg.isTimerMode(), "다른 모드에서 isTimerMode()는 false를 반환해야 함");
    }

    @Test
    @DisplayName("TIMER 게임 모드 메시지 생성 테스트")
    void testCreateTimerGameMode() {
        GameModeMessage message = new GameModeMessage("Host", GameModeMessage.GameMode.TIMER);

        assertNotNull(message, "메시지가 생성되어야 함");
        assertEquals(
                MessageType.GAME_MODE_SELECTED,
                message.getType(),
                "메시지 타입이 GAME_MODE_SELECTED여야 함");
        assertEquals("Host", message.getSenderId(), "발신자 ID가 올바르게 설정되어야 함");
        assertEquals(GameModeMessage.GameMode.TIMER, message.getGameMode(), "게임 모드가 TIMER이어야 함");
    }

    @Test
    @DisplayName("메시지 타입 검증 테스트")
    void testMessageType() {
        GameModeMessage message = new GameModeMessage("Server", GameModeMessage.GameMode.NORMAL);

        assertEquals(
                MessageType.GAME_MODE_SELECTED,
                message.getType(),
                "메시지 타입이 GAME_MODE_SELECTED여야 함");
    }

    @Test
    @DisplayName("타임스탬프 존재 테스트")
    void testTimestampExists() {
        GameModeMessage message = new GameModeMessage("Host", GameModeMessage.GameMode.ITEM);

        assertTrue(message.getTimestamp() > 0, "타임스탬프가 설정되어야 함");
    }
}
