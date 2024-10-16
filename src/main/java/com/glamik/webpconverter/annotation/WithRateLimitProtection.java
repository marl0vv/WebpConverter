package com.glamik.webpconverter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate that a method should be protected with rate limiting based on client IP address.
 * <p>
 * Methods annotated with {@code @WithRateLimitProtection} will be intercepted by an aspect that applies
 * rate limiting logic, restricting the number of times the method can be called from the same IP address
 * within a specified time window.
 * </p>
 *
 * @see com.glamik.webpconverter.aspect.RateLimitAspect
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface WithRateLimitProtection {
}
