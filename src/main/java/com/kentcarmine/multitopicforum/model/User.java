package com.kentcarmine.multitopicforum.model;

import com.kentcarmine.multitopicforum.annotations.ValidCharacters;
import com.kentcarmine.multitopicforum.annotations.ValidEmail;
import com.kentcarmine.multitopicforum.annotations.ValidUsername;
import com.kentcarmine.multitopicforum.helpers.ReverseDateOrderPostComparator;
import org.hibernate.annotations.SortComparator;
import org.hibernate.annotations.SortNatural;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Entity representing a user
 */
@Entity
@Table(name="users")
public class User {

    @Id
    @Size(min = 4, message = "Username must be at least {min} characters long")
    @ValidCharacters(message = "username must consist only of letters, numbers, - and _ characters")
    @ValidUsername
    private String username;

    @Size(min = 8, message = "password must be at least {min} characters long")
    private String password;

    @Column(name = "email", unique = true)
    @ValidEmail(message = "email must be a valid email address")
    private String email;

    @SortNatural
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private SortedSet<Authority> authorities;

    @SortComparator(ReverseDateOrderPostComparator.class)
    @OneToMany(mappedBy = "user")
    private SortedSet<Post> posts;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<PostVote> postVotes;

    @OneToMany(mappedBy = "disciplinedUser", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Discipline> disciplines;

    private boolean enabled;

    public User() {
        this.authorities = new TreeSet<>();
        this.enabled = false;
        this.posts = new TreeSet<>();
        this.postVotes = new HashSet<>();
    }

    public User(@Size(min = 4, message = "Username must be at least {min} characters long")
                @ValidCharacters(message = "username must consist only of letters, numbers, - and _ characters") String username,
                @Size(min = 8, message = "password must be at least {min} characters long") String password,
                @ValidEmail(message = "email must be a valid email address") String email,
                @NotEmpty(message = "user must have at least one role") SortedSet<Authority> authorities) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.authorities = authorities;
        this.enabled = false;
        this.posts = new TreeSet<>();
        this.postVotes = new HashSet<>();
        this.disciplines = new HashSet<>();
    }

