package org.amv.trafficsoft.xfcd.consumer.mysql;

import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.xfcd.consumer.jdbc.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@Configuration
@AutoConfigureAfter(TrafficsoftDeliveryJdbcConsumerAutoConfig.class)
@AutoConfigureBefore(TrafficsoftDeliveryJdbcConsumerAutoConfigCompleted.class)
@ConditionalOnClass(com.mysql.jdbc.Driver.class)
@ConditionalOnProperty(
        value = "amv.trafficsoft.xfcd.consumer.jdbc.driverClassName",
        havingValue = "com.mysql.jdbc.Driver"
)
@ConditionalOnBean(name = "trafficsoftDeliveryJdbcConsumerNamedTemplate")
@EnableTransactionManagement
public class TrafficsoftDeliveryMySqlConsumerAutoConfig {

    @Autowired
    @Qualifier("trafficsoftDeliveryJdbcConsumerNamedTemplate")
    private NamedParameterJdbcTemplate namedJdbcTemplate;

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

    @ConditionalOnMissingBean
    @Bean("trafficsoftDeliveryRowMapper")
    public TrafficsoftDeliveryRowMapper deliveryRowMapper() {
        return new TrafficsoftDeliveryRowMapper();
    }

    @Bean("MySqlTrafficsoftDeliveryDao")
    public TrafficsoftDeliveryJdbcDao deliveryDao(TrafficsoftDeliveryRowMapper rowMapper) {
        return new TrafficsoftDeliveryMySqlDaoImpl(namedJdbcTemplate, rowMapper);
    }

    @ConditionalOnMissingBean
    @Bean("trafficsoftXfcdNodeRowMapper")
    public TrafficsoftXfcdNodeRowMapper xfcdNodeRowMapper() {
        return new TrafficsoftXfcdNodeRowMapper();
    }

    @Bean("MySqlTrafficsoftNodeDao")
    public TrafficsoftXfcdNodeJdbcDao xfcdNodeDao(TrafficsoftXfcdNodeRowMapper rowMapper) {
        return new TrafficsoftXfcdNodeMySqlDaoImpl(namedJdbcTemplate, rowMapper);
    }

    @ConditionalOnMissingBean
    @Bean("trafficsoftXfcdStateRowMapper")
    public TrafficsoftXfcdStateRowMapper xfcdStateRowMapper() {
        return new TrafficsoftXfcdStateRowMapper();
    }

    @Bean("MySqlTrafficsoftXfcdStateDao")
    public TrafficsoftXfcdStateJdbcDao xfcdStateDao(TrafficsoftXfcdStateRowMapper rowMapper) {
        return new TrafficsoftXfcdStateMySqlDaoImpl(namedJdbcTemplate, rowMapper);
    }

    @ConditionalOnMissingBean
    @Bean("trafficsoftXfcdXfcdRowMapper")
    public TrafficsoftXfcdXfcdRowMapper xfcdXfcdRowMapper() {
        return new TrafficsoftXfcdXfcdRowMapper();
    }

    @Bean("MySqlTrafficsoftXfcdXfcdDao")
    public TrafficsoftXfcdXfcdJdbcDao xfcdXfcdDao(TrafficsoftXfcdXfcdRowMapper rowMapper) {
        return new TrafficsoftXfcdXfcdMySqlDaoImpl(namedJdbcTemplate, rowMapper);
    }
}
