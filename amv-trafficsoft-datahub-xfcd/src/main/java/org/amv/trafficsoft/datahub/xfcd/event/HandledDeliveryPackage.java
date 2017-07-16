package org.amv.trafficsoft.datahub.xfcd.event;

import lombok.Builder;
import lombok.Value;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackage;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;

@Value
@Builder
public class HandledDeliveryPackage {
    private TrafficsoftDeliveryPackage delivery;
}
