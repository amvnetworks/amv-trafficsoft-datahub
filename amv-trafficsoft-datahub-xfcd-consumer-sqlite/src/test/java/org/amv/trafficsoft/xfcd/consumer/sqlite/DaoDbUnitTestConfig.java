/*
================================================================================
  copyright (c) 2016 by AMV Networks GmbH. All rights reserved.
  ----------------------------------------------------------------------------
  module name:     DbUnitTestConfig
  package name:    org.amv.trafficsoft.core.persistence.config
  project:         trafficsoft-persistence
  id:              $Id: DbUnitTestConfig.java 3933 2017-05-18 11:15:28Z alei2 $
  creator:         Elisabeth Rosemann
================================================================================
*/
package org.amv.trafficsoft.xfcd.consumer.sqlite;

import com.github.springtestdbunit.bean.DatabaseConfigBean;
import com.github.springtestdbunit.bean.DatabaseDataSourceConnectionFactoryBean;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.ext.mysql.MySqlMetadataHandler;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@TestConfiguration
@EnableTransactionManagement
public class DaoDbUnitTestConfig {

    @Bean
    public PlatformTransactionManager transactionManager() {
        DataSourceTransactionManager txManager = new DataSourceTransactionManager(dataSource());
        return txManager;
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource());
    }

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
        return new NamedParameterJdbcTemplate(jdbcTemplate());
    }

    /**
     * the connection factory - required since we have a custom H2 DB config in
     * {@link DaoDbUnitTestConfig#dbUnitDatabaseConfig()}
     */
    @Bean
    public DatabaseDataSourceConnectionFactoryBean dbUnitDatabaseConnection() {
        DatabaseDataSourceConnectionFactoryBean bean = new DatabaseDataSourceConnectionFactoryBean();
        bean.setDataSource(dataSource());
        bean.setDatabaseConfig(dbUnitDatabaseConfig());

        return bean;
    }

    /**
     * custom configuration since we have case-sensitive table names and requiring
     * MySQL specifics
     */
    @Bean
    public DatabaseConfigBean dbUnitDatabaseConfig() {
        // making sure the unit tests in H2 use MySQL specifics
        DatabaseConfigBean config = new DatabaseConfigBean();
        config.setDatatypeFactory(new MySqlDataTypeFactory());
        config.setMetadataHandler(new MySqlMetadataHandler());
        config.setCaseSensitiveTableNames(true);
        config.setAllowEmptyFields(true);
        config.setEscapePattern("\"?\"");
        return config;
    }

    @Primary
    @Bean(destroyMethod = "shutdown")
    public DataSource dataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName(org.sqlite.JDBC.class.getName());
        dataSourceBuilder.url("jdbc:sqlite:~amv-trafficsoft-datahub-xfcd-consumer-sqlite-test.db");
        return dataSourceBuilder.build();
    }

    @PostConstruct
    void startSchemaMigration() {
        final Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource());
        flyway.setLocations("classpath:/db/sqlite/xfcd/migration");

        flyway.migrate();
    }
}
