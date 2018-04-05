package org.amv.trafficsoft.datahub.xfcd.demo.mqtt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;

@Data
@ConfigurationProperties("amv.trafficsoft.datahub.mqtt")
public class MqttProperties {
    private String serverId = "datahub";
    private User user;
    private MoquetteProperties server;

    public List<User> getUsers() {
        return user == null ? Collections.emptyList() : Collections.singletonList(user);
    }

    @Data
    public static class User {
        private String username = "example";
        private String password;
    }
}
