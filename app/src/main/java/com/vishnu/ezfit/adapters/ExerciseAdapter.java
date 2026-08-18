package com.vishnu.ezfit.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.vishnu.ezfit.R;
import com.vishnu.ezfit.databinding.ItemExerciseBinding;
import com.vishnu.ezfit.databinding.ItemSetBinding;
import com.vishnu.ezfit.models.Exercise;
import java.util.Locale;
import androidx.core.content.ContextCompat;
public class ExerciseAdapter extends ListAdapter<Exercise, ExerciseAdapter.ViewHolder> {

    private final ExerciseListener listener;
    private final boolean isEditable;
    private final Context context;


    // Modified constructor to properly receive context
    public ExerciseAdapter(Context context, ExerciseListener listener, boolean isEditable) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.listener = listener;
        this.isEditable = isEditable;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemExerciseBinding binding = ItemExerciseBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Exercise exercise = getItem(position);
        holder.bind(exercise);
        holder.itemView.post(() -> {
            if (exercise.isExpanded()) {
                holder.binding.setsContainer.setVisibility(View.VISIBLE);
            }
        });
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemExerciseBinding binding;

        ViewHolder(ItemExerciseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(v -> toggleExpansion());
        }
        private void toggleExpansion() {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Exercise exercise = getItem(position);
                exercise.setExpanded(!exercise.isExpanded());
                notifyItemChanged(position);
            }
        }
        void bind(Exercise exercise) {
            // Basic information binding
            binding.tvExerciseName.setText(exercise.getName());
            binding.tvMuscleGroup.setText(exercise.getMuscleGroup());
            binding.tvEquipment.setText(exercise.getEquipment());

            // Set details using first set's values
            if (!exercise.getSets().isEmpty()) {
                Exercise.ExerciseSet firstSet = exercise.getSets().get(0);
                binding.exerciseDetails.setText(context.getString(
                        R.string.exercise_details_format,
                        exercise.getSets().size(),
                        firstSet.getReps(),
                        firstSet.getWeight()
                ));
            }

            // Handle expansion state
            binding.setsContainer.setVisibility(exercise.isExpanded() ? View.VISIBLE : View.GONE);
            binding.expandIcon.setRotation(exercise.isExpanded() ? 180 : 0);
            binding.expandIcon.animate().rotation(exercise.isExpanded() ? 180 : 0).start();

            if (exercise.isExpanded()) {
                setupSets(exercise);
            }
        }

        private void setupSets(Exercise exercise) {
            binding.setsContainer.removeAllViews();
            Context localContext = binding.getRoot().getContext();

            for (Exercise.ExerciseSet set : exercise.getSets()) {
                ItemSetBinding setBinding = ItemSetBinding.inflate(
                        LayoutInflater.from(localContext),
                        binding.setsContainer,
                        true
                );

                // Set number display
                setBinding.tvSetNumber.setText(
                        localContext.getString(R.string.set_number, set.getSetNumber())
                );

                // Initialize set values
                setBinding.etWeight.setText(String.valueOf(set.getWeight()));
                setBinding.etReps.setText(String.valueOf(set.getReps()));

                // Handle logged state
                if (set.isLogged()) {
                    setBinding.etWeight.setEnabled(false);
                    setBinding.etReps.setEnabled(false);
                    setBinding.btnComplete.setEnabled(false);
                    setBinding.btnComplete.setText(R.string.logged);
                    setBinding.btnComplete.setBackgroundColor(ContextCompat.getColor(context, R.color.secondary_container));
                    setBinding.btnComplete.setTextColor(ContextCompat.getColor(context, R.color.on_secondary_container));
                }

                // Set completion handler
                setBinding.btnComplete.setOnClickListener(v -> {
                    try {
                        int weight = Integer.parseInt(setBinding.etWeight.getText().toString());
                        int reps = Integer.parseInt(setBinding.etReps.getText().toString());

                        if (weight <= 0 || reps <= 0) {
                            Toast.makeText(context, "Enter valid numbers", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Update the specific set
                        set.setWeight(weight);
                        set.setReps(reps);
                        set.setLogged(true);

                        // Disable inputs
                        setBinding.etWeight.setEnabled(false);
                        setBinding.etReps.setEnabled(false);
                        setBinding.btnComplete.setEnabled(false);
                        setBinding.btnComplete.setText(R.string.logged);

                        // Notify database through listener
                        if (listener != null) {
                            listener.onSetLogged(exercise, set.getSetNumber(), weight, reps);
                        }

                        // Update UI
                        notifyItemChanged(getAdapterPosition());

                    } catch (NumberFormatException e) {
                        Toast.makeText(context, "Invalid numbers", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    public interface ExerciseListener {
        void onSetLogged(Exercise exercise, int setNumber, int weight, int reps);
        void onExerciseUpdated(Exercise exercise);
    }

    private static final DiffUtil.ItemCallback<Exercise> DIFF_CALLBACK =
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