package co.com.bancolombia.model.shared.exception;

import co.com.bancolombia.ecs.model.management.BusinessExceptionECS;
import co.com.bancolombia.model.shared.cqrs.ContextData;
import lombok.Getter;

import java.util.Map;

/**
 * @Comment
 * En la clase de excepción ejemplo BusinessException
 * o AppException debe extender de la clase BusinessExceptionECS del modelo de la librería.
 */
@Getter
public class BusinessException extends BusinessExceptionECS {


    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(ConstantBusinessException message) {
        super(message);
    }

    public BusinessException(ConstantBusinessException message, String optionalInfo) {
        super(message, optionalInfo);
    }

    public BusinessException(ConstantBusinessException message, MetaInfo metaInfo) {
        super(message, metaInfo);
    }

    public BusinessException(ConstantBusinessException message, Map<String, String> optionalInfo) {
        super(message, optionalInfo);
    }

    public BusinessException(ConstantBusinessException message, String optionalInfo, MetaInfo metaInfo) {
        super(message, optionalInfo, metaInfo);
    }

    public BusinessException(ConstantBusinessException message, Map<String, String> optionalInfo, MetaInfo metaInfo) {
        super(message, optionalInfo, metaInfo);
    }

    public BusinessException(ConstantBusinessException message, ContextData contextData) {
        super(message, MetaInfo.builder().messageId(contextData.getMessageId().getValue().toString()).build());
    }

    public BusinessException(ConstantBusinessException message, String optionalInfo, ContextData contextData) {
        super(message, optionalInfo, MetaInfo.builder().messageId(contextData.getMessageId().getValue().toString())
                .build());
    }

    public BusinessException(ConstantBusinessException message, Map<String, String> optionalInfo,
                             ContextData contextData) {
        super(message, optionalInfo, MetaInfo.builder().messageId(contextData.getMessageId().getValue().toString())
                .build());
    }
}
