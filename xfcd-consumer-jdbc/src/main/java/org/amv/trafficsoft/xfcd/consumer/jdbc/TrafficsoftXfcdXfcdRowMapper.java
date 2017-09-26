package org.amv.trafficsoft.xfcd.consumer.jdbc;

import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TrafficsoftXfcdXfcdRowMapper implements RowMapper<TrafficsoftXfcdXfcdEntity> {
    @Override
    public TrafficsoftXfcdXfcdEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        long nodeId = rs.getLong("IMXFCD_N_ID");
        String type = rs.getString("TYPE");
        BigDecimal valueOrNull = rs.getBigDecimal("VAL");
        String valueAsStringOrNull = rs.getString("VALSTR");

        return TrafficsoftXfcdXfcdEntity.builder()
                .nodeId(nodeId)
                .type(type)
                .value(valueOrNull)
                .valueAsString(valueAsStringOrNull)
                .build();
    }
}
