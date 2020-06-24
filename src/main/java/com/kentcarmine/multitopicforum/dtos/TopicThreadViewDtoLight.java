package com.kentcarmine.multitopicforum.dtos;

import com.kentcarmine.multitopicforum.model.User;

import java.util.Date;

/**
 * Lightweight thread model DTO that does not include the lists of posts belonging to the thread, only the first and
 * last posts and a count of the total posts.
 */
public class TopicThreadViewDtoLight extends AbstractTopicThreadViewDto {

//    private User creator;
//    private Date createdAt;

    private PostViewDto firstPost;
    private PostViewDto lastPost;

    private int postCount;

    public TopicThreadViewDtoLight() {
        super();
    }

    public TopicThreadViewDtoLight(String title, TopicForumViewDtoLight forum) {
//        this.title = title;
//        this.forum = forum;
        super(title, forum);
//        this.isLocked = false;
        postCount = 0;
    }

    @Override
    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    public void setFirstPost(PostViewDto firstPost) {
        this.firstPost = firstPost;
    }

    public void setLastPost(PostViewDto lastPost) {
        this.lastPost = lastPost;
    }

    @Override
    public PostViewDto getFirstPost() {
        return firstPost;
    }

    @Override
    public PostViewDto getLastPost() {
        return lastPost;
    }

}
