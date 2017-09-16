package org.amv.trafficsoft.datahub.xfcd;

import io.vertx.core.Vertx;
import org.amv.trafficsoft.datahub.VertxEventBusReactorAdapter;
import org.amv.trafficsoft.datahub.xfcd.event.TrafficsoftEvent;

// TODO: rename to TrafficsoftEvents
public class XfcdEvents extends VertxEventBusReactorAdapter<TrafficsoftEvent> {

    public XfcdEvents(Vertx vertx) {
        super(vertx);
    }
}
