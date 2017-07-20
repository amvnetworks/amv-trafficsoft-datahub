package org.amv.trafficsoft.xfcd.consumer.sqlite;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftXfcdXfcdEntity;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftXfcdXfcdJdbcDao;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class TrafficsoftXfcdXfcdSqliteDaoImpl implements TrafficsoftXfcdXfcdJdbcDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<TrafficsoftXfcdXfcdEntity> rowMapper;

    public TrafficsoftXfcdXfcdSqliteDaoImpl(NamedParameterJdbcTemplate jdbcTemplate, RowMapper<TrafficsoftXfcdXfcdEntity> rowMapper) {
        this.jdbcTemplate = requireNonNull(jdbcTemplate);
        this.rowMapper = requireNonNull(rowMapper);
    }

    @Override
    @Transactional
    public void saveAll(List<TrafficsoftXfcdXfcdEntity> entities) {
        requireNonNull(entities);

        /**
         * `INSERT OR IGNORE` is used here so no DuplicateKeyException is thrown
         * if the delivery has already been saved.
         * This might happen if a delivery has been saved but not confirmed
         * for example due to an application shutdown.
         */
        String sql = "INSERT OR IGNORE INTO `amv_trafficsoft_xfcd_xfcd` " +
                "(`IMXFCD_N_ID`, `TYPE`, `VAL`, `VALSTR`) " +
                "VALUES (:nodeId, :type, :value, :valueAsString)";

        entities.forEach(entity -> {
            Map<String, Object> paramMap = Maps.newHashMap();
            paramMap.put("nodeId", entity.getNodeId());
            paramMap.put("type", entity.getType());
            paramMap.put("value", entity.getValue().orElse(null));
            paramMap.put("valueAsString", entity.getValueAsString().orElse(null));

            jdbcTemplate.update(sql, paramMap);
        });
    }

    @Override
    @Transactional
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
    @Transactional
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
