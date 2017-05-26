package org.amv.trafficsoft.datahub.xfcd;

import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import reactor.core.publisher.BaseSubscriber;

public class CassandraDeliverySink extends BaseSubscriber<DeliveryRestDto>
        implements XfcdDeliveryFluxSink {

    public CassandraDeliverySink() {

    }

    @Override
    protected void hookOnNext(DeliveryRestDto value) {

    }

}
