package org.amv.trafficsoft.xfcd.consumer.jdbc;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TrafficsoftXfcdStateRowMapper implements RowMapper<TrafficsoftXfcdStateEntity> {
    @Override
    public TrafficsoftXfcdStateEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        long nodeId = rs.getLong("IMXFCD_N_ID");
        String code = rs.getString("CD");
        String valueOrNull = rs.getString("VAL");

        return TrafficsoftXfcdStateEntity.builder()
                .nodeId(nodeId)
                .code(code)
                .value(valueOrNull)
                .build();
    }
}
