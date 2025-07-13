package com.example.fitnestx.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnestx.R;
import com.example.fitnestx.adapters.ExerciseListAdapter;
import com.example.fitnestx.data.entity.ExerciseEntity;
import com.example.fitnestx.data.repository.ExerciseRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ExerciseByMuscleGroupActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ExerciseListAdapter adapter;
    private ExerciseRepository exerciseRepository;
    private ImageButton btnBack;
    private TextView titleText;

    private int muscleGroupId;
    private String muscleGroupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_by_muscle_group);

        getIntentData();
        initViews();
        setupRecyclerView();
        loadExercises();
    }

    private void getIntentData() {
        muscleGroupId = getIntent().getIntExtra("muscleGroupId", -1);
        muscleGroupName = getIntent().getStringExtra("muscleGroupName");
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewExercises);
        btnBack = findViewById(R.id.btn_back);
        titleText = findViewById(R.id.title_text);

        exerciseRepository = new ExerciseRepository(this);

        btnBack.setOnClickListener(v -> finish());
        titleText.setText(muscleGroupName != null ? muscleGroupName : "Bài tập");
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ExerciseListAdapter(new ArrayList<>(), new ExerciseListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ExerciseEntity exercise) {
                // Chuyển sang IndividualExerciseDetailActivity thay vì DetailExerciseActivity
                Intent intent = new Intent(ExerciseByMuscleGroupActivity.this, IndividualExerciseDetailActivity.class);
                intent.putExtra(IndividualExerciseDetailActivity.EXTRA_EXERCISE_ID, exercise.getExerciseId());
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void loadExercises() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ExerciseEntity> allExercises = exerciseRepository.getAllExercises();
            List<ExerciseEntity> exercisesByMuscleGroup = new ArrayList<>();

            // Lọc ra các bài tập thuộc nhóm cơ này
            for (ExerciseEntity exercise : allExercises) {
                if (exercise.getMuscleGroupId() == muscleGroupId) {
                    exercisesByMuscleGroup.add(exercise);
                }
            }

            runOnUiThread(() -> {
                adapter.updateData(exercisesByMuscleGroup);
            });
        });
    }
}
