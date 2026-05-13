package co.com.bancolombia.ecs.application.handler;

import co.com.bancolombia.ecs.infra.shared.common.domain.ContextECS;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

/**
 * Restaura el {@code messageId} en el ThreadLocal antes de que el
 * {@code WebExceptionHandler} de la aplicación procese el error.
 *
 * <p>{@code AbstractErrorWebExceptionHandler} crea un pipeline reactivo nuevo
 * que no hereda el Reactor Context del {@code WebFilter}. Este handler lee el
 * messageId almacenado en los atributos del {@link ServerWebExchange} por
 * {@code ReactiveLogsHandler} y lo restaura en {@link ContextECS} de forma
 * sincrónica, garantizando que {@code ContextECS.getMessageId()} retorne el
 * valor correcto dentro de cualquier {@code WebExceptionHandler} posterior.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class EcsErrorContextHandler implements WebExceptionHandler {

    @Override
    @NonNull
    public Mono<Void> handle(@NonNull ServerWebExchange exchange, @NonNull Throwable ex) {
        String messageId = exchange.getAttribute(ContextECS.KEY_MESSAGE_ID);
        if (messageId != null) {
            ContextECS.setMessageId(messageId);
        }
        return Mono.error(ex);
    }
}
