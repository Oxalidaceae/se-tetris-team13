package team13.tetris;

import javafx.application.Application;
import javafx.stage.Stage;

import team13.tetris.config.Settings;
import team13.tetris.config.SettingsRepository;
import team13.tetris.game.logic.GameEngine;
import team13.tetris.game.model.Board;
import team13.tetris.game.controller.CompositeGameStateListener;
import team13.tetris.input.KeyInputHandler;
import team13.tetris.scenes.GameScene;

public class App extends Application {
	@Override
	public void start(Stage primaryStage) {
		Board board = new Board(10, 20);
		CompositeGameStateListener composite = new CompositeGameStateListener();
		// 터미널에 보드 상태를 출력하지 않도록 콘솔 리스너는 추가하지 않습니다.
		GameEngine engine = new GameEngine(board, composite);
		engine.startNewGame();

		// settings.json 파일에서 설정 로드
		Settings settings = SettingsRepository.load();
		KeyInputHandler keyInputHandler = new KeyInputHandler(settings);

		GameScene gs = new GameScene(engine, keyInputHandler);
		composite.add(gs);

		primaryStage.setTitle("SE-Tetris - Test Scene");
		primaryStage.setScene(gs.createScene());
		primaryStage.show();

		// ensure key input goes to the scene
		gs.requestFocus();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
