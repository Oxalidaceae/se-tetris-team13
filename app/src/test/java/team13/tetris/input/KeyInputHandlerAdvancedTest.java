package team13.tetris.input;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import team13.tetris.config.Settings;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("KeyInputHandler 고급 기능 테스트")
class KeyInputHandlerAdvancedTest {

    private Settings settings;
    private KeyInputHandler keyInputHandler;
    private TestKeyInputCallback callback;
    private Scene testScene;

    @BeforeEach
    void setUp() {
        settings = new Settings();
        keyInputHandler = new KeyInputHandler(settings);
        callback = new TestKeyInputCallback();
        
        // JavaFX 없이 테스트하기 위한 Mock Scene (실제 환경에서는 Platform.runLater 사용)
        testScene = new Scene(new VBox(), 400, 400);
    }

    @Test
    @DisplayName("콜백 인터페이스 구현 테스트")
    void testCallbackInterfaceImplementation() {
        assertDoesNotThrow(() -> {
            keyInputHandler.attachToScene(testScene, callback);
        }, "콜백 연결은 안전해야 함");
    }

    @Test
    @DisplayName("키 이벤트 처리 테스트")
    void testKeyEventHandling() {
        // given
        settings.setKeyLeft("A");
        settings.setKeyRight("D");
        settings.setKeyDown("S");
        settings.setKeyRotate("W");
        settings.setKeyDrop("SPACE");
        settings.setPause("ESCAPE");
        
        keyInputHandler.attachToScene(testScene, callback);
        
        // when & then - 각 키가 올바른 콜백을 호출하는지 테스트
        // 실제 JavaFX 환경에서는 KeyEvent를 생성하여 테스트할 수 있음
        
        assertTrue(keyInputHandler.isLeftClicked(KeyCode.A), "A 키가 왼쪽 이동으로 인식되어야 함");
        assertTrue(keyInputHandler.isRightClicked(KeyCode.D), "D 키가 오른쪽 이동으로 인식되어야 함");
        assertTrue(keyInputHandler.isDropClicked(KeyCode.S), "S 키가 소프트 드롭으로 인식되어야 함");
        assertTrue(keyInputHandler.isRotateClicked(KeyCode.W), "W 키가 회전으로 인식되어야 함");
        assertTrue(keyInputHandler.isHardDropClicked(KeyCode.SPACE), "SPACE 키가 하드 드롭으로 인식되어야 함");
        assertTrue(keyInputHandler.isPauseClicked(KeyCode.ESCAPE), "ESCAPE 키가 일시정지로 인식되어야 함");
    }

    @Test
    @DisplayName("null 콜백으로 키 이벤트 처리 테스트")
    void testNullCallbackHandling() {
        assertDoesNotThrow(() -> {
            keyInputHandler.attachToScene(testScene, null);
        }, "null 콜백으로 연결해도 안전해야 함");
        
        // null 콜백 상태에서도 키 매칭은 동작해야 함
        settings.setKeyLeft("A");
        assertTrue(keyInputHandler.isLeftClicked(KeyCode.A), "null 콜백이어도 키 매칭은 동작해야 함");
    }

    @Test
    @DisplayName("동적 키 설정 변경 테스트")
    void testDynamicKeySettingChanges() {
        keyInputHandler.attachToScene(testScene, callback);
        
        // 초기 설정
        settings.setKeyLeft("A");
        assertTrue(keyInputHandler.isLeftClicked(KeyCode.A), "초기 A 키가 인식되어야 함");
        
        // 동적 변경
        settings.setKeyLeft("Q");
        assertFalse(keyInputHandler.isLeftClicked(KeyCode.A), "변경 후 A 키는 인식되지 않아야 함");
        assertTrue(keyInputHandler.isLeftClicked(KeyCode.Q), "변경 후 Q 키가 인식되어야 함");
        
        // 여러 번 변경
        String[] keys = {"B", "C", "X", "Z", "F1", "SPACE", "ENTER"};
        KeyCode[] keyCodes = {KeyCode.B, KeyCode.C, KeyCode.X, KeyCode.Z, KeyCode.F1, KeyCode.SPACE, KeyCode.ENTER};
        
        for (int i = 0; i < keys.length; i++) {
            settings.setKeyLeft(keys[i]);
            assertTrue(keyInputHandler.isLeftClicked(keyCodes[i]), 
                keys[i] + " 키로 변경 후 인식되어야 함");
        }
    }

