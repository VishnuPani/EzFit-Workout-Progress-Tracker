package com.vishnu.ezfit.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.vishnu.ezfit.R;
import com.vishnu.ezfit.adapters.ExerciseHistoryAdapter;
import com.vishnu.ezfit.database.WorkoutSplitDbHelper;
import com.vishnu.ezfit.models.Exercise;
import com.vishnu.ezfit.models.ExerciseHistory;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProgressFragment extends Fragment {

    private LineChart progressChart;
    private RecyclerView historyRecycler;
    private ExerciseHistoryAdapter historyAdapter;
    private WorkoutSplitDbHelper dbHelper;
    private List<Exercise> allExercises = new ArrayList<>();
    private ArrayAdapter<Exercise> exerciseAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress, container, false);
        dbHelper = new WorkoutSplitDbHelper(requireContext());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressChart = view.findViewById(R.id.progress_chart);
        historyRecycler = view.findViewById(R.id.history_recycler);
        AutoCompleteTextView exerciseDropdown = view.findViewById(R.id.exercise_dropdown);

        setupChart();
        setupHistoryRecycler();
        setupExerciseSelector(exerciseDropdown);
    }

    private void setupExerciseSelector(AutoCompleteTextView dropdown) {
        allExercises = dbHelper.getAllExercises();

        // Create custom adapter with filtering
        exerciseAdapter = new ArrayAdapter<Exercise>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                allExercises
        ) {
            @NonNull
            @Override
            public Filter getFilter() {
                return new ExerciseFilter();
            }
        };

        dropdown.setAdapter(exerciseAdapter);
        dropdown.setThreshold(1); // Start filtering after 1 character
        dropdown.setOnItemClickListener((parent, view, position, id) -> {
            // Get from filtered list
            Exercise selected = (Exercise) parent.getItemAtPosition(position);
            loadExerciseData(selected.getId());
        });

        // Set initial selection
        if (!allExercises.isEmpty()) {
            dropdown.setText(allExercises.get(0).toString(), false);
            loadExerciseData(allExercises.get(0).getId());
        }
    }

    private class ExerciseFilter extends Filter {
        private List<Exercise> originalList = new ArrayList<>(allExercises);
        private List<Exercise> filteredList = new ArrayList<>();

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filteredList.clear();
            FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(originalList);
            } else {
                String pattern = constraint.toString().toLowerCase().trim();
                for (Exercise exercise : originalList) {
                    if (exercise.getName().toLowerCase().contains(pattern)) {
                        filteredList.add(exercise);
                    }
                }
            }

            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            exerciseAdapter.clear();
            exerciseAdapter.addAll((List<Exercise>) results.values);
            exerciseAdapter.notifyDataSetChanged();
        }
    }

    private void setupChart() {
        progressChart.getDescription().setEnabled(false);
        progressChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        progressChart.getAxisRight().setEnabled(false);
        progressChart.getLegend().setEnabled(false);
    }

    private void setupHistoryRecycler() {
        historyAdapter = new ExerciseHistoryAdapter();
        historyRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        historyRecycler.setAdapter(historyAdapter);
    }

    private void loadExerciseData(long exerciseId) {
        // Clear previous data
        progressChart.clear();
        historyAdapter.submitList(null);

        // Load new data
        List<ExerciseHistory> exerciseHistory = dbHelper.getExerciseHistory(exerciseId);

        updateChart(exerciseHistory);
        historyAdapter.submitList(exerciseHistory);
        historyRecycler.scheduleLayoutAnimation(); // Smooth update
    }

    private void updateChart(List<ExerciseHistory> history) {
        List<Entry> entries = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());

        for (ExerciseHistory entry : history) {
            entries.add(new Entry(
                    entry.getDate().getTime(),
                    entry.getWeight()
            ));
        }

        if (!entries.isEmpty()) {
            LineDataSet dataSet = new LineDataSet(entries, "Weight Progress");
            dataSet.setColor(Color.parseColor("#6750A4"));
            dataSet.setValueTextColor(Color.DKGRAY);

            LineData lineData = new LineData(dataSet);
            progressChart.setData(lineData);

            progressChart.getXAxis().setValueFormatter(new ValueFormatter() {
                private final SimpleDateFormat mFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());

                @Override
                public String getFormattedValue(float value) {
                    return mFormat.format(new Date((long) value));
                }
            });

            progressChart.notifyDataSetChanged();
            progressChart.invalidate();
        } else {
            progressChart.clear();
            progressChart.invalidate();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}