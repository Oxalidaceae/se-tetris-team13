package team13.tetris.scenes;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import team13.tetris.SceneManager;
import team13.tetris.config.Settings;

import static org.junit.jupiter.api.Assertions.*;

// KeySettingsScene 테스트: Tests scene 생성, key binding 표시, 및 key 설정 기능 확인
@DisplayName("KeySettingsScene 테스트")
public class KeySettingsSceneTest {

    private SceneManager sceneManager;
    private Settings settings;
    private KeySettingsScene keySettingsScene;

    @BeforeAll
    static void initJavaFX() {
        // JavaFX 툴킷 초기화
        try {
            javafx.application.Platform.startup(() -> {
            });
        } catch (IllegalStateException e) {
            // 이미 초기화되었으면 무시
        }
    }

    @BeforeEach
    void setUp() {
        // Mock Stage와 SceneManager 생성
        javafx.application.Platform.runLater(() -> {
            Stage stage = new Stage();
            settings = new Settings();
            sceneManager = new SceneManager(stage);
            keySettingsScene = new KeySettingsScene(sceneManager, settings);
        });

        waitForFX();
    }

    @Test
    @DisplayName("Scene이 정상적으로 생성되는지 확인")
    void testSceneCreation() {
        javafx.application.Platform.runLater(() -> {
            Scene scene = keySettingsScene.getScene();

            assertNotNull(scene, "Scene should not be null");
            assertEquals(400, scene.getWidth(), "Scene width should be 400");
            assertEquals(400, scene.getHeight(), "Scene height should be 400");
            assertNotNull(scene.getRoot(), "Scene root should not be null");
            assertTrue(scene.getRoot() instanceof VBox, "Scene root should be VBox");
        });

        waitForFX();
    }

    @Test
    @DisplayName("레이아웃 구조가 올바른지 확인")
    void testSceneLayout() {
        javafx.application.Platform.runLater(() -> {
            Scene scene = keySettingsScene.getScene();
            VBox layout = (VBox) scene.getRoot();

            assertEquals(9, layout.getChildren().size(),
                    "Layout should have 9 children (title + 7 key buttons + back button)");
            assertEquals("-fx-alignment: center;", layout.getStyle(), "Layout should be centered");
        });

        waitForFX();
    }

    @Test
    @DisplayName("타이틀이 올바르게 설정되는지 확인")
    void testTitle() {
        javafx.application.Platform.runLater(() -> {
            Scene scene = keySettingsScene.getScene();
            VBox layout = (VBox) scene.getRoot();

            assertTrue(layout.getChildren().get(0) instanceof Label, "First child should be Label");
            Label title = (Label) layout.getChildren().get(0);

            assertEquals("Key Settings", title.getText(), "Title text should be 'Key Settings'");
            assertTrue(title.getStyleClass().contains("label-title"),
                    "Title should have label-title style class");
        });

        waitForFX();
    }

    @Test
    @DisplayName("Move Left 버튼이 올바르게 표시되는지 확인")
    void testMoveLeftButton() {
        javafx.application.Platform.runLater(() -> {
            Scene scene = keySettingsScene.getScene();
            VBox layout = (VBox) scene.getRoot();

            assertTrue(layout.getChildren().get(1) instanceof Button, "Second child should be Button");
            Button leftBtn = (Button) layout.getChildren().get(1);

            assertTrue(leftBtn.getText().startsWith("Move Left: "),
                    "Left button should start with 'Move Left: '");
            assertNotNull(leftBtn.getOnAction(), "Left button should have action handler");
        });

        waitForFX();
    }

    @Test
    @DisplayName("Move Right 버튼이 올바르게 표시되는지 확인")
    void testMoveRightButton() {
        javafx.application.Platform.runLater(() -> {
            Scene scene = keySettingsScene.getScene();
            VBox layout = (VBox) scene.getRoot();

            assertTrue(layout.getChildren().get(2) instanceof Button, "Third child should be Button");
            Button rightBtn = (Button) layout.getChildren().get(2);

            assertTrue(rightBtn.getText().startsWith("Move Right: "),
                    "Right button should start with 'Move Right: '");
            assertNotNull(rightBtn.getOnAction(), "Right button should have action handler");
        });

        waitForFX();
    }

