package org.amv.trafficsoft.datahub.xfcd;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.AbstractScheduledService;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.TopicProcessor;

@Slf4j
@Builder
public class ScheduledXfcdGetDataService extends AbstractScheduledService {

    @NonNull
    private XfcdGetDataPublisher xfcdGetDataPublisher;
    @NonNull
    private XfcdConfirmDeliveriesSuccessPublisher xfcdConfirmDeliveriesSuccessPublisher;
    @NonNull
    private Scheduler scheduler;
    @NonNull
    private EventBus eventBus;

    @Override
    protected Scheduler scheduler() {
        return scheduler;
    }

    @Override
    protected void startUp() throws Exception {
        Flux.from(xfcdConfirmDeliveriesSuccessPublisher)
                .subscribe(new BaseSubscriber<ConfirmDeliveriesSuccessEvent>() {
                    @Override
                    protected void hookOnNext(ConfirmDeliveriesSuccessEvent value) {
                        log.info("got confirmed deliveries SUCCESS");
                    }
                });
    }

    @Override
    protected void shutDown() throws Exception {
    }

    @Override
    protected void runOneIteration() throws Exception {
        log.info("ScheduledXfcdGetDataService starting.");

        TopicProcessor<DeliveryRestDto> sink = TopicProcessor.create();

        sink
                .doOnNext(deliveryRestDto -> eventBus.post(deliveryRestDto))
                .doOnNext(deliveryRestDto -> {
                    log.trace("received delivery: {}", deliveryRestDto);
                })
                .doOnError(e -> log.error("", e))
                .doOnComplete(() -> {
                    log.info("ScheduledXfcdGetDataService completed.");
                })
                .subscribe();

        Flux.from(xfcdGetDataPublisher).subscribe(sink);
    }
}
