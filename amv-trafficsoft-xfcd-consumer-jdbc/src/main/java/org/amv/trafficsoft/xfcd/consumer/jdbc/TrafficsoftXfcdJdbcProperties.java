package org.amv.trafficsoft.xfcd.consumer.jdbc;

import com.google.common.base.Strings;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

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
    private boolean schemaMigrationEnabled = false;

    public Optional<String> getDriverClassName() {
        return Optional.ofNullable(driverClassName)
                .map(Strings::emptyToNull);
    }
}
