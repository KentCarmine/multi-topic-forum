package com.kentcarmine.multitopicforum.dtos;

/**
 * Interface specifiying methods for DTO classes that include a password and confirmPassword field.
 */
public interface PasswordDto {
    String getPassword();

    void setPassword(String password);

    String getConfirmPassword();

    void setConfirmPassword(String confirmPassword);
}
