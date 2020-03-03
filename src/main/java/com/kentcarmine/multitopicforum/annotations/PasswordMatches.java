package com.kentcarmine.multitopicforum.annotations;

import com.kentcarmine.multitopicforum.validators.PasswordMatchValidator;
import org.springframework.beans.factory.annotation.Value;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation that indicates that passwords of the annotated object should match
 */
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = PasswordMatchValidator.class)
@Documented
public @interface PasswordMatches {
    String message() default "Passwords must match";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
