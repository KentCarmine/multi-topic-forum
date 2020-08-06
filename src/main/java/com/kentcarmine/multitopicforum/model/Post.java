package com.kentcarmine.multitopicforum.model;

import org.springframework.beans.factory.annotation.Value;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Entity that models a single forum Post within a TopicThread.
 */
@Entity
public class Post implements Comparable<Post>, PostUpdatedTimable {
    private static final String DATE_TIME_FORMAT_STRING = "MM-dd-yyyy, HH:mm";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "thread_id")
    @NotNull
    private TopicThread thread;

//    @Lob
    @Column(columnDefinition = "text")
    @NotBlank(message = "{Post.content.notBlank}")
    private String content;

    @ManyToOne
    @JoinColumn(name = "username")
    @NotNull
    private User user;

    private Date postedAt;

    private boolean deleted;
    private Date deletedAt;

    @ManyToOne
    @JoinColumn(name = "deleted_by_username")
    private User deletedBy;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<PostVote> postVotes;

    public Post() {
        postVotes = new HashSet<>();
        deleted = false;
    }

    public Post(String content, Date postedAt) {
        this.content = content;
        this.postedAt = postedAt;
        this.postVotes = new HashSet<>();
        deleted = false;
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

    public String getDisplayPostedAt() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT_STRING);

        return sdf.format(postedAt);
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

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Date getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Date deletedAt) {
        this.deletedAt = deletedAt;
    }

    public User getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(User deletedBy) {
        this.deletedBy = deletedBy;
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

    public boolean isDeletableBy(User loggedInUser) {
        if (loggedInUser == null) {
            return false;
        }

        return loggedInUser.isHigherAuthority(this.getUser());
    }

    /**
     * Determines if this post can be restored from deletion by the given user.
     *
     * @param loggedInUser the user to check restoration authority of
     * @return true if the given user can restore this post or if it has already been restored, false otherwise
     */
    public boolean isRestorableBy(User loggedInUser) {
        if (loggedInUser == null) {
            return false;
        }

        if (!this.isDeleted() || this.getDeletedBy() == null) {
            return true;
        }

        if (this.getDeletedBy().equals(loggedInUser) || loggedInUser.isHigherAuthority(this.getDeletedBy())) {
            return true;
        }

        return false;
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
        String forumName = null;
        String threadTitle = null;
        if (Objects.nonNull(thread)) {
            threadTitle = thread.getTitle();
            if (Objects.nonNull(thread.getForum())) {
                forumName = thread.getForum().getName();
            }
        }

        String username = null;
        if (Objects.nonNull(user)) {
            username = user.getUsername();
        }

        String deletedName = null;
        if (Objects.nonNull(deletedBy)) {
            deletedName = deletedBy.getUsername();
        }

        String deletedAtStr = null;
        if (Objects.nonNull(deletedAt)) {
            deletedAtStr = deletedAt.toString();
        }

        return "Post{" +
                "id=" + id +
                ", forum=" + forumName +
                ", thread=" + threadTitle +
                ", content='" + content + '\'' +
                ", user=" + username +
                ", postedAt=" + postedAt +
                ", deleted=" + deleted +
                ", deletedAt=" + deletedAtStr +
                ", deltedBy=" + deletedName +
                '}';
    }

    public String getDisplayablePostedAt() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm").withLocale(Locale.getDefault())
                .withZone(ZoneId.systemDefault());
        return dtf.format(postedAt.toInstant());
    }
}
