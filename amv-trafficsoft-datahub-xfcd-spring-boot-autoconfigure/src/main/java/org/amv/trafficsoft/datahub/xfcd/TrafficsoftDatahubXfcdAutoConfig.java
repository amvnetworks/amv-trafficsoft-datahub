package org.amv.trafficsoft.datahub.xfcd;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.util.concurrent.AbstractScheduledService.Scheduler;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import io.vertx.core.json.Json;
import io.vertx.rxjava.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.rest.client.autoconfigure.TrafficsoftApiRestClientAutoConfig;
import org.amv.trafficsoft.rest.client.autoconfigure.TrafficsoftApiRestProperties;
import org.amv.trafficsoft.rest.client.xfcd.XfcdClient;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.Executors;
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
        public ConfirmDeliveriesVerticle confirmDeliveriesVerticle() {
            return ConfirmDeliveriesVerticle.builder()
                    .build();
        }

        @Bean
        public XfcdGetDataVerticle TrafficsoftDeliveryPackageEmitterVehicle(Vertx vertx, XfcdClient xfcdClient) {
            return XfcdGetDataVerticle.builder()
                    .flux(trafficsoftDeliveryPackageHotFlux(xfcdClient))
                    .subscriber(new TrafficsoftDeliveryPackageSubscriber() {
                        @Override
                        public void onSubscribe(Subscription subscription) {
                            log.info("onSubscribe");
                            subscription.request(Long.MAX_VALUE);
                        }

                        @Override
                        public void onNext(TrafficsoftDeliveryPackage trafficsoftDeliveryPackage) {
                            log.info("onNext");
                            vertx.eventBus().publish(VertxMessages.deliveryPackage, Json.encode(trafficsoftDeliveryPackage));
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            log.info("onError");
                        }

                        @Override
                        public void onComplete() {
                            log.info("onComplete");

                        }
                    })
                    .build();
        }

        @Bean
        public TrafficsoftDeliveryPackageHotFlux trafficsoftDeliveryPackageHotFlux(XfcdClient xfcdClient) {
            return TrafficsoftDeliveryPackageHotFlux.builder()
                    .xfcdClient(xfcdClient)
                    .contractId(apiRestProperties.getContractId())
                    .build();
        }

        @Bean
        public XfcdGetDataPublisher xfcdGetDataPublisher(XfcdClient xfcdClient) {
            return XfcdGetDataPublisher.builder()
                    .xfcdClient(xfcdClient)
                    .contractId(apiRestProperties.getContractId())
                    .build();
        }

        @Bean
        public AsyncEventBus asyncEventBus() {
            return new AsyncEventBus("async-event-bus", Executors.newFixedThreadPool(20));
        }

        @Bean
        public LoggingDeliveryPackageEventHandler loggingDeliverySink() {
            return new LoggingDeliveryPackageEventHandler(asyncEventBus());
        }

        @Bean
        public XfcdConfirmDeliveriesService confirmDelivieriesService(XfcdClient xfcdClient) {
            return XfcdConfirmDeliveriesService.builder()
                    .xfcdHandledDeliveryPublisher(xfcdHandledDeliveryPublisher())
                    .xfcdClient(xfcdClient)
                    .contractId(apiRestProperties.getContractId())
                    .eventBus(asyncEventBus())
                    .build();
        }

        @Bean
        public ScheduledXfcdGetDataService scheduledXfcdGetDataService(XfcdClient xfcdClient) {
            return ScheduledXfcdGetDataService.builder()
                    .xfcdGetDataPublisher(xfcdGetDataPublisher(xfcdClient))
                    .xfcdConfirmDeliveriesSuccessPublisher(xfcdConfirmDeliveriesSuccessPublisher())
                    .eventBus(asyncEventBus())
                    .scheduler(Scheduler.newFixedDelaySchedule(
                            TimeUnit.SECONDS.toMillis(1),
                            TimeUnit.SECONDS.toMillis(30),
                            TimeUnit.MILLISECONDS
                    ))
                    .build();
        }

        @Bean
        public SpringServiceManager serviceManager(List<Service> services) {
            return new SpringServiceManager(new ServiceManager(services));
        }

        @Bean
        public XfcdHandledDeliveryPublisher xfcdHandledDeliveryPublisher() {
            return new XfcdHandledDeliveryPublisher(asyncEventBus());
        }

        @Bean
        public XfcdConfirmDeliveriesSuccessPublisher xfcdConfirmDeliveriesSuccessPublisher() {
            return new XfcdConfirmDeliveriesSuccessPublisher(asyncEventBus());
        }
    }
}
