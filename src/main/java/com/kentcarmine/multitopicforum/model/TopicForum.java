package com.kentcarmine.multitopicforum.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Entity that models a topic forum. That is: a forum for discussion about a specific topic (ie. cars, tabletop gaming,
 * etc).
 */
@Entity
public class TopicForum {

    @Id
    @Size(min=4, message="forum name must be at least {min} characters long")
    private String name;

    @Size(min = 1, max = 500, message = "Description must be between {min} and {max} characters long")
    @NotBlank(message = "description must not be blank")
    @Lob
    private String description;

//    private List<TopicThread> threads; // TODO: Wire up (1 TopicForum - many TopicThreads)

    public TopicForum() {
    }

    public TopicForum(@Size(min=4, message="forum name must be at least {min} characters long") String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(@Size(min=4, message="forum name must be at least {min} characters long") String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "TopicForum{" +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
