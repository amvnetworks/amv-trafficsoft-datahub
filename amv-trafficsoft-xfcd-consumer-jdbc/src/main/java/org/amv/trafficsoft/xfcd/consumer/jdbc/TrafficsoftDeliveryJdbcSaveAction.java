package org.amv.trafficsoft.xfcd.consumer.jdbc;

import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public interface TrafficsoftDeliveryJdbcSaveAction {
    void apply(JdbcTemplate jdbcTemplate, List<DeliveryRestDto> deliveries);
}
