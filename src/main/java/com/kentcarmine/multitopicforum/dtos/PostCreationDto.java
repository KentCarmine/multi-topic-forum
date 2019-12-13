package com.kentcarmine.multitopicforum.dtos;

import javax.validation.constraints.NotBlank;

/**
 * DTO used for creating new Posts in a TopicThread
 */
public class PostCreationDto {

    @NotBlank(message = "Post content must not be blank")
    private String content;

    public PostCreationDto() {
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
}
