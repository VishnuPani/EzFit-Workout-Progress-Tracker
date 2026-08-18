package com.vishnu.ezfit.models;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Exercise implements Parcelable {
    private long id;
    private String name;
    private String muscleGroup;
    private String equipment;
    private boolean expanded;
    private List<ExerciseSet> sets;

    public Exercise(long id, String name, String muscleGroup, String equipment) {
        this(id, name, muscleGroup, equipment, 3, 12, 0, false);
    }

    public Exercise(long id, String name, String muscleGroup, String equipment,
                    int totalSets, int defaultReps, int defaultWeight, boolean logged) {
        this.id = id;
        this.name = name;
        this.muscleGroup = muscleGroup;
        this.equipment = equipment;
        this.expanded = false;
        this.sets = new ArrayList<>();

        // Initialize with default sets
        for (int i = 0; i < totalSets; i++) {
            sets.add(new ExerciseSet(i + 1, defaultReps, defaultWeight, logged));
        }
    }

    protected Exercise(Parcel in) {
        id = in.readLong();
        name = in.readString();
        muscleGroup = in.readString();
        equipment = in.readString();
        expanded = in.readByte() != 0;
        sets = in.createTypedArrayList(ExerciseSet.CREATOR);
    }

    public static final Creator<Exercise> CREATOR = new Creator<Exercise>() {
        @Override
        public Exercise createFromParcel(Parcel in) {
            return new Exercise(in);
        }

        @Override
        public Exercise[] newArray(int size) {
            return new Exercise[size];
        }
    };

    // Getters and setters
    public long getId() { return id; }
    public String getName() { return name; }
    public String getMuscleGroup() { return muscleGroup; }
    public String getEquipment() { return equipment; }
    public boolean isExpanded() { return expanded; }
    public List<ExerciseSet> getSets() { return sets; }

    public void setExpanded(boolean expanded) { this.expanded = expanded; }
    @Override
    public String toString() {
        return name + " (" + muscleGroup + " - " + equipment + ")";
    }
    public void setSets(List<ExerciseSet> sets) { this.sets = sets; }

    public int getTotalSets() { return sets.size(); }
    public int getCompletedSets() {
        int count = 0;
        for (ExerciseSet set : sets) {
            if (set.isLogged()) {
                count++;
            }
        }
        return count;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(muscleGroup);
        dest.writeString(equipment);
        dest.writeByte((byte) (expanded ? 1 : 0));
        dest.writeTypedList(sets);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Exercise exercise = (Exercise) o;
        return id == exercise.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Nested ExerciseSet class
    public static class ExerciseSet implements Parcelable {
        private int setNumber;
        private int reps;
        private int weight;
        private boolean logged;

        public ExerciseSet(int setNumber, int reps, int weight, boolean logged) {
            this.setNumber = setNumber;
            this.reps = reps;
            this.weight = weight;
            this.logged = logged;
        }

        protected ExerciseSet(Parcel in) {
            setNumber = in.readInt();
            reps = in.readInt();
            weight = in.readInt();
            logged = in.readByte() != 0;
        }

        public static final Creator<ExerciseSet> CREATOR = new Creator<ExerciseSet>() {
            @Override
            public ExerciseSet createFromParcel(Parcel in) {
                return new ExerciseSet(in);
            }

            @Override
            public ExerciseSet[] newArray(int size) {
                return new ExerciseSet[size];
            }
        };

        public int getSetNumber() { return setNumber; }
        public int getReps() { return reps; }
        public int getWeight() { return weight; }
        public boolean isLogged() { return logged; }

        public void setReps(int reps) { this.reps = reps; }
        public void setWeight(int weight) { this.weight = weight; }
        public void setLogged(boolean logged) { this.logged = logged; }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(setNumber);
            dest.writeInt(reps);
            dest.writeInt(weight);
            dest.writeByte((byte) (logged ? 1 : 0));
        }
    }
}