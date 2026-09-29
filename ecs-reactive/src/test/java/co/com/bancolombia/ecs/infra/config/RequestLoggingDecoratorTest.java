package co.com.bancolombia.ecs.infra.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

    private void givenContentType(MediaType mediaType) {
        HttpHeaders headers = new HttpHeaders();
        if (mediaType != null) {
            headers.setContentType(mediaType);
        }
        Mockito.when(mockRequest.getHeaders()).thenReturn(headers);
    }

    private DataBuffer bufferOf() {
        return bufferFactory.wrap(RequestLoggingDecoratorTest.BODY.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void testShouldCacheBodyAndReturnSameFlux() {
        givenContentType(MediaType.APPLICATION_JSON);
        Mockito.when(mockRequest.getBody()).thenReturn(Flux.just(bufferOf()));

        RequestLoggingDecorator decorator = new RequestLoggingDecorator(mockRequest, bufferFactory);

        StepVerifier.create(decorator.getBody())
            .consumeNextWith(dataBuffer -> {
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                assertEquals(BODY, new String(bytes, StandardCharsets.UTF_8));
            })
            .verifyComplete();

        assertEquals(BODY, decorator.getBodyAsString());
    }

    @Test
    void testShouldReturnEmptyBodyWhenOriginalBodyIsEmpty() {
        givenContentType(MediaType.APPLICATION_JSON);
        Mockito.when(mockRequest.getBody()).thenReturn(Flux.empty());

        RequestLoggingDecorator decorator = new RequestLoggingDecorator(mockRequest, bufferFactory);

        StepVerifier.create(decorator.getBody())
            .expectComplete()
            .verify();

        assertEquals("", decorator.getBodyAsString());
    }

    @Test
    void testShouldReturnSameCachedBodyMultipleTimes() {
        givenContentType(MediaType.APPLICATION_JSON);
        Mockito.when(mockRequest.getBody()).thenReturn(Flux.just(bufferOf()));

        RequestLoggingDecorator decorator = new RequestLoggingDecorator(mockRequest, bufferFactory);

        StepVerifier.create(decorator.getBody()).expectNextCount(1).verifyComplete();
        StepVerifier.create(decorator.getBody()).expectNextCount(1).verifyComplete();
        assertEquals(BODY, decorator.getBodyAsString());
    }


    @Test
    void testApplicationXmlCachesBody() {
        givenContentType(MediaType.APPLICATION_XML);
        Mockito.when(mockRequest.getBody()).thenReturn(Flux.just(bufferOf()));

        RequestLoggingDecorator decorator = new RequestLoggingDecorator(mockRequest, bufferFactory);

        assertEquals(BODY, decorator.getBodyAsString());
    }

    @Test
    void testTextPlainCachesBody() {
        givenContentType(MediaType.TEXT_PLAIN);
        Mockito.when(mockRequest.getBody()).thenReturn(Flux.just(bufferOf()));

        RequestLoggingDecorator decorator = new RequestLoggingDecorator(mockRequest, bufferFactory);

        assertEquals(BODY, decorator.getBodyAsString());
    }

    @Test
    void testFormUrlencodedCachesBody() {
        givenContentType(MediaType.APPLICATION_FORM_URLENCODED);
        Mockito.when(mockRequest.getBody()).thenReturn(Flux.just(bufferOf()));

        RequestLoggingDecorator decorator = new RequestLoggingDecorator(mockRequest, bufferFactory);

        assertEquals(BODY, decorator.getBodyAsString());
    }

    @Test
    void testCustomJsonSubtypeCachesBody() {
        givenContentType(MediaType.parseMediaType("application/vnd.api+json"));
        Mockito.when(mockRequest.getBody()).thenReturn(Flux.just(bufferOf()));

        RequestLoggingDecorator decorator = new RequestLoggingDecorator(mockRequest, bufferFactory);

        assertEquals(BODY, decorator.getBodyAsString());
    }

    @Test
    void testCustomXmlSubtypeCachesBody() {
        givenContentType(MediaType.parseMediaType("application/atom+xml"));
        Mockito.when(mockRequest.getBody()).thenReturn(Flux.just(bufferOf()));

        RequestLoggingDecorator decorator = new RequestLoggingDecorator(mockRequest, bufferFactory);

        assertEquals(BODY, decorator.getBodyAsString());
    }


    @Test
    void testMultipartFormDataPassesThroughBody() {
        givenContentType(MediaType.MULTIPART_FORM_DATA);
        DataBuffer original = bufferOf();
        Mockito.when(mockRequest.getBody()).thenReturn(Flux.just(original));

        RequestLoggingDecorator decorator = new RequestLoggingDecorator(mockRequest, bufferFactory);

        StepVerifier.create(decorator.getBody()).expectNextCount(1).verifyComplete();
        assertEquals("", decorator.getBodyAsString());
    }

    @Test
    void testOctetStreamPassesThroughBody() {
        givenContentType(MediaType.APPLICATION_OCTET_STREAM);
        Mockito.when(mockRequest.getBody()).thenReturn(Flux.just(bufferOf()));

        RequestLoggingDecorator decorator = new RequestLoggingDecorator(mockRequest, bufferFactory);

        StepVerifier.create(decorator.getBody()).expectNextCount(1).verifyComplete();
        assertEquals("", decorator.getBodyAsString());
    }

    @Test
    void testImagePngPassesThroughBody() {
        givenContentType(MediaType.IMAGE_PNG);
        Mockito.when(mockRequest.getBody()).thenReturn(Flux.just(bufferOf()));

        RequestLoggingDecorator decorator = new RequestLoggingDecorator(mockRequest, bufferFactory);

        StepVerifier.create(decorator.getBody()).expectNextCount(1).verifyComplete();
        assertEquals("", decorator.getBodyAsString());
    }

    @Test
    void testNullContentTypePassesThroughBody() {
        givenContentType(null);
        Mockito.when(mockRequest.getBody()).thenReturn(Flux.just(bufferOf()));

        RequestLoggingDecorator decorator = new RequestLoggingDecorator(mockRequest, bufferFactory);

        StepVerifier.create(decorator.getBody()).expectNextCount(1).verifyComplete();
        assertEquals("", decorator.getBodyAsString());
    }

}