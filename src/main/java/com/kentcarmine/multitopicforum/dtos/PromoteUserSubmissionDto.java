package com.kentcarmine.multitopicforum.dtos;

import com.kentcarmine.multitopicforum.model.UserRole;

/**
 * DTO representing a request from the client to promote the user with the given username to the given rank.
 */
public class PromoteUserSubmissionDto {
    private String username;
    private UserRole promotableRank;

    public PromoteUserSubmissionDto() {
    }

    public PromoteUserSubmissionDto(String username, String promotableRank) {
        this.username = username;
        this.promotableRank = UserRole.valueOf(promotableRank);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserRole getPromotableRank() {
        return promotableRank;
    }

    public void setPromotableRank(String promotableRank) {
        this.promotableRank = UserRole.valueOf(promotableRank);
    }

    @Override
    public String toString() {
        return "PromoteUserSubmissionDto{" +
                "username='" + username + '\'' +
                ", promotableRank=" + promotableRank +
                '}';
    }
}
