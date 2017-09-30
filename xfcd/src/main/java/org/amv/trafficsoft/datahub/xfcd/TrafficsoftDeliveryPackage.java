package org.amv.trafficsoft.datahub.xfcd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.amv.trafficsoft.rest.xfcd.model.TrackRestDto;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableList.toImmutableList;

@JsonDeserialize(as = TrafficsoftDeliveryPackageImpl.class)
public interface TrafficsoftDeliveryPackage {
    long getContractId();

    List<DeliveryRestDto> getDeliveries();

    @JsonIgnore
    default List<Long> getDeliveryIds() {
        return Optional.ofNullable(getDeliveries())
                .orElse(Collections.emptyList()).stream()
                .map(DeliveryRestDto::getDeliveryId)
                .collect(toImmutableList());
    }

    @JsonIgnore
    default boolean isEmpty() {
        return Optional.ofNullable(getDeliveries())
                .map(Collection::isEmpty)
                .orElse(true);
    }

    @JsonIgnore
    default int getAmountOfNodes() {
        return Optional.ofNullable(getDeliveries())
                .orElse(Collections.emptyList())
                .stream()
                .map(DeliveryRestDto::getTrack)
                .flatMap(Collection::stream)
                .map(TrackRestDto::getNodes)
                .mapToInt(Collection::size)
                .sum();
    }
}
