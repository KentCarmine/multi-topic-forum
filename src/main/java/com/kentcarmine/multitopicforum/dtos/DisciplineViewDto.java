package com.kentcarmine.multitopicforum.dtos;

import com.kentcarmine.multitopicforum.model.DisciplineType;

import java.util.Date;

/**
 * DTO that contains data about a given Discipline, including if the currently logged in user can rescind that discipline.
 */
public class DisciplineViewDto {

    private Long id;
    private String disciplinedUsername;
    private String discipliningUsername;
    private DisciplineType disciplineType;
    private String disciplinedAt;
    private String disciplinedUntil;
    private String disciplineDuration;
    private String reason;
    private boolean rescinded;
    private boolean canRescind;

    public DisciplineViewDto() {
    }

    public DisciplineViewDto(Long id, String disciplinedUsername, String discipliningUsername, DisciplineType disciplineType, Date disciplinedAt, Date disciplinedUntil, Integer disciplineDuration, String reason, boolean rescinded) {
        this.id = id;
        this.disciplinedUsername = disciplinedUsername;
        this.discipliningUsername = discipliningUsername;
        this.disciplineType = disciplineType;
        this.reason = reason;
        this.rescinded = rescinded;

        this.disciplinedAt = disciplinedAt.toString();

        if (disciplineType.equals(DisciplineType.BAN)) {
            this.disciplineDuration = "Permanent";
            this.disciplinedUntil = "N/A";
        } else {
            this.disciplineDuration = disciplineDuration.toString();
            this.disciplinedUntil = disciplinedUntil.toString();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDisciplinedUsername() {
        return disciplinedUsername;
    }

    public void setDisciplinedUsername(String disciplinedUsername) {
        this.disciplinedUsername = disciplinedUsername;
    }

    public String getDiscipliningUsername() {
        return discipliningUsername;
    }

    public void setDiscipliningUsername(String discipliningUsername) {
        this.discipliningUsername = discipliningUsername;
    }

    public DisciplineType getDisciplineType() {
        return disciplineType;
    }

    public void setDisciplineType(DisciplineType discipineType) {
        this.disciplineType = discipineType;
    }

    public String getViewableDisciplineType() {
        return disciplineType.getDisplayValue();
    }

    public String getDisciplinedAt() {
        return disciplinedAt;
    }

    public void setDisciplinedAt(String disciplinedAt) {
        this.disciplinedAt = disciplinedAt;
    }

    public String getDisciplinedUntil() {
        return disciplinedUntil;
    }

    public void setDisciplinedUntil(String disciplinedUntil) {
        this.disciplinedUntil = disciplinedUntil;
    }

    public String getDisciplineDuration() {
        return disciplineDuration;
    }

    public void setDisciplineDuration(Integer disciplineDuration) {
        this.disciplineDuration = disciplineDuration.toString();
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isRescinded() {
        return rescinded;
    }

    public void setRescinded(boolean rescinded) {
        this.rescinded = rescinded;
    }

    public boolean canRescind() {
        return canRescind;
    }

    public void setCanRescind(boolean canRescind) {
        this.canRescind = canRescind;
    }

    public boolean isBan() {
        return disciplineType == DisciplineType.BAN;
    }

    public boolean isSuspension() {
        return disciplineType == DisciplineType.SUSPENSION;
    }

    @Override
    public String toString() {
        return "DisciplineViewDto{" +
                "id=" + id +
                ", disciplinedUsername='" + disciplinedUsername + '\'' +
                ", discipliningUsername='" + discipliningUsername + '\'' +
                ", disciplineType=" + disciplineType +
                ", disciplinedAt='" + disciplinedAt + '\'' +
                ", disciplinedUntil='" + disciplinedUntil + '\'' +
                ", disciplineDuration='" + disciplineDuration + '\'' +
                ", reason='" + reason + '\'' +
                ", rescinded=" + rescinded +
                ", canRescind=" + canRescind +
                '}';
    }
}
