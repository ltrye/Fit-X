package com.example.fitnestx.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnestx.R;
import com.example.fitnestx.data.entity.WorkoutPlanEntity;
import com.example.fitnestx.data.entity.WorkoutSessionEntity;
import com.example.fitnestx.data.repository.SessionExerciseRepository;
import com.example.fitnestx.data.repository.WorkoutSessionRepository;

import java.util.List;
import java.util.concurrent.Executors;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder>{
    private List<WorkoutSessionEntity> workSessionList;
    private SessionExerciseRepository sessionExerciseRepository;
    private Context context;


    public GoalAdapter(List<WorkoutSessionEntity> goalList, Context context, SessionExerciseRepository sessionExerciseRepository) {
        this.workSessionList = goalList;
        this.context = context;
        this.sessionExerciseRepository = sessionExerciseRepository;
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_plan, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        WorkoutSessionEntity workoutSession = workSessionList.get(position);

        holder.dayText.setText(workoutSession.getDate());
        holder.descriptionText.setText("Đang tải...");

        // Query DB in background
        Executors.newSingleThreadExecutor().execute(() -> {
            // Truy vấn DB trong background thread
            int total = sessionExerciseRepository.TotalSessionExerciseById(workoutSession.getSessionId());

            // Cập nhật UI trong main thread
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                holder.descriptionText.setText("Tổng bài tập: " + total);
            });
        });

        // Set icon based on day
        if (workoutSession.getDate().contains("Ngày 1")) {
            holder.iconImage.setImageResource(R.drawable.ic_exercise);
        } else {
            holder.iconImage.setImageResource(R.drawable.ic_study);
        }
        holder.itemView.setOnClickListener(v -> {
            // Tạo Intent để mở DetailActivity
            android.content.Intent intent = new android.content.Intent(context, com.example.fitnestx.ui.ExcerciseActivity.class);
            // Truyền sessionId sang DetailActivity
            intent.putExtra("sessionId", workoutSession.getSessionId());
            context.startActivity(intent);
        });
//        // Handle completion status
//        holder.checkIcon.setImageResource(goal.getIsCompleted() ?
//                R.drawable.ic_check_filled : R.drawable.ic_check_empty);
//
//        holder.checkIcon.setOnClickListener(v -> {
//            goal.setCompleted(!goal.getIsCompleted());
//            notifyItemChanged(position);
//        });
    }

    @Override
    public int getItemCount() {
        return workSessionList.size();
    }

    static class GoalViewHolder extends RecyclerView.ViewHolder {
        TextView dayText, descriptionText, timeText;
        ImageView iconImage, checkIcon;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            dayText = itemView.findViewById(R.id.day_text);
            descriptionText = itemView.findViewById(R.id.description_text);
            timeText = itemView.findViewById(R.id.time_text);
            iconImage = itemView.findViewById(R.id.icon_image);
            checkIcon = itemView.findViewById(R.id.check_icon);
        }
    }
}
