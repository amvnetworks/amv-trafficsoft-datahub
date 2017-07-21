package org.amv.trafficsoft.datahub.xfcd;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.json.Json;
import io.vertx.core.streams.Pump;
import io.vertx.ext.reactivestreams.ReactiveReadStream;
import io.vertx.ext.reactivestreams.ReactiveWriteStream;
import org.amv.trafficsoft.datahub.xfcd.event.XfcdEvent;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;

import static java.util.Objects.requireNonNull;

public class XfcdEvents {
    private final Vertx vertx;

    public XfcdEvents(Vertx vertx) {
        this.vertx = requireNonNull(vertx);
    }

    public <T extends XfcdEvent> void publish(Class<T> clazz, Publisher<T> publisher) {
        requireNonNull(clazz);
        requireNonNull(publisher);

        ReactiveReadStream<Object> rrs = ReactiveReadStream.readStream();

        Flux.from(publisher)
                .map(Json::encode)
                .subscribe(rrs);

        MessageProducer<Object> messageProducer = vertx.eventBus().publisher(clazz.getName());

        Pump pump = Pump.pump(rrs, messageProducer);

        pump.start();
    }

    public <T extends XfcdEvent> void subscribe(Class<T> clazz, Subscriber<T> subscriber) {
        requireNonNull(clazz);
        requireNonNull(subscriber);

        final MessageConsumer<String> consumer = vertx.eventBus().consumer(clazz.getName());

        ReactiveWriteStream<String> rws = ReactiveWriteStream.writeStream(vertx);

        Flux.from(rws)
                .map(json -> Json.decodeValue(json, clazz))
                .subscribe(subscriber);

        Pump pump = Pump.pump(consumer.bodyStream(), rws);

        pump.start();
    }
}
