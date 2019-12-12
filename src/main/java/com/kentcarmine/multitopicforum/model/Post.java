package com.kentcarmine.multitopicforum.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Date;

@Entity
public class Post implements Comparable<Post> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "thread_id")
    @NotNull
    private TopicThread thread;

    @Lob
    private String content;

    @ManyToOne
    @JoinColumn(name = "username")
    @NotNull
    private User user;

    private Date postedAt;

    // TODO: Set up upvote/downvote management (unique per user+post, table with composite PK and vote value 1, 0, or -1)

    public Post() {

    }

    public Post(String content, Date postedAt) {
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

    public Date getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(Date postedAt) {
        this.postedAt = postedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public TopicThread getThread() {
        return thread;
    }

    public void setThread(TopicThread thread) {
        this.thread = thread;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Post)) {
            return false;
        }

        return this.getId() == ((Post)(obj)).getId();
    }

    @Override
    public int compareTo(Post o) {
        if (this.getPostedAt().getTime() > o.getPostedAt().getTime()) {
            return 1;
        } else if (this.getPostedAt().getTime() < o.getPostedAt().getTime()) {
            return -1;
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", forum=" + thread.getForum().getName() +
                ", thread=" + thread.getTitle() +
                ", content='" + content + '\'' +
                ", user=" + user.getUsername() +
                ", postedAt=" + postedAt +
                '}';
    }
}
