package org.amv.spring.vertx;

import io.vertx.core.Vertx;
import io.vertx.ext.healthchecks.HealthChecks;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnClass(HealthChecks.class)
@AutoConfigureAfter({
        VertxAutoConfig.class,
        VertxRxAutoConfig.class
})
public class VertxHealthCheckAutoConfig {

    @ConditionalOnMissingBean(HealthChecks.class)
    @Bean
    public HealthChecks healthChecks(Vertx vertx) {
        return HealthChecks.create(vertx);
    }

    @ConditionalOnClass(io.vertx.rxjava.ext.healthchecks.HealthChecks.class)
    @ConditionalOnMissingBean(io.vertx.rxjava.ext.healthchecks.HealthChecks.class)
    @Bean
    public io.vertx.rxjava.ext.healthchecks.HealthChecks rxHealthChecks(io.vertx.rxjava.core.Vertx vertx) {
        return io.vertx.rxjava.ext.healthchecks.HealthChecks.create(vertx);
    }

}
