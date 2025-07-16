package com.example.fitnestx.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.example.fitnestx.R;
import com.example.fitnestx.data.entity.ExerciseEntity;
import com.example.fitnestx.data.repository.ExerciseRepository;
import com.example.fitnestx.fragments.TopMenuFragment;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IndividualExerciseDetailActivity extends AppCompatActivity {
    public static final String EXTRA_EXERCISE_ID = "exercise_id";
    private static final String TAG = "IndividualExerciseDetail";

    private PlayerView playerView;
    private ExoPlayer player;
    private ImageView closeButton;
    private ExerciseEntity exerciseEntity;
    private TextView exerciseTitle, exerciseDescription;
    private TextView exerciseGuideline;
    private ExerciseRepository exerciseRepository;

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_exercise_detail);

        exerciseRepository = new ExerciseRepository(this);

        // Khởi tạo views
        initViews();

        // Thiết lập UI và sự kiện
        setupUI();

        // Thêm TopMenuFragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_top_menu, new TopMenuFragment());
        transaction.commit();

        // Thiết lập ExoPlayer
        setupPlayer();
    }

    private void initViews() {
        playerView = findViewById(R.id.player_view);
        exerciseTitle = findViewById(R.id.exercise_title);
        exerciseDescription = findViewById(R.id.exercise_description);
        closeButton = findViewById(R.id.close_button);
        exerciseGuideline = findViewById(R.id.exercise_guideline);
    }

    @OptIn(markerClass = UnstableApi.class)
    private void setupPlayer() {
        // Khởi tạo ExoPlayer
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        // Tải video từ Firebase Storage
        try {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference videoRef = storage.getReference().child(exerciseEntity.getVideoURL());

            videoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                MediaItem mediaItem = MediaItem.fromUri(uri);
                player.setMediaItem(mediaItem);
                player.prepare();
                player.play();
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Lỗi lấy video URL từ Firebase: " + e.getMessage());
                Toast.makeText(this, "Không thể phát video", Toast.LENGTH_SHORT).show();
            });

            playerView.setShowFastForwardButton(true);
            playerView.setShowRewindButton(true);

            // Xử lý fullscreen
            playerView.setFullscreenButtonClickListener(isFullscreen -> {
                if (isFullscreen) {
                    getWindow().getDecorView().setSystemUiVisibility(
                            PlayerView.SYSTEM_UI_FLAG_FULLSCREEN |
                                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    );
                    playerView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                } else {
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    float pxHeight = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, 200f, getResources().getDisplayMetrics()
                    );
                    ViewGroup.LayoutParams layoutParams = playerView.getLayoutParams();
                    layoutParams.height = (int) pxHeight;
                    playerView.setLayoutParams(layoutParams);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error loading video: " + e.getMessage());
            Toast.makeText(this, "Không thể tải video", Toast.LENGTH_SHORT).show();
        }

        // Lắng nghe lỗi phát lại
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY) {
                    long duration = player.getDuration();
                    Log.d(TAG, "Duration: " + duration);
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                Log.e(TAG, "Player error: " + error.getMessage());
                Toast.makeText(IndividualExerciseDetailActivity.this,
                        "Lỗi phát video: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupUI() {
        // Lấy exercise ID từ Intent
        Intent intent = getIntent();
        int exerciseId = intent.getIntExtra(EXTRA_EXERCISE_ID, -1);

        if (exerciseId == -1) {
            Toast.makeText(this, "Không tìm thấy bài tập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load exercise data
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            exerciseEntity = exerciseRepository.getExerciseById(exerciseId);
            String description = exerciseRepository.GetDesByExId(exerciseId);
            String guideline = exerciseRepository.GetGuideByExId(exerciseId);

            if (exerciseEntity != null) {
                runOnUiThread(() -> {
                    exerciseTitle.setText(exerciseEntity.getName());
                    exerciseDescription.setText(description);
                    if (guideline != null && !guideline.isEmpty()) {
                        String formattedGuideline = guideline.replaceAll(" (?=\\d+\\.)", "\n");
                        exerciseGuideline.setText(formattedGuideline);
                    } else {
                        exerciseGuideline.setText("");
                    }
                });
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Không tìm thấy thông tin bài tập", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });

        // Sự kiện nút đóng
        closeButton.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null && player.isPlaying()) {
            player.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
