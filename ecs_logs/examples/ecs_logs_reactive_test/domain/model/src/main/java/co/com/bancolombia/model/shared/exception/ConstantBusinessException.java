package co.com.bancolombia.model.shared.exception;

import co.com.bancolombia.ecs.model.management.ErrorManagement;

import static java.net.HttpURLConnection.*;

/**
 * @Comment
 * En la clase de constantes de las excepciones ejemplo
 * ConstantBusinessException debe implementar la interfaz ErrorManagement del modelo de la librería.
 */
public enum ConstantBusinessException implements ErrorManagement {
    MISSING_HEADER_SEARCH_CLIENT_PARAMETERS(HTTP_BAD_REQUEST,
            CodeMessage.INVALID_HEADER_DETAIL,
            BusinessCode.INVALID_HEADER_CODE,
            InternalMessage.MISSING_HEADER_UPDATE_PARAMETERS,
            CodeLog.SAER400_01_64),
    INVALID_HEADERS_SEARCH_PARAMETERS(HTTP_BAD_REQUEST,
            CodeMessage.INVALID_DATA_DETAIL,
            BusinessCode.INVALID_DATA_CODE,
            InternalMessage.INVALID_HEADER_SEARCH_PARAMETERS,
            CodeLog.SAER400_36_217),
    DEFAULT_EXCEPTION(HTTP_INTERNAL_ERROR,
            CodeMessage.DEFAULT_MESSAGE,
            BusinessCode.INTERNAL_ERROR_CODE,
            InternalMessage.TECHNICAL_ERROR,
            CodeLog.LOG404_00),
    DOCUMENT_NUMBER_NULL(HTTP_BAD_REQUEST,
            CodeMessage.MISSING_PARAMS_BODY,
            BusinessCode.MISSING_PARAMS_CODE,
            InternalMessage.DOCUMENT_NUMBER_NULL,
            CodeLog.SAER400_03_105),
    ACRONYM_NULL(HTTP_BAD_REQUEST,
            CodeMessage.MISSING_PARAMS_BODY,
            BusinessCode.MISSING_PARAMS_CODE,
            InternalMessage.ACRONYM_NULL,
            CodeLog.SAER400_03_108),
    MISSING_SEARCH_CLIENT_PARAMETERS_AGGREGATE(HTTP_BAD_REQUEST,
            CodeMessage.MISSING_PARAMS_BODY,
            BusinessCode.MISSING_PARAMS_CODE,
            InternalMessage.MISSING_PARAMETERS_SEARCH_PARAMETERS_AGGREGATE,
            CodeLog.SAER400_03_132),
    INVALID_DOCUMENT_NUMBER(HTTP_BAD_REQUEST,
            CodeMessage.INVALID_DATA_DETAIL,
            BusinessCode.INVALID_DATA_CODE,
            InternalMessage.INVALID_DOCUMENT_NUMBER,
            CodeLog.SAER400_36_206),
    INVALID_ACRONYM(HTTP_BAD_REQUEST,
            CodeMessage.INVALID_DATA_DETAIL,
            BusinessCode.INVALID_DATA_CODE,
            InternalMessage.INVALID_ALIAS,
            CodeLog.SAER400_36_209),
    MISSING_REQUIRED_DATA_SEARCH_PARAMETERS(HTTP_BAD_REQUEST,
            CodeMessage.MISSING_PARAMS_BODY,
            BusinessCode.MISSING_PARAMS_CODE,
            InternalMessage.MISSING_DATA_RETRIEVE_PARAMETERS,
            CodeLog.SAER400_03_128),
    INVALID_FORMAT_FIELDS_RETRIEVE_PARAMETERS(HTTP_BAD_REQUEST,
            CodeMessage.MISSING_PARAMS_BODY,
            BusinessCode.MISSING_PARAMS_CODE,
            InternalMessage.INVALID_FORMAT_FOR_FIELDS_RETRIEVE_PARAMETERS,
            CodeLog.SAER400_03_129),
    NOT_FOUND_REGISTER_RETRIEVE_PARAM_CLIENT_02(HTTP_CONFLICT,
            CodeMessage.EMPTY_SEARCH_IDENTITY_DETAIL,
            BusinessCode.EMPTY_SEARCH_IDENTITY,
            InternalMessage.NOT_FOUND_REGISTER_RETRIEVE_PARAM_CLIENT_02,
            CodeLog.BPER409_51_23);


