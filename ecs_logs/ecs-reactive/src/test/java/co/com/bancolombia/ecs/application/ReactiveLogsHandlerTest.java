package co.com.bancolombia.ecs.application;

import co.com.bancolombia.ecs.application.filter.ReactiveLogsHandler;
import co.com.bancolombia.ecs.infra.config.EcsPropertiesConfig;
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
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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

    @InjectMocks
    private ReactiveLogsHandler webHandler;

    @BeforeEach
    void setUp() {
        webHandler = new ReactiveLogsHandler(ecsPropertiesConfig);
    }

    @Test
    void testShouldSkipExcludedPaths() {
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(webHandler.filter(exchange, chain))
            .verifyComplete();

        verify(chain).filter(exchange);
    }

    @Test
    void testShouldSkipWhenLogsAreNoShow() {
        when(ecsPropertiesConfig.getShowRequestLogs()).thenReturn(Boolean.FALSE);
        when(ecsPropertiesConfig.getShowResponseLogs()).thenReturn(Boolean.FALSE);

        webHandler = new ReactiveLogsHandler(ecsPropertiesConfig);

        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(webHandler.filter(exchange, chain))
            .verifyComplete();

        verify(chain).filter(exchange);
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
        when(response.getStatusCode()).thenReturn(HttpStatusCode.valueOf(200));

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
        when(serviceProperties.getName()).thenReturn("test-service");

        when(sensitiveRequestProperties.getDelimiter()).thenReturn("\\|");
        when(sensitiveRequestProperties.getShow()).thenReturn(Boolean.TRUE);
        when(sensitiveRequestProperties.getAllowHeaders()).thenReturn("message-id|code|channel|acronym-channel");
        when(sensitiveRequestProperties.getFields()).thenReturn("");
        when(sensitiveRequestProperties.getExcludedPaths()).thenReturn("");
        when(sensitiveRequestProperties.getPatterns()).thenReturn("");
        when(sensitiveRequestProperties.getReplacement()).thenReturn("*****");

        when(sensitiveResponseProperties.getDelimiter()).thenReturn("\\|");
        when(sensitiveResponseProperties.getShow()).thenReturn(Boolean.TRUE);
        when(sensitiveResponseProperties.getFields()).thenReturn("");
        when(sensitiveResponseProperties.getPatterns()).thenReturn("");
        when(sensitiveResponseProperties.getReplacement()).thenReturn("*****");

        ecsPropertiesConfig = new EcsPropertiesConfig(serviceProperties, sensitiveRequestProperties,
            sensitiveResponseProperties);
        webHandler = new ReactiveLogsHandler(ecsPropertiesConfig);
    }

}