package org.amv.trafficsoft.datahub.xfcd;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.ScheduledXfcdConfirmDelivieriesService.ConfirmDeliveriesSuccessEvent;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.TopicProcessor;

@Slf4j
@Builder
public class XfcdGetDataService extends AbstractExecutionThreadService {

    private XfcdGetDataPublisher xfcdGetDataPublisher;
    private XfcdConfirmDeliveriesSuccessPublisher xfcdConfirmDeliveriesSuccessPublisher;
    private EventBus eventBus;


    @Override
    protected void startUp() throws Exception {
        Flux.from(xfcdConfirmDeliveriesSuccessPublisher)
                .subscribe(new BaseSubscriber<ConfirmDeliveriesSuccessEvent>() {

                    @Override
                    protected void hookOnNext(ConfirmDeliveriesSuccessEvent value) {
                        log.info("got confirmed deliveries SUCCESS");
                        run();
                    }
                });
    }

    @Override
    protected void shutDown() throws Exception {
    }

    @Override
    protected void run() {
        log.debug("About to call publisher {}", xfcdGetDataPublisher.getClass());

        TopicProcessor<DeliveryRestDto> sink = TopicProcessor.create();

        sink
                .doOnNext(deliveryRestDto -> eventBus.post(deliveryRestDto))
                .doOnNext(deliveryRestDto -> {
                    log.trace("received delivery: {}", deliveryRestDto);
                })
                .doOnError(e -> log.error("", e))
                .doOnComplete(() -> {
                    log.info("XfcdGetDataService completed.");
                })
                .subscribe();

        log.info("XfcdGetDataService starting.");
        Flux.from(xfcdGetDataPublisher).subscribe(sink);
    }
}
