package org.amv.trafficsoft.datahub.xfcd;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggingDeliveryPackageEventHandler {

    public LoggingDeliveryPackageEventHandler(EventBus eventBus) {
        eventBus.register(this);
    }

    @Subscribe
    public void onNext(TrafficsoftDeliveryPackage value) {
        if (log.isDebugEnabled()) {
            log.debug("{}", value);
        }
    }
}
