package team13.tetris;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import team13.tetris.game.controller.GameStateListener;
import team13.tetris.game.logic.GameEngine;
import team13.tetris.game.model.Board;

public class GameEngineTest {
    @Test
    public void spawnAndSoftDrop() {
        Board b = new Board(10, 20);
        // 테스트에서는 콘솔 리스너 대신 간단한 빈 리스너를 사용합니다.
        GameStateListener l = new GameStateListener() {
            @Override public void onBoardUpdated(team13.tetris.game.model.Board board) {}
            @Override public void onPieceSpawned(team13.tetris.game.model.Tetromino tetromino, int px, int py) {}
            @Override public void onLinesCleared(int lines) {}
            @Override public void onGameOver() {}
            @Override public void onNextPiece(team13.tetris.game.model.Tetromino next) {}
            @Override public void onScoreChanged(int score) {}
        };
        GameEngine e = new GameEngine(b, l);
        e.startNewGame();
        assertNotNull(e.getCurrent());
        e.softDrop();
        int[][] snap = b.snapshot();
        assertEquals(20, snap.length);
        assertEquals(10, snap[0].length);
    }
}
