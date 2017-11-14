package org.amv.trafficsoft.datahub.example.demo;

import io.vertx.rxjava.core.AbstractVerticle;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebSocketExampleHtmlVerticle extends AbstractVerticle {

    private final int port;

    public WebSocketExampleHtmlVerticle(int port) {
        this.port = port;
    }

    @Override
    public void start() {
        vertx.createHttpServer()
                .requestHandler(req -> {
                    if (req.uri().equals("/websocket.html")) {
                        req.response().sendFile("html/websocket/index.html");
                    }
                    if (req.uri().equals("/xfcd-latest-data.html")) {
                        req.response().sendFile("html/websocket/xfcd-latest-data.html");
                    }
                    if (req.uri().equals("/xfcd-delivery-stream.html")) {
                        req.response().sendFile("html/websocket/xfcd-delivery-stream.html");
                    }
                    if (req.uri().equals("/xfcd-latest-data.html")) {
                        req.response().sendFile("html/websocket/xfcd-delivery-stream.html");
                    }
                })
                .listen(port);
    }
}