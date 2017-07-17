package org.amv.trafficsoft.xfcd.consumer.jdbc;

import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackage;
import org.springframework.dao.DataAccessException;

public interface TrafficsoftDeliveryPackageJdbcDao {
    void save(TrafficsoftDeliveryPackage deliveryPackage) throws DataAccessException;
}
