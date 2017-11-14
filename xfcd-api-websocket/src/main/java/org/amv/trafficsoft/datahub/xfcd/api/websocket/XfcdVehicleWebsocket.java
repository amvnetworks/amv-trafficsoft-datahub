package org.amv.trafficsoft.datahub.xfcd.api.websocket;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.primitives.Longs;
import io.vertx.core.json.Json;
import io.vertx.rxjava.core.http.ServerWebSocket;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.XfcdEvents;
import org.amv.trafficsoft.datahub.xfcd.event.IncomingDeliveryEvent;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import reactor.core.publisher.BaseSubscriber;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.*;

@Slf4j
public class XfcdVehicleWebsocket implements WebSocketHandler {

    private final XfcdEvents xfcdEvents;

    public XfcdVehicleWebsocket(XfcdEvents xfcdEvents) {
        this.xfcdEvents = requireNonNull(xfcdEvents);
    }

    @Override
    public boolean supports(ServerWebSocket ws) {
        String path = Strings.nullToEmpty(ws.path());
        return path.startsWith("/xfcd/vehicle/stream");
    }

    @Override
    public void handle(ServerWebSocket ws) {
        checkArgument(supports(ws));

        List<Long> vehicleIds = parseQuery(ws.query()).entrySet().stream()
                .filter(entry -> "vehicleId".equals(entry.getKey()))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .map(Longs::tryParse)
                .collect(toImmutableList());

        if (vehicleIds.isEmpty()) {
            ws.reject(401);
        }

        handleInternal(ws, vehicleIds);
    }

    private void handleInternal(ServerWebSocket ws, List<Long> vehicleIds) {
        ws.accept();

        BaseSubscriber<IncomingDeliveryEvent> subscriber = createSubscriber(ws, vehicleIds);

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

    private BaseSubscriber<IncomingDeliveryEvent> createSubscriber(ServerWebSocket ws, List<Long> vehicleIds) {
        return new BaseSubscriber<IncomingDeliveryEvent>() {
            @Override
            protected void hookOnNext(IncomingDeliveryEvent value) {
                value.getDeliveryPackage().getDeliveries()
                        .stream()
                        .map(DeliveryRestDto::getTrack)
                        .flatMap(Collection::stream)
                        .filter(track -> track.getVehicleId() != null)
                        .filter(track -> vehicleIds.contains(track.getVehicleId()))
                        .forEach(track -> ws.writeTextMessage(Json.encode(track)));
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
    }


    private Map<String, List<String>> parseQuery(String query) {
        return Pattern.compile("&").splitAsStream(Strings.nullToEmpty(query))
                .map(s -> Arrays.copyOf(s.split("="), 2))
                .collect(groupingBy(s -> decode(s[0]), mapping(s -> decode(s[1]), toList())));

    }

    private static String decode(final String encoded) {
        try {
            return encoded == null ? null : URLDecoder.decode(encoded, Charsets.UTF_8.name());
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException("Impossible: UTF-8 is a required encoding", e);
        }
    }
}