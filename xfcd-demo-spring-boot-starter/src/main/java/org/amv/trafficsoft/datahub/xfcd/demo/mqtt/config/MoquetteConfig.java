package org.amv.trafficsoft.datahub.xfcd.demo.mqtt.config;

import com.google.common.collect.ImmutableList;
import io.moquette.interception.InterceptHandler;
import io.moquette.server.Server;
import io.moquette.server.config.IConfig;
import io.moquette.server.config.MemoryConfig;
import io.moquette.spi.security.IAuthenticator;
import io.moquette.spi.security.IAuthorizator;
import io.moquette.spi.security.ISslContextCreator;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.demo.mqtt.config.MoquetteProperties.MoquetteSslProperties;
import org.amv.trafficsoft.xfcd.mqtt.moquette.ext.SimpleAuthenticator;
import org.amv.trafficsoft.xfcd.mqtt.moquette.ext.SimpleAuthenticator.InternalUser;
import org.amv.trafficsoft.xfcd.mqtt.moquette.ext.SimpleAuthorizator;
import org.amv.trafficsoft.xfcd.mqtt.moquette.ext.ITopicPolicy;
import org.amv.trafficsoft.xfcd.mqtt.moquette.ext.TopicPolicies;
import org.amv.trafficsoft.xfcd.mqtt.moquette.handler.LoggingHandler;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Configuration
@EnableConfigurationProperties({
        MqttProperties.class
})
public class MoquetteConfig {

    private final MqttProperties mqttProperties;
    private final MoquetteProperties moquetteProperties;
    private final Environment environment;

    @Autowired
    public MoquetteConfig(Environment environment, MqttProperties mqttProperties) {
        this.environment = requireNonNull(environment);
        this.mqttProperties = requireNonNull(mqttProperties);
        this.moquetteProperties = mqttProperties.getServer();
    }

    @Bean
    @ConditionalOnMissingBean(IAuthorizator.class)
    public IAuthorizator authorizator(TopicPolicies topicPolicies) {
        return new SimpleAuthorizator(topicPolicies);
    }

    @Bean
    @ConditionalOnMissingBean(IAuthenticator.class)
    public IAuthenticator authenticator() {
        List<InternalUser> users = mqttProperties.getUsers().stream()
                .map(u -> InternalUser.builder()
                        .username(u.getUsername())
                        .password(u.getPassword())
                        .build())
                .collect(Collectors.toList());

        return new SimpleAuthenticator(users);
    }

    @Bean("mqttServerOne")
    public Server mqttServerOne() {
        return new Server();
    }

    @Bean("moquetteServerOneInitializer")
    public MoquetteServerInitializingBean moquetteServerOneInitializer(
            IAuthenticator authenticator,
            IAuthorizator authorizator,
            @Qualifier("mqttServerOneConfig") IConfig configServerOne,
            @Qualifier("mqttServerOne") Server mqttServer) {
        List<InterceptHandler> handlers = ImmutableList.<InterceptHandler>builder()
                .add(new LoggingHandler())
                .build();
        return new MoquetteServerInitializingBean(mqttServer, configServerOne, handlers, authenticator, authorizator);
    }

    @Bean("mqttServerOneConfig")
    public IConfig configServerOne() {
        Properties properties = new Properties();
        properties.setProperty("port", String.valueOf(moquetteProperties.getPort()));
        properties.setProperty("websocket_port", String.valueOf(moquetteProperties.getWebsocketPort()));
        properties.setProperty("allow_anonymous", String.valueOf(moquetteProperties.isAllowAnonymous()));
        properties.setProperty("host", moquetteProperties.getHost());

        final Optional<MoquetteSslProperties> sslOptional = moquetteProperties.getSsl();
        if (sslOptional.isPresent()) {
            MoquetteSslProperties ssl = sslOptional.get();
            properties.setProperty("ssl_port", String.valueOf(ssl.getPort()));
            properties.setProperty("jks_path", ssl.getJksPath());
            properties.setProperty("key_store_password", ssl.getKeyStorePassword());
            properties.setProperty("key_manager_password", ssl.getKeyManagerPassword());
        }

        return new MemoryConfig(properties);
    }

    @Slf4j
    public static class MoquetteServerInitializingBean implements InitializingBean, DisposableBean {
        private final Server moquetteServer;
        private final IConfig config;
        private final List<? extends InterceptHandler> handlers;
        private final IAuthenticator authenticator;
        private final IAuthorizator authorizator;

        public MoquetteServerInitializingBean(Server moquetteServer,
                                              IConfig config,
                                              List<? extends InterceptHandler> handlers,
                                              IAuthenticator authenticator,
                                              IAuthorizator authorizator) {
            this.moquetteServer = requireNonNull(moquetteServer);
            this.config = requireNonNull(config);
            this.handlers = requireNonNull(handlers);
            this.authenticator = requireNonNull(authenticator);
            this.authorizator = requireNonNull(authorizator);
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            log.info("Starting MQTT Server");

            ISslContextCreator nullMeansDefaultSslCreator = null;
            moquetteServer.startServer(config, handlers, nullMeansDefaultSslCreator, authenticator, authorizator);

            log.info("MQTT Server started");
        }

        @Override
        public void destroy() {
            log.info("Stopping MQTT Server");

            moquetteServer.stopServer();

            log.info("MQTT Server stopped");
        }
    }

}
