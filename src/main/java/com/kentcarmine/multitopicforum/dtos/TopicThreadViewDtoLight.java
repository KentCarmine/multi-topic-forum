package com.kentcarmine.multitopicforum.dtos;

import com.kentcarmine.multitopicforum.model.User;

import java.util.Date;

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
