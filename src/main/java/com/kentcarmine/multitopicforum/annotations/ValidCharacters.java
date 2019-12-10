package com.kentcarmine.multitopicforum.annotations;


import com.kentcarmine.multitopicforum.validators.RestrictedCharacterSetValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Annotation indicating the annotated field should be composed of only certain characters.
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RestrictedCharacterSetValidator.class)
@Documented
public @interface ValidCharacters {
    String message() default "Contains prohibited characters";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
