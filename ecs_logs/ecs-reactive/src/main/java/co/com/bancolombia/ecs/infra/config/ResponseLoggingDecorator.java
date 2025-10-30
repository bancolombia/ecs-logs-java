package co.com.bancolombia.ecs.infra.config;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ResponseLoggingDecorator extends ServerHttpResponseDecorator {

    private final List<byte[]> cacheBody = new ArrayList<>();
    private final DataBufferFactory bufferFactory;

    public ResponseLoggingDecorator(ServerHttpResponse delegate, DataBufferFactory bufferFactory) {
        super(delegate);
        this.bufferFactory = bufferFactory;
    }

    @NonNull
    @Override
    public Mono<Void> writeWith(@NonNull Publisher<? extends DataBuffer> body) {
        Flux<DataBuffer> flux = Flux.from(body);

        Flux<DataBuffer> cachedFlux = flux.map(originalBuffer -> {
            var bytes = new byte[originalBuffer.readableByteCount()];
            originalBuffer.read(bytes);
            cacheBody.add(bytes);

            DataBufferUtils.release(originalBuffer);
            return bufferFactory.wrap(bytes);
        });

        return super.writeWith(cachedFlux);
    }

    public String getBodyAsString() {
        return cacheBody.stream()
            .map(chunk -> new String(chunk, StandardCharsets.UTF_8))
            .collect(Collectors.joining());
    }
}