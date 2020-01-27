package com.kentcarmine.multitopicforum.dtos;

/**
 * DTO modeling data sent by the client to delete a given post.
 */
public class DeletePostSubmissionDto {
    private Long postId;

    public DeletePostSubmissionDto() {
    }

    public DeletePostSubmissionDto(Long postId) {
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
        return "DeletePostSubmissionDto{" +
                "postId=" + postId +
                '}';
    }
}
