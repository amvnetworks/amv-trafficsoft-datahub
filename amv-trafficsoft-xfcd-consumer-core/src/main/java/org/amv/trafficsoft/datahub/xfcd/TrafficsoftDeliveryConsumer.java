package org.amv.trafficsoft.datahub.xfcd;


import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;

import java.util.List;

public interface TrafficsoftDeliveryConsumer {
    void accept(List<DeliveryRestDto> deliveries);
}
