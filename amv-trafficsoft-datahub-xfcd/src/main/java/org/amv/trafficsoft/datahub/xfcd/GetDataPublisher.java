package org.amv.trafficsoft.datahub.xfcd;

import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.rest.client.xfcd.XfcdClient;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;
import rx.Observable;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Slf4j
public class GetDataPublisher implements XfcdDeliveryPublisher {

    private final XfcdClient xfcdClient;
    private final long contractId;

    public GetDataPublisher(XfcdClient xfcdClient, long contractId) {
        this.xfcdClient = requireNonNull(xfcdClient);
        this.contractId = contractId;
    }

    @Override
    public void subscribe(Subscriber<? super DeliveryRestDto> subscriber) {
        Publisher<List<DeliveryRestDto>> listPublisher = new Publisher<List<DeliveryRestDto>>() {
            @Override
            public void subscribe(Subscriber<? super List<DeliveryRestDto>> sub) {
                Observable<List<DeliveryRestDto>> listObservable = xfcdClient
                        .getDataAndConfirmDeliveries(contractId, Collections.emptyList())
                        .toObservable();

                sub.onSubscribe(new BaseSubscriber<List<DeliveryRestDto>>() {
                    protected void hookOnNext(List<DeliveryRestDto> value){
                       log.info("{}", value);
                    }
                });
                listObservable.subscribe(new rx.Subscriber<List<DeliveryRestDto>>() {
                    @Override
                    public void onCompleted() {
                        sub.onComplete();
                    }

                    @Override
                    public void onError(Throwable e) {
                        sub.onError(e);
                    }

                    @Override
                    public void onNext(List<DeliveryRestDto> deliveryRestDtos) {
                        sub.onNext(deliveryRestDtos);
                    }
                });
            }
        };

        Mono.from(listPublisher)
                .flatMapIterable(deliveries -> deliveries)
                .subscribe(subscriber);
    }
}
