package team13.tetris.game;

import java.util.Scanner;

/**
 * Interactive demo for Timer class
 */
public class TimerDemo {
    
    public static void main(String[] args) {
        System.out.println("=== Timer Class Demo ===");
        System.out.println("Testing Timer functionality with interactive and automatic tests");
        System.out.println();
        
        // Run automatic tests first
        runAutomaticTests();
        
        // Run interactive demo
        runInteractiveDemo();
    }
    
    private static void runAutomaticTests() {
        System.out.println("1. AUTOMATIC TESTS");
        System.out.println("==================");
        
        Timer timer = new Timer();
        
        // Test 1: Initial state
        System.out.println("Test 1 - Initial State:");
        printTimerState(timer);
        assert timer.getElapsedTime() == 0.0 : "Initial time should be 0";
        assert timer.getSpeedFactor() == 1.0 : "Initial speed should be 1.0";
        System.out.println("[OK] PASSED\n");
        
        // Test 2: Time progression
        System.out.println("Test 2 - Time Progression:");
        timer.tick(1.5);
        timer.tick(2.5);
        printTimerState(timer);
        assert Math.abs(timer.getElapsedTime() - 4.0) < 0.001 : "Time should be 4.0";
        System.out.println("[OK] PASSED\n");
        
        // Test 3: Speed increases
        System.out.println("Test 3 - Speed Increases:");
        for (int i = 0; i < 5; i++) {
            timer.increaseSpeed();
        }
        printTimerState(timer);
        assert Math.abs(timer.getSpeedFactor() - 1.5) < 0.001 : "Speed should be 1.5";
        assert timer.getCurrentLevel() == 1 : "Level should still be 1";
        System.out.println("[OK] PASSED\n");
        
        // Test 4: Level progression
        System.out.println("Test 4 - Level Progression:");
        timer.setSpeedFactor(2.7);
        printTimerState(timer);
        assert timer.getCurrentLevel() == 2 : "Level should be 2";
        System.out.println("[OK] PASSED\n");
        
        // Test 5: Max speed limit
        System.out.println("Test 5 - Max Speed Limit:");
        timer.setSpeedFactor(15.0); // Try to exceed max
        printTimerState(timer);
        assert timer.getSpeedFactor() == 2.7 : "Speed should remain unchanged";
        
        timer.setSpeedFactor(10.0);
        timer.increaseSpeed(); // Try to exceed via increase
        printTimerState(timer);
        assert timer.getSpeedFactor() == 10.0 : "Speed should not exceed 10.0";
        System.out.println("[OK] PASSED\n");
        
        // Test 6: Reset functionality
        System.out.println("Test 6 - Reset Functionality:");
        timer.reset();
        printTimerState(timer);
        assert timer.getElapsedTime() == 0.0 : "Time should be reset";
        assert timer.getSpeedFactor() == 1.0 : "Speed should be reset";
        System.out.println("[OK] PASSED\n");
        
        // Test 7: Realistic game simulation
        System.out.println("Test 7 - Game Simulation (30 seconds, speed increases every 5s):");
        simulateGame(timer, 30, 5);
        System.out.println("[OK] Simulation completed\n");
        
        System.out.println("All automatic tests PASSED!");
        System.out.println("=" + "=".repeat(50) + "\n");
    }
    
