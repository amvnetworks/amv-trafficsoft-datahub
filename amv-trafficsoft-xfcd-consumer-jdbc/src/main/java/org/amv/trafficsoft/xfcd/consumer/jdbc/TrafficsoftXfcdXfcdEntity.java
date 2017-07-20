package org.amv.trafficsoft.xfcd.consumer.jdbc;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.Optional;

@Value
@Builder(toBuilder = true)
public class TrafficsoftXfcdXfcdEntity {
    private String type;
    private long nodeId;
    private BigDecimal value;
    private String valueAsString;

    public Optional<BigDecimal> getValue() {
        return Optional.ofNullable(value);
    }

    public Optional<String> getValueAsString() {
        return Optional.ofNullable(valueAsString);
    }
}
