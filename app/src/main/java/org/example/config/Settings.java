package org.example.config;


// 사용자 설정 값 저장 클래스
public class Settings {
    private boolean colorBlindMode = false;
    private String windowSize = "MEDIUM"; // SMALL, MEDIUM, LARGE

    private String keyLeft = "LEFT";
    private String keyRight = "RIGHT";
    private String keyRotate = "UP";
    private String keyDrop = "x";

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
    public void setKeyLeft(String keyLeft) { this.keyLeft = keyLeft; }

    public String getKeyRight() { 
        return keyRight; 
    }
    public void setKeyRight(String keyRight) { 
        this.keyRight = keyRight; 
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
}
