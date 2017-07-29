package org.amv.trafficsoft.xfcd.consumer.mysql;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftXfcdXfcdEntity;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftXfcdXfcdJdbcDao;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@Transactional(transactionManager = "trafficsoftDeliveryJdbcConsumerTransactionManager")
public class TrafficsoftXfcdXfcdMySqlDaoImpl implements TrafficsoftXfcdXfcdJdbcDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<TrafficsoftXfcdXfcdEntity> rowMapper;

    public TrafficsoftXfcdXfcdMySqlDaoImpl(NamedParameterJdbcTemplate jdbcTemplate, RowMapper<TrafficsoftXfcdXfcdEntity> rowMapper) {
        this.jdbcTemplate = requireNonNull(jdbcTemplate);
        this.rowMapper = requireNonNull(rowMapper);
    }

    @Override
    public void saveAll(List<TrafficsoftXfcdXfcdEntity> entities) {
        requireNonNull(entities);

        String sql = "INSERT INTO `amv_trafficsoft_xfcd_xfcd` " +
                "(`CREATED_AT`, `IMXFCD_N_ID`, `TYPE`, `VAL`, `VALSTR`) " +
                "VALUES (:now, :nodeId, :type, :value, :valueAsString) " +
                "ON DUPLICATE KEY UPDATE " +
                "`UPDATED_AT` = :now";

        entities.forEach(entity -> {
            Map<String, Object> paramMap = Maps.newHashMap();
            paramMap.put("now", Date.from(Instant.now()));
            paramMap.put("nodeId", entity.getNodeId());
            paramMap.put("type", entity.getType());
            paramMap.put("value", entity.getValue().orElse(null));
            paramMap.put("valueAsString", entity.getValueAsString().orElse(null));

            jdbcTemplate.update(sql, paramMap);
        });
    }

    @Override
    public List<TrafficsoftXfcdXfcdEntity> findByNodeId(long nodeId) {
        String sql = "SELECT `IMXFCD_N_ID`, `TYPE`, `VAL`, `VALSTR` " +
                "FROM `IMXFCD_Xfcd` " +
                "WHERE `IMXFCD_N_ID` = :nodeId " +
                "ORDER BY `TYPE`";

        List<TrafficsoftXfcdXfcdEntity> xfcds = jdbcTemplate.query(sql, ImmutableMap.<String, Object>builder()
                .put("nodeId", nodeId)
                .build(), rowMapper);

        return ImmutableList.copyOf(xfcds);
    }

    @Override
    public List<TrafficsoftXfcdXfcdEntity> findByNodeIds(List<Long> nodeIds) {
        requireNonNull(nodeIds, "`nodeIds` must not be null");

        if (nodeIds.isEmpty()) {
            return Collections.emptyList();
        }

        String sql = "SELECT `IMXFCD_N_ID`, `TYPE`, `VAL`, `VALSTR` " +
                "FROM `IMXFCD_Xfcd` " +
                "WHERE `IMXFCD_N_ID` IN(:nodeIds) " +
                "ORDER BY `TYPE`";

        List<TrafficsoftXfcdXfcdEntity> xfcds = jdbcTemplate.query(sql, ImmutableMap.<String, Object>builder()
                .put("nodeIds", nodeIds)
                .build(), rowMapper);

        return ImmutableList.copyOf(xfcds);
    }
}
