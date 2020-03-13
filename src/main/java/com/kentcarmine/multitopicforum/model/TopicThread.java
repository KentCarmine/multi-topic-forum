package com.kentcarmine.multitopicforum.model;

import org.hibernate.annotations.SortNatural;
import org.springframework.beans.factory.annotation.Value;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.*;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Entity that models a topic thread, that is, a thread of one or more posts within a given topic forum.
 */
@Entity
public class TopicThread {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{TopicThread.title.notBlank}")
    @Size(min=4, message="{TopicThread.title.length}")
    private String title;

    @ManyToOne
    @JoinColumn(name = "forumName")
    @NotNull
    private TopicForum forum;

    @SortNatural
    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private SortedSet<Post> posts;

    private boolean isLocked;

    @ManyToOne
    @JoinColumn(name = "lockingUsername")
    private User lockingUser;

    // TODO: Add methods to get creation date and creating user of thread (by getting those values from first post)

    public TopicThread() {
        this.posts = new TreeSet<>();
    }

    public TopicThread(String title, TopicForum forum) {
        this.title = title;
        this.forum = forum;
        this.posts = new TreeSet<>();
        this.isLocked = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public SortedSet<Post> getPosts() {
        return posts;
    }

    public void setPosts(SortedSet<Post> posts) {
        this.posts = posts;
    }

    public int getPostCount() {
        return this.posts.size();
    }

    public Post getFirstPost() {
        if (getPosts().isEmpty()) {
            return null;
        }

        return getPosts().first();
    }

    public Post getLastPost() {
        if (getPosts().isEmpty()) {
            return null;
        }

        return getPosts().last();
    }

    public Date getCreatedAt() {
        return getFirstPost().getPostedAt();
    }

    public User getCreator() {
        return getFirstPost().getUser();
    }

    public TopicForum getForum() {
        return forum;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public User getLockingUser() {
        return lockingUser;
    }

    public void setLockingUser(User lockingUser) {
        this.lockingUser = lockingUser;
    }

    public void lock(User lockingUser) {
        setLocked(true);
        setLockingUser(lockingUser);
    }

    public void unlock() {
        setLocked(false);
        setLockingUser(null);
    }

    /**
     * Gets a string representation of the amount of time since this thread was created. Only includes the amount of
     * time in the largest whole unit of time.
     *
     * @return a string representation of the amount of time since this thread was created.
     */
    public String getTimeSinceCreation() {
        return this.getFirstPost().getTimeSinceCreation();
    }

    /**
     * Gets a string representation of the amount of time since the most recent post was added to it. Only includes the
     * amount of time in the largest whole unit of time.
     *
     * @return a string representation of the amount of time since this thread was most recently updated with a post.
     */
    public String getTimeSinceMostRecentPost() {
        return this.getLastPost().getTimeSinceCreation();
    }

    @Override
    public String toString() {
        User lockingUser = this.lockingUser;
        StringBuilder sb = new StringBuilder(
                "TopicThread{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", forum=" + forum +
                ", posts=" + posts +
                ", isLocked=" + isLocked);

        if (lockingUser != null) {
            sb.append(", lockingUser=" + lockingUser.getUsername());
        } else {
            sb.append(", lockingUser=null");
        }

        sb.append('}');

        return sb.toString();
    }
}
