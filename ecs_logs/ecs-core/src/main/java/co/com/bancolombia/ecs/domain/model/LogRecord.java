package co.com.bancolombia.ecs.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogRecord<T, R> {

    public static final String DATE_FORMAT = "dd/MM/yyyy HH:mm:ss:SSSS";

    @Builder.Default
    private String messageId = UUID.randomUUID().toString();
    @Builder.Default
    private String date = currentDate();
    private String service;
    private String consumer;
    private AdditionalInfo<T, R> additionalInfo;
    private Level level;
    private ErrorLog<T, R> error;

    private static String currentDate() {
        var date = LocalDateTime.now(ZoneOffset.of("-05:00"));
        return date.format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    }

    public String toJson() {
        var objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException ex) {
            return "{\"error:\" \"json conversion fail\"}";
        }
    }

    public enum Level {
        DEBUG("DEBUG"),
        INFO("INFO"),
        WARNING("WARNING"),
        ERROR("ERROR"),
        FATAL("FATAL");

        private final String value;

        Level(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ErrorLog<T, R> {
        private String type;
        private String message;
        private String description;
        private Map<T, R> optionalInfo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AdditionalInfo<T, R> {
        private String method;
        private String uri;
        private Map<T, R> headers;
        private Map<T, R> requestBody;
        private Map<T, R> responseBody;
        private String responseResult;
        private String responseCode;
    }
}






