package team13.tetris.network.protocol;


// 게임 보드 상태를 전송하는 메시지
public class BoardUpdateMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private final int[][] boardState;           // 게임 보드 상태 (2D 배열)
    private final int currentPieceX;            // 현재 블록 X 좌표
    private final int currentPieceY;            // 현재 블록 Y 좌표
    private final int currentPieceType;         // 현재 블록 타입 (T, I, O, S, Z, J, L)
    private final int currentPieceRotation;     // 현재 블록 회전 상태 (0, 1, 2, 3)
    private final int score;                    // 현재 점수
    private final int linesCleared;             // 삭제한 줄 수
    private final int level;                    // 현재 레벨
    
    /**
     * 보드 상태 업데이트 메시지를 생성합니다.
     * 
     * @param playerId 보드 상태를 보내는 플레이어 ID
     * @param boardState 게임 보드 상태 (10x20 배열)
     * @param currentPieceX 현재 블록 X 좌표
     * @param currentPieceY 현재 블록 Y 좌표
     * @param currentPieceType 현재 블록 타입
     * @param currentPieceRotation 현재 블록 회전 상태
     * @param score 현재 점수
     * @param linesCleared 삭제한 줄 수
     * @param level 현재 레벨
     */
    public BoardUpdateMessage(String playerId, int[][] boardState, 
                            int currentPieceX, int currentPieceY, 
                            int currentPieceType, int currentPieceRotation,
                            int score, int linesCleared, int level) {
        super(MessageType.BOARD_UPDATE, playerId);
        
        this.boardState = deepCopyBoard(boardState);
        this.currentPieceX = currentPieceX;
        this.currentPieceY = currentPieceY;
        this.currentPieceType = currentPieceType;
        this.currentPieceRotation = currentPieceRotation;
        this.score = score;
        this.linesCleared = linesCleared;
        this.level = level;
    }
    
    
    // 2차원 배열을 깊은 복사합니다.
    private int[][] deepCopyBoard(int[][] original) {
        if (original == null) {
            return null;
        }
        
        int[][] copy = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            if (original[i] != null) {
                copy[i] = original[i].clone();
            }
        }
        return copy;
    }
    
    public int[][] getBoardState() {
        return deepCopyBoard(boardState); // 외부에서 수정하지 못하도록 복사본 반환
    }
    
    public int getCurrentPieceX() {
        return currentPieceX;
    }
    
    public int getCurrentPieceY() {
        return currentPieceY;
    }
    
    public int getCurrentPieceType() {
        return currentPieceType;
    }
    
    public int getCurrentPieceRotation() {
        return currentPieceRotation;
    }
    
    public int getScore() {
        return score;
    }
    
    public int getLinesCleared() {
        return linesCleared;
    }
    
    public int getLevel() {
        return level;
    }
    
    public String getPlayerId() {
        return getSenderId();
    }
    
    @Override
    public String toString() {
        return "BoardUpdateMessage{" +
               "playerId='" + getSenderId() + '\'' +
               ", currentPiece=(" + currentPieceX + "," + currentPieceY + ")" +
               ", pieceType=" + currentPieceType +
               ", rotation=" + currentPieceRotation +
               ", score=" + score +
               ", linesCleared=" + linesCleared +
               ", level=" + level +
               ", timestamp=" + getTimestamp() +
               ", elapsedTime=" + getElapsedTime() + "ms" +
               '}';
    }
}
