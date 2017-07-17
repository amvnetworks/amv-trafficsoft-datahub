package org.amv.spring.vertx;

import io.vertx.core.Verticle;
import io.vertx.rxjava.core.RxHelper;
import io.vertx.rxjava.core.Vertx;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rx.Observable;
import rx.observables.BlockingObservable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration
@ConditionalOnClass(Vertx.class)
public class VertxRxAutoConfig {

    @ConditionalOnMissingBean(Vertx.class)
    @Bean
    public Vertx vertx() {
        return Vertx.vertx();
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

    public static class VertxStartStopService implements InitializingBean,
            DisposableBean {

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
            vertx.deploymentIDs()
                    .forEach(vertx::undeploy);
            vertx.close();
        }
    }
}
