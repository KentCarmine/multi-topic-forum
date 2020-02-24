package com.kentcarmine.multitopicforum.model;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Enum representing Authorities a User can have.
 */
public enum UserRole {
    USER(1, "User"),
    MODERATOR(2, "Moderator"),
    ADMINISTRATOR(3, "Administrator"),
    SUPER_ADMINISTRATOR(Integer.MAX_VALUE, "Superadministrator"),
    CHANGE_PASSWORD_PRIVILEGE(Integer.MIN_VALUE, "Change Password Privilege");

    private static final SortedSet<UserRole> sortedRanks = constructSortedRanks();

    private int rank; // Positive rank values indicate overall authority, negative values indicate other permissions
    private String displayRank;

    UserRole(int rank, String displayRank) {
        this.rank = rank;
        this.displayRank = displayRank;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getDisplayRank() {
        return displayRank;
    }

    public void setDisplayRank(String displayRank) {
        this.displayRank = displayRank;
    }

    public boolean isHigherRank(UserRole other) {
        return this.rank > other.rank;
    }

    private static SortedSet<UserRole> constructSortedRanks() {
        SortedSet<UserRole> sortedRanks = new TreeSet<>(new Comparator<UserRole>() {
            @Override
            public int compare(UserRole o1, UserRole o2) {
                if (o1.rank > o2.rank) {
                    return 1;
                } else if (o1.rank < o2.rank) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        List<UserRole> enumList = Stream.of(UserRole.values()).collect(Collectors.toList());

        sortedRanks.addAll(enumList);

        return sortedRanks;
    }

    public static UserRole getNextAuthority(UserRole authority) {
        Iterator<UserRole> iter = sortedRanks.iterator();
        UserRole cur = iter.next();

        while (iter.hasNext()) {
            if(cur.equals(authority) && iter.hasNext()) {
                return iter.next();
            }

            cur = iter.next();
        }

        return null;
    }

    public static UserRole getPreviousAuthority(UserRole authority) {
        LinkedList<UserRole> llist = new LinkedList<>(sortedRanks);
        UserRole prev = null;

        for (UserRole role : llist) {
            if (role.equals(authority) && prev != null && prev.getRank() > 0) {
                return prev;
            }
            prev = role;
        }

        return null;
    }
}
