package com.kentcarmine.multitopicforum.dtos;

import com.kentcarmine.multitopicforum.annotations.PasswordMatches;

import javax.validation.constraints.Size;

@PasswordMatches
public class UserPasswordDto implements PasswordDto {

    @Size(min = 8, message = "password must be at least {min} characters long")
    private String password;

    private String confirmPassword;

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
