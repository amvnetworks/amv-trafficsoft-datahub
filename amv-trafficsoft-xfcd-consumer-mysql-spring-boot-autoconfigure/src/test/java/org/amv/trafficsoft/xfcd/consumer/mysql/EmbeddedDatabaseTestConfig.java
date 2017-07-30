package org.amv.trafficsoft.xfcd.consumer.mysql;

import com.google.common.annotations.VisibleForTesting;
import com.wix.mysql.EmbeddedMysql;
import com.wix.mysql.config.MysqldConfig;
import com.wix.mysql.config.SchemaConfig;
import com.wix.mysql.distribution.Version;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftXfcdJdbcProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.io.IOException;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static com.wix.mysql.EmbeddedMysql.anEmbeddedMysql;
import static com.wix.mysql.config.Charset.UTF8;
import static com.wix.mysql.config.MysqldConfig.aMysqldConfig;
import static com.wix.mysql.config.SchemaConfig.aSchemaConfig;

@TestConfiguration
public class EmbeddedDatabaseTestConfig {
    private static final Version embeddedMySqlServerVersion = Version.v5_5_40;
    @VisibleForTesting
    static final String SCHEMA_NAME = "amv_trafficsoft_xfcd_consumer_mysql_test";

    @Autowired
    TrafficsoftXfcdJdbcProperties properties;

    @Bean(destroyMethod = "stop")
    @Order(value = Ordered.HIGHEST_PRECEDENCE)
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
                    .withUser(properties.getUsername(), properties.getPassword())
                    .withCharset(UTF8)
                    .withTimeZone(TimeZone.getDefault())
                    .withTimeout(10, TimeUnit.SECONDS)
                    .withServerVariable("max_connect_errors", 1)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
