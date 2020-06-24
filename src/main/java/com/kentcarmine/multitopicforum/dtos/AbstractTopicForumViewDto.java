package com.kentcarmine.multitopicforum.dtos;

import com.kentcarmine.multitopicforum.annotations.ValidCharacters;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public abstract class AbstractTopicForumViewDto {

    @NotBlank(message = "{Forum.name.notBlank}")
    @Size(min=4, message="{Forum.name.minSize}")
    @ValidCharacters(message = "{Forum.name.validChars}")
    private String name;

    @NotBlank(message = "{Forum.description.notBlank}")
    @Size(min = 1, max = 500, message = "{Forum.description.length}")
    private String description;

    private String updateTimeDifferenceMessage;

    public AbstractTopicForumViewDto() {
    }

    public AbstractTopicForumViewDto(@Size(min=4, message="{Forum.name.minSize}") String name, String description) {
        this.name = name;
        this.description = description;
    }

    public AbstractTopicForumViewDto(@Size(min=4, message="{Forum.name.minSize}") String name, String description,
                                     String updateTimeDifferenceMessage) {
        this.name = name;
        this.description = description;
        this.updateTimeDifferenceMessage = updateTimeDifferenceMessage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUpdateTimeDifferenceMessage() {
        return updateTimeDifferenceMessage;
    }

    public void setUpdateTimeDifferenceMessage(String updateTimeDifferenceMessage) {
        this.updateTimeDifferenceMessage = updateTimeDifferenceMessage;
    }

    public abstract int getNumThreads();

    public abstract boolean hasThreads();

    public abstract PostViewDto getMostRecentPost();

}
