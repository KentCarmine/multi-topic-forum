package com.kentcarmine.multitopicforum.annotations;

import com.kentcarmine.multitopicforum.dtos.UserDisciplineSubmissionDto;
import com.kentcarmine.multitopicforum.validators.PasswordMatchValidator;
import com.kentcarmine.multitopicforum.validators.UserDisciplineSubmissionValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation that indicates that when submitting a user discipline request, exactly one of ban or suspension should be
 * selected, and there should only be a value for suspension hours if suspension is selected.
 */
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = UserDisciplineSubmissionValidator.class)
@Documented
public @interface ValidUserDisciplineSubmission {
    String message() default "Invalid discipline request";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
