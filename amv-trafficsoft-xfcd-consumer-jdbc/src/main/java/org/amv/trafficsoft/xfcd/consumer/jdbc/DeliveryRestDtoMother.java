package org.amv.trafficsoft.xfcd.consumer.jdbc;

import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.amv.trafficsoft.rest.xfcd.model.TrackRestDto;
import org.apache.commons.lang3.RandomUtils;

import java.sql.Date;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

// TODO move to test package of amv-trafficsoft-rest-model module
public final class DeliveryRestDtoMother {
    private DeliveryRestDtoMother() {
        throw new UnsupportedOperationException();
    }

    public static DeliveryRestDto random() {
        return DeliveryRestDto.builder()
                .deliveryId(RandomUtils.nextLong())
                .timestamp(Date.from(Instant.now()))
                .addTrack(TrackRestDto.builder()
                        .id(RandomUtils.nextLong())
                        .vehicleId(RandomUtils.nextLong())
                        .build())
                .build();
    }

    public static List<DeliveryRestDto> randomList() {
        return Collections.singletonList(random());
    }
}
