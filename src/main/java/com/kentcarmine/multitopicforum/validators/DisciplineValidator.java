package com.kentcarmine.multitopicforum.validators;

import com.kentcarmine.multitopicforum.annotations.ValidDiscipline;
import com.kentcarmine.multitopicforum.model.Discipline;
import com.kentcarmine.multitopicforum.model.DisciplineType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator that processes @ValidDiscipline annotations
 */
public class DisciplineValidator implements ConstraintValidator<ValidDiscipline, Discipline> {

    @Override
    public void initialize(ValidDiscipline constraintAnnotation) {
    }

    @Override
    public boolean isValid(Discipline discipline, ConstraintValidatorContext constraintValidatorContext) {
        if (discipline.getDisciplineType().equals(DisciplineType.BAN)) {
            return discipline.getDisciplineDurationHours() == null && discipline.getDisciplineEndTime() == null;
        } else if (discipline.getDisciplineType().equals(DisciplineType.SUSPENSION)){
            if (discipline.getDisciplineDurationHours() == null || discipline.getDisciplineEndTime() == null || discipline.getDisciplineDurationHours().longValue() <= 0) {
                return false;
            }

            return true;
        }

        return false;
    }
}
