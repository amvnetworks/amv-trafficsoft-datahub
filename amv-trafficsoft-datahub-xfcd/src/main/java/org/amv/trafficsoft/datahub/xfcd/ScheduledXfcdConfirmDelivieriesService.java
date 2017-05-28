package org.amv.trafficsoft.datahub.xfcd;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Queues;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.AbstractScheduledService;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.MapDbDeliverySink.HandledDelivery;
import org.amv.trafficsoft.rest.client.xfcd.XfcdClient;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import rx.Observable;

import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Slf4j
public class ScheduledXfcdConfirmDelivieriesService extends AbstractScheduledService {


    @Value
    @Builder
    public static class ConfirmedDelivery {
        private DeliveryRestDto delivery;
    }

    private final Scheduler scheduler;
    private final EventBus eventBus;
    private final XfcdClient xfcdClient;
    private final long contractId;

    private final Queue<HandledDelivery> queue =
            Queues.newConcurrentLinkedQueue();

    public ScheduledXfcdConfirmDelivieriesService(Scheduler scheduler,
                                                  XfcdClient xfcdClient,
                                                  long contractId,
                                                  EventBus eventBus) {
        this.scheduler = scheduler;
        this.xfcdClient = xfcdClient;
        this.contractId = contractId;
        this.eventBus = eventBus;

        this.eventBus.register(this);
    }

    @Subscribe
    public void onNext(HandledDelivery value) {
        queue.add(value);
    }

    @Override
    protected void startUp() throws Exception {
    }

    @Override
    protected void shutDown() throws Exception {
    }

    @Override
    protected void runOneIteration() throws Exception {
        if (queue.isEmpty()) {
            log.info("Queue is empty");
            return;
        }
        log.info("Queue contains {} elements", queue.size());

        ImmutableList<HandledDelivery> handledDeliveries = ImmutableList.copyOf(queue.stream()
                .collect(toList()));

        Set<Long> deliveryIds = handledDeliveries.stream()
                .map(HandledDelivery::getDelivery)
                .map(DeliveryRestDto::getDeliveryId)
                .collect(Collectors.toSet());
        log.info("About to confirm deliveries {}", deliveryIds);

        xfcdClient.confirmDeliveries(contractId, ImmutableList.copyOf(deliveryIds))
                .toObservable()
                .doOnNext(foo -> log.info("Confirmed delivery {}", deliveryIds))
                .map(foo -> handledDeliveries)
                .doOnNext(ids -> {
                    log.info("about to remove {} elements from queue", ids.size());
                    queue.removeAll(ids);
                    log.info("Queue contains now {} elements", queue.size());
                })
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

    @Override
    protected Scheduler scheduler() {
        return scheduler;
    }

    @Builder
    public static class ConfirmDeliveriesSuccessEvent {

    }
}
