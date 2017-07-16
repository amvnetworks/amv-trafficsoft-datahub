package org.amv.trafficsoft.datahub.xfcd;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.util.concurrent.AbstractScheduledService.Scheduler;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import org.amv.trafficsoft.rest.client.autoconfigure.TrafficsoftApiRestClientAutoConfig;
import org.amv.trafficsoft.rest.client.autoconfigure.TrafficsoftApiRestProperties;
import org.amv.trafficsoft.rest.client.xfcd.XfcdClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

@Configuration
@AutoConfigureAfter(TrafficsoftApiRestClientAutoConfig.class)
public class TrafficsoftDatahubXfcdAutoConfig {

    private final TrafficsoftApiRestProperties apiRestProperties;

    @Autowired
    public TrafficsoftDatahubXfcdAutoConfig(TrafficsoftApiRestProperties apiRestProperties) {
        this.apiRestProperties = requireNonNull(apiRestProperties);
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
    public LoggingDeliverySink loggingDeliverySink() {
        return new LoggingDeliverySink(asyncEventBus());
    }

    @Bean
    public XfcdConfirmDeliveriesService scheduledConfirmDelivieriesService(XfcdClient xfcdClient) {
        return XfcdConfirmDeliveriesService.builder()
                .scheduler(
                        Scheduler.newFixedDelaySchedule(
                                TimeUnit.SECONDS.toMillis(1),
                                TimeUnit.SECONDS.toMillis(12),
                                TimeUnit.MILLISECONDS
                        ))
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
