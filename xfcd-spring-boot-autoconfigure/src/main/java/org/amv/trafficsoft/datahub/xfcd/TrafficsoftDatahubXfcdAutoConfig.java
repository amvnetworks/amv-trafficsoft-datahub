package org.amv.trafficsoft.datahub.xfcd;

import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.DeliveryRetrievalVerticle.DeliveryRetrievalConfig;
import org.amv.trafficsoft.rest.client.autoconfigure.TrafficsoftApiRestClientAutoConfig;
import org.amv.trafficsoft.rest.client.autoconfigure.TrafficsoftApiRestProperties;
import org.amv.trafficsoft.rest.client.xfcd.XfcdClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
     *
     * @return a validator for xfcd settings
     * TODO: currently disabled because only one "configurationPropertiesValidator" is invoked
    @Bean
    public static TrafficsoftDatahubXfcdPropertiesValidator configurationPropertiesValidator() {
        return new TrafficsoftDatahubXfcdPropertiesValidator();
    }*/

    /**
     * In case the xfcd datahub module is disabled (e.g. during development)
     * a bean of XfcdEvents must be in the context for the application
     * to start up normally as other beans may depend on it.
     *
     * @param vertx A vertx instance
     * @return an instance of XfcdEvents
     */
    @Bean
    @ConditionalOnMissingBean(XfcdEvents.class)
    public XfcdEvents xfcdEvents(Vertx vertx) {
        return new XfcdEvents(vertx);
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
        public DeliveryRetrievalVerticle deliveryRetrievalVerticle(XfcdEvents xfcdEvents,
                                                                   TrafficsoftDeliveryPublisher trafficsoftDeliveryPublisher,
                                                                   DeliveryRetrievalConfig deliveryRetrievalConfig) {
            return new DeliveryRetrievalVerticle(xfcdEvents, trafficsoftDeliveryPublisher, deliveryRetrievalConfig);
        }

        @Bean
        public DeliveryRetrievalConfig deliveryRetrievalConfig() {
            return DeliveryRetrievalConfig.builder()
                    .intervalInMs(TimeUnit.SECONDS.toMillis(datahubXfcdProperties.getFetchIntervalInSeconds()))
                    .initialDelayInMs(TimeUnit.SECONDS.toMillis(datahubXfcdProperties.getInitialFetchDelayInSeconds()))
                    .maxAmountOfNodesPerDelivery(datahubXfcdProperties.getMaxAmountOfNodesPerDelivery())
                    .refetchImmediatelyOnDeliveryWithMaxAmountOfNodes(datahubXfcdProperties.isRefetchImmediatelyOnDeliveryWithMaxAmountOfNodes())
                    .build();
        }

        @Bean
        public TrafficsoftDeliveryPublisher xfcdGetDataPublisher(XfcdClient xfcdClient) {
            return new TrafficsoftDeliveryPublisherImpl(xfcdClient, apiRestProperties.getContractId());
        }

        @Bean
        public DeliveryConfirmationVerticle confirmDeliveriesVerticle(XfcdEvents xfcdEvents, XfcdClient xfcdClient) {
            return new DeliveryConfirmationVerticle(xfcdEvents, xfcdClient, apiRestProperties.getContractId());
        }

        @Bean
        public XfcdEventLoggingVerticle loggingDeliveriesVerticle(XfcdEvents xfcdEvents) {
            return new XfcdEventLoggingVerticle(xfcdEvents);
        }

        @Bean
        public XfcdEventMetricsVerticle xfcdEventMetricsVerticle(XfcdEvents xfcdEvents) {
            return new XfcdEventMetricsVerticle(xfcdEvents);
        }
    }
}
