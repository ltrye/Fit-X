package com.example.fitnestx.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

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
import com.example.fitnestx.data.entity.SessionExerciseEntity;
import com.example.fitnestx.data.repository.ExerciseRepository;
import com.example.fitnestx.data.repository.MuscleGroupRepository;
import com.example.fitnestx.data.repository.SessionExerciseRepository;
import com.example.fitnestx.data.repository.WorkoutSessionRepository;
import com.example.fitnestx.viewmodel.ExerciseWithSessionStatus;


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
    TextView Spec, Date,exerciseCount;
    private ExerciseRepository exerciseRepository;
    private SessionExerciseRepository sessionExerciseRepository;
    private SessionExerciseEntity sessionExerciseEntity;
    private ExerciseWithSessionStatus exerciseWithSessionStatus;
    private MuscleGroupRepository muscleGroupRepository;
    private WorkoutSessionRepository workoutSessionRepository;

    String spec, date;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercises);

        exerciseRepository = new ExerciseRepository(this);
        sessionExerciseRepository = new SessionExerciseRepository(this);
        muscleGroupRepository = new MuscleGroupRepository(this);
        workoutSessionRepository = new WorkoutSessionRepository(this);

        recyclerView = findViewById(R.id.recyclerViewExercises);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        exerciseCount = findViewById(R.id.ExerciseCount);

        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            Intent intent = getIntent();
            int sessionId = intent.getIntExtra("sessionId", -1);
            resultIntent.putExtra("sessionId", sessionId);  // sessionId bạn đã có sẵn
            setResult(Activity.RESULT_OK, resultIntent);

            finish();
        });


        Spec = findViewById(R.id.Spec);
        Date = findViewById(R.id.Date);

//        setupActionRecyclerView();
        reloadExercises();


    }
    @Override
    protected void onResume() {
        super.onResume();
        reloadExercises(); // Tạo thêm hàm này để tái nạp danh sách từ DB
    }
    private void reloadExercises() {
        Intent intent = getIntent();
        int sessionId = intent.getIntExtra("sessionId", -1);
        int total = intent.getIntExtra("totalDay", -1);

        Executors.newSingleThreadExecutor().execute(() -> {
            date = workoutSessionRepository.getWorkoutSessionById(sessionId).getDate();
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

            spec = specBuilder.toString();
            spec = spec.substring(0, spec.length() - 5); // xóa " And " cuối cùng

            List<SectionItem> sectionItems = new ArrayList<>();
            for (Integer parentId : groupedByParent.keySet()) {
                MuscleGroupEntity group = idToGroup.get(parentId);
                String title = (group != null) ? group.getName() : "Nhóm khác";

                sectionItems.add(new SectionItem(title)); // Header
                for (ExerciseEntity ex : groupedByParent.get(parentId)) {
                    sessionExerciseEntity = sessionExerciseRepository.getSessionExercise(sessionId,ex.getExerciseId());
                    exerciseWithSessionStatus = new ExerciseWithSessionStatus(ex,sessionExerciseEntity.isMarked());
                    sectionItems.add(new SectionItem(exerciseWithSessionStatus));
                }
            }

            List<ExerciseWithSessionStatus> exerciseItems = new ArrayList<>();
            for (SectionItem item : sectionItems) {
                if (item.getType() == SectionItem.TYPE_ITEM) {
                    exerciseItems.add(item.getExerciseWithStatus());
                }
            }

            runOnUiThread(() -> {
                Spec.setText(spec);
                Date.setText(date);
                exerciseCount.setText(total+" Exercises");
                if (sectionExerciseAdapter == null) {
                    // Adapter chưa được tạo, tạo mới và gán vào RecyclerView
                    sectionExerciseAdapter = new SectionExerciseAdapter(sectionItems, new SectionExerciseAdapter.OnItemClickListener() {
                        @Override
                        public void onExerciseClick(int position, ExerciseEntity exercise) {
                            int actualIndex = -1;
                            for (int i = 0; i < exerciseItems.size(); i++) {
                                if (exerciseItems.get(i).getExercise().getExerciseId() == exercise.getExerciseId()) {
                                    actualIndex = i;
                                    break;
                                }
                            }
                            if (actualIndex != -1) {
                                Intent detailIntent = new Intent(ExcerciseActivity.this, DetailExerciseActivity.class);
                                detailIntent.putExtra("sessionId", sessionId);
                                detailIntent.putExtra("currentIndex", actualIndex);
                                detailIntent.putExtra("exerciseList", new ArrayList<>(exerciseItems)); // must be Serializable
                                startActivity(detailIntent);
                            }
                        }
                    });
                    recyclerView.setAdapter(sectionExerciseAdapter);
                } else {
                    // Adapter đã có, chỉ cần update dữ liệu
                    sectionExerciseAdapter.updateData(sectionItems);
                }
            });
        });
    }

//     private void setupActionRecyclerView() {
//        RecyclerView recyclerView = findViewById(R.id.recycler_view_imageExercise);
//
//        // Tạo danh sách icons
//        List<Integer> icons = Arrays.asList(
//                R.drawable.ic_refresh,
//                R.drawable.ic_share
//        );
//
//        // Setup GridLayoutManager với 2 cột ngang
//        GridLayoutManager layoutManager = new GridLayoutManager(this, 2,
//                GridLayoutManager.HORIZONTAL, false);
//        recyclerView.setLayoutManager(layoutManager);
//
//        // Setup adapter
//        ImageExerciseAdapter adapter = new ImageExerciseAdapter(icons, new ImageExerciseAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(int position, int iconRes) {
////                handleActionClick(position, iconRes);
//            }
//        });
//
//        recyclerView.setAdapter(adapter);
//    }

}
