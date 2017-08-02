package org.amv.spring.vertx;

import io.vertx.core.Verticle;
import io.vertx.core.VertxOptions;
import io.vertx.rxjava.core.RxHelper;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.eventbus.EventBus;
import io.vertx.rxjava.core.file.FileSystem;
import io.vertx.rxjava.core.shareddata.SharedData;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rx.Observable;
import rx.observables.BlockingObservable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration
@ConditionalOnClass(Vertx.class)
@EnableConfigurationProperties(VertxProperties.class)
@AutoConfigureAfter(VertxMetricsAutoConfig.class)
public class VertxRxAutoConfig extends AbstractVertxAutoConfig {

    @Autowired
    public VertxRxAutoConfig(VertxProperties properties) {
        super(properties);
    }

    @ConditionalOnMissingBean(Vertx.class)
    @Bean
    public Vertx rxVertx(VertxOptions vertxOptions) {
        return Vertx.vertx(vertxOptions);
    }

    @ConditionalOnMissingBean(io.vertx.core.Vertx.class)
    @Bean
    public io.vertx.core.Vertx vertx(Vertx vertx) {
        return vertx.getDelegate();
    }

    @ConditionalOnMissingBean(EventBus.class)
    @Bean
    public EventBus eventBus(Vertx vertx) {
        return vertx.eventBus();
    }

    @ConditionalOnMissingBean(FileSystem.class)
    @Bean
    public FileSystem fileSystem(Vertx vertx) {
        return vertx.fileSystem();
    }

    @ConditionalOnMissingBean(SharedData.class)
    @Bean
    public SharedData sharedData(Vertx vertx) {
        return vertx.sharedData();
    }

    @ConditionalOnMissingBean(VertxStartStopService.class)
    @Bean
    public VertxStartStopService vertxStartStopService(Vertx vertx,
                                                       Optional<List<Verticle>> verticles) {
        return VertxStartStopService.builder()
                .verticles(verticles.orElse(Collections.emptyList()))
                .vertx(vertx)
                .build();
    }

    public static class VertxStartStopService implements InitializingBean, DisposableBean {

        private final Vertx vertx;
        private final List<Verticle> verticles;

        @Builder
        VertxStartStopService(Vertx vertx, List<Verticle> verticles) {
            this.vertx = requireNonNull(vertx);
            this.verticles = requireNonNull(verticles);
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            Observable.from(verticles)
                    .map(verticle -> RxHelper.deployVerticle(vertx, verticle))
                    .map(Observable::toBlocking)
                    .map(BlockingObservable::single)
                    .forEach(response -> {
                        log.debug("deployed verticle {}", response);
                    });
        }

        @Override
        public void destroy() throws Exception {
            Set<String> deploymentIds = vertx.deploymentIDs();

            boolean hasDeployedVerticles = !deploymentIds.isEmpty();
            if (hasDeployedVerticles) {
                CountDownLatch countDownLatch = new CountDownLatch(deploymentIds.size());

                deploymentIds.forEach(id -> vertx.rxUndeploy(id)
                        .doOnSuccess(foo -> countDownLatch.countDown())
                        .doOnError(e -> {
                            log.error("", e);
                            countDownLatch.countDown();
                        })
                        .subscribe());

                countDownLatch.await();
            }

            vertx.close();
        }
    }
}
