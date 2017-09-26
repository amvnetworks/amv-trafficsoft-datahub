package org.amv.trafficsoft.xfcd.consumer.jdbc;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackage;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.amv.trafficsoft.rest.xfcd.model.NodeRestDto;
import org.amv.trafficsoft.rest.xfcd.model.ParameterRestDto;
import org.amv.trafficsoft.rest.xfcd.model.TrackRestDto;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Slf4j
public class DelegatingTrafficsoftDeliveryPackageDao implements TrafficsoftDeliveryPackageJdbcDao {

    private final TrafficsoftDeliveryJdbcDao deliveryDao;
    private final TrafficsoftXfcdNodeJdbcDao nodeDao;
    private final TrafficsoftXfcdStateJdbcDao stateDao;
    private final TrafficsoftXfcdXfcdJdbcDao xfcdDao;

    @Builder
    DelegatingTrafficsoftDeliveryPackageDao(TrafficsoftDeliveryJdbcDao deliveryDao,
                                            TrafficsoftXfcdNodeJdbcDao nodeDao,
                                            TrafficsoftXfcdStateJdbcDao stateDao,
                                            TrafficsoftXfcdXfcdJdbcDao xfcdDao) {
        this.deliveryDao = requireNonNull(deliveryDao);
        this.nodeDao = requireNonNull(nodeDao);
        this.stateDao = requireNonNull(stateDao);
        this.xfcdDao = requireNonNull(xfcdDao);
    }

    @Override
    @Transactional(transactionManager = "trafficsoftDeliveryJdbcConsumerTransactionManager")
    public void save(TrafficsoftDeliveryPackage deliveryPackage) throws DataAccessException {
        requireNonNull(deliveryPackage);

        if (deliveryPackage.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Skip saving empty delivery package");
            }
            return;
        }

        List<DeliveryRestDto> deliveries = deliveryPackage.getDeliveries();

        if (log.isDebugEnabled()) {
            log.debug("Saving {} deliveries: {}", deliveries.size(), deliveryPackage.getDeliveryIds());
        }

        deliveryPackage.getDeliveries()
                .forEach(delivery -> saveDelivery(deliveryPackage, delivery));

        if (log.isDebugEnabled()) {
            log.debug("Saved {} deliveries with {} nodes", deliveries.size(), deliveryPackage.getAmountOfNodes());
        }
    }

    private void saveDelivery(TrafficsoftDeliveryPackage deliveryPackage, DeliveryRestDto delivery) {
        requireNonNull(delivery);

        TrafficsoftDeliveryEntity deliveryEntity = TrafficsoftDeliveryEntity.builder()
                .id(delivery.getDeliveryId())
                .timestamp(delivery.getTimestamp().toInstant())
                .confirmedAt(null)
                .build();

        deliveryDao.save(deliveryEntity);

        delivery.getTrack()
                .forEach(track -> saveTrack(deliveryPackage, delivery, track));

    }

    private void saveTrack(TrafficsoftDeliveryPackage deliveryPackage, DeliveryRestDto delivery, TrackRestDto track) {
        requireNonNull(track);

        track.getNodes().forEach(node -> saveNode(deliveryPackage, delivery, track, node));
    }

    private void saveNode(TrafficsoftDeliveryPackage deliveryPackage,
                          DeliveryRestDto delivery,
                          TrackRestDto track,
                          NodeRestDto node) {
        requireNonNull(node);

        TrafficsoftXfcdNodeEntity nodeEntity = TrafficsoftXfcdNodeEntity.builder()
                .id(node.getId())
                .businessPartnerId(deliveryPackage.getContractId())
                .deliveryId(delivery.getDeliveryId())
                .vehicleId(track.getVehicleId())
                .tripId(track.getId())
                .timestamp(Optional.ofNullable(node.getTimestamp())
                        .map(Date::toInstant)
                        .orElse(null))
                .satelliteCount(Optional.ofNullable(node.getSatellites())
                        .orElse(0))
                .latitude(node.getLatitude())
                .longitude(node.getLongitude())
                .altitude(node.getAltitude())
                .heading(node.getHeading())
                .horizontalDilution(node.getHdop())
                .verticalDilution(node.getVdop())
                .speed(node.getSpeed())
                .build();

        nodeDao.save(nodeEntity);

        node.getStates()
                .forEach(state -> saveState(node, state));
        node.getXfcds()
                .forEach(xfcd -> saveXfcd(node, xfcd));
    }

    private void saveState(NodeRestDto node,
                           ParameterRestDto state) {
        requireNonNull(state);

        final TrafficsoftXfcdStateEntity stateEntity = TrafficsoftXfcdStateEntity.builder()
                .nodeId(node.getId())
                .code(state.getParam())
                .value(state.getValue())
                .build();

        stateDao.save(stateEntity);
    }

    private void saveXfcd(NodeRestDto node,
                          ParameterRestDto xfcd) {
        requireNonNull(xfcd);

        final TrafficsoftXfcdXfcdEntity xfcdEntity = TrafficsoftXfcdXfcdEntity.builder()
                .nodeId(node.getId())
                .type(xfcd.getParam())
                .valueAsString(xfcd.getValue())
                .build();

        xfcdDao.save(xfcdEntity);
    }
}
