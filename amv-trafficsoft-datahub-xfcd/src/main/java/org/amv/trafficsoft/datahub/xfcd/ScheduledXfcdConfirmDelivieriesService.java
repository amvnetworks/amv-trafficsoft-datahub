package org.amv.trafficsoft.datahub.xfcd;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Queues;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.AbstractScheduledService;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.rest.client.xfcd.XfcdClient;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import rx.Observable;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

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

    private final Queue<MapDbDeliverySink.HandledDelivery> queue =
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
    public void onNext(MapDbDeliverySink.HandledDelivery value) {
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
            return;
        }

        Map<Long, DeliveryRestDto> handledDeliveryIds = queue.stream()
                .map(MapDbDeliverySink.HandledDelivery::getDelivery)
                .collect(Collectors.toMap(DeliveryRestDto::getDeliveryId, i -> i));

        Set<Long> deliveryIds = handledDeliveryIds.keySet();
        log.info("About to confirm deliveries {}", handledDeliveryIds);

        xfcdClient.confirmDeliveries(contractId, ImmutableList.copyOf(deliveryIds))
                .toObservable()
                .doOnNext(foo -> log.info("Confirmed delivery {}", deliveryIds))
                .map(foo -> handledDeliveryIds.values())
                .doOnNext(queue::removeAll)
                .flatMap(Observable::from)
                .map(delivery -> ConfirmedDelivery.builder()
                        .delivery(delivery)
                        .build())
                .subscribe(eventBus::post);
    }

    @Override
    protected Scheduler scheduler() {
        return scheduler;
    }
}
