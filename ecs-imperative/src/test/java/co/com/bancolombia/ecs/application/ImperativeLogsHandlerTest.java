package co.com.bancolombia.ecs.application;

import co.com.bancolombia.ecs.application.filter.ImperativeLogsHandler;
import co.com.bancolombia.ecs.domain.model.ExceptionLevel;
import co.com.bancolombia.ecs.infra.config.EcsPropertiesConfig;
import co.com.bancolombia.ecs.infra.config.PrintOnErrorProperties;
import co.com.bancolombia.ecs.infra.config.managementid.application.MessageIdMngUseCase;
import co.com.bancolombia.ecs.infra.config.sensitive.SensitiveRequestProperties;
import co.com.bancolombia.ecs.infra.config.sensitive.SensitiveResponseProperties;
import co.com.bancolombia.ecs.infra.config.service.ServiceProperties;
import co.com.bancolombia.ecs.model.management.BusinessExceptionECS;
import co.com.bancolombia.ecs.model.management.ErrorManagement;
import co.com.bancolombia.ecs.model.request.LogRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImperativeLogsHandlerTest {

    @Mock
    private ServiceProperties serviceProperties;

    @Mock
    private SensitiveRequestProperties sensitiveRequestProperties;

    @Mock
    private SensitiveResponseProperties sensitiveResponseProperties;

    @Mock
    private EcsPropertiesConfig ecsPropertiesConfig;

    @Mock
    private MessageIdMngUseCase messageIdMngUseCase;

    @Mock
    private FilterChain filterChain;

    @Mock
    private PrintOnErrorProperties printOnErrorProperties;

    @InjectMocks
    private ImperativeLogsHandler imperativeLogsHandler;

    private MockHttpServletRequest mockRequest;
    private MockHttpServletResponse mockResponse;
    private ContentCachingRequestWrapper wrappedRequest;
    private ContentCachingResponseWrapper wrappedResponse;

    private static final int MAX_PAYLOAD_SIZE = 1024 * 1024;

    @BeforeEach
    void setUp() {
        mockRequest = new MockHttpServletRequest();
        mockResponse = new MockHttpServletResponse();
        wrappedRequest = new ContentCachingRequestWrapper(mockRequest, MAX_PAYLOAD_SIZE);
        wrappedResponse = new ContentCachingResponseWrapper(mockResponse);
        imperativeLogsHandler = new ImperativeLogsHandler(ecsPropertiesConfig, messageIdMngUseCase);
    }

    @ParameterizedTest
    @MethodSource("pathShowRequestShowResponseCases")
    void testShouldSkipFilter(String path, boolean showRequest, boolean showResponse)
            throws ServletException, IOException {
        mocksPropertiesConfig(showRequest, showResponse);
        mockRequest.setRequestURI(path);

        imperativeLogsHandler.doFilter(mockRequest, mockResponse, filterChain);

        verify(filterChain).doFilter(any(), any());
    }
    private static Stream<Arguments> pathShowRequestShowResponseCases() {
        return Stream.of(
                Arguments.of("/actuator/health/check", true, true),
                Arguments.of("/", true, true),
                Arguments.of("/api/test", false, false)
        );
    }

    @Test
    void testShouldProcessRequestAndResponseLogs() throws IOException, ServletException {
        mocksPropertiesConfig(true, true);
        testFilter();
    }

    @Test
    void testShouldProcessRequestLogs() throws IOException, ServletException {
        mocksPropertiesConfig(true, false);
        testFilter();
    }

    @Test
    void testShouldProcessResponseLogs() throws IOException, ServletException {
        mocksPropertiesConfig(false, true);
        testFilter();
    }

    @Test
    void testShouldHandleBusinessExceptionECS() throws IOException, ServletException {
        mocksPropertiesConfig(true, true);
        mockRequest.setRequestURI("/api/test");
        mockRequest.setMethod("GET");
        mockRequest.addHeader("message-id", "12345");
        mockRequest.setContent("{\"data\": \"test\"}".getBytes());
        BusinessExceptionECS exception = new BusinessExceptionECS("error");
        doThrow(exception).when(filterChain).doFilter(any(), any());

        assertThrows(BusinessExceptionECS.class, () ->
            imperativeLogsHandler.doFilter(wrappedRequest, wrappedResponse, filterChain));

        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void testShouldHandleGenericException() throws IOException, ServletException {
        mocksPropertiesConfig(true, true);
        mockRequest.setRequestURI("/api/test");
        mockRequest.setMethod("GET");
        mockRequest.addHeader("message-id", "12345");
        mockRequest.setContent("{\"data\": \"test\"}".getBytes());
        RuntimeException exception = new RuntimeException("Test error");
        doThrow(exception).when(filterChain).doFilter(wrappedRequest, wrappedResponse);

        assertThrows(RuntimeException.class, () ->
            imperativeLogsHandler.doFilter(wrappedRequest, wrappedResponse, filterChain));

        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void testShouldLogRequestOnErrorStatusWithException() throws IOException, ServletException {
        mocksPropertiesConfig(true, true);
        mockRequest.setRequestURI("/api/test");
        mockRequest.setMethod("GET");
        mockRequest.addHeader("message-id", "12345");
        mockRequest.setContent("{\"data\": \"test\"}".getBytes());

        wrappedResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        wrappedResponse.getWriter().write("{\"error\": \"Internal error\"}");

        var exception = new BusinessExceptionECS("error");
        wrappedRequest.setAttribute("handledException", exception);

        assertDoesNotThrow(() ->
            imperativeLogsHandler.doFilter(wrappedRequest, wrappedResponse, filterChain));

        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void testShouldLogStatusOnlyErrorResponseThroughErrorPath() throws IOException, ServletException {
        mocksPropertiesConfig(true, true);
        mockRequest.setRequestURI("/api/test");
        mockRequest.setMethod("GET");
        mockRequest.addHeader("message-id", "12345");
        mockRequest.setContent("{\"data\": \"test\"}".getBytes());
        wrappedResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        wrappedResponse.getWriter().write("{\"error\": \"Internal error\"}");
        AtomicReference<LogRequest> capturedRequest = new AtomicReference<>();

        try (MockedStatic<co.com.bancolombia.ecs.infra.EcsImperativeLogger> mockedLogger =
                     Mockito.mockStatic(co.com.bancolombia.ecs.infra.EcsImperativeLogger.class)) {
            mockedLogger.when(() -> co.com.bancolombia.ecs.infra.EcsImperativeLogger.build(
                            Mockito.any(LogRequest.class), Mockito.anyString()))
                    .thenAnswer(invocation -> {
                        capturedRequest.set(invocation.getArgument(0));
                        return null;
                    });

            assertDoesNotThrow(() ->
                    imperativeLogsHandler.doFilter(wrappedRequest, wrappedResponse, filterChain));
        }

        verify(filterChain).doFilter(any(), any());
        assertNotNull(capturedRequest.get());
        assertEquals("500", capturedRequest.get().getResponseCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), capturedRequest.get().getResponseResult());
        ResponseStatusException loggedException =
                assertInstanceOf(ResponseStatusException.class, capturedRequest.get().getError());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, loggedException.getStatusCode());
    }

    @Test
    void testShouldLogRequestOnErrorStatusWithUnhandledException() throws IOException, ServletException {
        mocksPropertiesConfig(true, true);
        mockRequest.setRequestURI("/api/test");
        mockRequest.setMethod("GET");
        mockRequest.addHeader("message-id", "12345");
        mockRequest.setContent("{\"data\": \"test\"}".getBytes());

        wrappedResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        wrappedResponse.getWriter().write("{\"error\": \"Internal error\"}");

        var exception = new RuntimeException("error");
        wrappedRequest.setAttribute("handledException", exception);

        assertDoesNotThrow(() ->
            imperativeLogsHandler.doFilter(wrappedRequest, wrappedResponse, filterChain));

        verify(filterChain).doFilter(any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Exception", "BusinessExceptionECS"})
    @NullSource
    void testShouldHandlePrintOnErrorDifferentLevels(String level) throws ServletException, IOException {
        mocksPropertiesConfig(false, false, true, level, "/actuator");
        mockRequest.setRequestURI("/api/test");
        mockRequest.setMethod("POST");
        mockRequest.addHeader("message-id", "12345");
        mockRequest.setContent("{\"data\":\"ok\"}".getBytes());
        wrappedResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        wrappedRequest.setAttribute("handledException", new RuntimeException("boom"));

        assertDoesNotThrow(() ->
                imperativeLogsHandler.doFilter(wrappedRequest, wrappedResponse, filterChain));
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void testShouldLogCompletedForbiddenResponseInPrintOnErrorMode() throws IOException, ServletException {
        mocksPropertiesConfig(false, false, true, "Exception", "/actuator");
        mockRequest.setRequestURI("/api/cors");
        mockRequest.setMethod("OPTIONS");
        mockRequest.addHeader("message-id", "12345");
        mockRequest.setContent("{\"data\":\"ok\"}".getBytes());
        wrappedResponse.setStatus(HttpStatus.FORBIDDEN.value());

        AtomicReference<LogRequest> capturedRequest = new AtomicReference<>();

        try (MockedStatic<co.com.bancolombia.ecs.infra.EcsImperativeLogger> mockedLogger =
                     Mockito.mockStatic(co.com.bancolombia.ecs.infra.EcsImperativeLogger.class)) {
            mockedLogger.when(() -> co.com.bancolombia.ecs.infra.EcsImperativeLogger.build(
                    Mockito.any(LogRequest.class), Mockito.anyString()))
                .thenAnswer(invocation -> {
                    capturedRequest.set(invocation.getArgument(0));
                    return null;
                });

            assertDoesNotThrow(() ->
                imperativeLogsHandler.doFilter(wrappedRequest, wrappedResponse, filterChain));
        }

        verify(filterChain).doFilter(any(), any());
        assertNotNull(capturedRequest.get());
        assertEquals("403", capturedRequest.get().getResponseCode());
        assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), capturedRequest.get().getResponseResult());
        ResponseStatusException loggedException =
            assertInstanceOf(ResponseStatusException.class, capturedRequest.get().getError());
        assertEquals(HttpStatus.FORBIDDEN, loggedException.getStatusCode());
    }

    @Test
    void testShouldIgnoreHeadersWithNullValue() {
        mocksPropertiesConfig(true, false);
        AtomicReference<LogRequest> capturedRequest = new AtomicReference<>();
        MockHttpServletRequest requestWithNullHeader = Mockito.spy(new MockHttpServletRequest());
        doAnswer(inv -> Collections.enumeration(List.of("message-id", "x-null"))).when(requestWithNullHeader).getHeaderNames();
        doReturn("12345").when(requestWithNullHeader).getHeader("message-id");
        doReturn(null).when(requestWithNullHeader).getHeader("x-null");
        requestWithNullHeader.setRequestURI("/api/test");
        requestWithNullHeader.setMethod("GET");
        requestWithNullHeader.setContent("{\"data\": \"test\"}".getBytes());

        try (MockedStatic<co.com.bancolombia.ecs.infra.EcsImperativeLogger> mockedLogger =
                     Mockito.mockStatic(co.com.bancolombia.ecs.infra.EcsImperativeLogger.class)) {
            mockedLogger.when(() -> co.com.bancolombia.ecs.infra.EcsImperativeLogger.build(
                    Mockito.any(LogRequest.class), Mockito.anyString()))
                .thenAnswer(invocation -> {
                    capturedRequest.set(invocation.getArgument(0));
                    return null;
                });

            assertDoesNotThrow(() ->
                imperativeLogsHandler.doFilter(requestWithNullHeader, new MockHttpServletResponse(), filterChain));
        }

        assertNotNull(capturedRequest.get());
        assertFalse(capturedRequest.get().getHeaders().containsKey("x-null"));
        assertEquals("12345", capturedRequest.get().getHeaders().get("message-id"));
    }

    @Test
    void testShouldPreserveResponseStatusExceptionStatusWhenLoggingErrors() {
        mocksPropertiesConfig(true, false);
        mockRequest.setRequestURI("/api/test");
        mockRequest.setMethod("OPTIONS");
        mockRequest.addHeader("message-id", "12345");
        wrappedResponse.setStatus(HttpStatus.FORBIDDEN.value());

        AtomicReference<LogRequest> capturedRequest = new AtomicReference<>();
        ResponseStatusException exception =
            new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid CORS request");
        wrappedRequest.setAttribute("handledException", exception);

        try (MockedStatic<co.com.bancolombia.ecs.infra.EcsImperativeLogger> mockedLogger =
                     Mockito.mockStatic(co.com.bancolombia.ecs.infra.EcsImperativeLogger.class)) {
            mockedLogger.when(() -> co.com.bancolombia.ecs.infra.EcsImperativeLogger.build(
                    Mockito.any(LogRequest.class), Mockito.anyString()))
                .thenAnswer(invocation -> {
                    capturedRequest.set(invocation.getArgument(0));
                    return null;
                });

            assertDoesNotThrow(() ->
                imperativeLogsHandler.doFilter(wrappedRequest, wrappedResponse, filterChain));
        }

        assertNotNull(capturedRequest.get());
        assertEquals("403", capturedRequest.get().getResponseCode());
        assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), capturedRequest.get().getResponseResult());
        assertSame(exception, capturedRequest.get().getError());
    }

    @Test
    void testShouldFallbackToInternalServerErrorWhenBusinessStatusIsNull() {
        mocksPropertiesConfig(true, false);
        mockRequest.setRequestURI("/api/test");
        mockRequest.setMethod("GET");
        mockRequest.addHeader("message-id", "12345");
        wrappedResponse.setStatus(HttpStatus.BAD_REQUEST.value());

        AtomicReference<LogRequest> capturedRequest = new AtomicReference<>();
        BusinessExceptionECS exception = new BusinessExceptionECS(errorManagementWithNullStatus());
        wrappedRequest.setAttribute("handledException", exception);

        try (MockedStatic<co.com.bancolombia.ecs.infra.EcsImperativeLogger> mockedLogger =
                     Mockito.mockStatic(co.com.bancolombia.ecs.infra.EcsImperativeLogger.class)) {
            mockedLogger.when(() -> co.com.bancolombia.ecs.infra.EcsImperativeLogger.build(
                    Mockito.any(LogRequest.class), Mockito.anyString()))
                .thenAnswer(invocation -> {
                    capturedRequest.set(invocation.getArgument(0));
                    return null;
                });

            assertDoesNotThrow(() ->
                imperativeLogsHandler.doFilter(wrappedRequest, wrappedResponse, filterChain));
        }

        assertNotNull(capturedRequest.get());
        assertEquals("500", capturedRequest.get().getResponseCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), capturedRequest.get().getResponseResult());
        assertSame(exception, capturedRequest.get().getError());
    }

    @Test
    void testShouldParseValidJsonBodies() {
        mocksPropertiesConfig(true, true);
        mockRequest.setRequestURI("/api/test");
        mockRequest.setMethod("POST");
        mockRequest.addHeader("message-id", "12345");
        mockRequest.setContent("{\"data\":\"test\"}".getBytes());

        FilterChain parsingChain = (request, response) -> {
            ContentCachingRequestWrapper requestWrapper = (ContentCachingRequestWrapper) request;
            requestWrapper.getInputStream().readAllBytes();
            ContentCachingResponseWrapper responseWrapper = (ContentCachingResponseWrapper) response;
            responseWrapper.setStatus(HttpStatus.OK.value());
            responseWrapper.getWriter().write("{\"result\":\"success\"}");
        };

        assertDoesNotThrow(() -> imperativeLogsHandler.doFilter(mockRequest, mockResponse, parsingChain));
    }


    private void testFilter() throws IOException, ServletException {
        mockRequest.setRequestURI("/api/test");
        mockRequest.setMethod("POST");
        mockRequest.addHeader("message-id", "12345");
        mockRequest.addHeader("consumer-acronym", "test-consumer");
        mockRequest.setContent("{\"data\": \"test\", \"password\": \"secret\"}".getBytes());
        mockResponse.setStatus(HttpStatus.OK.value());
        mockResponse.getWriter().write("{\"result\": \"success\", \"secret\": \"hidden\"}");
        wrappedResponse.copyBodyToResponse();
        wrappedResponse.setStatus(HttpStatus.OK.value());
        assertDoesNotThrow(() -> imperativeLogsHandler.doFilter(wrappedRequest, wrappedResponse, filterChain));

        verify(filterChain).doFilter(any(), any());
    }

    private void mocksPropertiesConfig(boolean showRequest, boolean showResponse) {
        mocksPropertiesConfig(showRequest, showResponse, null, null, "/actuator");
    }

    private void mocksPropertiesConfig(boolean showRequest, boolean showResponse,
                                       Boolean printOnError, String level,
                                       String excludedPaths) {
        when(serviceProperties.getName()).thenReturn("test-service");
        when(sensitiveRequestProperties.getShow()).thenReturn(showRequest);

        boolean printOnErrorActive = Boolean.TRUE.equals(printOnError);
        if (showRequest || printOnErrorActive) {
            when(sensitiveRequestProperties.getDelimiter()).thenReturn("\\|");
            when(sensitiveRequestProperties.getAllowHeaders()).thenReturn("message-id|code|channel|acronym-channel");
            when(sensitiveRequestProperties.getFields()).thenReturn("");
            when(sensitiveRequestProperties.getExcludedPaths()).thenReturn(excludedPaths);
            when(sensitiveRequestProperties.getPatterns()).thenReturn("");
            when(sensitiveRequestProperties.getReplacement()).thenReturn("*****");
        }

        when(sensitiveResponseProperties.getShow()).thenReturn(showResponse);
        if (showResponse || printOnErrorActive) {
            when(sensitiveResponseProperties.getDelimiter()).thenReturn("\\|");
            when(sensitiveResponseProperties.getFields()).thenReturn("");
            when(sensitiveResponseProperties.getPatterns()).thenReturn("");
            when(sensitiveResponseProperties.getReplacement()).thenReturn("*****");
        }

        when(printOnErrorProperties.getPrintReqResp()).thenReturn(printOnError);
        when(printOnErrorProperties.getPrintReqRespLevel()).thenReturn(
                ExceptionLevel.toExceptionLevelIgnoreCase(level));

        ecsPropertiesConfig = new EcsPropertiesConfig(
                serviceProperties,
                sensitiveRequestProperties,
                sensitiveResponseProperties,
                printOnErrorProperties);
        imperativeLogsHandler = new ImperativeLogsHandler(ecsPropertiesConfig, messageIdMngUseCase);
    }

    private ErrorManagement errorManagementWithNullStatus() {
        return new ErrorManagement() {
            @Override
            public Integer getStatus() {
                return null;
            }

            @Override
            public String getMessage() {
                return "Invalid CORS request";
            }

            @Override
            public String getErrorCode() {
                return "CORS-403";
            }

            @Override
            public String getInternalMessage() {
                return "CORS validation failed";
            }

            @Override
            public String getLogCode() {
                return "CORS-403-00";
            }
        };
    }
}