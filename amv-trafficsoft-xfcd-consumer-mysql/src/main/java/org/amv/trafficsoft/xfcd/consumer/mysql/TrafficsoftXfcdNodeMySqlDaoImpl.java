package org.amv.trafficsoft.xfcd.consumer.mysql;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftXfcdNodeEntity;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftXfcdNodeJdbcDao;
import org.springframework.dao.DataAccessException;
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
    public void saveAll(List<TrafficsoftXfcdNodeEntity> nodes) throws DataAccessException {
        requireNonNull(nodes);

        String sql = "INSERT INTO `amv_trafficsoft_xfcd_node` " +
                "(`CREATED_AT` ," +
                "`ID` ," +
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
                "VALUES (:now, :id, :altitude, :heading, :hdop, :latdeg, :londeg, :ts," +
                " :satcnt, :speed, :tripid, :vehicleId, :vdop, :bpcId, :deliveryId) " +
                "ON DUPLICATE KEY UPDATE " +
                "`UPDATED_AT` = :now";


        nodes.forEach(node -> {
            final Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(15);
            paramMap.put("now", Date.from(Instant.now()));
            paramMap.put("id", node.getId());
            paramMap.put("altitude", node.getAltitude()
                    .map(val -> val.setScale(2, BigDecimal.ROUND_HALF_UP))
                    .orElse(null));
            paramMap.put("heading", node.getHeading()
                    .map(val -> val.setScale(2, BigDecimal.ROUND_HALF_UP))
                    .orElse(null));
            paramMap.put("hdop", node.getHorizontalDilution()
                    .map(val -> val.setScale(1, BigDecimal.ROUND_HALF_UP))
                    .orElse(null));
            paramMap.put("latdeg", node.getLatitude()
                    .map(val -> val.setScale(6, BigDecimal.ROUND_HALF_UP))
                    .orElse(null));
            paramMap.put("londeg", node.getLongitude()
                    .map(val -> val.setScale(6, BigDecimal.ROUND_HALF_UP))
                    .orElse(null));
            paramMap.put("ts", node.getTimestamp()
                    .orElseGet(Instant::now)
                    .toEpochMilli());
            paramMap.put("satcnt", node.getSatelliteCount());
            paramMap.put("speed", node.getSpeed()
                    .map(val -> val.setScale(2, BigDecimal.ROUND_HALF_UP))
                    .orElse(null));
            paramMap.put("tripid", node.getTripId());
            paramMap.put("vehicleId", node.getVehicleId());
            paramMap.put("vdop", node.getVerticalDilution()
                    .map(val -> val.setScale(1, BigDecimal.ROUND_HALF_UP))
                    .orElse(null));
            paramMap.put("bpcId", node.getBpcId());
            paramMap.put("deliveryId", node.getDeliveryId());

            jdbcTemplate.update(sql, paramMap);
        });
    }

    @Override
    public List<TrafficsoftXfcdNodeEntity> findByContractIdAndDeliveryId(int bpcId, long deliveryId) {
        String sql = "SELECT n.`ID`, n.`IMXFCD_D_ID`, " +
                "n.`BPC_ID`,  n.`V_ID`, n.`TRIPID`, n.`TS`, " +
                "n.`LONDEG`, n.`LATDEG`, n.`SPEED`, n.`HEADING`, " +
                "n.`ALTITUDE`, n.`SATCNT`, n.`HDOP`, n.`VDOP` " +
                "FROM `IMXFCD_Node` n " +
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
    public List<TrafficsoftXfcdNodeEntity> findByContractIdAndDeliveryIds(int bpcId, List<Long> deliveryIds) {
        requireNonNull(deliveryIds, "`deliveryIds` must not be null");

        if (deliveryIds.isEmpty()) {
            return Collections.emptyList();
        }

        String sql = "SELECT n.`ID`, n.`IMXFCD_D_ID`, " +
                "n.`BPC_ID`,  n.`V_ID`, n.`TRIPID`, n.`TS`, " +
                "n.`LONDEG`, n.`LATDEG`, n.`SPEED`, n.`HEADING`, " +
                "n.`ALTITUDE`, n.`SATCNT`, n.`HDOP`, n.`VDOP` " +
                "FROM `IMXFCD_Node` n " +
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
