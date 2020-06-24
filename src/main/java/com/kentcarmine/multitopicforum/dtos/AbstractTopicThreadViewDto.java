package com.kentcarmine.multitopicforum.dtos;

import com.kentcarmine.multitopicforum.model.ThreadUpdatedTimeable;
import com.kentcarmine.multitopicforum.model.User;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;


public abstract class AbstractTopicThreadViewDto  implements ThreadUpdatedTimeable {

    //    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{TopicThread.title.notBlank}")
    @Size(min=4, message="{TopicThread.title.length}")
    private String title;

    @NotNull
    private AbstractTopicForumViewDto forum;

//    private SortedSet<PostViewDto> posts;

    private boolean isLocked;
    private User lockingUser;

    private String creationTimeDifferenceMessage;
    private String updateTimeDifferenceMessage;

    private Date createdAt;
    private Date updatedAt;

    public AbstractTopicThreadViewDto() {

    }

    public AbstractTopicThreadViewDto(String title, AbstractTopicForumViewDto forum) {
        this.title = title;
        this.forum = forum;
//        this.posts = new TreeSet<>();
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

    public AbstractTopicForumViewDto getForum() {
        return forum;
    }

    public void setForum(AbstractTopicForumViewDto forum) {
        this.forum = forum;
    }

//    public SortedSet<PostViewDto> getPosts() {
//        return posts;
//    }
//
//    public void setPosts(SortedSet<PostViewDto> posts) {
//        this.posts = posts;
//    }
//
//    public void addPost(PostViewDto post) {
//        this.posts.add(post);
//    }

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

    public abstract int getPostCount();

    public abstract PostViewDto getFirstPost();

    public abstract PostViewDto getLastPost();

    public Date getCreatedAt() {
        return createdAt;
    }

//    public abstract User getCreator();

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

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        User lockingUser = this.lockingUser;

        String forumName = null;
        if (forum != null) {
            forumName = forum.getName();
        }

        StringBuilder sb = new StringBuilder(
                "AbstractTopicThreadViewDto{" +
                        "id=" + id +
                        ", title='" + title + '\'' +
                        ", forum=" + forumName +
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
