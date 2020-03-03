package com.kentcarmine.multitopicforum.dtos;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * DTO for TopicThreads, used to create TopicThreads with a first post.
 */
public class TopicThreadCreationDto {

    @NotBlank(message = "TopicThread.title.notBlank")
    @Size(min=4, message="{TopicThread.title.length}")
    private String title;

    @NotBlank(message = "{Post.content.notBlank}")
    private String firstPostContent;

    public TopicThreadCreationDto() {
    }

    public TopicThreadCreationDto(
            @NotBlank(message = "TopicThread.title.notBlank")
            @Size(min=4, message="{TopicThread.title.length}")
                    String title,
            @NotBlank(message = "{Post.content.notBlank}")
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
