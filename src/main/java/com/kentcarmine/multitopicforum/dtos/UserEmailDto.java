package com.kentcarmine.multitopicforum.dtos;

import com.kentcarmine.multitopicforum.annotations.ValidEmail;

/**
 * Represents an email address received from a form
 */
public class UserEmailDto {

    @ValidEmail(message = "email must be a valid email address")
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
