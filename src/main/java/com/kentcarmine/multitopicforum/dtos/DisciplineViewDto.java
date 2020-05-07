package com.kentcarmine.multitopicforum.dtos;

import com.kentcarmine.multitopicforum.model.DisciplineType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * DTO that contains data about a given Discipline, including if the currently logged in user can rescind that discipline.
 */
public class DisciplineViewDto {
    private static final String DATE_TIME_FORMAT_STRING = "MM-dd-yyyy, HH:mm z";

    private Long id;
    private String disciplinedUsername;
    private String discipliningUsername;
    private DisciplineType disciplineType;
    private Date disciplinedAt;
    private String disciplinedAtString;
    private Date disciplinedUntil;
    private String disciplinedUntilString;
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

        this.disciplinedAt = disciplinedAt;
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT_STRING);
        this.disciplinedAtString = sdf.format(disciplinedAt);

        if (disciplineType.equals(DisciplineType.BAN)) {
            this.disciplineDuration = "Permanent";
            this.disciplinedUntilString = "N/A";
        } else {
            this.disciplineDuration = disciplineDuration.toString();
            this.disciplinedUntilString = sdf.format(disciplinedUntil);
            this.disciplinedUntil = disciplinedUntil;
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

    public Date getDisciplinedAt() {
        return disciplinedAt;
    }

    public void setDisciplinedAt(Date disciplinedAt) {
        this.disciplinedAt = disciplinedAt;
    }

    public String getDisciplinedAtString() {
        return disciplinedAtString;
    }

    public void setDisciplinedAtString(String disciplinedAtString) {
        this.disciplinedAtString = disciplinedAtString;
    }

    public String getDisciplinedUntilString() {
        return disciplinedUntilString;
    }

    public void setDisciplinedUntilString(String disciplinedUntilString) {
        this.disciplinedUntilString = disciplinedUntilString;
    }

    public Date getDisciplinedUntil() {
        return disciplinedUntil;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DisciplineViewDto that = (DisciplineViewDto) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "DisciplineViewDto{" +
                "id=" + id +
                ", disciplinedUsername='" + disciplinedUsername + '\'' +
                ", discipliningUsername='" + discipliningUsername + '\'' +
                ", disciplineType=" + disciplineType +
                ", disciplinedAt='" + disciplinedAt + '\'' +
                ", disciplinedUntil='" + disciplinedUntilString + '\'' +
                ", disciplineDuration='" + disciplineDuration + '\'' +
                ", reason='" + reason + '\'' +
                ", rescinded=" + rescinded +
                ", canRescind=" + canRescind +
                '}';
    }
}
