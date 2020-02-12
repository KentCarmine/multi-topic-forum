package com.kentcarmine.multitopicforum.model;

/**
 * Represents the type of a Discipline. Either Suspension or Ban.
 */
public enum DisciplineType {
    SUSPENSION("Suspension"),
    BAN("Ban");

    private String displayValue;

    DisciplineType(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public void setDisplayValue(String displayValue) {
        this.displayValue = displayValue;
    }

    @Override
    public String toString() {
        return "DisciplineType{" +
                "displayValue='" + displayValue + '\'' +
                '}';
    }
}
