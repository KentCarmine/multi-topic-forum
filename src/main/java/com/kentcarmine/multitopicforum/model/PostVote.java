package com.kentcarmine.multitopicforum.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Entity for modeling upvotes and downvotes on posts.
 */
@Entity
@Table(name = "post_vote", uniqueConstraints = @UniqueConstraint(columnNames = {"username", "post_id"}))
public class PostVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(value = EnumType.STRING)
    private PostVoteState postVoteState;

    @ManyToOne
    @JoinColumn(name = "username")
    @NotNull
    private User user;

    @ManyToOne
    @JoinColumn(name = "post_id")
    @NotNull
    private Post post;

    public PostVote() {
        postVoteState = PostVoteState.NONE;
    }

    public PostVote(@NotNull PostVoteState postVoteState, @NotNull User user, @NotNull Post post) {
        this.postVoteState = postVoteState;
        this.user = user;
        this.post = post;
    }

    public PostVoteState getPostVoteState() {
        return postVoteState;
    }

    public void setPostVoteState(PostVoteState postVoteState) {
        this.postVoteState = postVoteState;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public boolean isUpvote() {
        return postVoteState.getValue() == PostVoteState.UPVOTE.getValue();
    }

    public boolean isDownvote() {
        return postVoteState.getValue() == PostVoteState.DOWNVOTE.getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostVote postVote = (PostVote) o;
        return Objects.equals(id, postVote.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PostVote{" +
                "id=" + id +
                ", postVoteState=" + postVoteState +
                ", user=" + user +
                ", post=" + post +
                '}';
    }
}
