package org.amv.trafficsoft.xfcd.consumer.mysql;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftXfcdXfcdEntity;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftXfcdXfcdJdbcDao;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
                "VALUES (:now, :node_id, :val_type, :val, :val_as_string) " +
                "ON DUPLICATE KEY UPDATE " +
                "`UPDATED_AT` = :now";

        entities.forEach(entity -> {
            Map<String, Object> paramMap = Maps.newHashMap();
            paramMap.put("now", Date.from(Instant.now()));
            paramMap.put("node_id", entity.getNodeId());
            paramMap.put("val_type", entity.getType());

            // TODO: if val is "null" it throws an exception
            // even as "VAL" is declared "nullable" -> Investigate!
            paramMap.put("val", entity.getValue()
                    .map(val -> val.setScale(6, BigDecimal.ROUND_HALF_UP))
                    .orElse(BigDecimal.ZERO));
            paramMap.put("val_as_string", entity.getValueAsString().orElse(null));

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
