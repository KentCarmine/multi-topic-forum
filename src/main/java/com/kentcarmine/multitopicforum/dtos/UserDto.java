package com.kentcarmine.multitopicforum.dtos;

import com.kentcarmine.multitopicforum.annotations.PasswordMatches;
import com.kentcarmine.multitopicforum.annotations.ValidCharacters;
import com.kentcarmine.multitopicforum.annotations.ValidEmail;
import com.kentcarmine.multitopicforum.annotations.ValidUsername;

import javax.validation.constraints.Size;


/**
 * Represents a User object received from a form.
 */
@PasswordMatches
public class UserDto implements PasswordDto {

    @Size(min = 4, max = 16, message = "{User.username.length}")
    @ValidCharacters(message = "{User.username.validChars}")
    @ValidUsername
    private String username;

    @ValidEmail(message = "{User.email.mustBeValid}")
    private String email;

    @Size(min = 8, message = "{User.password.length}")
    private String password;
    private String confirmPassword;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getConfirmPassword() {
        return confirmPassword;
    }

    @Override
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
