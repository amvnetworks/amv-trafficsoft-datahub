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
        int bpcId = rs.getInt("BPC_ID");
        long deliveryId = rs.getLong("IMXFCD_D_ID");
        long vehicleId = rs.getLong("V_ID");
        long tripId = rs.getLong("TRIPID");
        Instant timestamp = Instant.ofEpochMilli(rs.getLong("TS"));
        BigDecimal londeg = rs.getBigDecimal("LONDEG");
        BigDecimal latdeg = rs.getBigDecimal("LATDEG");
        BigDecimal speed = rs.getBigDecimal("SPEED");
        BigDecimal heading = rs.getBigDecimal("HEADING");
        BigDecimal altitude = rs.getBigDecimal("ALTITUDE");
        int satcnt = rs.getInt("SATCNT");
        BigDecimal hdop = rs.getBigDecimal("HDOP");
        BigDecimal vdop = rs.getBigDecimal("VDOP");

        return TrafficsoftXfcdNodeEntity.builder()
                .id(id)
                .businessPartnerId(bpcId)
                .deliveryId(deliveryId)
                .vehicleId(vehicleId)
                .tripId(tripId)
                .timestamp(timestamp)
                .longitude(londeg)
                .latitude(latdeg)
                .speed(speed)
                .heading(heading)
                .altitude(altitude)
                .satelliteCount(satcnt)
                .horizontalDilution(hdop)
                .verticalDilution(vdop)
                .build();
    }
}
