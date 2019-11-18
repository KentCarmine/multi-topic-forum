package com.kentcarmine.multitopicforum.model;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="users")
public class User {

    // TODO: Ensure unique
    @Id
    @Size(min = 4, message = "Username must be at least {min} characters long")
    private String username;

    @Size(min = 8, message = "password must be at least {min} characters long")
    private String password;

    // TODO: Ensure unique
    @Email(message = "email must be a valid email address")
    private String email;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Authority> authorities;

    // private List<Posts> posts; // TODO: Wire up (1 User - many Posts)

    // TODO: Upvote/Downvote tracking to prevent duplicate votes

    public User() {
    }

    public User(String username, String password, String email, Set<Authority> authorities) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.authorities = authorities;
    }

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.authorities = new HashSet<>();
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
