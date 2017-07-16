package org.amv.trafficsoft.xfcd.consumer.jdbc;

import org.apache.commons.lang3.RandomUtils;

import java.time.Instant;

import static com.google.common.base.Preconditions.checkArgument;


public final class TrafficsoftDeliveryEntityMother {
    private TrafficsoftDeliveryEntityMother() {
        throw new UnsupportedOperationException();
    }

    public static TrafficsoftDeliveryEntity random() {
        return TrafficsoftDeliveryEntity.builder()
                .id(RandomUtils.nextLong())
                .timestamp(Instant.now())
                .confirmedAt(Instant.now())
                .build();
    }

    public static TrafficsoftDeliveryEntity randomUnconfirmed() {
        final TrafficsoftDeliveryEntity deliveryDbo = random().toBuilder()
                .confirmedAt(null)
                .build();

        checkArgument(!deliveryDbo.isConfirmed(), "Sanity check");

        return deliveryDbo;
    }
}
