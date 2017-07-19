package org.amv.trafficsoft.xfcd.consumer.jdbc;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.amv.trafficsoft.datahub.xfcd.TrafficsoftDeliveryPackage;
import org.amv.trafficsoft.rest.xfcd.model.DeliveryRestDto;
import org.amv.trafficsoft.rest.xfcd.model.NodeRestDto;
import org.amv.trafficsoft.rest.xfcd.model.TrackRestDto;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.requireNonNull;

@Slf4j
public class DelegatingTrafficsoftDeliveryPackageDao implements TrafficsoftDeliveryPackageJdbcDao {

    private final TrafficsoftDeliveryJdbcDao deliveryDao;
    private final TrafficsoftXfcdNodeJdbcDao nodeDao;

    @Builder
    DelegatingTrafficsoftDeliveryPackageDao(TrafficsoftDeliveryJdbcDao deliveryDao, TrafficsoftXfcdNodeJdbcDao nodeDao) {
        this.deliveryDao = requireNonNull(deliveryDao);
        this.nodeDao = requireNonNull(nodeDao);
    }

    @Override
    @Transactional
    public void save(TrafficsoftDeliveryPackage deliveryPackage) throws DataAccessException {
        if (deliveryPackage.isEmpty()) {
            return;
        }

        deliveryPackage.getDeliveries().stream()
                .forEach(delivery -> saveDelivery(deliveryPackage, delivery));
    }

    private void saveDelivery(TrafficsoftDeliveryPackage deliveryPackage, DeliveryRestDto delivery) {
        final TrafficsoftDeliveryEntity deliveryEntity = TrafficsoftDeliveryEntity.builder()
                .id(delivery.getDeliveryId())
                .timestamp(delivery.getTimestamp().toInstant())
                .confirmedAt(null)
                .build();

        deliveryDao.save(deliveryEntity);

        delivery.getTrack().stream()
                .forEach(track -> saveTrack(deliveryPackage, delivery, track));

    }

    private void saveTrack(TrafficsoftDeliveryPackage deliveryPackage, DeliveryRestDto delivery, TrackRestDto track) {
        track.getNodes().stream()
                .forEach(node -> saveNode(deliveryPackage, delivery, track, node));
    }

    private void saveNode(TrafficsoftDeliveryPackage deliveryPackage,
                          DeliveryRestDto delivery,
                          TrackRestDto track,
                          NodeRestDto node) {
        final TrafficsoftXfcdNodeEntity build = TrafficsoftXfcdNodeEntity.builder()
                .id(node.getId())
                .bpcId((int) deliveryPackage.getContractId())
                .deliveryId(delivery.getDeliveryId())
                .tripId(track.getId())
                .timestamp(node.getTimestamp().toInstant())
                .latitude(node.getLatitude())
                .longitude(node.getLongitude())
                .altitude(node.getAltitude())
                .heading(node.getHeading())
                .horizontalDilution(node.getHdop())
                .verticalDilution(node.getVdop())
                .speed(node.getSpeed())
                .vehicleId(track.getVehicleId())
                .build();

        nodeDao.save(build);
    }
}
