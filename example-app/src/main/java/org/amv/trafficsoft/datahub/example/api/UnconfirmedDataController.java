package org.amv.trafficsoft.datahub.example.api;

import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryJdbcDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/data")
public class UnconfirmedDataController {

    @Value("${amv.trafficsoft.rest.contract-id}")
    private long contractId;

    @Autowired
    TrafficsoftDeliveryJdbcDao dao;

    @GetMapping("/unconfirmed")
    public List<Long> index() {
        return dao.findIdsOfUnconfirmedDeliveriesByBpcId(contractId);
    }
}
