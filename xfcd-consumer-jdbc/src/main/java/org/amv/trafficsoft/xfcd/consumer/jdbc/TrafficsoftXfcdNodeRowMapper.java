package org.amv.trafficsoft.xfcd.consumer.jdbc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

@Slf4j
public class TrafficsoftXfcdNodeRowMapper implements RowMapper<TrafficsoftXfcdNodeEntity> {

    @Override
    public TrafficsoftXfcdNodeEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        long id = rs.getLong("ID");
        long bpcId = rs.getLong("BPC_ID");
        long deliveryId = rs.getLong("IMXFCD_D_ID");
        long vehicleId = rs.getLong("V_ID");
        long tripId = rs.getLong("TRIPID");
        Instant timestamp = Instant.ofEpochMilli(rs.getLong("TS"));
        BigDecimal longitudeOrNull = rs.getBigDecimal("LONDEG");
        BigDecimal latitudeOrNull = rs.getBigDecimal("LATDEG");
        BigDecimal speedOrNull = rs.getBigDecimal("SPEED");
        BigDecimal headingOrNull = rs.getBigDecimal("HEADING");
        BigDecimal altitudeOrNull = rs.getBigDecimal("ALTITUDE");
        Integer satelliteCountOrNull = MoreResultSets.getInteger(rs, "SATCNT").orElse(null);
        BigDecimal horizontalDilutionOrNull = rs.getBigDecimal("HDOP");
        BigDecimal verticalDilutionOrNull = rs.getBigDecimal("VDOP");

        return TrafficsoftXfcdNodeEntity.builder()
                .id(id)
                .businessPartnerId(bpcId)
                .deliveryId(deliveryId)
                .vehicleId(vehicleId)
                .tripId(tripId)
                .timestamp(timestamp)
                .longitude(longitudeOrNull)
                .latitude(latitudeOrNull)
                .speed(speedOrNull)
                .heading(headingOrNull)
                .altitude(altitudeOrNull)
                .satelliteCount(satelliteCountOrNull)
                .horizontalDilution(horizontalDilutionOrNull)
                .verticalDilution(verticalDilutionOrNull)
                .build();
    }
}
