package com.facebook.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.*;
import java.util.Calendar;
import java.util.Date;

@Documented
@Constraint(validatedBy = PastAtLeastYears.BirthdateValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PastAtLeastYears {
    String message() default "Birthdate must be at least {years} years in the past";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    int years(); // The number of years to check against

    class BirthdateValidator implements ConstraintValidator<PastAtLeastYears, Date> {
        private int years;

        @Override
        public void initialize(PastAtLeastYears constraintAnnotation) {
            this.years = constraintAnnotation.years();
        }

        @Override
        public boolean isValid(Date value, ConstraintValidatorContext context) {
            if (value == null) return true;

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, -years);
            Date minDate = calendar.getTime();

            return value.before(minDate);
        }
    }
}
