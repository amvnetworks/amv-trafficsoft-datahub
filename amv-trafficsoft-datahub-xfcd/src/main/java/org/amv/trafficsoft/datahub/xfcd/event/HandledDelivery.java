package org.amv.trafficsoft.datahub.xfcd.event;

import lombok.Builder;
import lombok.Value;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;

@Value
@Builder
public class HandledDelivery {
    private DeliveryRestDto delivery;
}
