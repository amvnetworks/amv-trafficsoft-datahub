package org.amv.trafficsoft.xfcd.consumer.sqlite;

import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.xfcd.consumer.jdbc.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
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

    @ConditionalOnMissingBean
    @Bean("trafficsoftDeliveryRowMapper")
    public TrafficsoftDeliveryRowMapper deliveryRowMapper() {
        return new TrafficsoftDeliveryRowMapper();
    }

    @Bean("sqliteTrafficsoftDeliveryDao")
    public TrafficsoftDeliveryJdbcDao deliveryDao(TrafficsoftDeliveryRowMapper rowMapper) {
        return new TrafficsoftDeliverySqliteDaoImpl(namedJdbcTemplate, rowMapper);
    }

    @ConditionalOnMissingBean
    @Bean("trafficsoftXfcdNodeRowMapper")
    public TrafficsoftXfcdNodeRowMapper xfcdNodeRowMapper() {
        return new TrafficsoftXfcdNodeRowMapper();
    }

    @Bean("sqliteTrafficsoftNodeDao")
    public TrafficsoftXfcdNodeJdbcDao xfcdNodeDao(TrafficsoftXfcdNodeRowMapper rowMapper) {
        return new TrafficsoftXfcdNodeSqliteDaoImpl(namedJdbcTemplate, rowMapper);
    }

    @ConditionalOnMissingBean
    @Bean("trafficsoftXfcdStateRowMapper")
    public TrafficsoftXfcdStateRowMapper xfcdStateRowMapper() {
        return new TrafficsoftXfcdStateRowMapper();
    }

    @Bean("sqliteTrafficsoftXfcdStateDao")
    public TrafficsoftXfcdStateJdbcDao xfcdStateDao(TrafficsoftXfcdStateRowMapper rowMapper) {
        return new TrafficsoftXfcdStateSqliteDaoImpl(namedJdbcTemplate, rowMapper);
    }

    @ConditionalOnMissingBean
    @Bean("trafficsoftXfcdXfcdRowMapper")
    public TrafficsoftXfcdXfcdRowMapper xfcdXfcdRowMapper() {
        return new TrafficsoftXfcdXfcdRowMapper();
    }

    @Bean("sqliteTrafficsoftXfcdXfcdDao")
    public TrafficsoftXfcdXfcdJdbcDao xfcdXfcdDao(TrafficsoftXfcdXfcdRowMapper rowMapper) {
        return new TrafficsoftXfcdXfcdSqliteDaoImpl(namedJdbcTemplate, rowMapper);
    }
}
