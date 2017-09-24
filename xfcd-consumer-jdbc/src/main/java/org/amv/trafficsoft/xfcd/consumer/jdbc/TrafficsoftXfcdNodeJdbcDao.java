package org.amv.trafficsoft.xfcd.consumer.jdbc;

import java.util.Collections;
import java.util.List;

public interface TrafficsoftXfcdNodeJdbcDao {

    void saveAll(List<TrafficsoftXfcdNodeEntity> nodes);

    default void save(TrafficsoftXfcdNodeEntity node) {
        saveAll(Collections.singletonList(node));
    }

    List<TrafficsoftXfcdNodeEntity> findByContractIdAndDeliveryId(int bpcId, long deliveryId);

    List<TrafficsoftXfcdNodeEntity> findByContractIdAndDeliveryIds(int bpcId, List<Long> deliveryIds);
}
