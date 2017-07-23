package org.amv.trafficsoft.xfcd.consumer.jdbc;

import com.zaxxer.hikari.HikariConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * marker class after which jdbc config will be completed
 * this is useful in combination with {@link @AutoConfigureAfter} and
 * {@link @AutoConfigureBefore}.
 */
@Slf4j
@Configuration
@ConditionalOnProperty("amv.trafficsoft.xfcd.consumer.jdbc.enabled")
public class TrafficsoftDeliveryJdbcConsumerAutoConfigCompleted {

    @Autowired
    @Qualifier("trafficsoftDeliveryJdbcConsumerHikariConfig")
    public HikariConfig hikariConfig;

    @PostConstruct
    public void logDebugInformation() {
        if (!log.isDebugEnabled()) {
            return;
        }

        log.debug("Trafficsoft JDBC auto-configuration has been completed.\n" +
                        "jdbc-url: {}\n" +
                        "user: {}\n" +
                        "driver: {}\n" +
                        "max-pool-size: {}\n",
                hikariConfig.getJdbcUrl(),
                hikariConfig.getUsername(),
                hikariConfig.getDriverClassName(),
                hikariConfig.getMaximumPoolSize());
    }
}
