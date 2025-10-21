package org.amv.trafficsoft.xfcd.consumer.jdbc;

import com.codahale.metrics.MetricRegistry;
import com.zaxxer.hikari.HikariConfig;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.ConfirmingDeliveryConsumer;
import org.amv.trafficsoft.datahub.xfcd.IncomingDeliveryConsumerVerticle;
import org.amv.trafficsoft.datahub.xfcd.IncomingDeliveryEventConsumer;
import org.amv.trafficsoft.datahub.xfcd.XfcdEvents;
import io.vertx.core.Vertx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Configuration
@ConditionalOnProperty("amv.trafficsoft.xfcd.consumer.jdbc.enabled")
public class JdbcIncomingDeliveryConsumerAutoConfigCompleted {

    @Autowired
    @Qualifier("trafficsoftDeliveryJdbcConsumerHikariConfig")
    public HikariConfig hikariConfig;

    @Autowired
    private TrafficsoftXfcdJdbcProperties properties;

    @Autowired(required = false)
    private MetricRegistry metricRegistry;

    @PostConstruct
    public void logDebugInformation() {
        if (!log.isDebugEnabled()) {
            return;
        }

        log.debug("Trafficsoft JDBC auto-configuration has been completed.\n" +
                        "jdbc-url: {}\n" +
                        "user: {}\n" +
                        "driver: {}\n" +
                        "max-pool-size: {}\n",
                hikariConfig.getJdbcUrl(),
                hikariConfig.getUsername(),
                hikariConfig.getDriverClassName(),
                hikariConfig.getMaximumPoolSize());
    }

    @Bean("trafficsoftJdbcDeliveryConsumer")
    public JdbcDeliveryConsumer jdbcDeliveryConsumer(TrafficsoftDeliveryPackageJdbcDao trafficsoftDeliveryPackageJdbcDao) {
        return JdbcDeliveryConsumer.builder()
                .deliveryPackageDao(trafficsoftDeliveryPackageJdbcDao)
                .build();
    }

    @Bean("trafficsoftIncomingDeliveryEventConsumerJdbc")
    public IncomingDeliveryEventConsumer incomingDeliveryEventConsumer(JdbcDeliveryConsumer jdbcDeliveryConsumer) {
        return ConfirmingDeliveryConsumer.builder()
                .confirmDelivery(properties.isSendConfirmationEvents())
                .deliveryConsumer(jdbcDeliveryConsumer)
                .build();
    }

    @Bean("trafficsoftIncomingDeliveryConsumerVerticleJdbc")
    public IncomingDeliveryConsumerVerticle incomingDeliveryConsumerVerticle(XfcdEvents xfcdEvents, IncomingDeliveryEventConsumer eventConsumer) {
        return IncomingDeliveryConsumerVerticle.builder()
                .xfcdEvents(xfcdEvents)
                .incomingDeliveryEventConsumer(eventConsumer)
                .build();
    }

    @Bean
    public SmartLifecycle trafficsoftIncomingDeliveryConsumerVerticleDeployer(Vertx vertx, IncomingDeliveryConsumerVerticle verticle) {
        final AtomicBoolean running = new AtomicBoolean(false);
        return new SmartLifecycle() {
            @Override
            public void start() {
                vertx.deployVerticle(verticle, res -> {
                    if (res.succeeded()) {
                        log.info("Deployed IncomingDeliveryConsumerVerticle with deploymentId={}", res.result());
                        running.set(true);
                    } else {
                        log.error("Failed to deploy IncomingDeliveryConsumerVerticle", res.cause());
                    }
                });
            }

            @Override
            public void stop() {
                running.set(false);
            }

            @Override
            public boolean isRunning() {
                return running.get();
            }

            @Override
            public int getPhase() {
                return Integer.MAX_VALUE;
            }

            @Override
            public boolean isAutoStartup() {
                return true;
            }

            @Override
            public void stop(Runnable callback) {
                stop();
                callback.run();
            }
        };
    }
}
