package com.vishnu.ezfit.adapters;

import android.widget.Filter;
import android.widget.Filterable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.vishnu.ezfit.databinding.ItemExerciseBinding;
import com.vishnu.ezfit.models.Exercise;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExerciseLibraryAdapter extends ListAdapter<Exercise, ExerciseLibraryAdapter.ViewHolder>
        implements Filterable {

    private final Set<Long> selectedIds = new HashSet<>();
    private final ExerciseSelectionListener listener;
    private final boolean multiSelectMode;
    private List<Exercise> originalList;

    public ExerciseLibraryAdapter(List<Exercise> exercises,
                                  ExerciseSelectionListener listener,
                                  boolean multiSelectMode) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        this.multiSelectMode = multiSelectMode;
        this.originalList = new ArrayList<>(exercises);
        submitList(exercises);
    }


    public void setSelectedExercises(List<Exercise> exercises) {
        selectedIds.clear();
        for (Exercise exercise : exercises) {
            selectedIds.add(exercise.getId());
        }
        notifyDataSetChanged();
    }


    public List<Exercise> getSelectedExercises() {
        List<Exercise> selected = new ArrayList<>();
        for (Exercise exercise : getCurrentList()) {
            if (selectedIds.contains(exercise.getId())) {
                selected.add(exercise);
            }
        }
        return selected;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemExerciseBinding binding = ItemExerciseBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

   public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemExerciseBinding binding;

        ViewHolder(ItemExerciseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Exercise exercise = getItem(position);
                    toggleSelection(exercise.getId());
                }
            });
        }

        private void toggleSelection(long exerciseId) {
            if (selectedIds.contains(exerciseId)) {
                selectedIds.remove(exerciseId);
                listener.onExerciseDeselected(getItem(getAdapterPosition()));
            } else {
                if (!multiSelectMode) selectedIds.clear();
                selectedIds.add(exerciseId);
                listener.onExerciseSelected(getItem(getAdapterPosition()));
            }
            notifyItemChanged(getAdapterPosition());
        }


        void bind(Exercise exercise) {
            binding.tvExerciseName.setText(exercise.getName());
            binding.tvMuscleGroup.setText(exercise.getMuscleGroup());
            binding.tvEquipment.setText(exercise.getEquipment());

            // Update selection state
            itemView.setActivated(selectedIds.contains(exercise.getId()));
        }

        private void updateSelectionUI(Exercise exercise) {
            boolean selected = selectedIds.contains(exercise.getId());
            itemView.setActivated(selected);
        }

        private void setupClickListeners(Exercise exercise) {
            itemView.setOnClickListener(v -> handleSelection(exercise));
        }

        private void handleSelection(Exercise exercise) {
            if (selectedIds.contains(exercise.getId())) {
                selectedIds.remove(exercise.getId());
                listener.onExerciseDeselected(exercise);
            } else {
                if (!multiSelectMode) selectedIds.clear();
                selectedIds.add(exercise.getId());
                listener.onExerciseSelected(exercise);
            }
            notifyItemChanged(getAdapterPosition());
        }
    }

    @Override
    public Filter getFilter() {
        return new ExerciseFilter();
    }

    // ExerciseLibraryAdapter.java
    private class ExerciseFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Exercise> filtered = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filtered.addAll(originalList);
            } else {
                String pattern = constraint.toString().toLowerCase().trim();
                for (Exercise exercise : originalList) {
                    if (exercise.getName().toLowerCase().contains(pattern) ||
                            exercise.getMuscleGroup().toLowerCase().contains(pattern)) {
                        filtered.add(exercise);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filtered;
            return results;
        }


        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            submitList((List<Exercise>) results.values);
        }
    }


    public interface ExerciseSelectionListener {
        void onExerciseSelected(Exercise exercise);
        void onExerciseDeselected(Exercise exercise);
    }

    public static final DiffUtil.ItemCallback<Exercise> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Exercise>() {
                @Override
                public boolean areItemsTheSame(@NonNull Exercise oldItem, @NonNull Exercise newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Exercise oldItem, @NonNull Exercise newItem) {
                    return oldItem.equals(newItem);
                }
            };
}