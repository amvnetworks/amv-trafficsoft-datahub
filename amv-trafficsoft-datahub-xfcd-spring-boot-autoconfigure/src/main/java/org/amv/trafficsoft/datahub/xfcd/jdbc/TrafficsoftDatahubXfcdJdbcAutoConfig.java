package org.amv.trafficsoft.datahub.xfcd.jdbc;

import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDatahubXfcdAutoConfig;
import org.amv.trafficsoft.datahub.xfcd.event.XfcdEvents;
import org.amv.trafficsoft.xfcd.consumer.jdbc.*;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@AutoConfigureAfter(TrafficsoftDatahubXfcdAutoConfig.class)
public class TrafficsoftDatahubXfcdJdbcAutoConfig {

    @ConditionalOnMissingBean(TrafficsoftDeliveryPackageJdbcDao.class)
    @Bean("delegatingTrafficsoftDeliveryPackageDao")
    public TrafficsoftDeliveryPackageJdbcDao deliveryPackageDao(TrafficsoftDeliveryJdbcDao deliveryDao,
                                                                TrafficsoftXfcdNodeJdbcDao xfcdNodeDao,
                                                                TrafficsoftXfcdStateJdbcDao xfcdStateDao,
                                                                TrafficsoftXfcdXfcdJdbcDao xfcdXfcdDao) {
        return DelegatingTrafficsoftDeliveryPackageDao.builder()
                .deliveryDao(deliveryDao)
                .nodeDao(xfcdNodeDao)
                .stateDao(xfcdStateDao)
                .xfcdDao(xfcdXfcdDao)
                .build();
    }

    @Bean("trafficsoftDeliveryJdbcVerticle")
    public TrafficsoftDeliveryJdbcVerticle trafficsoftDeliveryJdbcVerticle(XfcdEvents xfcdEvents,
                                                                           TrafficsoftDeliveryPackageJdbcDao trafficsoftDeliveryPackageJdbcDao) {
        return TrafficsoftDeliveryJdbcVerticle.builder()
                .xfcdEvents(xfcdEvents)
                .deliveryPackageDao(trafficsoftDeliveryPackageJdbcDao)
                .primaryDataStore(true)
                .build();
    }

}
