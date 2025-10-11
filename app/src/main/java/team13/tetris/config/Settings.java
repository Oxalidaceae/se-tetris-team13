package team13.tetris.config;


// 사용자 설정 값 저장 클래스
public class Settings {
    private boolean colorBlindMode = false;
    private String windowSize = "MEDIUM"; // SMALL, MEDIUM, LARGE

    private String keyLeft = "A";   // 왼쪽 이동
    private String keyRight = "D"; // 오른쪽 이동
    private String keyDown = "S";   // 아래쪽 이동(한칸씩)
    private String keyRotate = "J";   // 회전
    private String keyDrop = "K";      // hard drop
    private String pause = "P";        // 일시정지
    private String exit = "ESC";     // 게임 종료

    public boolean isColorBlindMode() {
        return colorBlindMode;
    }

    public void setColorBlindMode(boolean colorBlindMode) {
        this.colorBlindMode = colorBlindMode;
    }

    public String getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(String windowSize) {
        this.windowSize = windowSize;
    }

    public String getKeyLeft() { 
        return keyLeft; 
    }
    public void setKeyLeft(String keyLeft) { 
        this.keyLeft = keyLeft; 
    }

    public String getKeyRight() { 
        return keyRight; 
    }
    public void setKeyRight(String keyRight) { 
        this.keyRight = keyRight; 
    }

    public String getKeyDown() { 
        return keyDown; 
    }

    public void setKeyDown(String keyDown) { 
        this.keyDown = keyDown; 
    }

    public String getKeyRotate() { 
        return keyRotate; 
    }
    public void setKeyRotate(String keyRotate) { 
        this.keyRotate = keyRotate; 
    }

    public String getKeyDrop() { 
        return keyDrop; 
    }
    public void setKeyDrop(String keyDrop) { 
        this.keyDrop = keyDrop; 
    }

    public String getPause() { 
        return pause; 
    }

    public void setPause(String pause) { 
        this.pause = pause; 
    }

    public String getExit() { 
        return exit; 
    }

    public void setExit(String exit) { 
        this.exit = exit; 
    }

    public boolean isKeyAlreadyUsed(String key) {
    key = key.toUpperCase();
    return key.equals(keyLeft.toUpperCase()) ||
           key.equals(keyRight.toUpperCase()) ||
           key.equals(keyDown.toUpperCase()) ||
           key.equals(keyRotate.toUpperCase()) ||
           key.equals(keyDrop.toUpperCase()) ||
           key.equals(pause.toUpperCase()) ||
           key.equals(exit.toUpperCase());
}


    public void restoreDefaultKeys() {
        setKeyLeft("A");
        setKeyRight("D");
        setKeyDown("S");
        setKeyRotate("J");
        setKeyDrop("K");
        setPause("P");
        setExit("ESC");
    }

}