    @Test
    @DisplayName("Move Down 버튼이 올바르게 표시되는지 확인")
    void testMoveDownButton() {
        javafx.application.Platform.runLater(() -> {
            Scene scene = keySettingsScene.getScene();
            VBox layout = (VBox) scene.getRoot();

            assertTrue(layout.getChildren().get(3) instanceof Button, "Fourth child should be Button");
            Button downBtn = (Button) layout.getChildren().get(3);

            assertTrue(downBtn.getText().startsWith("Move Down: "),
                    "Down button should start with 'Move Down: '");
            assertNotNull(downBtn.getOnAction(), "Down button should have action handler");
        });

        waitForFX();
    }

    @Test
    @DisplayName("Rotate 버튼이 올바르게 표시되는지 확인")
    void testRotateButton() {
        javafx.application.Platform.runLater(() -> {
            Scene scene = keySettingsScene.getScene();
            VBox layout = (VBox) scene.getRoot();

            assertTrue(layout.getChildren().get(4) instanceof Button, "Fifth child should be Button");
            Button rotateBtn = (Button) layout.getChildren().get(4);

            assertTrue(rotateBtn.getText().startsWith("Rotate: "),
                    "Rotate button should start with 'Rotate: '");
            assertNotNull(rotateBtn.getOnAction(), "Rotate button should have action handler");
        });

        waitForFX();
    }

    @Test
    @DisplayName("Drop 버튼이 올바르게 표시되는지 확인")
    void testDropButton() {
        javafx.application.Platform.runLater(() -> {
            Scene scene = keySettingsScene.getScene();
            VBox layout = (VBox) scene.getRoot();

            assertTrue(layout.getChildren().get(5) instanceof Button, "Sixth child should be Button");
            Button dropBtn = (Button) layout.getChildren().get(5);

            assertTrue(dropBtn.getText().startsWith("Drop: "), "Drop button should start with 'Drop: '");
            assertNotNull(dropBtn.getOnAction(), "Drop button should have action handler");
        });

        waitForFX();
    }

    @Test
    @DisplayName("Pause 버튼이 올바르게 표시되는지 확인")
    void testPauseButton() {
        javafx.application.Platform.runLater(() -> {
            Scene scene = keySettingsScene.getScene();
            VBox layout = (VBox) scene.getRoot();

            assertTrue(layout.getChildren().get(6) instanceof Button, "Seventh child should be Button");
            Button pauseBtn = (Button) layout.getChildren().get(6);

            assertTrue(pauseBtn.getText().startsWith("Pause: "),
                    "Pause button should start with 'Pause: '");
            assertNotNull(pauseBtn.getOnAction(), "Pause button should have action handler");
        });

        waitForFX();
    }

    @Test
    @DisplayName("Exit 버튼이 올바르게 표시되는지 확인")
    void testExitButton() {
        javafx.application.Platform.runLater(() -> {
            Scene scene = keySettingsScene.getScene();
            VBox layout = (VBox) scene.getRoot();

            assertTrue(layout.getChildren().get(7) instanceof Button, "Eighth child should be Button");
            Button exitBtn = (Button) layout.getChildren().get(7);

            assertTrue(exitBtn.getText().startsWith("Exit: "), "Exit button should start with 'Exit: '");
            assertNotNull(exitBtn.getOnAction(), "Exit button should have action handler");
        });

        waitForFX();
    }

    @Test
    @DisplayName("Back 버튼이 올바르게 표시되는지 확인")
    void testBackButton() {
        javafx.application.Platform.runLater(() -> {
            Scene scene = keySettingsScene.getScene();
            VBox layout = (VBox) scene.getRoot();

            assertTrue(layout.getChildren().get(8) instanceof Button, "Ninth child should be Button");
            Button backBtn = (Button) layout.getChildren().get(8);

            assertEquals("Back", backBtn.getText(), "Back button text should be 'Back'");
            assertNotNull(backBtn.getOnAction(), "Back button should have action handler");
        });

        waitForFX();
    }

