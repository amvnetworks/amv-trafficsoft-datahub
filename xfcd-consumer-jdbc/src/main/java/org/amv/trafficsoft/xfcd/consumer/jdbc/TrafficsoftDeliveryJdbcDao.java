package org.amv.trafficsoft.xfcd.consumer.jdbc;

import org.springframework.dao.DataAccessException;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface TrafficsoftDeliveryJdbcDao {

    /**
     * @param deliveries a list of deliveries
     * @throws DataAccessException in case of an underlying jdbc exception
     */
    void saveAll(List<TrafficsoftDeliveryEntity> deliveries) throws DataAccessException;

    default void save(TrafficsoftDeliveryEntity delivery) throws DataAccessException {
        saveAll(Collections.singletonList(delivery));
    }

    List<TrafficsoftDeliveryEntity> findByIds(List<Long> deliveryIds);

    default Optional<TrafficsoftDeliveryEntity> findById(long deliveryId) {
        return findByIds(Collections.singletonList(deliveryId)).stream()
                .findFirst();
    }

    List<Long> findIdsOfUnconfirmedDeliveriesByBpcId(long bpcId);

    void confirmDeliveriesByIds(Collection<Long> ids);

    default void confirmDeliveryById(long deliveryId) {
        confirmDeliveriesByIds(Collections.singletonList(deliveryId));
    }
}
