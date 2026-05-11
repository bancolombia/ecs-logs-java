package co.com.bancolombia.ecs.application;

import co.com.bancolombia.ecs.application.filter.ReactiveLogsHandler;
import co.com.bancolombia.ecs.domain.model.ExceptionLevel;
import co.com.bancolombia.ecs.infra.config.EcsPropertiesConfig;
import co.com.bancolombia.ecs.infra.config.PrintOnErrorProperties;
import co.com.bancolombia.ecs.infra.config.managementid.application.MessageIdMngUseCase;
import co.com.bancolombia.ecs.infra.config.sensitive.SensitiveRequestProperties;
import co.com.bancolombia.ecs.infra.config.sensitive.SensitiveResponseProperties;
import co.com.bancolombia.ecs.infra.config.service.ServiceProperties;
import co.com.bancolombia.ecs.model.management.BusinessExceptionECS;
import co.com.bancolombia.ecs.model.management.ErrorManagement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class ReactiveLogsHandlerTest {

    @Mock
    private ServiceProperties serviceProperties;

    @Mock
    private SensitiveRequestProperties sensitiveRequestProperties;

    @Mock
    private SensitiveResponseProperties sensitiveResponseProperties;

    @Mock
    private EcsPropertiesConfig ecsPropertiesConfig;

    @Mock
    private WebFilterChain chain;

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private ServerHttpResponse response;

    @Mock
    private PrintOnErrorProperties printOnErrorProperties;

    @Mock
    private MessageIdMngUseCase messageIdMngUseCase;

    @InjectMocks
    private ReactiveLogsHandler webHandler;

    @BeforeEach
    void setUp() {
        webHandler = new ReactiveLogsHandler(ecsPropertiesConfig, messageIdMngUseCase);
    }

    @Test
    void testShouldSkipExcludedPaths() {
        when(ecsPropertiesConfig.getPrintReqRespOnErrorOnly()).thenReturn(Boolean.FALSE);
        when(ecsPropertiesConfig.getShowRequestLogs()).thenReturn(Boolean.TRUE);
        when(ecsPropertiesConfig.getShowResponseLogs()).thenReturn(Boolean.TRUE);
        when(exchange.getRequest()).thenReturn(request);
        when(request.getURI()).thenReturn(URI.create("/excluded-path"));
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        when(ecsPropertiesConfig.getExcludedPaths()).thenReturn(Set.of("/excluded-path"));
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        webHandler = new ReactiveLogsHandler(ecsPropertiesConfig, messageIdMngUseCase);

        StepVerifier.create(webHandler.filter(exchange, chain))
            .verifyComplete();

        verify(chain).filter(exchange);
    }

    @Test
    void testShouldSkipWhenLogsAreNoShow() {
        when(ecsPropertiesConfig.getShowRequestLogs()).thenReturn(Boolean.FALSE);
        when(ecsPropertiesConfig.getShowResponseLogs()).thenReturn(Boolean.FALSE);

        webHandler = new ReactiveLogsHandler(ecsPropertiesConfig, messageIdMngUseCase);

        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(webHandler.filter(exchange, chain))
            .verifyComplete();

        verify(chain).filter(exchange);
    }

    @Test
    void testShouldSkipRootPath() {
        when(ecsPropertiesConfig.getShowRequestLogs()).thenReturn(Boolean.TRUE);
        when(ecsPropertiesConfig.getShowResponseLogs()).thenReturn(Boolean.TRUE);
        webHandler = new ReactiveLogsHandler(ecsPropertiesConfig, messageIdMngUseCase);

        when(exchange.getRequest()).thenReturn(request);
        when(request.getURI()).thenReturn(URI.create("/"));
        when(request.getHeaders()).thenReturn(new HttpHeaders());

        WebFilterChain mockChain = mock(WebFilterChain.class);
        when(mockChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(webHandler.filter(exchange, mockChain))
            .verifyComplete();

        verify(mockChain).filter(exchange);
    }

    @Test
    void testShouldLogRequestAndResponse() {
        mocksPropertiesConfig();
        URI uri = URI.create("/api/data");
        mockExchange();

        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(request.getURI()).thenReturn(uri);
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        when(request.getMethod()).thenReturn(HttpMethod.GET);

        mockBodyFactory();
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);

        WebFilterChain mockChain = mock(WebFilterChain.class);
        when(mockChain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(webHandler.filter(exchange, mockChain))
            .verifyComplete();
    }

    @Test
    void testShouldHandleErrorAndLogItWithBusinessException() {
        mocksPropertiesConfig();
        URI uri = URI.create("/api/error");

        mockExchange();

        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(request.getURI()).thenReturn(uri);
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        when(request.getMethod()).thenReturn(HttpMethod.GET);

        mockBodyFactory();

        var businessException = new BusinessExceptionECS(ErrorManagement.DEFAULT_EXCEPTION);

        WebFilterChain mockChain = mock(WebFilterChain.class);
        when(mockChain.filter(any())).thenReturn(Mono.error(businessException));

        StepVerifier.create(webHandler.filter(exchange, mockChain))
            .expectError(BusinessExceptionECS.class)
            .verify();
    }

    @Test
    void testShouldHandleGenericException() {
        mocksPropertiesConfig();
        URI uri = URI.create("/api/error");

        mockExchange();

        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(request.getURI()).thenReturn(uri);
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        when(request.getMethod()).thenReturn(HttpMethod.GET);

        mockBodyFactory();

        WebFilterChain mockChain = mock(WebFilterChain.class);
        when(mockChain.filter(any())).thenReturn(Mono.error(new RuntimeException("Generic Error")));

        StepVerifier.create(webHandler.filter(exchange, mockChain))
            .expectError(RuntimeException.class)
            .verify();
    }

    @Test
    void testShouldLogCompletedForbiddenResponseThroughErrorPath(CapturedOutput output) {
        mocksPropertiesConfig();
        URI uri = URI.create("/api/cors");

        mockExchange();

        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(request.getURI()).thenReturn(uri);
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        when(request.getMethod()).thenReturn(HttpMethod.OPTIONS);
        when(response.getStatusCode()).thenReturn(HttpStatus.FORBIDDEN);

        mockBodyFactory();

        WebFilterChain mockChain = mock(WebFilterChain.class);
        when(mockChain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(webHandler.filter(exchange, mockChain))
                .verifyComplete();

        assertTrue(output.getOut().contains("\"uri\":\"/api/cors\""));
        assertTrue(output.getOut().contains("\"responseCode\":\"403\""));
        assertTrue(output.getOut().contains("\"responseResult\":\"Forbidden\""));
        assertTrue(output.getOut().contains("\"level\":\"ERROR\""));
        assertTrue(output.getOut().contains("\"type\":\"org.springframework.web.server.ResponseStatusException\""));
    }

    @Test
    void testShouldPreserveResponseStatusExceptionStatusWhenLoggingErrors(CapturedOutput output) {
        mocksPropertiesConfig();
        URI uri = URI.create("/api/cors");
        ResponseStatusException exception =
                new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid CORS request");

        mockExchange();

        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(request.getURI()).thenReturn(uri);
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        when(request.getMethod()).thenReturn(HttpMethod.OPTIONS);

        mockBodyFactory();

        WebFilterChain mockChain = mock(WebFilterChain.class);
        when(mockChain.filter(any())).thenReturn(Mono.error(exception));

        StepVerifier.create(webHandler.filter(exchange, mockChain))
                .expectErrorMatches(error -> error==exception)
                .verify();

        assertTrue(output.getOut().contains("\"uri\":\"/api/cors\""));
        assertTrue(output.getOut().contains("\"responseCode\":\"403\""));
        assertTrue(output.getOut().contains("\"responseResult\":\"Forbidden\""));
        assertTrue(output.getOut().contains("\"level\":\"ERROR\""));
        assertTrue(output.getOut().contains("\"type\":\"org.springframework.web.server.ResponseStatusException\""));
    }

    @Test
    void testShouldFallbackToInternalServerErrorWhenBusinessStatusIsNull(CapturedOutput output) {
        mocksPropertiesConfig();
        URI uri = URI.create("/api/error");
        BusinessExceptionECS exception = new BusinessExceptionECS(errorManagementWithNullStatus());

        mockExchange();

        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(request.getURI()).thenReturn(uri);
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        when(request.getMethod()).thenReturn(HttpMethod.GET);

        mockBodyFactory();

        WebFilterChain mockChain = mock(WebFilterChain.class);
        when(mockChain.filter(any())).thenReturn(Mono.error(exception));

        StepVerifier.create(webHandler.filter(exchange, mockChain))
                .expectErrorMatches(error -> error==exception)
                .verify();

        assertTrue(output.getOut().contains("\"uri\":\"/api/error\""));
        assertTrue(output.getOut().contains("\"responseCode\":\"500\""));
        assertTrue(output.getOut().contains("\"responseResult\":\"Internal Server Error\""));
        assertTrue(output.getOut().contains("\"level\":\"ERROR\""));
        assertTrue(output.getOut().contains("\"type\":\"CORS-403-00\""));
    }

    @Test
    void testShouldHandlePrintOnErrorWhenLevelMatches() {
        mocksPropertiesConfig(true, true, true, "Exception", "/excluded");
        URI uri = URI.create("/api/error");
        mockExchange();
        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(request.getURI()).thenReturn(uri);
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        mockBodyFactory();

        WebFilterChain mockChain = mock(WebFilterChain.class);
        when(mockChain.filter(any())).thenReturn(Mono.error(new RuntimeException("boom")));

        StepVerifier.create(webHandler.filter(exchange, mockChain))
            .expectError(RuntimeException.class)
            .verify();
    }

    @Test
    void testShouldLogCompletedForbiddenResponseInPrintOnErrorMode(CapturedOutput output) {
        mocksPropertiesConfig(true, true, true, "Exception", "/excluded");
        URI uri = URI.create("/api/cors");

        mockExchange();

        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(request.getURI()).thenReturn(uri);
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        when(request.getMethod()).thenReturn(HttpMethod.OPTIONS);
        when(response.getStatusCode()).thenReturn(HttpStatus.FORBIDDEN);

        mockBodyFactory();

        WebFilterChain mockChain = mock(WebFilterChain.class);
        when(mockChain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(webHandler.filter(exchange, mockChain))
            .verifyComplete();

        assertTrue(output.getOut().contains("\"uri\":\"/api/cors\""));
        assertTrue(output.getOut().contains("\"responseCode\":\"403\""));
        assertTrue(output.getOut().contains("\"responseResult\":\"Forbidden\""));
        assertTrue(output.getOut().contains("\"level\":\"ERROR\""));
        assertTrue(output.getOut().contains("\"type\":\"org.springframework.web.server.ResponseStatusException\""));
    }

    @Test
    void testShouldHandlePrintOnErrorWhenLevelDoesNotMatch() {
        mocksPropertiesConfig(true, true, true, "BusinessExceptionECS", "/excluded");
        URI uri = URI.create("/api/error");
        mockExchange();
        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(request.getURI()).thenReturn(uri);
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        mockBodyFactory();

        WebFilterChain mockChain = mock(WebFilterChain.class);
        when(mockChain.filter(any())).thenReturn(Mono.error(new RuntimeException("boom")));

        StepVerifier.create(webHandler.filter(exchange, mockChain))
            .expectError(RuntimeException.class)
            .verify();
    }

    @Test
    void testShouldHandleNullLevelsFromConfig() {
        when(ecsPropertiesConfig.getShowRequestLogs()).thenReturn(Boolean.FALSE);
        when(ecsPropertiesConfig.getShowResponseLogs()).thenReturn(Boolean.FALSE);
        when(ecsPropertiesConfig.getPrintReqRespOnErrorOnly()).thenReturn(Boolean.FALSE);
        when(ecsPropertiesConfig.getPrintReqRespLevels()).thenReturn(null);

        ReactiveLogsHandler localHandler = new ReactiveLogsHandler(ecsPropertiesConfig, messageIdMngUseCase);
        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(localHandler.filter(exchange, chain))
            .verifyComplete();

        verify(chain).filter(exchange);
    }

    @Test
    void testShouldHandlePrintOnErrorWhenLevelsEmpty() {
        mocksPropertiesConfig(true, true, true, null, "/excluded");
        URI uri = URI.create("/api/error");
        mockExchange();
        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(request.getURI()).thenReturn(uri);
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        mockBodyFactory();

        WebFilterChain mockChain = mock(WebFilterChain.class);
        when(mockChain.filter(any())).thenReturn(Mono.error(new RuntimeException("boom")));

        StepVerifier.create(webHandler.filter(exchange, mockChain))
            .expectError(RuntimeException.class)
            .verify();
    }

    @Test
    void testShouldHandlePrintOnErrorWhenPathExcluded() {
        mocksPropertiesConfig(true, true, true, "Exception", "/api");
        URI uri = URI.create("/api/data");
        when(exchange.getRequest()).thenReturn(request);
        when(request.getURI()).thenReturn(uri);
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(webHandler.filter(exchange, chain))
            .verifyComplete();

        verify(chain).filter(exchange);
    }

    @Test
    void testShouldLogWhenOnlyRequestIsEnabled() {
        mocksPropertiesConfig(null, true, false, null, "");
        URI uri = URI.create("/api/data");
        mockExchange();
        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(request.getURI()).thenReturn(uri);
        when(request.getMethod()).thenReturn(HttpMethod.POST);
        HttpHeaders headers = new HttpHeaders();
        headers.add("message-id", "123");
        when(request.getHeaders()).thenReturn(headers);
        mockBodyFactory();

        WebFilterChain mockChain = mock(WebFilterChain.class);
        when(mockChain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(webHandler.filter(exchange, mockChain))
            .verifyComplete();
    }

    @Test
    void testShouldLogWhenOnlyResponseIsEnabled() {
        mocksPropertiesConfig(null, false, true, null, "");
        URI uri = URI.create("/api/data");
        mockExchange();
        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(request.getURI()).thenReturn(uri);
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        mockBodyFactory();
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);

        WebFilterChain mockChain = mock(WebFilterChain.class);
        when(mockChain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(webHandler.filter(exchange, mockChain))
            .verifyComplete();
    }

    // ── Tests Regla 1: resolveMessageId con flag true/false/null ─────────────

    @Test
    void testShouldNotGenerateUuidWhenFlagIsFalseAndNoHeader() {
        // Bug fix: flag=false NO debe generar UUID (antes con != null lo generaba)
        when(sensitiveRequestProperties.getShow()).thenReturn(Boolean.FALSE);
        when(sensitiveResponseProperties.getShow()).thenReturn(Boolean.FALSE);
        ecsPropertiesConfig = new EcsPropertiesConfig(
                serviceProperties,
                sensitiveRequestProperties,
                sensitiveResponseProperties,
                printOnErrorProperties);
        webHandler = new ReactiveLogsHandler(ecsPropertiesConfig, messageIdMngUseCase);

        when(exchange.getRequest()).thenReturn(request);
        HttpHeaders headers = new HttpHeaders(); // sin message-id
        when(request.getHeaders()).thenReturn(headers);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(webHandler.filter(exchange, chain))
                .verifyComplete();
        // No lanza excepción: la cadena se completa sin generar UUID cuando flag=false
    }

    @Test
    void testShouldGenerateUuidWhenFlagIsNullAndNoHeader() {
        // flag=null → igual que true: debe generar UUID cuando no hay header
        when(sensitiveRequestProperties.getShow()).thenReturn(Boolean.FALSE);
        when(sensitiveResponseProperties.getShow()).thenReturn(Boolean.FALSE);
        ecsPropertiesConfig = new EcsPropertiesConfig(
                serviceProperties,
                sensitiveRequestProperties,
                sensitiveResponseProperties,
                printOnErrorProperties);
        webHandler = new ReactiveLogsHandler(ecsPropertiesConfig, messageIdMngUseCase);

        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        when(messageIdMngUseCase.resolveFromHeaders(any(), any())).thenReturn("auto-generated-uuid");
        java.util.Map<String, Object> attributes = new java.util.HashMap<>();
        when(exchange.getAttributes()).thenReturn(attributes);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(webHandler.filter(exchange, chain))
                .verifyComplete();

        assertTrue(attributes.containsKey("ECS_MESSAGE_ID"), "flag=null debe añadir messageId al contexto del exchange");
    }

    @Test
    void testShouldUseHeaderMessageIdWhenPresent() {
        // Cuando el header trae message-id, siempre se usa (independiente del flag)
        when(serviceProperties.getName()).thenReturn("test-service");
        when(sensitiveRequestProperties.getShow()).thenReturn(Boolean.FALSE);
        when(sensitiveResponseProperties.getShow()).thenReturn(Boolean.FALSE);
        when(printOnErrorProperties.getPrintReqResp()).thenReturn(null);
        when(printOnErrorProperties.getPrintReqRespLevel()).thenReturn(null);

        ecsPropertiesConfig = new EcsPropertiesConfig(
                serviceProperties,
                sensitiveRequestProperties,
                sensitiveResponseProperties,
                printOnErrorProperties);
        webHandler = new ReactiveLogsHandler(ecsPropertiesConfig, messageIdMngUseCase);

        when(exchange.getRequest()).thenReturn(request);
        HttpHeaders headers = new HttpHeaders();
        headers.add("message-id", "header-message-id-456");
        when(request.getHeaders()).thenReturn(headers);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(webHandler.filter(exchange, chain))
                .verifyComplete();
    }

    private void mockBodyFactory() {
        DataBufferFactory bufferFactory = new DefaultDataBufferFactory();
        DataBuffer dataBuffer = bufferFactory.wrap("{\"key\":\"value\"}".getBytes());
        Flux<DataBuffer> requestBody = Flux.just(dataBuffer);
        when(request.getBody()).thenReturn(requestBody);
    }

    private void mockExchange() {
        ServerWebExchange.Builder exchangeBuilder = mock(ServerWebExchange.Builder.class);

        when(exchange.mutate()).thenReturn(exchangeBuilder);
        when(exchangeBuilder.request(any(ServerHttpRequest.class))).thenReturn(exchangeBuilder);
        when(exchangeBuilder.response(any(ServerHttpResponse.class))).thenReturn(exchangeBuilder);
        when(exchangeBuilder.build()).thenReturn(exchange);
    }

    private void mocksPropertiesConfig() {
        mocksPropertiesConfig(null, true, true, null, "");
    }

    private void mocksPropertiesConfig(Boolean printOnError,
                                      boolean showRequest,
                                      boolean showResponse,
                                      String level,
                                      String excludedPaths) {
        when(serviceProperties.getName()).thenReturn("test-service");

        boolean printOnErrorActive = Boolean.TRUE.equals(printOnError);
        when(sensitiveRequestProperties.getShow()).thenReturn(showRequest);
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
        webHandler = new ReactiveLogsHandler(ecsPropertiesConfig, messageIdMngUseCase);
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