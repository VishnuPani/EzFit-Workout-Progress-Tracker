package com.vishnu.ezfit.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.vishnu.ezfit.models.Exercise;
import com.vishnu.ezfit.models.ExerciseHistory;
import com.vishnu.ezfit.models.WorkoutSplit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import android.util.Log;
import com.vishnu.ezfit.database.WorkoutSplitContract;
import com.vishnu.ezfit.database.WorkoutSplitContract.SplitExerciseEntry;
import com.vishnu.ezfit.database.WorkoutSplitContract.SplitEntry;
import android.database.SQLException;
import com.vishnu.ezfit.database.WorkoutSplitContract.HistoryEntry;
import com.vishnu.ezfit.database.WorkoutSplitContract.ExerciseEntry;
import java.util.*;
public class WorkoutSplitDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "FitnessTracker.db";

    public WorkoutSplitDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
        insertInitialData(db);
    }

    private void createTables(SQLiteDatabase db) {
        // Splits Table
        db.execSQL(
                "CREATE TABLE " + WorkoutSplitContract.SplitEntry.TABLE_NAME + " (" +
                        WorkoutSplitContract.SplitEntry._ID + " INTEGER PRIMARY KEY," +
                        WorkoutSplitContract.SplitEntry.COLUMN_NAME + " TEXT," +
                        WorkoutSplitContract.SplitEntry.COLUMN_TYPE + " INTEGER)"
        );

        // Exercises Table
        db.execSQL(
                "CREATE TABLE " + WorkoutSplitContract.ExerciseEntry.TABLE_NAME + " (" +
                        WorkoutSplitContract.ExerciseEntry._ID + " INTEGER PRIMARY KEY," +
                        WorkoutSplitContract.ExerciseEntry.COLUMN_NAME + " TEXT," +
                        WorkoutSplitContract.ExerciseEntry.COLUMN_MUSCLE_GROUP + " TEXT," +
                        WorkoutSplitContract.ExerciseEntry.COLUMN_EQUIPMENT + " TEXT)"
        );

        // Split-Exercise Table (v2 schema)
        db.execSQL(WorkoutSplitContract.SplitExerciseEntry.CREATE_TABLE_SQL_V2);

        // History Table
        db.execSQL(
                "CREATE TABLE " + WorkoutSplitContract.HistoryEntry.TABLE_NAME + " (" +
                        WorkoutSplitContract.HistoryEntry._ID + " INTEGER PRIMARY KEY," +
                        WorkoutSplitContract.HistoryEntry.COLUMN_EXERCISE_ID + " INTEGER," +
                        WorkoutSplitContract.HistoryEntry.COLUMN_DATE + " INTEGER," +
                        WorkoutSplitContract.HistoryEntry.COLUMN_SET_NUMBER + " INTEGER," +
                        WorkoutSplitContract.HistoryEntry.COLUMN_WEIGHT + " INTEGER," +
                        WorkoutSplitContract.HistoryEntry.COLUMN_REPS + " INTEGER)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Wipe existing data for development
        db.execSQL("DROP TABLE IF EXISTS " + SplitExerciseEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SplitEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ExerciseEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + HistoryEntry.TABLE_NAME);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion); // Handle downgrade same as upgrade
    }

    // ================== Splits Methods ================== //
    public List<WorkoutSplit> getAllSplits() {
        List<WorkoutSplit> splits = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                WorkoutSplitContract.SplitEntry.TABLE_NAME,
                null, null, null, null, null, null
        );

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(
                    WorkoutSplitContract.SplitEntry._ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(
                    WorkoutSplitContract.SplitEntry.COLUMN_NAME));
            int type = cursor.getInt(cursor.getColumnIndexOrThrow(
                    WorkoutSplitContract.SplitEntry.COLUMN_TYPE));

            WorkoutSplit split = new WorkoutSplit(id, name, type);
            split.setDays(getSplitDays(id));
            splits.add(split);
        }
        cursor.close();
        return splits;
    }

    public Map<String, List<Exercise>> getSplitDays(long splitId) {
        Map<String, List<Exercise>> days = new LinkedHashMap<>(); // Preserves insertion order
        SQLiteDatabase db = getReadableDatabase();

        // Modified query with proper ordering
        String query = "SELECT " + SplitExerciseEntry.COLUMN_DAY +
                " FROM " + SplitExerciseEntry.TABLE_NAME +
                " WHERE " + SplitExerciseEntry.COLUMN_SPLIT_ID + " = ?" +
                " GROUP BY " + SplitExerciseEntry.COLUMN_DAY +
                " ORDER BY MIN(" + SplitExerciseEntry._ID + ")"; // Order by first occurrence

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(splitId)});

        while (cursor.moveToNext()) {
            String day = cursor.getString(0);
            days.put(day, getExercisesForDay(splitId, day));
        }
        cursor.close();
        return days;
    }

    // ================== Exercise Methods ================== //
    public List<Exercise> getExercisesForDay(long splitId, String day) {
        if (day == null || day.trim().isEmpty()) {
            Log.e("DB", "Invalid day parameter");
            return new ArrayList<>();
        }
        List<Exercise> exercises = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT e.*, se." + WorkoutSplitContract.SplitExerciseEntry.COLUMN_SETS + ", " +
                "se." + WorkoutSplitContract.SplitExerciseEntry.COLUMN_REPS + ", " +
                "se." + WorkoutSplitContract.SplitExerciseEntry.COLUMN_WEIGHT + ", " +
                "se." + WorkoutSplitContract.SplitExerciseEntry.COLUMN_LOGGED + " " +
                "FROM " + WorkoutSplitContract.ExerciseEntry.TABLE_NAME + " e " +
                "INNER JOIN " + WorkoutSplitContract.SplitExerciseEntry.TABLE_NAME + " se " +
                "ON e." + WorkoutSplitContract.ExerciseEntry._ID + " = se." +
                WorkoutSplitContract.SplitExerciseEntry.COLUMN_EXERCISE_ID + " " +
                "WHERE se." + WorkoutSplitContract.SplitExerciseEntry.COLUMN_SPLIT_ID + " = ? " +
                "AND se." + WorkoutSplitContract.SplitExerciseEntry.COLUMN_DAY + " = ?";

        String[] selectionArgs = new String[]{
                String.valueOf(splitId),
                day // Already validated above
        };

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(splitId), day});

        while (cursor.moveToNext()) {
            exercises.add(new Exercise(
                    cursor.getLong(cursor.getColumnIndexOrThrow(ExerciseEntry._ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_MUSCLE_GROUP)),
                    cursor.getString(cursor.getColumnIndexOrThrow(ExerciseEntry.COLUMN_EQUIPMENT)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(SplitExerciseEntry.COLUMN_SETS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(SplitExerciseEntry.COLUMN_REPS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(SplitExerciseEntry.COLUMN_WEIGHT)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(SplitExerciseEntry.COLUMN_LOGGED)) == 1
            ));
        }
        cursor.close();
        return exercises;
    }

    // ================== getallexercise method ==============//
    // WorkoutSplitDbHelper.java
    public List<Exercise> getAllExercises() {
        List<Exercise> exercises = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(
                WorkoutSplitContract.ExerciseEntry.TABLE_NAME,
                null, null, null, null, null, null
        )) {
            Log.d("DB", "Exercise table has " + cursor.getCount() + " entries");
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(WorkoutSplitContract.ExerciseEntry._ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(WorkoutSplitContract.ExerciseEntry.COLUMN_NAME));
                String muscle = cursor.getString(cursor.getColumnIndexOrThrow(WorkoutSplitContract.ExerciseEntry.COLUMN_MUSCLE_GROUP));
                String equipment = cursor.getString(cursor.getColumnIndexOrThrow(WorkoutSplitContract.ExerciseEntry.COLUMN_EQUIPMENT));

                Log.d("DB", "Found exercise: " + name);

                exercises.add(new Exercise(id, name, muscle, equipment));
            }

        } catch (Exception e) {
            Log.e("Database", "Error getting exercises", e);
        }
        return exercises;
    }

    // ================== progresstracking ================== //
    public void logExerciseSet(long exerciseId, int setNumber, int weight, int reps) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues historyValues = new ContentValues();
        historyValues.put(WorkoutSplitContract.HistoryEntry.COLUMN_EXERCISE_ID, exerciseId);
        historyValues.put(WorkoutSplitContract.HistoryEntry.COLUMN_DATE, System.currentTimeMillis());
        historyValues.put(WorkoutSplitContract.HistoryEntry.COLUMN_SET_NUMBER, setNumber);
        historyValues.put(WorkoutSplitContract.HistoryEntry.COLUMN_WEIGHT, weight);
        historyValues.put(WorkoutSplitContract.HistoryEntry.COLUMN_REPS, reps);
        db.insert(WorkoutSplitContract.HistoryEntry.TABLE_NAME, null, historyValues);

        try
    {
        db.insertOrThrow(HistoryEntry.TABLE_NAME, null, historyValues);
    } catch(
    SQLException e)
    {
        Log.e("Database", "Error logging set: " + e.getMessage());
    }
}
    public List<ExerciseHistory> getExerciseHistory(long exerciseId) {
        List<ExerciseHistory> history = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                HistoryEntry._ID,
                HistoryEntry.COLUMN_DATE,
                HistoryEntry.COLUMN_SET_NUMBER,
                HistoryEntry.COLUMN_WEIGHT,
                HistoryEntry.COLUMN_REPS
        };

        String selection = HistoryEntry.COLUMN_EXERCISE_ID + " = ?";
        String[] selectionArgs = { String.valueOf(exerciseId) };

        Cursor cursor = db.query(
                HistoryEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null, null,
                HistoryEntry.COLUMN_DATE + " ASC"
        );

        while(cursor.moveToNext()) {
            history.add(new ExerciseHistory(
                    cursor.getLong(0),
                    new Date(cursor.getLong(1)),
                    cursor.getInt(2),
                    cursor.getInt(3)
            ));
        }
        cursor.close();
        return history;
    }
    // ================== Create SPLIT ================== //
    public long createCustomSplit(String name, Map<String, List<Exercise>> days) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            long splitId = insertSplit(db, name, WorkoutSplit.TYPE_CUSTOM);

            for (Map.Entry<String, List<Exercise>> entry : days.entrySet()) {
                String day = entry.getKey();
                for (Exercise exercise : entry.getValue()) {
                    ContentValues values = new ContentValues();
                    values.put(SplitExerciseEntry.COLUMN_SPLIT_ID, splitId);
                    values.put(SplitExerciseEntry.COLUMN_EXERCISE_ID, exercise.getId());
                    values.put(SplitExerciseEntry.COLUMN_DAY, day);
                    values.put(SplitExerciseEntry.COLUMN_SETS, 3); // Fixed 3 sets
                    values.put(SplitExerciseEntry.COLUMN_REPS, 12); // Default 12 reps
                    values.put(SplitExerciseEntry.COLUMN_WEIGHT, 0); // Initial weight

                    db.insert(SplitExerciseEntry.TABLE_NAME, null, values);
                }
            }
            db.setTransactionSuccessful();
            return splitId;
        } catch (Exception e) {
            Log.e("DB", "Create split failed", e);
            return -1;
        } finally {
            db.endTransaction();
        }
    }
    // ================== Update SPLIT ================== //
    public void updateSplit(long splitId, String newName, Map<String, List<Exercise>> days) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            // Update split name
            ContentValues values = new ContentValues();
            values.put(SplitEntry.COLUMN_NAME, newName);
            db.update(SplitEntry.TABLE_NAME, values,
                    SplitEntry._ID + " = ?", new String[]{String.valueOf(splitId)});

            // Delete existing exercises
            db.delete(SplitExerciseEntry.TABLE_NAME,
                    SplitExerciseEntry.COLUMN_SPLIT_ID + " = ?",
                    new String[]{String.valueOf(splitId)});

            // Add new exercises
            for (Map.Entry<String, List<Exercise>> entry : days.entrySet()) {
                String day = entry.getKey();
                for (Exercise exercise : entry.getValue()) {
                    ContentValues exerciseValues = new ContentValues();
                    exerciseValues.put(SplitExerciseEntry.COLUMN_SPLIT_ID, splitId);
                    exerciseValues.put(SplitExerciseEntry.COLUMN_EXERCISE_ID, exercise.getId());
                    exerciseValues.put(SplitExerciseEntry.COLUMN_DAY, day);
                    // Add other columns as needed
                    db.insert(SplitExerciseEntry.TABLE_NAME, null, exerciseValues);
                }
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
    // ================== DELETE SPLIT ================== //
    public void deleteSplit(long splitId) {
        SQLiteDatabase db = getWritableDatabase();

        // Delete from splits table
        db.delete(WorkoutSplitContract.SplitEntry.TABLE_NAME,
                WorkoutSplitContract.SplitEntry._ID + " = ?",
                new String[]{String.valueOf(splitId)});

        // Delete related exercises
        db.delete(WorkoutSplitContract.SplitExerciseEntry.TABLE_NAME,
                WorkoutSplitContract.SplitExerciseEntry.COLUMN_SPLIT_ID + " = ?",
                new String[]{String.valueOf(splitId)});
    }
    // ================== CRUD Operations ================== //
    public void updateExerciseParameters(long splitId, long exerciseId,
                                         int setNumber, int reps, int weight) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SplitExerciseEntry.COLUMN_REPS, reps);
        values.put(SplitExerciseEntry.COLUMN_WEIGHT, weight);

        db.update(SplitExerciseEntry.TABLE_NAME,
                values,
                SplitExerciseEntry.COLUMN_SPLIT_ID + " = ? AND " +
                        SplitExerciseEntry.COLUMN_EXERCISE_ID + " = ? AND " +
                        SplitExerciseEntry.COLUMN_SET_NUMBER + " = ?",
                new String[]{
                        String.valueOf(splitId),
                        String.valueOf(exerciseId),
                        String.valueOf(setNumber)
                }
        );
    }
    // ================== Initial Data ================== //
    // WorkoutSplitDbHelper.java
    private void insertInitialData(SQLiteDatabase db) {
        try {
            insertExerciseLibrary(db);  // Make sure this is called
            insertPrebuiltSplits(db);
        } catch (Exception e) {
            Log.e("Database", "Initial data insert failed", e);
        }
    }

    private void insertPrebuiltSplits(SQLiteDatabase db) {
        //PPL
        long pplId = insertSplit(db, "Push/Pull/Legs Split", WorkoutSplit.TYPE_PREBUILT);
        linkExercisesToSplit(db, pplId, "Push Day", new String[]{"Bench Press", "Machine Chest Press", "Chest Fly", "Tricep Dips", "Tricep Pushdown"});
        linkExercisesToSplit(db, pplId, "Pull Day", new String[]{"Deadlifts","Bent-over Rows","Lat Pulldown", "Dumbbell Curl", "Preacher Curl"});
        linkExercisesToSplit(db, pplId, "Leg Day", new String[]{"Squats", "Romanian Deadlifts", "Leg Press", "Leg Extension", "Calf Raise"});

// Upper/Lower Split
        long upperLowerId = insertSplit(db, "Upper/Lower Split", WorkoutSplit.TYPE_PREBUILT);
        linkExercisesToSplit(db, upperLowerId, "Upper Body", new String[]{"Bench Press", "Deadlifts", "Overhead Press", "Tricep Dips", "Preacher Curl"});
        linkExercisesToSplit(db, upperLowerId, "Lower Body", new String[]{"Squats", "Romanian Deadlifts", "Leg Extension", "Calf Raise"});

// Bro Split
        long broSplitId = insertSplit(db, "Bro Split", WorkoutSplit.TYPE_PREBUILT);
        linkExercisesToSplit(db, broSplitId, "Chest", new String[]{"Bench Press", "Incline Dumbbell Press", "Chest Fly"});
        linkExercisesToSplit(db, broSplitId, "Shoulder", new String[]{"Overhead Press", "Lateral Raises", "Face Pulls"});
        linkExercisesToSplit(db, broSplitId, "Triceps", new String[]{"Tricep Dips", "Tricep Pushdown", "Skull Crushers"});
        linkExercisesToSplit(db, broSplitId, "Back", new String[]{"Deadlifts", "Bent-over Rows", "Lat Pulldown"});
        linkExercisesToSplit(db, broSplitId, "Biceps", new String[]{"Dumbbell Curl", "Hammer Curl", "Preacher Curl"});
        linkExercisesToSplit(db, broSplitId, "Legs", new String[]{"Squats", "Romanian Deadlifts", "Calf Raise"});

    }
    private long insertSplit(SQLiteDatabase db, String name, int type) {
        ContentValues values = new ContentValues();
        values.put(WorkoutSplitContract.SplitEntry.COLUMN_NAME, name);
        values.put(WorkoutSplitContract.SplitEntry.COLUMN_TYPE, type);
        return db.insert(WorkoutSplitContract.SplitEntry.TABLE_NAME, null, values);

    }


    private void linkExercisesToSplit(SQLiteDatabase db, long splitId, String day, String[] exerciseNames) {
        for (String exerciseName : exerciseNames) {
            long exerciseId = getExerciseIdByName(db, exerciseName.trim());
            if (exerciseId != -1) {
                ContentValues values = new ContentValues();
                values.put(WorkoutSplitContract.SplitExerciseEntry.COLUMN_SPLIT_ID, splitId);
                values.put(WorkoutSplitContract.SplitExerciseEntry.COLUMN_EXERCISE_ID, exerciseId);
                values.put(WorkoutSplitContract.SplitExerciseEntry.COLUMN_DAY, day);
                db.insert(WorkoutSplitContract.SplitExerciseEntry.TABLE_NAME, null, values);
            }
        }
    }

    private long getExerciseIdByName(SQLiteDatabase db, String exerciseName) {
        Cursor cursor = db.query(
                WorkoutSplitContract.ExerciseEntry.TABLE_NAME,
                new String[]{WorkoutSplitContract.ExerciseEntry._ID},
                WorkoutSplitContract.ExerciseEntry.COLUMN_NAME + " = ?",
                new String[]{exerciseName},
                null, null, null
        );

        long id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getLong(0);
        }
        cursor.close();
        return id;
    }
    private void insertExerciseLibrary(SQLiteDatabase db) {
        String[][] exercises = {
                {"Bench Press", "Chest", "Barbell"},
                {"Incline Dumbbell Press", "Chest", "Barbell"},
                {"Chest Fly", "Chest", "Machine"},
                {"Dips", "Chest", "Bodyweight"},
                {"Push-ups", "Chest", "Bodyweight"},
                {"Machine Chest Press", "Chest", "Machine"},

                {"Overhead Press", "Shoulders", "Barbell"},
                {"Lateral Raises", "Shoulders", "Dumbbell"},
                {"Face Pulls", "Shoulders", "Machine"},
                {"Arnold Press", "Shoulders", "Dumbbell"},
                {"Reverse Pec Deck", "Shoulders", "Machine"},
                {"Handstand Push-ups", "Shoulders", "Bodyweight"},

                {"Close-Grip Bench Press", "Triceps", "Barbell"},
                {"Tricep Dips", "Triceps", "Bodyweight"},
                {"Overhead Tricep Extension", "Triceps", "Dumbbell"},
                {"Tricep Pushdown", "Triceps", "Machine"},
                {"Skull Crushers", "Triceps", "Barbell"},
                {"Machine Triceps Extension", "Triceps", "Machine"},

                {"Lat Pulldown", "Back", "Machine"},
                {"Pull-ups", "Back", "Bodyweight"},
                {"Bent-over Rows", "Back", "Barbell"},
                {"Seated Cable Row", "Back", "Machine"},
                {"T-Bar Row", "Back", "Barbell"},
                {"Deadlifts", "Back", "Barbell"},

                {"Barbell Curl", "Biceps", "Barbell"},
                {"Dumbbell Curl", "Biceps", "Dumbbell"},
                {"Hammer Curl", "Biceps", "Dumbbell"},
                {"Preacher Curl", "Biceps", "Machine"},
                {"Concentration Curl", "Biceps", "Dumbbell"},
                {"Cable Curl", "Biceps", "Machine"},

                {"Wrist Curl", "Forearms", "Dumbbell"},
                {"Reverse Wrist Curl", "Forearms", "Dumbbell"},
                {"Farmer’s Walk", "Forearms", "Dumbbell"},
                {"Reverse Curl", "Forearms", "Barbell"},
                {"Cable Wrist Curl", "Forearms", "Machine"},

                {"Squats", "Legs", "Barbell"},
                {"Leg Press", "Legs", "Machine"},
                {"Lunges", "Legs", "Dumbbell"},
                {"Romanian Deadlifts", "Legs", "Barbell"},
                {"Leg Extension", "Legs", "Machine"},
                {"Calf Raise", "Legs", "Machine"},

                {"Plank", "Core", "Bodyweight"},
                {"Hanging Leg Raise", "Core", "Bodyweight"},
                {"Sit-ups", "Core", "Bodyweight"},
                {"Dumbbell Side Bend", "Core", "Dumbbell"},
                {"Machine Crunch", "Core", "Machine"},
                {"Leg Raise Machine", "Core", "Machine"}

        };

        for (String[] exercise : exercises) {
            ContentValues values = new ContentValues();
            values.put(WorkoutSplitContract.ExerciseEntry.COLUMN_NAME, exercise[0]);
            values.put(WorkoutSplitContract.ExerciseEntry.COLUMN_MUSCLE_GROUP, exercise[1]);
            values.put(WorkoutSplitContract.ExerciseEntry.COLUMN_EQUIPMENT, exercise[2]);
            db.insert(WorkoutSplitContract.ExerciseEntry.TABLE_NAME, null, values);
        }
    }
}