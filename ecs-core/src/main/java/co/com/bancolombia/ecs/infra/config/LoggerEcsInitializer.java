package co.com.bancolombia.ecs.infra.config;

import co.com.bancolombia.ecs.infra.config.managementid.application.MessageIdMngUseCase;
import co.com.bancolombia.ecs.infra.config.managementid.domain.MessageIdRequestProperties;
import co.com.bancolombia.ecs.helpers.SamplingHelper;
import io.micrometer.context.ContextRegistry;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Configuration
@Log4j2
@EnableConfigurationProperties(MessageIdRequestProperties.class)
public class LoggerEcsInitializer {

    private static final String START_CODE_40X = "40";
    private static final String START_CODE_20X = "20";
    private static final String VERSION_MESSAGE = "This application is built with Spring Boot {} and requires Java {}";
    private static final String SB_VERSION = "4.0.2";
    private static final String JAVA_VERSION = "21";
    public static final Pattern ERROR_CODE_REGEX = Pattern.compile("^[\\w\\-]+$");

    public LoggerEcsInitializer(SamplingConfig samplingConfig,
                                PrintOnErrorProperties printOnErrorProperties) {
        log.info(VERSION_MESSAGE, SB_VERSION, JAVA_VERSION);

        if (Boolean.TRUE.equals(printOnErrorProperties.getPrintReqResp())) {
            SamplingHelper.setSamplingEnabled(false);
            log.info("print-on-error.print-req-resp is enabled: sampling has been suspended.");
        }

        registerContextEcsAccessor();

        Map<String, SamplingConfig.SamplingRule> rulesMap = buildRulesMap(samplingConfig);
        SamplingHelper.init(rulesMap);
    }

    /**
     * Exposes {@link MessageIdMngUseCase} as a Spring bean so it can be injected
     * into {@code ReactiveLogsHandler} and any other Spring-managed component.
     * The use case constructor also publishes the flag to {@link co.com.bancolombia.ecs.infra.shared.common.domain.ContextECS}
     * for use by non-Spring domain objects (middleware chain of responsibility).
     */
    @Bean
    public MessageIdMngUseCase messageIdMngUseCase(MessageIdRequestProperties messageIdRequestProperties) {
        return new MessageIdMngUseCase(messageIdRequestProperties);
    }

    private void registerContextEcsAccessor() {
        ContextRegistry.getInstance().registerThreadLocalAccessor(new EcsContextAccessor());
        Hooks.enableAutomaticContextPropagation();
        log.info("ContextECS ThreadLocalAccessor registrado y propagación automática de Reactor Context habilitada: " +
                "ContextECS.getMessageId() está disponible en cualquier hilo del pipeline reactivo.");
    }

    private Map<String, SamplingConfig.SamplingRule> buildRulesMap(SamplingConfig samplingConfig) {
        if (samplingConfig.getRules() != null && !samplingConfig.getRules().isEmpty()) {
            validateRules(samplingConfig);
            return samplingConfig.getRules().stream()
                    .flatMap(rule -> expandRule(rule).entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } else {
            return Map.of();
        }
    }

    private Map<String, SamplingConfig.SamplingRule> expandRule(SamplingConfig.SamplingRule rule) {
        if (rule.getResponseCode().startsWith(START_CODE_40X)) {
            return Arrays.stream(rule.getErrorCodes().split("\\|"))
                    .map(String::trim)
                    .collect(Collectors.toMap(
                            errorCode -> rule.getUri() + "|" + errorCode,
                            errorCode -> rule
                    ));
        } else {
            String key = rule.getUri() + "|" + rule.getResponseCode();
            return Map.of(key, rule);
        }
    }

    private void validateRules(SamplingConfig samplingConfig) {
        samplingConfig.getRules().forEach(rule -> {
            validateNoErrorCodesFor20X(rule);
            validateErrorCodesRequiredFor40X(rule);
            validateErrorCodesFormat(rule);
        });
    }

    private void validateNoErrorCodesFor20X(SamplingConfig.SamplingRule rule) {
        boolean is2xx = rule.getResponseCode().startsWith(START_CODE_20X);
        if (is2xx && rule.getErrorCodes() != null && !rule.getErrorCodes().isBlank()) {
            throw new IllegalArgumentException(String.format(
                    "The rule with URI [%s] and code [%s] must not have errorCodes configured",
                    rule.getUri(), rule.getResponseCode()
            ));
        }
    }

    private void validateErrorCodesRequiredFor40X(SamplingConfig.SamplingRule rule) {
        if (rule.getResponseCode().startsWith(START_CODE_40X)
                && (rule.getErrorCodes() == null || rule.getErrorCodes().isBlank())) {
            throw new IllegalArgumentException(String.format(
                    "The rule with URI [%s] and code [%s] must have errorCodes configured",
                    rule.getUri(), rule.getResponseCode()
            ));
        }
    }

    private void validateErrorCodesFormat(SamplingConfig.SamplingRule rule) {
        if (rule.getErrorCodes() != null && !rule.getErrorCodes().isBlank()) {
            String[] codes = rule.getErrorCodes().trim().split("\\|");
            for (String code : codes) {
                if (code.isBlank() || !ERROR_CODE_REGEX.matcher(code).matches()) {
                    throw new IllegalArgumentException(String.format(
                            "The rule with URI [%s] has an invalid error code: [%s]. " +
                                    "Expected format: code1|code2|...|codeN with alphanumeric codes and hyphens only.",
                            rule.getUri(), code
                    ));
                }
            }
        }
    }
}
