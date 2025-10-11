package team13.tetris;

import team13.tetris.config.Settings;
import team13.tetris.config.SettingsRepository;
import team13.tetris.scenes.GameScene;
import team13.tetris.scenes.KeySettingsScene;
import team13.tetris.scenes.MainMenuScene;
import team13.tetris.scenes.ScoreboardScene;
import team13.tetris.scenes.SettingsScene;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class SceneManager {
    private final Stage stage;
    private boolean colorBlindMode = false; // 색맹 모드 상태 변수

    public SceneManager(Stage stage) {
        this.stage = stage;
    }

    // 메인 메뉴 씬으로 전환
    public void showMainMenu(Settings settings) {
        changeScene(new MainMenuScene(this, settings).getScene());
    }

    // 옵션(설정) 씬으로 전환
    public void showSettings(Settings settings) {
        changeScene(new SettingsScene(this, settings).getScene());
    }

    // 스코어보드 씬으로 전환
    public void showScoreboard(Settings settings) {
        changeScene(new ScoreboardScene(this, settings).getScene());
    }

    // 게임 씬으로 전환
    public void showGame(Settings settings) {
        // Board와 KeyInputHandler 생성
        team13.tetris.game.model.Board board = new team13.tetris.game.model.Board(10, 20);
        team13.tetris.input.KeyInputHandler keyInputHandler = new team13.tetris.input.KeyInputHandler(settings);

        // GameScene을 먼저 생성 (GameEngine은 null로 시작)
        GameScene gameScene = new GameScene(null, keyInputHandler);

        // GameEngine을 생성하고 GameScene을 listener로 등록
        team13.tetris.game.logic.GameEngine gameEngine = new team13.tetris.game.logic.GameEngine(board, gameScene);

        // GameScene에 GameEngine 설정
        gameScene.setEngine(gameEngine);

        // Scene 생성 및 전환
        changeScene(gameScene.createScene());
        gameScene.requestFocus();

        // 게임 시작
        gameEngine.startNewGame();
    }

    // 키 설정 씬으로 전환
    public void showKeySettings(Settings settings) {
        changeScene(new KeySettingsScene(this, settings).getScene());
    }

    // 씬 전환 메서드
    public void changeScene(Scene scene) {
        applyStylesheet(scene);
        stage.setScene(scene);
    }

    public void exitWithSave(Settings settings) {
        settings.setColorBlindMode(isColorBlindMode());
        SettingsRepository.save(settings);
        stage.close();
    }

    // 색맹 모드 상태 확인 메서드
    public boolean isColorBlindMode() {
        return colorBlindMode;
    }

    // 색맹 모드 설정 메서드
    public void setColorBlindMode(boolean enabled) {
        this.colorBlindMode = enabled;
        applyStylesheet(stage.getScene()); // 현재 씬에 스타일시트 적용
    }

    // 창 크기 설정 메서드
    public void setWindowSize(int width, int height) {
        stage.setWidth(width);
        stage.setHeight(height);
    }

    // 씬에 맞는 스타일시트 적용 메서드
    private void applyStylesheet(Scene scene) {
        scene.getStylesheets().clear();

        String cssPath = colorBlindMode
                ? "/colorblind.css"
                : "/application.css";

        scene.getStylesheets().add(
                getClass().getResource(cssPath).toExternalForm());
    }

    // 키보드로 버튼들 간 이동 및 선택 기능 활성화 메서드
    public void enableArrowAsTab(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, new javafx.event.EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                Node focusedNode = scene.getFocusOwner();
                if (focusedNode == null)
                    return;

                // 아래/오른쪽 방향키 → Tab 이동
                if (e.getCode() == KeyCode.DOWN || e.getCode() == KeyCode.RIGHT) {
                    e.consume();
                    focusedNode.fireEvent(new KeyEvent(
                            KeyEvent.KEY_PRESSED,
                            "",
                            "",
                            KeyCode.TAB,
                            false, false, false, false));
                }
                // 위/왼쪽 방향키 → Shift+Tab 이동
                else if (e.getCode() == KeyCode.UP || e.getCode() == KeyCode.LEFT) {
                    e.consume();
                    focusedNode.fireEvent(new KeyEvent(
                            KeyEvent.KEY_PRESSED,
                            "",
                            "",
                            KeyCode.TAB,
                            true, false, false, false // shift=true
                    ));
                }
            }
        });
    }
}
