package team13.tetris.game;

/**
 * Timer class for managing Tetris game timing
 * Manages game speed progression and elapsed time tracking
 * Speed level system: speedFactor determines both drop interval and score multiplier
 */
public class Timer {
    
    private double elapsedTime;
    private double speedFactor;
    private static final double BASE_INTERVAL = 1000.0; // milliseconds
    private static final double SPEED_INCREMENT = 0.1;
    private static final double MAX_SPEED_FACTOR = 10.0;
    
    public Timer() {
        this.elapsedTime = 0.0;
        this.speedFactor = 1.0;
    }
    
    /**
     * Advance time by one tick
     * @param deltaTime Time elapsed since previous tick (seconds)
     */
    public void tick(double deltaTime) {
        elapsedTime += deltaTime;
    }
    
    /**
     * Increases game speed by SPEED_INCREMENT, up to MAX_SPEED_FACTOR
     */
    public void increaseSpeed() {
        if (speedFactor < MAX_SPEED_FACTOR) {
            speedFactor += SPEED_INCREMENT;
        }
    }
    
    /**
     * Get block drop interval based on current speed
     * @return Drop interval (milliseconds)
     */
    public double getInterval() {
        return BASE_INTERVAL / speedFactor;
    }
    
    /**
     * @return Elapsed time in seconds
     */
    public double getElapsedTime() {
        return elapsedTime;
    }
    
    /**
     * @return Current speed multiplier (1.0 = base speed)
     */
    public double getSpeedFactor() {
        return speedFactor;
    }
    
    /**
     * Resets timer to initial state (time=0, speed=1.0)
     */
    public void reset() {
        elapsedTime = 0.0;
        speedFactor = 1.0;
    }
    
    /**
     * Sets speed factor if within valid range (0 < factor <= MAX_SPEED_FACTOR)
     * @param speedFactor Speed multiplier to set
     */
    public void setSpeedFactor(double speedFactor) {
        if (speedFactor > 0 && speedFactor <= MAX_SPEED_FACTOR) {
            this.speedFactor = speedFactor;
        }
    }
    
    /**
     * @return Formatted time string in "MM:SS" format
     */
    public String getFormattedTime() {
        int minutes = (int) (elapsedTime / 60);
        int seconds = (int) (elapsedTime % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    /**
     * @return Current speed level based on speed factor (integer part of speedFactor)
     */
    public int getSpeedLevel() {
        return (int) speedFactor;
    }
}
