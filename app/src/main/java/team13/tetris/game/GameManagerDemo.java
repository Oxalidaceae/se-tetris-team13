package team13.tetris.game;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

/**
 * Demo application for testing GameManager functionality
 */
public class GameManagerDemo extends Application {
    
    private GameManager gameManager;
    private Label stateLabel;
    private Label scoreLabel;
    private Label linesLabel;
    private Label timeLabel;
    private Label levelLabel;
    private Label runningLabel;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("GameManager Demo");
        
        // Initialize GameManager
        gameManager = new GameManager(primaryStage);
        
        // Create UI
        VBox root = createUI();
        
        // Setup update timer to refresh UI
        Timeline updateTimer = new Timeline(new KeyFrame(Duration.millis(100), e -> updateUI()));
        updateTimer.setCycleCount(Timeline.INDEFINITE);
        updateTimer.play();
        
        Scene scene = new Scene(root, 500, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Initial UI update
        updateUI();
    }
    
    private VBox createUI() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        
        // Title
        Label title = new Label("GameManager Demo");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        // Status display
        VBox statusBox = new VBox(5);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusBox.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-padding: 10;");
        
        stateLabel = new Label("State: MENU");
        runningLabel = new Label("Running: false");
        scoreLabel = new Label("Score: 0");
        linesLabel = new Label("Lines Cleared: 0");
        timeLabel = new Label("Time: 00:00");
        levelLabel = new Label("Level: 1");
        
        statusBox.getChildren().addAll(
            new Label("Game Status:"),
            stateLabel, runningLabel, scoreLabel, linesLabel, timeLabel, levelLabel
        );
        
        // Control buttons
        VBox controlBox = new VBox(10);
        controlBox.setAlignment(Pos.CENTER);
        
        // Game control buttons
        HBox gameControls = new HBox(10);
        gameControls.setAlignment(Pos.CENTER);
        
        Button startBtn = new Button("Start Game");
        startBtn.setOnAction(e -> {
            gameManager.startGame();
            System.out.println("Game started!");
        });
        
        Button pauseBtn = new Button("Toggle Pause");
        pauseBtn.setOnAction(e -> {
            gameManager.togglePause();
            System.out.println("Pause toggled!");
        });
        
        Button endBtn = new Button("End Game");
        endBtn.setOnAction(e -> {
            gameManager.endGame();
            System.out.println("Game ended!");
        });
        
        gameControls.getChildren().addAll(startBtn, pauseBtn, endBtn);
        
        // Score testing buttons
        HBox scoreControls = new HBox(10);
        scoreControls.setAlignment(Pos.CENTER);
        
        Button add100Btn = new Button("+100 Points");
        add100Btn.setOnAction(e -> {
            gameManager.addScore(100);
            System.out.println("Added 100 points!");
        });
        
        Button add500Btn = new Button("+500 Points");
        add500Btn.setOnAction(e -> {
            gameManager.addScore(500);
            System.out.println("Added 500 points!");
        });
        
        scoreControls.getChildren().addAll(add100Btn, add500Btn);
        
        // Line clear testing buttons
        HBox lineControls = new HBox(10);
        lineControls.setAlignment(Pos.CENTER);
        
        Button single = new Button("Single (1 line)");
        single.setOnAction(e -> {
            gameManager.linesCleared(1);
            System.out.println("Single line cleared!");
        });
        
        Button double_ = new Button("Double (2 lines)");
        double_.setOnAction(e -> {
            gameManager.linesCleared(2);
            System.out.println("Double line cleared!");
        });
        
        Button triple = new Button("Triple (3 lines)");
        triple.setOnAction(e -> {
            gameManager.linesCleared(3);
            System.out.println("Triple line cleared!");
        });
        
        Button tetris = new Button("TETRIS! (4 lines)");
        tetris.setOnAction(e -> {
            gameManager.linesCleared(4);
            System.out.println("TETRIS! 4 lines cleared!");
        });
        
        lineControls.getChildren().addAll(single, double_, triple, tetris);
        
