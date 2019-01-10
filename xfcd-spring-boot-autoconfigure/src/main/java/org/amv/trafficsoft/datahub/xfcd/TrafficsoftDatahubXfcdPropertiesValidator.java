package org.amv.trafficsoft.datahub.xfcd;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static com.google.common.base.Preconditions.checkArgument;

@Slf4j
public class TrafficsoftDatahubXfcdPropertiesValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return TrafficsoftDatahubXfcdProperties.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        checkArgument(supports(target.getClass()), "Unsupported type.");

        TrafficsoftDatahubXfcdProperties properties = (TrafficsoftDatahubXfcdProperties) target;

        if (!properties.isEnabled()) {
            log.debug("Skip validating {} bean as it property `enabled` is `false`",
                    TrafficsoftDatahubXfcdProperties.class.getSimpleName());
            return;
        }

        if (properties.getInitialFetchDelayInSeconds() < 0L) {
            errors.rejectValue("initialFetchDelayInSeconds", "invalid", "Value must be greater or equal to 0");
        }
        if (properties.getFetchIntervalInSeconds() < 30L) {
            errors.rejectValue("fetchIntervalInSeconds", "invalid", "Value must be greater or equal to 30");
        }
        if (properties.getMaxAmountOfNodesPerDelivery() < 0) {
            errors.rejectValue("maxAmountOfNodesPerDelivery", "invalid", "Value must be greater or equal to 0");
        }
    }
}
