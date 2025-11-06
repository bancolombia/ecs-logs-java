package co.com.bancolombia.ecs.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LogRequest {
    private String messageId;
    private String consumer;
    private String method;
    private String url;
    private Throwable error;
    private Map<String, String> headers;
    private Map<String, String> requestBody;
    private Map<String, String> responseBody;
    private String responseResult;
    private String responseCode;
}

