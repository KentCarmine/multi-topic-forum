package com.kentcarmine.multitopicforum.model;

import com.kentcarmine.multitopicforum.annotations.ValidDiscipline;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.Date;

/**
 * Entity representing user suspensions or bans
 */
@Entity
@ValidDiscipline
//@Table(name = "discipline", uniqueConstraints = @UniqueConstraint(columnNames = "disciplined_user_username"))
public class Discipline {
    private static final int SECONDS_PER_HOUR = 60 * 60;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "disciplined_username", nullable = false)
    private User disciplinedUser;

    @ManyToOne(optional = false)
    @JoinColumn(name = "disciplining_username")
    @NotNull
    private User discipliningUser;

    @NotNull
    @Enumerated(value = EnumType.STRING)
    private DisciplineType disciplineType;

    @NotNull
    private Date disciplinedAt;

    private Integer disciplineDurationHours;

    @NotBlank(message = "Reason must not be blank")
    @Size(min = 2, max = 500, message = "Reason must be between 2 and 500 characters")
    private String reason;

    private boolean rescinded;

    public Discipline() {
    }

    public Discipline(@NotNull User disciplinedUser, @NotNull User discipliningUser, @NotNull Date disciplinedAt, String reason) {
        this.disciplinedUser = disciplinedUser;
        this.discipliningUser = discipliningUser;
        this.disciplinedAt = disciplinedAt;
        this.disciplineDurationHours = null;
        this.reason = reason;
        this.rescinded = false;
    }

    public Discipline(@NotNull User disciplinedUser, @NotNull User discipliningUser, @NotNull DisciplineType disciplineType, @NotNull Date disciplinedAt, String reason) {
        this.disciplinedUser = disciplinedUser;
        this.discipliningUser = discipliningUser;
        this.disciplineType = disciplineType;
        this.disciplinedAt = disciplinedAt;
        this.disciplineDurationHours = null;
        this.reason = reason;
        this.rescinded = false;
    }

    public Discipline(@NotNull User disciplinedUser, @NotNull User discipliningUser, @NotNull DisciplineType disciplineType, @NotNull Date disciplinedAt, Integer disciplineDuration, String reason) {
        this.disciplinedUser = disciplinedUser;
        this.discipliningUser = discipliningUser;
        this.disciplineType = disciplineType;
        this.disciplinedAt = disciplinedAt;
        this.disciplineDurationHours = disciplineDuration;
        this.reason = reason;
        this.rescinded = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getDisciplinedUser() {
        return disciplinedUser;
    }

    public void setDisciplinedUser(User disciplinedUser) {
        this.disciplinedUser = disciplinedUser;
    }

    public User getDiscipliningUser() {
        return discipliningUser;
    }

    public void setDiscipliningUser(User discipliningUser) {
        this.discipliningUser = discipliningUser;
    }

    public DisciplineType getDisciplineType() {
        return disciplineType;
    }

    public void setDisciplineType(DisciplineType disciplineType) {
        this.disciplineType = disciplineType;
    }

    public Date getDisciplinedAt() {
        return disciplinedAt;
    }

    public void setDisciplinedAt(Date disciplinedAt) {
        this.disciplinedAt = disciplinedAt;
    }

    public Integer getDisciplineDurationHours() {
        return disciplineDurationHours;
    }

    public void setDisciplineDurationHours(Integer disciplineDurationHours) {
        this.disciplineDurationHours = disciplineDurationHours;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Date getDisciplineEndTime() {
        Date endDate = null;

        if (!disciplineType.equals(DisciplineType.BAN)) {
            endDate = Date.from(disciplinedAt.toInstant().plusSeconds(disciplineDurationHours.intValue() * SECONDS_PER_HOUR));
        }

        return endDate;
    }

    public boolean isOver() {
        if (isRescinded()) {
            return true;
        }

        if (disciplineType.equals(DisciplineType.BAN) || getDisciplineEndTime() == null) {
            return false;
        }

        return Date.from(Instant.now()).after(getDisciplineEndTime());
    }

    public boolean isActive() {
        return !isOver();
    }

    public boolean isBan() {
        return disciplineType.equals(DisciplineType.BAN);
    }

    public boolean isSuspension() {
        return disciplineType.equals(DisciplineType.SUSPENSION);
    }

    public boolean isRescinded() {
        return rescinded;
    }

    public void setRescinded(boolean rescinded) {
        this.rescinded = rescinded;
    }

    @Override
    public String toString() {
        String disciplinedUsername = "NULL";
        String discipliningUsername = "NULL";

        if (disciplinedUser != null) {
            disciplinedUsername = disciplinedUser.getUsername();
        }

        if (discipliningUser != null) {
            discipliningUsername = discipliningUser.getUsername();
        }

        return "Discipline{" +
                "disciplinedUser=" + disciplinedUsername +
                ", discipliningUser=" + discipliningUsername +
                ", disciplineType=" + disciplineType +
                ", disciplinedAt=" + disciplinedAt +
                ", disciplineDuration=" + disciplineDurationHours +
                '}';
    }
}
