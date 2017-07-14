package org.amv.trafficsoft.datahub.xfcd;

import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;

import java.util.List;

public interface TrafficsoftDeliveryPackage {
    List<DeliveryRestDto> getDeliveries();
}
