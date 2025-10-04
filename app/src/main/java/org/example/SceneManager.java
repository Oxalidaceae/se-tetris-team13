package org.example;

import org.example.config.Settings;
import org.example.config.SettingsRepository;
import org.example.scenes.GameScene;
import org.example.scenes.KeySettingsScene;
import org.example.scenes.MainMenuScene;
import org.example.scenes.ScoreboardScene;
import org.example.scenes.SettingsScene;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {
    private final Stage stage;  
    private boolean colorBlindMode = false;    // 색맹 모드 상태 변수 

    public SceneManager(Stage stage) {
        this.stage = stage;
    }

    //  메인 메뉴 씬으로 전환
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
        changeScene(new GameScene(this, settings).getScene());
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
        applyStylesheet(stage.getScene());  // 현재 씬에 스타일시트 적용
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
            getClass().getResource(cssPath).toExternalForm()
        );
    }
}
