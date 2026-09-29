package co.com.bancolombia.ecs.infra.config;

import lombok.Setter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

@Setter
public class RequestLoggingDecorator extends ServerHttpRequestDecorator {
    private final Flux<DataBuffer> cachedBody;
    private final AtomicReference<String> cachedBodyString = new AtomicReference<>("");

    public RequestLoggingDecorator(ServerHttpRequest delegate, DataBufferFactory bufferFactory) {
        super(delegate);

        var httpHeaders = delegate.getHeaders();
        var contentType = httpHeaders.getContentType();

        if (isBodyReadableAsString(contentType)) {
            Flux<DataBuffer> body = super.getBody();
            Mono<DataBuffer> joined = DataBufferUtils.join(body).cache();

            this.cachedBody = joined.flatMapMany(dataBuffer -> {
                var bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                cachedBodyString.set(new String(bytes, StandardCharsets.UTF_8));
                DataBufferUtils.release(dataBuffer);
                return Flux.just(bufferFactory.wrap(bytes));
            }).cache();

            this.cachedBody.subscribe();
        } else {
            this.cachedBody = super.getBody();
        }
    }


    private static boolean isBodyReadableAsString(MediaType contentType) {
        if (contentType == null) return false;
        String subtype = contentType.getSubtype();
        return "text".equals(contentType.getType())
                || contentType.isCompatibleWith(MediaType.APPLICATION_JSON)
                || contentType.isCompatibleWith(MediaType.APPLICATION_XML)
                || contentType.isCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED)
                || subtype.endsWith("+json")
                || subtype.endsWith("+xml");
    }

    @Override
    @NonNull
    public Flux<DataBuffer> getBody() {
        return cachedBody;
    }

    public String getBodyAsString() {
        return cachedBodyString.get();
    }
}
