package org.amv.trafficsoft.xfcd.consumer.sqlite;

import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class TestDbConfig {
    //@Bean
    //@Primary
    public DataSource dataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName(org.sqlite.JDBC.class.getName());
        dataSourceBuilder.url("jdbc:sqlite:~amv-trafficsoft-xfcd-consumer-sqlite-test.db");
        return dataSourceBuilder.build();
    }
}
