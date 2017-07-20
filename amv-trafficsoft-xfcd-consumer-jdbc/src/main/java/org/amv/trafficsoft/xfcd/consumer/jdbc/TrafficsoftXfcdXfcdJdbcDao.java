package org.amv.trafficsoft.xfcd.consumer.jdbc;

import org.springframework.dao.DataAccessException;

import java.util.Collections;
import java.util.List;

public interface TrafficsoftXfcdXfcdJdbcDao {

    void saveAll(List<TrafficsoftXfcdXfcdEntity> entities) throws DataAccessException;

    default void save(TrafficsoftXfcdXfcdEntity entity) throws DataAccessException {
        saveAll(Collections.singletonList(entity));
    }

    List<TrafficsoftXfcdXfcdEntity> findByNodeIds(List<Long> nodeIds);

    default List<TrafficsoftXfcdXfcdEntity> findByNodeId(long nodeId) {
        return findByNodeIds(Collections.singletonList(nodeId));
    }
}
