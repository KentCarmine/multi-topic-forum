package com.kentcarmine.multitopicforum.dtos;

/**
 * DTO representing a response from the server to a user demotion request.
 */
public class DemoteUserResponseDto {
    private String message;
    private String newPromoteButtonUrl;
    private String newDemoteButtonUrl;

    public DemoteUserResponseDto() {
    }

    public DemoteUserResponseDto(String message) {
        this.message = message;
    }

    public DemoteUserResponseDto(String message, String newPromoteButtonUrl, String newDemoteButtonUrl) {
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
        return "DemoteUserResponseDto{" +
                "message='" + message + '\'' +
                ", newPromoteButtonUrl='" + newPromoteButtonUrl + '\'' +
                ", newDemoteButtonUrl='" + newDemoteButtonUrl + '\'' +
                '}';
    }
}
