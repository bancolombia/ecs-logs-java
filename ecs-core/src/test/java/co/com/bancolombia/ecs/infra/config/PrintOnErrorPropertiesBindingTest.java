package co.com.bancolombia.ecs.infra.config;

import co.com.bancolombia.ecs.domain.model.ExceptionLevel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class PrintOnErrorPropertiesBindingTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @Test
    void shouldBindValidLevelSuccessfully() {
        contextRunner
                .withPropertyValues(
                        "adapter.ecs.logs.print-on-error.print-req-resp=true",
                        "adapter.ecs.logs.print-on-error.print-req-resp-level=Exception")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    PrintOnErrorProperties properties = context.getBean(PrintOnErrorProperties.class);
                    assertThat(properties.getPrintReqRespLevel()).isEqualTo(ExceptionLevel.EXCEPTION);
                });
    }

    @Test
    void shouldFailContextWhenLevelIsInvalid() {
        contextRunner
                .withPropertyValues(
                        "adapter.ecs.logs.print-on-error.print-req-resp=true",
                        "adapter.ecs.logs.print-on-error.print-req-resp-level=NoValido")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(IllegalArgumentException.class);
                    assertThat(context.getStartupFailure()).hasStackTraceContaining("Valor no permitido");
                    assertThat(context.getStartupFailure()).hasStackTraceContaining("NoValido");
                    assertThat(context.getStartupFailure()).hasStackTraceContaining(
                            "Valores permitidos: BusinessExceptionECS, Exception, Throwable.");
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(PrintOnErrorProperties.class)
    static class TestConfig {
    }
}
