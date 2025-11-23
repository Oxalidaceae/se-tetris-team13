package team13.tetris.game.controller;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import team13.tetris.game.model.Board;
import team13.tetris.game.model.Tetromino;

// 엔진 이벤트를 여러 리스너로 전달하기 위한 간단한 합성(Composite) 리스너입니다.
public class CompositeGameStateListener implements GameStateListener {
    private final List<GameStateListener> delegates = new CopyOnWriteArrayList<>();

    public void add(GameStateListener l) {
        if (l == null) return;
        delegates.add(l);
    }

    public void remove(GameStateListener l) {
        delegates.remove(l);
    }

    @Override
    public void onBoardUpdated(Board board) {
        for (GameStateListener l : delegates) {
            try {
                l.onBoardUpdated(board);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    @Override
    public void onPieceSpawned(Tetromino tetromino, int px, int py) {
        for (GameStateListener l : delegates) {
            try {
                l.onPieceSpawned(tetromino, px, py);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    @Override
    public void onLinesCleared(int lines) {
        for (GameStateListener l : delegates) {
            try {
                l.onLinesCleared(lines);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    @Override
    public void onGameOver() {
        for (GameStateListener l : delegates) {
            try {
                l.onGameOver();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    @Override
    public void onNextPiece(Tetromino next) {
        for (GameStateListener l : delegates) {
            try {
                l.onNextPiece(next);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    @Override
    public void onScoreChanged(int score) {
        for (GameStateListener l : delegates) {
            try {
                l.onScoreChanged(score);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
