package co.com.bancolombia.ecs.infra.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = SensitiveRulesConfig.class)
@EnableConfigurationProperties(SensitiveRulesConfig.class)
class SensitiveRulesConfigTest {

    @Autowired
    private SensitiveRulesConfig sensitiveRulesConfig;

    @BeforeEach
    void setUp() {
        sensitiveRulesConfig.setSensitiveData(null);
    }

    @Test
    void shouldBindPropertiesCorrectlyWithPartialMasking() {
        sensitiveRulesConfig.setSensitiveData(
                "[{\"uriPattern\":\"/retrieve-approvers\",\"fieldPaths\":[\"firstName\",\"lastName\"]," +
                        "\"maskingType\":\"PARTIAL\",\"visibilityPercentage\":0.3,\"maskingChar\":\"*\",\"enabled\":true}]"
        );

        assertThat(sensitiveRulesConfig).isNotNull();
        assertThat(sensitiveRulesConfig.getSensitiveData()).isNotNull();
        assertThat(sensitiveRulesConfig.getSensitiveData()).contains("PARTIAL");
        assertThat(sensitiveRulesConfig.getSensitiveData()).contains("0.3");
        assertThat(sensitiveRulesConfig.getSensitiveData()).contains("firstName");
    }

    @Test
    void shouldBindPropertiesCorrectlyWithCustomMasking() {
        sensitiveRulesConfig.setSensitiveData(
                "[{\"uriPattern\":\"/user-profile\",\"fieldPaths\":[\"email\",\"phone\"]," +
                        "\"maskingType\":\"CUSTOM\",\"customMask\":\"[PROTECTED]\",\"enabled\":true}]"
        );

        assertThat(sensitiveRulesConfig).isNotNull();
        assertThat(sensitiveRulesConfig.getSensitiveData()).isNotNull();
        assertThat(sensitiveRulesConfig.getSensitiveData()).contains("CUSTOM");
        assertThat(sensitiveRulesConfig.getSensitiveData()).contains("[PROTECTED]");
        assertThat(sensitiveRulesConfig.getSensitiveData()).contains("email");
    }

    @Test
    void shouldBindPropertiesCorrectlyWithFullMasking() {
        sensitiveRulesConfig.setSensitiveData(
                "[{\"uriPattern\":\"/admin\",\"fieldPaths\":[\"ssn\",\"creditCard\"]," +
                        "\"maskingType\":\"FULL\",\"maskingChar\":\"X\",\"enabled\":true}]"
        );

        assertThat(sensitiveRulesConfig).isNotNull();
        assertThat(sensitiveRulesConfig.getSensitiveData()).isNotNull();
        assertThat(sensitiveRulesConfig.getSensitiveData()).contains("FULL");
        assertThat(sensitiveRulesConfig.getSensitiveData()).contains("\"X\"");
        assertThat(sensitiveRulesConfig.getSensitiveData()).contains("ssn");
    }

    @Test
    void shouldBindPropertiesCorrectlyWithRemoveMasking() {
        sensitiveRulesConfig.setSensitiveData(
                "[{\"uriPattern\":\"/secure\",\"fieldPaths\":[\"password\",\"token\"]," +
                        "\"maskingType\":\"REMOVE\",\"enabled\":true}]"
        );

        assertThat(sensitiveRulesConfig).isNotNull();
        assertThat(sensitiveRulesConfig.getSensitiveData()).isNotNull();
        assertThat(sensitiveRulesConfig.getSensitiveData()).contains("REMOVE");
        assertThat(sensitiveRulesConfig.getSensitiveData()).contains("password");
        assertThat(sensitiveRulesConfig.getSensitiveData()).contains("token");
    }

