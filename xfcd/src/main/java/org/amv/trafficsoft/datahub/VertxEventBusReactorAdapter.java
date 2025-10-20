package org.amv.trafficsoft.datahub;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.Json;
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

        final String address = clazz.getName();
        Flux.from(publisher)
                .map(Json::encode)
                .subscribe(json -> vertx.eventBus().publish(address, json));
    }

    public <T extends E> void subscribe(Class<T> clazz, Subscriber<T> subscriber) {
        requireNonNull(clazz);
        requireNonNull(subscriber);

        final String address = clazz.getName();
        final MessageConsumer<String> consumer = vertx.eventBus().consumer(address);

        Flux.<T>create(sink -> {
            consumer.handler(msg -> {
                try {
                    T value = Json.decodeValue(msg.body(), clazz);
                    sink.next(value);
                } catch (Throwable t) {
                    sink.error(t);
                }
            });
            sink.onDispose(consumer::unregister);
        }).subscribe(subscriber);
    }
}
