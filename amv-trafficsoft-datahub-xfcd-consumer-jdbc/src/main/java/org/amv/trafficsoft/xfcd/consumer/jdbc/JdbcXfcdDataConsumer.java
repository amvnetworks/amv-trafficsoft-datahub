package org.amv.trafficsoft.xfcd.consumer.jdbc;

import lombok.Builder;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackage;
import org.amv.trafficsoft.datahub.xfcd.XfcdDataConsumer;

import static java.util.Objects.requireNonNull;

public class JdbcXfcdDataConsumer implements XfcdDataConsumer {

    private final TrafficsoftDeliveryPackageJdbcDao deliveryPackageDao;
    private final boolean sendConfirmationEvents;

    @Builder
    JdbcXfcdDataConsumer(TrafficsoftDeliveryPackageJdbcDao deliveryPackageDao, boolean sendConfirmationEvents) {
        this.deliveryPackageDao = requireNonNull(deliveryPackageDao);
        this.sendConfirmationEvents = sendConfirmationEvents;
    }

    @Override
    public void consume(TrafficsoftDeliveryPackage deliveryPackage) {
        deliveryPackageDao.save(deliveryPackage);
    }

    @Override
    public boolean sendsConfirmationEvents() {
        return sendConfirmationEvents;
    }
}
