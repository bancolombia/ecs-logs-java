package co.com.bancolombia.api.shared.common.domain.response;

import co.com.bancolombia.model.shared.cqrs.ContextData;
import lombok.Data;

@Data
public class SuccessApiResponse {
    private Meta meta;
    private Object data;

    private SuccessApiResponse(Meta meta, Object data) {
        this.meta = meta;
        this.data = data;
    }

    public static SuccessApiResponse build(Object data, ContextData contextData) {
        return new SuccessApiResponse(new Meta(contextData.getMessageId().getValue().toString()), data);
    }
}