package org.amv.trafficsoft.datahub.xfcd;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;

import java.util.List;

@Value
@Builder(builderClassName = "Builder")
public class TrafficsoftDeliveryPackageImpl implements TrafficsoftDeliveryPackage {
    @Singular("addDelivery")
    private List<DeliveryRestDto> deliveries;
}
