package co.com.bancolombia.ecs.application;

import co.com.bancolombia.ecs.domain.model.LogRecord;
import co.com.bancolombia.ecs.helpers.LoggerEcsTestHelper;
import co.com.bancolombia.ecs.helpers.SamplingHelper;
import co.com.bancolombia.ecs.helpers.SensitiveHelper;
import co.com.bancolombia.ecs.infra.config.SamplingConfig;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LoggerEcsTest {

    public static final String SERVICE = "Service";
    public static final String SYSTEM_ERROR = "SystemError";
    public static final String CONNECTION_FAILED = "Connection failed";
    public static final String METHOD = "POST";
    public static final String URI = "/api/v1/logs";

    LogRecord<String, String> logRecord;

    @BeforeEach
    void setup() {
        logRecord = new LogRecord<>();
        logRecord.setService(SERVICE);
        logRecord.setLevel(LogRecord.Level.ERROR);
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        logRecord.setError(LogRecord.ErrorLog.<String, String>builder()
            .type(SYSTEM_ERROR)
            .message(CONNECTION_FAILED)
            .description("Database connection timeout")
            .optionalInfo(map)
            .build()
        );
        logRecord.setMessageId("message-id");
        logRecord.setConsumer("consumer");
        Map<String, String> headers = new HashMap<>();
        headers.put("key", "value");
        Map<String, String> request = new HashMap<>();
        request.put("key", "value");
        Map<String, String> response = new HashMap<>();
        response.put("key", "value");
        logRecord.setAdditionalInfo(LogRecord.AdditionalInfo.<String, String>builder()
            .method(METHOD)
            .uri(URI)
            .headers(headers)
            .requestBody(request)
            .responseBody(response)
            .responseCode("200")
            .responseResult("OK")
            .build()
        );
    }

    @Test
    void testLogPrivateConstructor() throws Exception {
        Constructor<LoggerEcs> constructor = LoggerEcs.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        var state = constructor.newInstance();

        assertNotNull(state);
    }

    @Test
    void testGetRecordValue() {
        assertNotNull(logRecord.getMessageId());
        assertNotNull(logRecord.getDate());
        assertEquals(SERVICE, logRecord.getService());
        assertEquals(LogRecord.Level.ERROR.value(), logRecord.getLevel().value());

        String jsonLog = logRecord.toJson();
        assertTrue(jsonLog.contains(SYSTEM_ERROR));
        assertTrue(jsonLog.contains(CONNECTION_FAILED));
        assertTrue(jsonLog.contains(URI));
        assertTrue(jsonLog.contains(METHOD));
    }

    @Test
    void testPrintAllLogLevelsWithEmptySampling() {
        Logger mockLogger = mock(Logger.class);

        try (MockedStatic<LoggerEcs> mockedLoggerEcs = Mockito.mockStatic(
            LoggerEcs.class, Mockito.CALLS_REAL_METHODS)) {
            mockedLoggerEcs.when(() -> {
                LoggerEcs.print(logRecord);
            }).thenAnswer(invocation -> {
                switch (logRecord.getLevel()) {
                    case DEBUG -> verify(mockLogger).debug(logRecord.toJson());
                    case INFO -> verify(mockLogger).info(logRecord.toJson());
                    case WARNING -> verify(mockLogger).warn(logRecord.toJson());
                    case ERROR -> verify(mockLogger).error(logRecord.toJson());
                    case FATAL -> verify(mockLogger).fatal(logRecord.toJson());
                }
                return null;
            });

            for (LogRecord.Level level : LogRecord.Level.values()) {
                logRecord = LogRecord.<String, String>builder()
                    .level(level)
                    .additionalInfo(LogRecord.AdditionalInfo.<String, String>builder()
                            .method(METHOD)
                            .uri(URI)
                            .responseCode("200")
                            .responseResult("OK")
                            .build())
                    .build();
                LoggerEcs.print(logRecord);
            }
        }
    }

    @Test
    void testLogErrorJsonPrint() {
        class Unserializable {
        }
        var jsonRecord = new LogRecord<String, Object>();
        var additionalInfo = LogRecord.AdditionalInfo.<String, Object>builder()
            .responseBody(Map.of("key", new Unserializable()))
            .build();
        jsonRecord.setAdditionalInfo(additionalInfo);

        var result = jsonRecord.toJson();
        assertNotNull(result);
        assertEquals("{\"error:\" \"json conversion fail\"}", result);
    }

    @ParameterizedTest
    @EnumSource(LogRecord.Level.class)
    void testSamplingLogicAllLevels(LogRecord.Level state) {
        logRecord.setLevel(state);
        SamplingConfig.SamplingRule rule = new SamplingConfig.SamplingRule();
        rule.setShowCount(2);
        rule.setSkipCount(3);
        LoggerEcsTestHelper th = new LoggerEcsTestHelper();

        Map<String, SamplingConfig.SamplingRule> endpoints = new HashMap<>();
        endpoints.put("/api/v1/logs|200", rule);

        SamplingHelper.init(endpoints);

        int totalCalls = 20;
        int printed = 0;

        try (MockedStatic<LoggerEcs> mockedLoggerEcs = Mockito.mockStatic(LoggerEcs.class, Mockito.CALLS_REAL_METHODS)) {
            for (int i = 0; i < totalCalls; i++) {
                LoggerEcs.print(logRecord);
            }

            for (int i = 0; i < totalCalls; i++) {
                if (th.shouldPrint(i, rule.getShowCount(), rule.getSkipCount())) {
                    printed++;
                }
            }
        }

        assertEquals(8, printed, "Debe imprimir exactamente 8 logs en 20 intentos " +
                "imprime 4 cada 10 intentos");
    }

    @Test
    void shouldFilterSensitiveDataWhenUriIsPresent() {
        var logRecordSensitive = LoggerEcsTestHelper.generateTestLogRecord();
        logRecordSensitive.getAdditionalInfo().setUri("/test/endpoint");

        String originalJson = logRecordSensitive.toJson();

        try (MockedStatic<SamplingHelper> mockedSampling = Mockito.mockStatic(SamplingHelper.class);
             MockedStatic<SensitiveHelper> mockedSensitive = Mockito.mockStatic(SensitiveHelper.class)) {

            mockedSampling.when(() -> SamplingHelper.validatePrint(logRecordSensitive)).thenReturn(Boolean.TRUE);
            mockedSensitive.when(() -> SensitiveHelper.filterSensitiveData(originalJson, "/test/endpoint"))
                    .thenReturn("{\"filtered\":\"data\"}");

            LoggerEcs.print(logRecordSensitive);

            mockedSensitive.verify(() -> SensitiveHelper.filterSensitiveData(originalJson, "/test/endpoint"));
        }
    }

    @Test
    void shouldNotFilterSensitiveDataWhenAdditionalInfoIsNull() {
        var logRecordSensitive = LoggerEcsTestHelper.generateTestLogRecord();
        logRecordSensitive.setAdditionalInfo(null);

        String originalJson = logRecordSensitive.toJson();

        try (MockedStatic<SamplingHelper> mockedSampling = Mockito.mockStatic(SamplingHelper.class);
             MockedStatic<SensitiveHelper> mockedSensitive = Mockito.mockStatic(SensitiveHelper.class)) {

            mockedSampling.when(() -> SamplingHelper.validatePrint(logRecordSensitive)).thenReturn(Boolean.TRUE);

            LoggerEcs.print(logRecordSensitive);

            mockedSensitive.verify(() -> SensitiveHelper.filterSensitiveData(originalJson, null), Mockito.never());
        }
    }

    @Test
    void shouldNotFilterSensitiveDataWhenUriIsNull() {
        var logRecordSensitive = LoggerEcsTestHelper.generateTestLogRecord();
        logRecordSensitive.getAdditionalInfo().setUri(null);

        try (MockedStatic<SamplingHelper> mockedSampling = Mockito.mockStatic(SamplingHelper.class);
             MockedStatic<SensitiveHelper> mockedSensitive = Mockito.mockStatic(SensitiveHelper.class)) {

            mockedSampling.when(() -> SamplingHelper.validatePrint(logRecordSensitive)).thenReturn(Boolean.TRUE);

            LoggerEcs.print(logRecordSensitive);

            mockedSensitive.verify(() -> SensitiveHelper.filterSensitiveData(Mockito.anyString(), Mockito.anyString()), Mockito.never());
        }
    }

}