package com.kentcarmine.multitopicforum.dtos;

import com.kentcarmine.multitopicforum.annotations.ValidCharacters;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * DTO for TopicForum objects. Used to create TopicForums
 */
public class TopicForumDto {

    @NotBlank(message = "name must not be blank")
    @Size(min=4, message="forum name must be at least {min} characters long")
    @ValidCharacters(message = "name must consist only of letters, numbers, - and _ characters")
    private String name;

    @NotBlank(message = "description must not be blank")
    @Size(min = 1, max = 500, message = "Description must be between {min} and {max} characters long")
    private String description;

    public TopicForumDto() {
    }

    public TopicForumDto(
            @NotBlank(message = "name must not be blank")
            @Size(min = 4, message = "forum name must be at least {min} characters long")
            @ValidCharacters(message = "name must consist only of letters, numbers, - and _ characters")
                    String name,
            @NotBlank
            @Size(min = 1, max = 500, message = "Description must be between {min} and {max} characters long")
                    String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(@NotBlank(message = "name must not be blank")
                        @Size(min = 4, message = "forum name must be at least {min} characters long")
                        @ValidCharacters(message = "name must consist only of letters, numbers, - and _ characters")
                                String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(@NotBlank
                               @Size(min = 1, max = 500,
                                       message = "Description must be between {min} and {max} characters long")
                                       String description) {
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
