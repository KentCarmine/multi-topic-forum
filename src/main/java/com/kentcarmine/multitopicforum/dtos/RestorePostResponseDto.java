package com.kentcarmine.multitopicforum.dtos;

/**
 * DTO modeling the response to a restore post request sent from the server to the client.
 */
public class RestorePostResponseDto {
    private String message;
    private Long postId;
    private String reloadUrl;

    public RestorePostResponseDto() {
    }

    public RestorePostResponseDto(String message, Long postId) {
        this.message = message;
        this.postId = postId;
        this.reloadUrl = null;
    }

    public RestorePostResponseDto(String message, Long postId, String reloadUrl) {
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
        return "RestorePostResponseDto{" +
                "message='" + message + '\'' +
                ", postId=" + postId +
                ", reloadUrl='" + reloadUrl + '\'' +
                '}';
    }
}
