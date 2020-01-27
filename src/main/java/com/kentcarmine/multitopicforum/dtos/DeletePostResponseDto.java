package com.kentcarmine.multitopicforum.dtos;

/**
 * DTO modeling the response to a delete request sent from the server to the client.
 */
public class DeletePostResponseDto {
    private String message;
    private Long postId;
    private String reloadUrl;

    public DeletePostResponseDto() {
    }

    public DeletePostResponseDto(String message, Long postId) {
        this.message = message;
        this.postId = postId;
        this.reloadUrl = null;
    }

    public DeletePostResponseDto(String message, Long postId, String reloadUrl) {
        this.message = message;
        this.postId = postId;
        this.reloadUrl = reloadUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getReloadUrl() {
        return reloadUrl;
    }

    public void setReloadUrl(String reloadUrl) {
        this.reloadUrl = reloadUrl;
    }

    @Override
    public String toString() {
        return "DeletePostResponseDto{" +
                "message='" + message + '\'' +
                ", postId=" + postId +
                ", reloadUrl='" + reloadUrl + '\'' +
                '}';
    }
}
