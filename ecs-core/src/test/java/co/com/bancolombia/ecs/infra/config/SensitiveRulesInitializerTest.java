package co.com.bancolombia.ecs.infra.config;

import co.com.bancolombia.ecs.helpers.SensitiveHelper;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;

class SensitiveRulesInitializerTest {

    @Test
    void shouldInitializeSensitiveRulesWithRules() {
        SensitiveRulesConfig sensitiveConfig = new SensitiveRulesConfig();
        sensitiveConfig.setSensitiveData(
                "[{\"uriPattern\":\"/retrieve-approvers\",\"fieldPaths\":[\"firstName\",\"lastName\"]," +
                        "\"maskingType\":\"PARTIAL\",\"visibilityPercentage\":0.3,\"enabled\":true}," +
                        "{\"uriPattern\":\"/user-profile\",\"fieldPaths\":[\"email\"]," +
                        "\"maskingType\":\"CUSTOM\",\"customMask\":\"[PROTECTED]\",\"enabled\":true}]"
        );

        try (MockedStatic<SensitiveHelper> mockedHelper = Mockito.mockStatic(SensitiveHelper.class)) {
            new SensitiveRulesInitializer(sensitiveConfig);
            mockedHelper.verify(() -> SensitiveHelper.init(Mockito.argThat(
                    rulesMap -> rulesMap.containsKey("/retrieve-approvers") &&
                            rulesMap.containsKey("/user-profile") &&
                            rulesMap.size() == 2
            )), times(1));
        }
    }

    @Test
    void shouldInitializeSensitiveRulesWithMultipleRulesForSameUri() {
        SensitiveRulesConfig sensitiveConfig = new SensitiveRulesConfig();
        sensitiveConfig.setSensitiveData(
                "[{\"uriPattern\":\"/retrieve-approvers\",\"fieldPaths\":[\"firstName\"]," +
                        "\"maskingType\":\"PARTIAL\",\"visibilityPercentage\":0.3,\"enabled\":true}," +
                        "{\"uriPattern\":\"/retrieve-approvers\",\"fieldPaths\":[\"lastName\"]," +
                        "\"maskingType\":\"FULL\",\"enabled\":true}]"
        );

        try (MockedStatic<SensitiveHelper> mockedHelper = Mockito.mockStatic(SensitiveHelper.class)) {
            new SensitiveRulesInitializer(sensitiveConfig);
            mockedHelper.verify(() -> SensitiveHelper.init(Mockito.argThat(
                    rulesMap -> rulesMap.containsKey("/retrieve-approvers") &&
                            rulesMap.get("/retrieve-approvers").size() == 2
            )), times(1));
        }
    }

    @Test
    void shouldNotInitializeSensitiveRulesWithDisabledRule() {
        SensitiveRulesConfig sensitiveConfig = new SensitiveRulesConfig();
        sensitiveConfig.setSensitiveData(
                "[{\"uriPattern\":\"/test\",\"fieldPaths\":[\"testField\"]," +
                        "\"maskingType\":\"FULL\",\"enabled\":false}]"
        );

        try (MockedStatic<SensitiveHelper> mockedHelper = Mockito.mockStatic(SensitiveHelper.class)) {
            new SensitiveRulesInitializer(sensitiveConfig);
            mockedHelper.verify(() -> SensitiveHelper.init(Map.of()), times(1));
        }
    }

    @Test
    void shouldInitializeSensitiveRulesWithComplexFieldPaths() {
        SensitiveRulesConfig sensitiveConfig = new SensitiveRulesConfig();
        sensitiveConfig.setSensitiveData(
                "[{\"uriPattern\":\"/retrieve-detail\",\"fieldPaths\":[" +
                        "\"additionalInfo.responseBody.data.flowResponse[*].naturalPersonInformationDetail.firstName\"]," +
                        "\"maskingType\":\"PARTIAL\",\"visibilityPercentage\":0.35,\"enabled\":true}]"
        );

        try (MockedStatic<SensitiveHelper> mockedHelper = Mockito.mockStatic(SensitiveHelper.class)) {
            new SensitiveRulesInitializer(sensitiveConfig);
            mockedHelper.verify(() -> SensitiveHelper.init(Mockito.argThat(
                    rulesMap -> rulesMap.containsKey("/retrieve-detail") && rulesMap.size() == 1
            )), times(1));
        }
    }

