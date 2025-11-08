package team13.tetris.network.protocol;

// 플레이어 입력 이벤트를 전송하는 메시지
public class InputMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    public InputMessage(MessageType inputType, String playerId) {
        super(inputType, playerId);
        
        // 입력 관련 타입만 허용
        validateInputType(inputType);
    }
    
    private void validateInputType(MessageType type) {
        switch (type) {
            case MOVE_LEFT:
            case MOVE_RIGHT:
            case ROTATE:
            case SOFT_DROP:
            case HARD_DROP:
                break;
            default:
                throw new IllegalArgumentException("Invalid input message type: " + type);
        }
    }
    
    public MessageType getInputType() {
        return getType();
    }

    public String getPlayerId() {
        return getSenderId();
    }
    
    @Override
    public String toString() {
        return "InputMessage{" +
               "inputType=" + getType() +
               ", playerId='" + getSenderId() + '\'' +
               ", timestamp=" + getTimestamp() +
               ", elapsedTime=" + getElapsedTime() + "ms" +
               '}';
    }
}