        // Timer testing buttons
        HBox timerControls = new HBox(10);
        timerControls.setAlignment(Pos.CENTER);
        
        Button tickBtn = new Button("+5 seconds");
        tickBtn.setOnAction(e -> {
            gameManager.getGameTimer().tick(5.0);
            System.out.println("Added 5 seconds to timer!");
        });
        
        Button speedBtn = new Button("Increase Speed");
        speedBtn.setOnAction(e -> {
            gameManager.getGameTimer().increaseSpeed();
            System.out.println("Speed increased!");
        });
        
        Button resetTimerBtn = new Button("Reset Timer");
        resetTimerBtn.setOnAction(e -> {
            gameManager.getGameTimer().reset();
            System.out.println("Timer reset!");
        });
        
        timerControls.getChildren().addAll(tickBtn, speedBtn, resetTimerBtn);
        
        // Advanced testing buttons
        HBox advancedControls = new HBox(10);
        advancedControls.setAlignment(Pos.CENTER);
        
        Button simulateBtn = new Button("Simulate Game");
        simulateBtn.setOnAction(e -> simulateGameplay());
        
        Button showScoreBtn = new Button("Show Scores");
        showScoreBtn.setOnAction(e -> gameManager.showGameOverScene());
        
        advancedControls.getChildren().addAll(simulateBtn, showScoreBtn);
        
        // Assemble control box
        controlBox.getChildren().addAll(
            new Label("Game Controls:"),
            gameControls,
            new Label("Score Testing:"),
            scoreControls,
            new Label("Line Clear Testing:"),
            lineControls,
            new Label("Timer Testing:"),
            timerControls,
            new Label("Advanced Testing:"),
            advancedControls
        );
        
        root.getChildren().addAll(title, statusBox, controlBox);
        
        return root;
    }
    
    private void updateUI() {
        if (gameManager != null) {
            stateLabel.setText("State: " + gameManager.getState());
            runningLabel.setText("Running: " + gameManager.isGameRunning());
            scoreLabel.setText("Score: " + gameManager.getCurrentScore());
            linesLabel.setText("Lines Cleared: " + gameManager.getLinesCleared());
            timeLabel.setText("Time: " + gameManager.getGameTimer().getFormattedTime() + 
                            " (" + String.format("%.1f", gameManager.getGameTimer().getElapsedTime()) + "s)");
            levelLabel.setText("Speed Level: " + gameManager.getGameTimer().getSpeedLevel() + 
                            " (Speed: " + String.format("%.1f", gameManager.getGameTimer().getSpeedFactor()) + "x)");
        }
    }
    
    private void simulateGameplay() {
        System.out.println("=== Starting Gameplay Simulation ===");
        
        // Start game
        gameManager.startGame();
        System.out.println("Game started");
        
        // Simulate some time passing
        gameManager.getGameTimer().tick(30.0);
        System.out.println("30 seconds passed");
        
        // Simulate scoring
        gameManager.addScore(200);
        System.out.println("Added 200 bonus points");
        
        // Simulate line clears
        gameManager.linesCleared(1); // Single
        gameManager.linesCleared(2); // Double  
        gameManager.linesCleared(4); // Tetris!
        gameManager.linesCleared(3); // Triple
        System.out.println("Cleared various lines");
        
        // Increase speed a few times
        for (int i = 0; i < 5; i++) {
            gameManager.getGameTimer().increaseSpeed();
        }
        System.out.println("Increased speed 5 times");
        
        // Add more time
        gameManager.getGameTimer().tick(45.0);
        System.out.println("45 more seconds passed");
        
        // Final scoring burst
        gameManager.linesCleared(4); // Another Tetris!
        gameManager.addScore(1000);
        System.out.println("Final scoring burst");
        
        System.out.println("=== Simulation Complete ===");
        System.out.println("Final Score: " + gameManager.getCurrentScore());
        System.out.println("Total Lines: " + gameManager.getLinesCleared());
        System.out.println("Final Time: " + gameManager.getGameTimer().getFormattedTime());
        System.out.println("Final Speed Level: " + gameManager.getGameTimer().getSpeedLevel());
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}