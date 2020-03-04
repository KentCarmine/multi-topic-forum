package com.kentcarmine.multitopicforum.validators;

import com.kentcarmine.multitopicforum.annotations.ValidUserDisciplineSubmission;
import com.kentcarmine.multitopicforum.dtos.UserDisciplineSubmissionDto;
import com.kentcarmine.multitopicforum.services.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator that processes @ValidUserDisciplineSubmission annotations
 **/
public class UserDisciplineSubmissionValidator implements ConstraintValidator<ValidUserDisciplineSubmission, UserDisciplineSubmissionDto> {
    private int min;
    private int max;

    @Override
    public boolean isValid(UserDisciplineSubmissionDto userDisciplineSubmissionDto, ConstraintValidatorContext constraintValidatorContext) {
        if (userDisciplineSubmissionDto.isBan() == userDisciplineSubmissionDto.isSuspension()) {
            return false;
        }

        if (userDisciplineSubmissionDto.isSuspension()) {
            if (!isNumericString(userDisciplineSubmissionDto.getSuspensionHours())) {
                return false;
            }

            int suspensionHours = Integer.parseInt(userDisciplineSubmissionDto.getSuspensionHours());

            if (suspensionHours < min || suspensionHours > max) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void initialize(ValidUserDisciplineSubmission constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    private boolean isNumericString(String str) {
        return str.matches("^\\d+$");
    }
}
