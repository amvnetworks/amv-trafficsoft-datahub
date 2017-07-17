package org.amv.trafficsoft.datahub.xfcd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@JsonDeserialize(as = TrafficsoftDeliveryPackageImpl.class)
public interface TrafficsoftDeliveryPackage {
    List<DeliveryRestDto> getDeliveries();

    @JsonIgnore
    default List<Long> getDelivieryIds() {
        return Optional.ofNullable(getDeliveries())
                .orElse(Collections.emptyList()).stream()
                .map(DeliveryRestDto::getDeliveryId)
                .collect(Collectors.toList());
    }
}
