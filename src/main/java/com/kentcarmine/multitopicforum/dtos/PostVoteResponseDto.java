package com.kentcarmine.multitopicforum.dtos;

/**
 * Models data sent when responding to a user's upvote or downvote submission.
 */
public class PostVoteResponseDto {
    private Long postId;
    private boolean hasUpvote;
    private boolean hasDownvote;
    private boolean voteUpdated;
    private int voteTotal;

    public PostVoteResponseDto() {
    }

    public PostVoteResponseDto(Long postId, boolean hasUpvote, boolean hasDownvote, boolean voteUpdated, int voteTotal) {
        this.postId = postId;
        this.hasUpvote = hasUpvote;
        this.hasDownvote = hasDownvote;
        this.voteUpdated = voteUpdated;
        this.voteTotal = voteTotal;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public boolean isHasUpvote() {
        return hasUpvote;
    }

    public void setHasUpvote(boolean hasUpvote) {
        this.hasUpvote = hasUpvote;
    }

    public boolean isHasDownvote() {
        return hasDownvote;
    }

    public void setHasDownvote(boolean hasDownvote) {
        this.hasDownvote = hasDownvote;
    }

    public boolean isVoteUpdated() {
        return voteUpdated;
    }

    public void setVoteUpdated(boolean voteUpdated) {
        this.voteUpdated = voteUpdated;
    }

    public int getVoteTotal() {
        return voteTotal;
    }

    public void setVoteTotal(int voteTotal) {
        this.voteTotal = voteTotal;
    }

    @Override
    public String toString() {
        return "PostVoteResponseDto{" +
                "postId=" + postId +
                ", hasUpvote=" + hasUpvote +
                ", hasDownvote=" + hasDownvote +
                ", voteUpdated=" + voteUpdated +
                ", voteTotal=" + voteTotal +
                '}';
    }
}
