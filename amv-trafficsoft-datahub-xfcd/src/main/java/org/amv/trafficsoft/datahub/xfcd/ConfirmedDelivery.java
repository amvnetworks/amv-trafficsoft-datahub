package org.amv.trafficsoft.datahub.xfcd;

import lombok.Builder;
import lombok.Value;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;

@Value
@Builder
public class ConfirmedDelivery {
    private DeliveryRestDto delivery;
}
