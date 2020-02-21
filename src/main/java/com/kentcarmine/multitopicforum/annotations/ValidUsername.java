package com.kentcarmine.multitopicforum.annotations;


import com.kentcarmine.multitopicforum.validators.UsernameValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Annotation indicating the annotated username field should not allow 'anonymous' or 'anonymousUser' in either upper
 * or lower case.
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UsernameValidator.class)
@Documented
public @interface ValidUsername {
    String message() default "Invalid username, please choose another";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