    private final Integer status;
    private final String message;
    private final String errorCode;
    private final String internalMessage;
    private final String logCode;

    ConstantBusinessException(Integer status, String message, String errorCode, String internalMessage,
                              String logCode) {
        this.status = status;
        this.message = message;
        this.errorCode = errorCode;
        this.internalMessage = internalMessage;
        this.logCode = logCode;
    }

    public static class CodeMessage {
        public static final String INVALID_HEADER_DETAIL = "Faltan cabeceras obligatorias";
        public static final String MISSING_PARAMS_BODY = "Faltan parámetros obligatorios";
        public static final String INVALID_DATA_DETAIL = "Uno o más datos no poseen un valor válido";
        public static final String EMPTY_SEARCH_IDENTITY_DETAIL = "Registro no encontrado";
        public static final String DEFAULT_MESSAGE = "Ha ocurrido un error interno en el servicio.";
    }

    public static class BusinessCode {
        public static final String INVALID_HEADER_CODE = "SAER400-01";
        public static final String MISSING_PARAMS_CODE = "SAER400-03";
        public static final String INVALID_DATA_CODE = "SAER400-35";
        public static final String EMPTY_SEARCH_IDENTITY = "BPER409-51";
        public static final String INTERNAL_ERROR_CODE = "SA500-01";
    }

    public static class InternalMessage {
        public static final String INVALID_HEADER_SEARCH_PARAMETERS = "Las cabeceras message-id, code" +
                "relations-identifier,aid-creator,x-request-id no poseen un valor valido";
        public static final String MISSING_HEADER_UPDATE_PARAMETERS = "Faltan cabeceras obligatorias message-id, code" +
                ", relations-identifier, aid-creator";
        public static final String TECHNICAL_ERROR = "Error técnico";
        public static final String MISSING_DATA_RETRIEVE_PARAMETERS =
                "El campo data no puede estar vacio en el body de GetClientParametersRequest";
        public static final String MISSING_PARAMETERS_SEARCH_PARAMETERS_AGGREGATE =
                "Campos nulos al construir el agregado SearchClientParameters";
        public static final String DOCUMENT_NUMBER_NULL = "Valor en document number null";
        public static final String ACRONYM_NULL = "Valor en Acronym null";
        public static final String INVALID_FORMAT_FOR_FIELDS_RETRIEVE_PARAMETERS = "Los campos del " +
                "agregado GetClientParametersRequest no poseen un formato valido";
        public static final String INVALID_DOCUMENT_NUMBER =
                "El valor de número de documento es inválido";
        public static final String INVALID_ALIAS =
                "El valor enviado al agregado Alias es invalido";
        public static final String NOT_FOUND_REGISTER_RETRIEVE_PARAM_CLIENT_02 = "La consulta getParametersClientByMdm " +
                "del adaptador SchemaVsClientRepositoryAdapter no retorno registros";
    }

    private static class CodeLog {
        public static final String SAER400_01_64 = "SAER400-01-64";
        public static final String SAER400_03_105 = "SAER400-03-105";
        public static final String SAER400_03_108 = "SAER400-03-108";
        public static final String SAER400_03_128 = "SAER400-03-128";
        public static final String SAER400_03_129 = "SAER400-03-129";
        public static final String SAER400_03_132 = "SAER400-03-132";
        public static final String SAER400_36_206 = "SAER400-35-206";
        public static final String SAER400_36_209 = "SAER400-35-209";
        public static final String SAER400_36_217 = "SAER400-35-217";
        public static final String LOG404_00 = "LOG404-000";
        public static final String BPER409_51_23 = "BPER409-51-23";
    }

    @Override
    public Integer getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getInternalMessage() {
        return internalMessage;
    }

    @Override
    public String getLogCode() {
        return logCode;
    }
}
