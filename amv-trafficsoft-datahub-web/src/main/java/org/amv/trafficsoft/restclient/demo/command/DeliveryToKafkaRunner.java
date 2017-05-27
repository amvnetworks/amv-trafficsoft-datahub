package org.amv.trafficsoft.restclient.demo.command;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.DeliveryKafkaSink;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.reactivestreams.Publisher;
import org.springframework.boot.CommandLineRunner;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Slf4j
@Builder
public class DeliveryToKafkaRunner implements CommandLineRunner {

    private DeliveryKafkaSink deliveryKafkaSink;
    private Publisher<DeliveryRestDto> publisher;
    private Duration period;

    @Override
    public void run(String... args) throws Exception {
        Flux.interval(Duration.ofMillis(1), period)
                .publishOn(Schedulers.immediate())
                .flatMap(i -> Flux.from(publisher))
                .subscribe(deliveryKafkaSink);
    }
}
