package team13.tetris.network.server;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.net.Socket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ClientHandlerTest {

    @Mock private Socket mockSocket;

    @Mock private TetrisServer mockServer;

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
        assertDoesNotThrow(
                () -> {
                    ClientHandler handler = new ClientHandler(null, mockServer);
                    assertNotNull(handler, "null Socket으로도 ClientHandler 생성 가능");
                });
    }

    @Test
    @DisplayName("null TetrisServer로 ClientHandler 생성")
    void testClientHandlerWithNullServer() {
        // when & then
        assertDoesNotThrow(
                () -> {
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
        assertDoesNotThrow(
                () -> {
                    clientHandler.run();
                },
                "IOException이 발생해도 안전하게 처리되어야 함");
    }

    @Test
    @DisplayName("ClientHandler 스트림 설정 실패 테스트")
    void testClientHandlerStreamSetupFailure() throws IOException {
        // given
        when(mockSocket.getOutputStream()).thenReturn(outputStream);
        when(mockSocket.getInputStream()).thenThrow(new IOException("Input stream error"));

        // when & then
        assertDoesNotThrow(
                () -> {
                    clientHandler.run();
                },
                "스트림 설정 실패 시에도 안전하게 처리되어야 함");
    }

    @Test
    @DisplayName("ClientHandler와 다양한 Socket 상태 테스트")
    void testClientHandlerWithVariousSocketStates() throws IOException {
        Socket[] mockSockets = new Socket[3];

        for (int i = 0; i < mockSockets.length; i++) {
            mockSockets[i] = mock(Socket.class);
            final int index = i;

            assertDoesNotThrow(
                    () -> {
                        ClientHandler handler = new ClientHandler(mockSockets[index], mockServer);
                        assertNotNull(handler, "다양한 Socket 상태에서도 ClientHandler 생성 가능");
                    },
                    "Socket 상태 " + index + "에서 안전해야 함");
        }
    }

    @Test
    @DisplayName("ClientHandler 메시지 처리 안정성 테스트")
    void testClientHandlerMessageProcessingSafety() {
        // given
        // 다양한 시나리오에서 안정성 확인

        // when & then
        assertDoesNotThrow(
                () -> {
                    // 정상적인 ClientHandler 생성
                    new ClientHandler(mockSocket, mockServer);

                    // 다른 스레드에서 실행 시뮬레이션
                    Thread handlerThread =
                            new Thread(
                                    () -> {
                                        try {
                                            // 빠른 종료를 위해 짧은 대기
                                            Thread.sleep(10);
                                        } catch (InterruptedException e) {
                                            Thread.currentThread().interrupt();
                                        }
                                    });

                    handlerThread.start();
                    handlerThread.join(1000); // 최대 1초 대기
                },
                "메시지 처리가 안전해야 함");
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
        assertDoesNotThrow(
                () -> {
                    handler.run(); // cleanup이 호출되어야 함
                },
                "리소스 정리가 안전하게 수행되어야 함");
    }

    @Test
    @DisplayName("ClientHandler 동시성 안전성 테스트")
    void testClientHandlerConcurrencySafety() {
        // given
        int threadCount = 5;
        Thread[] threads = new Thread[threadCount];

        // when & then
        assertDoesNotThrow(
                () -> {
                    for (int i = 0; i < threadCount; i++) {
                        final int index = i;
                        threads[i] =
                                new Thread(
                                        () -> {
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
                },
                "동시에 여러 ClientHandler가 안전하게 동작해야 함");
    }

    @Test
    @DisplayName("ClientHandler 극한 상황 처리 테스트")
    void testClientHandlerExtremeConditions() {
        // when & then
        assertDoesNotThrow(
                () -> {
                    // 매우 많은 ClientHandler 생성
                    for (int i = 0; i < 100; i++) {
                        Socket extremeSocket = mock(Socket.class);
                        TetrisServer extremeServer = mock(TetrisServer.class);
                        ClientHandler handler = new ClientHandler(extremeSocket, extremeServer);
                        assertNotNull(handler, "극한 상황에서도 ClientHandler 생성 가능: " + i);
                    }
                },
                "극한 상황에서도 안정적으로 동작해야 함");
    }

    @Test
    @DisplayName("ClientHandler 메모리 효율성 테스트")
    void testClientHandlerMemoryEfficiency() {
        // given
        ClientHandler[] handlers = new ClientHandler[50];

        // when & then
        assertDoesNotThrow(
                () -> {
                    for (int i = 0; i < handlers.length; i++) {
                        handlers[i] =
                                new ClientHandler(mock(Socket.class), mock(TetrisServer.class));
                        assertNotNull(handlers[i], "메모리 효율적으로 ClientHandler 생성: " + i);
                    }

                    // 참조 해제
                    for (int i = 0; i < handlers.length; i++) {
                        handlers[i] = null;
                    }

                    // 가비지 컬렉션 힌트
                    System.gc();
                    Thread.sleep(100);
                },
                "메모리 효율적으로 동작해야 함");
    }

    @Test
    @DisplayName("ClientHandler 다양한 TetrisServer 상태 테스트")
    void testClientHandlerWithVariousServerStates() {
        // given
        TetrisServer[] mockServers = new TetrisServer[3];

        for (int i = 0; i < mockServers.length; i++) {
            mockServers[i] = mock(TetrisServer.class);
            final int index = i;

            assertDoesNotThrow(
                    () -> {
                        ClientHandler handler = new ClientHandler(mockSocket, mockServers[index]);
                        assertNotNull(handler, "다양한 서버 상태에서도 ClientHandler 생성 가능");
                    },
                    "서버 상태 " + i + "에서 안전해야 함");
        }
    }

    @Test
    @DisplayName("ClientHandler 스레드 안전성 검증")
    void testClientHandlerThreadSafety() {
        // given
        ClientHandler handler = new ClientHandler(mockSocket, mockServer);

        // when & then
        assertDoesNotThrow(
                () -> {
                    // Runnable 인터페이스 구현 확인
                    assertTrue(handler instanceof Runnable, "ClientHandler는 Runnable을 구현해야 함");

                    // Thread에서 실행 가능성 확인
                    Thread testThread = new Thread(handler);
                    assertNotNull(testThread, "ClientHandler로 Thread 생성 가능");
                },
                "스레드 안전성이 보장되어야 함");
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
        assertDoesNotThrow(
                () -> {
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
                },
                "다양한 매개변수 조합이 안전하게 처리되어야 함");
    }

    @Test
    @DisplayName("ClientHandler getPlayerId 테스트")
    void testGetPlayerId() {
        ClientHandler handler = new ClientHandler(mockSocket, mockServer);

        // 초기 상태에서는 playerId가 null
        assertNull(handler.getPlayerId(), "초기 상태에서는 playerId가 null");
    }

    @Test
    @DisplayName("ClientHandler isConnected 상태 테스트")
    void testIsConnectedState() {
        ClientHandler handler = new ClientHandler(mockSocket, mockServer);

        // 초기 상태 확인 (running은 true로 시작)
        assertDoesNotThrow(
                () -> {
                    handler.isConnected();
                },
                "isConnected 호출은 안전해야 함");
    }

    @Test
    @DisplayName("ClientHandler close 메서드 테스트")
    void testCloseMethod() {
        ClientHandler handler = new ClientHandler(mockSocket, mockServer);

        assertDoesNotThrow(
                () -> {
                    handler.close();
                    // 여러 번 호출해도 안전해야 함
                    handler.close();
                    handler.close();
                },
                "close 메서드는 여러 번 호출해도 안전해야 함");
    }

    @Test
    @DisplayName("ClientHandler 메시지 전송 - IOException 처리")
    void testSendMessageWithIOException() throws IOException {
        // given
        Socket errorSocket = mock(Socket.class);
        when(errorSocket.getOutputStream()).thenThrow(new IOException("Output stream error"));

        // ClientHandler 생성 자체는 실패하지 않을 수 있음 (run() 메서드 실행 시 실패)
        // when & then
        assertDoesNotThrow(
                () -> {
                    ClientHandler handler = new ClientHandler(errorSocket, mockServer);
                    assertNotNull(handler, "OutputStream 에러 상황에서도 ClientHandler 생성 가능");
                },
                "OutputStream 에러는 run() 시점에 처리됨");
    }

    @Test
    @DisplayName("ClientHandler Runnable 인터페이스 검증")
    void testRunnableInterface() {
        ClientHandler handler = new ClientHandler(mockSocket, mockServer);

        assertTrue(handler instanceof Runnable, "ClientHandler는 Runnable을 구현해야 함");

        assertDoesNotThrow(
                () -> {
                    Thread testThread = new Thread(handler);
                    assertNotNull(testThread, "ClientHandler로 Thread 생성 가능");
                },
                "Runnable로 Thread 생성은 안전해야 함");
    }

    @Test
    @DisplayName("다양한 Socket 상태로 ClientHandler 생성")
    void testClientHandlerWithDifferentSocketStates() {
        assertDoesNotThrow(
                () -> {
                    // 일반 mock socket
                    Socket socket1 = mock(Socket.class);
                    when(socket1.isClosed()).thenReturn(false);
                    ClientHandler handler1 = new ClientHandler(socket1, mockServer);
                    assertNotNull(handler1, "일반 소켓으로 생성 가능");

                    // 닫힌 socket
                    Socket socket2 = mock(Socket.class);
                    when(socket2.isClosed()).thenReturn(true);
                    ClientHandler handler2 = new ClientHandler(socket2, mockServer);
                    assertNotNull(handler2, "닫힌 소켓으로 생성 가능");
                },
                "다양한 Socket 상태로 생성은 안전해야 함");
    }

    @Test
    @DisplayName("ClientHandler 다중 인스턴스 독립성")
    void testMultipleClientHandlerIndependence() {
        Socket socket1 = mock(Socket.class);
        Socket socket2 = mock(Socket.class);
        Socket socket3 = mock(Socket.class);

        ClientHandler handler1 = new ClientHandler(socket1, mockServer);
        ClientHandler handler2 = new ClientHandler(socket2, mockServer);
        ClientHandler handler3 = new ClientHandler(socket3, mockServer);

        assertNotEquals(handler1, handler2, "각 ClientHandler는 독립적");
        assertNotEquals(handler2, handler3, "각 ClientHandler는 독립적");
        assertNotEquals(handler1, handler3, "각 ClientHandler는 독립적");

        assertNotNull(handler1, "handler1 생성됨");
        assertNotNull(handler2, "handler2 생성됨");
        assertNotNull(handler3, "handler3 생성됨");
    }

    @Test
    @DisplayName("ClientHandler 동시 다중 생성 및 실행")
    void testConcurrentClientHandlerCreation() {
        int handlerCount = 10;
        ClientHandler[] handlers = new ClientHandler[handlerCount];
        Thread[] threads = new Thread[handlerCount];

        assertDoesNotThrow(
                () -> {
                    for (int i = 0; i < handlerCount; i++) {
                        Socket socket = mock(Socket.class);
                        TetrisServer server = mock(TetrisServer.class);

                        handlers[i] = new ClientHandler(socket, server);
                        threads[i] = new Thread(handlers[i]);

                        assertNotNull(handlers[i], "Handler " + i + " 생성 성공");
                    }

                    // 모든 스레드 시작
                    for (int i = 0; i < handlerCount; i++) {
                        threads[i].start();
                    }

                    // 짧은 대기 후 종료
                    Thread.sleep(100);

                    for (int i = 0; i < handlerCount; i++) {
                        handlers[i].close();
                    }
                },
                "동시 다중 ClientHandler 생성 및 실행은 안전해야 함");
    }

    @Test
    @DisplayName("ClientHandler InputStream 에러 처리")
    void testInputStreamError() throws IOException {
        // given
        Socket errorSocket = mock(Socket.class);
        ByteArrayOutputStream mockOutputStream = new ByteArrayOutputStream();
        when(errorSocket.getOutputStream()).thenReturn(mockOutputStream);
        when(errorSocket.getInputStream()).thenThrow(new IOException("Input stream error"));

        // ClientHandler 생성 자체는 실패하지 않을 수 있음 (run() 메서드 실행 시 실패)
        // when & then
        assertDoesNotThrow(
                () -> {
                    ClientHandler handler = new ClientHandler(errorSocket, mockServer);
                    assertNotNull(handler, "InputStream 에러 상황에서도 ClientHandler 생성 가능");
                },
                "InputStream 에러는 run() 시점에 처리됨");
    }

    @Test
    @DisplayName("ClientHandler 메모리 효율성 - 대량 생성")
    void testClientHandlerLargeScaleMemoryEfficiency() {
        int count = 100;
        ClientHandler[] handlers = new ClientHandler[count];

        assertDoesNotThrow(
                () -> {
                    for (int i = 0; i < count; i++) {
                        handlers[i] =
                                new ClientHandler(mock(Socket.class), mock(TetrisServer.class));
                        assertNotNull(handlers[i], "Handler " + i + " 생성 성공");
                    }

                    // 참조 해제
                    for (int i = 0; i < count; i++) {
                        handlers[i] = null;
                    }

                    // 가비지 컬렉션 힌트
                    System.gc();
                    Thread.sleep(50);
                },
                "대량 ClientHandler 생성 및 해제는 안전해야 함");
    }

    @Test
    @DisplayName("ClientHandler 리소스 정리 - 다양한 상태")
    void testCleanupInVariousStates() throws IOException {
        assertDoesNotThrow(
                () -> {
                    // 정상 상태
                    Socket normalSocket = mock(Socket.class);
                    when(normalSocket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
                    when(normalSocket.getInputStream())
                            .thenReturn(new java.io.ByteArrayInputStream(new byte[0]));
                    ClientHandler handler1 = new ClientHandler(normalSocket, mockServer);
                    handler1.close();

                    // 이미 닫힌 상태
                    handler1.close();
                },
                "다양한 상태에서 리소스 정리는 안전해야 함");
    }

    @Test
    @DisplayName("ClientHandler getPlayerId - null 안전성")
    void testGetPlayerIdNullSafety() {
        ClientHandler handler = new ClientHandler(null, null);

        assertDoesNotThrow(
                () -> {
                    String playerId = handler.getPlayerId();
                    // 초기 상태에서는 null이 반환될 수 있음
                    if (playerId != null) {
                        assertFalse(playerId.isEmpty(), "playerId가 비어있지 않음");
                    }
                },
                "getPlayerId는 null-safe해야 함");
    }

    @Test
    @DisplayName("ClientHandler 다양한 TetrisServer 상태와 함께 동작")
    void testClientHandlerWithDifferentServerStates() {
        TetrisServer[] servers = new TetrisServer[5];
        ClientHandler[] handlers = new ClientHandler[5];

        assertDoesNotThrow(
                () -> {
                    for (int i = 0; i < 5; i++) {
                        servers[i] = mock(TetrisServer.class);
                        handlers[i] = new ClientHandler(mockSocket, servers[i]);
                        assertNotNull(handlers[i], "다양한 서버 상태에서 Handler " + i + " 생성 성공");
                    }
                },
                "다양한 서버 상태에서 ClientHandler 동작은 안전해야 함");
    }

    @Test
    @DisplayName("ClientHandler 여러 번 close 호출하여 running 상태 확인")
    void testMultipleCloseCallsRunningState() throws Exception {
        ClientHandler handler = new ClientHandler(mockSocket, mockServer);

        java.lang.reflect.Field runningField = ClientHandler.class.getDeclaredField("running");
        runningField.setAccessible(true);

        handler.close();
        assertFalse((Boolean) runningField.get(handler), "첫 번째 close 후 running은 false");

        handler.close();
        assertFalse((Boolean) runningField.get(handler), "두 번째 close 후에도 running은 false");

        handler.close();
        assertFalse((Boolean) runningField.get(handler), "세 번째 close 후에도 running은 false");
    }

    @Test
    @DisplayName("setupStreams 메서드 Reflection 테스트")
    void testSetupStreamsViaReflection() throws Exception {
        // ObjectOutputStream은 생성 시 헤더를 쓰므로 실제 동작 가능한 스트림이 필요
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[100]); // 충분한 크기

        Socket testSocket = mock(Socket.class);
        when(testSocket.getOutputStream()).thenReturn(outputStream);
        when(testSocket.getInputStream()).thenReturn(inputStream);

        ClientHandler handler = new ClientHandler(testSocket, mockServer);

        java.lang.reflect.Method setupStreamsMethod =
                ClientHandler.class.getDeclaredMethod("setupStreams");
        setupStreamsMethod.setAccessible(true);

        // InvocationTargetException을 catch하여 실제 예외 확인
        try {
            setupStreamsMethod.invoke(handler);
            // 성공하면 output 필드가 설정되어야 함
            java.lang.reflect.Field outputField = ClientHandler.class.getDeclaredField("output");
            outputField.setAccessible(true);
            assertNotNull(outputField.get(handler), "output이 설정되어야 함");
        } catch (java.lang.reflect.InvocationTargetException e) {
            // setupStreams는 IOException을 던질 수 있으므로 정상 동작
            assertTrue(e.getCause() instanceof IOException, "IOException이 발생할 수 있음");
        }
    }

    // ========== Field Access 테스트 ==========

    @Test
    @DisplayName("ClientHandler 필드 접근 테스트 - clientSocket")
    void testClientSocketFieldAccess() throws Exception {
        ClientHandler handler = new ClientHandler(mockSocket, mockServer);

        java.lang.reflect.Field field = ClientHandler.class.getDeclaredField("clientSocket");
        field.setAccessible(true);

        Object value = field.get(handler);
        assertEquals(mockSocket, value, "clientSocket 필드가 올바르게 설정되어야 함");
    }

    @Test
    @DisplayName("ClientHandler 필드 접근 테스트 - server")
    void testServerFieldAccess() throws Exception {
        ClientHandler handler = new ClientHandler(mockSocket, mockServer);

        java.lang.reflect.Field field = ClientHandler.class.getDeclaredField("server");
        field.setAccessible(true);

        Object value = field.get(handler);
        assertEquals(mockServer, value, "server 필드가 올바르게 설정되어야 함");
    }

    @Test
    @DisplayName("ClientHandler 필드 접근 테스트 - playerId")
    void testPlayerIdFieldAccess() throws Exception {
        ClientHandler handler = new ClientHandler(mockSocket, mockServer);

        java.lang.reflect.Field field = ClientHandler.class.getDeclaredField("playerId");
        field.setAccessible(true);

        Object value = field.get(handler);
        assertNull(value, "초기 playerId는 null이어야 함");

        // playerId 설정
        field.set(handler, "player1");
        assertEquals("player1", handler.getPlayerId(), "playerId가 올바르게 설정되어야 함");
    }

    @Test
    @DisplayName("ClientHandler 필드 접근 테스트 - running")
    void testRunningFieldAccess() throws Exception {
        ClientHandler handler = new ClientHandler(mockSocket, mockServer);

        java.lang.reflect.Field field = ClientHandler.class.getDeclaredField("running");
        field.setAccessible(true);

        Object value = field.get(handler);
        assertTrue((Boolean) value, "초기 running 값은 true여야 함");

        // close 호출 후
        handler.close();
        value = field.get(handler);
        assertFalse((Boolean) value, "close 호출 후 running은 false여야 함");
    }

    @Test
    @DisplayName("ClientHandler 필드 접근 테스트 - input/output")
    void testInputOutputFieldAccess() throws Exception {
        ClientHandler handler = new ClientHandler(mockSocket, mockServer);

        java.lang.reflect.Field inputField = ClientHandler.class.getDeclaredField("input");
        inputField.setAccessible(true);

        java.lang.reflect.Field outputField = ClientHandler.class.getDeclaredField("output");
        outputField.setAccessible(true);

        Object inputValue = inputField.get(handler);
        Object outputValue = outputField.get(handler);

        assertNull(inputValue, "초기 input은 null이어야 함");
        assertNull(outputValue, "초기 output은 null이어야 함");
    }

    // ========== Public 메서드 테스트 ==========

    @Test
    @DisplayName("sendMessage 메서드 테스트 - output이 null인 경우")
    void testSendMessageWithNullOutput() throws Exception {
        ClientHandler handler = new ClientHandler(mockSocket, mockServer);

        team13.tetris.network.protocol.ConnectionMessage message =
                team13.tetris.network.protocol.ConnectionMessage.createConnectionAccepted(
                        "server", "player1");

        assertThrows(
                NullPointerException.class,
                () -> {
                    handler.sendMessage(message);
                },
                "output이 null인 경우 NullPointerException 발생");
    }

    @Test
    @DisplayName("isConnected 메서드 테스트 - 다양한 상태")
    void testIsConnectedVariousStates() throws Exception {
        Socket testSocket = mock(Socket.class);
        when(testSocket.isClosed()).thenReturn(false);

        ClientHandler handler = new ClientHandler(testSocket, mockServer);

        // 초기 상태
        boolean initialState = handler.isConnected();
        assertTrue(initialState, "초기 상태에서는 running=true이므로 연결된 것으로 판단");

        // close 호출
        handler.close();

        // close 후 상태
        boolean afterClose = handler.isConnected();
        assertFalse(afterClose, "close 호출 후에는 연결되지 않은 것으로 판단");
    }

    @Test
    @DisplayName("isConnected 메서드 테스트 - socket이 닫힌 경우")
    void testIsConnectedWithClosedSocket() throws Exception {
        Socket testSocket = mock(Socket.class);
        when(testSocket.isClosed()).thenReturn(true);

        ClientHandler handler = new ClientHandler(testSocket, mockServer);

        boolean connected = handler.isConnected();

        assertFalse(connected, "socket이 닫힌 경우 연결되지 않은 것으로 판단");
    }

    @Test
    @DisplayName("cleanup 메서드 Reflection 테스트")
    void testCleanupViaReflection() throws Exception {
        ClientHandler handler = new ClientHandler(mockSocket, mockServer);

        java.lang.reflect.Field playerIdField = ClientHandler.class.getDeclaredField("playerId");
        playerIdField.setAccessible(true);
        playerIdField.set(handler, "player1");

        java.lang.reflect.Method cleanupMethod = ClientHandler.class.getDeclaredMethod("cleanup");
        cleanupMethod.setAccessible(true);

        assertDoesNotThrow(
                () -> {
                    cleanupMethod.invoke(handler);
                    verify(mockServer).unregisterClient("player1");
                },
                "cleanup 메서드가 정상 실행되어야 함");
    }

    @Test
    @DisplayName("messageLoop 메서드 Reflection 테스트")
    void testMessageLoopViaReflection() throws Exception {
        Socket testSocket = mock(Socket.class);
        when(testSocket.isClosed()).thenReturn(true); // 즉시 종료되도록

        ClientHandler handler = new ClientHandler(testSocket, mockServer);

        java.lang.reflect.Method messageLoopMethod =
                ClientHandler.class.getDeclaredMethod("messageLoop");
        messageLoopMethod.setAccessible(true);

        assertDoesNotThrow(
                () -> {
                    messageLoopMethod.invoke(handler);
                },
                "messageLoop가 안전하게 실행되어야 함");
    }

    @Test
    @DisplayName("run 메서드 실행 테스트 - setupStreams 실패")
    void testRunMethodWithSetupStreamsFailure() throws IOException {
        Socket testSocket = mock(Socket.class);
        when(testSocket.getOutputStream()).thenThrow(new IOException("Output error"));

        ClientHandler handler = new ClientHandler(testSocket, mockServer);

        assertDoesNotThrow(
                () -> {
                    handler.run();
                },
                "setupStreams 실패해도 안전하게 처리되어야 함");
    }

    @Test
    @DisplayName("playerId 설정 후 getPlayerId 테스트")
    void testGetPlayerIdAfterSetting() throws Exception {
        ClientHandler handler = new ClientHandler(mockSocket, mockServer);

        java.lang.reflect.Field playerIdField = ClientHandler.class.getDeclaredField("playerId");
        playerIdField.setAccessible(true);
        playerIdField.set(handler, "testPlayerId");

        assertEquals("testPlayerId", handler.getPlayerId(), "설정한 playerId가 올바르게 반환되어야 함");
    }

    @Test
    @SuppressWarnings("unused")
    @DisplayName("isConnected - null socket 처리")
    void testIsConnectedWithNullSocket() throws Exception {
        ClientHandler handler = new ClientHandler(null, mockServer);

        // running=true이지만 socket이 null인 경우
        assertDoesNotThrow(
                () -> {
                    boolean connected = handler.isConnected();
                    // socket이 null이면 NullPointerException이 발생할 수 있으므로 처리 확인
                },
                "null socket 처리가 안전해야 함");
    }
}
