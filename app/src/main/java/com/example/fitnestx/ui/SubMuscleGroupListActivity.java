package com.example.fitnestx.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnestx.R;
import com.example.fitnestx.adapters.MuscleGroupAdapter;
import com.example.fitnestx.data.entity.MuscleGroupEntity;
import com.example.fitnestx.data.repository.MuscleGroupRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class SubMuscleGroupListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MuscleGroupAdapter adapter;
    private MuscleGroupRepository muscleGroupRepository;
    private ImageButton btnBack;
    private TextView titleText;

    private int parentMuscleGroupId;
    private String parentMuscleGroupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muscle_group_list);

        getIntentData();
        initViews();
        setupRecyclerView();
        loadSubMuscleGroups();

    }

    private void getIntentData() {
        parentMuscleGroupId = getIntent().getIntExtra("parentMuscleGroupId", -1);
        parentMuscleGroupName = getIntent().getStringExtra("parentMuscleGroupName");
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewMuscleGroups);
        btnBack = findViewById(R.id.btn_back);
        titleText = findViewById(R.id.title_text);

        muscleGroupRepository = new MuscleGroupRepository(this);

        btnBack.setOnClickListener(v -> finish());
        titleText.setText(parentMuscleGroupName != null ? parentMuscleGroupName : "Nhóm cơ nhỏ");
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MuscleGroupAdapter(new ArrayList<>(), new MuscleGroupAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MuscleGroupEntity muscleGroup) {
                Intent intent = new Intent(SubMuscleGroupListActivity.this, ExerciseByMuscleGroupActivity.class);
                intent.putExtra("muscleGroupId", muscleGroup.getMuscleGroupId());
                intent.putExtra("muscleGroupName", muscleGroup.getName());
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void loadSubMuscleGroups() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<MuscleGroupEntity> allMuscleGroups = muscleGroupRepository.getListMuscleGroup();
            List<MuscleGroupEntity> subMuscleGroups = new ArrayList<>();

            // Lọc ra các nhóm cơ nhỏ có parentId = parentMuscleGroupId
            for (MuscleGroupEntity muscleGroup : allMuscleGroups) {
                if (muscleGroup.getParentId() != null &&
                        muscleGroup.getParentId().equals(parentMuscleGroupId)) {
                    subMuscleGroups.add(muscleGroup);
                }
            }

            runOnUiThread(() -> {
                adapter.updateData(subMuscleGroups);
            });
        });
    }
}
