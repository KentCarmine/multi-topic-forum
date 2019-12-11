package com.kentcarmine.multitopicforum.model;

import com.kentcarmine.multitopicforum.annotations.ValidCharacters;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class TopicThread {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "title must not be blank")
    @Size(min=4, message="thread title must be at least {min} characters long")
    @ValidCharacters(message = "thread title must consist only of letters, numbers, - and _ characters")
    private String title;

    @ManyToOne
    @JoinColumn(name = "forumName")
    @NotNull
    private TopicForum forum;

    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Post> posts;

    // TODO: Add methods to get creation date and creating user of thread (by getting those values from first post)

    public TopicThread() {
        this.posts = new ArrayList<>();
    }

    public TopicThread(String title, TopicForum forum) {
        this.title = title;
        this.forum = forum;
        this.posts = new ArrayList<>();
    }

    public TopicThread(String title, TopicForum forum, Post firstPost) {
        this.title = title;
        this.forum = forum;
        this.posts = new ArrayList<>();
        this.posts.add(firstPost);
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

    public List<Post> getPosts() {
        return posts; // TODO: Sort before returning?
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
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
