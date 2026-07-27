package com.bidradar.company.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

public class AllowedValuesValidator implements ConstraintValidator<AllowedValues, String> {

    private Set<String> allowed;

    @Override
    public void initialize(AllowedValues annotation) {
        this.allowed = Set.of(annotation.values());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return allowed.contains(value);
    }
}
