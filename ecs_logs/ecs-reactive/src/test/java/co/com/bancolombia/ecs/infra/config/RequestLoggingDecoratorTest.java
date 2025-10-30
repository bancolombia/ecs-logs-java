package co.com.bancolombia.ecs.infra.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequestLoggingDecoratorTest {

    private static final String BODY = "test-body";
    private DataBufferFactory bufferFactory;
    private ServerHttpRequest mockRequest;

    @BeforeEach
    void setUp() {
        bufferFactory = new DefaultDataBufferFactory();
        mockRequest = Mockito.mock(ServerHttpRequest.class);
    }

    @Test
    void testShouldCacheBodyAndReturnSameFlux() {
        DataBuffer originalBuffer = bufferFactory.wrap(BODY.getBytes(StandardCharsets.UTF_8));
        Mockito.when(mockRequest.getBody()).thenReturn(Flux.just(originalBuffer));

        RequestLoggingDecorator decorator = new RequestLoggingDecorator(mockRequest, bufferFactory);

        StepVerifier.create(decorator.getBody())
            .consumeNextWith(dataBuffer -> {
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                String result = new String(bytes, StandardCharsets.UTF_8);
                assertEquals(BODY, result);
            })
            .verifyComplete();

        assertEquals(BODY, decorator.getBodyAsString());
    }

    @Test
    void testShouldReturnEmptyBodyWhenOriginalBodyIsEmpty() {
        Mockito.when(mockRequest.getBody()).thenReturn(Flux.empty());

        RequestLoggingDecorator decorator = new RequestLoggingDecorator(mockRequest, bufferFactory);

        StepVerifier.create(decorator.getBody())
            .expectComplete()
            .verify();

        assertEquals("", decorator.getBodyAsString());
    }

    @Test
    void testShouldReturnSameCachedBodyMultipleTimes() {
        DataBuffer originalBuffer = bufferFactory.wrap(BODY.getBytes(StandardCharsets.UTF_8));
        Mockito.when(mockRequest.getBody()).thenReturn(Flux.just(originalBuffer));

        RequestLoggingDecorator decorator = new RequestLoggingDecorator(mockRequest, bufferFactory);

        StepVerifier.create(decorator.getBody())
            .expectNextCount(1)
            .verifyComplete();

        StepVerifier.create(decorator.getBody())
            .expectNextCount(1)
            .verifyComplete();

        assertEquals(BODY, decorator.getBodyAsString());
    }
}