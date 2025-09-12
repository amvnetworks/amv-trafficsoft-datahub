package org.amv.trafficsoft.xfcd.consumer.mysql;

import com.google.common.annotations.VisibleForTesting;
import org.amv.trafficsoft.xfcd.consumer.jdbc.TrafficsoftXfcdJdbcProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class EmbeddedDatabaseTestConfig {
    @VisibleForTesting
    static final String SCHEMA_NAME = "amv_trafficsoft_xfcd_consumer_mysql_test";

    @Autowired
    TrafficsoftXfcdJdbcProperties properties;

    @Bean(destroyMethod = "stop")
    @Order(value = Ordered.HIGHEST_PRECEDENCE)
    public MySQLContainer<?> mysqlContainer() {
        MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.30"))
                .withDatabaseName(SCHEMA_NAME)
                .withUsername(properties.getUsername())
                .withPassword(properties.getPassword())
                .withExposedPorts(3306);

        mysql.start();
        return mysql;
    }
}
