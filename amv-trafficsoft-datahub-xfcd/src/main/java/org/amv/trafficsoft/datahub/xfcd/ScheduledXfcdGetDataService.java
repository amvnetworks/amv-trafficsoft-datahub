package org.amv.trafficsoft.datahub.xfcd;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.AbstractScheduledService;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.event.ConfirmDeliveriesSuccessEvent;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDtoMother;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.TopicProcessor;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;

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

        eventBus.post(TrafficsoftDeliveryPackageImpl.builder()
                .deliveries(DeliveryRestDtoMother.randomList())
                .build());

        TopicProcessor<TrafficsoftDeliveryPackage> sink = TopicProcessor.<TrafficsoftDeliveryPackage>builder()
                .name(this.getClass().getName())
                .build();

        sink
                .publishOn(Schedulers.single())
                .subscribeOn(Schedulers.single())
                .doOnNext(delivery -> eventBus.post(delivery))
                .doOnNext(delivery -> {
                    log.trace("received delivery: {}", delivery);
                })
                .doOnError(e -> log.error("", e))
                .doOnComplete(() -> {
                    log.info("ScheduledXfcdGetDataService completed.");
                })
                .subscribe();

        Flux.from(xfcdGetDataPublisher).subscribe(sink);
    }
}
