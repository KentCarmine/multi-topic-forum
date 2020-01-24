package com.kentcarmine.multitopicforum.validators;

import com.kentcarmine.multitopicforum.annotations.ValidVoteValue;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class VoteValueValidator implements ConstraintValidator<ValidVoteValue, Integer> {

    @Override
    public void initialize(final ValidVoteValue constraintAnnotation) {
    }

    @Override
    public boolean isValid(Integer integer, ConstraintValidatorContext constraintValidatorContext) {
        return (integer.intValue() == 1) || (integer.intValue() == -1);
    }
}