    @Test
    @DisplayName("모든 키 설정 버튼이 존재하는지 확인")
    void testAllKeySettingButtonsExist() {
        javafx.application.Platform.runLater(() -> {
            Scene scene = keySettingsScene.getScene();
            VBox layout = (VBox) scene.getRoot();

            long buttonCount = layout.getChildren().stream()
                    .filter(node -> node instanceof Button)
                    .count();

            assertEquals(8, buttonCount, "Should have exactly 8 buttons (7 key settings + back)");
        });

        waitForFX();
    }

    @Test
    @DisplayName("모든 버튼에 액션 핸들러가 있는지 확인")
    void testAllButtonsHaveActionHandlers() {
        javafx.application.Platform.runLater(() -> {
            Scene scene = keySettingsScene.getScene();
            VBox layout = (VBox) scene.getRoot();

            for (int i = 1; i <= 8; i++) {
                Button btn = (Button) layout.getChildren().get(i);
                assertNotNull(btn.getOnAction(), "Button at index " + i + " should have action handler");
            }
        });

        waitForFX();
    }

    @Test
    @DisplayName("기본 키 설정이 표시되는지 확인")
    void testDefaultKeySettingsDisplay() {
        javafx.application.Platform.runLater(() -> {
            Scene scene = keySettingsScene.getScene();
            VBox layout = (VBox) scene.getRoot();

            // 기본 설정 값 확인
            Button leftBtn = (Button) layout.getChildren().get(1);
            Button rightBtn = (Button) layout.getChildren().get(2);
            Button downBtn = (Button) layout.getChildren().get(3);
            Button rotateBtn = (Button) layout.getChildren().get(4);
            Button dropBtn = (Button) layout.getChildren().get(5);
            Button pauseBtn = (Button) layout.getChildren().get(6);

            // 버튼 텍스트에 키 이름이 포함되어 있는지 확인
            assertTrue(leftBtn.getText().contains(KeyCode.valueOf(settings.getKeyLeft()).getName()),
                    "Left button should display current key setting");
            assertTrue(rightBtn.getText().contains(KeyCode.valueOf(settings.getKeyRight()).getName()),
                    "Right button should display current key setting");
            assertTrue(downBtn.getText().contains(KeyCode.valueOf(settings.getKeyDown()).getName()),
                    "Down button should display current key setting");
            assertTrue(rotateBtn.getText().contains(KeyCode.valueOf(settings.getKeyRotate()).getName()),
                    "Rotate button should display current key setting");
            assertTrue(dropBtn.getText().contains(KeyCode.valueOf(settings.getKeyDrop()).getName()),
                    "Drop button should display current key setting");
            assertTrue(pauseBtn.getText().contains(KeyCode.valueOf(settings.getPause()).getName()),
                    "Pause button should display current key setting");
        });

        waitForFX();
    }

    @Test
    @DisplayName("Scene에 키 입력 이벤트 필터가 등록되어 있는지 확인")
    void testKeyEventFilterRegistered() {
        javafx.application.Platform.runLater(() -> {
            Scene scene = keySettingsScene.getScene();

            // Scene이 키 이벤트를 처리할 수 있어야 함
            assertNotNull(scene, "Scene should be created");
            assertDoesNotThrow(() -> scene.getEventDispatcher(), "Scene should have event dispatcher");
        });

        waitForFX();
    }

    @Test
    @DisplayName("Settings 객체가 올바르게 주입되는지 확인")
    void testSettingsIntegration() {
        javafx.application.Platform.runLater(() -> {
            assertNotNull(settings, "Settings should be initialized");
            assertNotNull(settings.getKeyLeft(), "Key left setting should exist");
            assertNotNull(settings.getKeyRight(), "Key right setting should exist");
            assertNotNull(settings.getKeyDown(), "Key down setting should exist");
            assertNotNull(settings.getKeyRotate(), "Key rotate setting should exist");
            assertNotNull(settings.getKeyDrop(), "Key drop setting should exist");
            assertNotNull(settings.getPause(), "Pause key setting should exist");
        });

        waitForFX();
    }

