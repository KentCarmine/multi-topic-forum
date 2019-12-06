package com.kentcarmine.multitopicforum.validators;

import com.kentcarmine.multitopicforum.annotations.PasswordMatches;
import com.kentcarmine.multitopicforum.dtos.PasswordDto;
import com.kentcarmine.multitopicforum.dtos.UserDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatches, Object> {

    @Override
    public void initialize(final PasswordMatches constraintAnnotation) {
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
//        UserDto userDto = (UserDto)obj;
//        if (userDto == null || userDto.getPassword() == null || userDto.getConfirmPassword() == null) {
//            return false;
//        }
//
//        return userDto.getPassword().equals(userDto.getConfirmPassword());
        PasswordDto passwordDto = (PasswordDto)obj;
        if (passwordDto == null || passwordDto.getPassword() == null || passwordDto.getConfirmPassword() == null) {
            return false;
        }

        return passwordDto.getPassword().equals(passwordDto.getConfirmPassword());
    }

}
