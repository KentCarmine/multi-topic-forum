package com.kentcarmine.multitopicforum.dtos;

import com.kentcarmine.multitopicforum.annotations.PasswordMatches;
import com.kentcarmine.multitopicforum.annotations.ValidCharacters;

import javax.validation.constraints.Size;

/**
 * Represents a password and password confirmation field recieved from a form. Also includes information about the user
 * and a password reset token.
 */
@PasswordMatches
public class UserPasswordDto implements PasswordDto {

    @Size(min = 8, message = "password must be at least {min} characters long")
    private String password;

    private String confirmPassword;

    @ValidCharacters(message = "username must consist only of letters, numbers, - and _ characters")
    private String username;

    private String token;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
