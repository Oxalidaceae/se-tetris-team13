package team13.tetris.network.protocol;

import java.io.Serializable;

// 네트워크를 통해 전송되는 모든 메시지의 기본 클래스(모든 네트워크 메시지는 이 클래스를 상속받아야 함)
public abstract class NetworkMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final MessageType type;
    private final String senderId;
    private final long timestamp; // 입력 이벤트가 발생한 시간 기록
    
    protected NetworkMessage(MessageType type, String senderId) {
        if (type == null) {
            throw new IllegalArgumentException("Message type cannot be null");
        }
        if (senderId == null || senderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Sender ID cannot be null or empty");
        }
        
        this.type = type;
        this.senderId = senderId;
        this.timestamp = System.currentTimeMillis();
    }
    
    public MessageType getType() {
        return type;
    }
    
    public String getSenderId() {
        return senderId;
    }

    public long getTimestamp() {
        return timestamp;
    }
    
    // 메시지가 생성된 후 경과된 시간을 밀리초 단위로 반환
    public long getElapsedTime() {
        return System.currentTimeMillis() - timestamp;
    }
    
    @Override
    public String toString() {
        return "NetworkMessage{" +
               "type=" + type +
               ", timestamp=" + timestamp +
               ", senderId='" + senderId + '\'' +
               ", elapsedTime=" + getElapsedTime() + "ms" +
               '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        NetworkMessage that = (NetworkMessage) obj;
        return timestamp == that.timestamp &&
               type == that.type &&
               senderId.equals(that.senderId);
    }
    
    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + senderId.hashCode();
        return result;
    }
}
