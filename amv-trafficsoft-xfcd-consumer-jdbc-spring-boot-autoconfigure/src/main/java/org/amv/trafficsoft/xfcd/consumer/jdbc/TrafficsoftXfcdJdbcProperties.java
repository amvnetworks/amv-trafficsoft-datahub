package org.amv.trafficsoft.xfcd.consumer.jdbc;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.Optional;

@Data
@ConfigurationProperties("amv.trafficsoft.xfcd.consumer.jdbc")
public class TrafficsoftXfcdJdbcProperties {

    private boolean enabled;
    private String jdbcUrl;
    private String username;
    private String password;
    private String driverClassName;
    private String flywayScriptsLocation;
    private boolean sendConfirmationEvents;
    private boolean schemaMigrationEnabled = false;

    private PoolProperties pool = new PoolProperties();
    private Map<String, String> dataSource = Maps.newHashMap();

    public Optional<String> getDriverClassName() {
        return Optional.ofNullable(driverClassName)
                .map(Strings::emptyToNull);
    }

    @Data
    public static class PoolProperties {
        private long initializationFailTimeout = 1;
        private int maxPoolSize = 25;
    }
}
