package co.com.bancolombia.api.shared.common.domain.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Error {
    private String code;
    private String detail;
}
