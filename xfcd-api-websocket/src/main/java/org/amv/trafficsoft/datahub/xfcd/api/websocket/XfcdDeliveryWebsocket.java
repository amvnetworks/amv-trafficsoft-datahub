package org.amv.trafficsoft.datahub.xfcd.api.websocket;

import com.google.common.base.Strings;
import io.vertx.core.json.Json;
import io.vertx.rxjava.core.http.ServerWebSocket;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.XfcdEvents;
import org.amv.trafficsoft.datahub.xfcd.event.IncomingDeliveryEvent;
import reactor.core.publisher.BaseSubscriber;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Slf4j
public class XfcdDeliveryWebsocket implements WebSocketHandler {

    private final XfcdEvents xfcdEvents;

    public XfcdDeliveryWebsocket(XfcdEvents xfcdEvents) {
        this.xfcdEvents = requireNonNull(xfcdEvents);
    }

    @Override
    public boolean supports(ServerWebSocket ws) {
        String path = Strings.nullToEmpty(ws.path());
        return path.startsWith("/xfcd/delivery/stream");
    }

    @Override
    public void handle(ServerWebSocket ws) {
        checkArgument(supports(ws));

        ws.accept();

        BaseSubscriber<IncomingDeliveryEvent> subscriber = new BaseSubscriber<IncomingDeliveryEvent>() {
            @Override
            protected void hookOnNext(IncomingDeliveryEvent value) {
                ws.writeTextMessage(Json.encode(value));
            }

            @Override
            protected void hookOnComplete() {
                this.dispose();
                ws.end();
            }

            @Override
            protected void hookOnError(Throwable throwable) {
                log.error("", throwable);
                this.dispose();
                ws.end();
            }
        };

        xfcdEvents.subscribe(IncomingDeliveryEvent.class, subscriber);

        ws.endHandler(foo -> {
            log.info("Websocket ended");
            subscriber.dispose();
        });

        ws.closeHandler(foo -> {
            log.info("Websocket closed");
            subscriber.dispose();
        });
    }
}