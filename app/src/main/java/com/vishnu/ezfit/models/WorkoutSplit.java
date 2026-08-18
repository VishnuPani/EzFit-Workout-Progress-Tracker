package com.vishnu.ezfit.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import android.os.Parcel;
import android.os.Parcelable;

public class WorkoutSplit implements Parcelable {
    public static final int TYPE_PREBUILT = 0;
    public static final int TYPE_CUSTOM = 1;

    private long id;
    private String name;
    private int type; // 0=prebuilt, 1=custom
    private Map<String, List<Exercise>> days;
    public WorkoutSplit(long id, String name, int type) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.days = new HashMap<>();
    }

    // Getters and setters
    public long getId() { return id; }
    public String getName() { return name; }
    public int getType() { return type; }
    public Map<String, List<Exercise>> getDays() { return days; }
    public void setDays(Map<String, List<Exercise>> days) { this.days = days; }

@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    WorkoutSplit that = (WorkoutSplit) o;
    return id == that.id &&
            type == that.type &&
            Objects.equals(name, that.name);
}

@Override
public int hashCode() {
    return Objects.hash(id, name, type);
}
    protected WorkoutSplit(Parcel in) {
        id = in.readLong();
        name = in.readString();
        type = in.readInt();
        days = new HashMap<>();
        in.readMap(days, Exercise.class.getClassLoader());
    }

    public static final Creator<WorkoutSplit> CREATOR = new Creator<WorkoutSplit>() {
        @Override
        public WorkoutSplit createFromParcel(Parcel in) {
            return new WorkoutSplit(in);
        }

        @Override
        public WorkoutSplit[] newArray(int size) {
            return new WorkoutSplit[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeInt(type);
        dest.writeMap(days);
    }
}