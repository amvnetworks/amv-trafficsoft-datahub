package org.amv.trafficsoft.datahub.xfcd;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import reactor.core.publisher.WorkQueueProcessor;

@Slf4j
public class XfcdGetDataService extends AbstractExecutionThreadService {

    private final XfcdGetDataPublisher xfcdGetDataPublisher;
    private EventBus eventBus;

    public XfcdGetDataService(XfcdGetDataPublisher xfcdGetDataPublisher, EventBus eventBus) {
        this.xfcdGetDataPublisher = xfcdGetDataPublisher;
        this.eventBus = eventBus;
    }

    @Override
    protected void startUp() throws Exception {
    }

    @Override
    protected void shutDown() throws Exception {
    }

    @Override
    protected void run() throws Exception {
        log.debug("About to call publisher {}", xfcdGetDataPublisher.getClass());
        WorkQueueProcessor<DeliveryRestDto> sink = WorkQueueProcessor.create();

        sink
                .doOnNext(deliveryRestDto -> eventBus.post(deliveryRestDto))
                .doOnNext(deliveryRestDto -> {
                    log.debug("received delivery: {}", deliveryRestDto);
                })
                .doOnError(e -> log.error("", e))
                .doOnComplete(() -> {
                    log.info("completed.");
                });

        xfcdGetDataPublisher.subscribe(sink);

        /*Flux.from(xfcdGetDataPublisher)
                .doOnNext(deliveryRestDto -> eventBus.post(deliveryRestDto))
                .subscribe(deliveryRestDto -> {
                    log.debug("received delivery: {}", deliveryRestDto);
                }, error -> {
                    log.error("", error);
                }, () -> {
                    log.info("completed.");
                });*/
    }
}
