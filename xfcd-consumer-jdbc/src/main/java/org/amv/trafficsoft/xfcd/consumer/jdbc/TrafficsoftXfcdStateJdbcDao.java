package org.amv.trafficsoft.xfcd.consumer.jdbc;

import org.springframework.dao.DataAccessException;

import java.util.Collections;
import java.util.List;

public interface TrafficsoftXfcdStateJdbcDao {
    void saveAll(List<TrafficsoftXfcdStateEntity> entities) throws DataAccessException;

    default void save(TrafficsoftXfcdStateEntity entity) throws DataAccessException {
        saveAll(Collections.singletonList(entity));
    }

    List<TrafficsoftXfcdStateEntity> findByNodeIds(List<Long> nodeIds);

    default List<TrafficsoftXfcdStateEntity> findByNodeId(long nodeId) {
        return findByNodeIds(Collections.singletonList(nodeId));
    }
}
