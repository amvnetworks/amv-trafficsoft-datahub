package org.amv.trafficsoft.restclient.demo;

import org.amv.trafficsoft.datahub.xfcd.GetDataPublisher;
import org.amv.trafficsoft.datahub.xfcd.XfcdKafkaConfig;
import org.amv.trafficsoft.rest.client.xfcd.XfcdClient;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.amv.trafficsoft.restclient.demo.command.DeliveryToKafkaRunner;
import org.amv.trafficsoft.restclient.demo.command.LastDataRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import reactor.kafka.sender.KafkaSender;

import java.time.Duration;

import static java.util.Objects.requireNonNull;

@Configuration
@Import({
        TrafficsoftRestClientConfig.class,
        XfcdKafkaConfig.class
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
    public CommandLineRunner deliveryToKafkaRunner(KafkaSender<Long, DeliveryRestDto> kafkaSender,
                                                   GetDataPublisher publisher) {
        return DeliveryToKafkaRunner.builder()
                .kafkaSender(kafkaSender)
                .publisher(publisher)
                .period(Duration.ofSeconds(30))
                .build();
    }
}
