package co.com.bancolombia.ecs.domain.model;

import co.com.bancolombia.ecs.infra.config.SensitiveRulesConfig;
import lombok.Data;

@Data
public class PathContext {
    private final String[] pathParts;
    private final int currentIndex;
    private final SensitiveRulesConfig.SensitiveDataRule rule;

    public PathContext(String[] pathParts, int currentIndex,
                       SensitiveRulesConfig.SensitiveDataRule rule) {
        this.pathParts = pathParts;
        this.currentIndex = currentIndex;
        this.rule = rule;
    }

    public String[] getPathParts() {
        return pathParts;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public SensitiveRulesConfig.SensitiveDataRule getRule() {
        return rule;
    }

    public PathContext next() {
        return new PathContext(pathParts, currentIndex + 1, rule);
    }
}
