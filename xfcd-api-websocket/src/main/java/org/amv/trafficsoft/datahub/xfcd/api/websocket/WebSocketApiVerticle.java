package org.amv.trafficsoft.datahub.xfcd.api.websocket;

import com.google.common.collect.ImmutableList;
import io.vertx.core.Handler;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.http.ServerWebSocket;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Slf4j
public class WebSocketApiVerticle extends AbstractVerticle {

    private final List<WebSocketHandler> webSocketHandlers;
    private final int port;

    public WebSocketApiVerticle(List<WebSocketHandler> webSocketHandlers, int port) {
        this.webSocketHandlers = ImmutableList.copyOf(requireNonNull(webSocketHandlers));
        this.port = port;
    }

    @Override
    public void start() {
        vertx.createHttpServer()
                .requestHandler(req -> {
                    if (req.uri().equals("/xfcd-latest-data.html")) {
                        req.response().sendFile("xfcd-latest-data.html");
                    }
                    if (req.uri().equals("/xfcd-delivery-stream.html")) {
                        req.response().sendFile("xfcd-delivery-stream.html");
                    }
                    if (req.uri().equals("/xfcd-latest-data.html")) {
                        req.response().sendFile("xfcd-delivery-stream.html");
                    }
                })
                .websocketHandler(webSocketHandler())
                .listen(port);
    }

    private Handler<ServerWebSocket> webSocketHandler() {
        return ws -> {
            final List<WebSocketHandler> handlers = webSocketHandlers.stream()
                    .filter(handler -> handler.supports(ws))
                    .collect(Collectors.toList());

            if (handlers.isEmpty()) {
                ws.reject(404);
            }

            handlers.forEach(handler -> handler.handle(ws));
        };
    }
}