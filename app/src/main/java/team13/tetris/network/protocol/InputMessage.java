package team13.tetris.network.protocol;

// 플레이어 입력 이벤트를 전송하는 메시지
public class InputMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    public InputMessage(MessageType inputType, String playerId) {
        super(inputType, playerId);
        
        if (!isInputType(inputType)) {
            throw new IllegalArgumentException("Invalid input type: " + inputType);
        }
    }
    
    private boolean isInputType(MessageType type) {
        return type == MessageType.MOVE_LEFT || 
               type == MessageType.MOVE_RIGHT || 
               type == MessageType.ROTATE || 
               type == MessageType.SOFT_DROP || 
               type == MessageType.HARD_DROP;
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
