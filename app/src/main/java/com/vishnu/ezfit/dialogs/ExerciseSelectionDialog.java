package com.vishnu.ezfit.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.vishnu.ezfit.R;
import com.vishnu.ezfit.adapters.ExerciseLibraryAdapter;
import com.vishnu.ezfit.database.WorkoutSplitDbHelper;
import com.vishnu.ezfit.models.Exercise;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;

public class ExerciseSelectionDialog extends DialogFragment {

    public interface ExerciseSelectionListener {
        void onExercisesSelected(List<Exercise> selectedExercises);
    }

    private ExerciseLibraryAdapter adapter;
    private ExerciseSelectionListener listener;
    private List<Exercise> initialSelection;
    private WorkoutSplitDbHelper dbHelper;
    private Button btnConfirm;

    public static ExerciseSelectionDialog newInstance(WorkoutSplitDbHelper dbHelper, List<Exercise> initialSelection) {
        ExerciseSelectionDialog dialog = new ExerciseSelectionDialog();
        Bundle args = new Bundle();
        args.putParcelableArrayList("initial", new ArrayList<>(initialSelection));
        dialog.setArguments(args);
        dialog.setDbHelper(dbHelper);
        return dialog;
    }

    public void setDbHelper(WorkoutSplitDbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void setExerciseSelectedListener(ExerciseSelectionListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            initialSelection = getArguments().getParcelableArrayList("initial");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_exercise_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SearchView searchView = view.findViewById(R.id.search_view);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_exercises);
        btnConfirm = view.findViewById(R.id.btn_confirm);

        // Always show confirm button with initial count
        btnConfirm.setVisibility(View.VISIBLE);
        updateConfirmButtonText(0);

        // Load exercises from database
        List<Exercise> exercises = dbHelper != null ?
                dbHelper.getAllExercises() :
                new ArrayList<>();

        Log.d("ExerciseDialog", "Loaded exercises: " + exercises.size());

        // Initialize adapter with selection handling
        adapter = new ExerciseLibraryAdapter(
                exercises,
                new ExerciseLibraryAdapter.ExerciseSelectionListener() {
                    @Override
                    public void onExerciseSelected(Exercise exercise) {
                        updateSelectionCount();
                    }

                    @Override
                    public void onExerciseDeselected(Exercise exercise) {
                        updateSelectionCount();
                    }
                },
                true // Multi-select enabled
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Restore initial selections
        if (initialSelection != null && !initialSelection.isEmpty()) {
            adapter.setSelectedExercises(initialSelection);
            updateSelectionCount();
        }

        // Configure search
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });

        btnConfirm.setOnClickListener(v -> {
            if (listener != null && adapter != null) {
                listener.onExercisesSelected(adapter.getSelectedExercises());
            }
            dismiss();
        });
    }

    private void updateSelectionCount() {
        int selectedCount = adapter.getSelectedExercises().size();
        String buttonText = getResources().getString(
                R.string.confirm_selection,
                selectedCount
        );
        btnConfirm.setText(buttonText);
    }

    private void updateConfirmButtonText(int count) {
        String buttonText = getResources().getQuantityString(
                R.plurals.confirm_selection_count,
                count,
                count
        );
        btnConfirm.setText(buttonText);
    }
}