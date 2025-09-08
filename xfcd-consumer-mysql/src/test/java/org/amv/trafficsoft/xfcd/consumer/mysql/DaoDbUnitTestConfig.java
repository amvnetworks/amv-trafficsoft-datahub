package org.amv.trafficsoft.xfcd.consumer.mysql;

import com.github.springtestdbunit.bean.DatabaseConfigBean;
import com.github.springtestdbunit.bean.DatabaseDataSourceConnectionFactoryBean;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.ext.mysql.MySqlMetadataHandler;
import org.flywaydb.core.Flyway;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;

@TestConfiguration
@EnableTransactionManagement
public class DaoDbUnitTestConfig {
    private static final String SCHEMA_NAME = "amv_trafficsoft_xfcd_consumer_mysql_test";

    @Bean(destroyMethod = "stop")
    public MySQLContainer<?> mysqlContainer() {
        MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:5.7"))
                .withDatabaseName(SCHEMA_NAME)
                .withUsername("differentUser")
                .withPassword("anotherPassword")
                .withExposedPorts(3306);
        
        mysql.start();
        return mysql;
    }

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
        DatabaseConfigBean config = new DatabaseConfigBean();
        config.setDatatypeFactory(new MySqlDataTypeFactory());
        config.setMetadataHandler(new MySqlMetadataHandler());
        config.setCaseSensitiveTableNames(true);
        config.setAllowEmptyFields(true);
        return config;
    }

    @Primary
    @Bean
    public DataSource dataSource() {
        final MySQLContainer<?> mysqlContainer = mysqlContainer(); // make sure MySQL container is started.

        final String url = String.format("jdbc:mysql://localhost:%d/%s?profileSQL=true&generateSimpleParameterMetadata=true",
                mysqlContainer.getFirstMappedPort(),
                SCHEMA_NAME);

        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.username(mysqlContainer.getUsername());
        dataSourceBuilder.password(mysqlContainer.getPassword());
        dataSourceBuilder.driverClassName(com.mysql.jdbc.Driver.class.getName());
        dataSourceBuilder.url(url);
        return dataSourceBuilder.build();
    }

    @PostConstruct
    void startSchemaMigration() {
        final Flyway flyway = Flyway.configure()
            .sqlMigrationPrefix("V")
            .dataSource(dataSource())
            .locations("classpath:/db/mysql/xfcd/migration")
            .load();

        flyway.migrate();
    }
}
