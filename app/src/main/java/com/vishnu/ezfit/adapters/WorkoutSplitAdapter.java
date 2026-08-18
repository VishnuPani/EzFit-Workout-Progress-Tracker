package com.vishnu.ezfit.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.vishnu.ezfit.R;
import com.vishnu.ezfit.databinding.ItemSplitBinding;
import com.vishnu.ezfit.models.WorkoutSplit;
import android.view.View;
import androidx.core.content.ContextCompat;
import java.util.List;
import java.util.Map;

public class WorkoutSplitAdapter extends ListAdapter<WorkoutSplit, WorkoutSplitAdapter.ViewHolder> {

    public interface SplitListener {
        void onSplitClicked(WorkoutSplit split);
        void onDayClicked(WorkoutSplit split, String day);
        void onSplitDeleted(WorkoutSplit split);
        void onSplitEdited(WorkoutSplit split);
    }

    private final SplitListener listener;

    public WorkoutSplitAdapter(SplitListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSplitBinding binding = ItemSplitBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSplitBinding binding;
        private final Context context;
        private final SplitListener listener;

        ViewHolder(ItemSplitBinding binding, SplitListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.context = binding.getRoot().getContext();
            this.listener = listener;
            setupButtons();
        }

        void bind(WorkoutSplit split) {
            binding.tvSplitName.setText(split.getName());
            setupDays(split);
            setupActions(split);
            setupButtonVisibility(split);
        }

        private void setupDays(WorkoutSplit split) {
            binding.dayContainer.removeAllViews();
            for (String day : split.getDays().keySet()) {
                MaterialButton button = createDayButton(day);
                button.setOnClickListener(v -> listener.onDayClicked(split, day));
                binding.dayContainer.addView(button);
            }
        }
        private void setupButtons() {
            binding.btnEdit.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onSplitEdited(getItem(position));
                }
            });

            binding.btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onSplitDeleted(getItem(position));
                }
            });
        }

        private void setupButtonVisibility(WorkoutSplit split) {
            boolean isCustom = split.getType() == WorkoutSplit.TYPE_CUSTOM;
            binding.btnEdit.setVisibility(isCustom ? View.VISIBLE : View.GONE);
            binding.btnDelete.setVisibility(isCustom ? View.VISIBLE : View.GONE);
        }

        private MaterialButton createDayButton(String day) {
            MaterialButton button = new MaterialButton(context);

            // Styling
            button.setText(day);
            button.setCornerRadius(16);
            button.setStrokeWidth(2);
            button.setStrokeColor(ContextCompat.getColorStateList(context, R.color.secondary));
            button.setTextAppearance(context, R.style.TextAppearance_MaterialComponents_Button);

            // Layout
            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 16, 0);
            button.setLayoutParams(params);

            return button;
        }

        private void setupActions(WorkoutSplit split) {
            // Disable delete/edit for pre-built splits
            binding.btnEdit.setVisibility(
                    split.getType() == WorkoutSplit.TYPE_CUSTOM ? View.VISIBLE : View.GONE
            );
            binding.getRoot().setOnClickListener(v -> listener.onSplitClicked(split));
            binding.btnDelete.setOnClickListener(v -> listener.onSplitDeleted(split));
            binding.btnDelete.setVisibility(
                    split.getType() == WorkoutSplit.TYPE_CUSTOM ? View.VISIBLE : View.GONE
            );
        }
    }

    private static final DiffUtil.ItemCallback<WorkoutSplit> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<WorkoutSplit>() {
                @Override
                public boolean areItemsTheSame(@NonNull WorkoutSplit oldItem, @NonNull WorkoutSplit newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull WorkoutSplit oldItem, @NonNull WorkoutSplit newItem) {
                    return oldItem.equals(newItem);
                }
            };
}