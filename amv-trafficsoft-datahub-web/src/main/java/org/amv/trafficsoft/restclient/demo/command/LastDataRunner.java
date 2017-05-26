package org.amv.trafficsoft.restclient.demo.command;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.rest.client.xfcd.XfcdClient;
import org.amv.trafficsoft.rest.xfcd.model.NodeRestDto;
import org.springframework.boot.CommandLineRunner;
import rx.Scheduler;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import java.util.List;

import static java.util.Objects.requireNonNull;

@Slf4j
public class LastDataRunner implements CommandLineRunner {

    private final XfcdClient xfcdClient;
    private final long contractId;
    private final List<Long> vehicleIds;

    public LastDataRunner(XfcdClient xfcdClient, long contractId, List<Long> vehicleIds) {
        this.xfcdClient = requireNonNull(xfcdClient);
        this.contractId = contractId;
        this.vehicleIds = ImmutableList.copyOf(vehicleIds);
    }

    @Override
    public void run(String... args) throws Exception {
        Action1<List<NodeRestDto>> onNext = nodeRestDtos -> {
            log.info("Received Nodes Amount: {}", nodeRestDtos.size());
            nodeRestDtos.forEach(node -> log.info("Received Node: {}", node));
        };
        Action1<Throwable> onError = error -> {
            log.error("{}", error);
        };
        Action0 onComplete = () -> {
            log.info("Completed.");
        };

        Scheduler sameThreadExecutor = Schedulers.immediate();

        log.info("==================================================");
        this.xfcdClient.getLastData(this.contractId, this.vehicleIds)
                .toObservable()
                .observeOn(sameThreadExecutor)
                .subscribeOn(sameThreadExecutor)
                .subscribe(onNext, onError, onComplete);
        log.info("==================================================");
    }
}
