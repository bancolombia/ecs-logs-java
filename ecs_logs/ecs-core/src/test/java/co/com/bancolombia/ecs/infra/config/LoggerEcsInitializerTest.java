package co.com.bancolombia.ecs.infra.config;

import co.com.bancolombia.ecs.helpers.SamplingHelper;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;

class LoggerEcsInitializerTest {

    @Test
    void shouldInitializeLoggerEcsWithRules() {
        SamplingConfig.SamplingRule rule1 = new SamplingConfig.SamplingRule();
        rule1.setUri("/actors/createCustomerParameters");
        rule1.setResponseCode("409");
        rule1.setShowCount(5);
        rule1.setSkipCount(10);
        rule1.setErrorCodes("BPER409-01|BPER409-02");

        SamplingConfig.SamplingRule rule2 = new SamplingConfig.SamplingRule();
        rule2.setUri("/actors/updateCustomer");
        rule2.setResponseCode("200");
        rule2.setShowCount(3);
        rule2.setSkipCount(7);

        SamplingConfig samplingConfig = new SamplingConfig();

        samplingConfig.setRules20XJson("[{\"uri\":\"/actors/updateCustomer\",\"responseCode\":\"200\",\"showCount\":3," +
                "\"skipCount\":7}]");
        samplingConfig.setRules40XJson("[{\"uri\":\"/actors/createCustomerParameters\",\"responseCode\":\"409\"" +
                ",\"showCount\":5,\"skipCount\":10,\"errorCodes\":\"BPER409-01|BPER409-02\"}]");


        Map<String, SamplingConfig.SamplingRule> expectedMap = Map.of(
                "/actors/createCustomerParameters|BPER409-01", rule1,
                "/actors/createCustomerParameters|BPER409-02", rule1,
                "/actors/updateCustomer|200", rule2
        );

        try (MockedStatic<SamplingHelper> mockedLogger = Mockito.mockStatic(SamplingHelper.class)) {
            new LoggerEcsInitializer(samplingConfig);
            mockedLogger.verify(() -> SamplingHelper.init(expectedMap), times(1));
        }
    }

    @Test
    void notInitializeLoggerEcsWithError200() {
        SamplingConfig.SamplingRule rule1 = new SamplingConfig.SamplingRule();
        rule1.setUri("/actors/createCustomerParameters");
        rule1.setResponseCode("200");
        rule1.setShowCount(5);
        rule1.setSkipCount(10);

        SamplingConfig.SamplingRule rule2 = new SamplingConfig.SamplingRule();
        rule2.setUri("/actors/updateCustomer");
        rule2.setResponseCode("200");
        rule2.setShowCount(3);
        rule2.setSkipCount(7);
        rule2.setErrorCodes("BPER500-01");

        SamplingConfig samplingConfig = new SamplingConfig();
        samplingConfig.setRules20XJson("[{\"uri\":\"/actors/createCustomerParameters\",\"responseCode\":\"200\"" +
                ",\"showCount\":5,\"skipCount\":10}," +
                "{\"uri\":\"/actors/updateCustomer\",\"responseCode\":\"200\",\"showCount\":3," +
                "\"skipCount\":7,\"errorCodes\":\"BPER500-01\"}]");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new LoggerEcsInitializer(samplingConfig)
        );

