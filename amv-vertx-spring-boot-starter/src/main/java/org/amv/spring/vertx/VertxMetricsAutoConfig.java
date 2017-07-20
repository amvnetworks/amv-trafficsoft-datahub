package org.amv.spring.vertx;

import io.vertx.core.metrics.MetricsOptions;
import io.vertx.core.spi.VertxMetricsFactory;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.dropwizard.impl.VertxMetricsFactoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnClass(VertxMetricsFactoryImpl.class)
public class VertxMetricsAutoConfig {

    @ConditionalOnMissingBean(VertxMetricsFactory.class)
    @Bean
    public VertxMetricsFactory vertxMetricsFactory() {
        return new VertxMetricsFactoryImpl();
    }

    @ConditionalOnMissingBean(MetricsOptions.class)
    @Bean
    public MetricsOptions metricsOptions(VertxMetricsFactory vertxMetricsFactory) {
        return new DropwizardMetricsOptions()
                .setFactory(vertxMetricsFactory)
                .setEnabled(false);
    }
}
