package com.vishnu.ezfit.database;

import android.provider.BaseColumns;

public final class WorkoutSplitContract {
    // Prevent instantiation
    private WorkoutSplitContract() {}

    /* Workout Splits Table */
    public static class SplitEntry implements BaseColumns {
        public static final String TABLE_NAME = "workout_splits";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_TYPE = "type"; // 0=prebuilt, 1=custom
    }

    /* Exercises Table */
    public static class ExerciseEntry implements BaseColumns {
        public static final String TABLE_NAME = "exercises";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_MUSCLE_GROUP = "muscle_group";
        public static final String COLUMN_EQUIPMENT = "equipment";
    }

    /* Split-Exercise Relationship Table */

    public static class SplitExerciseEntry implements BaseColumns {
        public static final String TABLE_NAME = "split_exercises";
        public static final String COLUMN_SPLIT_ID = "split_id";
        public static final String COLUMN_EXERCISE_ID = "exercise_id";
        public static final String COLUMN_DAY = "day";
        public static final String COLUMN_SETS = "sets";
        public static final String COLUMN_REPS = "reps";
        public static final String COLUMN_WEIGHT = "weight";
        public static final String COLUMN_SET_NUMBER = "set_number";
        public static final String COLUMN_LOGGED = "logged";

        public static final String CREATE_TABLE_SQL_V2 =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY," +
                        COLUMN_SPLIT_ID + " INTEGER," +
                        COLUMN_EXERCISE_ID + " INTEGER," +
                        COLUMN_DAY + " TEXT NOT NULL," +
                        COLUMN_SETS + " INTEGER DEFAULT 3," +
                        COLUMN_REPS + " INTEGER DEFAULT 12," +
                        COLUMN_WEIGHT + " INTEGER DEFAULT 0," +
                        COLUMN_SET_NUMBER + " INTEGER,"+
                        COLUMN_LOGGED + " INTEGER DEFAULT 0)";
    }
    /* Exercise History Table */
    public static class HistoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "exercise_history";
        public static final String COLUMN_EXERCISE_ID = "exercise_id";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_SET_NUMBER = "set_number"; // Added
        public static final String COLUMN_WEIGHT = "weight";
        public static final String COLUMN_REPS = "reps";
    }
}
