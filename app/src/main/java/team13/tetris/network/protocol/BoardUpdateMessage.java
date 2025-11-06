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
    
    // 간단한 게임 상태만 포함하는 생성자 (게임 상태 동기화용)
    public BoardUpdateMessage(String playerId, int[][] board, int score, int lines, int level) {
        super(MessageType.BOARD_UPDATE, playerId);
        
        this.boardState = deepCopyBoard(board);
        // 블록 정보는 기본값으로 설정 (상태 동기화에서는 필요 없음)
        this.currentPieceX = -1;
        this.currentPieceY = -1;
        this.currentPieceType = -1;
        this.currentPieceRotation = 0;
        this.score = score;
        this.linesCleared = lines;
        this.level = level;
    }

    // 상세한 블록 정보를 포함하는 생성자 (실시간 블록 움직임용)
    public BoardUpdateMessage(String playerId, int[][] board, int pieceX, int pieceY, 
                            int pieceType, int pieceRotation, int score, int lines, int level) {
        super(MessageType.BOARD_UPDATE, playerId);
        
        this.boardState = deepCopyBoard(board);
        this.currentPieceX = pieceX;
        this.currentPieceY = pieceY;
        this.currentPieceType = pieceType;
        this.currentPieceRotation = pieceRotation;
        this.score = score;
        this.linesCleared = lines;
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
