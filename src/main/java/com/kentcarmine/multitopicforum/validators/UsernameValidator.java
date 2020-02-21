package com.kentcarmine.multitopicforum.validators;

import com.kentcarmine.multitopicforum.annotations.ValidUsername;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator for ValidUsername annotations.
 */
public class UsernameValidator implements ConstraintValidator<ValidUsername, String> {
    private static final String[] FORBIDDEN_USERNAMES = {"anonymous", "anonymousUser", "anonymous_user"};

    @Override
    public boolean isValid(String username, ConstraintValidatorContext constraintValidatorContext) {
//        if (username == null) {
//            return false;
//        }

        for (String forbiddenName : FORBIDDEN_USERNAMES) {
            if (username.equalsIgnoreCase(forbiddenName)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void initialize(ValidUsername constraintAnnotation) {
    }
}
