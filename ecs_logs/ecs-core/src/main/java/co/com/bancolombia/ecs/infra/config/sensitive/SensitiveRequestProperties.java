package co.com.bancolombia.ecs.infra.config.sensitive;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "adapter.ecs.logs.request")
public class SensitiveRequestProperties {
    private String allowHeaders;
    private String fields;
    private String patterns;
    private String replacement;
    private String delimiter;
    private String excludedPaths;
    private Boolean show;
}
