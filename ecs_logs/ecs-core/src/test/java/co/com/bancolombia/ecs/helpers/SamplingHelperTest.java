package co.com.bancolombia.ecs.helpers;

import co.com.bancolombia.ecs.domain.model.LogRecord;
import co.com.bancolombia.ecs.infra.config.SamplingConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SamplingHelperTest {

    private LogRecord<String, String> logRecord;
    private LogRecord<String, String> logRecordError;

    @BeforeEach
    void setUp() {
        SamplingHelper.reset();
        logRecord = new LogRecord<>();
        LogRecord.AdditionalInfo<String, String> info = new LogRecord.AdditionalInfo<>();
        info.setUri("/test/endpoint");
        info.setResponseCode("200");
        logRecord.setAdditionalInfo(info);

        logRecordError = new LogRecord<>();
        LogRecord.AdditionalInfo<String, String> infoError = new LogRecord.AdditionalInfo<>();
        infoError.setUri("/test/endpoint");
        infoError.setResponseCode("409");
        logRecordError.setAdditionalInfo(infoError);
        LogRecord.ErrorLog<String, String> errorLog = LogRecord.ErrorLog.<String, String>builder()
                .type("BPER409-52-04")
                .message("Connection failed")
                .description("Database connection timeout")
                .optionalInfo(Map.of("key", "value"))
                .build();
        logRecordError.setError(errorLog);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        SamplingHelper.reset();
    }

    @Test
    void shouldInitRulesCorrectly() {
        Map<String, SamplingConfig.SamplingRule> rulesMap = new HashMap<>();
        SamplingConfig.SamplingRule rule = new SamplingConfig.SamplingRule();
        rule.setShowCount(2);
        rule.setSkipCount(1);
        rulesMap.put("/test/endpoint|200", rule);

        SamplingHelper.init(rulesMap);

        assertTrue(SamplingHelper.validatePrint(logRecord));
    }

    @Test
    void shouldInitWithNullMap() {
        SamplingHelper.init(null);
        assertTrue(SamplingHelper.validatePrint(logRecord));
    }

    @Test
    void shouldReturnFalseWhenAdditionalInfoIsNull() {
        logRecord.setAdditionalInfo(null);
        assertFalse(SamplingHelper.validatePrint(logRecord));
    }

    @Test
    void shouldReturnFalseWhenUriOrResponseCodeIsNull() {
        logRecord.getAdditionalInfo().setUri(null);
        assertFalse(SamplingHelper.validatePrint(logRecord));

        logRecord.getAdditionalInfo().setUri("/test/endpoint");
        logRecord.getAdditionalInfo().setResponseCode(null);
        assertFalse(SamplingHelper.validatePrint(logRecord));
    }

    @Test
    void shouldRespectShowAndSkipRules() {
        SamplingConfig.SamplingRule rule = new SamplingConfig.SamplingRule();
        rule.setShowCount(2);
        rule.setSkipCount(1);

        SamplingHelper.init(Map.of("/test/endpoint|200", rule));

        boolean[] expected = {true, true, false, true, true, false};
        boolean[] actual = new boolean[6];

        for (int i = 0; i < 6; i++) {
            actual[i] = SamplingHelper.validatePrint(logRecord);
        }

        assertArrayEquals(expected, actual);
    }

    @Test
    void shouldRespectShowAndSkipRulesWithResponseCode409() {
        SamplingConfig.SamplingRule rule = new SamplingConfig.SamplingRule();
        rule.setResponseCode("409");
        rule.setErrorCodes("BPER409-52");
        rule.setUri("/test/endpoint");
        rule.setShowCount(2);
        rule.setSkipCount(1);

        SamplingHelper.init(Map.of("/test/endpoint|BPER409-52", rule));

        boolean[] expected = {true, true, false, true, true, false};
        boolean[] actual = new boolean[6];

        for (int i = 0; i < 6; i++) {
            actual[i] = SamplingHelper.validatePrint(logRecordError);
        }

        assertArrayEquals(expected, actual);
    }

    @Test
    void shouldRespectShowsWithResponseCode409() {
        LogRecord.ErrorLog<String, String> errorLog = LogRecord.ErrorLog.<String, String>builder()
                .type("BPER409")
                .build();
        logRecordError.setError(errorLog);
        SamplingConfig.SamplingRule rule = new SamplingConfig.SamplingRule();
        rule.setResponseCode("409");
        rule.setErrorCodes("BPER409-52");
        rule.setUri("/test/endpoint");
        rule.setShowCount(2);
        rule.setSkipCount(1);

        SamplingHelper.init(Map.of("/test/endpoint|BPER409-52", rule));

        boolean[] expected = {true, true, true, true, true, true};
        boolean[] actual = new boolean[6];

        for (int i = 0; i < 6; i++) {
            actual[i] = SamplingHelper.validatePrint(logRecordError);
        }

        assertArrayEquals(expected, actual);
    }

    @Test
    void shouldResetCounterAfterCycle() {
        SamplingConfig.SamplingRule rule = new SamplingConfig.SamplingRule();
        rule.setShowCount(1);
        rule.setSkipCount(1);

        SamplingHelper.init(Map.of("/test/endpoint|200", rule));

        assertTrue(SamplingHelper.validatePrint(logRecord));
        assertFalse(SamplingHelper.validatePrint(logRecord));
        assertTrue(SamplingHelper.validatePrint(logRecord));
    }
}
