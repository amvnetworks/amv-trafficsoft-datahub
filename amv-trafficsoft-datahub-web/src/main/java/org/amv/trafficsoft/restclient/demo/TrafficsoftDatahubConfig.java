package org.amv.trafficsoft.restclient.demo;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.util.concurrent.AbstractScheduledService.Scheduler;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import org.amv.trafficsoft.datahub.xfcd.*;
import org.amv.trafficsoft.datahub.xfcd.experimental.MapDbDeliverySink;
import org.amv.trafficsoft.rest.client.xfcd.XfcdClient;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
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
        return XfcdGetDataPublisher.builder()
                .xfcdClient(xfcdClient)
                .contractId(customerProperties.getContractId())
                .build();
    }

    @Bean
    public AsyncEventBus asyncEventBus() {
        return new AsyncEventBus("async-event-bus", Executors.newFixedThreadPool(20));
    }

    /*@Bean
    public ChronicleMap<Long, DeliveryRestDto> chronicleDeliveryRestDtoMap() throws IOException {
        return ChronicleMap
                .of(Long.class, DeliveryRestDto.class)
                .name("amv-trafficsoft-deliveries")
                .entries(50_000)
                --> not working .averageValue(DeliveryRestDto.builder().build())
                .createPersistedTo(new File("amv-trafficsoft-deliveries.chronicle.db"));
    }*/

    @Bean(destroyMethod = "close")
    public DB db() {
        return DBMaker
                .fileDB("amv-trafficsoft-deliveries.mapdb.db")
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

    /*
    @Bean
    public ChronicleMapDeliverySink chronicleMapDeliverySink() throws IOException {
        return new ChronicleMapDeliverySink(asyncEventBus(), chronicleDeliveryRestDtoMap());
    }*/

    @Bean
    public MapDbDeliverySink mapDbDeliverySink() {
        return new MapDbDeliverySink(asyncEventBus(), deliveryDatabase());
    }

    @Bean
    public LoggingDeliverySink LoggingDeliverySink() {
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
                .contractId(customerProperties.getContractId())
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
