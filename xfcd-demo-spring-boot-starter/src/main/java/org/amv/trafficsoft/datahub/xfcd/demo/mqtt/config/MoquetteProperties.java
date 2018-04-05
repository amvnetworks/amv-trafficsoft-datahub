package org.amv.trafficsoft.datahub.xfcd.demo.mqtt.config;

import lombok.Data;

import java.util.Optional;

@Data
public class MoquetteProperties {
    private int port = 18881;
    private int websocketPort = 18883;
    private String host = "0.0.0.0";
    private boolean allowAnonymous;
    private MoquetteSslProperties ssl;

    public Optional<MoquetteSslProperties> getSsl() {
        return Optional.ofNullable(ssl);
    }

    @Data
    public static class MoquetteSslProperties {
        private int port = 18882;
        private String jksPath = "keystore/example/example_keystore.jks";
        private String keyStorePassword = "example";
        private String keyManagerPassword = "example";
    }
}
