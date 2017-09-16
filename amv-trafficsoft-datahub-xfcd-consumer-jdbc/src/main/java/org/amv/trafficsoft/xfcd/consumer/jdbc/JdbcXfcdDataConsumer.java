package org.amv.trafficsoft.xfcd.consumer.jdbc;

import lombok.Builder;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackage;
import org.amv.trafficsoft.datahub.xfcd.DeliveryConsumer;

import static java.util.Objects.requireNonNull;

public class JdbcXfcdDataConsumer implements DeliveryConsumer {

    private final TrafficsoftDeliveryPackageJdbcDao deliveryPackageDao;

    @Builder
    JdbcXfcdDataConsumer(TrafficsoftDeliveryPackageJdbcDao deliveryPackageDao) {
        this.deliveryPackageDao = requireNonNull(deliveryPackageDao);
    }

    @Override
    public void consume(TrafficsoftDeliveryPackage deliveryPackage) {
        deliveryPackageDao.save(deliveryPackage);
    }

}
