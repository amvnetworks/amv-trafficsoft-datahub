package org.amv.trafficsoft.restclient.demo;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import org.amv.trafficsoft.datahub.xfcd.*;
import org.amv.trafficsoft.rest.client.xfcd.XfcdClient;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

@Configuration
@Import({
        TrafficsoftRestClientConfig.class,
        //XfcdKafkaConfig.class
})
public class TrafficsoftDatahubConfig {

    private final CustomerProperties customerProperties;

    @Autowired
    public TrafficsoftDatahubConfig(CustomerProperties customerProperties) {
        this.customerProperties = requireNonNull(customerProperties);
    }

    @Bean
    public XfcdGetDataPublisher xfcdGetDataPublisher(XfcdClient xfcdClient) {
        return new XfcdGetDataPublisher(xfcdClient, customerProperties.getContractId());
    }

    @Bean
    public AsyncEventBus asyncEventBus() {
        return new AsyncEventBus("async-event-bus", Executors.newFixedThreadPool(20));
    }

    @Bean(destroyMethod = "close")
    public DB db() {
        return DBMaker
                .fileDB("amv-trafficsoft-deliveries.db")
                .fileMmapEnableIfSupported()
                .transactionEnable()
                .make();
    }

    @SuppressWarnings("unchecked")
    @Bean(destroyMethod = "close")
    public HTreeMap<Long, String> deliveriesMap() {
        DB.HashMapMaker<Long, String> deliveries1 = db()
                .hashMap("deliveries", Serializer.LONG, Serializer.STRING);
        return deliveries1.createOrOpen();
    }

    @Bean
    public MapDbDeliverySink.DeliveryDatabase deliveryDatabase() {
        return MapDbDeliverySink.DeliveryDatabase.builder()
                .deliveriesMap(deliveriesMap())
                .build();
    }

    @Bean
    public MapDbDeliverySink mapDbDeliverySink() {
        return new MapDbDeliverySink(asyncEventBus(), deliveryDatabase());
    }

    @Bean
    public LoggingDeliverySink LoggingDeliverySink() {
        return new LoggingDeliverySink(asyncEventBus());
    }


    @Bean
    public ScheduledXfcdConfirmDelivieriesService scheduledConfirmDelivieriesService(XfcdClient xfcdClient) {
        return new ScheduledXfcdConfirmDelivieriesService(
                AbstractScheduledService.Scheduler.newFixedDelaySchedule(
                        TimeUnit.SECONDS.toMillis(1),
                        TimeUnit.SECONDS.toMillis(12),
                        TimeUnit.MILLISECONDS
                ),
                xfcdClient,
                customerProperties.getContractId(),
                asyncEventBus()
        );
    }

    @Bean
    public XfcdGetDataService xfcdGetDataService(XfcdClient xfcdClient) {
        return XfcdGetDataService.builder()
                .xfcdGetDataPublisher(xfcdGetDataPublisher(xfcdClient))
                .xfcdConfirmDeliveriesSuccessPublisher(xfcdConfirmDeliveriesSuccessPublisher())
                .eventBus(asyncEventBus())
                .build();
    }

    /*
    @Bean
    public HandledDeliveryHandler HandledDeliveryHandler(XfcdClient xfcdClient) {
        return HandledDeliveryHandler.builder()
                .asyncEventBus(asyncEventBus())
                .xfcdClient(xfcdClient)
                .contractId(customerProperties.getContractId())
                .build();
    }*/

    /*@Bean
    public CommandLineRunner deliveryToEvemtBusRunner(XfcdGetDataPublisher publisher, AsyncEventBus asyncEventBus) {
        return DeliveryToEventBus.builder()
                .eventBus(asyncEventBus)
                .publisher(publisher)
                .period(Duration.ofSeconds(30))
                .build();
    }*/


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
    /*@Bean
    public SpringServiceManager springServiceManager(ServiceManager serviceManager) {
        return new SpringServiceManager(serviceManager);
    }*/

    public static class SpringServiceManager extends AbstractIdleService implements
            InitializingBean, DisposableBean {

        private final ServiceManager delegate;

        public SpringServiceManager(ServiceManager delegate) {
            this.delegate = delegate;
        }

        @Override
        protected void startUp() throws Exception {
            delegate.startAsync();
        }

        @Override
        protected void shutDown() throws Exception {
            delegate.stopAsync();
        }

        @Override
        public void destroy() throws Exception {
            shutDown();
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            startUp();
        }
    }
/*
    @Bean
    public DeliveryKafkaSink DeliveryKafkaSink(KafkaSender<Long, DeliveryRestDto> kafkaSender) {
        return DeliveryKafkaSink.builder()
                .kafkaSender(kafkaSender)
                .topic("delivery")
                .build();
    }


    @Bean
    public CommandLineRunner deliveryToKafkaRunner(XfcdGetDataPublisher publisher, DeliveryKafkaSink deliveryKafkaSink) {
        return DeliveryToKafkaRunner.builder()
                .deliveryKafkaSink(deliveryKafkaSink)
                .publisher(publisher)
                .period(Duration.ofSeconds(30))
                .build();
    }*/
}
