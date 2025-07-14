package com.example.fitnestx.adapters;

import static com.example.fitnestx.ui.PlanActivity.REQUEST_CODE_EXERCISE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fitnestx.R;
import com.example.fitnestx.data.entity.ExerciseEntity;
import com.example.fitnestx.data.entity.MuscleGroupEntity;
import com.example.fitnestx.data.entity.SessionExerciseEntity;
import com.example.fitnestx.data.entity.WorkoutPlanEntity;
import com.example.fitnestx.data.entity.WorkoutSessionEntity;
import com.example.fitnestx.data.repository.ExerciseRepository;
import com.example.fitnestx.data.repository.MuscleGroupRepository;
import com.example.fitnestx.data.repository.SessionExerciseRepository;
import com.example.fitnestx.data.repository.WorkoutSessionRepository;
import com.example.fitnestx.ui.ExcerciseActivity;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder>{
    private List<WorkoutSessionEntity> workSessionList;
    private SessionExerciseRepository sessionExerciseRepository;
    private WorkoutSessionRepository workoutSessionRepository;
    private ExerciseRepository exerciseRepository;
    private MuscleGroupRepository muscleGroupRepository;
    private Map<Integer, String> sessionImageMap = new HashMap<>();

    private Activity context;
    private Boolean ismarked = false;

    public GoalAdapter(List<WorkoutSessionEntity> goalList, Activity context, SessionExerciseRepository sessionExerciseRepository) {
        this.workSessionList = goalList;
        this.context = context;
        this.sessionExerciseRepository = sessionExerciseRepository;
    }
    public WorkoutSessionEntity getItem(int position) {
        return workSessionList.get(position);
    }
    public void updateItem(int position, WorkoutSessionEntity updatedSession) {
        workSessionList.set(position, updatedSession);
        notifyItemChanged(position);
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_plan, parent, false);
        workoutSessionRepository = new WorkoutSessionRepository(context);
        exerciseRepository = new ExerciseRepository(context);
        muscleGroupRepository = new MuscleGroupRepository(context);
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
            List<SessionExerciseEntity> sessionExercises = sessionExerciseRepository.getExercisesListBySessionId(workoutSession.getSessionId());
            boolean isMarked = sessionExercises.stream().allMatch(SessionExerciseEntity::isMarked);
            if (isMarked) {
                workoutSession.setCompleted(true);
                workoutSessionRepository.updateWorkoutSession(workoutSession);
                holder.checkIcon.setImageResource(workoutSession.getIsCompleted() ?
                        R.drawable.ic_check_filled : R.drawable.ic_check_empty);
            }

            //nhóm các nhóm cơ
            List<Integer> exerciseIds = sessionExerciseRepository.GetListIdExercsieBySessionId(workoutSession.getSessionId());
            List<ExerciseEntity> exercises = new ArrayList<>();
            for (int id : exerciseIds) {
                ExerciseEntity ex = exerciseRepository.getExerciseById(id);
                if (ex != null) exercises.add(ex);
            }

            List<MuscleGroupEntity> allMuscles = muscleGroupRepository.getListMuscleGroup();
            Map<Integer, MuscleGroupEntity> idToGroup = new HashMap<>();
            Map<Integer, Integer> childToParentMap = new HashMap<>();

            for (MuscleGroupEntity mg : allMuscles) {
                idToGroup.put(mg.getMuscleGroupId(), mg);
                if (mg.getParentId() != null) {
                    childToParentMap.put(mg.getMuscleGroupId(), mg.getParentId());
                }
            }

            Map<Integer, List<ExerciseEntity>> groupedByParent = new HashMap<>();
            Set<Integer> usedParentIds = new HashSet<>();
            Set<String> usedSpecs = new HashSet<>();
            for (ExerciseEntity ex : exercises) {
                int childId = ex.getMuscleGroupId();
                Integer parentId = childToParentMap.get(childId);
                if (parentId != null) {
                    groupedByParent.computeIfAbsent(parentId, k -> new ArrayList<>()).add(ex);
                    usedParentIds.add(parentId);
                } else {
                    groupedByParent.computeIfAbsent(childId, k -> new ArrayList<>()).add(ex);
                    usedParentIds.add(childId);
                }
            }
            StringBuilder specBuilder = new StringBuilder();
            for (Integer parentId : usedParentIds) {
                MuscleGroupEntity muscleGroupEntity = muscleGroupRepository.getMuscleGroupById(parentId);
                String spec = muscleGroupEntity.getSpec();
                if (usedSpecs.add(spec)) { // chỉ thêm nếu chưa tồn tại
                    specBuilder.append(spec).append(" And ");
                }
            }

            String spec = specBuilder.toString();
            spec = spec.substring(0, spec.length() - 5); // xóa " And " cuối cùng

            // Cập nhật UI trong main thread
            String finalSpec = spec;
            String image = "";
            if (!usedParentIds.isEmpty()) {
                int sessionId = workoutSession.getSessionId();

                if (sessionImageMap.containsKey(sessionId)) {
                    image = sessionImageMap.get(sessionId); // dùng lại ảnh đã có
                } else {
                    List<Integer> list = new ArrayList<>(usedParentIds);
                    Random rand = new Random();
                    int randomElement = list.get(rand.nextInt(list.size()));
                    MuscleGroupEntity muscleGroupEntity = muscleGroupRepository.getMuscleGroupById(randomElement);
                    image = muscleGroupEntity.getImage();
                    sessionImageMap.put(sessionId, image); // lưu lại ảnh
                }
            }



            String finalImage = image;
            new Handler(Looper.getMainLooper()).post(() -> {
                // CHỈ CẬP NHẬT UI ở đây
                Log.d("haha","onBindViewHolder:"+ finalImage);
                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                if (finalImage != null && !finalImage.isEmpty()) {
                    storageRef.child(finalImage).getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                Glide.with(holder.itemView.getContext())
                                        .load(uri.toString())
                                        .placeholder(R.drawable.ic_exercise_placeholder)
                                        .into(holder.iconImage);
                            })
                            .addOnFailureListener(exception -> {
                                // Nếu lỗi, dùng ảnh mặc định
                                holder.iconImage.setImageResource(R.drawable.ic_exercise_placeholder);
                            });
                } else {
                    holder.iconImage.setImageResource(R.drawable.ic_exercise_placeholder);
                }

                holder.descriptionText.setText("Tổng bài tập: " + total);
                holder.timeText.setText(finalSpec);
                holder.checkIcon.setImageResource(workoutSession.getIsCompleted() ?
                        R.drawable.ic_check_filled : R.drawable.ic_check_empty);

                // Đặt click listener ở đây để đúng thread
                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(context, ExcerciseActivity.class);
                    intent.putExtra("sessionId", workoutSession.getSessionId());
                    intent.putExtra("totalDay", total);
                    ((Activity) context).startActivityForResult(intent, REQUEST_CODE_EXERCISE);
                });
            });

        });






    }

    @Override
    public int getItemCount() {
        return workSessionList.size();
    }
    // Thêm vào cuối class GoalAdapter (trước dấu đóng ngoặc cuối cùng)
    public void updateData(List<WorkoutSessionEntity> newList) {
        this.workSessionList.clear();
        this.workSessionList.addAll(newList);
        notifyDataSetChanged(); // Gọi để RecyclerView render lại
    }

    static class GoalViewHolder extends RecyclerView.ViewHolder {
        TextView dayText, descriptionText, timeText;
        ImageView iconImage, checkIcon;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            dayText = itemView.findViewById(R.id.day_text);
            descriptionText = itemView.findViewById(R.id.description_text);
            timeText = itemView.findViewById(R.id.name_day);
            iconImage = itemView.findViewById(R.id.icon_image);
            checkIcon = itemView.findViewById(R.id.check_icon);
        }
    }
}
