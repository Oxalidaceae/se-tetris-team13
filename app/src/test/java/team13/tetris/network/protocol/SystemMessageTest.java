package team13.tetris.network.protocol;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SystemMessageTest {

    @Test
    @DisplayName("ERROR 메시지 생성 테스트")
    void testCreateError() {
        SystemMessage message = SystemMessage.createError("Server", "Connection failed");

        assertNotNull(message, "메시지가 생성되어야 함");
        assertEquals(MessageType.ERROR, message.getType(), "메시지 타입이 ERROR여야 함");
        assertEquals("Server", message.getSenderId(), "발신자 ID가 올바르게 설정되어야 함");
        assertEquals("Connection failed", message.getMessage(), "메시지 내용이 올바르게 설정되어야 함");
        assertEquals(SystemMessage.SystemLevel.ERROR, message.getLevel(), "레벨이 ERROR여야 함");
        assertTrue(message.isError(), "isError()가 true를 반환해야 함");
    }

    @Test
    @DisplayName("WARNING 메시지 생성 테스트")
    void testCreateWarning() {
        SystemMessage message = SystemMessage.createWarning("Server", "High latency detected");

        assertNotNull(message, "메시지가 생성되어야 함");
        assertEquals(MessageType.ERROR, message.getType(), "메시지 타입이 ERROR여야 함");
        assertEquals("Server", message.getSenderId(), "발신자 ID가 올바르게 설정되어야 함");
        assertEquals("High latency detected", message.getMessage(), "메시지 내용이 올바르게 설정되어야 함");
        assertEquals(SystemMessage.SystemLevel.WARNING, message.getLevel(), "레벨이 WARNING이어야 함");
        assertTrue(message.isError(), "isError()가 true를 반환해야 함");
    }

    @Test
    @DisplayName("INFO 메시지 생성 테스트")
    void testCreateInfo() {
        SystemMessage message = SystemMessage.createInfo("Server", "Game started successfully");

        assertNotNull(message, "메시지가 생성되어야 함");
        assertEquals(MessageType.ERROR, message.getType(), "메시지 타입이 ERROR여야 함");
        assertEquals("Server", message.getSenderId(), "발신자 ID가 올바르게 설정되어야 함");
        assertEquals("Game started successfully", message.getMessage(), "메시지 내용이 올바르게 설정되어야 함");
        assertEquals(SystemMessage.SystemLevel.INFO, message.getLevel(), "레벨이 INFO여야 함");
        assertTrue(message.isError(), "isError()가 true를 반환해야 함");
    }

    @Test
    @DisplayName("생성자로 직접 메시지 생성 테스트")
    void testConstructor() {
        SystemMessage message =
                new SystemMessage("Client", "Test message", SystemMessage.SystemLevel.WARNING);

        assertNotNull(message, "메시지가 생성되어야 함");
        assertEquals("Client", message.getSenderId(), "발신자 ID가 올바르게 설정되어야 함");
        assertEquals("Test message", message.getMessage(), "메시지 내용이 올바르게 설정되어야 함");
        assertEquals(SystemMessage.SystemLevel.WARNING, message.getLevel(), "레벨이 WARNING이어야 함");
    }

    @Test
    @DisplayName("null 메시지로 생성 시 예외 발생")
    void testNullMessage() {
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    new SystemMessage("Server", null, SystemMessage.SystemLevel.ERROR);
                },
                "메시지가 null일 때 예외가 발생해야 함");
    }

    @Test
    @DisplayName("빈 메시지로 생성 시 예외 발생")
    void testEmptyMessage() {
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    new SystemMessage("Server", "", SystemMessage.SystemLevel.ERROR);
                },
                "메시지가 빈 문자열일 때 예외가 발생해야 함");

        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    new SystemMessage("Server", "   ", SystemMessage.SystemLevel.ERROR);
                },
                "메시지가 공백만 있을 때 예외가 발생해야 함");
    }

    @Test
    @DisplayName("null 레벨로 생성 시 기본값 ERROR 사용")
    void testNullLevel() {
        SystemMessage message = new SystemMessage("Server", "Test message", null);

        assertNotNull(message, "메시지가 생성되어야 함");
        assertEquals(
                SystemMessage.SystemLevel.ERROR,
                message.getLevel(),
                "null 레벨일 때 기본값 ERROR를 사용해야 함");
    }

    @Test
    @DisplayName("toString 메서드 테스트")
    void testToString() {
        SystemMessage message = SystemMessage.createError("Server", "Connection timeout");
        String toString = message.toString();

        assertNotNull(toString, "toString 결과가 null이 아니어야 함");
        assertTrue(toString.contains("Server"), "toString에 발신자 ID가 포함되어야 함");
        assertTrue(toString.contains("Connection timeout"), "toString에 메시지 내용이 포함되어야 함");
        assertTrue(toString.contains("ERROR"), "toString에 레벨이 포함되어야 함");
        assertTrue(toString.contains("SystemMessage"), "toString에 클래스 이름이 포함되어야 함");
    }

    @Test
    @DisplayName("다양한 메시지 내용 테스트")
    void testVariousMessages() {
        // 짧은 메시지
        SystemMessage short1 = SystemMessage.createInfo("S", "OK");
        assertEquals("OK", short1.getMessage());

        // 긴 메시지
        String longMessage =
                "This is a very long error message that contains a lot of detailed information about what went wrong in the system.";
        SystemMessage long1 = SystemMessage.createError("Server", longMessage);
        assertEquals(longMessage, long1.getMessage());

        // 특수 문자 포함 메시지
        SystemMessage special =
                SystemMessage.createWarning("Server", "Error at line 42: NullPointerException");
        assertEquals("Error at line 42: NullPointerException", special.getMessage());

        // 유니코드 메시지
        SystemMessage unicode = SystemMessage.createInfo("서버", "연결이 성공했습니다 🎮");
        assertEquals("연결이 성공했습니다 🎮", unicode.getMessage());
    }

    @Test
    @DisplayName("각 레벨별 메시지 생성 확인")
    void testAllLevels() {
        SystemMessage error = new SystemMessage("S", "Error", SystemMessage.SystemLevel.ERROR);
        assertEquals(SystemMessage.SystemLevel.ERROR, error.getLevel());

        SystemMessage warning =
                new SystemMessage("S", "Warning", SystemMessage.SystemLevel.WARNING);
        assertEquals(SystemMessage.SystemLevel.WARNING, warning.getLevel());

        SystemMessage info = new SystemMessage("S", "Info", SystemMessage.SystemLevel.INFO);
        assertEquals(SystemMessage.SystemLevel.INFO, info.getLevel());
    }

    @Test
    @DisplayName("타임스탬프 테스트")
    void testTimestamp() throws InterruptedException {
        SystemMessage message1 = SystemMessage.createError("Server", "First error");
        Thread.sleep(10);
        SystemMessage message2 = SystemMessage.createError("Server", "Second error");

        assertTrue(
                message2.getTimestamp() > message1.getTimestamp(), "두 번째 메시지의 타임스탬프가 첫 번째보다 커야 함");
    }

    @Test
    @DisplayName("isError 메서드 테스트")
    void testIsError() {
        SystemMessage error = SystemMessage.createError("Server", "Error");
        SystemMessage warning = SystemMessage.createWarning("Server", "Warning");
        SystemMessage info = SystemMessage.createInfo("Server", "Info");

        assertTrue(error.isError(), "ERROR 메시지는 isError()가 true여야 함");
        assertTrue(warning.isError(), "WARNING 메시지도 MessageType.ERROR를 사용하므로 isError()가 true여야 함");
        assertTrue(info.isError(), "INFO 메시지도 MessageType.ERROR를 사용하므로 isError()가 true여야 함");
    }

    @Test
    @DisplayName("getElapsedTime 메서드 테스트")
    void testGetElapsedTime() throws InterruptedException {
        SystemMessage message = SystemMessage.createError("Server", "Test error");

        long elapsed1 = message.getElapsedTime();
        assertTrue(elapsed1 >= 0, "경과 시간은 0 이상이어야 함");

        Thread.sleep(10);

        long elapsed2 = message.getElapsedTime();
        assertTrue(elapsed2 > elapsed1, "시간이 지나면 경과 시간이 증가해야 함");
    }
}
