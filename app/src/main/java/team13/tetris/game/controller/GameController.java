package team13.tetris.game.controller;

// UI가 호출할 수 있는 고수준 제어(시작, 일시정지, 이동 등)를 노출하는 인터페이스입니다.
public interface GameController {
    void start();
    void pause();
    void resume();
    void moveLeft();
    void moveRight();
    void softDrop();
    void hardDrop();
    void rotateCW();
}
