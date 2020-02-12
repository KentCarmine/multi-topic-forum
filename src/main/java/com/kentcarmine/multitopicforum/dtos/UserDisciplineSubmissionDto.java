package com.kentcarmine.multitopicforum.dtos;

import com.kentcarmine.multitopicforum.annotations.ValidUserDisciplineSubmission;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * DTO that handles transferring data about user disciplinary actions
 */
@ValidUserDisciplineSubmission
public class UserDisciplineSubmissionDto {
    private static final int HOURS_IN_30_DAYS = 30 * 24;

    private static final int MIN_SUSPENSION_HOURS = 1;
    private static final int MAX_SUSPENSION_HOURS = HOURS_IN_30_DAYS;

    @NotBlank
    @NotNull
    private String disciplinedUsername;

    private String disciplineType;

    @Min(value = MIN_SUSPENSION_HOURS)
    @Max(value = MAX_SUSPENSION_HOURS)
    private int suspensionHours;

    @NotBlank
    @NotNull
    private String reason;


    public UserDisciplineSubmissionDto() {
    }

    public UserDisciplineSubmissionDto(@NotBlank @NotNull String disciplinedUsername, String disciplineType,
                                       @Min(value = MIN_SUSPENSION_HOURS) @Max(value = MAX_SUSPENSION_HOURS) int suspensionHours,
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

    public int getSuspensionHours() {
        return suspensionHours;
    }

    public void setSuspensionHours(int suspensionHours) {
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
