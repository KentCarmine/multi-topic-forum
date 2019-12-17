package com.kentcarmine.multitopicforum.model;

import com.kentcarmine.multitopicforum.annotations.ValidCharacters;
import com.kentcarmine.multitopicforum.annotations.ValidEmail;
import com.kentcarmine.multitopicforum.helpers.ReverseDateOrderPostComparator;
import org.hibernate.annotations.SortComparator;
import org.hibernate.annotations.SortNatural;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.*;

/**
 * Entity representing a user
 */
@Entity
@Table(name="users")
public class User {

    @Id
    @Size(min = 4, message = "Username must be at least {min} characters long")
    @ValidCharacters(message = "username must consist only of letters, numbers, - and _ characters")
    private String username;

    @Size(min = 8, message = "password must be at least {min} characters long")
    private String password;

    @Column(name = "email", unique = true)
    @ValidEmail(message = "email must be a valid email address")
    private String email;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Authority> authorities;

    @SortComparator(ReverseDateOrderPostComparator.class)
    @OneToMany(mappedBy = "user")
    private SortedSet<Post> posts;

    // TODO: Upvote/Downvote tracking to prevent duplicate votes

    private boolean enabled;

    public User() {
        this.authorities = new HashSet<>();
        this.enabled = false;
        this.posts = new TreeSet<>();
    }

    public User(@Size(min = 4, message = "Username must be at least {min} characters long")
                @ValidCharacters(message = "username must consist only of letters, numbers, - and _ characters") String username,
                @Size(min = 8, message = "password must be at least {min} characters long") String password,
                @ValidEmail(message = "email must be a valid email address") String email,
                @NotEmpty(message = "user must have at least one role") Set<Authority> authorities) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.authorities = authorities;
        this.enabled = false;
        this.posts = new TreeSet<>();
    }

    public User(@Size(min = 4, message = "Username must be at least {min} characters long")
                @ValidCharacters(message = "username must consist only of letters, numbers, - and _ characters") String username,
                @Size(min = 8, message = "password must be at least {min} characters long") String password,
                @ValidEmail(message = "email must be a valid email address") String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.authorities = new HashSet<>();
        this.enabled = false;
        this.posts = new TreeSet<>();
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

    public void removeAuthority(UserRole roleToRemove) {
        System.out.println("### Auth size before: " + authorities.size());
        this.authorities.removeIf((a) -> a.getAuthority().equals(roleToRemove));
        System.out.println("### Auth size after: " + authorities.size());
    }

    public boolean hasAuthority(UserRole role) {
        return this.authorities.stream().anyMatch((a) -> a.getAuthority().equals(role));
    }

    public SortedSet<Post> getPosts() {
        return posts;
    }

    public void setPosts(SortedSet<Post> posts) {
        this.posts = posts;
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
