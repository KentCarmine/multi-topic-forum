package com.kentcarmine.multitopicforum.validators;

import com.kentcarmine.multitopicforum.annotations.ValidCharacters;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validator that processes @ValidCharacter annotations, ensuring that the annotated field contains only alphanumeric
 * characters and '-' and '_' signs.
 */
public class RestrictedCharacterSetValidator implements ConstraintValidator<ValidCharacters, String> {
    private Pattern pattern;
    private Matcher matcher;

    private static final String REGEX = "^[a-zA-Z0-9\\-\\_]*$";

    @Override
    public void initialize(ValidCharacters annotation) {

    }

    @Override
    public boolean isValid(String string, ConstraintValidatorContext context) {
        return validateString(string);
    }

    private boolean validateString(String string) {
        pattern = pattern.compile(REGEX);
        matcher = pattern.matcher(string);
        return matcher.matches();
    }
}
