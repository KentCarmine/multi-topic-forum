package com.kentcarmine.multitopicforum.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Entity representing user Authorities
 */
@Entity
@Table(name="authorities")
public class Authority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="username")
    @NotNull
    private User user;

    @NotNull
    @Enumerated(value = EnumType.STRING)
    private UserRole authority;

    public Authority() {
    }

    public Authority(@NotNull User user, @NotNull UserRole authority) {
        this.user = user;
        this.authority = authority;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserRole getAuthority() {
        return authority;
    }

    public void setAuthority(UserRole authority) {
        this.authority = authority;
    }

    @Override
    public String toString() {
        String username = null;
        if (user != null) {
            username = user.getUsername();
        }

        return "Authority{" +
                "id=" + id +
                ", user=" + username +
                ", authority=" + authority +
                '}';
    }
}
