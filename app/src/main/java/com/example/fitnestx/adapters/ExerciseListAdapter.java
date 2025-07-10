package com.example.fitnestx.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fitnestx.R;
import com.example.fitnestx.data.entity.ExerciseEntity;

import java.util.List;

public class ExerciseListAdapter extends RecyclerView.Adapter<ExerciseListAdapter.ViewHolder> {

    private List<ExerciseEntity> exercises;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(ExerciseEntity exercise);
    }

    public ExerciseListAdapter(List<ExerciseEntity> exercises, OnItemClickListener listener) {
        this.exercises = exercises;
        this.onItemClickListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView exerciseImage;
        TextView exerciseName;
        TextView exerciseDifficulty;
        ImageView arrowIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            exerciseImage = itemView.findViewById(R.id.ivExerciseImage);
            exerciseName = itemView.findViewById(R.id.tvExerciseName);
            exerciseDifficulty = itemView.findViewById(R.id.tvExerciseDifficulty);
            arrowIcon = itemView.findViewById(R.id.ivArrowIcon);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise_simple, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ExerciseEntity exercise = exercises.get(position);

        holder.exerciseName.setText(exercise.getName());

        String difficultyText = getDifficultyText(exercise.getDifficulty());
        holder.exerciseDifficulty.setText(difficultyText);

        // Load image using Glide
        if (exercise.getImageUrl() != null && !exercise.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(exercise.getImageUrl())
                    .placeholder(R.drawable.ic_exercise_placeholder)
                    .into(holder.exerciseImage);
        } else {
            holder.exerciseImage.setImageResource(R.drawable.ic_exercise_placeholder);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(exercise);
            }
        });
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    private String getDifficultyText(int difficulty) {
        switch (difficulty) {
            case 1: return "Dễ";
            case 2: return "Trung bình";
            case 3: return "Khó";
            default: return "Không xác định";
        }
    }

    public void updateData(List<ExerciseEntity> newExercises) {
        this.exercises.clear();
        this.exercises.addAll(newExercises);
        notifyDataSetChanged();
    }
}
