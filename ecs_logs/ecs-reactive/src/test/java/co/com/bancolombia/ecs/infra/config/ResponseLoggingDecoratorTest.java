package co.com.bancolombia.ecs.infra.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResponseLoggingDecoratorTest {

    private static final String BODY = "response-body";
    private DataBufferFactory bufferFactory;
    private ServerHttpResponse mockResponse;

    @BeforeEach
    void setUp() {
        bufferFactory = new DefaultDataBufferFactory();
        mockResponse = Mockito.mock(ServerHttpResponse.class);
        Mockito.when(mockResponse.writeWith(Mockito.any(Publisher.class)))
            .thenAnswer(invocation -> Mono.empty());
    }

    @Test
    void testShouldCacheBodyAndReturnAsString() {
        DataBuffer buffer = bufferFactory.wrap(BODY.getBytes(StandardCharsets.UTF_8));
        var realResponse = new MockServerHttpResponse();
        var decorator = new ResponseLoggingDecorator(realResponse, bufferFactory);

        Mono<Void> result = decorator.writeWith(Flux.just(buffer));

        StepVerifier.create(result)
            .verifyComplete();

        assertEquals(BODY, decorator.getBodyAsString());
    }

    @Test
    void testShouldReturnEmptyStringWhenBodyIsEmpty() {
        ResponseLoggingDecorator decorator = new ResponseLoggingDecorator(mockResponse, bufferFactory);

        Mono<Void> result = decorator.writeWith(Flux.empty());

        StepVerifier.create(result).verifyComplete();
        assertEquals("", decorator.getBodyAsString());
    }

    @Test
    void testShouldHandleMultipleChunks() {
        String part1 = "Hello, ";
        String part2 = "World!";
        DataBuffer buffer1 = bufferFactory.wrap(part1.getBytes(StandardCharsets.UTF_8));
        DataBuffer buffer2 = bufferFactory.wrap(part2.getBytes(StandardCharsets.UTF_8));

        var realResponse = new MockServerHttpResponse();
        var decorator = new ResponseLoggingDecorator(realResponse, bufferFactory);

        Mono<Void> result = decorator.writeWith(Flux.just(buffer1, buffer2));

        StepVerifier.create(result)
            .expectComplete()
            .verify();

        assertEquals(part1 + part2, decorator.getBodyAsString());
    }

    @Test
    void testShouldReturnSameBodyMultipleTimes() {
        DataBuffer buffer = bufferFactory.wrap(BODY.getBytes(StandardCharsets.UTF_8));
        var realResponse = new MockServerHttpResponse();
        var decorator = new ResponseLoggingDecorator(realResponse, bufferFactory);

        StepVerifier.create(decorator.writeWith(Flux.just(buffer)))
            .verifyComplete();

        assertEquals(BODY, decorator.getBodyAsString());
        assertEquals(BODY, decorator.getBodyAsString());
    }

    @Test
    void testShouldNotFailWithNullBytes() {
        DataBuffer buffer = bufferFactory.wrap(new byte[0]);
        ResponseLoggingDecorator decorator = new ResponseLoggingDecorator(mockResponse, bufferFactory);

        Mono<Void> result = decorator.writeWith(Flux.just(buffer));

        StepVerifier.create(result).verifyComplete();
        assertEquals("", decorator.getBodyAsString());
    }
}