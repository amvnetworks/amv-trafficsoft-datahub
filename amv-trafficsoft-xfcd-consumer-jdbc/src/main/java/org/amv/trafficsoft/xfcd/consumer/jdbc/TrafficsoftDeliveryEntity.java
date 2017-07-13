package org.amv.trafficsoft.xfcd.consumer.jdbc;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Optional;

@Value
@Builder(toBuilder = true)
public class TrafficsoftDeliveryEntity {
    private long id;
    private Instant timestamp;
    private Instant confirmedAt;

    public Optional<Instant> getConfirmedAt() {
        return Optional.ofNullable(confirmedAt);
    }

    public boolean isConfirmed() {
        return getConfirmedAt().isPresent();
    }
}
