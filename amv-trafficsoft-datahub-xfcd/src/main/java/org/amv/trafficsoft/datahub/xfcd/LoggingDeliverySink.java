package org.amv.trafficsoft.datahub.xfcd;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;

@Slf4j
public class LoggingDeliverySink {

    public LoggingDeliverySink(EventBus eventBus) {
        eventBus.register(this);
    }

    @Subscribe
    public void onNext(DeliveryRestDto value) {
        //log.info("{}", value);
    }
}
