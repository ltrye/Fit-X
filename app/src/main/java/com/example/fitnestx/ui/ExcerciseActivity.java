package com.example.fitnestx.ui;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnestx.Helpers.SectionItem;
import com.example.fitnestx.R;
import com.example.fitnestx.adapters.ImageExerciseAdapter;
import com.example.fitnestx.adapters.SectionExerciseAdapter;
import com.example.fitnestx.data.entity.ExerciseEntity;
import com.example.fitnestx.data.entity.MuscleGroupEntity;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcerciseActivity extends AppCompatActivity {
    private SectionExerciseAdapter sectionExerciseAdapter;
    private RecyclerView recyclerView;
    ImageButton btnBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercises);

        recyclerView = findViewById(R.id.recyclerViewExercises);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        //Grid image Exercise
        setupActionRecyclerView();
        // Danh sách mẫu (bạn có thể lấy từ DB nếu cần)
        List<ExerciseEntity> exercises = new ArrayList<>();
        exercises.add(new ExerciseEntity(1, "Warm Up", "Khởi động cơ thể", "", 1, false, 1, "Ngực", false));
        exercises.add(new ExerciseEntity(2, "Jumping Jack", "Nhảy tại chỗ", "", 1, false, 2, "Ngực", false));
        exercises.add(new ExerciseEntity(3, "Skipping", "Nhảy dây", "", 1, false, 2, "Ngực", false));
        exercises.add(new ExerciseEntity(4, "Incline Push-Ups", "Hít đất nghiêng", "", 2, false, 2, "Ngực", false));
        exercises.add(new ExerciseEntity(5, "Push-Ups", "Hít đất thường", "", 2, false, 3, "Ngực", false));
        exercises.add(new ExerciseEntity(6, "Shoulder Tap", "Chạm vai", "", 1, false, 3, "Vai", false));
        exercises.add(new ExerciseEntity(7, "Lateral Raise", "Nâng vai ngang", "", 2, false, 3, "Vai", false));

        // Giả dữ liệu nhóm cơ
        List<MuscleGroupEntity> muscleGroups = new ArrayList<>();
        muscleGroups.add(new MuscleGroupEntity(1, "Ngực", 24, null));
        muscleGroups.add(new MuscleGroupEntity(2, "Vai", 24, null));

        // Tạo section từ bài tập theo parent group
        List<SectionItem> sectionItems = groupExercisesByParentGroup(exercises, muscleGroups);


        // Adapter
        sectionExerciseAdapter = new SectionExerciseAdapter(sectionItems, (pos, exercise) -> {
            sectionExerciseAdapter.toggleExerciseStatus(pos);
        });

        recyclerView.setAdapter(sectionExerciseAdapter);
    }
    private List<SectionItem> groupExercisesByParentGroup(List<ExerciseEntity> exercises, List<MuscleGroupEntity> muscleGroups) {
        // Tạo map id -> MuscleGroupEntity
        Map<Integer, MuscleGroupEntity> groupMap = new HashMap<>();
        for (MuscleGroupEntity group : muscleGroups) {
            groupMap.put(group.getMuscleGroupId(), group);
        }

        // Group bài tập theo parent group id
        Map<Integer, List<ExerciseEntity>> grouped = new HashMap<>();
        for (ExerciseEntity ex : exercises) {
            MuscleGroupEntity group = groupMap.get(ex.getMuscleGroupId());
            int parentId = (group != null && group.getParentId() != null)
                    ? group.getParentId()
                    : ex.getMuscleGroupId();

            if (!grouped.containsKey(parentId)) {
                grouped.put(parentId, new ArrayList<>());
            }
            grouped.get(parentId).add(ex);
        }

        // Tạo list SectionItem
        List<SectionItem> sectionItems = new ArrayList<>();
        for (Map.Entry<Integer, List<ExerciseEntity>> entry : grouped.entrySet()) {
            String groupName = groupMap.containsKey(entry.getKey())
                    ? groupMap.get(entry.getKey()).getName()
                    : "Nhóm khác";

            sectionItems.add(new SectionItem(groupName)); // Header
            for (ExerciseEntity ex : entry.getValue()) {
                sectionItems.add(new SectionItem(ex));
            }
        }

        return sectionItems;
    }
    private void setupActionRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view_imageExercise);

        // Tạo danh sách icons
        List<Integer> icons = Arrays.asList(
                R.drawable.ic_refresh,
                R.drawable.ic_share
        );

        // Setup GridLayoutManager với 2 cột ngang
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2,
                GridLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        // Setup adapter
        ImageExerciseAdapter adapter = new ImageExerciseAdapter(icons, new ImageExerciseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, int iconRes) {
//                handleActionClick(position, iconRes);
            }
        });

        recyclerView.setAdapter(adapter);
    }
}
