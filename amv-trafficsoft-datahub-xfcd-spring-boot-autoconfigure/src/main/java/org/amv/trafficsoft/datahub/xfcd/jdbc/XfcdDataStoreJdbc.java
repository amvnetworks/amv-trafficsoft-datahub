package org.amv.trafficsoft.datahub.xfcd.jdbc;

import lombok.Builder;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackage;
import org.amv.trafficsoft.datahub.xfcd.XfcdDataStore;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryPackageJdbcDao;

import static java.util.Objects.requireNonNull;

public class XfcdDataStoreJdbc implements XfcdDataStore {

    private final TrafficsoftDeliveryPackageJdbcDao deliveryPackageDao;
    private final boolean primaryDataStore;

    @Builder
    XfcdDataStoreJdbc(TrafficsoftDeliveryPackageJdbcDao deliveryPackageDao, boolean primaryDataStore) {
        this.deliveryPackageDao = requireNonNull(deliveryPackageDao);
        this.primaryDataStore = primaryDataStore;
    }

    @Override
    public void save(TrafficsoftDeliveryPackage deliveryPackage) {
        deliveryPackageDao.save(deliveryPackage);
    }

    @Override
    public boolean isPrimaryDataStore() {
        return primaryDataStore;
    }
}
