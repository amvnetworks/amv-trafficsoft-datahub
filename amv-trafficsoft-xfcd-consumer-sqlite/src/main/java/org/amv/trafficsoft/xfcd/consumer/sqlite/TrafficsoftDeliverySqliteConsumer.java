package org.amv.trafficsoft.xfcd.consumer.sqlite;

import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryConsumer;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackage;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryEntity;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryJdbcDao;
import reactor.core.publisher.BaseSubscriber;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@Slf4j
public class TrafficsoftDeliverySqliteConsumer extends BaseSubscriber<TrafficsoftDeliveryPackage> implements TrafficsoftDeliveryConsumer {

    private final TrafficsoftDeliveryJdbcDao deliveryDao;

    public TrafficsoftDeliverySqliteConsumer(TrafficsoftDeliveryJdbcDao deliveryDao) {
        this.deliveryDao = requireNonNull(deliveryDao);
    }

    @Override
    protected void hookOnNext(TrafficsoftDeliveryPackage deliveryPackage) {
        requireNonNull(deliveryPackage, "`deliveryPackage` must not be null");

        final List<DeliveryRestDto> deliveries = deliveryPackage.getDeliveries();
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

        deliveryDao.saveAll(deliveryEntities);
    }
}
