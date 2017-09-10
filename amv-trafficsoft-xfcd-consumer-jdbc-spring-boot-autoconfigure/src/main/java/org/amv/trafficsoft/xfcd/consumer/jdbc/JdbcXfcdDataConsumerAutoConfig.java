package org.amv.trafficsoft.xfcd.consumer.jdbc;

import com.codahale.metrics.MetricRegistry;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Properties;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration
@EnableConfigurationProperties(TrafficsoftXfcdJdbcProperties.class)
@AutoConfigureBefore(TrafficsoftDeliveryJdbcConsumerAutoConfigCompleted.class)
public class JdbcXfcdDataConsumerAutoConfig {

    /**
     * Reasons for static declaration: created very early in the application’s lifecycle
     * allows the bean to be created without having to instantiate the @Configuration class
     * <p>
     * https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html#boot-features-external-config-validation
     */
    @Bean
    public static TrafficsoftXfcdJdbcPropertiesValidator configurationPropertiesValidator() {
        return new TrafficsoftXfcdJdbcPropertiesValidator();
    }

    @Configuration
    @EnableTransactionManagement
    @ConditionalOnProperty("amv.trafficsoft.xfcd.consumer.jdbc.enabled")
    public class TrafficsoftDeliveryJdbcConsumerConfig {

        private final TrafficsoftXfcdJdbcProperties properties;

        @Autowired(required = false)
        private MetricRegistry metricRegistry;

        @Autowired
        public TrafficsoftDeliveryJdbcConsumerConfig(TrafficsoftXfcdJdbcProperties trafficsoftXfcdJdbcProperties) {
            this.properties = requireNonNull(trafficsoftXfcdJdbcProperties);
        }

        @Bean(name = "trafficsoftDeliveryJdbcConsumerHikariConfig")
        public HikariConfig jdbcConsumerHikariConfig() {
            HikariConfig config = new HikariConfig();

            properties.getDriverClassName()
                    .ifPresent(config::setDriverClassName);

            config.setJdbcUrl(properties.getJdbcUrl());
            config.setUsername(properties.getUsername());
            config.setPassword(properties.getPassword());
            config.setPoolName("trafficsoft-xfcd-consumer-jdbc-pool");

            if (metricRegistry != null) {
                config.setMetricRegistry(metricRegistry);
            }

            config.setMaximumPoolSize(properties.getPool().getMaxPoolSize());
            config.setInitializationFailTimeout(properties.getPool().getInitializationFailTimeout());

            Map<String, String> dataSource = properties.getDataSource();
            Properties dataSourceProperties = new Properties();
            dataSourceProperties.putAll(dataSource);
            config.setDataSourceProperties(dataSourceProperties);

            return config;
        }

        @Bean(name = "trafficsoftDeliveryJdbcConsumerDataSource")
        public HikariDataSource jdbcConsumerHikariDataSource() {
            return new HikariDataSource(jdbcConsumerHikariConfig());
        }

        @Bean(name = "trafficsoftDeliveryJdbcConsumerTemplate")
        public JdbcTemplate jdbcConsumerJdbcTemplate() {
            return new JdbcTemplate(jdbcConsumerHikariDataSource());
        }

        @Bean(name = "trafficsoftDeliveryJdbcConsumerNamedTemplate")
        public NamedParameterJdbcTemplate jdbcConsumerJdbcNamedTemplate() {
            return new NamedParameterJdbcTemplate(jdbcConsumerHikariDataSource());
        }

        @Bean(name = "trafficsoftDeliveryJdbcConsumerTransactionManager")
        public PlatformTransactionManager jdbcConsumerTransactionManager() {
            return new DataSourceTransactionManager(jdbcConsumerHikariDataSource());
        }

        @PostConstruct
        public void postConstruct() {
            final boolean skipSchemaMigration = !properties.isSchemaMigrationEnabled();

            if (skipSchemaMigration) {
                log.info("Skipping Trafficsoft Delivery schema migration");
            } else {
                startSchemaMigration();
            }
        }

        private void startSchemaMigration() {
            final Flyway flyway = new Flyway();
            flyway.setDataSource(jdbcConsumerHikariDataSource());
            flyway.setLocations(properties.getFlywayScriptsLocation());

            log.info("Starting Trafficsoft Delivery schema migration v{}", flyway.getBaselineVersion().getVersion());

            flyway.migrate();
        }
    }
}
