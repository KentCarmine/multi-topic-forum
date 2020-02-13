package com.kentcarmine.multitopicforum.validators;

import com.kentcarmine.multitopicforum.annotations.ValidUserDisciplineSubmission;
import com.kentcarmine.multitopicforum.dtos.UserDisciplineSubmissionDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator that processes @ValidUserDisciplineSubmission annotations
 **/
public class UserDisciplineSubmissionValidator implements ConstraintValidator<ValidUserDisciplineSubmission, UserDisciplineSubmissionDto> {
    private static final int HOURS_IN_30_DAYS = 30 * 24;
    private static final int MIN_SUSPENSION_HOURS = 1;
    private static final int MAX_SUSPENSION_HOURS = HOURS_IN_30_DAYS;

    @Override
    public boolean isValid(UserDisciplineSubmissionDto userDisciplineSubmissionDto, ConstraintValidatorContext constraintValidatorContext) {
        if (userDisciplineSubmissionDto.isBan() == userDisciplineSubmissionDto.isSuspension()) {
//            System.out.println("### in UserDisciplineSubmissionValidator.isValid(). both ban and suspension case.");
            return false;
        }

        if (userDisciplineSubmissionDto.isSuspension()) {
            if (!isNumericString(userDisciplineSubmissionDto.getSuspensionHours())) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext.buildConstraintViolationWithTemplate("Suspension hours value must be between "
                        + MIN_SUSPENSION_HOURS + " and " + MAX_SUSPENSION_HOURS + " (inclusive).").addConstraintViolation();
//            System.out.println("### in UserDisciplineSubmissionValidator.isValid(). suspension value not numeric");
                return false;
            }

            int suspensionHours = Integer.parseInt(userDisciplineSubmissionDto.getSuspensionHours());

            if (suspensionHours < MIN_SUSPENSION_HOURS || suspensionHours > MAX_SUSPENSION_HOURS) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext.buildConstraintViolationWithTemplate("Suspension hours value must be between "
                        + MIN_SUSPENSION_HOURS + " and " + MAX_SUSPENSION_HOURS + " (inclusive).").addConstraintViolation();
//            System.out.println("### in UserDisciplineSubmissionValidator.isValid(). suspension value out of range");
                return false;
            }
        }

//        System.out.println("### in UserDisciplineSubmissionValidator.isValid(). valid case.");
        return true;
    }

    @Override
    public void initialize(ValidUserDisciplineSubmission constraintAnnotation) {
    }

    private boolean isNumericString(String str) {
        return str.matches("^\\d+$");
    }
}
