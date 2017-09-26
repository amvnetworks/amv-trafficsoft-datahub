package org.amv.trafficsoft.xfcd.consumer.jdbc;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Value
@Builder(toBuilder = true)
public class TrafficsoftXfcdNodeEntity {
    private long id;
    private long businessPartnerId;
    private long deliveryId;
    private long vehicleId;
    private long tripId;
    @NonNull
    private Instant timestamp;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private BigDecimal speed;
    private BigDecimal heading;
    private BigDecimal altitude;
    private Integer satelliteCount;
    private BigDecimal horizontalDilution;
    private BigDecimal verticalDilution;

    public Optional<BigDecimal> getLongitude() {
        return Optional.ofNullable(longitude);
    }

    public Optional<BigDecimal> getLatitude() {
        return Optional.ofNullable(latitude);
    }

    public Optional<BigDecimal> getSpeed() {
        return Optional.ofNullable(speed);
    }

    public Optional<BigDecimal> getHeading() {
        return Optional.ofNullable(heading);
    }

    public Optional<BigDecimal> getAltitude() {
        return Optional.ofNullable(altitude);
    }

    public Optional<BigDecimal> getHorizontalDilution() {
        return Optional.ofNullable(horizontalDilution);
    }

    public Optional<BigDecimal> getVerticalDilution() {
        return Optional.ofNullable(verticalDilution);
    }

    public Optional<Integer> getSatelliteCount() {
        return Optional.ofNullable(satelliteCount);
    }
}
