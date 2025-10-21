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

        final MessageProducer<Object> messageProducer = vertx.eventBus().publisher(clazz.getName());

        Flux.from(publisher)
                .map(Json::encode)
                .doOnNext(messageProducer::write)
                .doOnComplete(messageProducer::close)
                .subscribe();
    }

    public <T extends E> void subscribe(Class<T> clazz, Subscriber<T> subscriber) {
        requireNonNull(clazz);
        requireNonNull(subscriber);

        final MessageConsumer<String> consumer = vertx.eventBus().consumer(clazz.getName());

        Flux.<String>create(sink -> {
            consumer.handler(msg -> sink.next(msg.body()));
            consumer.exceptionHandler(sink::error);
            consumer.endHandler(v -> sink.complete());
            sink.onCancel(consumer::unregister);
        })
        .map(json -> Json.decodeValue(json, clazz))
        .subscribe(subscriber);
    }
}
