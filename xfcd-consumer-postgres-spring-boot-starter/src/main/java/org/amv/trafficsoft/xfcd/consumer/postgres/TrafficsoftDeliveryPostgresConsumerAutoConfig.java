package org.amv.trafficsoft.xfcd.consumer.postgres;

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
@AutoConfigureAfter(JdbcIncomingDeliveryConsumerAutoConfig.class)
@AutoConfigureBefore(JdbcIncomingDeliveryConsumerAutoConfigCompleted.class)
@ConditionalOnClass(org.postgresql.Driver.class)
@ConditionalOnProperty(
        value = "amv.trafficsoft.xfcd.consumer.jdbc.driverClassName",
        havingValue = "org.postgresql.Driver"
)
@ConditionalOnBean(name = "trafficsoftDeliveryJdbcConsumerNamedTemplate")
@EnableTransactionManagement
public class TrafficsoftDeliveryPostgresConsumerAutoConfig {

    @Autowired
    @Qualifier("trafficsoftDeliveryJdbcConsumerNamedTemplate")
    private NamedParameterJdbcTemplate namedJdbcTemplate;

    @Autowired
    public TrafficsoftDeliveryPostgresConsumerAutoConfig(TrafficsoftXfcdJdbcProperties trafficsoftXfcdJdbcProperties) {
        updateDataSourcePropertiesIfNecessary(trafficsoftXfcdJdbcProperties);
    }

    private void updateDataSourcePropertiesIfNecessary(TrafficsoftXfcdJdbcProperties trafficsoftXfcdJdbcProperties) {
        Map<String, String> dataSourceProperties = trafficsoftXfcdJdbcProperties.getDataSource();
        Map<String, String> enhancedDataSourceProperties = addMissingPostgresDefaultDataSourceProperties(dataSourceProperties);
        trafficsoftXfcdJdbcProperties.setDataSource(enhancedDataSourceProperties);
    }

    private Map<String, String> addMissingPostgresDefaultDataSourceProperties(Map<String, String> dataSourcePropertiesMap) {
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

    @Bean("postgresTrafficsoftDeliveryDao")
    public TrafficsoftDeliveryJdbcDao deliveryDao(TrafficsoftDeliveryRowMapper rowMapper) {
        return new TrafficsoftDeliveryPostgresDaoImpl(namedJdbcTemplate, rowMapper);
    }

    @ConditionalOnMissingBean
    @Bean("trafficsoftXfcdNodeRowMapper")
    public TrafficsoftXfcdNodeRowMapper xfcdNodeRowMapper() {
        return new TrafficsoftXfcdNodeRowMapper();
    }

    @Bean("postgresTrafficsoftNodeDao")
    public TrafficsoftXfcdNodeJdbcDao xfcdNodeDao(TrafficsoftXfcdNodeRowMapper rowMapper) {
        return new TrafficsoftXfcdNodePostgresDaoImpl(namedJdbcTemplate, rowMapper);
    }

    @ConditionalOnMissingBean
    @Bean("trafficsoftXfcdStateRowMapper")
    public TrafficsoftXfcdStateRowMapper xfcdStateRowMapper() {
        return new TrafficsoftXfcdStateRowMapper();
    }

    @Bean("postgresTrafficsoftXfcdStateDao")
    public TrafficsoftXfcdStateJdbcDao xfcdStateDao(TrafficsoftXfcdStateRowMapper rowMapper) {
        return new TrafficsoftXfcdStatePostgresDaoImpl(namedJdbcTemplate, rowMapper);
    }

    @ConditionalOnMissingBean
    @Bean("trafficsoftXfcdXfcdRowMapper")
    public TrafficsoftXfcdXfcdRowMapper xfcdXfcdRowMapper() {
        return new TrafficsoftXfcdXfcdRowMapper();
    }

    @Bean("postgresTrafficsoftXfcdXfcdDao")
    public TrafficsoftXfcdXfcdJdbcDao xfcdXfcdDao(TrafficsoftXfcdXfcdRowMapper rowMapper) {
        return new TrafficsoftXfcdXfcdPostgresDaoImpl(namedJdbcTemplate, rowMapper);
    }
}
