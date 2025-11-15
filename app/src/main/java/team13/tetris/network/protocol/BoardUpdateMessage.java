package team13.tetris.network.protocol;

import java.util.Queue;
import java.util.LinkedList;

// 게임 보드 상태를 전송하는 메시지 (다음 블록, incoming blocks 포함)
public class BoardUpdateMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private final int[][] boardState;           // 게임 보드 상태 (2D 배열)
    private final int currentPieceX;            // 현재 블록 X 좌표
    private final int currentPieceY;            // 현재 블록 Y 좌표
    private final int currentPieceType;         // 현재 블록 타입 (T, I, O, S, Z, J, L)
    private final int currentPieceRotation;     // 현재 블록 회전 상태 (0, 1, 2, 3)
    private final int nextPieceType;            // 다음 블록 타입
    private final Queue<int[][]> incomingBlocks; // 공격받을 블록 미리보기
    private final int score;                    // 현재 점수
    private final int linesCleared;             // 삭제한 줄 수
    private final int level;                    // 현재 레벨
    
    // 전체 정보를 포함하는 생성자
    public BoardUpdateMessage(String playerId, int[][] board, int pieceX, int pieceY, 
                            int pieceType, int pieceRotation, int nextPieceType,
                            Queue<int[][]> incomingBlocks, int score, int lines, int level) {
        super(MessageType.BOARD_UPDATE, playerId);
        
        this.boardState = deepCopyBoard(board);
        this.currentPieceX = pieceX;
        this.currentPieceY = pieceY;
        this.currentPieceType = pieceType;
        this.currentPieceRotation = pieceRotation;
        this.nextPieceType = nextPieceType;
        this.incomingBlocks = incomingBlocks != null ? new LinkedList<>(incomingBlocks) : new LinkedList<>();
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
    
    public int getNextPieceType() {
        return nextPieceType;
    }
    
    public Queue<int[][]> getIncomingBlocks() {
        return new LinkedList<>(incomingBlocks);
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
               ", nextPieceType=" + nextPieceType +
               ", incomingBlocksCount=" + incomingBlocks.size() +
               ", score=" + score +
               ", linesCleared=" + linesCleared +
               ", level=" + level +
               ", timestamp=" + getTimestamp() +
               ", elapsedTime=" + getElapsedTime() + "ms" +
               '}';
    }
}