    public User(@Size(min = 4, message = "Username must be at least {min} characters long")
                @ValidCharacters(message = "username must consist only of letters, numbers, - and _ characters") String username,
                @Size(min = 8, message = "password must be at least {min} characters long") String password,
                @ValidEmail(message = "email must be a valid email address") String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.authorities = new TreeSet<>();
        this.enabled = false;
        this.posts = new TreeSet<>();
        this.postVotes = new HashSet<>();
        this.disciplines = new HashSet<>();
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

    public Set<PostVote> getPostVotes() {
        return postVotes;
    }

    public void setPostVotes(Set<PostVote> postVotes) {
        this.postVotes = postVotes;
    }

    public SortedSet<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(SortedSet<Authority> authorities) {
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

//    public boolean isHigherAuthority(User otherUser) {
//        UserRole thisRank = null;
//        for (Authority a : getAuthorities()) {
//            if (thisRank == null || a.getAuthority().isHigherRank(thisRank)) {
//                thisRank = a.getAuthority();
//            }
//        }
//
//        UserRole otherRank = null;
//        for (Authority a : otherUser.getAuthorities()) {
//            if (otherRank == null || a.getAuthority().isHigherRank(otherRank)) {
//                otherRank = a.getAuthority();
//            }
//        }
//
//        return thisRank.isHigherRank(otherRank);
//    }

    public boolean isHigherAuthority(User otherUser) {
//        System.out.println("### in isHigherAuthority(). THIS.getHighestAuthority() = " + this.getHighestAuthority());
//        System.out.println("### in isHigherAuthority(). otherUser = " + otherUser);
        return this.getHighestAuthority().isHigherRank(otherUser.getHighestAuthority());
    }

    public void removeAuthority(UserRole roleToRemove) {
//        System.out.println("### Auth size before: " + authorities.size());
        this.authorities.removeIf((a) -> a.getAuthority().equals(roleToRemove));
//        System.out.println("### Auth size after: " + authorities.size());
    }

    public boolean hasAuthority(UserRole role) {
        return this.authorities.stream().anyMatch((a) -> a.getAuthority().equals(role));
    }

    /**
     * Get this user's highest UserRole authority
     *
     * @return this user's highest UserRole authority
     */
    public UserRole getHighestAuthority() {
        return authorities.last().getAuthority();
    }

    /**
     * Get the rank one grade higher than this user's maximum rank
     *
     * @return the rank one grade higher than this user's maximum rank
     */
    public UserRole getIncrementedRank() {
        return UserRole.getNextAuthority(this.getHighestAuthority());
    }

    /**
     * Get the rank one grade lower than this user's maximum rank
     *
     * @return the rank one grade lower than this user's maximum rank
     */
    public UserRole getDecrementedRank() {
        return UserRole.getPreviousAuthority(this.getHighestAuthority());
    }

    /**
     * Determines if this user is promotable by the given otherUser
     *
     * @param otherUser the user to check if they can promote this user
     * @return true if the other user can promote this user, false otherwise
     */
    public boolean isPromotableBy(User otherUser) {
        if (this.getIncrementedRank() == null) {
            return false;
        }

        if (otherUser.getHighestAuthority().isHigherRank(this.getIncrementedRank())) {
            return true;
        }

        return false;
    }

    /**
     * Determines if this user is demotable by the given otherUser
     *
     * @param otherUser the user to check if they can demote this user
     * @return true if the other user can demote this user, false otherwise
     */
    public boolean isDemotableBy(User otherUser) {
        if (this.getDecrementedRank() == null) {
            return false;
        }

        if (otherUser.getHighestAuthority().isHigherRank(this.getHighestAuthority())) {
            return true;
        }

        return false;
    }

    public boolean isSuperadmin() {
        return getHighestAuthority().equals(UserRole.SUPER_ADMINISTRATOR);
    }

    public boolean isAdmin() {
        return getHighestAuthority().equals(UserRole.ADMINISTRATOR);
    }

    public boolean isModerator() {
        return getHighestAuthority().equals(UserRole.MODERATOR);
    }

    public boolean isUser() {
        return getHighestAuthority().equals(UserRole.USER);
    }

    public SortedSet<Post> getPosts() {
        return posts;
    }

    public void setPosts(SortedSet<Post> posts) {
        this.posts = posts;
    }

    public Set<Discipline> getDisciplines() {
        return disciplines;
    }

    public void setDisciplines(Set<Discipline> disciplines) {
        this.disciplines = disciplines;
    }

    public void addDiscipline(Discipline newDiscipline) {
        this.disciplines.add(newDiscipline);
    }

    /**
     * Returns true if this user is currently banned or suspended. False otherwise.
     *
     * @return true if this user is currently banned or suspended. False otherwise.
     */
    public boolean isBannedOrSuspended() {
        for (Discipline disc : disciplines) {
            if (disc.isActive()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if this user is currently banned. False otherwise.
     *
     * @return true if this user is currently banned. False otherwise.
     */
    public boolean isBanned() {
        for (Discipline disc : disciplines) {
            if (disc.isActive() && disc.isBan()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns a set of disciplines associated with this user that are currently active.
     *
     * @return a set of disciplines associated with this user that are currently active.
     */
    public Set<Discipline> getActiveDisciplines() {
        return disciplines.stream().filter(Discipline::isActive).collect(Collectors.toSet());
    }

    /**
     * Returns a set of disciplines associated with this user that are currently inactive.
     *
     * @return a set of disciplines associated with this user that are currently inactive.
     */
    public Set<Discipline> getInactiveDisciplines() {
        return disciplines.stream().filter(Discipline::isOver).collect(Collectors.toSet());
    }

    /**
     * Gets the active discipline associated with this user that has the greatest duration, or null if this user has no
     * active disciplines
     *
     * @return the active discipline associated with this user that has the greatest duration, or null if this user has
     * no active disciplines
     */
    public Discipline getGreatestDurationActiveDiscipline() {
        Set<Discipline> activeDisciplines = getActiveDisciplines();

        Discipline mostSevere = null;
//        Date now = Date.from(Instant.now());

        for (Discipline ad : activeDisciplines) {
            if (ad.getDisciplineType().equals(DisciplineType.BAN)) {
                return ad;
            }

            if (ad.getDisciplineType().equals(DisciplineType.SUSPENSION)
                    && (mostSevere == null || ad.getDisciplineEndTime().after(mostSevere.getDisciplineEndTime()))) {
                mostSevere = ad;
            }
        }

        return mostSevere;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return username.equals(user.username);
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
