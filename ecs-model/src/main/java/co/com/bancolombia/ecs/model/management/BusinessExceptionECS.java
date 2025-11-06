package co.com.bancolombia.ecs.model.management;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Setter
@Getter
public class BusinessExceptionECS extends RuntimeException {
    public static final String EMPTY_VALUE = "";
    public static final String OPTIONAL = "OPTIONAL";

    private final transient ErrorManagement constantBusinessException;
    private final Map<String, String> optionalInfo;
    private final MetaInfo metaInfo;


    public BusinessExceptionECS(String message) {
        super(message);
        this.metaInfo = MetaInfo.builder().build();
        this.optionalInfo = new HashMap<>();
        this.constantBusinessException = ErrorManagement.DEFAULT_EXCEPTION;

    }

    public BusinessExceptionECS(ErrorManagement message) {
        this(message, EMPTY_VALUE);
    }

    public BusinessExceptionECS(ErrorManagement message, String optionalInfo) {
        super(validateMessage(message).getLogCode());
        this.constantBusinessException = validateMessage(message);
        this.optionalInfo = fillMap(optionalInfo != null
            ? optionalInfo
            : EMPTY_VALUE);
        this.metaInfo = MetaInfo.builder().build();
    }

    public BusinessExceptionECS(ErrorManagement message, MetaInfo metaInfo) {
        super(validateMessage(message).getLogCode());
        this.constantBusinessException = validateMessage(message);
        this.optionalInfo = fillMap(EMPTY_VALUE);
        this.metaInfo = metaInfo;
    }

    public BusinessExceptionECS(ErrorManagement message, Map<String, String> optionalInfo) {
        super(validateMessage(message).getLogCode());
        this.constantBusinessException = validateMessage(message);
        this.optionalInfo = optionalInfo;
        this.metaInfo = MetaInfo.builder().build();
    }

    public BusinessExceptionECS(ErrorManagement message, String optionalInfo, MetaInfo metaInfo) {
        super(validateMessage(message).getLogCode());
        this.constantBusinessException = validateMessage(message);
        this.optionalInfo = fillMap(optionalInfo != null
            ? optionalInfo
            : EMPTY_VALUE);
        this.metaInfo = (metaInfo != null) ? metaInfo : MetaInfo.builder().build();
    }

    public BusinessExceptionECS(ErrorManagement message,
                                Map<String, String> optionalInfo,
                                MetaInfo metaInfo) {
        super(validateMessage(message).getLogCode());
        this.constantBusinessException = validateMessage(message);
        this.optionalInfo = optionalInfo;
        this.metaInfo = (metaInfo != null) ? metaInfo : MetaInfo.builder().build();
    }

    private static ErrorManagement validateMessage(ErrorManagement message) {
        return (message == null)
            ? ErrorManagement.DEFAULT_EXCEPTION
            : message;
    }

    private Map<String, String> fillMap(String value) {
        Map<String, String> optionalInfoMap = new HashMap<>();
        optionalInfoMap.put(OPTIONAL, value);
        return optionalInfoMap;
    }

    @Builder
    @Getter
    public static class MetaInfo implements Serializable {
        @Builder.Default
        private final String messageId = UUID.randomUUID().toString();
    }
}
