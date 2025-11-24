package team13.tetris.network.protocol;

// 채팅 메시지를 전송하는 클래스
public class ChatMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;

    private final String message;

    public ChatMessage(String senderId, String message) {
        super(MessageType.CHAT, senderId);

        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Chat message cannot be null or empty");
        }

        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ChatMessage{"
                + "senderId='"
                + getSenderId()
                + '\''
                + ", message='"
                + message
                + '\''
                + ", timestamp="
                + getTimestamp()
                + '}';
    }
}
