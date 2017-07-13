package org.amv.trafficsoft.xfcd.consumer.sqlite;

import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryConsumer;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryEntity;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryJdbcDao;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@Slf4j
public class TrafficsoftDeliverySqliteConsumer implements TrafficsoftDeliveryConsumer {

    private final TrafficsoftDeliveryJdbcDao saveAction;

    public TrafficsoftDeliverySqliteConsumer(TrafficsoftDeliveryJdbcDao saveAction) {
        this.saveAction = requireNonNull(saveAction);
    }

    @Override
    public void accept(List<DeliveryRestDto> deliveries) {
        requireNonNull(deliveries, "`deliveries` must not be null");

        if (deliveries.isEmpty()) {
            return;
        }

        final List<TrafficsoftDeliveryEntity> deliveryEntities = deliveries.stream()
                .map(val -> TrafficsoftDeliveryEntity.builder()
                        .id(val.getDeliveryId())
                        .timestamp(val.getTimestamp().toInstant())
                        .confirmedAt(null)
                        .build())
                .collect(toList());

        saveAction.saveAll(deliveryEntities);
    }
}
