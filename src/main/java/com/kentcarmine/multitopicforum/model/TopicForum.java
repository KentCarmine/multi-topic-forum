package com.kentcarmine.multitopicforum.model;

import com.kentcarmine.multitopicforum.annotations.ValidCharacters;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity that models a topic forum. That is: a forum for discussion about a specific topic (ie. cars, tabletop gaming,
 * etc).
 */
@Entity
public class TopicForum {

    @Id
    @NotBlank(message = "name must not be blank")
    @Size(min=4, message="forum name must be at least {min} characters long")
    @ValidCharacters(message = "name must consist only of letters, numbers, - and _ characters")
    private String name;

    @Lob
    @NotBlank(message = "description must not be blank")
    @Size(min = 1, max = 500, message = "Description must be between {min} and {max} characters long")
    private String description;

    @OneToMany(mappedBy = "forum", cascade = CascadeType.ALL)
    private List<TopicThread> threads;

    public TopicForum() {
        this.threads = new ArrayList<>();
    }

    public TopicForum(@Size(min=4, message="forum name must be at least {min} characters long") String name, String description) {
        this.name = name;
        this.description = description;
        this.threads = new ArrayList<>();
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

    public List<TopicThread> getThreads() {
        return threads;
    }

    public void setThreads(List<TopicThread> threads) {
        this.threads = threads;
    }

    public void addThread(TopicThread thread) {
        this.threads.add(thread);
    }

    @Override
    public String toString() {
        return "TopicForum{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", threads=" + threads +
                '}';
    }
}
