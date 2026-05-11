package co.com.bancolombia.ecs.infra.config;

import co.com.bancolombia.ecs.domain.model.ExceptionLevel;
import co.com.bancolombia.ecs.model.management.BusinessExceptionECS;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "adapter.ecs.logs.print-on-error")
public class PrintOnErrorProperties {
    private static final String EXCEPTION_LEVEL_PROPERTY_NAME = "adapter.ecs.logs.print-on-error.print-req-resp-level";

    private Boolean printReqResp;
    private ExceptionLevel printReqRespLevel;

    public void setPrintReqRespLevel(String printReqRespLevel) {
        if (printReqRespLevel == null || printReqRespLevel.isBlank()) {
            this.printReqRespLevel = null;
            return;
        }

        String normalizedLevel = printReqRespLevel.trim().toLowerCase();
        var exceptionLevel = ExceptionLevel.toExceptionLevelIgnoreCase(normalizedLevel);
        if (exceptionLevel == null) {
            throw new IllegalArgumentException(String.format(
                    "Valor no permitido para la variable de configuración %s: %s | Valores permitidos: %s.",
                    EXCEPTION_LEVEL_PROPERTY_NAME,
                    printReqRespLevel,
                    Arrays.stream(ExceptionLevel.values())
                            .map(ExceptionLevel::getLevelString)
                            .collect(Collectors.joining(", "))
            ));
        }

        this.printReqRespLevel = exceptionLevel;
    }

    public static boolean matchesConfiguredLevel(Throwable error, ExceptionLevel configuredLevel) {
        if (error == null || configuredLevel == null) {
            return false;
        }
        return switch (configuredLevel) {
            case BUSINESS_EXCEPTION_ECS -> error instanceof BusinessExceptionECS;
            case EXCEPTION -> error instanceof Exception;
            // By definition of method params error is a Throwable
            case THROWABLE -> true;
            default -> false;
        };
    }
}
