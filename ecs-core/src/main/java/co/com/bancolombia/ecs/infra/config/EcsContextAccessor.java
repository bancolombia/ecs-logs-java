package co.com.bancolombia.ecs.infra.config;

import co.com.bancolombia.ecs.infra.shared.common.domain.ContextECS;
import io.micrometer.context.ThreadLocalAccessor;


public class EcsContextAccessor implements ThreadLocalAccessor<String> {

    @Override
    public Object key() {
        return ContextECS.KEY_MESSAGE_ID;
    }

    @Override
    public String getValue() {
        return ContextECS.getMessageId();
    }

    @Override
    public void setValue(String value) {
        ContextECS.setMessageId(value);
    }

    @Override
    public void setValue() {
        ContextECS.clear();
    }
}
