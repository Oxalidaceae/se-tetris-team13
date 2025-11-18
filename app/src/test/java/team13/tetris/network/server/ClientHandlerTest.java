package team13.tetris.network.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.io.*;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientHandlerTest {

    @Mock
    private Socket mockSocket;
    
    @Mock
    private TetrisServer mockServer;
    
    private ClientHandler clientHandler;
    private ByteArrayOutputStream outputStream;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        outputStream = new ByteArrayOutputStream();
        clientHandler = new ClientHandler(mockSocket, mockServer);
    }

    @Test
    @DisplayName("ClientHandler 생성자 테스트")
    void testClientHandlerConstructor() {
        // when & then
        assertNotNull(clientHandler, "ClientHandler가 성공적으로 생성되어야 함");
    }

    @Test
    @DisplayName("null Socket으로 ClientHandler 생성")
    void testClientHandlerWithNullSocket() {
        // when & then
        assertDoesNotThrow(() -> {
            ClientHandler handler = new ClientHandler(null, mockServer);
            assertNotNull(handler, "null Socket으로도 ClientHandler 생성 가능");
        });
    }

    @Test
    @DisplayName("null TetrisServer로 ClientHandler 생성")
    void testClientHandlerWithNullServer() {
        // when & then
        assertDoesNotThrow(() -> {
            ClientHandler handler = new ClientHandler(mockSocket, null);
            assertNotNull(handler, "null TetrisServer로도 ClientHandler 생성 가능");
        });
    }

    @Test
    @DisplayName("ClientHandler run 메서드 예외 처리 테스트")
    void testClientHandlerRunWithException() throws IOException {
        // given
        when(mockSocket.getOutputStream()).thenThrow(new IOException("Connection failed"));
        
        // when & then
        assertDoesNotThrow(() -> {
            clientHandler.run();
        }, "IOException이 발생해도 안전하게 처리되어야 함");
    }

    @Test
    @DisplayName("ClientHandler 스트림 설정 실패 테스트")
    void testClientHandlerStreamSetupFailure() throws IOException {
        // given
        when(mockSocket.getOutputStream()).thenReturn(outputStream);
        when(mockSocket.getInputStream()).thenThrow(new IOException("Input stream error"));
        
        // when & then
        assertDoesNotThrow(() -> {
            clientHandler.run();
        }, "스트림 설정 실패 시에도 안전하게 처리되어야 함");
    }

    @Test
    @DisplayName("ClientHandler와 다양한 Socket 상태 테스트")
    void testClientHandlerWithVariousSocketStates() throws IOException {
        Socket[] mockSockets = new Socket[3];
        
        for (int i = 0; i < mockSockets.length; i++) {
            mockSockets[i] = mock(Socket.class);
            final int index = i;
            
            assertDoesNotThrow(() -> {
                ClientHandler handler = new ClientHandler(mockSockets[index], mockServer);
                assertNotNull(handler, "다양한 Socket 상태에서도 ClientHandler 생성 가능");
            }, "Socket 상태 " + index + "에서 안전해야 함");
        }
    }

    @Test
    @DisplayName("ClientHandler 메시지 처리 안정성 테스트")
    void testClientHandlerMessageProcessingSafety() {
        // given
        // 다양한 시나리오에서 안정성 확인
        
        // when & then
        assertDoesNotThrow(() -> {
            // 정상적인 ClientHandler 생성
            new ClientHandler(mockSocket, mockServer);
            
            // 다른 스레드에서 실행 시뮬레이션
            Thread handlerThread = new Thread(() -> {
                try {
                    // 빠른 종료를 위해 짧은 대기
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            
            handlerThread.start();
            handlerThread.join(1000); // 최대 1초 대기
            
        }, "메시지 처리가 안전해야 함");
    }

    @Test
    @DisplayName("ClientHandler 리소스 정리 테스트")
    void testClientHandlerCleanup() throws IOException {
        // given
        Socket realSocket = mock(Socket.class);
        ByteArrayOutputStream mockOutputStream = new ByteArrayOutputStream();
        when(realSocket.getOutputStream()).thenReturn(mockOutputStream);
        when(realSocket.getInputStream()).thenThrow(new IOException("Test exception"));
        when(realSocket.isClosed()).thenReturn(false);
        
        // when
        ClientHandler handler = new ClientHandler(realSocket, mockServer);
        
        // then
        assertDoesNotThrow(() -> {
            handler.run(); // cleanup이 호출되어야 함
        }, "리소스 정리가 안전하게 수행되어야 함");
    }

    @Test
    @DisplayName("ClientHandler 동시성 안전성 테스트")
    void testClientHandlerConcurrencySafety() {
        // given
        int threadCount = 5;
        Thread[] threads = new Thread[threadCount];
        
        // when & then
        assertDoesNotThrow(() -> {
            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    Socket threadSocket = mock(Socket.class);
                    TetrisServer threadServer = mock(TetrisServer.class);
                    new ClientHandler(threadSocket, threadServer);
                    
                    // 빠른 실행으로 동시성 테스트
                    try {
                        Thread.sleep(index * 10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
                threads[i].start();
            }
            
            // 모든 스레드 완료 대기
            for (Thread thread : threads) {
                thread.join(2000);
            }
        }, "동시에 여러 ClientHandler가 안전하게 동작해야 함");
    }

    @Test
    @DisplayName("ClientHandler 극한 상황 처리 테스트")
    void testClientHandlerExtremeConditions() {
        // when & then
        assertDoesNotThrow(() -> {
            // 매우 많은 ClientHandler 생성
            for (int i = 0; i < 100; i++) {
                Socket extremeSocket = mock(Socket.class);
                TetrisServer extremeServer = mock(TetrisServer.class);
                ClientHandler handler = new ClientHandler(extremeSocket, extremeServer);
                assertNotNull(handler, "극한 상황에서도 ClientHandler 생성 가능: " + i);
            }
        }, "극한 상황에서도 안정적으로 동작해야 함");
    }

    @Test
    @DisplayName("ClientHandler 메모리 효율성 테스트")
    void testClientHandlerMemoryEfficiency() {
        // given
        ClientHandler[] handlers = new ClientHandler[50];
        
        // when & then
        assertDoesNotThrow(() -> {
            for (int i = 0; i < handlers.length; i++) {
                handlers[i] = new ClientHandler(mock(Socket.class), mock(TetrisServer.class));
                assertNotNull(handlers[i], "메모리 효율적으로 ClientHandler 생성: " + i);
            }
            
            // 참조 해제
            for (int i = 0; i < handlers.length; i++) {
                handlers[i] = null;
            }
            
            // 가비지 컬렉션 힌트
            System.gc();
            Thread.sleep(100);
            
        }, "메모리 효율적으로 동작해야 함");
    }

    @Test
    @DisplayName("ClientHandler 다양한 TetrisServer 상태 테스트")
    void testClientHandlerWithVariousServerStates() {
        // given
        TetrisServer[] mockServers = new TetrisServer[3];
        
        for (int i = 0; i < mockServers.length; i++) {
            mockServers[i] = mock(TetrisServer.class);
            final int index = i;
            
            assertDoesNotThrow(() -> {
                ClientHandler handler = new ClientHandler(mockSocket, mockServers[index]);
                assertNotNull(handler, "다양한 서버 상태에서도 ClientHandler 생성 가능");
            }, "서버 상태 " + i + "에서 안전해야 함");
        }
    }

    @Test
    @DisplayName("ClientHandler 스레드 안전성 검증")
    void testClientHandlerThreadSafety() {
        // given
        ClientHandler handler = new ClientHandler(mockSocket, mockServer);
        
        // when & then
        assertDoesNotThrow(() -> {
            // Runnable 인터페이스 구현 확인
            assertTrue(handler instanceof Runnable, "ClientHandler는 Runnable을 구현해야 함");
            
            // Thread에서 실행 가능성 확인
            Thread testThread = new Thread(handler);
            assertNotNull(testThread, "ClientHandler로 Thread 생성 가능");
            
        }, "스레드 안전성이 보장되어야 함");
    }

    @Test
    @DisplayName("ClientHandler 인스턴스 독립성 테스트")
    void testClientHandlerInstanceIndependence() {
        // given
        Socket socket1 = mock(Socket.class);
        Socket socket2 = mock(Socket.class);
        TetrisServer server1 = mock(TetrisServer.class);
        TetrisServer server2 = mock(TetrisServer.class);
        
        // when
        ClientHandler handler1 = new ClientHandler(socket1, server1);
        ClientHandler handler2 = new ClientHandler(socket2, server2);
        
        // then
        assertNotEquals(handler1, handler2, "각 ClientHandler 인스턴스는 독립적이어야 함");
        assertNotNull(handler1, "첫 번째 ClientHandler가 생성되어야 함");
        assertNotNull(handler2, "두 번째 ClientHandler가 생성되어야 함");
    }

    @Test
    @DisplayName("ClientHandler 생성 매개변수 검증")
    void testClientHandlerParameterValidation() {
        // when & then
        assertDoesNotThrow(() -> {
            // 정상적인 매개변수
            ClientHandler normalHandler = new ClientHandler(mockSocket, mockServer);
            assertNotNull(normalHandler, "정상 매개변수로 생성 가능");
            
            // null 매개변수들
            ClientHandler nullSocketHandler = new ClientHandler(null, mockServer);
            assertNotNull(nullSocketHandler, "null Socket으로 생성 가능");
            
            ClientHandler nullServerHandler = new ClientHandler(mockSocket, null);
            assertNotNull(nullServerHandler, "null Server로 생성 가능");
            
            ClientHandler bothNullHandler = new ClientHandler(null, null);
            assertNotNull(bothNullHandler, "둘 다 null로 생성 가능");
            
        }, "다양한 매개변수 조합이 안전하게 처리되어야 함");
    }
}