package org.amv.trafficsoft.xfcd.consumer.sqlite;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftXfcdStateEntity;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftXfcdStateJdbcDao;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@Transactional(transactionManager = "trafficsoftDeliveryJdbcConsumerTransactionManager")
public class TrafficsoftXfcdStateSqliteDaoImpl implements TrafficsoftXfcdStateJdbcDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<TrafficsoftXfcdStateEntity> rowMapper;

    public TrafficsoftXfcdStateSqliteDaoImpl(NamedParameterJdbcTemplate jdbcTemplate, RowMapper<TrafficsoftXfcdStateEntity> rowMapper) {
        this.jdbcTemplate = requireNonNull(jdbcTemplate);
        this.rowMapper = requireNonNull(rowMapper);
    }

    @Override
    public void saveAll(List<TrafficsoftXfcdStateEntity> entities) {
        requireNonNull(entities);

        /**
         * `INSERT OR IGNORE` is used here so no DuplicateKeyException is thrown
         * if the delivery has already been saved.
         * This might happen if a delivery has been saved but not confirmed
         * for example due to an application shutdown.
         */
        String sql = "INSERT OR IGNORE INTO `amv_trafficsoft_xfcd_state` " +
                "(`IMXFCD_N_ID`, `CD`, `VAL`) " +
                "VALUES (:nodeId, :code, :value)";

        entities.forEach(entity -> {
            Map<String, Object> paramMap = Maps.newHashMap();
            paramMap.put("nodeId", entity.getNodeId());
            paramMap.put("code", entity.getCode());
            paramMap.put("value", entity.getValue().orElse(null));

            jdbcTemplate.update(sql, paramMap);
        });
    }

    @Override
    public List<TrafficsoftXfcdStateEntity> findByNodeId(long nodeId) {
        String sql = "SELECT `CD`, `VAL`, `IMXFCD_N_ID` " +
                "FROM `IMXFCD_State` " +
                "WHERE `IMXFCD_N_ID` = :nodeId " +
                "ORDER BY `CD`";

        List<TrafficsoftXfcdStateEntity> states = jdbcTemplate.query(sql, ImmutableMap.<String, Object>builder()
                .put("nodeId", nodeId)
                .build(), rowMapper);

        return ImmutableList.copyOf(states);
    }

    @Override
    public List<TrafficsoftXfcdStateEntity> findByNodeIds(List<Long> nodeIds) {
        requireNonNull(nodeIds, "`nodeIds` must not be null");

        if (nodeIds.isEmpty()) {
            return Collections.emptyList();
        }

        String sql = "SELECT `CD`, `VAL`, `IMXFCD_N_ID` " +
                "FROM `IMXFCD_State` " +
                "WHERE `IMXFCD_N_ID` IN(:nodeIds) " +
                "ORDER BY `CD`";

        List<TrafficsoftXfcdStateEntity> states = jdbcTemplate.query(sql, ImmutableMap.<String, Object>builder()
                .put("nodeIds", nodeIds)
                .build(), rowMapper);

        return ImmutableList.copyOf(states);
    }
}
