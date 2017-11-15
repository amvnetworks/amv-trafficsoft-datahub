package org.amv.trafficsoft.datahub.example;

import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.example.demo.DemoDeliveryProducerVerticle;
import org.amv.trafficsoft.datahub.xfcd.XfcdEvents;
import org.amv.trafficsoft.datahub.xfcd.api.websocket.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
@Configuration
@ConditionalOnProperty(value = "demo.websocket.enabled", havingValue = "true")
public class TrafficsoftDataHubWebSocketDemoConfig {

    @Value("${server.port}")
    private int serverPort;

    private int websocketServerPort() {
        return serverPort + 1;
    }

    @Bean
    public WebSocketApiVerticle webSocketApiVerticle(List<WebSocketHandler> webSocketHandlers) {
        return new WebSocketApiVerticle(webSocketHandlers, websocketServerPort());
    }

    @Bean
    public XfcdDeliveryWebsocket xfcdDeliveryWebsocketVerticle(XfcdEvents xfcdEvents) {
        return new XfcdDeliveryWebsocket(xfcdEvents);
    }

    @Bean
    public XfcdVehicleWebsocket xfcdVehicleWebsocket(XfcdEvents xfcdEvents) {
        return new XfcdVehicleWebsocket(xfcdEvents);
    }

    @Bean
    public XfcdLatestDataWebsocket xfcdLatestDataWebsocket() {
        return new XfcdLatestDataWebsocket();
    }

    @Bean
    public DemoDeliveryProducerVerticle demoDeliveryProducerVerticle(XfcdEvents xfcdEvents) {
        return new DemoDeliveryProducerVerticle(xfcdEvents);
    }
}
