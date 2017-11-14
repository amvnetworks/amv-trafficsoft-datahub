package org.amv.trafficsoft.datahub.xfcd.api.websocket;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Longs;
import io.vertx.core.json.Json;
import io.vertx.rxjava.core.http.ServerWebSocket;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.rest.xfcd.model.ParameterRestDto;
import org.apache.commons.lang3.RandomUtils;

import java.util.Date;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

@Slf4j
public class XfcdLatestDataWebsocket implements WebSocketHandler {

    @Override
    public boolean supports(ServerWebSocket ws) {
        String path = Strings.nullToEmpty(ws.path());
        return path.startsWith("/xfcd/latest");
    }

    @Override
    public void handle(ServerWebSocket ws) {
        checkArgument(supports(ws));

        ws.accept();

        ws.handler(event -> {
            String command = event.getString(0, event.length());

            final Long vehicleId = Optional.ofNullable(command)
                    .map(Longs::tryParse)
                    .orElse(null);

            if (vehicleId == null) {
                ws.reject(401);
            }

            ws.writeTextMessage(Json.encode(ImmutableList.builder()
                    .add(ParameterRestDto.builder()
                            .value("vehicleId")
                            .param(String.valueOf(vehicleId))
                            .build())
                    .add(ParameterRestDto.builder()
                            .value("kmrd")
                            .param(String.valueOf(RandomUtils.nextLong(0L, 120_000L)))
                            .timestamp(new Date())
                            .build())
                    .add(ParameterRestDto.builder()
                            .param("move")
                            .value(String.valueOf(RandomUtils.nextBoolean() ? 1 : 0))
                            .build())
                    .build()));

        });

        ws.endHandler(foo -> {
            log.info("Websocket ended");
        });

        ws.closeHandler(foo -> {
            log.info("Websocket closed");
        });
    }
}