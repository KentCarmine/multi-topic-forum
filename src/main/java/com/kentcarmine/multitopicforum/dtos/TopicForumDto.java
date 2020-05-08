package com.kentcarmine.multitopicforum.dtos;

import com.kentcarmine.multitopicforum.annotations.ValidCharacters;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * DTO for TopicForum objects. Used to create TopicForums
 */
public class TopicForumDto {

    @NotBlank(message = "{Forum.name.notBlank}")
    @Size(min = 4, message = "{Forum.name.minSize}")
    @ValidCharacters(message = "{Forum.name.validChars}")
    private String name;

    @NotBlank(message = "{Forum.description.notBlank}")
    @Size(min = 1, max = 500, message = "{Forum.description.length}")
    private String description;

    public TopicForumDto() {
    }

    public TopicForumDto(
            @NotBlank(message = "{Forum.name.notBlank}")
            @Size(min = 4, message = "{Forum.name.minSize}")
            @ValidCharacters(message = "{Forum.name.validChars}")
                    String name,
            @NotBlank(message = "{Forum.description.notBlank}")
            @Size(min = 1, max = 500, message = "{Forum.description.length}")
                    String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(@NotBlank(message = "{Forum.name.notBlank}")
                        @Size(min = 4, message = "{Forum.name.minSize}")
                        @ValidCharacters(message = "{Forum.name.validChars}")
                                String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(@NotBlank(message = "{Forum.description.notBlank}")
                               @Size(min = 1, max = 500, message = "{Forum.description.length}")
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
