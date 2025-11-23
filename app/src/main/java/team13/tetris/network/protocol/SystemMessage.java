package team13.tetris.network.protocol;

// 시스템 메시지 (에러 등)
public class SystemMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;

    private final String message; // 메시지 내용
    private final SystemLevel level; // 메시지 레벨

    public enum SystemLevel {
        INFO, // 정보성 메시지
        WARNING, // 경고
        ERROR // 에러
    }

    // ERROR용 생성자 (메시지 포함)
    public SystemMessage(String senderId, String errorMessage, SystemLevel level) {
        super(MessageType.ERROR, senderId);

        if (errorMessage == null || errorMessage.trim().isEmpty()) {
            throw new IllegalArgumentException("Error message cannot be null or empty");
        }

        this.message = errorMessage;
        this.level = level != null ? level : SystemLevel.ERROR;
    }

    // ERROR 메시지 생성
    public static SystemMessage createError(String senderId, String errorMessage) {
        return new SystemMessage(senderId, errorMessage, SystemLevel.ERROR);
    }

    // WARNING 메시지 생성
    public static SystemMessage createWarning(String senderId, String warningMessage) {
        return new SystemMessage(senderId, warningMessage, SystemLevel.WARNING);
    }

    // INFO 메시지 생성
    public static SystemMessage createInfo(String senderId, String infoMessage) {
        return new SystemMessage(senderId, infoMessage, SystemLevel.INFO);
    }

    public String getMessage() {
        return message;
    }

    public SystemLevel getLevel() {
        return level;
    }

    public boolean isError() {
        return getType() == MessageType.ERROR;
    }

    @Override
    public String toString() {
        return "SystemMessage{"
                + "type="
                + getType()
                + ", sender='"
                + getSenderId()
                + '\''
                + ", level="
                + level
                + ", message='"
                + message
                + '\''
                + ", timestamp="
                + getTimestamp()
                + ", elapsedTime="
                + getElapsedTime()
                + "ms"
                + '}';
    }
}
