package team13.tetris.model.game;

import team13.tetris.model.game.Board;
import team13.tetris.model.game.Tetromino;

/**
 * 엔진이 UI/씬에게 상태 변경을 알리기 위해 호출하는 리스너 인터페이스입니다.
 */
public interface GameStateListener {
    void onBoardUpdated(Board board);
    void onPieceSpawned(Tetromino tetromino, int px, int py);
    void onLinesCleared(int lines);
    void onGameOver();
    void onNextPiece(Tetromino next);
    void onScoreChanged(int score);
}