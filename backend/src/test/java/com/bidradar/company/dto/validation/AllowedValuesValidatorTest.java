package com.bidradar.company.dto.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AllowedValuesValidatorTest {

    private record Holder(@AllowedValues(values = {"A", "B"}) String value) {}

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("허용된 값이면 검증을 통과한다")
    void 허용된값이면_통과() {
        Set<ConstraintViolation<Holder>> violations = validator.validate(new Holder("A"));
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("허용되지 않은 값이면 검증에 실패한다")
    void 허용되지않은값이면_실패() {
        Set<ConstraintViolation<Holder>> violations = validator.validate(new Holder("C"));
        assertThat(violations).hasSize(1);
    }

    @Test
    @DisplayName("null 값은 통과한다 (필수 여부는 NotBlank가 담당)")
    void null값은_통과() {
        Set<ConstraintViolation<Holder>> violations = validator.validate(new Holder(null));
        assertThat(violations).isEmpty();
    }
}
