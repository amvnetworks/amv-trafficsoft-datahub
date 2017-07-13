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
import com.google.common.collect.ImmutableMap;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.ext.mysql.MySqlMetadataHandler;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Configuration class for the entity smoke test - connects to a real database,
 * so usually not used.
 *
 * @author <a href='mailto:elisabeth.rosemann@amv-networks.com'>Elisabeth
 *         Rosemann</a>
 * @version $Revision: 3933 $
 * @since 27.06.2016
 */
@Configuration
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

    /**
     * In-memory H2 database setup
    @Bean(destroyMethod = "shutdown")
    public DataSource dataSource() {
        String databaseName = "dao_unit_test_db";
        String options = ImmutableMap.builder()
                .put("DB_CLOSE_DELAY", String.valueOf(-1))
                .put("DB_CLOSE_ON_EXIT", String.valueOf(false))
                .put("MODE", "MySQL")
                .put("DATABASE_TO_UPPER", String.valueOf(false))
                .build().entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(";"));

        String url = Stream.of(databaseName, options)
                .collect(Collectors.joining(";"));

        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName(url)
                //.addScript("classpath:/org/amv/trafficsoft/core/dao/schema.sql")
                .addScript("classpath:/db/migration/V1__init.sql")
                .build();
    }
     */

    @Primary
    @Bean(destroyMethod = "shutdown")
    public DataSource dataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("org.sqlite.JDBC");
        dataSourceBuilder.url("jdbc:sqlite:~amv-trafficsoft-xfcd-consumer-sqlite-test.db");
        return dataSourceBuilder.build();
    }
}
