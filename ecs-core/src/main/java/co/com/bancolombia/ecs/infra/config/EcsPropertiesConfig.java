package co.com.bancolombia.ecs.infra.config;

import co.com.bancolombia.ecs.domain.model.ExceptionLevel;
import co.com.bancolombia.ecs.infra.config.sensitive.SensitiveRequestProperties;
import co.com.bancolombia.ecs.infra.config.sensitive.SensitiveResponseProperties;
import co.com.bancolombia.ecs.infra.config.service.ServiceProperties;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Log4j2
public class EcsPropertiesConfig {
    // Service
    private final String serviceName;
    private final Boolean showRequestLogs;
    private final Boolean showResponseLogs;
    // Request
    private Set<String> allowRequestHeaders;
    private Set<String> sensitiveRequestFields;
    private Set<String> excludedPaths;
    private List<Pattern> sensitiveRequestPatterns;
    private String sensitiveRequestReplacement;
    private String delimiterRequest;
    // Response
    private Set<String> sensitiveResponseFields;
    private List<Pattern> sensitiveResponsePatterns;
    private String sensitiveResponseReplacement;
    private String delimiterResponse;
    // Print request/response only for configured errors
    private final Boolean printReqRespOnErrorOnly;
    private final ExceptionLevel printReqRespLevels;

    public EcsPropertiesConfig(ServiceProperties serviceProperties,
                               SensitiveRequestProperties requestProps,
                               SensitiveResponseProperties responseProps,
                               PrintOnErrorProperties printOnErrorProperties) {

        // Service
        this.serviceName = serviceProperties.getName();

        // Print request/response on error
        this.printReqRespOnErrorOnly = printOnErrorProperties != null
                ? printOnErrorProperties.getPrintReqResp()
                : null;
        this.printReqRespLevels =
                printOnErrorProperties != null && printOnErrorProperties.getPrintReqRespLevel() != null
                        ? printOnErrorProperties.getPrintReqRespLevel()
                        : null;

        boolean printReqRespActive = Boolean.TRUE.equals(this.printReqRespOnErrorOnly);

        // When print-on-error.print-req-resp is active, override show flags to false
        this.showRequestLogs = printReqRespActive ? Boolean.FALSE : requestProps.getShow();
        this.showResponseLogs = printReqRespActive ? Boolean.FALSE : responseProps.getShow();
        log.info("print-on-error.print-req-resp is enabled: request.show and response.show are overridden");

        // Request
        if (Boolean.TRUE.equals(requestProps.getShow()) || printReqRespActive) {
            this.delimiterRequest = requestProps.getDelimiter();
            this.allowRequestHeaders = splitToSet(requestProps.getAllowHeaders(), delimiterRequest);
            this.sensitiveRequestFields = splitToSet(requestProps.getFields(), delimiterRequest);
            this.excludedPaths = splitToSet(requestProps.getExcludedPaths(), delimiterRequest);
            this.sensitiveRequestPatterns = splitToList(requestProps.getPatterns(), delimiterRequest);
            this.sensitiveRequestReplacement = requestProps.getReplacement();
            requestHeadersAndPathAllowed(requestProps);
        }

        // Response
        if (Boolean.TRUE.equals(responseProps.getShow()) || printReqRespActive) {
            setRequestDefault(requestProps);
            this.delimiterResponse = responseProps.getDelimiter();
            this.sensitiveResponseFields = splitToSet(responseProps.getFields(), delimiterResponse);
            this.sensitiveResponsePatterns = splitToList(responseProps.getPatterns(), delimiterResponse);
            this.sensitiveResponseReplacement = responseProps.getReplacement();
        }
    }

    private void setRequestDefault(SensitiveRequestProperties requestProps) {
        requestHeadersAndPathAllowed(requestProps);
    }

    private void requestHeadersAndPathAllowed(SensitiveRequestProperties requestProps) {
        if (Objects.isNull(requestProps.getExcludedPaths()) || requestProps.getExcludedPaths().isEmpty()) {
            this.excludedPaths = Set.of("/actuator");
        }
        if (Objects.isNull(requestProps.getAllowHeaders()) || requestProps.getAllowHeaders().isEmpty()) {
            this.allowRequestHeaders = Set.of("message-id");
        }
    }

    private Set<String> splitToSet(String input, String delimiter) {
        if (isNotNullAndNotEmpty(input, delimiter)) {
            return Stream.of(input.split(delimiter))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private List<Pattern> splitToList(String input, String delimiter) {
        if (isNotNullAndNotEmpty(input, delimiter)) {
            return Stream.of(input.split(delimiter))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Pattern::compile)
                .toList();
        }
        return Collections.emptyList();
    }

    private boolean isNotNullAndNotEmpty(String input, String delimiter) {
        return input != null && !input.isEmpty() && delimiter != null && !delimiter.isEmpty();
    }
}
