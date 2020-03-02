package com.kentcarmine.multitopicforum.annotations;

import com.kentcarmine.multitopicforum.validators.SearchStringValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Annotation indicating the annotated field should be a valid string to be entered for a search.
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SearchStringValidator.class)
@Documented
public @interface ValidSearchString {
    String message() default "Invalid search text";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
