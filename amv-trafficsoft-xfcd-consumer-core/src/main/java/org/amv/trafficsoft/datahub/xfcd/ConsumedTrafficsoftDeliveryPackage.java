package org.amv.trafficsoft.datahub.xfcd;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ConsumedTrafficsoftDeliveryPackage {
    private TrafficsoftDeliveryPackage delivery;
}