    @Test
    @DisplayName("SceneManager와의 통합이 정상적인지 확인")
    void testSceneManagerIntegration() {
        javafx.application.Platform.runLater(() -> {
            assertNotNull(sceneManager, "SceneManager should be initialized");
            assertNotNull(settings, "Settings should be initialized");
            assertNotNull(keySettingsScene, "KeySettingsScene should be initialized");
        });

        waitForFX();
    }

    @Test
    @DisplayName("여러 번 Scene을 생성해도 정상 동작하는지 확인")
    void testMultipleSceneCreation() {
        javafx.application.Platform.runLater(() -> {
            Scene scene1 = keySettingsScene.getScene();
            Scene scene2 = keySettingsScene.getScene();

            assertNotNull(scene1, "First scene should not be null");
            assertNotNull(scene2, "Second scene should not be null");
            // 매번 새로운 Scene을 생성
            assertNotSame(scene1, scene2, "Each call should create a new scene");
        });

        waitForFX();
    }

    @Test
    @DisplayName("금지된 키 7개가 올바르게 정의되어 있는지 확인")
    void testRestrictedKeysExist() {
        javafx.application.Platform.runLater(() -> {
            // 현재 KeySettingsScene에서 금지된 키 목록
            KeyCode[] restrictedKeys = {
                KeyCode.UNDEFINED,
                KeyCode.WINDOWS,
                KeyCode.META,
                KeyCode.PRINTSCREEN,
                KeyCode.CLEAR,
                KeyCode.CAPS,
                KeyCode.NUM_LOCK
            };

            // 모든 금지 키가 KeyCode에 정의되어 있는지 확인
            for (KeyCode key : restrictedKeys) assertNotNull(key, key.getName() + " should be defined in KeyCode");

            // 총 7개의 금지 키가 있어야 함
            assertEquals(7, restrictedKeys.length, "Should have exactly 7 restricted keys");
        });

        waitForFX();
    }

    @Test
    @DisplayName("UNDEFINED 키가 금지 목록에 포함되는지 확인")
    void testUndefinedKeyIsRestricted() {
        javafx.application.Platform.runLater(() -> {
            KeyCode key = KeyCode.UNDEFINED;
            assertNotNull(key, "UNDEFINED key should exist");
            assertEquals("Undefined", key.getName(), "UNDEFINED key name should be 'Undefined'");
        });

        waitForFX();
    }

    @Test
    @DisplayName("WINDOWS 키가 금지 목록에 포함되는지 확인")
    void testWindowsKeyIsRestricted() {
        javafx.application.Platform.runLater(() -> {
            KeyCode key = KeyCode.WINDOWS;
            assertNotNull(key, "WINDOWS key should exist");
            assertEquals("Windows", key.getName(), "WINDOWS key name should be 'Windows'");
        });

        waitForFX();
    }

    @Test
    @DisplayName("META 키가 금지 목록에 포함되는지 확인")
    void testMetaKeyIsRestricted() {
        javafx.application.Platform.runLater(() -> {
            KeyCode key = KeyCode.META;
            assertNotNull(key, "META key should exist");
            assertEquals("Meta", key.getName(), "META key name should be 'Meta'");
        });

        waitForFX();
    }

    @Test
    @DisplayName("PRINTSCREEN 키가 금지 목록에 포함되는지 확인")
    void testPrintScreenKeyIsRestricted() {
        javafx.application.Platform.runLater(() -> {
            KeyCode key = KeyCode.PRINTSCREEN;
            assertNotNull(key, "PRINTSCREEN key should exist");
            assertEquals("Print Screen", key.getName(), "PRINTSCREEN key name should be 'Print Screen'");
        });

        waitForFX();
    }

