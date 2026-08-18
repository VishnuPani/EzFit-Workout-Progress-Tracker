package com.vishnu.ezfit.models;

import java.util.Date;
import java.util.Objects;

public class ExerciseHistory {
    private final long id;
    private final Date date;
    private final int weight;
    private final int reps;

    public ExerciseHistory(long id, Date date, int weight, int reps) {
        this.id = id;
        this.date = date;
        this.weight = weight;
        this.reps = reps;
    }

    // Getters
    public long getId() { return id; }
    public Date getDate() { return date; }
    public int getWeight() { return weight; }
    public int getReps() { return reps; }

    // Required for DiffUtil
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExerciseHistory that = (ExerciseHistory) o;
        return id == that.id &&
                weight == that.weight &&
                reps == that.reps &&
                Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, date, weight, reps);
    }
}