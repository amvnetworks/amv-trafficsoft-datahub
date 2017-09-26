package org.amv.trafficsoft.xfcd.consumer.jdbc;

import java.util.Collections;
import java.util.List;

public interface TrafficsoftXfcdNodeJdbcDao {

    void saveAll(List<TrafficsoftXfcdNodeEntity> nodes);

    default void save(TrafficsoftXfcdNodeEntity node) {
        saveAll(Collections.singletonList(node));
    }

    List<TrafficsoftXfcdNodeEntity> findByContractIdAndDeliveryIds(long bpcId, List<Long> deliveryIds);

    default List<TrafficsoftXfcdNodeEntity> findByContractIdAndDeliveryId(long bpcId, long deliveryId) {
        return findByContractIdAndDeliveryIds(bpcId, Collections.singletonList(deliveryId));
    }
}
