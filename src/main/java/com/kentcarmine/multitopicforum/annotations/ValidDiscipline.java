package com.kentcarmine.multitopicforum.annotations;

import com.kentcarmine.multitopicforum.validators.DisciplineValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation that indicates that Discipline object should have a positive duration of disciplineType is Suspended, but
 * have no duration if disciplineType is Ban
 */
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = DisciplineValidator.class)
@Documented
public @interface ValidDiscipline {
    String message() default "Invalid combination of discipline values.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
