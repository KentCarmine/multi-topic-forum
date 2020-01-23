package com.kentcarmine.multitopicforum.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.*;

/**
 * Entity that models a single forum Post within a TopicThread.
 */
@Entity
public class Post implements Comparable<Post> {
    private static final int ABBREVIATED_CONTENT_LENGTH = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "thread_id")
    @NotNull
    private TopicThread thread;

    @NotBlank(message = "Post content must not be blank")
    @Lob
    private String content;

    @ManyToOne
    @JoinColumn(name = "username")
    @NotNull
    private User user;

    private Date postedAt;

    @OneToMany(mappedBy = "post", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<PostVote> postVotes;

    public Post() {
        postVotes = new HashSet<>();
    }

    public Post(String content, Date postedAt) {
        this.content = content;
        this.postedAt = postedAt;
        this.postVotes = new HashSet<>();
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

    public Set<PostVote> getPostVotes() {
        return postVotes;
    }

    public void setPostVotes(Set<PostVote> postVotes) {
        this.postVotes = postVotes;
    }

    public void addPostVote(PostVote postVote) {
        postVotes.add(postVote);
    }

    /**
     * Get the sum of the total vote values for this post.
     *
     * @return the sum of the total vote values for this post.
     */
    public int getVoteCount() {
        int sum = 0;
        for (PostVote vote : getPostVotes()) {
            sum += vote.getPostVoteState().getValue();
        }

        return sum;
    }

    public String getAbbreviatedContent() {
        return getAbbreviatedContent(ABBREVIATED_CONTENT_LENGTH);
    }

    /**
     * Get the first length characters of the content, and append ... if more characters are available.
     *
     * @param length the number of characters of content to get
     * @return the first length characters of the content, possibly appended with ...
     */
    public String getAbbreviatedContent(int length) {
        if (content.length() <= length) {
            return content;
        } else {
            return content.substring(0, length) + "...";
        }
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
