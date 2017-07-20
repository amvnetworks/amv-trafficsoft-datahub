package org.amv.trafficsoft.datahub.xfcd.event;


import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackage;

@Value
@Builder(builderClassName = "Builder")
public class IncomingDeliveryEvent implements XfcdEvent {
    @NonNull
    private TrafficsoftDeliveryPackage deliveryPackage;
}