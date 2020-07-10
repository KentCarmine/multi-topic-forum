package com.kentcarmine.multitopicforum.model;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

/**
 * Entity representing an account registration verificaiton token associated with a user account. Used when creating a
 * user account. Can expire.
 */
@Entity
public class VerificationToken {
    private static final int EXPIRATION = 24 * 60; // 24 hrs
//    private static final int EXPIRATION = 1; // For testing only

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "username")
    private User user;

    private Date expiryDate;

    public VerificationToken() {

    }

    public VerificationToken(String token, User user) {
        this.token = token;
        this.user = user;
        this.expiryDate = calculateExpiryDate(EXPIRATION);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void updateToken(String newToken) {
        this.token = newToken;
        this.expiryDate = calculateExpiryDate(EXPIRATION);
    }

    @Override
    public String toString() {
        return "VerificationToken{" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", user=" + user +
                ", expiryDate=" + expiryDate +
                '}';
    }

    private Date calculateExpiryDate(int expiryMins) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Timestamp(calendar.getTime().getTime()));
        calendar.add(Calendar.MINUTE, expiryMins);

        return new Date(calendar.getTime().getTime());
    }
}
