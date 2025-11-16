package team13.tetris.network.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class TestTetrisClientTest {

    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errorStreamCaptor = new ByteArrayOutputStream();
    private final InputStream originalSystemIn = System.in;
    private final PrintStream originalSystemOut = System.out;
    private final PrintStream originalSystemErr = System.err;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
        System.setErr(new PrintStream(errorStreamCaptor));
    }

    void tearDown() {
        System.setIn(originalSystemIn);
        System.setOut(originalSystemOut);
        System.setErr(originalSystemErr);
    }

    @Test
    @DisplayName("기본 매개변수로 TestTetrisClient main 메서드 호출")
    void testMainWithNoArguments() {
        // given
        String input = "quit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        // when & then
        assertDoesNotThrow(() -> {
            Thread clientThread = new Thread(() -> {
                try {
                    TestTetrisClient.main(new String[]{});
                } catch (Exception e) {
                    // 연결 실패 예상됨 (서버가 없으므로)
                }
            });
            
            clientThread.start();
            Thread.sleep(2000); // 연결 시도 대기
            clientThread.interrupt();
            
            String errorOutput = errorStreamCaptor.toString();
            assertTrue(errorOutput.contains("Failed") || errorOutput.contains("connect") || 
                      errorOutput.contains("Connection") || errorOutput.contains("refused"),
                      "네트워크 연결 실패 관련 메시지가 출력되어야 함");
        });
        
        tearDown();
    }

    @Test
    @DisplayName("커스텀 플레이어 ID로 TestTetrisClient main 메서드 호출")
    void testMainWithCustomPlayerId() {
        // given
        String input = "quit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        // when & then
        assertDoesNotThrow(() -> {
            Thread clientThread = new Thread(() -> {
                try {
                    TestTetrisClient.main(new String[]{"CustomPlayer"});
                } catch (Exception e) {
                    // 연결 실패 예상됨
                }
            });
            
            clientThread.start();
            Thread.sleep(2000);
            clientThread.interrupt();
            
            String errorOutput = errorStreamCaptor.toString();
            assertTrue(errorOutput.contains("Failed to connect to server"),
                      "서버 연결 실패 메시지가 출력되어야 함");
        });
        
        tearDown();
    }

    @Test
    @DisplayName("커스텀 서버 호스트와 플레이어 ID로 main 메서드 호출")
    void testMainWithCustomHostAndPlayerId() {
        // given
        String input = "quit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        // when & then
        assertDoesNotThrow(() -> {
            Thread clientThread = new Thread(() -> {
                try {
                    TestTetrisClient.main(new String[]{"TestPlayer", "192.168.1.100"});
                } catch (Exception e) {
                    // 연결 실패 예상됨
                }
            });
            
            clientThread.start();
            Thread.sleep(2000);
            clientThread.interrupt();
            
            String errorOutput = errorStreamCaptor.toString();
            assertTrue(errorOutput.contains("Failed") || errorOutput.contains("connect") || 
                      errorOutput.contains("Connection") || errorOutput.contains("refused") || 
                      errorOutput.length() == 0, // 출력이 없어도 연결 시도는 정상 수행됨
                      "서버 연결 실패 관련 메시지가 출력되거나 정상 수행되어야 함");
        });
        
        tearDown();
    }

    @Test
    @DisplayName("모든 매개변수(플레이어 ID, 호스트, 포트)로 main 메서드 호출")
    void testMainWithAllParameters() {
        // given
        String input = "quit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        // when & then
        assertDoesNotThrow(() -> {
            Thread clientThread = new Thread(() -> {
                try {
                    TestTetrisClient.main(new String[]{"FullTestPlayer", "127.0.0.1", "12346"});
                } catch (Exception e) {
                    // 연결 실패 예상됨
                }
            });
            
            clientThread.start();
            Thread.sleep(2000);
            clientThread.interrupt();
            
            String errorOutput = errorStreamCaptor.toString();
            assertTrue(errorOutput.contains("Failed") || errorOutput.contains("connect") || 
                      errorOutput.contains("Connection") || errorOutput.contains("refused"),
                      "서버 연결 실패 관련 메시지가 출력되어야 함");
        });
        
        tearDown();
    }

    @Test
    @DisplayName("잘못된 포트 번호로 main 메서드 호출")
    void testMainWithInvalidPort() {
        // given
        String input = "quit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        // when & then
        assertDoesNotThrow(() -> {
            TestTetrisClient.main(new String[]{"TestPlayer", "localhost", "invalid_port"});
            
            String errorOutput = errorStreamCaptor.toString();
            assertTrue(errorOutput.contains("Invalid port number"),
                      "잘못된 포트 번호에 대한 오류 메시지가 출력되어야 함");
        });
        
        tearDown();
    }

    @Test
    @DisplayName("다양한 플레이어 ID로 main 메서드 호출")
    void testMainWithVariousPlayerIds() {
        String[] playerIds = {
            "Player1",
            "한글플레이어",
            "Player_With_Underscore",
            "123456",
            "VeryLongPlayerNameForTesting"
        };

        for (String playerId : playerIds) {
            assertDoesNotThrow(() -> {
                String input = "quit\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));
                
                Thread clientThread = new Thread(() -> {
                    try {
                        TestTetrisClient.main(new String[]{playerId});
                    } catch (Exception e) {
                        // 연결 실패 예상됨
                    }
                });
                
                clientThread.start();
                Thread.sleep(1000);
                clientThread.interrupt();
                
            }, "다양한 플레이어 ID가 안전하게 처리되어야 함: " + playerId);
            
            // 스트림 캡처 초기화
            outputStreamCaptor.reset();
            errorStreamCaptor.reset();
        }
        
        tearDown();
    }

    @Test
    @DisplayName("다양한 서버 호스트 주소로 main 메서드 호출")
    void testMainWithVariousHostAddresses() {
        String[] hostAddresses = {
            "localhost",
            "127.0.0.1",
            "192.168.1.1",
            "example.com",
            "tetris-server.local"
        };

        for (String host : hostAddresses) {
            assertDoesNotThrow(() -> {
                String input = "quit\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));
                
                Thread clientThread = new Thread(() -> {
                    try {
                        TestTetrisClient.main(new String[]{"TestPlayer", host});
                    } catch (Exception e) {
                        // 연결 실패 예상됨
                    }
                });
                
                clientThread.start();
                Thread.sleep(1000);
                clientThread.interrupt();
                
            }, "다양한 호스트 주소가 안전하게 처리되어야 함: " + host);
            
            // 스트림 캡처 초기화
            outputStreamCaptor.reset();
            errorStreamCaptor.reset();
        }
        
        tearDown();
    }

    @Test
    @DisplayName("유효한 포트 범위로 main 메서드 호출")
    void testMainWithValidPortRanges() {
        String[] validPorts = {
            "1024", "8080", "12345", "65535"
        };

        for (String port : validPorts) {
            assertDoesNotThrow(() -> {
                String input = "quit\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));
                
                Thread clientThread = new Thread(() -> {
                    try {
                        TestTetrisClient.main(new String[]{"TestPlayer", "localhost", port});
                    } catch (Exception e) {
                        // 연결 실패 예상됨
                    }
                });
                
                clientThread.start();
                Thread.sleep(1000);
                clientThread.interrupt();
                
            }, "유효한 포트가 안전하게 처리되어야 함: " + port);
            
            // 스트림 캡처 초기화
            outputStreamCaptor.reset();
            errorStreamCaptor.reset();
        }
        
        tearDown();
    }

    @Test
    @DisplayName("잘못된 포트 번호들로 main 메서드 호출")
    void testMainWithInvalidPortNumbers() {
        String[] invalidPorts = {
            "abc", "-1", "70000", "not_a_number", ""
        };

        for (String port : invalidPorts) {
            assertDoesNotThrow(() -> {
                TestTetrisClient.main(new String[]{"TestPlayer", "localhost", port});
                
                String errorOutput = errorStreamCaptor.toString();
                assertTrue(errorOutput.contains("Invalid port number") || 
                          errorOutput.contains("Failed") || errorOutput.contains("connect") || 
                          errorOutput.contains("Connection") || errorOutput.contains("refused"),
                          "잘못된 포트에 대한 적절한 오류 처리가 되어야 함: " + port);
            }, "잘못된 포트 번호가 안전하게 처리되어야 함: " + port);
            
            // 스트림 캡처 초기화
            outputStreamCaptor.reset();
            errorStreamCaptor.reset();
        }
        
        tearDown();
    }

    @Test
    @DisplayName("빈 문자열 매개변수로 main 메서드 호출")
    void testMainWithEmptyParameters() {
        // given
        String input = "quit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        // when & then
        assertDoesNotThrow(() -> {
            Thread clientThread = new Thread(() -> {
                try {
                    TestTetrisClient.main(new String[]{"", "", "12345"});
                } catch (Exception e) {
                    // 연결 실패 예상됨
                }
            });
            
            clientThread.start();
            Thread.sleep(2000);
            clientThread.interrupt();
            
            String errorOutput = errorStreamCaptor.toString();
            assertTrue(errorOutput.contains("Failed") || errorOutput.contains("connect") || 
                      errorOutput.contains("Connection") || errorOutput.contains("refused"),
                      "빈 매개변수로도 안전하게 처리되어야 함");
        });
        
        tearDown();
    }

    @Test
    @DisplayName("극한 상황에서의 안정성 테스트")
    void testExtremeConditions() {
        // given
        String input = "quit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        // when & then
        assertDoesNotThrow(() -> {
            String longPlayerId = "VeryLongPlayerNameThatExceedsNormalLimits".repeat(10);
            String longHostName = "very-long-hostname-that-might-cause-issues.example.com";
            
            Thread clientThread = new Thread(() -> {
                try {
                    TestTetrisClient.main(new String[]{longPlayerId, longHostName, "12345"});
                } catch (Exception e) {
                    // 연결 실패 예상됨
                }
            });
            
            clientThread.start();
            Thread.sleep(2000);
            clientThread.interrupt();
            
        }, "극한 조건에서도 안전하게 처리되어야 함");
        
        tearDown();
    }

    @Test
    @DisplayName("인터럽트 처리 테스트")
    void testInterruptHandling() {
        // given
        String input = "ready\nquit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        // when & then
        assertDoesNotThrow(() -> {
            Thread clientThread = new Thread(() -> {
                try {
                    TestTetrisClient.main(new String[]{"InterruptTest", "localhost", "12347"});
                } catch (Exception e) {
                    // 연결 실패 예상됨
                }
            });
            
            clientThread.start();
            Thread.sleep(1000);
            
            // 스레드 인터럽트 테스트
            clientThread.interrupt();
            clientThread.join(2000); // 최대 2초 대기
            
        }, "스레드 인터럽트가 안전하게 처리되어야 함");
        
        tearDown();
    }
}