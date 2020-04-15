package com.kentcarmine.multitopicforum.dtos;

/**
 * Class that models a result of a search for users, including a user's name and a string indicating the last time
 * they were active
 */
public class UserSearchResultDto implements Comparable<UserSearchResultDto> {

    private String username;

    private String lastActive;

    public UserSearchResultDto() {
    }

    public UserSearchResultDto(String username, String lastActive) {
        this.username = username;
        this.lastActive = lastActive;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLastActive() {
        return lastActive;
    }

    public void setLastActive(String lastActive) {
        this.lastActive = lastActive;
    }

    @Override
    public int compareTo(UserSearchResultDto o) {
        return username.compareToIgnoreCase(o.getUsername());
    }

    @Override
    public String toString() {
        return "UserSearchResultDto{" +
                "username='" + username + '\'' +
                ", lastActive='" + lastActive + '\'' +
                '}';
    }
}
