package co.com.bancolombia.ecs.infra.config.managementid.application;

import co.com.bancolombia.ecs.helpers.DataSanitizer;
import co.com.bancolombia.ecs.infra.config.managementid.domain.MessageIdRequestProperties;
import co.com.bancolombia.ecs.infra.shared.common.domain.ContextECS;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Log4j2
public class MessageIdMngUseCase {

    private static volatile Boolean enabledFlag;
    private static final String HEADER_NAME = "message-id";

    public MessageIdMngUseCase(MessageIdRequestProperties messageIdRequestProperties) {
        enabledFlag = messageIdRequestProperties.getEnabled();
        if (Boolean.TRUE.equals(enabledFlag)) {
            log.info("message-id: UUID will be auto-generated on every request without message-id header.");
        } else if (Boolean.FALSE.equals(enabledFlag)) {
            log.info("message-id: UUID will be generated for exceptions that lack a message-id.");
        } else {
            log.info("message-id: auto-generation disabled. Library behaves as before.");
        }
    }


    /**
     * Extrae y normaliza el header message-id desde un mapa de headers HTTP.
     * Soporta variantes como {@code message-id}, {@code messageId}, {@code Message-Id}, etc.
     * Acepta directamente {@code HttpHeaders} (Spring Reactive/MVC) o cualquier
     * {@code Map<String, List<String>>} sin conversión en el caller.
     * Delega en {@link #resolveFromRequestEnvironment(String)} para aplicar la lógica del flag.
     *
     * @param headers      mapa de headers crudos del request; {@code null} equivale a sin headers
     * @param allowedHeaders set de headers permitidos configurado en {@link co.com.bancolombia.ecs.infra.config.EcsPropertiesConfig}
     */
    public String resolveFromHeaders(Set<Map.Entry<String, List<String>>> headers, Set<String> allowedHeaders) {
        if (headers == null || headers.isEmpty()) {
            return resolveFromRequestEnvironment(null);
        }
        Map<String, String> normalized = DataSanitizer.sanitizeHeaders(headers, allowedHeaders);
        return resolveFromRequestEnvironment(normalized.get(HEADER_NAME));
    }

    /**
     * Resuelve el messageId para una petición entrante a partir del valor raw del header.
     * <ul>
     *   <li>{@code true}  – genera UUID si el header está ausente o en blanco.</li>
     *   <li>{@code true/null} – genera UUID si el header está ausente o en blanco.</li>
     *   <li>{@code false}      – devuelve el header tal como llega; {@code null} si no hay header.</li>
     * </ul>
     */
    public String resolveFromRequestEnvironment(String incomingHeaderValue) {
        if (!Boolean.FALSE.equals(enabledFlag)) {
            return Optional.ofNullable(incomingHeaderValue)
                    .filter(Predicate.not(String::isBlank))
                    .orElseGet(() -> UUID.randomUUID().toString());
        }
        return (incomingHeaderValue != null && !incomingHeaderValue.isBlank())
                ? incomingHeaderValue
                : null;
    }

    /**
     * Resuelve el messageId para una excepción.
     * <ul>
     *   <li>Si alguno de los candidatos (ctx, meta) es no vacío, lo retorna.</li>
     *   <li>Si ninguno tiene valor, genera siempre un UUID para garantizar trazabilidad,
     *       independientemente del valor de {@code enable_auto_register_message_id}.</li>
     * </ul>
     */
    public static String resolveForException(String metaInfoMessageId) {
        String contextMessageId = MessageIdMngUseCase.getFromContext().orElse(null);
        String resolved = Stream.of(contextMessageId, metaInfoMessageId)
                .filter(s -> s != null && !s.isBlank())
                .findFirst()
                .orElse(null);
        return (resolved != null) ? resolved:
                UUID.randomUUID().toString();
    }


    /**
     * Lee el messageId del contexto (Reactor Context / ThreadLocal).
     * Encapsula el acceso a {@link ContextECS} para que los handlers/middlewares
     * no dependan directamente de la infraestructura de contexto.
     *
     * @return Optional con el messageId si existe y no está en blanco; vacío en caso contrario.
     */
    public static Optional<String> getFromContext() {
        String id = ContextECS.getMessageId();
        return (id != null && !id.isBlank()) ? Optional.of(id) : Optional.empty();
    }
}
