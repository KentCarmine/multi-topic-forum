package com.kentcarmine.multitopicforum.dtos;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * DTO for TopicForum objects. Used to create TopicForums
 */
public class TopicForumDto {

    @NotBlank(message = "name must not be blank")
    @Size(min=4, message="forum name must be at least {min} characters long")
    private String name;

    @NotBlank(message = "description must not be blank")
    @Size(min = 1, max = 500, message = "Description must be between {min} and {max} characters long")
    private String description;

    public TopicForumDto() {
    }

    public TopicForumDto(@Size(min = 4, message = "forum name must be at least {min} characters long") String name, @NotBlank String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(@Size(min = 4, message = "forum name must be at least {min} characters long") String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(@NotBlank String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "TopicForumDto{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
