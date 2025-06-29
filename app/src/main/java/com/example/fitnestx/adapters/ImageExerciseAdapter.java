package com.example.fitnestx.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnestx.R;

import java.util.List;

public class ImageExerciseAdapter extends RecyclerView.Adapter<ImageExerciseAdapter.ViewHolder> {
    private List<Integer> icons;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position, int iconRes);
    }

    public ImageExerciseAdapter(List<Integer> icons, OnItemClickListener listener) {
        this.icons = icons;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.imageView.setImageResource(icons.get(position));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position, icons.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return icons.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_action);
        }
    }
}
