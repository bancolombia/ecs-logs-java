package co.com.bancolombia.model.shared.common.value;

import lombok.experimental.UtilityClass;

import java.util.Set;

@UtilityClass
public class Constants {

    public static final String ENTITLEMENT = "ENTITLEMENT";
    public static final String APP_NAME = "ClientParametersMs";
    public static final String EMPTY_VALUE = "";

    public static final String NULL = "NULL";

    public static final String USER = "USER";
    public static final String SYSTEM = "SYSTEM";

    public static final String CREATE = "CREATE";
    public static final String UPDATE = "UPDATE";

    public static final String ASSIGN_SCHEME_LOGS_CREATE = "ASIGNAR CONTROL DE SEGURIDAD POR CLIENTE";
    public static final String GET_SCHEME_DESCRIPTION = "CONSULTAR CONTROL DE SEGURIDAD QUE TIENE ASIGNADO UN CLIENTE";
    public static final String CREATE_PARAMETER_CLIENT = "CREAR PARAMETROS PARA EL CLIENTE";
    public static final String UPDATE_CLIENT_PARAMETERS = "MODIFICAR PARAMETROS POR CLIENTE";
    public static final String RETRIEVE_CLIENT_PARAMETERS
            = "CONSULTAR CONTROL DE SEGURIDAD DE UN CLIENTE Y ROLES TITULARES";
    public static final String DELETE_CLIENT_PARAMETERS
            = "ELIMINAR PARAMETROS POR CLIENTE";

    public static final String SUCCESS_CREATED = "Creación Exitosa";
    public static final String SUCCESS_QUERY = "CONSULTA EXITOSA";
    public static final String SUCCESS_UPDATE = "ACTUALIZACIÓN EXITOSA";

    //Date formats
    public static final String OFFSET_ID = "America/Bogota";
    public static final String DATE_TIME_FORMATTER = "dd/MM/yyyy HH:mm:ss:SSSS";

    //REGEX
    public static final String REGEX_DOCUMENT_NUMBER = "^[0-9]{1,15}$";
    public static final String REGEX_ALPHANUMERIC_DOCUMENT_NUMBER = "^[0-9A-Za-z]{1,15}$";

    //DATA ENTITLEMENT PARAMETERS
    public static final String DELEGATE_NUMBER = "delegates_number";
    public static final String EXPIRATION_DAY = "expiration_day";
    public static final String DELEGATE_NUMBER_CHANNEL = "channel_delegates_number";
    public static final String EXPIRATION_DAY_CHANNEL = "channel_expiration_day";

    public static final String STATUS_201_SUCCESS = "201-EXITOSO";
    public static final String STATUS_CONSULT_OK = "200";

    public static final String STATUS_ALIAS = "ACTIVO";

    // CONSTANT FOR TYPE ROLES
    public static final String REPLEGAL = "REPLEGAL";

    public static final String STATUS_200_SUCCESS = "200-EXITOSO";
    public static final String SUCCESS_TRANSACTION_TITLE = "Operación exitosa";

    public static final String STATUS_ACTIVE = "STA01";
    public static final String STATUS_DELETE = "STA03";

    //log
    public static final String SUCCESS_CREATE = "EL REGISTRO SE HA CREADO CORRECTAMENTE";
    public static final String PARAMETER_DELETED_DESCRIPTION = "ELIMINACIÓN DE PARAMETROS CLIENTE";
    public static final String RELATIONSHIP_DELETED_DESCRIPTION = "CAMBIO DE ESTADO DE LA RELACION";
    public static final String SUCCESS_STATUS_MODIFIED = "CAMBIO DE ESTADO EXITOSO";
    public static final String ROLE_DELETED_DESCRIPTION = "CAMBIO DE ESTADO ROL";
    public static final String GROUP_DELETED_DESCRIPTION = "CAMBIO DE ESTADO GP";
    public static final String PARAMETER_DELETED_EVENT_DESCRIPTION = "PUBLICAR ELIMINACIÓN DE CLIENTE";
    public static final String ROLE_DELETED_EVENT_DESCRIPTION = "PUBLICAR CAMBIO DE ESTADO ROL";
    public static final String RELATIONSHIP_DELETED_EVENT_DESCRIPTION = "PUBLICAR CAMBIO DE ESTADO DE LA RELACION";

    // Security Control
    public static final String SCHEMA_DELEGATE = "ESQ0003";

    //State transactions
    public static final int APPROVED_STATE = 4;
    public static final int FINISH_STATE = 6;
    public static final int CANCEL_STATE = 3;
    public static final String FINISH_TRANSACTION = "FINALIZADA";
    public static final String TRANSACTION_APPROVED = "APROBADA";


    public static final String UPDATE_PARAMETERS_TRX_ID = "ENT14";

    //Number
    public static final int ZERO = 0;
    public static final int ONE = 1;
    public static final int TWO = 2;
    public static final int THREE = 3;
    public static final int FOUR = 4;
    public static final int FIVE = 5;
    public static final int SIX = 6;
    public static final int SEVEN = 7;


    //Rollback
    public static final String UPDATE_CLIENT_PARAMETER_ROLLBACK = "ACTUALIZAR PARAMETROS DE UN CLIENTE";

    //NAMES ROLES
    public static final String TITULAR = "TITULAR";
    public static final String REP_LEGAL = "REPLEGAL";

    //Separators
    public static final String SLASH_SEPARATOR = "/";

    //STATE DESCRIPTION
    public static final String STATE_DELETE = "Eliminado";

    //EVENTS AND ROUTING KEYS
    public static final String EVENT = "EVENT";

    // HEADERS
    public static final Set<String> PROXY_HEADERS = Set.of(
            "x-forwarded-",
            "x-envoy-",
            "x-amzn-trace-id",
            "x-request-id",
            "via",
            "server",
            "x-forwarded-for",
            "x-forwarded-proto",
            "x-forwarded-port",
            "x-envoy-external-address",
            "x-envoy-attempt-count",
            "x-envoy-original-path",
            "x-forwarded-client-cert",
            "x-envoy-upstream-service-time",
            "x-envoy-upstream-rq-timeout-ms",
            "x-envoy-upstream-rq-per-try-timeout-ms"
    );

}