        assertTrue(exception.getMessage().contains("/actors/updateCustomer"));
    }

    @Test
    void notInitializeLoggerEcsWithError409ErrorCodeNull() {
        SamplingConfig samplingConfig = new SamplingConfig();
        samplingConfig.setRules40XJson("[{\"uri\":\"/actors/v2/createCustomerParameters\",\"responseCode\":\"409\"" +
                ",\"showCount\":5,\"skipCount\":10}]");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new LoggerEcsInitializer(samplingConfig)
        );

        assertTrue(exception.getMessage().contains("/actors/v2/createCustomerParameters"));
    }

    @Test
    void notInitializeLoggerEcsWithError409ErrorCodeBlank() {
        SamplingConfig samplingConfig = new SamplingConfig();
        samplingConfig.setRules40XJson("[{\"uri\":\"/actors/createCustomerParameters\",\"responseCode\":\"409\"" +
                ",\"showCount\":5,\"skipCount\":10,\"errorCodes\":\"\"}]");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new LoggerEcsInitializer(samplingConfig)
        );

        assertTrue(exception.getMessage().contains("/actors/createCustomerParameters"));
    }

    @Test
    void notInitializeLoggerEcsWithError409ErrorCodeNoFormat() {
        SamplingConfig.SamplingRule rule1 = new SamplingConfig.SamplingRule();
        rule1.setUri("/actors/createCustomerParameters");
        rule1.setResponseCode("409");
        rule1.setShowCount(5);
        rule1.setSkipCount(10);
        rule1.setErrorCodes("BPER409-01,BPER409-02");

        SamplingConfig.SamplingRule rule2 = new SamplingConfig.SamplingRule();
        rule2.setUri("/actors/updateCustomer");
        rule2.setResponseCode("200");
        rule2.setShowCount(3);
        rule2.setSkipCount(7);

        SamplingConfig samplingConfig = new SamplingConfig();
        samplingConfig.setRules20XJson("[{\"uri\":\"/actors/updateCustomer\",\"responseCode\":\"200\",\"showCount\":3," +
                "\"skipCount\":7}]");
        samplingConfig.setRules40XJson("[{\"uri\":\"/actors/createCustomerParameters\",\"responseCode\":\"409\"" +
                ",\"showCount\":5,\"skipCount\":10,\"errorCodes\":\"BPER409-01,BPER409-02\"}]");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new LoggerEcsInitializer(samplingConfig)
        );

        assertTrue(exception.getMessage().contains("/actors/createCustomerParameters"));
    }

    @Test
    void notInitializeLoggerEcsWithError409ErrorCodeNoFormatEmpty() {
        SamplingConfig.SamplingRule rule1 = new SamplingConfig.SamplingRule();
        rule1.setUri("/actors/createCustomerParameters");
        rule1.setResponseCode("409");
        rule1.setShowCount(5);
        rule1.setSkipCount(10);
        rule1.setErrorCodes("|BPER409-01|BPER409-02|");

        SamplingConfig.SamplingRule rule2 = new SamplingConfig.SamplingRule();
        rule2.setUri("/actors/updateCustomer");
        rule2.setResponseCode("200");
        rule2.setShowCount(3);
        rule2.setSkipCount(7);

        SamplingConfig samplingConfig = new SamplingConfig();
        samplingConfig.setRules40XJson("[{\"uri\":\"/actors/createCustomerParameters\",\"responseCode\":\"409\"" +
                ",\"showCount\":5,\"skipCount\":10,\"errorCodes\":\"|BPER409-01|BPER409-02|\"}]");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new LoggerEcsInitializer(samplingConfig)
        );

        assertTrue(exception.getMessage().contains("/actors/createCustomerParameters"));
    }

    @Test
    void shouldInitializeLoggerEcsWithEmptyMapWhenRulesAreNull() {
        SamplingConfig samplingConfig = new SamplingConfig();

        try (MockedStatic<SamplingHelper> mockedLogger = Mockito.mockStatic(SamplingHelper.class)) {
            new LoggerEcsInitializer(samplingConfig);
            mockedLogger.verify(() -> SamplingHelper.init(Map.of()), times(1));
        }
    }

    @Test
    void shouldInitializeLoggerEcsWithEmptyMapWhenRulesAreEmpty() {
        SamplingConfig samplingConfig = new SamplingConfig();
        samplingConfig.setRules20XJson("");

        try (MockedStatic<SamplingHelper> mockedLogger = Mockito.mockStatic(SamplingHelper.class)) {
            new LoggerEcsInitializer(samplingConfig);
            mockedLogger.verify(() -> SamplingHelper.init(Map.of()), times(1));
        }
    }

    @Test
    void notInitializeLoggerEcsWithRulesJsonBadFormat() {
        SamplingConfig samplingConfig = new SamplingConfig();
        samplingConfig.setRules20XJson("Json_mal_formado:{\"u:\"/actors/createCustomerParameters\"," +
                "\"responseCode\":\"200\",\"showCount\":5,\"skipCount\":10,\"errorCodes\":\"|BPER409-01\"" +
                "},{\"uri\":\"/actors/updateCustomer\"\"showCount\":3,\"skipCount\":7}]");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new LoggerEcsInitializer(samplingConfig)
        );

        assertTrue(exception.getMessage().contains("adapter.ecs.logs.sampling.rules20XJson"));
    }
}
