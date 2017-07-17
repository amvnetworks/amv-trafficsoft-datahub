package org.amv.trafficsoft.datahub.xfcd.jdbc;

import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDatahubXfcdAutoConfig;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryJdbcDao;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@AutoConfigureAfter(TrafficsoftDatahubXfcdAutoConfig.class)
//@ConditionalOnBean(TrafficsoftDeliveryJdbcDao.class)
public class TrafficsoftDatahubXfcdJdbcAutoConfig {

    @Bean
    public TrafficsoftDeliveryJdbcVerticle trafficsoftDeliveryJdbcVerticle(TrafficsoftDeliveryJdbcDao trafficsoftDeliveryJdbcDao) {
        return TrafficsoftDeliveryJdbcVerticle.builder()
                .deliveryDao(trafficsoftDeliveryJdbcDao)
                .primaryDataStore(true)
                .build();
    }

}
