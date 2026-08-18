package com.vishnu.ezfit.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vishnu.ezfit.R;
import com.vishnu.ezfit.adapters.WorkoutSplitAdapter;
import com.vishnu.ezfit.database.WorkoutSplitDbHelper;
import com.vishnu.ezfit.databinding.FragmentHomeBinding;
import com.vishnu.ezfit.models.WorkoutSplit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.vishnu.ezfit.models.Exercise;
import com.vishnu.ezfit.dialogs.CreateSplitDialog;
import android.app.AlertDialog;



public class HomeFragment extends Fragment implements WorkoutSplitAdapter.SplitListener {

    private WorkoutSplitDbHelper dbHelper;
    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new WorkoutSplitDbHelper(requireContext());
        setupRecyclerViews();
        setupFab();
        loadSplits();
    }

    private void setupRecyclerViews() {
        // Pre-built Splits
        binding.rvPrebuiltSplits.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPrebuiltSplits.setAdapter(new WorkoutSplitAdapter(this));

        // Custom Splits
        binding.rvCustomSplits.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCustomSplits.setAdapter(new WorkoutSplitAdapter(this));
    }

    private void loadSplits() {
        List<WorkoutSplit> allSplits = dbHelper.getAllSplits();
        List<WorkoutSplit> prebuilt = new ArrayList<>();
        List<WorkoutSplit> custom = new ArrayList<>();

        for (WorkoutSplit split : allSplits) {
            if (split.getType() == WorkoutSplit.TYPE_PREBUILT) {
                prebuilt.add(split);
            } else {
                custom.add(split);
            }
        }

        ((WorkoutSplitAdapter) binding.rvPrebuiltSplits.getAdapter()).submitList(prebuilt);
        ((WorkoutSplitAdapter) binding.rvCustomSplits.getAdapter()).submitList(custom);
    }

    @Override
    public void onDayClicked(WorkoutSplit split, String day) {
        // Fixed: Use proper newInstance method
        WorkoutFragment fragment = WorkoutFragment.newInstance(split.getId(), day);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }
    private void setupFab() {
        binding.fabAddSplit.setOnClickListener(v -> {
            CreateSplitDialog dialog = CreateSplitDialog.newInstance();
            dialog.setSplitCreationListener((splitId, splitName, days) -> {
                dbHelper.createCustomSplit(splitName, days);
                loadSplits();
            });
            dialog.show(getParentFragmentManager(), "create_split");
        });
    }

    // Other interface methods
    @Override public void onSplitClicked(WorkoutSplit split) { /* Handle click */ }
    // In HomeFragment.java
    @Override
    public void onSplitEdited(WorkoutSplit split) {
        CreateSplitDialog dialog = CreateSplitDialog.newInstance(split);
        dialog.setSplitCreationListener((splitId, newName, days) -> {
            dbHelper.updateSplit(splitId, newName, days);
            loadSplits();
        });
        dialog.show(getParentFragmentManager(), "edit_split");
    }
    @Override
    public void onSplitDeleted(WorkoutSplit split) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Split")
                .setMessage("Are you sure you want to delete this split?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteSplit(split.getId());
                    loadSplits();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dbHelper.close();
        binding = null;
    }
}