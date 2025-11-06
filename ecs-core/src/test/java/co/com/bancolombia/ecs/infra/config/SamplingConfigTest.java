package co.com.bancolombia.ecs.infra.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = SamplingConfig.class)
@EnableConfigurationProperties(SamplingConfig.class)
class SamplingConfigTest {

    @Autowired
    private SamplingConfig samplingConfig;

    @BeforeEach
    void setUp() {
        samplingConfig.setRules40XJson(null);
        samplingConfig.setRules20XJson(null);
    }

    @Test
    void shouldBindPropertiesCorrectlyRules20X() {
        samplingConfig.setRules20XJson("[{\"uri\":\"/actors/createLegalPerson\",\"responseCode\":\"200\"," +
                "\"showCount\":2,\"skipCount\":2},{\"uri\":\"/actors/createNaturalPerson\",\"responseCode\":\"200\"," +
                "\"showCount\":2,\"skipCount\":2}]");

        assertThat(samplingConfig).isNotNull();
        assertThat(samplingConfig.getRules()).isNotNull();
        assertThat(samplingConfig.getRules().get(0).getUri()).isEqualTo("/actors/createLegalPerson");
        assertThat(samplingConfig.getRules().get(0).getResponseCode()).isEqualTo("200");

        assertThat(samplingConfig.getRules().get(1).getUri()).isEqualTo("/actors/createNaturalPerson");
        assertThat(samplingConfig.getRules().get(1).getResponseCode()).isEqualTo("200");
    }

    @Test
    void shouldBindPropertiesCorrectlyRules40X() {
        samplingConfig.setRules40XJson("[{\"uri\":\"/actors/createLegalPerson\",\"responseCode\":\"409\"," +
                "\"showCount\":2,\"skipCount\":2,\"errorCodes\":\"BPER409-52\"},{\"uri\":\"/actors/createNaturalPerson\"," +
                "\"responseCode\":\"409\",\"showCount\":2,\"skipCount\":2,\"errorCodes\":\"BPER409-52\"}]");

        assertThat(samplingConfig).isNotNull();
        assertThat(samplingConfig.getRules()).isNotNull();
        assertThat(samplingConfig.getRules().get(0).getUri()).isEqualTo("/actors/createLegalPerson");
        assertThat(samplingConfig.getRules().get(0).getResponseCode()).isEqualTo("409");

        assertThat(samplingConfig.getRules().get(1).getUri()).isEqualTo("/actors/createNaturalPerson");
        assertThat(samplingConfig.getRules().get(1).getResponseCode()).isEqualTo("409");
    }

    @Test
    void shouldBindPropertiesCorrectlyRules20X_40X() {
        samplingConfig.setRules20XJson("[{\"uri\":\"/actors/createLegalPerson\",\"responseCode\":\"200\"," +
                "\"showCount\":2,\"skipCount\":2},{\"uri\":\"/actors/createNaturalPerson\",\"responseCode\":\"200\"," +
                "\"showCount\":2,\"skipCount\":2}]");

        samplingConfig.setRules40XJson("[{\"uri\":\"/actors/createLegalPerson\",\"responseCode\":\"409\"," +
                "\"showCount\":2,\"skipCount\":2,\"errorCodes\":\"BPER409-52\"},{\"uri\":\"/actors/createNaturalPerson\"," +
                "\"responseCode\":\"409\",\"showCount\":2,\"skipCount\":2,\"errorCodes\":\"BPER409-52\"}]");

        assertThat(samplingConfig).isNotNull();
        assertThat(samplingConfig.getRules()).isNotNull();
        assertThat(samplingConfig.getRules().get(0).getUri()).isEqualTo("/actors/createLegalPerson");
        assertThat(samplingConfig.getRules().get(0).getResponseCode()).isEqualTo("200");

        assertThat(samplingConfig.getRules().get(1).getUri()).isEqualTo("/actors/createNaturalPerson");
        assertThat(samplingConfig.getRules().get(1).getResponseCode()).isEqualTo("200");

        assertThat(samplingConfig.getRules().get(2).getUri()).isEqualTo("/actors/createLegalPerson");
        assertThat(samplingConfig.getRules().get(2).getResponseCode()).isEqualTo("409");

        assertThat(samplingConfig.getRules().get(3).getUri()).isEqualTo("/actors/createNaturalPerson");
        assertThat(samplingConfig.getRules().get(3).getResponseCode()).isEqualTo("409");
    }

    @Test
    void shouldNotBindPropertiesRulesWithInvalidCode() {
        String messageExcepcion = "";
        samplingConfig.setRules20XJson("[{\"uri\":\"/actors/createLegalPerson\",\"responseCode\":\"200\"," +
                "\"showCount\":2,\"skipCount\":2},{\"uri\":\"/actors/createNaturalPerson\",\"responseCode\":\"409\"," +
                "\"showCount\":2,\"skipCount\":2,\"errorCodes\":\"BPER409-52\"}]");

       try {
           samplingConfig.getRules();
       } catch (Exception e) {
           messageExcepcion = e.getMessage();
       }

        assertTrue(messageExcepcion.contains("have an invalid response code. Expected starts with:"));
    }

    @Test
    void shouldNotBindProperties20XRulesWithErrorFormat() {
        String messageExcepcion = "";
        samplingConfig.setRules20XJson("[{\"uri\":\"/actors/createLegalPerson\",\"responseCode\":\"200\"," +
                "\"showCount\":2,\"skipCount\":2},{\"uri\":");

        try {
            samplingConfig.getRules();
        } catch (Exception e) {
            messageExcepcion = e.getMessage();
        }

        assertTrue(messageExcepcion.contains("Error parsing adapter.ecs.logs.sampling.rules20XJson"));
    }

    @Test
    void shouldNotBindProperties40XRulesWithErrorFormat() {
        String messageExcepcion = "";
        samplingConfig.setRules40XJson("[{\"uri\":\"/actors/createLegalPerson\",\":\"403\"," +
                "\"showCount\":2,\"skipCount\":{\"uri\":");

        try {
            samplingConfig.getRules();
        } catch (Exception e) {
            messageExcepcion = e.getMessage();
        }

        assertTrue(messageExcepcion.contains("Error parsing adapter.ecs.logs.sampling.rules40XJson"));
    }

    @Test
    void shouldBindEmptyPropertiesCorrectlyRules20X_40X() {
        samplingConfig.setRules20XJson("");

        samplingConfig.setRules40XJson("");

        assertThat(samplingConfig).isNotNull();
        assertThat(samplingConfig.getRules()).isNotNull();
        assertThat(samplingConfig.getRules()).isEmpty();
    }

    @Test
    void shouldBindPropertiesNullRules20X_40X() {
        samplingConfig.setRules20XJson(null);

        samplingConfig.setRules40XJson(null);

        assertThat(samplingConfig).isNotNull();
        assertThat(samplingConfig.getRules()).isNotNull();
        assertThat(samplingConfig.getRules()).isEmpty();
    }
}
