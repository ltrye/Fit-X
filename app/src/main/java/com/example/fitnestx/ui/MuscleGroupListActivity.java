package com.example.fitnestx.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
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

public class MuscleGroupListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MuscleGroupAdapter adapter;
    private MuscleGroupRepository muscleGroupRepository;
    private ImageButton btnBack;
    private TextView titleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muscle_group_list);

        initViews();
        setupRecyclerView();
        loadMuscleGroups();

    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewMuscleGroups);
        btnBack = findViewById(R.id.btn_back);
        titleText = findViewById(R.id.title_text);

        muscleGroupRepository = new MuscleGroupRepository(this);

        btnBack.setOnClickListener(v -> finish());
        titleText.setText("Nhóm cơ");
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MuscleGroupAdapter(new ArrayList<>(), new MuscleGroupAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MuscleGroupEntity muscleGroup) {
                Intent intent = new Intent(MuscleGroupListActivity.this, SubMuscleGroupListActivity.class);
                intent.putExtra("parentMuscleGroupId", muscleGroup.getMuscleGroupId());
                intent.putExtra("parentMuscleGroupName", muscleGroup.getName());
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void loadMuscleGroups() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<MuscleGroupEntity> allMuscleGroups = muscleGroupRepository.getListMuscleGroup();
            List<MuscleGroupEntity> parentMuscleGroups = new ArrayList<>();

            // Lọc ra các nhóm cơ bảng (parentId == null)
            for (MuscleGroupEntity muscleGroup : allMuscleGroups) {
                if (muscleGroup.getParentId() == null) {
                    parentMuscleGroups.add(muscleGroup);
                }
            }

            runOnUiThread(() -> {
                adapter.updateData(parentMuscleGroups);
            });
        });
    }
}
