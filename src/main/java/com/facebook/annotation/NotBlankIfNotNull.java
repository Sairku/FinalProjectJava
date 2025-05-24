package com.facebook.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NotBlankIfNotNull.NotBlankIfNotNullValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotBlankIfNotNull {
    String message() default "Field must not be blank if provided";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class NotBlankIfNotNullValidator implements ConstraintValidator<NotBlankIfNotNull, String> {
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            return value == null || !value.trim().isEmpty();
        }
    }
}
