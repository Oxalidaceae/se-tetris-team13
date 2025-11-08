package team13.tetris.network.protocol;

// 연결 관련 메시지 (접속 요청, 승인, 거절, 게임 제어)
public class ConnectionMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private final String message;           // 메시지 내용
    private final String targetPlayerId;    // 대상 플레이어 ID (필요한 경우)
    
    public ConnectionMessage(MessageType type, String senderId, String message) {
        this(type, senderId, message, null);
    }
    
    public ConnectionMessage(MessageType type, String senderId, String message, String targetPlayerId) {
        super(type, senderId);
        
        // 연결 관련 타입만 허용
        validateConnectionType(type);
        
        this.message = message != null ? message : "";
        this.targetPlayerId = targetPlayerId;
    }
    
    private void validateConnectionType(MessageType type) {
        switch (type) {
            case CONNECTION_REQUEST:
            case CONNECTION_ACCEPTED:
            case CONNECTION_REJECTED:
            case DISCONNECT:
            case GAME_START:
            case PAUSE:
            case RESUME:
            case GAME_OVER:
                break;
            default:
                throw new IllegalArgumentException("Invalid connection message type: " + type);
        }
    }
    
    public static ConnectionMessage createConnectionRequest(String playerId, String playerName) {
        return new ConnectionMessage(MessageType.CONNECTION_REQUEST, playerId, "Player '" + playerName + "' requests to connect");
    }
    
    public static ConnectionMessage createConnectionAccepted(String serverId, String acceptedPlayerId) {
        return new ConnectionMessage(MessageType.CONNECTION_ACCEPTED, serverId, "Connection accepted", acceptedPlayerId);
    }
    
    public static ConnectionMessage createConnectionRejected(String serverId, String reason) {
        return new ConnectionMessage(MessageType.CONNECTION_REJECTED, serverId, reason);
    }
    
    public static ConnectionMessage createGameStart(String senderId) {
        return new ConnectionMessage(MessageType.GAME_START, senderId, "Game starting!");
    }
    
    public static ConnectionMessage createGamePause(String senderId, String reason) {
        return new ConnectionMessage(MessageType.PAUSE, senderId, reason != null ? reason : "Game paused");
    }
    
    public static ConnectionMessage createGameResume(String senderId) {
        return new ConnectionMessage(MessageType.RESUME, senderId, "Game resumed");
    }
    
    public static ConnectionMessage createGameOver(String senderId, String reason) {
        return new ConnectionMessage(MessageType.GAME_OVER, senderId, reason != null ? reason : "Game ended");
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getTargetPlayerId() {
        return targetPlayerId;
    }

    public boolean hasTarget() {
        return targetPlayerId != null && !targetPlayerId.trim().isEmpty();
    }
    
    public boolean isConnectionRequest() {
        return getType() == MessageType.CONNECTION_REQUEST;
    }
    
    public boolean isConnectionResponse() {
        MessageType type = getType();
        return type == MessageType.CONNECTION_ACCEPTED || 
               type == MessageType.CONNECTION_REJECTED ||
               type == MessageType.DISCONNECT;
    }

    public boolean isGameControl() {
        MessageType type = getType();
        return type == MessageType.GAME_START || 
               type == MessageType.PAUSE || 
               type == MessageType.RESUME || 
               type == MessageType.GAME_OVER;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ConnectionMessage{");
        sb.append("type=").append(getType());
        sb.append(", sender='").append(getSenderId()).append('\'');
        if (hasTarget()) {
            sb.append(", target='").append(targetPlayerId).append('\'');
        }
        sb.append(", message='").append(message).append('\'');
        sb.append(", timestamp=").append(getTimestamp());
        sb.append(", elapsedTime=").append(getElapsedTime()).append("ms");
        sb.append('}');
        return sb.toString();
    }
}
