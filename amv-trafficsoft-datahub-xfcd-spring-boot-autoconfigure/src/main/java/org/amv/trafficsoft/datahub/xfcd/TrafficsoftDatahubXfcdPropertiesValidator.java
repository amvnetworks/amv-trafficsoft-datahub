package org.amv.trafficsoft.datahub.xfcd;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static com.google.common.base.Preconditions.checkArgument;

public class TrafficsoftDatahubXfcdPropertiesValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return TrafficsoftDatahubXfcdProperties.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        checkArgument(supports(target.getClass()), "Unsupported type.");

        TrafficsoftDatahubXfcdProperties properties = (TrafficsoftDatahubXfcdProperties) target;


    }
}
