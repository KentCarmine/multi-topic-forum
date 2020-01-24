package com.kentcarmine.multitopicforum.annotations;

import com.kentcarmine.multitopicforum.validators.VoteValueValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Annotation indicating the annotated field should be a valid vote value (meaning 1 or -1)
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = VoteValueValidator.class)
@Documented
public @interface ValidVoteValue {
    String message() default "Invalid value";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
