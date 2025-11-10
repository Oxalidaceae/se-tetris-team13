package team13.tetris.network.protocol;

// 줄 삭제 정보를 전송하는 메시지 (공격 계산용)
public class LinesClearedMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private final int linesCleared;         // 삭제한 줄 수
    private final boolean willAttack;       // 공격 여부 (2줄 이상이면 true)
    
    public LinesClearedMessage(String playerId, int linesCleared) {
        super(MessageType.LINES_CLEARED, playerId);
        
        if (linesCleared < 1 || linesCleared > 10) {
            throw new IllegalArgumentException("Lines cleared must be between 1 and 10");
        }
        
        this.linesCleared = linesCleared;
        this.willAttack = linesCleared >= 2;  // 2줄 이상이면 공격
    }
    
    public int getLinesCleared() {
        return linesCleared;
    }
    
    public boolean willAttack() {
        return willAttack;
    }
    
    public int getAttackLines() {
        if (!willAttack) {
            return 0;
        }
        
        // 공격 라인 계산 로직 (AttackMessage와 동일)
        return switch (linesCleared) {
            case 1 -> 0;
            case 2 -> 2;
            case 3 -> 3;
            case 4 -> 4;
            case 5 -> 5;
            case 6 -> 6;
            case 7 -> 7;
            case 8 -> 8;
            case 9 -> 9;
            case 10 -> 10;
            default -> 0;
        };
    }
    
    @Override
    public String toString() {
        return "LinesClearedMessage{" +
               "playerId='" + getSenderId() + '\'' +
               ", linesCleared=" + linesCleared +
               ", willAttack=" + willAttack +
               ", attackLines=" + getAttackLines() +
               ", timestamp=" + getTimestamp() +
               ", elapsedTime=" + getElapsedTime() + "ms" +
               '}';
    }
}
