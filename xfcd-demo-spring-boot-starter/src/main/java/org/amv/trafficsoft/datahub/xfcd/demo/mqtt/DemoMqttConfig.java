package org.amv.trafficsoft.datahub.xfcd.demo.mqtt;

import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.XfcdEvents;
import org.amv.trafficsoft.datahub.xfcd.demo.DemoConfig;
import org.amv.trafficsoft.datahub.xfcd.demo.mqtt.config.MoquetteConfig;
import org.amv.trafficsoft.datahub.xfcd.demo.mqtt.config.MqttProperties;
import org.amv.trafficsoft.xfcd.mqtt.MqttApiVerticle;
import org.amv.trafficsoft.xfcd.mqtt.moquette.ext.ITopicPolicy;
import org.amv.trafficsoft.xfcd.mqtt.moquette.ext.RegexTopicPolicy;
import org.amv.trafficsoft.xfcd.mqtt.moquette.ext.TopicPolicies;
import org.amv.trafficsoft.xfcd.mqtt.moquette.ext.TopicPoliciesImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration
@Import(MoquetteConfig.class)
@AutoConfigureAfter(DemoConfig.class)
@ConditionalOnProperty(value = "demo.mqtt.enabled", havingValue = "true")
public class DemoMqttConfig {

    private final Environment environment;
    private final MqttProperties mqttProperties;
    private final MoquetteConfig moquetteConfig;

    @Autowired
    public DemoMqttConfig(Environment environment,
                          MqttProperties mqttProperties,
                          MoquetteConfig moquetteConfig) {
        this.environment = requireNonNull(environment);
        this.moquetteConfig = requireNonNull(moquetteConfig);
        this.mqttProperties = requireNonNull(mqttProperties);
    }

    @Bean
    public MqttApiVerticle mqttApiVerticle(XfcdEvents xfcdEvents) {
        return new MqttApiVerticle(mqttProperties.getServerId(),
                moquetteConfig.mqttServerOne(),
                xfcdEvents);
    }


    @Bean
    public TopicPolicies topicPolicies() {
        return TopicPoliciesImpl.builder()
                .addPolicy(readonlyTopicPolicy())
                .fallbackPolicy(fallbackTopicPolicy())
                .build();
    }

    private ITopicPolicy readonlyTopicPolicy() {
        return RegexTopicPolicy.builder()
                .regex("/?" + MqttApiVerticle.TOPIC_NAME + "/?.*")
                .readable(true)
                .writeable(false)
                .build();
    }

    private ITopicPolicy fallbackTopicPolicy() {
        return RegexTopicPolicy.builder()
                .regex(".+")
                .readable(false)
                .writeable(false)
                .build();
    }
}