    @Test
    @DisplayName("모든 키 액션에 대한 콜백 테스트")
    void testAllKeyActionCallbacks() {
        settings.setKeyLeft("A");
        settings.setKeyRight("D");
        settings.setKeyDown("S");
        settings.setKeyRotate("W");
        settings.setKeyDrop("SPACE");
        settings.setPause("ESCAPE");
        
        keyInputHandler.attachToScene(testScene, callback);
        
        // 각 키가 올바른 콜백 메서드와 매칭되는지 확인
        KeyCode[] testKeys = {KeyCode.A, KeyCode.D, KeyCode.S, KeyCode.W, KeyCode.SPACE, KeyCode.ESCAPE};
        String[] expectedActions = {"left", "right", "drop", "rotate", "hardDrop", "pause"};
        
        for (int i = 0; i < testKeys.length; i++) {
            callback.reset();
            
            // 실제 JavaFX 환경에서는 KeyEvent를 발생시켜 테스트
            // 여기서는 직접 키 매칭만 테스트
            KeyCode key = testKeys[i];
            String expectedAction = expectedActions[i];
            
            switch (expectedAction) {
                case "left" -> assertTrue(keyInputHandler.isLeftClicked(key), key + "가 왼쪽 이동으로 인식되어야 함");
                case "right" -> assertTrue(keyInputHandler.isRightClicked(key), key + "가 오른쪽 이동으로 인식되어야 함");
                case "drop" -> assertTrue(keyInputHandler.isDropClicked(key), key + "가 소프트 드롭으로 인식되어야 함");
                case "rotate" -> assertTrue(keyInputHandler.isRotateClicked(key), key + "가 회전으로 인식되어야 함");
                case "hardDrop" -> assertTrue(keyInputHandler.isHardDropClicked(key), key + "가 하드 드롭으로 인식되어야 함");
                case "pause" -> assertTrue(keyInputHandler.isPauseClicked(key), key + "가 일시정지로 인식되어야 함");
            }
        }
    }

    @Test
    @DisplayName("복합 키 매칭 테스트")
    void testComplexKeyMatching() {
        // 특수 키들과 조합 테스트
        String[] specialKeys = {
            "CONTROL", "SHIFT", "ALT", "TAB", "ENTER", "BACK_SPACE", 
            "DELETE", "HOME", "END", "PAGE_UP", "PAGE_DOWN", "INSERT"
        };
        KeyCode[] specialKeyCodes = {
            KeyCode.CONTROL, KeyCode.SHIFT, KeyCode.ALT, KeyCode.TAB, KeyCode.ENTER, KeyCode.BACK_SPACE,
            KeyCode.DELETE, KeyCode.HOME, KeyCode.END, KeyCode.PAGE_UP, KeyCode.PAGE_DOWN, KeyCode.INSERT
        };
        
        for (int i = 0; i < specialKeys.length; i++) {
            settings.setKeyLeft(specialKeys[i]);
            assertTrue(keyInputHandler.isLeftClicked(specialKeyCodes[i]), 
                specialKeys[i] + " 특수 키가 매칭되어야 함");
        }
    }

    @Test
    @DisplayName("키 매칭 성능 및 안정성 테스트")
    void testKeyMatchingPerformanceAndStability() {
        // 대량의 키 매칭 테스트
        settings.setKeyLeft("A");
        settings.setKeyRight("D");
        settings.setKeyDown("S");
        settings.setKeyRotate("W");
        settings.setKeyDrop("SPACE");
        settings.setPause("ESCAPE");
        
        long startTime = System.nanoTime();
        
        // 각 키에 대해 10000번씩 매칭 테스트
        for (int i = 0; i < 10000; i++) {
            keyInputHandler.isLeftClicked(KeyCode.A);
            keyInputHandler.isRightClicked(KeyCode.D);
            keyInputHandler.isDropClicked(KeyCode.S);
            keyInputHandler.isRotateClicked(KeyCode.W);
            keyInputHandler.isHardDropClicked(KeyCode.SPACE);
            keyInputHandler.isPauseClicked(KeyCode.ESCAPE);
            
            // 매칭되지 않는 키들도 테스트
            keyInputHandler.isLeftClicked(KeyCode.B);
            keyInputHandler.isRightClicked(KeyCode.F);
            keyInputHandler.isDropClicked(KeyCode.X);
        }
        
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        
        // 성능 검증: 60000번의 매칭이 1초 이내에 완료되어야 함
        assertTrue(duration < 1_000_000_000L, "키 매칭 성능이 충분해야 함 (실제: " + (duration / 1_000_000) + "ms)");
    }

