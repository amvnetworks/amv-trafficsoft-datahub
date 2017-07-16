package org.amv.trafficsoft.xfcd.consumer.jdbc;

import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackage;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackageSubscriber;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import reactor.core.publisher.BaseSubscriber;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@Slf4j
public class TrafficsoftDeliveryJdbcPackageSubscriberImpl extends BaseSubscriber<TrafficsoftDeliveryPackage> implements TrafficsoftDeliveryPackageSubscriber {

    private final TrafficsoftDeliveryJdbcDao deliveryDao;

    public TrafficsoftDeliveryJdbcPackageSubscriberImpl(TrafficsoftDeliveryJdbcDao deliveryDao) {
        this.deliveryDao = requireNonNull(deliveryDao);
    }

    @Override
    protected void hookOnNext(TrafficsoftDeliveryPackage deliveryPackage) {
        requireNonNull(deliveryPackage, "`deliveryPackage` must not be null");

        final List<DeliveryRestDto> deliveries = deliveryPackage.getDeliveries();

        if (log.isDebugEnabled()) {
            log.debug("saving {} deliveries", deliveries.size());
        }

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
