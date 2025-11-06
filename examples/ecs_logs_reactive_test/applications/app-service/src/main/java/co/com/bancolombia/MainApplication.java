package co.com.bancolombia;

import co.com.bancolombia.ecs.application.ReactiveLogsConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Import;

/**
 * @Comment
 * Agregar la configuración @Import(ReactiveLogsConfiguration.class) en la clase principal del proyecto:
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@Import(ReactiveLogsConfiguration.class)
public class MainApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
}
