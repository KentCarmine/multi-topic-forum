package com.kentcarmine.multitopicforum.model;

import com.kentcarmine.multitopicforum.annotations.ValidCharacters;
import com.kentcarmine.multitopicforum.helpers.ThreadUpdateTimeComparator;
import org.hibernate.annotations.SortComparator;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.*;

/**
 * Entity that models a topic forum. That is: a forum for discussion about a specific topic (ie. cars, tabletop gaming,
 * etc).
 */
@Entity
public class TopicForum {

    @Id
    @NotBlank(message = "{Forum.name.notBlank}")
    @Size(min=4, message="{Forum.name.minSize}")
    @ValidCharacters(message = "{Forum.name.validChars}")
    private String name;

    @Lob
    @NotBlank(message = "{Forum.description.notBlank}")
    @Size(min = 1, max = 500, message = "{Forum.description.length}")
    private String description;

//    @OneToMany(mappedBy = "forum", cascade = CascadeType.ALL)
//    private List<TopicThread> threads;

    @SortComparator(ThreadUpdateTimeComparator.class)
    @OneToMany(mappedBy = "forum", cascade = CascadeType.ALL)
    private SortedSet<TopicThread> threads;

//    public TopicForum() {
//        this.threads = new ArrayList<>();
//    }

    public TopicForum() {
//        this.threads = new TreeSet<>(THREAD_COMPARATOR);
        this.threads = new TreeSet<>(new ThreadUpdateTimeComparator());
    }

//    public TopicForum(@Size(min=4, message="{Forum.name.minSize}") String name, String description) {
//        this.name = name;
//        this.description = description;
//        this.threads = new ArrayList<>();
//    }

    public TopicForum(@Size(min=4, message="{Forum.name.minSize}") String name, String description) {
        this.name = name;
        this.description = description;
//        this.threads = new TreeSet<>(THREAD_COMPARATOR);
        this.threads = new TreeSet<>(new ThreadUpdateTimeComparator());

    }

    public String getName() {
        return name;
    }

    public void setName(@Size(min=4, message="{Forum.name.minSize}") String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

//    public List<TopicThread> getThreads() {
//        return threads;
//    }
//
//    public void setThreads(List<TopicThread> threads) {
//        this.threads = threads;
//    }

    public SortedSet<TopicThread> getThreads() {
        return threads;
    }

    public void setThreads(SortedSet<TopicThread> threads) {
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
