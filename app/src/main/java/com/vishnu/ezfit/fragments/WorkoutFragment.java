package com.vishnu.ezfit.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.vishnu.ezfit.databinding.FragmentWorkoutBinding;
import com.vishnu.ezfit.adapters.ExerciseAdapter;
import com.vishnu.ezfit.database.WorkoutSplitDbHelper;
import com.vishnu.ezfit.models.Exercise;
import java.util.List;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import com.vishnu.ezfit.database.WorkoutSplitContract;
import com.vishnu.ezfit.models.Exercise.ExerciseSet;
import android.util.Log;

public class WorkoutFragment extends Fragment implements ExerciseAdapter.ExerciseListener {

    private FragmentWorkoutBinding binding;
    private WorkoutSplitDbHelper dbHelper;
    private ExerciseAdapter adapter;
    private long splitId;
    private String day;

    public static WorkoutFragment newInstance(long splitId, String day) {
        WorkoutFragment fragment = new WorkoutFragment();
        Bundle args = new Bundle();
        args.putLong("splitId", splitId);
        args.putString("day", day);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWorkoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new WorkoutSplitDbHelper(requireContext());

        if (getArguments() != null) {
            splitId = getArguments().getLong("splitId");
            day = getArguments().getString("day");
        }

        setupUI();
        loadExercises();

        binding.btnFinishWorkout.setOnClickListener(v -> finishWorkout());
    }

    private void setupUI() {
        binding.tvWorkoutDay.setText(day);

        adapter = new ExerciseAdapter(
                requireContext(),
                this,
                true // Enable editing
        );

        binding.recyclerExercises.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerExercises.setAdapter(adapter);
    }
    @Override
    public void onResume() {
        super.onResume();
        // Reload data when returning to the fragment
        loadExercises();
    }

    private void loadExercises() {
        if (splitId <= 0 || day == null || day.isEmpty()) {
            Toast.makeText(requireContext(), "Invalid workout configuration", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return;
        }
        List<Exercise> exercises = dbHelper.getExercisesForDay(splitId, day);
        for (Exercise exercise : exercises) {
            exercise.setExpanded(false);
        }
        adapter.submitList(exercises);
    }


    private void finishWorkout() {
        if (validateWorkoutCompletion()) {
            saveWorkoutData();
            navigateBack();
        }
    }


    private boolean validateWorkoutCompletion() {
        for (Exercise exercise : adapter.getCurrentList()) {
            boolean hasModifiedSets = false;

            // Check if any sets were modified
            for (Exercise.ExerciseSet set : exercise.getSets()) {
                if (set.isLogged() || set.getReps() != 12 || set.getWeight() != 0) {
                    hasModifiedSets = true;
                    break;
                }
            }

            // Only validate exercises with modified sets
            if (hasModifiedSets) {
                for (Exercise.ExerciseSet set : exercise.getSets()) {
                    if (!set.isLogged()) {
                        Toast.makeText(requireContext(),
                                "Complete all sets for " + exercise.getName(),
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
            }
        }
        return true;
    }


    private void saveWorkoutData() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long date = System.currentTimeMillis();

        try {
            db.beginTransaction();
            for (Exercise exercise : adapter.getCurrentList()) {
                for (ExerciseSet set : exercise.getSets()) {
                    if (set.getSetNumber() == 0) {
                        Log.e("WorkoutFragment", "Invalid set number for exercise: " + exercise.getName());
                        continue;
                    }
                    ContentValues values = new ContentValues();
                    values.put(WorkoutSplitContract.HistoryEntry.COLUMN_EXERCISE_ID, exercise.getId());
                    values.put(WorkoutSplitContract.HistoryEntry.COLUMN_DATE, date);
                    values.put(WorkoutSplitContract.HistoryEntry.COLUMN_SET_NUMBER, set.getSetNumber());
                    values.put(WorkoutSplitContract.HistoryEntry.COLUMN_WEIGHT, set.getWeight());
                    values.put(WorkoutSplitContract.HistoryEntry.COLUMN_REPS, set.getReps());

                    db.insert(WorkoutSplitContract.HistoryEntry.TABLE_NAME, null, values);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        Toast.makeText(requireContext(), "Workout saved", Toast.LENGTH_SHORT).show();
    }

    private void navigateBack() {
        if (isAdded() && getActivity() != null) {
            requireActivity().getSupportFragmentManager().popBackStack();
        }
    }


    @Override
    public void onSetLogged(Exercise exercise, int setNumber, int weight, int reps) {
        Exercise.ExerciseSet set = exercise.getSets().get(setNumber - 1);

        // Update the specific set
        set.setWeight(weight);
        set.setReps(reps);
        set.setLogged(true);

        // Update database
        dbHelper.logExerciseSet(exercise.getId(), setNumber, weight, reps);

        // Update parameters in database (if needed)
        dbHelper.updateExerciseParameters(
                splitId,
                exercise.getId(),
                set.getSetNumber(),  // Pass specific set number
                set.getReps(),
                set.getWeight()
        );

        adapter.notifyItemChanged(adapter.getCurrentList().indexOf(exercise));
    }
    @Override
    public void onExerciseUpdated(Exercise exercise) {
        // Handled in onSetLogged
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dbHelper != null) {
            dbHelper.close();
        }
        binding = null;
    }
}