package team13.tetris.network.protocol;

// 여러 줄을 동시에 삭제했을 때 상대방에게 공격 라인 전송
public class AttackMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private final int attackLines;              // 공격할 줄 수
    private final int sourceLines;              // 원인이 된 삭제 줄 수 
    private final String targetPlayerId;        // 공격 대상 플레이어 ID
    
    public AttackMessage(String attackerPlayerId, String targetPlayerId, 
                        int sourceLines, int attackLines) {
        super(MessageType.ATTACK_SENT, attackerPlayerId);
        
        if (targetPlayerId == null || targetPlayerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Target player ID cannot be null or empty");
        }
        if (sourceLines < 1 || sourceLines > 10) {
            throw new IllegalArgumentException("Source lines must be between 1 and 10");
        }
        if (attackLines < 0) {
            throw new IllegalArgumentException("Attack lines cannot be negative");
        }
        if (attackLines > 10) {
            throw new IllegalArgumentException("Attack lines cannot exceed 10");
        }
        
        this.targetPlayerId = targetPlayerId;
        this.sourceLines = sourceLines;
        this.attackLines = attackLines;
    }
    
    public static AttackMessage createStandardAttack(String attackerPlayerId, String targetPlayerId, int clearedLines) {
        int attackLines = switch (clearedLines) {
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
        
        return new AttackMessage(attackerPlayerId, targetPlayerId, clearedLines, attackLines);
    }
    
    public int getAttackLines() {
        return attackLines;
    }
    
    public int getSourceLines() {
        return sourceLines;
    }
    
    public String getTargetPlayerId() {
        return targetPlayerId;
    }
    
    public String getAttackerPlayerId() {
        return getSenderId();
    }
    
    public boolean hasAttack() {
        return attackLines > 0;
    }
    
    @Override
    public String toString() {
        return "AttackMessage{" +
               "attacker='" + getSenderId() + '\'' +
               ", target='" + targetPlayerId + '\'' +
               ", sourceLines=" + sourceLines +
               ", attackLines=" + attackLines +
               ", timestamp=" + getTimestamp() +
               ", elapsedTime=" + getElapsedTime() + "ms" +
               '}';
    }
}
