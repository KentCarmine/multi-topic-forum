package com.kentcarmine.multitopicforum.dtos;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * DTO for TopicThreads, used to create TopicThreads with a first post.
 */
public class TopicThreadCreationDto {

    @NotBlank(message = "title must not be blank")
    @Size(min=4, message="thread title must be at least {min} characters long")
    private String title;

    @NotBlank(message = "Post content must not be blank")
    private String firstPostContent;

    public TopicThreadCreationDto() {
    }

    public TopicThreadCreationDto(
            @NotBlank(message = "title must not be blank")
            @Size(min = 4, message = "thread title must be at least {min} characters long")
                    String title,
            @NotBlank(message = "Post content must not be blank")
                    String firstPostContent) {
        this.title = title;
        this.firstPostContent = firstPostContent;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFirstPostContent() {
        return firstPostContent;
    }

    public void setFirstPostContent(String firstPostContent) {
        this.firstPostContent = firstPostContent;
    }

    @Override
    public String toString() {
        return "TopicThreadDto{" +
                "title='" + title + '\'' +
                ", firstPostContent='" + firstPostContent + '\'' +
                '}';
    }
}
