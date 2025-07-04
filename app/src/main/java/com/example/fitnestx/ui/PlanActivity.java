package com.example.fitnestx.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnestx.R;
import com.example.fitnestx.adapters.GoalAdapter;
import com.example.fitnestx.data.entity.WorkoutSessionEntity;
import com.example.fitnestx.data.repository.SessionExerciseRepository;
import com.example.fitnestx.data.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

public class PlanActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private GoalAdapter goalAdapter;
    private List<WorkoutSessionEntity> goalList;
    private TextView greetingText;
    private SessionExerciseRepository sessionExerciseRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan);
        sessionExerciseRepository = new SessionExerciseRepository(this);
        initViews();
        setupRecyclerView();
        loadGoals();
    }

    private void initViews() {
        greetingText = findViewById(R.id.greeting_text);
        recyclerView = findViewById(R.id.recycler_view);

        // Set greeting text
        greetingText.setText("Hello, ten user");
    }

    private void setupRecyclerView() {
        goalList = new ArrayList<>();
        goalAdapter = new GoalAdapter(goalList, this, sessionExerciseRepository);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(goalAdapter);
    }

    private void loadGoals() {
        // Sample data matching your image
        goalList.add(new WorkoutSessionEntity(1, 1, "Ngày 1",1,false ));
        goalList.add(new WorkoutSessionEntity(2, 1, "Ngày 2", 1,false));
        goalList.add(new WorkoutSessionEntity(3, 1, "Ngày 3", 1,false));

        goalAdapter.notifyDataSetChanged();
    }
}
