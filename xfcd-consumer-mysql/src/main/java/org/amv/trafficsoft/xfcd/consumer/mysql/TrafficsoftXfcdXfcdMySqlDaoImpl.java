package org.amv.trafficsoft.xfcd.consumer.mysql;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftXfcdXfcdEntity;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftXfcdXfcdJdbcDao;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

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

        if (entities.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO `amv_trafficsoft_xfcd_xfcd` " +
                "(`CREATED_AT`, `IMXFCD_N_ID`, `TYPE`, `VAL`, `VALSTR`) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "`UPDATED_AT` = ?";

        entities.forEach(entity -> {
            jdbcTemplate.getJdbcOperations().update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, new String[]{});
                Timestamp now = Timestamp.from(Instant.now());
                ps.setTimestamp(1, now);
                ps.setLong(2, entity.getNodeId());
                ps.setString(3, entity.getType());
                ps.setObject(4, entity.getValue()
                        .map(val -> val.setScale(6, BigDecimal.ROUND_HALF_UP))
                        .orElse(null));
                ps.setString(5, entity.getValueAsString().orElse(null));
                ps.setTimestamp(6, now);
                return ps;
            });
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