    @Test
    void shouldBindPropertiesCorrectlyWithMultipleRules() {
        sensitiveRulesConfig.setSensitiveData(
                "[{\"uriPattern\":\"/retrieve-approvers\",\"fieldPaths\":[\"firstName\"]," +
                        "\"maskingType\":\"PARTIAL\",\"visibilityPercentage\":0.2,\"enabled\":true}," +
                        "{\"uriPattern\":\"/user-profile\",\"fieldPaths\":[\"email\"]," +
                        "\"maskingType\":\"CUSTOM\",\"customMask\":\"[HIDDEN]\",\"enabled\":true}]"
        );

        assertThat(sensitiveRulesConfig).isNotNull();
        assertThat(sensitiveRulesConfig.getSensitiveData()).isNotNull();
        assertThat(sensitiveRulesConfig.getSensitiveData()).contains("retrieve-approvers");
        assertThat(sensitiveRulesConfig.getSensitiveData()).contains("user-profile");
        assertThat(sensitiveRulesConfig.getSensitiveData()).contains("PARTIAL");
        assertThat(sensitiveRulesConfig.getSensitiveData()).contains("CUSTOM");
    }

    @Test
    void shouldBindPropertiesCorrectlyWithComplexFieldPaths() {
        sensitiveRulesConfig.setSensitiveData(
                "[{\"uriPattern\":\"/retrieve-detail\",\"fieldPaths\":[" +
                        "\"additionalInfo.responseBody.data.flowResponse[*].naturalPersonInformationDetail.firstName\"," +
                        "\"additionalInfo.responseBody.data.flowResponse[*].naturalPersonInformationDetail.lastName\"]," +
                        "\"maskingType\":\"PARTIAL\",\"visibilityPercentage\":0.35,\"enabled\":true}]"
        );

        assertThat(sensitiveRulesConfig).isNotNull();
        assertThat(sensitiveRulesConfig.getSensitiveData()).isNotNull();
        assertThat(sensitiveRulesConfig.getSensitiveData()).contains("flowResponse[*]");
        assertThat(sensitiveRulesConfig.getSensitiveData()).contains("naturalPersonInformationDetail");
        assertThat(sensitiveRulesConfig.getSensitiveData()).contains("0.35");
    }

    @Test
    void shouldBindPropertiesCorrectlyWithDisabledRule() {
        sensitiveRulesConfig.setSensitiveData(
                "[{\"uriPattern\":\"/test\",\"fieldPaths\":[\"testField\"]," +
                        "\"maskingType\":\"FULL\",\"enabled\":false}]"
        );

        assertThat(sensitiveRulesConfig).isNotNull();
        assertThat(sensitiveRulesConfig.getSensitiveData()).isNotNull();
        assertThat(sensitiveRulesConfig.getSensitiveData()).contains("\"enabled\":false");
        assertThat(sensitiveRulesConfig.getSensitiveData()).contains("testField");
    }

    @Test
    void shouldBindEmptyPropertiesCorrectly() {
        sensitiveRulesConfig.setSensitiveData("");

        assertThat(sensitiveRulesConfig).isNotNull();
        assertThat(sensitiveRulesConfig.getSensitiveData()).isNotNull();
        assertThat(sensitiveRulesConfig.getSensitiveData()).isEmpty();
    }

    @Test
    void shouldBindNullPropertiesCorrectly() {
        sensitiveRulesConfig.setSensitiveData(null);

        assertThat(sensitiveRulesConfig).isNotNull();
        assertThat(sensitiveRulesConfig.getSensitiveData()).isNull();
    }

    @Test
    void shouldBindPropertiesCorrectlyWithDefaultValues() {
        sensitiveRulesConfig.setSensitiveData(
                "[{\"uriPattern\":\"/default-test\",\"fieldPaths\":[\"defaultField\"]}]"
        );

        assertThat(sensitiveRulesConfig).isNotNull();
        assertThat(sensitiveRulesConfig.getSensitiveData()).isNotNull();
        assertThat(sensitiveRulesConfig.getSensitiveData()).contains("defaultField");
    }

    @Test
    void shouldBindPropertiesCorrectlyWithCustomVisibilityPercentage() {
        sensitiveRulesConfig.setSensitiveData(
                "[{\"uriPattern\":\"/percentage-test\",\"fieldPaths\":[\"testField\"]," +
                        "\"maskingType\":\"PARTIAL\",\"visibilityPercentage\":0.5,\"enabled\":true}]"
        );

        assertThat(sensitiveRulesConfig).isNotNull();
        assertThat(sensitiveRulesConfig.getSensitiveData()).isNotNull();
        assertThat(sensitiveRulesConfig.getSensitiveData()).contains("0.5");
        assertThat(sensitiveRulesConfig.getSensitiveData()).contains("PARTIAL");
    }

}