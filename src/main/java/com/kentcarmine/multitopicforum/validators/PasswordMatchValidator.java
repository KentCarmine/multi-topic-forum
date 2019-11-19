package com.kentcarmine.multitopicforum.validators;

import com.kentcarmine.multitopicforum.annotations.PasswordMatches;
import com.kentcarmine.multitopicforum.dtos.UserDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatches, Object> {

    @Override
    public void initialize(final PasswordMatches constraintAnnotation) {
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        UserDto userDto = (UserDto)obj;
        return userDto.getPassword().equals(userDto.getConfirmPassword());
    }

}
