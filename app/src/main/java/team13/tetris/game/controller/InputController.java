package team13.tetris.game.controller;

/**
 * 최소한의 입력 컨트롤러 스텁입니다. 엔진 루프를 구현하는 동료가 GameEngine에 연결해야 합니다.
 * 책임 분리를 위해 간단히 위임만 수행합니다.
 */
public class InputController implements GameController {
    private final GameController delegate;

    public InputController(GameController delegate) {
        this.delegate = delegate;
    }

    @Override
    public void start() { delegate.start(); }

    @Override
    public void pause() { delegate.pause(); }

    @Override
    public void resume() { delegate.resume(); }

    @Override
    public void moveLeft() { delegate.moveLeft(); }

    @Override
    public void moveRight() { delegate.moveRight(); }

    @Override
    public void softDrop() { delegate.softDrop(); }

    @Override
    public void hardDrop() { delegate.hardDrop(); }

    @Override
    public void rotateCW() { delegate.rotateCW(); }
    
}
