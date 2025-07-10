package com.example.fitnestx.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fitnestx.Helpers.SectionItem;
import com.example.fitnestx.R;
import com.example.fitnestx.data.entity.ExerciseEntity;
import com.example.fitnestx.viewmodel.ExerciseWithSessionStatus;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class SectionExerciseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<SectionItem> items;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onExerciseClick(int position, ExerciseEntity exercise);
    }

    public SectionExerciseAdapter(List<SectionItem> items, OnItemClickListener listener) {
        this.items = items;
        this.onItemClickListener = listener;
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerText;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            headerText = itemView.findViewById(R.id.tvHeader);
        }
    }

    public static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        ImageView exerciseImage;
        TextView exerciseName;

        TextView exerciseDifficulty;
        ImageView statusIcon;

        public ExerciseViewHolder(View itemView) {
            super(itemView);
            exerciseImage = itemView.findViewById(R.id.ivExerciseImage);
            exerciseName = itemView.findViewById(R.id.tvExerciseName);
            exerciseDifficulty = itemView.findViewById(R.id.tvExerciseDifficulty);
            statusIcon = itemView.findViewById(R.id.ivStatusIcon);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == SectionItem.TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_exercise_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_exercise, parent, false);
            return new ExerciseViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SectionItem item = items.get(position);

        if (item.getType() == SectionItem.TYPE_HEADER) {
            ((HeaderViewHolder) holder).headerText.setText(item.getHeaderTitle());
        } else {
            ExerciseWithSessionStatus ex = item.getExerciseWithStatus();
            ExerciseEntity exercise = ex.getExercise();
            boolean isMarked = ex.isMarked();
            ExerciseViewHolder viewHolder = (ExerciseViewHolder) holder;

            viewHolder.exerciseName.setText(exercise.getName());


            String difficultyText = getDifficultyText(exercise.getDifficulty());
            viewHolder.exerciseDifficulty.setText(difficultyText);

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference().child("Exercise/" + exercise.getImageUrl());

            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(holder.itemView.getContext())
                        .load(uri.toString())
                        .placeholder(R.drawable.ic_exercise_placeholder)
                        .into(((ExerciseViewHolder) holder).exerciseImage);
            }).addOnFailureListener(e -> {
                ((ExerciseViewHolder) holder).exerciseImage.setImageResource(R.drawable.ic_exercise_placeholder);
            });


            viewHolder.statusIcon.setImageResource(
                    isMarked ? R.drawable.ic_check : R.drawable.ic_arrow_right
            );

            viewHolder.itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onExerciseClick(position, exercise);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String getDifficultyText(int difficulty) {
        switch (difficulty) {
            case 1: return "Dễ";
            case 2: return "Trung bình";
            case 3: return "Khó";
            default: return "Không xác định";
        }
    }
    public void updateData(List<SectionItem> newItems) {
        this.items.clear();
        this.items.addAll(newItems);
        notifyDataSetChanged();
    }


}
