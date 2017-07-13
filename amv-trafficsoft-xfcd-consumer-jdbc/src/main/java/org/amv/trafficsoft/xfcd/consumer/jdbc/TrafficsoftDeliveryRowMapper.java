package org.amv.trafficsoft.xfcd.consumer.jdbc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@Slf4j
public class TrafficsoftDeliveryRowMapper implements RowMapper<TrafficsoftDeliveryEntity> {

    @Override
    public TrafficsoftDeliveryEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        long id = rs.getLong("id");
        Instant timestamp = rs.getTimestamp("ts")
                .toInstant();
        Instant confirmedAtOrNull = Optional.ofNullable(rs.getTimestamp("confirmed"))
                .map(Timestamp::toInstant)
                .orElse(null);

        return TrafficsoftDeliveryEntity.builder()
                .id(id)
                .timestamp(timestamp)
                .confirmedAt(confirmedAtOrNull)
                .build();
    }
}
