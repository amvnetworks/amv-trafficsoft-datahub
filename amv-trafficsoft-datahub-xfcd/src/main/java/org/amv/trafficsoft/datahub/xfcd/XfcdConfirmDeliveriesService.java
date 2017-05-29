package org.amv.trafficsoft.datahub.xfcd;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.AbstractScheduledService;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.event.ConfirmDeliveriesSuccessEvent;
import org.amv.trafficsoft.datahub.xfcd.event.ConfirmedDelivery;
import org.amv.trafficsoft.datahub.xfcd.event.HandledDelivery;
import org.amv.trafficsoft.rest.client.xfcd.XfcdClient;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import rx.Observable;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Builder
public class XfcdConfirmDeliveriesService extends AbstractScheduledService {

    @NonNull
    private Scheduler scheduler;
    @NonNull
    private EventBus eventBus;
    @NonNull
    private XfcdClient xfcdClient;
    @NonNull
    private XfcdHandledDeliveryPublisher xfcdHandledDeliveryPublisher;

    private long contractId;

    @Override
    protected Scheduler scheduler() {
        return scheduler;
    }

    @Override
    protected void startUp() throws Exception {
        log.info("startUp()");

        Flux.from(xfcdHandledDeliveryPublisher)
                .publishOn(Schedulers.single())
                .subscribeOn(Schedulers.single())
                .bufferTimeout(100, Duration.ofSeconds(10))
                .doOnSubscribe(subscription -> log.info("subscribed"))
                .subscribe(new BaseSubscriber<List<HandledDelivery>>() {
                    @Override
                    protected void hookOnNext(List<HandledDelivery> handledDeliveries) {
                        log.info("Confirming {} deliveries", handledDeliveries.size());

                        confirmDeliveries(handledDeliveries);
                    }
                });
    }

    @Override
    protected void shutDown() {
        log.info("shutDown()");
    }

    @Override
    protected void runOneIteration() {
        log.info("runOneIteration()");
    }

    private void confirmDeliveries(List<HandledDelivery> handledDeliveries) {
        Set<Long> deliveryIds = handledDeliveries.stream()
                .map(HandledDelivery::getDelivery)
                .map(DeliveryRestDto::getDeliveryId)
                .collect(Collectors.toSet());
        log.info("About to confirm deliveries {}", deliveryIds);

        xfcdClient.confirmDeliveries(contractId, ImmutableList.copyOf(deliveryIds))
                .toObservable()
                .doOnNext(foo -> log.info("Confirmed delivery {}", deliveryIds))
                .map(foo -> handledDeliveries)
                .flatMap(Observable::from)
                .map(HandledDelivery::getDelivery)
                .map(delivery -> ConfirmedDelivery.builder()
                        .delivery(delivery)
                        .build())
                .doOnCompleted(() -> {
                    log.info("about to post ConfirmDeliveriesSuccessEvent event");
                    eventBus.post(ConfirmDeliveriesSuccessEvent.builder()
                            .build());
                })
                .subscribe(eventBus::post);
    }

}
