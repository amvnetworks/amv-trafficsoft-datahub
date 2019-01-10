package org.amv.trafficsoft.xfcd.consumer.mysql;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftXfcdNodeEntity;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftXfcdNodeJdbcDao;
import org.springframework.dao.DataAccessException;
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

@Slf4j
@Transactional(transactionManager = "trafficsoftDeliveryJdbcConsumerTransactionManager")
public class TrafficsoftXfcdNodeMySqlDaoImpl implements TrafficsoftXfcdNodeJdbcDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<TrafficsoftXfcdNodeEntity> rowMapper;

    public TrafficsoftXfcdNodeMySqlDaoImpl(NamedParameterJdbcTemplate jdbcTemplate, RowMapper<TrafficsoftXfcdNodeEntity> rowMapper) {
        this.jdbcTemplate = requireNonNull(jdbcTemplate);
        this.rowMapper = requireNonNull(rowMapper);
    }

    @Override
    public void saveAll(List<TrafficsoftXfcdNodeEntity> entities) throws DataAccessException {
        requireNonNull(entities);

        if (entities.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO `amv_trafficsoft_xfcd_node` " +
                "(`CREATED_AT`," +
                "`ID`," +
                "`ALTITUDE`," +
                "`HEADING`," +
                "`HDOP`," +
                "`LATDEG`," +
                "`LONDEG`," +
                "`TS`," +
                "`SATCNT`," +
                "`SPEED`," +
                "`TRIPID`," +
                "`V_ID`," +
                "`VDOP`," +
                "`BPC_ID`," +
                "`IMXFCD_D_ID`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "`UPDATED_AT` = ?";

        entities.forEach(entity -> {
            jdbcTemplate.getJdbcOperations().update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, new String[]{});
                Timestamp now = Timestamp.from(Instant.now());
                ps.setTimestamp(1, now);
                ps.setLong(2, entity.getId());
                ps.setObject(3, entity.getAltitude()
                        .map(val -> val.setScale(2, BigDecimal.ROUND_HALF_UP))
                        .orElse(null));
                ps.setObject(4, entity.getHeading()
                        .map(val -> val.setScale(2, BigDecimal.ROUND_HALF_UP))
                        .orElse(null));
                ps.setObject(5, entity.getHorizontalDilution()
                        .map(val -> val.setScale(1, BigDecimal.ROUND_HALF_UP))
                        .orElse(null));
                ps.setObject(6, entity.getLatitude()
                        .map(val -> val.setScale(6, BigDecimal.ROUND_HALF_UP))
                        .orElse(null));
                ps.setObject(7, entity.getLongitude()
                        .map(val -> val.setScale(6, BigDecimal.ROUND_HALF_UP))
                        .orElse(null));
                ps.setLong(8, entity.getTimestamp()
                        .toEpochMilli());
                ps.setObject(9, entity.getSatelliteCount().orElse(null));
                ps.setObject(10, entity.getSpeed()
                        .map(val -> val.setScale(2, BigDecimal.ROUND_HALF_UP))
                        .orElse(null));
                ps.setLong(11, entity.getTripId());
                ps.setLong(12, entity.getVehicleId());
                ps.setObject(13, entity.getVerticalDilution()
                        .map(val -> val.setScale(1, BigDecimal.ROUND_HALF_UP))
                        .orElse(null));
                ps.setLong(14, entity.getBusinessPartnerId());
                ps.setLong(15, entity.getDeliveryId());
                ps.setTimestamp(16, now);
                return ps;
            });
        });
    }

    @Override
    public List<TrafficsoftXfcdNodeEntity> findByContractIdAndDeliveryId(long bpcId, long deliveryId) {
        String sql = "SELECT n.`ID`, n.`IMXFCD_D_ID`, " +
                "n.`BPC_ID`,  n.`V_ID`, n.`TRIPID`, n.`TS`, " +
                "n.`LONDEG`, n.`LATDEG`, n.`SPEED`, n.`HEADING`, " +
                "n.`ALTITUDE`, n.`SATCNT`, n.`HDOP`, n.`VDOP` " +
                "FROM `amv_trafficsoft_xfcd_node` n " +
                "WHERE n.`BPC_ID` = :bpcId AND " +
                "n.`IMXFCD_D_ID` = :deliveryId " +
                "ORDER BY n.`IMXFCD_D_ID`, n.`V_ID`, n.`TRIPID`, n.`ID`";

        List<TrafficsoftXfcdNodeEntity> nodes = jdbcTemplate.query(sql, ImmutableMap.<String, Object>builder()
                .put("bpcId", bpcId)
                .put("deliveryId", deliveryId)
                .build(), rowMapper);

        return ImmutableList.copyOf(nodes);
    }

    @Override
    public List<TrafficsoftXfcdNodeEntity> findByContractIdAndDeliveryIds(long bpcId, List<Long> deliveryIds) {
        requireNonNull(deliveryIds, "`deliveryIds` must not be null");

        if (deliveryIds.isEmpty()) {
            return Collections.emptyList();
        }

        String sql = "SELECT n.`ID`, n.`IMXFCD_D_ID`, " +
                "n.`BPC_ID`,  n.`V_ID`, n.`TRIPID`, n.`TS`, " +
                "n.`LONDEG`, n.`LATDEG`, n.`SPEED`, n.`HEADING`, " +
                "n.`ALTITUDE`, n.`SATCNT`, n.`HDOP`, n.`VDOP` " +
                "FROM `amv_trafficsoft_xfcd_node` n " +
                "WHERE n.`BPC_ID` = :bpcId AND " +
                "n.`IMXFCD_D_ID` IN(:deliveryIds) " +
                "ORDER BY n.`IMXFCD_D_ID`, n.`V_ID`, n.`TRIPID`, n.`ID`";

        List<TrafficsoftXfcdNodeEntity> nodes = jdbcTemplate.query(sql, ImmutableMap.<String, Object>builder()
                .put("bpcId", bpcId)
                .put("deliveryIds", deliveryIds)
                .build(), rowMapper);

        return ImmutableList.copyOf(nodes);
    }
}
