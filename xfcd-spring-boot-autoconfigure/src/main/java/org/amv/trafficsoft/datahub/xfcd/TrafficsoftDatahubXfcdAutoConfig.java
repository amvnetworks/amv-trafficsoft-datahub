package org.amv.trafficsoft.datahub.xfcd;

import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
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
@EnableConfigurationProperties(TrafficsoftDatahubXfcdProperties.class)
public class TrafficsoftDatahubXfcdAutoConfig {
    /**
     * Reasons for static declaration: created very early in the application’s lifecycle
     * allows the bean to be created without having to instantiate the @Configuration class
     * <p>
     * https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html#boot-features-external-config-validation
     */
    @Bean
    public static TrafficsoftDatahubXfcdPropertiesValidator datahubXfcdPropertiesValidator() {
        return new TrafficsoftDatahubXfcdPropertiesValidator();
    }

    @Configuration
    @ConditionalOnProperty("amv.trafficsoft.datahub.xfcd.enabled")
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
        public XfcdEventLoggingVerticle loggingDeliveriesVerticle(XfcdEvents xfcdEvents) {
            return XfcdEventLoggingVerticle.builder()
                    .xfcdEvents(xfcdEvents)
                    .build();
        }

        @Bean
        public XfcdEventMetricsVerticle xfcdEventMetricsVerticle(XfcdEvents xfcdEvents) {
            return XfcdEventMetricsVerticle.builder()
                    .xfcdEvents(xfcdEvents)
                    .build();
        }

        @Bean
        public DeliveryConfirmationVerticle confirmDeliveriesVerticle(XfcdEvents xfcdEvents, XfcdClient xfcdClient) {
            return DeliveryConfirmationVerticle.builder()
                    .xfcdEvents(xfcdEvents)
                    .contractId(apiRestProperties.getContractId())
                    .xfcdClient(xfcdClient)
                    .build();
        }

        @Bean
        public DeliveryRetrievalVerticle deliveryRetrievalVerticle(XfcdEvents xfcdEvents,
                                                                   TrafficsoftDeliveryPublisher trafficsoftDeliveryPublisher) {
            return DeliveryRetrievalVerticle.builder()
                    .xfcdEvents(xfcdEvents)
                    .publisher(trafficsoftDeliveryPublisher)
                    .intervalInMs(TimeUnit.SECONDS.toMillis(datahubXfcdProperties.getFetchIntervalInSeconds()))
                    .maxAmountOfNodesPerDelivery(datahubXfcdProperties.getMaxAmountOfNodesPerDelivery())
                    .refetchImmediatelyOnDeliveryWithMaxAmountOfNodes(datahubXfcdProperties.isRefetchImmediatelyOnDeliveryWithMaxAmountOfNodes())
                    .build();
        }

        @Bean
        public TrafficsoftDeliveryPublisher xfcdGetDataPublisher(XfcdClient xfcdClient) {
            return TrafficsoftDeliveryPublisherImpl.builder()
                    .xfcdClient(xfcdClient)
                    .contractId(apiRestProperties.getContractId())
                    .build();
        }
    }
}
