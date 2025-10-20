package org.amv.trafficsoft.datahub.xfcd.api.websocket;

import io.vertx.core.Handler;
import io.vertx.rxjava3.core.http.ServerWebSocket;

public interface WebSocketHandler extends Handler<ServerWebSocket> {
    boolean supports(ServerWebSocket event);
}
