package com.vishnu.ezfit.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.vishnu.ezfit.R;
import com.vishnu.ezfit.database.WorkoutSplitDbHelper;
import com.vishnu.ezfit.databinding.DialogCreateSplitBinding;
import com.vishnu.ezfit.models.Exercise;
import com.vishnu.ezfit.models.WorkoutSplit;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import android.util.Log;
import com.google.android.material.textfield.TextInputEditText;
import androidx.core.content.ContextCompat;

public class CreateSplitDialog extends DialogFragment {

    public interface SplitCreationListener {
        void onSplitModified(long splitId, String splitName, Map<String, List<Exercise>> days);
    }

    private DialogCreateSplitBinding binding;
    private SplitCreationListener listener;
    private final Map<String, List<Exercise>> days = new LinkedHashMap<>();
    private WorkoutSplitDbHelper dbHelper;
    private WorkoutSplit existingSplit;

    public static CreateSplitDialog newInstance() {
        return new CreateSplitDialog();
    }

    public static CreateSplitDialog newInstance(WorkoutSplit existingSplit) {
        CreateSplitDialog dialog = new CreateSplitDialog();
        Bundle args = new Bundle();
        args.putParcelable("existing_split", existingSplit);
        dialog.setArguments(args);
        return dialog;
    }

    public void setSplitCreationListener(SplitCreationListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            existingSplit = getArguments().getParcelable("existing_split");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogCreateSplitBinding.inflate(inflater, container, false);
        dbHelper = new WorkoutSplitDbHelper(requireContext());
        setupUI();
        return binding.getRoot();
    }

    private void setupUI() {
        binding.btnAddDay.setOnClickListener(v -> showAddDayDialog());
        binding.btnSave.setOnClickListener(v -> saveSplit());

        // Load existing split data if editing
        if (existingSplit != null) {
            binding.etSplitName.setText(existingSplit.getName());
            days.putAll(existingSplit.getDays());
            updateDayButtons();
        }
    }

    private void showAddDayDialog() {
        DayDialog dialog = new DayDialog(dbHelper);
        dialog.setDayListener(new DayDialog.DayListener() {
            @Override
            public void onDayCreated(String dayName, List<Exercise> exercises) {
                if (!dayName.isEmpty() && !exercises.isEmpty()) {
                    days.put(dayName, exercises);
                    updateDayButtons();
                }
            }
        });
        dialog.show(getParentFragmentManager(), "day_dialog");
    }

    private void updateDayButtons() {
        binding.daysContainer.removeAllViews();
        for (String dayName : days.keySet()) {
            MaterialButton btn = createDayButton(dayName);
            btn.setOnClickListener(v -> editDay(dayName));
            binding.daysContainer.addView(btn);
        }
    }

    private MaterialButton createDayButton(String dayName) {
        MaterialButton button = new MaterialButton(requireContext());
        button.setText(dayName);
        button.setCornerRadius(16);
        button.setStrokeWidth(2);
        button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.selected_item_background));
        button.setLayoutParams(new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        return button;
    }

    private void editDay(String dayName) {
        DayDialog dialog = new DayDialog(dbHelper);
        dialog.setExercises(days.get(dayName));
        dialog.setDayListener((newName, exercises) -> {
            days.remove(dayName);
            if (!newName.isEmpty() && !exercises.isEmpty()) {
                days.put(newName, exercises);
                updateDayButtons();
            }
        });
        dialog.show(getParentFragmentManager(), "edit_day");
    }

    private void saveSplit() {
        String splitName = binding.etSplitName.getText().toString().trim();

        if (splitName.isEmpty()) {
            binding.etSplitName.setError("Enter split name");
            return;
        }

        if (days.isEmpty()) {
            Toast.makeText(requireContext(), "Add at least one day", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            long splitId = existingSplit != null ? existingSplit.getId() : -1;

            if (listener != null) {
                listener.onSplitModified(splitId, splitName, days);
            }
            dismiss();
        } catch (Exception e) {
            Log.e("CreateSplit", "Error: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dbHelper != null) {
            dbHelper.close();
        }
        binding = null;
    }

    public static class DayDialog extends DialogFragment {
        private List<Exercise> exercises = new ArrayList<>();
        private DayListener listener;
        private WorkoutSplitDbHelper dbHelper;

        public DayDialog() {}

        public DayDialog(WorkoutSplitDbHelper dbHelper) {
            this.dbHelper = dbHelper;
        }

        interface DayListener {
            void onDayCreated(String dayName, List<Exercise> exercises);
        }

        void setDayListener(DayListener listener) {
            this.listener = listener;
        }

        void setExercises(List<Exercise> exercises) {
            this.exercises = new ArrayList<>(exercises);
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 @Nullable ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.dialog_day, container, false);
            TextInputEditText etDayName = view.findViewById(R.id.etDayName);
            MaterialButton btnSelectExercises = view.findViewById(R.id.btnSelectExercises);
            MaterialButton btnSave = view.findViewById(R.id.btnSave);

            btnSelectExercises.setOnClickListener(v -> showExerciseSelector());
            btnSave.setOnClickListener(v -> {
                String dayName = etDayName.getText().toString().trim();

                if (dayName.isEmpty()) {
                    etDayName.setError("Enter day name");
                    return;
                }

                if (exercises.isEmpty()) {
                    Toast.makeText(requireContext(), "Select at least one exercise", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (listener != null) {
                    // Preserve existing exercise data
                    listener.onDayCreated(dayName, new ArrayList<>(exercises));
                    dismiss();
                }
            });

            return view;
        }

        private void showExerciseSelector() {
            ExerciseSelectionDialog dialog = ExerciseSelectionDialog.newInstance(dbHelper, exercises);
            dialog.setExerciseSelectedListener(selected -> {
                exercises.clear();
                exercises.addAll(selected);
            });
            dialog.show(getParentFragmentManager(), "select_exercises");
        }
    }
}