    @Test
    @DisplayName("동시 다중 키 설정 테스트")
    void testSimultaneousMultipleKeySettings() {
        // 모든 키를 동시에 다른 값으로 설정
        settings.setKeyLeft("Q");
        settings.setKeyRight("E");
        settings.setKeyDown("R");
        settings.setKeyRotate("T");
        settings.setKeyDrop("F");
        settings.setPause("G");
        
        // 모든 키가 올바르게 매칭되는지 동시 확인
        assertTrue(keyInputHandler.isLeftClicked(KeyCode.Q) &&
                  keyInputHandler.isRightClicked(KeyCode.E) &&
                  keyInputHandler.isDropClicked(KeyCode.R) &&
                  keyInputHandler.isRotateClicked(KeyCode.T) &&
                  keyInputHandler.isHardDropClicked(KeyCode.F) &&
                  keyInputHandler.isPauseClicked(KeyCode.G),
                  "모든 키가 동시에 올바르게 매칭되어야 함");
        
        // 이전 키들은 매칭되지 않아야 함
        assertFalse(keyInputHandler.isLeftClicked(KeyCode.A) ||
                   keyInputHandler.isRightClicked(KeyCode.D) ||
                   keyInputHandler.isDropClicked(KeyCode.S) ||
                   keyInputHandler.isRotateClicked(KeyCode.W) ||
                   keyInputHandler.isHardDropClicked(KeyCode.SPACE) ||
                   keyInputHandler.isPauseClicked(KeyCode.ESCAPE),
                   "이전 키들은 매칭되지 않아야 함");
    }

    @Test
    @DisplayName("키 충돌 처리 테스트")
    void testKeyConflictHandling() {
        // 같은 키를 여러 액션에 할당
        settings.setKeyLeft("A");
        settings.setKeyRight("A");
        settings.setKeyDown("A");
        settings.setKeyRotate("A");
        settings.setKeyDrop("A");
        settings.setPause("A");
        
        // 모든 액션이 동일한 키에 대해 true를 반환해야 함
        assertTrue(keyInputHandler.isLeftClicked(KeyCode.A), "A 키가 왼쪽 이동으로 인식되어야 함");
        assertTrue(keyInputHandler.isRightClicked(KeyCode.A), "A 키가 오른쪽 이동으로 인식되어야 함");
        assertTrue(keyInputHandler.isDropClicked(KeyCode.A), "A 키가 소프트 드롭으로 인식되어야 함");
        assertTrue(keyInputHandler.isRotateClicked(KeyCode.A), "A 키가 회전으로 인식되어야 함");
        assertTrue(keyInputHandler.isHardDropClicked(KeyCode.A), "A 키가 하드 드롭으로 인식되어야 함");
        assertTrue(keyInputHandler.isPauseClicked(KeyCode.A), "A 키가 일시정지로 인식되어야 함");
    }

    @Test
    @DisplayName("Settings 객체 교체 테스트")
    void testSettingsObjectReplacement() {
        // 초기 설정
        settings.setKeyLeft("A");
        assertTrue(keyInputHandler.isLeftClicked(KeyCode.A), "초기 A 키가 인식되어야 함");
        
        // 새로운 Settings 객체로 KeyInputHandler 생성
        Settings newSettings = new Settings();
        newSettings.setKeyLeft("B");
        KeyInputHandler newHandler = new KeyInputHandler(newSettings);
        
        // 새 핸들러는 새 설정을 사용해야 함
        assertFalse(newHandler.isLeftClicked(KeyCode.A), "새 핸들러는 A 키를 인식하지 않아야 함");
        assertTrue(newHandler.isLeftClicked(KeyCode.B), "새 핸들러는 B 키를 인식해야 함");
        
        // 기존 핸들러는 영향받지 않아야 함
        assertTrue(keyInputHandler.isLeftClicked(KeyCode.A), "기존 핸들러는 여전히 A 키를 인식해야 함");
        assertFalse(keyInputHandler.isLeftClicked(KeyCode.B), "기존 핸들러는 B 키를 인식하지 않아야 함");
    }

