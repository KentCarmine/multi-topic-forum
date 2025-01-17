package com.kentcarmine.multitopicforum.validators;

import com.kentcarmine.multitopicforum.annotations.PasswordMatches;
import com.kentcarmine.multitopicforum.dtos.PasswordDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator that processes @PasswordMatches annotations, ensuring that the annotated object has matching values in
 * its password and confirmPassword fields.
 */
public class PasswordMatchValidator implements ConstraintValidator<PasswordMatches, Object> {

    @Override
    public void initialize(final PasswordMatches constraintAnnotation) {
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        PasswordDto passwordDto = (PasswordDto)obj;
        if (passwordDto == null || passwordDto.getPassword() == null || passwordDto.getConfirmPassword() == null) {
            return false;
        }

        return passwordDto.getPassword().equals(passwordDto.getConfirmPassword());
    }

}
