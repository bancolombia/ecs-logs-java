package co.com.bancolombia.api.shared.common.application;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;

@UtilityClass
public class HeaderResponse {
    public static HttpHeaders addAdditionalHeaders() {
        var headersResponse = new HttpHeaders();
        headersResponse.set(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        headersResponse.set(HttpHeaders.PRAGMA, "no-cache");
        headersResponse.set(HttpHeaders.EXPIRES, "0");
        return headersResponse;
    }
}
