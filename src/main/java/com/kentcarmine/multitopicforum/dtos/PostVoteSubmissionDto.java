package com.kentcarmine.multitopicforum.dtos;

import com.kentcarmine.multitopicforum.annotations.ValidVoteValue;

/**
 * Models data received when the user submits an upvote or downvote on a post.
 */
public class PostVoteSubmissionDto {
    private Long postId;

    @ValidVoteValue
    private int voteValue;

    public PostVoteSubmissionDto() {
    }

    public PostVoteSubmissionDto(Long postId, int voteValue) {
        this.postId = postId;
        this.voteValue = voteValue;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public int getVoteValue() {
        return voteValue;
    }

    public void setVoteValue(int voteValue) {
        this.voteValue = voteValue;
    }

    @Override
    public String toString() {
        return "PostVoteDto{" +
                "postId=" + postId +
                ", voteValue=" + voteValue +
                '}';
    }
}
