package com.kentcarmine.multitopicforum.dtos;

import com.kentcarmine.multitopicforum.model.UserRole;

/**
 * DTO Containing data used when setting state of promote/demote buttons on user pages.
 */
public class UserRankAdjustmentDto {

    private String username;
    private UserRole highestAuthority;
    private UserRole incrementedRank;
    private UserRole decrementedRank;
    private boolean isPromotableByLoggedInUser;
    private boolean isDemotableByLoggedInUser;

    public UserRankAdjustmentDto() {
    }

    public UserRankAdjustmentDto(String username, UserRole highestAuthority, UserRole incrementedRank, UserRole decrementedRank) {
        this.username = username;
        this.highestAuthority = highestAuthority;
        this.incrementedRank = incrementedRank;
        this.decrementedRank = decrementedRank;
        this.isPromotableByLoggedInUser = false;
        this.isDemotableByLoggedInUser = false;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserRole getHighestAuthority() {
        return highestAuthority;
    }

    public void setHighestAuthority(UserRole highestAuthority) {
        this.highestAuthority = highestAuthority;
    }

    public UserRole getIncrementedRank() {
        return incrementedRank;
    }

    public void setIncrementedRank(UserRole incrementedRank) {
        this.incrementedRank = incrementedRank;
    }

    public UserRole getDecrementedRank() {
        return decrementedRank;
    }

    public void setDecrementedRank(UserRole decrementedRank) {
        this.decrementedRank = decrementedRank;
    }

    public boolean isPromotableByLoggedInUser() {
        return isPromotableByLoggedInUser;
    }

    public void setPromotableByLoggedInUser(boolean promotableByLoggedInUser) {
        isPromotableByLoggedInUser = promotableByLoggedInUser;
    }

    public boolean isDemotableByLoggedInUser() {
        return isDemotableByLoggedInUser;
    }

    public void setDemotableByLoggedInUser(boolean demotableByLoggedInUser) {
        isDemotableByLoggedInUser = demotableByLoggedInUser;
    }

    @Override
    public String toString() {
        return "UserRankAdjustmentDto{" +
                "username='" + username + '\'' +
                ", highestAuthority=" + highestAuthority +
                ", incrementedRank=" + incrementedRank +
                ", decrementedRank=" + decrementedRank +
                ", isPromotableByLoggedInUser=" + isPromotableByLoggedInUser +
                ", isDemotableByLoggedInUser=" + isDemotableByLoggedInUser +
                '}';
    }
}
