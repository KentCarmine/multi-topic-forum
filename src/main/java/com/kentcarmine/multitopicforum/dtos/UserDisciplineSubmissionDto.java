package com.kentcarmine.multitopicforum.dtos;

import com.kentcarmine.multitopicforum.annotations.ValidUserDisciplineSubmission;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * DTO that handles transferring data about user disciplinary actions
 */
@ValidUserDisciplineSubmission
public class UserDisciplineSubmissionDto {

    @NotBlank
    @NotNull
    private String disciplinedUsername;

    private String disciplineType;

    private String suspensionHours;

    @NotBlank(message = "Reason for disciplinary action must not be blank")
    @NotNull
    private String reason;

    public UserDisciplineSubmissionDto() {
    }

    public UserDisciplineSubmissionDto(@NotBlank @NotNull String disciplinedUsername, String disciplineType,
                                       @NotBlank @NotNull String reason) {
        this.disciplinedUsername = disciplinedUsername;
        this.disciplineType = disciplineType;
        this.suspensionHours = "0";
        this.reason = reason;
    }

    public UserDisciplineSubmissionDto(@NotBlank @NotNull String disciplinedUsername, String disciplineType, String suspensionHours,
                                       @NotBlank @NotNull String reason) {
        this.disciplinedUsername = disciplinedUsername;
        this.disciplineType = disciplineType;
        this.suspensionHours = suspensionHours;
        this.reason = reason;
    }

    public String getDisciplinedUsername() {
        return disciplinedUsername;
    }

    public void setDisciplinedUsername(String disciplinedUsername) {
        this.disciplinedUsername = disciplinedUsername;
    }

    public boolean isBan() {
        return disciplineType.equals("Ban");
    }

    public boolean isSuspension() {
        return disciplineType.equals("Suspension");
    }

    public String getDisciplineType() {
        return disciplineType;
    }

    public void setDisciplineType(String disciplineType) {
        this.disciplineType = disciplineType;
    }

    public String getSuspensionHours() {
        return suspensionHours;
    }

    public void setSuspensionHours(String suspensionHours) {
        this.suspensionHours = suspensionHours;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "UserDisciplineSubmissionDto{" +
                "disciplinedUsername='" + disciplinedUsername + '\'' +
                ", disciplineType='" + disciplineType + '\'' +
                ", suspensionHours=" + suspensionHours +
                ", reason='" + reason + '\'' +
                '}';
    }
}