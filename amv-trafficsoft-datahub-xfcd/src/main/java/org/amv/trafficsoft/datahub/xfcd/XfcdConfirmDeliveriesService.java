package org.amv.trafficsoft.datahub.xfcd;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.AbstractIdleService;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.event.ConfirmDeliveriesSuccessEvent;
import org.amv.trafficsoft.datahub.xfcd.event.ConfirmedDeliveryPackage;
import org.amv.trafficsoft.datahub.xfcd.event.HandledDeliveryPackage;
import org.amv.trafficsoft.rest.client.xfcd.XfcdClient;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import rx.Observable;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Builder
public class XfcdConfirmDeliveriesService extends AbstractIdleService {

    @NonNull
    private EventBus eventBus;
    @NonNull
    private XfcdClient xfcdClient;
    @NonNull
    private XfcdHandledDeliveryPublisher xfcdHandledDeliveryPublisher;

    private long contractId;

    @Override
    protected void startUp() throws Exception {
        log.info("startUp()");

        Flux.from(xfcdHandledDeliveryPublisher)
                .publishOn(Schedulers.single())
                .subscribeOn(Schedulers.single())
                .bufferTimeout(100, Duration.ofSeconds(10))
                .doOnSubscribe(subscription -> log.info("subscribed"))
                .subscribe(new BaseSubscriber<List<HandledDeliveryPackage>>() {
                    @Override
                    protected void hookOnNext(List<HandledDeliveryPackage> handledDeliveries) {
                        log.info("Confirming {} deliveries", handledDeliveries.size());

                        confirmDeliveries(handledDeliveries);
                    }
                });
    }

    @Override
    protected void shutDown() {
        log.info("shutDown()");
    }

    private void confirmDeliveries(List<HandledDeliveryPackage> handledDeliveries) {
        Set<Long> deliveryIds = handledDeliveries.stream()
                .map(HandledDeliveryPackage::getDelivery)
                .map(TrafficsoftDeliveryPackage::getDeliveries)
                .flatMap(Collection::stream)
                .map(DeliveryRestDto::getDeliveryId)
                .collect(Collectors.toSet());

        log.info("About to confirm deliveries {}", deliveryIds);

        xfcdClient.confirmDeliveries(contractId, ImmutableList.copyOf(deliveryIds))
                .toObservable()
                .doOnNext(foo -> log.info("Confirmed delivery {}", deliveryIds))
                .map(foo -> handledDeliveries)
                .flatMap(Observable::from)
                .map(HandledDeliveryPackage::getDelivery)
                .map(delivery -> ConfirmedDeliveryPackage.builder()
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
