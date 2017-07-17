package org.amv.trafficsoft.datahub;

import io.vertx.core.json.Json;
import org.amv.trafficsoft.rest.client.autoconfigure.TrafficsoftApiRestProperties;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryJdbcDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/data")
public class LastDataController {

    @Autowired
    TrafficsoftApiRestProperties apiRestProperties;

    @Autowired
    TrafficsoftDeliveryJdbcDao dao;

    @GetMapping("/unconfirmed")
    public String index() {
        final List<Long> idsOfUnconfirmedDeliveriesByBpcId = dao
                .findIdsOfUnconfirmedDeliveriesByBpcId((int) apiRestProperties.getContractId());
        return Json.encode(idsOfUnconfirmedDeliveriesByBpcId);
    }
}
