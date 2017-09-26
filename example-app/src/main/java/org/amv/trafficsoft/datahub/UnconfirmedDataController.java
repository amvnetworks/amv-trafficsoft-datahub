package org.amv.trafficsoft.datahub;

import io.vertx.core.json.Json;
import org.amv.trafficsoft.rest.client.autoconfigure.TrafficsoftApiRestProperties;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryJdbcDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/data")
public class UnconfirmedDataController {

    @Autowired
    TrafficsoftApiRestProperties apiRestProperties;

    @Autowired
    TrafficsoftDeliveryJdbcDao dao;

    @GetMapping("/unconfirmed")
    public String index() {
        final List<Long> idsOfUnconfirmedDeliveriesByBpcId = dao
                .findIdsOfUnconfirmedDeliveriesByBpcId(apiRestProperties.getContractId());
        return Json.encodePrettily(idsOfUnconfirmedDeliveriesByBpcId);
    }
}
