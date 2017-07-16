package org.amv.trafficsoft.datahub.xfcd.event;

import lombok.Builder;
import lombok.Value;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackage;

@Value
@Builder
public class ConfirmedDeliveryPackage {
    private TrafficsoftDeliveryPackage delivery;
}
