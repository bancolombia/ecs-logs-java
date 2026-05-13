package co.com.bancolombia.ecs.infra.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "adapter.ecs.logs.message-id")
public class MessageIdProperties {

    private final Boolean enableAutoRegisterMessageId;

    public MessageIdProperties(Boolean enableAutoRegisterMessageId) {
        this.enableAutoRegisterMessageId = enableAutoRegisterMessageId;
    }
}
