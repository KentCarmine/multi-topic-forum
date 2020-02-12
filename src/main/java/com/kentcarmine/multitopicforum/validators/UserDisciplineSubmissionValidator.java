package com.kentcarmine.multitopicforum.validators;

import com.kentcarmine.multitopicforum.annotations.ValidUserDisciplineSubmission;
import com.kentcarmine.multitopicforum.dtos.UserDisciplineSubmissionDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator that processes @ValidUserDisciplineSubmission annotations
 **/
public class UserDisciplineSubmissionValidator implements ConstraintValidator<ValidUserDisciplineSubmission, UserDisciplineSubmissionDto> {

    @Override
    public boolean isValid(UserDisciplineSubmissionDto userDisciplineSubmissionDto, ConstraintValidatorContext constraintValidatorContext) {
        if (userDisciplineSubmissionDto.isBan() == userDisciplineSubmissionDto.isSuspension()) {
            return false;
        }

        if (userDisciplineSubmissionDto.isBan() && userDisciplineSubmissionDto.getSuspensionHours() > 0) {
            return false;
        }

        if (userDisciplineSubmissionDto.isSuspension() && userDisciplineSubmissionDto.getSuspensionHours() <= 0) {
            return false;
        }

        return true;
    }

    @Override
    public void initialize(ValidUserDisciplineSubmission constraintAnnotation) {
    }
}
