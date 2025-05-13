package com.facebook.validation;

import com.facebook.enums.Gender;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidGender.GenderValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidGender {
    String message() default "Gender must be MALE, FEMALE, or CUSTOM";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class GenderValidator implements ConstraintValidator<ValidGender, String> {
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null) return false;

            try {
                Gender.valueOf(value.toUpperCase());

                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    }
}
