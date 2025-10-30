package co.com.bancolombia.model.coustumerservice.client.search.model;


import co.com.bancolombia.model.shared.common.model.Identification;
import lombok.Getter;

@Getter
public class SearchClientIdentification {
    private final Identification identification;

    public SearchClientIdentification(String identificationNumber) {
        this.identification  = new Identification(identificationNumber, this.getClass().getName());
    }
}
