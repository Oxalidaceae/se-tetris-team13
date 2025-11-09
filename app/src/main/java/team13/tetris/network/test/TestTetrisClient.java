package team13.tetris.network.test;

import team13.tetris.network.client.TetrisClient;

/**
 * 테스트용 Tetris 클라이언트 CLI
 * 네트워크 기능을 테스트하기 위한 간단한 명령줄 인터페이스
 */
public class TestTetrisClient {
    private static final int DEFAULT_PORT = 12345;
    private static final String DEFAULT_HOST = "localhost";
    
    public static void main(String[] args) {
        String playerId = "TestPlayer";
        String serverHost = DEFAULT_HOST;
        int serverPort = DEFAULT_PORT;
        
        // 명령행 인수 처리: [playerId] [serverHost] [serverPort]
        if (args.length > 0) {
            playerId = args[0];
        }
        if (args.length > 1) {
            serverHost = args[1];
        }
        if (args.length > 2) {
            try {
                serverPort = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                System.err.println("❌ Invalid port number: " + args[2]);
                return;
            }
        }
        
        TetrisClient client = new TetrisClient(playerId, serverHost, serverPort);
        
        // 테스트용 메시지 리스너 설정
        client.setMessageListener(new TestClientMessageListener());
        
        // 서버 접속 시도
        if (client.connect()) {
            System.out.println("🎮 P2P Tetris Client Connected!");
            System.out.println("📍 Connected to: " + serverHost + ":" + serverPort);
            System.out.println("👤 Player ID: " + playerId);
            System.out.println("\n📋 Available Commands:");
            System.out.println("  'ready'     - Mark yourself as ready to start");
            System.out.println("  'move L'    - Send move left");
            System.out.println("  'move R'    - Send move right");
            System.out.println("  'rotate'    - Send rotate");
            System.out.println("  'drop'      - Send hard drop");
            System.out.println("  'pause'     - Pause game");
            System.out.println("  'resume'    - Resume game");
            System.out.println("  'quit'      - Disconnect");
            System.out.println("----------------------------------------");
            
            // 종료 시 정리 작업
            Runtime.getRuntime().addShutdownHook(new Thread(client::disconnect));
            
            // 인터랙티브 명령 처리
            java.util.Scanner scanner = new java.util.Scanner(System.in);
            
            while (client.isConnected()) {
                System.out.print("Client> ");
                if (scanner.hasNextLine()) {
                    String input = scanner.nextLine().trim();
                    
                    if (input.equalsIgnoreCase("quit")) {
                        System.out.println("👋 Disconnecting from server...");
                        client.disconnect();
                        break;
                    } else if (input.equalsIgnoreCase("ready")) {
                        if (client.requestGameStart()) {
                            System.out.println("✅ Ready signal sent! Waiting for other players...");
                        } else {
                            System.out.println("❌ Failed to send ready signal");
                        }
                    } else if (input.equalsIgnoreCase("pause")) {
                        if (client.pauseGame()) {
                            System.out.println("⏸️ Pause request sent");
                        }
                    } else if (input.equalsIgnoreCase("resume")) {
                        if (client.resumeGame()) {
                            System.out.println("▶️ Resume request sent");
                        }
                    } else if (input.startsWith("move ")) {
                        String direction = input.substring(5).trim().toUpperCase();
                        if (direction.equals("L") || direction.equals("LEFT")) {
                            client.sendMoveLeft();
                            System.out.println("⬅️ Move left sent");
                        } else if (direction.equals("R") || direction.equals("RIGHT")) {
                            client.sendMoveRight();
                            System.out.println("➡️ Move right sent");
                        } else {
                            System.out.println("❌ Invalid direction. Use 'L' or 'R'");
                        }
                    } else if (input.equalsIgnoreCase("rotate")) {
                        client.sendRotate();
                        System.out.println("🔄 Rotate sent");
                    } else if (input.equalsIgnoreCase("drop")) {
                        client.sendHardDrop();
                        System.out.println("⬇️ Hard drop sent");
                    } else if (!input.isEmpty()) {
                        System.out.println("❓ Unknown command: " + input);
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
        } else {
            System.err.println("❌ Failed to connect to server");
        }
    }
}
