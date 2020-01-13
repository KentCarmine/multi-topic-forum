package com.kentcarmine.multitopicforum.validators;

import com.kentcarmine.multitopicforum.annotations.ValidSearchString;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator that processes @ValidSearchString annotations, ensuring that the annotated string is valid search text.
 * In this case, it checks to ensure that there are an even number of quotation marks.
 */
public class SearchStringValidator implements ConstraintValidator<ValidSearchString, String> {

    @Override
    public void initialize(final ValidSearchString constraintAnnotation) {
    }

    @Override
    public boolean isValid(String searchString, ConstraintValidatorContext constraintValidatorContext) {
        long quoteCount = searchString.chars().filter(c -> c == '"').count();

        return quoteCount % 2 == 0;
    }
}
