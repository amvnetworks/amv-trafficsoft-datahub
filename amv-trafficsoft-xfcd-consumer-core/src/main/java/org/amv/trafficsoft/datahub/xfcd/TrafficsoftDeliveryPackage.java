package org.amv.trafficsoft.datahub.xfcd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@JsonDeserialize(as = TrafficsoftDeliveryPackageImpl.class)
public interface TrafficsoftDeliveryPackage {
    long getContractId();

    List<DeliveryRestDto> getDeliveries();

    @JsonIgnore
    default List<Long> getDeliveryIds() {
        return Optional.ofNullable(getDeliveries())
                .orElse(Collections.emptyList()).stream()
                .map(DeliveryRestDto::getDeliveryId)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    default boolean isEmpty() {
        return Optional.ofNullable(getDeliveries())
                .map(Collection::isEmpty)
                .orElse(true);
    }
}
