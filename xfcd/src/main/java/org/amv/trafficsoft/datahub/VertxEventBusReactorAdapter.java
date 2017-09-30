package org.amv.trafficsoft.datahub;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.json.Json;
import io.vertx.core.streams.Pump;
import io.vertx.ext.reactivestreams.ReactiveReadStream;
import io.vertx.ext.reactivestreams.ReactiveWriteStream;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;

import static java.util.Objects.requireNonNull;

public class VertxEventBusReactorAdapter<E> {
    private final Vertx vertx;

    public VertxEventBusReactorAdapter(Vertx vertx) {
        this.vertx = requireNonNull(vertx);
    }

    public <T extends E> void publish(Class<T> clazz, Publisher<T> publisher) {
        requireNonNull(clazz);
        requireNonNull(publisher);

        ReactiveReadStream<Object> rrs = ReactiveReadStream.readStream();

        Flux.from(publisher)
                .map(Json::encode)
                .subscribe(rrs);

        MessageProducer<Object> messageProducer = vertx.eventBus().publisher(clazz.getName());

        Pump pump = Pump.pump(rrs, messageProducer);

        pump.start();

        rrs.endHandler(event -> {
            pump.stop();
        });
    }

    public <T extends E> void subscribe(Class<T> clazz, Subscriber<T> subscriber) {
        requireNonNull(clazz);
        requireNonNull(subscriber);

        final MessageConsumer<String> consumer = vertx.eventBus().consumer(clazz.getName());

        ReactiveWriteStream<String> rws = ReactiveWriteStream.writeStream(vertx);

        Pump pump = Pump.pump(consumer.bodyStream(), rws);

        Flux.from(rws)
                .doOnSubscribe(subscription -> {
                    pump.start();
                })
                .doOnComplete(() -> {
                    pump.stop();
                    rws.close();
                })
                .map(json -> Json.decodeValue(json, clazz))
                .subscribe(subscriber);
    }
}
