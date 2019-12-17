package com.kentcarmine.multitopicforum.model;

import org.hibernate.annotations.SortComparator;
import org.hibernate.annotations.SortNatural;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.*;

/**
 * Entity that models a topic thread, that is, a thread of one or more posts within a given topic forum.
 */
@Entity
public class TopicThread {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "title must not be blank")
    @Size(min=4, message="thread title must be at least {min} characters long")
    private String title;

    @ManyToOne
    @JoinColumn(name = "forumName")
    @NotNull
    private TopicForum forum;

    @SortNatural
    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private SortedSet<Post> posts;

    // TODO: Add methods to get creation date and creating user of thread (by getting those values from first post)

    public TopicThread() {
        this.posts = new TreeSet<>();
    }

    public TopicThread(String title, TopicForum forum) {
        this.title = title;
        this.forum = forum;
        this.posts = new TreeSet<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public SortedSet<Post> getPosts() {
        return posts;
    }

    public void setPosts(SortedSet<Post> posts) {
        this.posts = posts;
    }

    public Post getFirstPost() {
        return getPosts().first();
    }

    public Post getLastPost() {
        return getPosts().last();
    }

    public TopicForum getForum() {
        return forum;
    }

    @Override
    public String toString() {
        return "TopicThread{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", posts=" + posts +
                '}';
    }
}
