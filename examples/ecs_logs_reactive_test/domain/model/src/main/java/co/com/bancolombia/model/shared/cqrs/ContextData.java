package co.com.bancolombia.model.shared.cqrs;

import co.com.bancolombia.model.shared.common.value.MessageId;
import co.com.bancolombia.model.shared.common.value.XRequestId;
import co.com.bancolombia.model.shared.common.model.ConsumerChannel;
import co.com.bancolombia.model.shared.common.value.Acronym;
import lombok.Data;

@Data
public class ContextData {
    private final MessageId messageId;
    private final XRequestId xRequestId;
    private ConsumerChannel consumerChannel;

    public ContextData(String messageId, String xRequestId, String consumerAcronym) {
        this.messageId = new MessageId(messageId);
        this.consumerChannel = ConsumerChannel.builder().acronym(
                new Acronym(consumerAcronym, this.getClass().getName())).build();
        this.xRequestId = resolveXRequestId(messageId, xRequestId);
    }

    private XRequestId resolveXRequestId(String messageId, String xRequestId) {
        if (xRequestId != null && !xRequestId.isBlank()) {
            return new XRequestId(xRequestId);
        }
        return new XRequestId(messageId);
    }
}
