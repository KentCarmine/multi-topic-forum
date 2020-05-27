package com.kentcarmine.multitopicforum.dtos;

import javax.validation.constraints.NotBlank;

/**
 * DTO used for creating new Posts in a TopicThread
 */
public class PostCreationDto {

    @NotBlank(message = "Post content must not be blank")
    private String content;

    private int postPageNum;

    public PostCreationDto() {
        this.postPageNum = 1;
    }

    public PostCreationDto(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getPostPageNum() {
        return postPageNum;
    }

    public void setPostPageNum(int postPageNum) {
        this.postPageNum = postPageNum;
    }
}
