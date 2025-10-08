package team13.tetris.model.game;

/**
 * Represents the current state of the Tetris game.
 */
public enum GameState {
    READY,      // Game is ready to start
    PLAYING,    // Game is currently being played
    PAUSED,     // Game is temporarily paused
    GAME_OVER   // Game has ended
}