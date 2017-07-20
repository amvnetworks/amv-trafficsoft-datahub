package org.amv.trafficsoft.datahub.xfcd;

import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.event.XfcdEvents;
import org.amv.trafficsoft.rest.client.autoconfigure.TrafficsoftApiRestClientAutoConfig;
import org.amv.trafficsoft.rest.client.autoconfigure.TrafficsoftApiRestProperties;
import org.amv.trafficsoft.rest.client.xfcd.XfcdClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration
@AutoConfigureAfter(TrafficsoftApiRestClientAutoConfig.class)
@ConditionalOnProperty("amv.trafficsoft.datahub.xfcd.enabled")
public class TrafficsoftDatahubXfcdAutoConfig {
    /**
     * Reasons for static declaration: created very early in the application’s lifecycle
     * allows the bean to be created without having to instantiate the @Configuration class
     * <p>
     * https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html#boot-features-external-config-validation
     */
    @Bean
    public static TrafficsoftDatahubXfcdPropertiesValidator configurationPropertiesValidator() {
        return new TrafficsoftDatahubXfcdPropertiesValidator();
    }

    @Configuration
    @EnableConfigurationProperties(TrafficsoftDatahubXfcdProperties.class)
    public class TrafficsoftDatahubXfcdConfig {

        private final TrafficsoftDatahubXfcdProperties datahubXfcdProperties;
        private final TrafficsoftApiRestProperties apiRestProperties;

        @Autowired
        public TrafficsoftDatahubXfcdConfig(TrafficsoftDatahubXfcdProperties datahubXfcdProperties,
                                            TrafficsoftApiRestProperties apiRestProperties) {
            this.datahubXfcdProperties = requireNonNull(datahubXfcdProperties);
            this.apiRestProperties = requireNonNull(apiRestProperties);
        }

        @Bean
        public XfcdEvents xfcdEvents(Vertx vertx) {
            return new XfcdEvents(vertx);
        }

        @Bean
        public LoggingDeliveriesVerticle loggingDeliveriesVerticle(XfcdEvents xfcdEvents) {
            return LoggingDeliveriesVerticle.builder()
                    .xfcdEvents(xfcdEvents)
                    .build();
        }

        @Bean
        public ConfirmDeliveriesVerticle confirmDeliveriesVerticle(XfcdEvents xfcdEvents, XfcdClient xfcdClient) {
            return ConfirmDeliveriesVerticle.builder()
                    .xfcdEvents(xfcdEvents)
                    .contractId(apiRestProperties.getContractId())
                    .xfcdClient(xfcdClient)
                    .build();
        }

        @Bean
        public XfcdGetDataVerticle trafficsoftDeliveryPackageEmitterVehicle(XfcdEvents xfcdEvents,
                                                                            XfcdGetDataPublisher xfcdGetDataPublisher) {
            return XfcdGetDataVerticle.builder()
                    .xfcdEvents(xfcdEvents)
                    .publisher(xfcdGetDataPublisher)
                    .intervalInMs(TimeUnit.SECONDS.toMillis(datahubXfcdProperties.getFetchIntervalInSeconds()))
                    .build();
        }

        @Bean
        public XfcdGetDataPublisherImpl xfcdGetDataPublisher(XfcdClient xfcdClient) {
            return XfcdGetDataPublisherImpl.builder()
                    .xfcdClient(xfcdClient)
                    .contractId(apiRestProperties.getContractId())
                    .build();
        }
    }
}
