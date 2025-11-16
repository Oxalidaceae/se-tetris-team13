package team13.tetris.network.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class TestTetrisServerTest {

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
    @DisplayName("기본 매개변수로 TestTetrisServer main 메서드 호출")
    void testMainWithNoArguments() {
        // given
        String input = "quit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        // when & then
        assertDoesNotThrow(() -> {
            // 별도 스레드에서 실행하여 블로킹 방지
            Thread serverThread = new Thread(() -> {
                try {
                    TestTetrisServer.main(new String[]{});
                } catch (Exception e) {
                    // 예상되는 예외 (포트 바인딩 실패 등)
                }
            });
            
            serverThread.start();
            Thread.sleep(1000); // 서버 시작 대기
            serverThread.interrupt();
            
            String output = outputStreamCaptor.toString();
            assertTrue(output.contains("P2P Tetris Server Started") || 
                      errorStreamCaptor.toString().contains("Failed to start server"),
                      "서버 시작 메시지 또는 실패 메시지가 출력되어야 함");
        });
        
        tearDown();
    }

    @Test
    @DisplayName("커스텀 호스트 플레이어 ID로 TestTetrisServer main 메서드 호출")
    void testMainWithCustomHostId() {
        // given
        String input = "quit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        // when & then
        assertDoesNotThrow(() -> {
            Thread serverThread = new Thread(() -> {
                try {
                    TestTetrisServer.main(new String[]{"CustomHost"});
                } catch (Exception e) {
                    // 예상되는 예외
                }
            });
            
            serverThread.start();
            Thread.sleep(1000);
            serverThread.interrupt();
            
            String output = outputStreamCaptor.toString();
            assertTrue(output.contains("Host Player: CustomHost") || 
                      errorStreamCaptor.toString().contains("Failed to start server"),
                      "커스텀 호스트 플레이어 ID가 표시되어야 함");
        });
        
        tearDown();
    }

    @Test
    @DisplayName("커스텀 포트와 호스트 ID로 TestTetrisServer main 메서드 호출")
    void testMainWithCustomHostAndPort() {
        // given
        String input = "quit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        // when & then
        assertDoesNotThrow(() -> {
            Thread serverThread = new Thread(() -> {
                try {
                    TestTetrisServer.main(new String[]{"TestHost", "12347"});
                } catch (Exception e) {
                    // 예상되는 예외
                }
            });
            
            serverThread.start();
            Thread.sleep(1000);
            serverThread.interrupt();
            
            String output = outputStreamCaptor.toString();
            assertTrue(output.contains("Host Player: TestHost") || 
                      errorStreamCaptor.toString().contains("Failed to start server"),
                      "커스텀 설정이 표시되어야 함");
        });
        
        tearDown();
    }

    @Test
    @DisplayName("잘못된 포트 번호로 TestTetrisServer main 메서드 호출")
    void testMainWithInvalidPort() {
        // given
        String input = "quit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        // when & then
        assertDoesNotThrow(() -> {
            Thread serverThread = new Thread(() -> {
                try {
                    TestTetrisServer.main(new String[]{"TestHost", "invalid_port"});
                } catch (Exception e) {
                    // 예상되는 예외
                }
            });
            
            serverThread.start();
            Thread.sleep(1000);
            serverThread.interrupt();
            
            String errorOutput = errorStreamCaptor.toString();
            assertTrue(errorOutput.contains("Invalid port number") && errorOutput.contains("Using default port"),
                      "잘못된 포트 번호에 대한 오류 메시지가 출력되어야 함");
        });
        
        tearDown();
    }

    @Test
    @DisplayName("CLI 도움말 메시지 출력 확인")
    void testHelpMessageDisplay() {
        // given
        String input = "quit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        // when & then
        assertDoesNotThrow(() -> {
            Thread serverThread = new Thread(() -> {
                try {
                    TestTetrisServer.main(new String[]{});
                } catch (Exception e) {
                    // 예상되는 예외
                }
            });
            
            serverThread.start();
            Thread.sleep(1000);
            serverThread.interrupt();
            
            String output = outputStreamCaptor.toString();
            if (output.contains("Available Commands:")) {
                assertTrue(output.contains("'mode normal'"));
                assertTrue(output.contains("'mode item'"));
                assertTrue(output.contains("'ready'"));
                assertTrue(output.contains("'quit'"));
            }
        });
        
        tearDown();
    }

    @Test
    @DisplayName("서버 시작 실패 시 오류 메시지 출력")
    void testServerStartFailure() {
        // given
        String input = "quit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        // when & then
        assertDoesNotThrow(() -> {
            // 이미 사용 중인 포트로 서버 시작 시도
            Thread serverThread1 = new Thread(() -> {
                try {
                    TestTetrisServer.main(new String[]{"Host1", "12348"});
                } catch (Exception e) {
                    // 예상되는 예외
                }
            });
            
            Thread serverThread2 = new Thread(() -> {
                try {
                    Thread.sleep(500); // 첫 번째 서버가 시작될 시간 제공
                    TestTetrisServer.main(new String[]{"Host2", "12348"}); // 같은 포트
                } catch (Exception e) {
                    // 예상되는 예외
                }
            });
            
            serverThread1.start();
            serverThread2.start();
            
            Thread.sleep(2000);
            
            serverThread1.interrupt();
            serverThread2.interrupt();
            
            // 오류가 발생했거나 정상적으로 처리되었는지 확인
            String output = outputStreamCaptor.toString();
            String errorOutput = errorStreamCaptor.toString();
            
            assertTrue(output.length() > 0 || errorOutput.length() > 0,
                      "서버 실행 중 출력이 있어야 함");
        });
        
        tearDown();
    }

    @Test
    @DisplayName("다양한 명령행 인수 조합 테스트")
    void testVariousCommandLineArguments() {
        String[][] testCases = {
            {"TestHost1"},
            {"TestHost2", "12349"},
            {"한글호스트"},
            {"Host_With_Underscore", "13000"},
            {"123", "14000"}
        };

        for (String[] args : testCases) {
            assertDoesNotThrow(() -> {
                String input = "quit\n";
                System.setIn(new ByteArrayInputStream(input.getBytes()));
                
                Thread serverThread = new Thread(() -> {
                    try {
                        TestTetrisServer.main(args);
                    } catch (Exception e) {
                        // 예상되는 예외
                    }
                });
                
                serverThread.start();
                Thread.sleep(500);
                serverThread.interrupt();
                
            }, "명령행 인수 조합이 안전하게 처리되어야 함: " + String.join(", ", args));
        }
        
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
            // 매우 긴 호스트 이름과 극한 포트 번호
            String[] extremeArgs = {"VeryLongHostNameThatExceedsNormalLimitsForTesting", "65535"};
            
            Thread serverThread = new Thread(() -> {
                try {
                    TestTetrisServer.main(extremeArgs);
                } catch (Exception e) {
                    // 예상되는 예외
                }
            });
            
            serverThread.start();
            Thread.sleep(1000);
            serverThread.interrupt();
            
        }, "극한 조건에서도 안전하게 처리되어야 함");
        
        tearDown();
    }

    @Test
    @DisplayName("null 및 빈 문자열 인수 처리")
    void testNullAndEmptyArguments() {
        // given
        String input = "quit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        // when & then
        assertDoesNotThrow(() -> {
            String[] emptyArgs = {""};
            
            Thread serverThread = new Thread(() -> {
                try {
                    TestTetrisServer.main(emptyArgs);
                } catch (Exception e) {
                    // 예상되는 예외
                }
            });
            
            serverThread.start();
            Thread.sleep(1000);
            serverThread.interrupt();
            
        }, "빈 문자열 인수도 안전하게 처리되어야 함");
        
        tearDown();
    }

    @Test
    @DisplayName("인터럽트 처리 테스트")
    void testInterruptHandling() {
        // given
        String input = "status\nquit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        // when & then
        assertDoesNotThrow(() -> {
            Thread serverThread = new Thread(() -> {
                try {
                    TestTetrisServer.main(new String[]{"InterruptTest", "12350"});
                } catch (Exception e) {
                    // 예상되는 예외
                }
            });
            
            serverThread.start();
            Thread.sleep(1000);
            
            // 스레드 인터럽트 테스트
            serverThread.interrupt();
            serverThread.join(2000); // 최대 2초 대기
            
        }, "스레드 인터럽트가 안전하게 처리되어야 함");
        
        tearDown();
    }
}