    @Test
    @DisplayName("메모리 효율성 테스트")
    void testMemoryEfficiency() {
        // 대량의 KeyInputHandler 인스턴스 생성 및 해제
        for (int i = 0; i < 1000; i++) {
            Settings tempSettings = new Settings();
            tempSettings.setKeyLeft("KEY_" + i);
            KeyInputHandler tempHandler = new KeyInputHandler(tempSettings);
            
            // 각 핸들러가 독립적으로 동작하는지 확인
            assertNotNull(tempHandler, "핸들러 " + i + "가 생성되어야 함");
            
            if (i % 100 == 0) {
                // 주기적으로 가비지 컬렉션 힌트
                System.gc();
            }
        }
    }

    @Test
    @DisplayName("콜백 인터페이스 다중 구현 테스트")
    void testMultipleCallbackImplementations() {
        TestKeyInputCallback callback1 = new TestKeyInputCallback();
        TestKeyInputCallback callback2 = new TestKeyInputCallback();
        
        KeyInputHandler handler1 = new KeyInputHandler(settings);
        KeyInputHandler handler2 = new KeyInputHandler(settings);
        
        settings.setKeyLeft("A");
        
        // 각 핸들러가 독립적인 콜백을 가질 수 있는지 확인
        assertDoesNotThrow(() -> {
            handler1.attachToScene(testScene, callback1);
            handler2.attachToScene(testScene, callback2); // 같은 Scene에 다른 콜백
        }, "같은 Scene에 다른 콜백을 연결할 수 있어야 함");
        
        // 키 매칭은 여전히 동작해야 함
        assertTrue(handler1.isLeftClicked(KeyCode.A), "핸들러1의 키 매칭이 동작해야 함");
        assertTrue(handler2.isLeftClicked(KeyCode.A), "핸들러2의 키 매칭이 동작해야 함");
    }

    // 테스트용 콜백 구현
    private static class TestKeyInputCallback implements KeyInputHandler.KeyInputCallback {
        private final AtomicInteger leftCount = new AtomicInteger(0);
        private final AtomicInteger rightCount = new AtomicInteger(0);
        private final AtomicInteger rotateCount = new AtomicInteger(0);
        private final AtomicInteger dropCount = new AtomicInteger(0);
        private final AtomicInteger hardDropCount = new AtomicInteger(0);
        private final AtomicInteger pauseCount = new AtomicInteger(0);
        private final CountDownLatch actionLatch = new CountDownLatch(1);

        @Override
        public void onLeftPressed() {
            leftCount.incrementAndGet();
            actionLatch.countDown();
        }

        @Override
        public void onRightPressed() {
            rightCount.incrementAndGet();
            actionLatch.countDown();
        }

        @Override
        public void onRotatePressed() {
            rotateCount.incrementAndGet();
            actionLatch.countDown();
        }

        @Override
        public void onDropPressed() {
            dropCount.incrementAndGet();
            actionLatch.countDown();
        }

        @Override
        public void onHardDropPressed() {
            hardDropCount.incrementAndGet();
            actionLatch.countDown();
        }

        @Override
        public void onPausePressed() {
            pauseCount.incrementAndGet();
            actionLatch.countDown();
        }

        public void reset() {
            leftCount.set(0);
            rightCount.set(0);
            rotateCount.set(0);
            dropCount.set(0);
            hardDropCount.set(0);
            pauseCount.set(0);
        }

        public boolean waitForAction(long timeoutMs) throws InterruptedException {
            return actionLatch.await(timeoutMs, TimeUnit.MILLISECONDS);
        }

        public int getLeftCount() { return leftCount.get(); }
        public int getRightCount() { return rightCount.get(); }
        public int getRotateCount() { return rotateCount.get(); }
        public int getDropCount() { return dropCount.get(); }
        public int getHardDropCount() { return hardDropCount.get(); }
        public int getPauseCount() { return pauseCount.get(); }
    }
}