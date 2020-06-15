package com.kentcarmine.multitopicforum.dtos;

import com.kentcarmine.multitopicforum.model.PostUpdatedTimable;
import com.kentcarmine.multitopicforum.model.PostVote;
import com.kentcarmine.multitopicforum.model.User;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class PostViewDto implements Comparable<PostViewDto>, PostUpdatedTimable {
    private static final int ABBREVIATED_CONTENT_LENGTH = 50; // TODO: Move into properties file

    private Long id;

    @NotNull
    private AbstractTopicThreadViewDto thread;

    @NotBlank(message = "{Post.content.notBlank}")
    private String content;

    @NotNull
    private User user;

    private Date postedAt;

    private boolean deleted;
    private Date deletedAt;
    private User deletedBy;

    private Set<PostVote> postVotes;

    private String creationTimeDifferenceMessage;

    public PostViewDto() {
        postVotes = new HashSet<>();
        deleted = false;
    }

    public PostViewDto(String content, Date postedAt) {
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

    public AbstractTopicThreadViewDto getThread() {
        return thread;
    }

    public void setThread(AbstractTopicThreadViewDto thread) {
        this.thread = thread;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(Date postedAt) {
        this.postedAt = postedAt;
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

    public Set<PostVote> getPostVotes() {
        return postVotes;
    }

    public void setPostVotes(Set<PostVote> postVotes) {
        this.postVotes = postVotes;
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
        if (!(obj instanceof PostViewDto)) {
            return false;
        }

        return this.getId() == ((PostViewDto)(obj)).getId();
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

    public String getCreationTimeDifferenceMessage() {
        return creationTimeDifferenceMessage;
    }

    public void setCreationTimeDifferenceMessage(String creationTimeDifferenceMessage) {
        this.creationTimeDifferenceMessage = creationTimeDifferenceMessage;
    }

    @Override
    public int compareTo(PostViewDto o) {
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

        return "PostViewDto{" +
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

}
