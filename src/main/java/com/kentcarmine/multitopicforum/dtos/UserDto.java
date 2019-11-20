package com.kentcarmine.multitopicforum.dtos;

import com.kentcarmine.multitopicforum.annotations.PasswordMatches;
import com.kentcarmine.multitopicforum.annotations.ValidEmail;

import javax.validation.constraints.Size;


@PasswordMatches
public class UserDto {

    @Size(min = 4, message = "Username must be at least {min} characters long")
    private String username;

    @ValidEmail(message = "email must be a valid email address")
    private String email;

    @Size(min = 8, message = "password must be at least {min} characters long")
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
