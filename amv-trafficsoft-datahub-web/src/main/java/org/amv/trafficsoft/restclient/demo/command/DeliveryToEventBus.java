package org.amv.trafficsoft.restclient.demo.command;

import com.google.common.eventbus.AsyncEventBus;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.reactivestreams.Publisher;
import org.springframework.boot.CommandLineRunner;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Slf4j
@Builder
public class DeliveryToEventBus implements CommandLineRunner {

    private AsyncEventBus eventBus;
    private Publisher<DeliveryRestDto> publisher;
    private Duration period;

    @Override
    public void run(String... args) throws Exception {
        Flux.from(publisher)
                .doOnNext(deliveryRestDto -> eventBus.post(deliveryRestDto))
                .subscribe(deliveryRestDto -> {
                    log.debug("received delivery: {}", deliveryRestDto);
                }, error -> {
                    log.error("", error);
                }, () -> {
                    log.info("completed.");
                });
    }
}
