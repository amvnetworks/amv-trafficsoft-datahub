package org.amv.trafficsoft.xfcd.consumer.jdbc;

import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TrafficsoftXfcdXfcdRowMapper implements RowMapper<TrafficsoftXfcdXfcdEntity> {
    @Override
    public TrafficsoftXfcdXfcdEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        String type = rs.getString("TYPE");
        long nodeId = rs.getLong("IMXFCD_N_ID");
        BigDecimal valueOrNull = rs.getBigDecimal("VAL");
        String valueAsStringOrNull = rs.getString("VALSTR");

        return TrafficsoftXfcdXfcdEntity.builder()
                .type(type)
                .nodeId(nodeId)
                .value(valueOrNull)
                .valueAsString(valueAsStringOrNull)
                .build();
    }
}
