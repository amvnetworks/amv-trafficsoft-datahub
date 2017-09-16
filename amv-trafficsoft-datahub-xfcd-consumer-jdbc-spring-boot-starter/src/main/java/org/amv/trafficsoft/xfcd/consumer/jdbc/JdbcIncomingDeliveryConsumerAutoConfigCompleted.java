package org.amv.trafficsoft.xfcd.consumer.jdbc;

import com.codahale.metrics.MetricRegistry;
import com.zaxxer.hikari.HikariConfig;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.ConfirmingDeliveryConsumer;
import org.amv.trafficsoft.datahub.xfcd.IncomingDeliveryConsumerVerticle;
import org.amv.trafficsoft.datahub.xfcd.IncomingDeliveryEventConsumer;
import org.amv.trafficsoft.datahub.xfcd.XfcdEvents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

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

    @Bean("jdbcXfcdDataConsumer")
    public JdbcXfcdDataConsumer jdbcXfcdDataConsumer(TrafficsoftDeliveryPackageJdbcDao trafficsoftDeliveryPackageJdbcDao) {
        return JdbcXfcdDataConsumer.builder()
                .deliveryPackageDao(trafficsoftDeliveryPackageJdbcDao)
                .build();
    }

    @Bean("trafficsoftJdbcXfcdDataConsumer")
    public IncomingDeliveryEventConsumer trafficsoftJdbcXfcdDataConsumer(JdbcXfcdDataConsumer jdbcXfcdDataConsumer) {
        return ConfirmingDeliveryConsumer.builder()
                .confirmDelivery(properties.isSendConfirmationEvents())
                .deliveryConsumer(jdbcXfcdDataConsumer)
                .build();
    }

    @Bean("trafficsoftDeliveryDataStoreJdbcVerticle")
    public IncomingDeliveryConsumerVerticle trafficsoftDeliveryDataStoreVerticle(XfcdEvents xfcdEvents,
                                                                                 IncomingDeliveryEventConsumer confirmableXfcdDataConsumer) {
        return IncomingDeliveryConsumerVerticle.builder()
                .xfcdEvents(xfcdEvents)
                .incomingDeliveryEventConsumer(confirmableXfcdDataConsumer)
                .build();
    }
}
