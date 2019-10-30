package com.kentcarmine.multitopicforum.model;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private String content;

//    private User user; // TODO: Wire up (many Posts - 1 User)

    private LocalDate postedAt; // TODO: Configure time zone defaults

    // TODO: Set up upvote/downvote management (unique per user+post, table with composite PK and vote value 1, 0, or -1)

    public Post(String content, LocalDate postedAt) {
        this.content = content;
        this.postedAt = postedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDate getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(LocalDate postedAt) {
        this.postedAt = postedAt;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", postedAt=" + postedAt +
                '}';
    }
}
