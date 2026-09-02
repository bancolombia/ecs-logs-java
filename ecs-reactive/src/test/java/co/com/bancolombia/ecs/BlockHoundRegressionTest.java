package co.com.bancolombia.ecs;

import co.com.bancolombia.ecs.domain.model.LogRecord;
import co.com.bancolombia.ecs.infra.config.managementid.application.MessageIdMngUseCase;
import co.com.bancolombia.ecs.infra.config.managementid.domain.MessageIdRequestProperties;
import co.com.bancolombia.ecs.model.management.BusinessExceptionECS;
import co.com.bancolombia.ecs.model.management.ErrorManagement;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

class BlockHoundRegressionTest {

    @Test
    void businessExceptionDefaultMetaInfoShouldNotBlockOnReactiveScheduler() {
        StepVerifier.create(
                Mono.fromCallable(() -> new BusinessExceptionECS(ErrorManagement.DEFAULT_EXCEPTION))
                        .subscribeOn(Schedulers.parallel())
        ).expectNextCount(1).verifyComplete();
    }

    @Test
    void logRecordDefaultMessageIdShouldNotBlockOnReactiveScheduler() {
        StepVerifier.create(
                Mono.fromCallable(() -> LogRecord.builder().build())
                        .subscribeOn(Schedulers.parallel())
        ).expectNextCount(1).verifyComplete();
    }

    @Test
    void resolveForExceptionFallbackShouldNotBlockOnReactiveScheduler() {
        StepVerifier.create(
                Mono.fromCallable(() -> MessageIdMngUseCase.resolveForException(null))
                        .subscribeOn(Schedulers.parallel())
        ).expectNextCount(1).verifyComplete();
    }

    @Test
    void resolveFromRequestEnvironmentFallbackShouldNotBlockOnReactiveScheduler() {
        MessageIdRequestProperties properties = new MessageIdRequestProperties("true");
        properties.afterPropertiesSet();
        MessageIdMngUseCase useCase = new MessageIdMngUseCase(properties);
        StepVerifier.create(
                Mono.fromCallable(() -> useCase.resolveFromRequestEnvironment(null))
                        .subscribeOn(Schedulers.parallel())
        ).expectNextCount(1).verifyComplete();
    }
}
