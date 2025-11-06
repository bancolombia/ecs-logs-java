package co.com.bancolombia.model.shared.common.model;

import co.com.bancolombia.model.shared.common.value.Acronym;

import co.com.bancolombia.model.shared.common.value.Constants;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConsumerChannel {
    private final Acronym acronym;
    private final int segment;
    private final String status;

    public String getTransformedChannel() {
        return hasSegment() ? Constants.NULL : acronym.getValue();
    }

    public String getTransformedChannelGroup() {
        return hasSegment() ? String.valueOf(segment) : Constants.NULL;
    }

    private boolean hasSegment() {
        return segment != 0;
    }
}
