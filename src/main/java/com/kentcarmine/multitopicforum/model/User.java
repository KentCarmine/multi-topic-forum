package com.kentcarmine.multitopicforum.model;

import com.kentcarmine.multitopicforum.annotations.ValidEmail;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="users")
public class User {

    @Id
    @Size(min = 4, message = "Username must be at least {min} characters long")
    private String username;

    @Size(min = 8, message = "password must be at least {min} characters long")
    private String password;

    @Column(name = "email", unique = true)
    @ValidEmail(message = "email must be a valid email address")
    private String email;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Authority> authorities;

    // private List<Posts> posts; // TODO: Wire up (1 User - many Posts)

    // TODO: Upvote/Downvote tracking to prevent duplicate votes

    private boolean enabled;

    public User() {
        this.authorities = new HashSet<>();
        this.enabled = false;
    }

    public User(@Size(min = 4, message = "Username must be at least {min} characters long") String username,
                @Size(min = 8, message = "password must be at least {min} characters long") String password,
                @ValidEmail(message = "email must be a valid email address") String email,
                @NotEmpty(message = "user must have at least one role") Set<Authority> authorities) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.authorities = authorities;
        this.enabled = false;
    }

    public User(@Size(min = 4, message = "Username must be at least {min} characters long") String username,
                @Size(min = 8, message = "password must be at least {min} characters long") String password,
                @ValidEmail(message = "email must be a valid email address") String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.authorities = new HashSet<>();
        this.enabled = false;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<Authority> authorities) {
        this.authorities = authorities;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void addAuthority(UserRole newRole) {
        authorities.add(new Authority(this, newRole));
    }

    public void addAuthorities(UserRole ...newRoles) {
        for (UserRole role : newRoles) {
            addAuthority(role);
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", authorities=" + authorities +
                '}';
    }
}
