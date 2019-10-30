package com.kentcarmine.multitopicforum.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;
import java.util.List;

@Entity
public class TopicThread {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private LocalDate postedAt; // TODO: Configure time zone defaults

//    private List<Post> posts; // TODO: Wire up (1 TopicThread - many Posts)

//    private TopicForum forum; // TODO: Wire up (many TopicThread - 1 TopicForum) (not sure if needed, check)

    public TopicThread(String title, LocalDate postedAt) {
        this.title = title;
        this.postedAt = postedAt;
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

    public LocalDate getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(LocalDate postedAt) {
        this.postedAt = postedAt;
    }

    @Override
    public String toString() {
        return "TopicThread{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", postedAt=" + postedAt +
                '}';
    }
}
