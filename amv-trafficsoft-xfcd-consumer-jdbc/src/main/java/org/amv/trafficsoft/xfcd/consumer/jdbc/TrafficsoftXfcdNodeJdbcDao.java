package org.amv.trafficsoft.xfcd.consumer.jdbc;


import org.springframework.dao.DataAccessException;

import java.util.Collections;
import java.util.List;

public interface TrafficsoftXfcdNodeJdbcDao {

    /**
     * @param nodes a list of nodes
     * @throws DataAccessException
     */
    void saveAll(List<TrafficsoftXfcdNodeEntity> nodes) throws DataAccessException;

    default void save(TrafficsoftXfcdNodeEntity node) throws DataAccessException {
        saveAll(Collections.singletonList(node));
    }

    List<TrafficsoftXfcdNodeEntity> findByContractIdAndDeliveryId(int bpcId, long deliveryId);

    List<TrafficsoftXfcdNodeEntity> findByContractIdAndDeliveryIds(int bpcId, List<Long> deliveryIds);
}
