package co.com.bancolombia.model.shared.exception;

import co.com.bancolombia.ecs.model.management.BusinessExceptionECS;
import co.com.bancolombia.model.shared.cqrs.ContextData;

/**
 * @Comment
 * En la clase de excepción ejemplo BusinessException
 * o SystemException debe extender de la clase BusinessExceptionECS del modelo de la librería.
 */
public class SystemException extends BusinessExceptionECS {
    public SystemException(ConstantBusinessException message, ContextData contextData) {
        super(message, MetaInfo.builder().messageId(contextData.getMessageId().getValue().toString()).build());
    }
}
