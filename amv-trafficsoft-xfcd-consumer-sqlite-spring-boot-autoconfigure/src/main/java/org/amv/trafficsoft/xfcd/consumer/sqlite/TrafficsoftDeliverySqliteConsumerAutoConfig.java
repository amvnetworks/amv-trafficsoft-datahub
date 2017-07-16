package org.amv.trafficsoft.xfcd.consumer.sqlite;

import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryConsumer;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryJdbcConsumerAutoConfig;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryJdbcDao;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftDeliveryRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@Configuration
@AutoConfigureAfter(TrafficsoftDeliveryJdbcConsumerAutoConfig.class)
@ConditionalOnClass(org.sqlite.JDBC.class)
@ConditionalOnProperty(
        value = "amv.trafficsoft.xfcd.consumer.jdbc.driverClassName",
        havingValue = "org.sqlite.JDBC"
)
@ConditionalOnBean(name = "trafficsoftDeliveryJdbcConsumerNamedTemplate")
@EnableTransactionManagement
public class TrafficsoftDeliverySqliteConsumerAutoConfig {

    @Autowired
    @Qualifier("trafficsoftDeliveryJdbcConsumerNamedTemplate")
    private NamedParameterJdbcTemplate namedJdbcTemplate;

    @Bean
    public TrafficsoftDeliveryConsumer deliveryConsumer(TrafficsoftDeliveryJdbcDao deliveryDao) {
        return new TrafficsoftDeliverySqliteConsumer(deliveryDao);
    }

    @Bean
    public TrafficsoftDeliveryRowMapper deliveryRowMapper() {
        return new TrafficsoftDeliveryRowMapper();
    }

    @Bean
    public TrafficsoftDeliveryJdbcDao deliveryDao() {
        return new SqliteTrafficsoftDeliveryJdbcDao(namedJdbcTemplate, deliveryRowMapper());
    }
}
