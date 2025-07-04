package com.example.fitnestx.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.example.fitnestx.data.repository.ExerciseRepository;
import com.example.fitnestx.data.repository.MuscleGroupRepository;
import com.example.fitnestx.data.repository.SessionExerciseRepository;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

public class ExcerciseActivity extends AppCompatActivity {
    private SectionExerciseAdapter sectionExerciseAdapter;
    private RecyclerView recyclerView;
    ImageButton btnBack;
    private ExerciseRepository exerciseRepository;
    private SessionExerciseRepository sessionExerciseRepository;
    private MuscleGroupRepository muscleGroupRepository;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercises);

        exerciseRepository = new ExerciseRepository(this);
        sessionExerciseRepository = new SessionExerciseRepository(this);
        muscleGroupRepository = new MuscleGroupRepository(this);

        recyclerView = findViewById(R.id.recyclerViewExercises);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        setupActionRecyclerView();

        Intent intent = getIntent();
        int sessionId = intent.getIntExtra("sessionId", -1);

        // Chạy trong background thread
        Executors.newSingleThreadExecutor().execute(() -> {
            // 1. Load tất cả dữ liệu
            List<Integer> exerciseIds = sessionExerciseRepository.GetListIdExercsieBySessionId(sessionId);
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

            // 2. Group theo parentId thực sự
            Map<Integer, List<ExerciseEntity>> groupedByParent = new HashMap<>();
            Set<Integer> usedParentIds = new HashSet<>();

            for (ExerciseEntity ex : exercises) {
                int childId = ex.getMuscleGroupId();
                Integer parentId = childToParentMap.get(childId);

                if (parentId != null) {
                    groupedByParent.computeIfAbsent(parentId, k -> new ArrayList<>()).add(ex);
                    usedParentIds.add(parentId);
                } else {
                    // Nếu không có parent thì group theo chính nó
                    groupedByParent.computeIfAbsent(childId, k -> new ArrayList<>()).add(ex);
                    usedParentIds.add(childId);
                }
            }

            // 3. Tạo section items
            List<SectionItem> sectionItems = new ArrayList<>();
            for (Integer parentId : groupedByParent.keySet()) {
                MuscleGroupEntity group = idToGroup.get(parentId);
                String title = (group != null) ? group.getName() : "Nhóm khác";

                sectionItems.add(new SectionItem(title)); // Header
                for (ExerciseEntity ex : groupedByParent.get(parentId)) {
                    sectionItems.add(new SectionItem(ex));
                }
            }


            // Cập nhật UI trên Main Thread
            runOnUiThread(() -> {
                sectionExerciseAdapter = new SectionExerciseAdapter(sectionItems, new SectionExerciseAdapter.OnItemClickListener() {
                    @Override
                    public void onExerciseClick(int position, ExerciseEntity exercise) {
                        Intent detailIntent = new Intent(ExcerciseActivity.this, DetailExerciseActivity.class);
                        detailIntent.putExtra(DetailExerciseActivity.EXTRA_EXERCISE_ID, exercise.getExerciseId());
                        startActivity(detailIntent);
                        sectionExerciseAdapter.toggleExerciseStatus(position);
                    }
                });

                recyclerView.setAdapter(sectionExerciseAdapter);
            });
        });
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
