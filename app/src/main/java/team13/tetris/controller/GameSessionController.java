package team13.tetris.controller;

import javafx.application.Platform;
import team13.tetris.model.data.ScoreBoard;
import team13.tetris.model.data.Settings;
import team13.tetris.model.game.Board;
import team13.tetris.model.game.CompositeGameStateListener;
import team13.tetris.model.game.GameEngine;
import team13.tetris.model.game.GameStateListener;
import team13.tetris.model.game.Tetromino;
import team13.tetris.view.SceneManager;
import team13.tetris.view.scene.GameScene;

/**
 * 게임 세션을 관리하며 Model(GameEngine)과 View(GameScene) 사이를 연결하는 컨트롤러입니다.
 */
public class GameSessionController implements GameController {
    private final Settings settings;
    private final ScoreBoard scoreBoard;
    private final SceneManager sceneManager;
    private final Board board;
    private final GameEngine engine;
    private final CompositeGameStateListener listeners = new CompositeGameStateListener();
    private final KeyInputHandler keyInputHandler;

    private GameStateListener fxBridge;
    private boolean running = false;

    public GameSessionController(Settings settings, ScoreBoard scoreBoard, SceneManager sceneManager) {
        this.settings = settings;
        this.scoreBoard = scoreBoard;
        this.sceneManager = sceneManager;
        this.board = new Board(10, 20);
        this.engine = new GameEngine(board, listeners);
        this.keyInputHandler = new KeyInputHandler(settings);
    }

    public Settings getSettings() {
        return settings;
    }

    public ScoreBoard getScoreBoard() {
        return scoreBoard;
    }

    public KeyInputHandler getKeyInputHandler() {
        return keyInputHandler;
    }

    public void bind(GameScene view) {
        detachCurrentView();
        this.fxBridge = new FxDispatchingListener(view);
        listeners.add(fxBridge);
        view.renderBoard(new int[board.getHeight()][board.getWidth()], null, 0, 0);
        view.showNextPiece(null);
        view.updateScore(0);
    }

    public void detachCurrentView() {
        if (fxBridge != null) {
            listeners.remove(fxBridge);
            fxBridge = null;
        }
    }

    @Override
    public void start() {
        running = true;
        engine.startNewGame();
    }

    @Override
    public void pause() {
        engine.stopAutoDrop();
    }

    @Override
    public void resume() {
        engine.startAutoDrop();
    }

    @Override
    public void moveLeft() {
        engine.moveLeft();
    }

    @Override
    public void moveRight() {
        engine.moveRight();
    }

    @Override
    public void softDrop() {
        engine.softDrop();
    }

    @Override
    public void hardDrop() {
        engine.hardDrop();
    }

    @Override
    public void rotateCW() {
        engine.rotateCW();
    }

    @Override
    public void stop() {
        running = false;
        engine.stopAutoDrop();
        detachCurrentView();
    }

    public boolean isRunning() {
        return running;
    }

    private class FxDispatchingListener implements GameStateListener {
        private final GameScene view;

        private FxDispatchingListener(GameScene view) {
            this.view = view;
        }

        @Override
        public void onBoardUpdated(Board board) {
            if (view == null) return;
            Platform.runLater(() -> view.renderBoard(board.snapshot(), engine.getCurrent(), engine.getPieceX(), engine.getPieceY()));
        }

        @Override
        public void onPieceSpawned(Tetromino tetromino, int px, int py) {
            if (view == null) return;
            Platform.runLater(() -> view.renderBoard(board.snapshot(), tetromino, px, py));
        }

        @Override
        public void onLinesCleared(int lines) {
            if (view == null) return;
            Platform.runLater(() -> view.handleLinesCleared(lines));
        }

        @Override
        public void onGameOver() {
            running = false;
            engine.stopAutoDrop();
            detachCurrentView();
            Platform.runLater(() -> sceneManager.showGameOver(engine.getScore()));
        }

        @Override
        public void onNextPiece(Tetromino next) {
            if (view == null) return;
            Platform.runLater(() -> view.showNextPiece(next));
        }

        @Override
        public void onScoreChanged(int score) {
            if (view == null) return;
            Platform.runLater(() -> view.updateScore(score));
        }
    }
}
