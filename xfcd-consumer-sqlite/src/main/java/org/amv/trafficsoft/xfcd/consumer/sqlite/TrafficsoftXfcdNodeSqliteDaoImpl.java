package org.amv.trafficsoft.xfcd.consumer.sqlite;

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

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@Slf4j
@Transactional(transactionManager = "trafficsoftDeliveryJdbcConsumerTransactionManager")
public class TrafficsoftXfcdNodeSqliteDaoImpl implements TrafficsoftXfcdNodeJdbcDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<TrafficsoftXfcdNodeEntity> rowMapper;

    public TrafficsoftXfcdNodeSqliteDaoImpl(NamedParameterJdbcTemplate jdbcTemplate, RowMapper<TrafficsoftXfcdNodeEntity> rowMapper) {
        this.jdbcTemplate = requireNonNull(jdbcTemplate);
        this.rowMapper = requireNonNull(rowMapper);
    }

    @Override
    public void saveAll(List<TrafficsoftXfcdNodeEntity> nodes) throws DataAccessException {
        requireNonNull(nodes);

        /**
         * `INSERT OR IGNORE` is used here so no DuplicateKeyException is thrown
         * if the delivery has already been saved.
         * This might happen if a delivery has been saved but not confirmed
         * for example due to an application shutdown.
         */
        String sql = "INSERT OR IGNORE INTO `amv_trafficsoft_xfcd_node` " +
                "(`CREATED_AT`, " +
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
                "VALUES (:createdAt, :id, :altitude, :heading, :hdop, :latdeg, :londeg, :ts," +
                " :satcnt, :speed, :tripid, :vehicleId, :vdop, :bpcId, :deliveryId)";


        nodes.forEach(node -> {
            Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(15);

            paramMap.put("createdAt", Timestamp.from(Instant.now()));
            paramMap.put("id", node.getId());
            paramMap.put("tripid", node.getTripId());
            paramMap.put("vehicleId", node.getVehicleId());
            paramMap.put("ts", node.getTimestamp().toEpochMilli());
            paramMap.put("altitude", node.getAltitude().orElse(null));
            paramMap.put("heading", node.getHeading().orElse(null));
            paramMap.put("hdop", node.getHorizontalDilution().orElse(null));
            paramMap.put("latdeg", node.getLatitude().orElse(null));
            paramMap.put("londeg", node.getLongitude().orElse(null));
            paramMap.put("satcnt", node.getSatelliteCount().orElse(null));
            paramMap.put("speed", node.getSpeed().orElse(null));
            paramMap.put("vdop", node.getVerticalDilution().orElse(null));
            paramMap.put("bpcId", node.getBusinessPartnerId());
            paramMap.put("deliveryId", node.getDeliveryId());

            jdbcTemplate.update(sql, paramMap);
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
