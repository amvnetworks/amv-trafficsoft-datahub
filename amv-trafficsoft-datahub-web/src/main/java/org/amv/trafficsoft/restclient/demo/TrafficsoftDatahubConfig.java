package org.amv.trafficsoft.restclient.demo;

import com.google.common.eventbus.AsyncEventBus;
import org.amv.trafficsoft.datahub.xfcd.GetDataPublisher;
import org.amv.trafficsoft.datahub.xfcd.HandledDeliveryHandler;
import org.amv.trafficsoft.datahub.xfcd.LoggingDeliverySink;
import org.amv.trafficsoft.datahub.xfcd.MapDbDeliverySink;
import org.amv.trafficsoft.rest.client.xfcd.XfcdClient;
import org.amv.trafficsoft.restclient.demo.command.DeliveryToEventBus;
import org.amv.trafficsoft.restclient.demo.command.LastDataRunner;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import java.time.Duration;
import java.util.concurrent.Executors;

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
    @Profile("get-last-data-demo")
    public CommandLineRunner lastDataRunner(XfcdClient xfcdClient) {
        return new LastDataRunner(
                xfcdClient,
                customerProperties.getContractId(),
                customerProperties.getVehicleIds()
        );
    }

    @Bean
    public GetDataPublisher GetDataPublisher(XfcdClient xfcdClient) {
        return new GetDataPublisher(xfcdClient, customerProperties.getContractId());
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
    public HandledDeliveryHandler HandledDeliveryHandler(XfcdClient xfcdClient) {
        return HandledDeliveryHandler.builder()
                .asyncEventBus(asyncEventBus())
                .xfcdClient(xfcdClient)
                .contractId(customerProperties.getContractId())
                .build();
    }

    @Bean
    public CommandLineRunner deliveryToEvemtBusRunner(GetDataPublisher publisher, AsyncEventBus asyncEventBus) {
        return DeliveryToEventBus.builder()
                .eventBus(asyncEventBus)
                .publisher(publisher)
                .period(Duration.ofSeconds(30))
                .build();
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
    public CommandLineRunner deliveryToKafkaRunner(GetDataPublisher publisher, DeliveryKafkaSink deliveryKafkaSink) {
        return DeliveryToKafkaRunner.builder()
                .deliveryKafkaSink(deliveryKafkaSink)
                .publisher(publisher)
                .period(Duration.ofSeconds(30))
                .build();
    }*/
}
