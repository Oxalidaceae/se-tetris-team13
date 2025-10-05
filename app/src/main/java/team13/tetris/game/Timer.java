package team13.tetris.game;

/**
 * Timer class for managing Tetris game timing
 * Manages falling speed and elapsed time
 */
public class Timer {
    
    private double elapsedTime;      // Elapsed time (seconds)
    private double speedFactor;      // Speed multiplier (1.0 is base speed)
    private static final double BASE_INTERVAL = 1000.0; // Base interval (milliseconds)
    private static final double SPEED_INCREMENT = 0.1;   // Speed increase amount
    private static final double MAX_SPEED_FACTOR = 10.0; // Maximum speed multiplier
    
    /**
     * Timer constructor
     */
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
     * Increase game speed
     * Usually called when level increases
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
     * Get elapsed time
     * @return Elapsed time (seconds)
     */
    public double getElapsedTime() {
        return elapsedTime;
    }
    
    /**
     * Get current speed multiplier
     * @return Speed multiplier
     */
    public double getSpeedFactor() {
        return speedFactor;
    }
    
    /**
     * Reset the timer
     */
    public void reset() {
        elapsedTime = 0.0;
        speedFactor = 1.0;
    }
    
    /**
     * Set specific speed
     * @param speedFactor Speed multiplier to set
     */
    public void setSpeedFactor(double speedFactor) {
        if (speedFactor > 0 && speedFactor <= MAX_SPEED_FACTOR) {
            this.speedFactor = speedFactor;
        }
    }
    
    /**
     * Get formatted time as MM:SS string
     * @return Time string in "MM:SS" format
     */
    public String getFormattedTime() {
        int minutes = (int) (elapsedTime / 60);
        int seconds = (int) (elapsedTime % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    /**
     * Calculate current level based on speed
     * @return Current level
     */
    public int getCurrentLevel() {
        return (int) speedFactor;
    }
}
