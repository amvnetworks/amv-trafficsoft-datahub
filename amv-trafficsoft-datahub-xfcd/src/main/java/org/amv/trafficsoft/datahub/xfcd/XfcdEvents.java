package org.amv.trafficsoft.datahub.xfcd;

import io.vertx.core.Vertx;
import org.amv.trafficsoft.datahub.VertxEventBusReactorAdapter;
import org.amv.trafficsoft.datahub.xfcd.event.XfcdEvent;

public class XfcdEvents extends VertxEventBusReactorAdapter<XfcdEvent> {

    public XfcdEvents(Vertx vertx) {
        super(vertx);
    }
}
