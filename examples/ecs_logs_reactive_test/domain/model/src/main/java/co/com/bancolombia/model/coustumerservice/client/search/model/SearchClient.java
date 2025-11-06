package co.com.bancolombia.model.coustumerservice.client.search.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class SearchClient {
    private String identification;
    private String clientName;
    private String address;
    private String city;
}