    @Test
    void shouldInitializeSensitiveRulesWithEmptyMapWhenRulesAreNull() {
        SensitiveRulesConfig sensitiveConfig = new SensitiveRulesConfig();
        sensitiveConfig.setSensitiveData(null);

        try (MockedStatic<SensitiveHelper> mockedHelper = Mockito.mockStatic(SensitiveHelper.class)) {
            new SensitiveRulesInitializer(sensitiveConfig);
            mockedHelper.verify(() -> SensitiveHelper.init(Map.of()), times(1));
        }
    }


    @Test
    void notInitializeSensitiveRulesWithJsonBadFormat() {
        SensitiveRulesConfig sensitiveConfig = new SensitiveRulesConfig();
        sensitiveConfig.setSensitiveData(
                "Json_mal_formado:{\"uriPattern\":\"/test\",\"fieldPaths:[\"testField\"]," +
                        "\"maskingType\":\"FULL\",\"enabled\":true"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new SensitiveRulesInitializer(sensitiveConfig)
        );

        assertTrue(exception.getMessage().contains("Error parsing sensitive data rules"));
    }

    @Test
    void notInitializeSensitiveRulesWithMalformedArray() {
        SensitiveRulesConfig sensitiveConfig = new SensitiveRulesConfig();
        sensitiveConfig.setSensitiveData(
                "[{\"uriPattern\":\"/test\",\"fieldPaths\":[\"testField\"]," +
                        "\"maskingType\":\"FULL\",\"enabled\":true},}"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new SensitiveRulesInitializer(sensitiveConfig)
        );

        assertTrue(exception.getMessage().contains("Error parsing sensitive data rules"));
    }

    @Test
    void shouldInitializeSensitiveRulesWithDifferentMaskingTypes() {
        SensitiveRulesConfig sensitiveConfig = new SensitiveRulesConfig();
        sensitiveConfig.setSensitiveData(
                "[{\"uriPattern\":\"/test-full\",\"fieldPaths\":[\"field1\"]," +
                        "\"maskingType\":\"FULL\",\"enabled\":true}," +
                        "{\"uriPattern\":\"/test-remove\",\"fieldPaths\":[\"field2\"]," +
                        "\"maskingType\":\"REMOVE\",\"enabled\":true}," +
                        "{\"uriPattern\":\"/test-custom\",\"fieldPaths\":[\"field3\"]," +
                        "\"maskingType\":\"CUSTOM\",\"customMask\":\"[HIDDEN]\",\"enabled\":true}]"
        );

        try (MockedStatic<SensitiveHelper> mockedHelper = Mockito.mockStatic(SensitiveHelper.class)) {
            new SensitiveRulesInitializer(sensitiveConfig);
            mockedHelper.verify(() -> SensitiveHelper.init(Mockito.argThat(
                    rulesMap -> rulesMap.size() == 3 &&
                            rulesMap.containsKey("/test-full") &&
                            rulesMap.containsKey("/test-remove") &&
                            rulesMap.containsKey("/test-custom")
            )), times(1));
        }
    }

    @Test
    void shouldInitializeSensitiveRulesWithDefaultValues() {
        SensitiveRulesConfig sensitiveConfig = new SensitiveRulesConfig();
        sensitiveConfig.setSensitiveData(
                "[{\"uriPattern\":\"/default-test\",\"fieldPaths\":[\"defaultField\"]}]"
        );

        try (MockedStatic<SensitiveHelper> mockedHelper = Mockito.mockStatic(SensitiveHelper.class)) {
            new SensitiveRulesInitializer(sensitiveConfig);
            mockedHelper.verify(() -> SensitiveHelper.init(Mockito.argThat(
                    rulesMap -> rulesMap.containsKey("/default-test") &&
                            rulesMap.get("/default-test").size() == 1
            )), times(1));
        }
    }
}