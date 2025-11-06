package co.com.bancolombia.api.coustumerservice.client.search.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@lombok.Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchClientResponse {
    private String identification;
    private String clientName;
    private String address;
    private String city;
}