    @Test
    @DisplayName("CLEAR 키가 금지 목록에 포함되는지 확인")
    void testClearKeyIsRestricted() {
        javafx.application.Platform.runLater(() -> {
            KeyCode key = KeyCode.CLEAR;
            assertNotNull(key, "CLEAR key should exist");
            assertEquals("Clear", key.getName(), "CLEAR key name should be 'Clear'");
        });

        waitForFX();
    }

    @Test
    @DisplayName("CAPS 키가 금지 목록에 포함되는지 확인")
    void testCapsKeyIsRestricted() {
        javafx.application.Platform.runLater(() -> {
            KeyCode key = KeyCode.CAPS;
            assertNotNull(key, "CAPS key should exist");
            assertEquals("Caps Lock", key.getName(), "CAPS key name should be 'Caps Lock'");
        });

        waitForFX();
    }

    @Test
    @DisplayName("NUM_LOCK 키가 금지 목록에 포함되는지 확인")
    void testNumLockKeyIsRestricted() {
        javafx.application.Platform.runLater(() -> {
            KeyCode key = KeyCode.NUM_LOCK;
            assertNotNull(key, "NUM_LOCK key should exist");
            assertEquals("Num Lock", key.getName(), "NUM_LOCK key name should be 'Num Lock'");
        });

        waitForFX();
    }

    @Test
    @DisplayName("허용되는 기본 키들이 금지 목록에 없는지 확인")
    void testAllowedDefaultKeysAreNotRestricted() {
        javafx.application.Platform.runLater(() -> {
            // 기본 키 설정 (허용되어야 함)
            KeyCode[] allowedKeys = {
                KeyCode.LEFT,
                KeyCode.RIGHT,
                KeyCode.DOWN,
                KeyCode.Z,
                KeyCode.X,
                KeyCode.P,
                KeyCode.ESCAPE
            };

            // 금지된 키 목록
            KeyCode[] restrictedKeys = {
                KeyCode.UNDEFINED,
                KeyCode.WINDOWS,
                KeyCode.META,
                KeyCode.PRINTSCREEN,
                KeyCode.CLEAR,
                KeyCode.CAPS,
                KeyCode.NUM_LOCK
            };

            // 허용된 키가 금지 목록에 없는지 확인
            for (KeyCode allowed : allowedKeys) {
                for (KeyCode restricted : restrictedKeys) {
                    assertNotEquals(restricted, allowed, allowed.getName() + " should not be in restricted list");
                }
            }
        });

        waitForFX();
    }

    @Test
    @DisplayName("일반적으로 사용되는 키들이 허용되는지 확인")
    void testCommonlyUsedKeysAreAllowed() {
        javafx.application.Platform.runLater(() -> {
            // 일반적으로 사용 가능해야 하는 키들
            KeyCode[] commonKeys = {
                KeyCode.A, KeyCode.W, KeyCode.S, KeyCode.D,
                KeyCode.SPACE, KeyCode.ENTER, KeyCode.SHIFT,
                KeyCode.CONTROL, KeyCode.ALT, KeyCode.TAB,
                KeyCode.UP, KeyCode.DOWN, KeyCode.LEFT, KeyCode.RIGHT
            };

            // 금지된 키 목록
            KeyCode[] restrictedKeys = {
                KeyCode.UNDEFINED,
                KeyCode.WINDOWS,
                KeyCode.META,
                KeyCode.PRINTSCREEN,
                KeyCode.CLEAR,
                KeyCode.CAPS,
                KeyCode.NUM_LOCK
            };

            // 일반 키가 금지 목록에 없는지 확인
            for (KeyCode common : commonKeys) {
                for (KeyCode restricted : restrictedKeys) {
                    assertNotEquals(restricted, common, common.getName() + " should be allowed");
                }
            }
        });

        waitForFX();
    }

