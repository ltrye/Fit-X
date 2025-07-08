package com.example.fitnestx.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fitnestx.R;
import com.example.fitnestx.data.entity.MuscleGroupEntity;

import java.util.List;

public class MuscleGroupAdapter extends RecyclerView.Adapter<MuscleGroupAdapter.ViewHolder> {

    private List<MuscleGroupEntity> muscleGroups;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(MuscleGroupEntity muscleGroup);
    }

    public MuscleGroupAdapter(List<MuscleGroupEntity> muscleGroups, OnItemClickListener listener) {
        this.muscleGroups = muscleGroups;
        this.onItemClickListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView muscleGroupImage;
        TextView muscleGroupName;
        TextView muscleGroupSpec;
        ImageView arrowIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            muscleGroupImage = itemView.findViewById(R.id.ivMuscleGroupImage);
            muscleGroupName = itemView.findViewById(R.id.tvMuscleGroupName);
            muscleGroupSpec = itemView.findViewById(R.id.tvMuscleGroupSpec);
            arrowIcon = itemView.findViewById(R.id.ivArrowIcon);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_muscle_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MuscleGroupEntity muscleGroup = muscleGroups.get(position);

        holder.muscleGroupName.setText(muscleGroup.getName());
        holder.muscleGroupSpec.setText(muscleGroup.getSpec());

        // Load image using Glide
        if (muscleGroup.getImage() != null && !muscleGroup.getImage().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(muscleGroup.getImage())
                    .placeholder(R.drawable.ic_exercise_placeholder)
                    .into(holder.muscleGroupImage);
        } else {
            holder.muscleGroupImage.setImageResource(R.drawable.ic_exercise_placeholder);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(muscleGroup);
            }
        });
    }

    @Override
    public int getItemCount() {
        return muscleGroups.size();
    }

    public void updateData(List<MuscleGroupEntity> newMuscleGroups) {
        this.muscleGroups.clear();
        this.muscleGroups.addAll(newMuscleGroups);
        notifyDataSetChanged();
    }
}
