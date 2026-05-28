package com.mohammed.loadsimulator.dto;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = LocalhostOnlyUrlValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LocalhostOnlyUrl {

	String message() default "URL must use http://localhost or http://127.0.0.1";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
