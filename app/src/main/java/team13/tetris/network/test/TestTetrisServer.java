package team13.tetris.network.test;

import team13.tetris.network.server.TetrisServer;
import team13.tetris.network.protocol.GameModeMessage;

/**
 * 테스트용 Tetris 서버 CLI
 * 네트워크 기능을 테스트하기 위한 간단한 명령줄 인터페이스
 */
public class TestTetrisServer {
    private static final int DEFAULT_PORT = 12345;
    
    public static void main(String[] args) {
        String hostPlayerId = "HostPlayer";
        int port = DEFAULT_PORT;
        
        // 명령행 인수 처리: [hostPlayerId] [port]
        if (args.length > 0) {
            hostPlayerId = args[0];
        }
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number: " + args[1]);
                System.err.println("Using default port: " + DEFAULT_PORT);
            }
        }
        
        TetrisServer server = new TetrisServer(hostPlayerId, port);
        
        // 테스트용 메시지 리스너 설정
        server.setHostMessageListener(new TestServerMessageListener());
        
        // 서버 시작
        try {
            server.start();
            
            System.out.println("P2P Tetris Server Started (Host Mode)!");
            System.out.println("Host Player: " + hostPlayerId);
            System.out.println("Server IP: " + server.getServerIP());
            System.out.println("Port: " + port);
            System.out.println("\n Available Commands:");
            System.out.println("  'mode normal' - Select normal game mode");
            System.out.println("  'mode item'   - Select item game mode");
            System.out.println("  'ready'       - Set host ready");
            System.out.println("  'reset'       - Reset ready states");
            System.out.println("  'status'      - Show current status");
            System.out.println("  'move L/R'    - Send move left/right as host");
            System.out.println("  'rotate'      - Send rotate as host");
            System.out.println("  'drop'        - Send hard drop as host");
            System.out.println("  'pause'       - Pause game as host");
            System.out.println("  'resume'      - Resume game as host");
            System.out.println("  'quit'        - Stop server");
            System.out.println("----------------------------------------");
            
            // Ctrl+C로 서버 종료할 수 있도록 셧다운 훅 추가
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
            
            // 인터랙티브 명령 처리
            java.util.Scanner scanner = new java.util.Scanner(System.in);
            
            while (server.isRunning()) {
                System.out.print("Server> ");
                if (scanner.hasNextLine()) {
                    String input = scanner.nextLine().trim();
                    
                    if (input.equalsIgnoreCase("quit")) {
                        System.out.println("Shutting down server...");
                        server.stop();
                        break;
                    } else if (input.equalsIgnoreCase("ready")) {
                        server.setHostReady();
                        System.out.println("Host ready signal sent!");
                    } else if (input.equalsIgnoreCase("reset")) {
                        server.resetReadyStates();
                        System.out.println("Ready states reset");
                    } else if (input.equalsIgnoreCase("status")) {
                        System.out.println("Server Status:");
                        System.out.println("  - IP: " + server.getServerIP());
                        System.out.println("  - Connected clients: " + server.getClientCount());
                        System.out.println("  - Game mode: " + (server.getSelectedGameMode() != null ? server.getSelectedGameMode() : "Not selected"));
                        System.out.println("  - Server ready: " + (server.isServerReady() ? "✅" : "❌"));
                        System.out.println("  - Client ready: " + (server.isClientReady() ? "✅" : "❌"));
                    } else if (input.startsWith("mode ")) {
                        String mode = input.substring(5).trim().toUpperCase();
                        try {
                            GameModeMessage.GameMode gameMode = GameModeMessage.GameMode.valueOf(mode);
                            server.selectGameMode(gameMode);
                            System.out.println("Game mode selected: " + gameMode);
                        } catch (IllegalArgumentException e) {
                            System.out.println("Invalid game mode. Use 'normal' or 'item'");
                        }
                    } else if (input.startsWith("move ")) {
                        String direction = input.substring(5).trim().toUpperCase();
                        if (direction.equals("L") || direction.equals("LEFT")) {
                            if (server.sendHostMoveLeft()) {
                                System.out.println("Host move left sent");
                            } else {
                                System.out.println("Game not in progress");
                            }
                        } else if (direction.equals("R") || direction.equals("RIGHT")) {
                            if (server.sendHostMoveRight()) {
                                System.out.println("Host move right sent");
                            } else {
                                System.out.println("Game not in progress");
                            }
                        } else {
                            System.out.println("Invalid direction. Use 'L' or 'R'");
                        }
                    } else if (input.equalsIgnoreCase("rotate")) {
                        if (server.sendHostRotate()) {
                            System.out.println("Host rotate sent");
                        } else {
                            System.out.println("Game not in progress");
                        }
                    } else if (input.equalsIgnoreCase("drop")) {
                        if (server.sendHostHardDrop()) {
                            System.out.println("Host hard drop sent");
                        } else {
                            System.out.println("Game not in progress");
                        }
                    } else if (input.equalsIgnoreCase("pause")) {
                        if (server.pauseGameAsHost()) {
                            System.out.println("⏸Game paused by host");
                        } else {
                            System.out.println("Failed to pause game");
                        }
                    } else if (input.equalsIgnoreCase("resume")) {
                        if (server.resumeGameAsHost()) {
                            System.out.println("▶Game resumed by host");
                        } else {
                            System.out.println("Failed to resume game");
                        }
                    } else if (!input.isEmpty()) {
                        System.out.println("Unknown command: " + input);
                    }
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            
            scanner.close();
            
        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
