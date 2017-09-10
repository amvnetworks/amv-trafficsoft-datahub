package org.amv.trafficsoft.xfcd.consumer.mysql;

import com.github.springtestdbunit.bean.DatabaseConfigBean;
import com.github.springtestdbunit.bean.DatabaseDataSourceConnectionFactoryBean;
import com.wix.mysql.EmbeddedMysql;
import com.wix.mysql.config.MysqldConfig;
import com.wix.mysql.config.SchemaConfig;
import com.wix.mysql.distribution.Version;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.ext.mysql.MySqlMetadataHandler;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static com.wix.mysql.EmbeddedMysql.anEmbeddedMysql;
import static com.wix.mysql.config.Charset.UTF8;
import static com.wix.mysql.config.MysqldConfig.aMysqldConfig;
import static com.wix.mysql.config.SchemaConfig.aSchemaConfig;

@TestConfiguration
@EnableTransactionManagement
public class DaoDbUnitTestConfig {
    private static final Version embeddedMySqlServerVersion = Version.v5_5_40;
    private static final String SCHEMA_NAME = "amv_trafficsoft_xfcd_consumer_mysql_test";

    @Bean(destroyMethod = "stop")
    public EmbeddedMysql embeddedMysql() {
        EmbeddedMysql mysqld = anEmbeddedMysql(mysqldConfig())
                .addSchema(schemaConfig())
                .start();

        return mysqld;
    }

    @Bean
    public SchemaConfig schemaConfig() {
        return aSchemaConfig(SCHEMA_NAME)
                .build();
    }

    @Bean
    public MysqldConfig mysqldConfig() {
        try {
            return aMysqldConfig(embeddedMySqlServerVersion)
                    .withFreePort()
                    .withUser("differentUser", "anotherPassword")
                    .withCharset(UTF8)
                    .withTimeZone(TimeZone.getDefault())
                    .withTimeout(10, TimeUnit.SECONDS)
                    .withServerVariable("max_connect_errors", 1)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
    @Bean(destroyMethod = "shutdown")
    public DataSource dataSource() {
        final EmbeddedMysql embeddedMysql = embeddedMysql(); // make sure embeddedMySql is started.

        final String url = String.format("jdbc:mysql://localhost:%d/%s?profileSQL=true&amp;generateSimpleParameterMetadata=true",
                embeddedMysql.getConfig().getPort(),
                SCHEMA_NAME);

        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.username(embeddedMysql.getConfig().getUsername());
        dataSourceBuilder.password(embeddedMysql.getConfig().getPassword());
        dataSourceBuilder.driverClassName(com.mysql.jdbc.Driver.class.getName());
        dataSourceBuilder.url(url);
        return dataSourceBuilder.build();
    }

    @PostConstruct
    void startSchemaMigration() {
        final Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource());
        flyway.setLocations("classpath:/db/mysql/xfcd/migration");

        flyway.migrate();
    }
}
