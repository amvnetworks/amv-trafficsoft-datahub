package org.amv.trafficsoft.xfcd.consumer.mysql;

import com.github.springtestdbunit.bean.DatabaseConfigBean;
import com.github.springtestdbunit.bean.DatabaseDataSourceConnectionFactoryBean;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.ext.mysql.MySqlMetadataHandler;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import java.time.Duration;

@TestConfiguration
@EnableTransactionManagement
public class DaoDbUnitTestConfig {
    private static final Logger LOG = LoggerFactory.getLogger(DaoDbUnitTestConfig.class);
    private static final String SCHEMA_NAME = "amv_trafficsoft_xfcd_consumer_mysql_test";

    @Bean(destroyMethod = "stop")
    public MySQLContainer<?> mysqlContainer() {
        DockerImageName image = DockerImageName.parse("mysql:8.0.30");
        LOG.info("[DEBUG_LOG] Preparing MySQL Testcontainer with image={}", image);

        MySQLContainer<?> mysql = new MySQLContainer<>(image)
                .withDatabaseName(SCHEMA_NAME)
                .withUsername("differentUser")
                .withPassword("anotherPassword")
                .withExposedPorts(3306)
                .withStartupTimeout(Duration.ofMinutes(5));

        mysql.withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("Testcontainers.MySQL")));

        LOG.info("[DEBUG_LOG] Starting MySQL Testcontainer...");
        long startNanos = System.nanoTime();
        mysql.start();
        long durationMs = (System.nanoTime() - startNanos) / 1_000_000L;
        LOG.info("[DEBUG_LOG] MySQL Testcontainer started in {} ms. mappedPort={} jdbcUrl={} username={}",
                durationMs, mysql.getFirstMappedPort(), mysql.getJdbcUrl(), mysql.getUsername());
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

        final String url = String.format("jdbc:mysql://localhost:%d/%s?profileSQL=true&generateSimpleParameterMetadata=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                mysqlContainer.getFirstMappedPort(),
                SCHEMA_NAME);

        LOG.info("[DEBUG_LOG] Creating DataSource for Testcontainer. url={} user={}", url, mysqlContainer.getUsername());

        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.username(mysqlContainer.getUsername());
        dataSourceBuilder.password(mysqlContainer.getPassword());
        dataSourceBuilder.driverClassName(com.mysql.cj.jdbc.Driver.class.getName());
        dataSourceBuilder.url(url);
        return dataSourceBuilder.build();
    }

    @PostConstruct
    void startSchemaMigration() {
        LOG.info("[DEBUG_LOG] Starting Flyway migration for schema {}...", SCHEMA_NAME);
        long startNanos = System.nanoTime();
        final Flyway flyway = Flyway.configure()
            .sqlMigrationPrefix("V")
            .dataSource(dataSource())
            .locations("classpath:/db/mysql/xfcd/migration")
            .load();

        flyway.migrate();
        long durationMs = (System.nanoTime() - startNanos) / 1_000_000L;
        LOG.info("[DEBUG_LOG] Flyway migration completed in {} ms.", durationMs);
    }
}