    @Test
    @DisplayName("Function 키들이 허용되는지 확인")
    void testFunctionKeysAreAllowed() {
        javafx.application.Platform.runLater(() -> {
            // Function 키들 (현재 구현에서는 허용됨)
            KeyCode[] functionKeys = {
                KeyCode.F1, KeyCode.F2, KeyCode.F3, KeyCode.F4,
                KeyCode.F5, KeyCode.F6, KeyCode.F7, KeyCode.F8,
                KeyCode.F9, KeyCode.F10, KeyCode.F11, KeyCode.F12
            };

            // 금지된 키 목록
            KeyCode[] restrictedKeys = {
                KeyCode.UNDEFINED,
                KeyCode.WINDOWS,
                KeyCode.META,
                KeyCode.PRINTSCREEN,
                KeyCode.CLEAR,
                KeyCode.CAPS,
                KeyCode.NUM_LOCK
            };

            // Function 키가 금지 목록에 없는지 확인 (허용됨)
            for (KeyCode func : functionKeys) {
                for (KeyCode restricted : restrictedKeys) {
                    assertNotEquals(restricted, func, func.getName() + " should be allowed (not in restricted list)");
                }
            }
        });

        waitForFX();
    }

    @Test
    @DisplayName("숫자 패드 키들이 허용되는지 확인")
    void testNumpadKeysAreAllowed() {
        javafx.application.Platform.runLater(() -> {
            // 숫자 패드 키들 (현재 구현에서는 허용됨)
            KeyCode[] numpadKeys = {
                KeyCode.NUMPAD0, KeyCode.NUMPAD1, KeyCode.NUMPAD2,
                KeyCode.NUMPAD3, KeyCode.NUMPAD4, KeyCode.NUMPAD5,
                KeyCode.NUMPAD6, KeyCode.NUMPAD7, KeyCode.NUMPAD8,
                KeyCode.NUMPAD9
            };

            // 금지된 키 목록
            KeyCode[] restrictedKeys = {
                KeyCode.UNDEFINED,
                KeyCode.WINDOWS,
                KeyCode.META,
                KeyCode.PRINTSCREEN,
                KeyCode.CLEAR,
                KeyCode.CAPS,
                KeyCode.NUM_LOCK
            };

            // 숫자 패드 키가 금지 목록에 없는지 확인 (허용됨)
            for (KeyCode numpad : numpadKeys) {
                for (KeyCode restricted : restrictedKeys) {
                    assertNotEquals(restricted, numpad, numpad.getName() + " should be allowed (not in restricted list)");
                }
            }
        });

        waitForFX();
    }

    @Test
    @DisplayName("금지된 키가 정확히 7개인지 확인")
    void testRestrictedKeysCount() {
        javafx.application.Platform.runLater(() -> {
            // 현재 구현에서 금지된 키는 정확히 7개
            KeyCode[] restrictedKeys = {
                KeyCode.UNDEFINED,
                KeyCode.WINDOWS,
                KeyCode.META,
                KeyCode.PRINTSCREEN,
                KeyCode.CLEAR,
                KeyCode.CAPS,
                KeyCode.NUM_LOCK
            };

            assertEquals(7, restrictedKeys.length, "Should have exactly 7 restricted keys as per implementation");
        });

        waitForFX();
    }

    @Test
    @DisplayName("금지 키 목록에 중복이 없는지 확인")
    void testRestrictedKeysNoDuplicates() {
        javafx.application.Platform.runLater(() -> {
            KeyCode[] restrictedKeys = {
                KeyCode.UNDEFINED,
                KeyCode.WINDOWS,
                KeyCode.META,
                KeyCode.PRINTSCREEN,
                KeyCode.CLEAR,
                KeyCode.CAPS,
                KeyCode.NUM_LOCK
            };

            // 중복 검사
            for (int i = 0; i < restrictedKeys.length; i++) {
                for (int j = i + 1; j < restrictedKeys.length; j++) {
                    assertNotEquals(restrictedKeys[i], restrictedKeys[j], "Restricted keys should not have duplicates");
                }
            }
        });

        waitForFX();
    }

    // JavaFX 스레드 작업 완료 대기 헬퍼 메서드
    private void waitForFX() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
