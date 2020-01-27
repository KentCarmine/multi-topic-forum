package com.kentcarmine.multitopicforum.model;

/**
 * Enum representing Authorities a User can have.
 */
public enum UserRole {
    USER(1),
    MODERATOR(2),
    ADMINISTRATOR(3),
    SUPER_ADMINISTRATOR(Integer.MAX_VALUE),
    CHANGE_PASSWORD_PRIVILEGE(Integer.MIN_VALUE);

    private int rank;

    UserRole(int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public boolean isHigherRank(UserRole other) {
        return this.rank > other.rank;
    }
}
