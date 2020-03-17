package com.kentcarmine.multitopicforum.dtos;

import com.kentcarmine.multitopicforum.model.ThreadUpdatedTimeable;
import com.kentcarmine.multitopicforum.model.User;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

public class TopicThreadViewDto implements ThreadUpdatedTimeable {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{TopicThread.title.notBlank}")
    @Size(min=4, message="{TopicThread.title.length}")
    private String title;

    @NotNull
    private TopicForumViewDto forum;

    private SortedSet<PostViewDto> posts;

    private boolean isLocked;
    private User lockingUser;

    private String creationTimeDifferenceMessage;
    private String updateTimeDifferenceMessage;

    public TopicThreadViewDto() {
        this.posts = new TreeSet<>();
    }

    public TopicThreadViewDto(String title, TopicForumViewDto forum) {
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

    public TopicForumViewDto getForum() {
        return forum;
    }

    public void setForum(TopicForumViewDto forum) {
        this.forum = forum;
    }

    public SortedSet<PostViewDto> getPosts() {
        return posts;
    }

    public void setPosts(SortedSet<PostViewDto> posts) {
        this.posts = posts;
    }

    public void addPost(PostViewDto post) {
        this.posts.add(post);
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

    public int getPostCount() {
        return this.posts.size();
    }

    public PostViewDto getFirstPost() {
        if (getPosts().isEmpty()) {
            return null;
        }

        return getPosts().first();
    }

    public PostViewDto getLastPost() {
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

    public String getCreationTimeDifferenceMessage() {
        return creationTimeDifferenceMessage;
    }

    public void setCreationTimeDifferenceMessage(String creationTimeDifferenceMessage) {
        this.creationTimeDifferenceMessage = creationTimeDifferenceMessage;
    }

    public String getUpdateTimeDifferenceMessage() {
        return updateTimeDifferenceMessage;
    }

    public void setUpdateTimeDifferenceMessage(String updateTimeDifferenceMessage) {
        this.updateTimeDifferenceMessage = updateTimeDifferenceMessage;
    }

    @Override
    public String toString() {
        User lockingUser = this.lockingUser;

        String forumName = null;
        if (forum != null) {
            forumName = forum.getName();
        }

        StringBuilder sb = new StringBuilder(
                "TopicThreadViewDto{" +
                        "id=" + id +
                        ", title='" + title + '\'' +
                        ", forum=" + forumName +
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
