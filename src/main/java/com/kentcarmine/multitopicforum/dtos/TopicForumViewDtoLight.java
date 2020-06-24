package com.kentcarmine.multitopicforum.dtos;

import javax.validation.constraints.Size;

public class TopicForumViewDtoLight extends AbstractTopicForumViewDto {
//    @NotBlank(message = "{Forum.name.notBlank}")
//    @Size(min=4, message="{Forum.name.minSize}")
//    @ValidCharacters(message = "{Forum.name.validChars}")
//    private String name;
//
//    @NotBlank(message = "{Forum.description.notBlank}")
//    @Size(min = 1, max = 500, message = "{Forum.description.length}")
//    private String description;
//
//    private String updateTimeDifferenceMessage;

    private PostViewDto mostRecentPost;

    private int numThreads;

    public TopicForumViewDtoLight() {

    }

    public TopicForumViewDtoLight(@Size(min=4, message="{Forum.name.minSize}") String name, String description,
                                  int numThreads, PostViewDto mostRecentPost) {
        super(name, description);
        this.numThreads = numThreads;
        this.mostRecentPost = mostRecentPost;
    }

    @Override
    public int getNumThreads() {
        return numThreads;
    }

    @Override
    public boolean hasThreads() {
        return numThreads > 0;
    }

    @Override
    public PostViewDto getMostRecentPost() {
        if (!hasThreads()) {
            return null;
        }

        return mostRecentPost;
    }

    @Override
    public String toString() {
        return "TopicForumViewDtoLight{" +
                "name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", updateTimeDifferenceMessage='" + getUpdateTimeDifferenceMessage() + '\'' +
                '}';
    }
}
