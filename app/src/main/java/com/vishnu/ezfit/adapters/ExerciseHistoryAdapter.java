package com.vishnu.ezfit.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.vishnu.ezfit.databinding.ItemHistoryBinding;
import com.vishnu.ezfit.models.ExerciseHistory;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ExerciseHistoryAdapter extends ListAdapter<ExerciseHistory, ExerciseHistoryAdapter.ViewHolder> {
    private final SimpleDateFormat dateFormat;

    public ExerciseHistoryAdapter() {
        super(DIFF_CALLBACK);
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHistoryBinding binding = ItemHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemHistoryBinding binding;

        ViewHolder(ItemHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ExerciseHistory history) {
            binding.tvDate.setText(dateFormat.format(history.getDate()));
            binding.tvWeight.setText(String.valueOf(history.getWeight()));
            binding.tvReps.setText(String.valueOf(history.getReps()));
        }
    }


    private static final DiffUtil.ItemCallback<ExerciseHistory> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ExerciseHistory>() {
                @Override
                public boolean areItemsTheSame(@NonNull ExerciseHistory oldItem, @NonNull ExerciseHistory newItem) {
                    // Compare IDs
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull ExerciseHistory oldItem, @NonNull ExerciseHistory newItem) {
                    // Uses equals() we implemented
                    return oldItem.equals(newItem);
                }
            };
}