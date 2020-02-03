package com.kentcarmine.multitopicforum.dtos;

/**
 * DTO modeling data sent by the client to restore a given post.
 */
public class RestorePostSubmissionDto {
    private Long postId;

    public RestorePostSubmissionDto() {
    }

    public RestorePostSubmissionDto(Long postId) {
        this.postId = postId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    @Override
    public String toString() {
        return "RestorePostSubmissionDto{" +
                "postId=" + postId +
                '}';
    }
}
