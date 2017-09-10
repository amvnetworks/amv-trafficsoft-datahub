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

import java.util.Map;

@Slf4j
@Configuration
@AutoConfigureAfter(JdbcXfcdDataConsumerAutoConfig.class)
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

    @Autowired
    public TrafficsoftDeliveryMySqlConsumerAutoConfig(TrafficsoftXfcdJdbcProperties trafficsoftXfcdJdbcProperties) {
        updateDataSourcePropertiesIfNecessary(trafficsoftXfcdJdbcProperties);
    }

    private void updateDataSourcePropertiesIfNecessary(TrafficsoftXfcdJdbcProperties trafficsoftXfcdJdbcProperties) {
        Map<String, String> dataSourceProperties = trafficsoftXfcdJdbcProperties.getDataSource();
        Map<String, String> enhencedDataSourceProperties = addMissingMySqlDefaultDataSourceProperties(dataSourceProperties);
        trafficsoftXfcdJdbcProperties.setDataSource(enhencedDataSourceProperties);
    }

    private Map<String, String> addMissingMySqlDefaultDataSourceProperties(Map<String, String> dataSourcePropertiesMap) {
        dataSourcePropertiesMap.putIfAbsent("cachePrepStmts", String.valueOf(true));
        dataSourcePropertiesMap.putIfAbsent("prepStmtCacheSize", String.valueOf(250));
        dataSourcePropertiesMap.putIfAbsent("prepStmtCacheSqlLimit", String.valueOf(2048));
        dataSourcePropertiesMap.putIfAbsent("useServerPrepStmts", String.valueOf(true));
        dataSourcePropertiesMap.putIfAbsent("useLocalSessionState", String.valueOf(true));
        dataSourcePropertiesMap.putIfAbsent("useLocalTransactionState", String.valueOf(true));
        dataSourcePropertiesMap.putIfAbsent("rewriteBatchedStatements", String.valueOf(true));
        dataSourcePropertiesMap.putIfAbsent("cacheResultSetMetadata", String.valueOf(true));
        dataSourcePropertiesMap.putIfAbsent("cacheServerConfiguration", String.valueOf(true));
        dataSourcePropertiesMap.putIfAbsent("elideSetAutoCommits", String.valueOf(true));
        dataSourcePropertiesMap.putIfAbsent("maintainTimeStats", String.valueOf(false));

        return dataSourcePropertiesMap;
    }

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

    @Bean("mySqlTrafficsoftDeliveryDao")
    public TrafficsoftDeliveryJdbcDao deliveryDao(TrafficsoftDeliveryRowMapper rowMapper) {
        return new TrafficsoftDeliveryMySqlDaoImpl(namedJdbcTemplate, rowMapper);
    }

    @ConditionalOnMissingBean
    @Bean("trafficsoftXfcdNodeRowMapper")
    public TrafficsoftXfcdNodeRowMapper xfcdNodeRowMapper() {
        return new TrafficsoftXfcdNodeRowMapper();
    }

    @Bean("mySqlTrafficsoftNodeDao")
    public TrafficsoftXfcdNodeJdbcDao xfcdNodeDao(TrafficsoftXfcdNodeRowMapper rowMapper) {
        return new TrafficsoftXfcdNodeMySqlDaoImpl(namedJdbcTemplate, rowMapper);
    }

    @ConditionalOnMissingBean
    @Bean("trafficsoftXfcdStateRowMapper")
    public TrafficsoftXfcdStateRowMapper xfcdStateRowMapper() {
        return new TrafficsoftXfcdStateRowMapper();
    }

    @Bean("mySqlTrafficsoftXfcdStateDao")
    public TrafficsoftXfcdStateJdbcDao xfcdStateDao(TrafficsoftXfcdStateRowMapper rowMapper) {
        return new TrafficsoftXfcdStateMySqlDaoImpl(namedJdbcTemplate, rowMapper);
    }

    @ConditionalOnMissingBean
    @Bean("trafficsoftXfcdXfcdRowMapper")
    public TrafficsoftXfcdXfcdRowMapper xfcdXfcdRowMapper() {
        return new TrafficsoftXfcdXfcdRowMapper();
    }

    @Bean("mySqlTrafficsoftXfcdXfcdDao")
    public TrafficsoftXfcdXfcdJdbcDao xfcdXfcdDao(TrafficsoftXfcdXfcdRowMapper rowMapper) {
        return new TrafficsoftXfcdXfcdMySqlDaoImpl(namedJdbcTemplate, rowMapper);
    }
}