    private static void runInteractiveDemo() {
        System.out.println("2. INTERACTIVE DEMO");
        System.out.println("===================");
        System.out.println("Commands:");
        System.out.println("  tick <seconds>     - Advance time by seconds");
        System.out.println("  speed              - Increase speed");
        System.out.println("  set <factor>       - Set speed factor");
        System.out.println("  reset              - Reset timer");
        System.out.println("  simulate <sec> <inc> - Simulate game (sec=duration, inc=speed increase interval)");
        System.out.println("  quit               - Exit demo");
        System.out.println();
        
        Timer timer = new Timer();
        Scanner scanner = new Scanner(System.in);
        
        printTimerState(timer);
        
        while (true) {
            System.out.print("timer> ");
            String input = scanner.nextLine().trim();
            String[] parts = input.split("\\s+");
            
            try {
                switch (parts[0].toLowerCase()) {
                    case "tick":
                        if (parts.length > 1) {
                            double seconds = Double.parseDouble(parts[1]);
                            timer.tick(seconds);
                            System.out.println("Advanced time by " + seconds + " seconds");
                        } else {
                            timer.tick(1.0);
                            System.out.println("Advanced time by 1 second");
                        }
                        printTimerState(timer);
                        break;
                        
                    case "speed":
                        double oldSpeed = timer.getSpeedFactor();
                        timer.increaseSpeed();
                        double newSpeed = timer.getSpeedFactor();
                        if (newSpeed > oldSpeed) {
                            System.out.println("Speed increased from " + String.format("%.1f", oldSpeed) + 
                                             " to " + String.format("%.1f", newSpeed));
                        } else {
                            System.out.println("Speed already at maximum (" + newSpeed + ")");
                        }
                        printTimerState(timer);
                        break;
                        
                    case "set":
                        if (parts.length > 1) {
                            double factor = Double.parseDouble(parts[1]);
                            double oldFactor = timer.getSpeedFactor();
                            timer.setSpeedFactor(factor);
                            if (timer.getSpeedFactor() == factor) {
                                System.out.println("Speed set to " + factor);
                            } else {
                                System.out.println("Invalid speed factor. Speed remains " + oldFactor);
                            }
                        } else {
                            System.out.println("Usage: set <factor>");
                        }
                        printTimerState(timer);
                        break;
                        
                    case "reset":
                        timer.reset();
                        System.out.println("Timer reset");
                        printTimerState(timer);
                        break;
                        
                    case "simulate":
                        int duration = parts.length > 1 ? Integer.parseInt(parts[1]) : 10;
                        int interval = parts.length > 2 ? Integer.parseInt(parts[2]) : 2;
                        System.out.println("Simulating " + duration + " seconds of gameplay...");
                        simulateGame(timer, duration, interval);
                        break;
                        
                    case "quit":
                    case "exit":
                        System.out.println("Demo ended. Goodbye!");
                        scanner.close();
                        return;
                        
                    case "help":
                        System.out.println("Available commands: tick, speed, set, reset, simulate, quit");
                        break;
                        
                    default:
                        System.out.println("Unknown command. Type 'help' for available commands.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format. Please try again.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
            
            System.out.println();
        }
    }
    
    private static void printTimerState(Timer timer) {
        System.out.println("  Time: " + timer.getFormattedTime() + 
                          " (" + String.format("%.1f", timer.getElapsedTime()) + "s)");
        System.out.println("  Speed: " + String.format("%.1f", timer.getSpeedFactor()) + "x");
        System.out.println("  Level: " + timer.getCurrentLevel());
        System.out.println("  Drop Interval: " + String.format("%.0f", timer.getInterval()) + "ms");
    }
    
    private static void simulateGame(Timer timer, int durationSeconds, int speedIncreaseInterval) {
        Timer gameTimer = new Timer();
        gameTimer.setSpeedFactor(timer.getSpeedFactor()); // Start with current speed
        
        System.out.println("Starting simulation with speed " + timer.getSpeedFactor() + "x");
        
        for (int second = 1; second <= durationSeconds; second++) {
            gameTimer.tick(1.0);
            
            if (second % speedIncreaseInterval == 0) {
                double oldSpeed = gameTimer.getSpeedFactor();
                gameTimer.increaseSpeed();
                if (gameTimer.getSpeedFactor() > oldSpeed) {
                    System.out.println("  [" + String.format("%02d", second) + "s] Speed increased to " + 
                                     String.format("%.1f", gameTimer.getSpeedFactor()) + "x");
                }
            }
            
            if (second % 10 == 0 || second == durationSeconds) {
                System.out.println("  [" + String.format("%02d", second) + "s] " + 
                                 gameTimer.getFormattedTime() + " - Level " + 
                                 gameTimer.getCurrentLevel() + " - " + 
                                 String.format("%.0f", gameTimer.getInterval()) + "ms interval");
            }
        }
        
        // Update the main timer with simulation results
        timer.tick(durationSeconds);
        timer.setSpeedFactor(gameTimer.getSpeedFactor());
        
        System.out.println("Simulation completed!");
        printTimerState(timer);
    }
}