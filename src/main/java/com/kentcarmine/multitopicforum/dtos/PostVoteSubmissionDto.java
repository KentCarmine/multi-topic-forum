package com.kentcarmine.multitopicforum.dtos;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * Models data received when the user submits an upvote or downvote on a post.
 */
public class PostVoteSubmissionDto {
    private Long postId;

    @Min(value = -1, message = "voteValue must be between -1 and 1")
    @Max(value = 1, message = "voteValue must be between -1 and 1")
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
