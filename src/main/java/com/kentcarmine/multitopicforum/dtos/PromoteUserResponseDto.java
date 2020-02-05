package com.kentcarmine.multitopicforum.dtos;

/**
 * DTO representing a response from the server to a user promotion request.
 */
public class PromoteUserResponseDto {
    private String message;
    private String newPromoteButtonUrl;
    private String newDemoteButtonUrl;

    public PromoteUserResponseDto() {
    }

    public PromoteUserResponseDto(String message) {
        this.message = message;
        newPromoteButtonUrl = null;
        newDemoteButtonUrl = null;
    }

    public PromoteUserResponseDto(String message, String newPromoteButtonUrl, String newDemoteButtonUrl) {
        this.message = message;
        this.newPromoteButtonUrl = newPromoteButtonUrl;
        this.newDemoteButtonUrl = newDemoteButtonUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNewPromoteButtonUrl() {
        return newPromoteButtonUrl;
    }

    public void setNewPromoteButtonUrl(String newPromoteButtonUrl) {
        this.newPromoteButtonUrl = newPromoteButtonUrl;
    }

    public String getNewDemoteButtonUrl() {
        return newDemoteButtonUrl;
    }

    public void setNewDemoteButtonUrl(String newDemoteButtonUrl) {
        this.newDemoteButtonUrl = newDemoteButtonUrl;
    }

    @Override
    public String toString() {
        return "PromoteUserResponseDto{" +
                "message='" + message + '\'' +
                ", newPromoteButtonUrl='" + newPromoteButtonUrl + '\'' +
                ", newDemoteButtonUrl='" + newDemoteButtonUrl + '\'' +
                '}';
    }
}
