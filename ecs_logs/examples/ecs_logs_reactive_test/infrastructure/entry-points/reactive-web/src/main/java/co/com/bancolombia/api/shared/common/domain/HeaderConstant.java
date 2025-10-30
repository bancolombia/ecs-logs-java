package co.com.bancolombia.api.shared.common.domain;

public enum HeaderConstant {
    MESSAGE_ID("message-Id"),
    MESSAGE_ID_LOWER("message-id"),
    CODE("code"),
    AID_CREATOR_LOWER("aid-creator"),
    RELATION_ID("relations-identifier"),
    X_REQUEST_ID("x-request-id");

    private final String headerName;

    HeaderConstant(String headerName) {
        this.headerName = headerName;
    }

    public String value() {
        return headerName;
    }
}
