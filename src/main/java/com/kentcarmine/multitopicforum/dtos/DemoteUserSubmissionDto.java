package com.kentcarmine.multitopicforum.dtos;

import com.kentcarmine.multitopicforum.model.UserRole;

/**
 * DTO representing a request from the client to demote the user with the given username to the given rank.
 */
public class DemoteUserSubmissionDto {
    private String username;
    private UserRole demotableRank;

    public DemoteUserSubmissionDto() {
    }

    public DemoteUserSubmissionDto(String username, String demotableRank) {
        this.username = username;
        this.demotableRank = UserRole.valueOf(demotableRank);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserRole getDemotableRank() {
        return demotableRank;
    }

    public void setDemotableRank(String demotableRank) {
        this.demotableRank = UserRole.valueOf(demotableRank);;
    }

    @Override
    public String toString() {
        return "DemoteUserSubmissionDto{" +
                "username='" + username + '\'' +
                ", demotableRank=" + demotableRank +
                '}';
    }
}
