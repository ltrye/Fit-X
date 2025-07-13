package com.example.fitnestx.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

import com.example.fitnestx.R;
import com.example.fitnestx.data.entity.ExerciseEntity;
import com.example.fitnestx.data.entity.SessionExerciseEntity;
import com.example.fitnestx.data.entity.WorkoutPlanEntity;
import com.example.fitnestx.data.entity.WorkoutSessionEntity;
import com.example.fitnestx.data.repository.ExerciseRepository;
import com.example.fitnestx.data.repository.SessionExerciseRepository;
import com.example.fitnestx.data.repository.WorkoutPlanRepository;
import com.example.fitnestx.data.repository.WorkoutSessionRepository;
import com.example.fitnestx.fragments.TopMenuFragment;
import com.example.fitnestx.viewmodel.ExerciseWithSessionStatus;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetailExerciseActivity extends AppCompatActivity {
    public static final String EXTRA_EXERCISE_ID = "exercise_id";

    public static final String EXTRA_EXERCISE = "HAHA";
    private static final String TAG = "ExoPlayerDebug";
//    public static final String EXTRA_EXERCISE_ID = "exercise_id";
    private List<ExerciseWithSessionStatus> exerciseList;
    private int currentIndex;
    private PlayerView playerView;
    private ExoPlayer player;
    private ImageView closeButton;
    private Button skipButton,nextButton;
    private  ExerciseEntity exerciseEntity;
    private TextView exerciseTitle, exerciseDescription,note;
    private TextView exerciseGuideline;
    private ExerciseRepository exerciseRepository;
    private SessionExerciseRepository sessionExerciseRepository;
    private SessionExerciseEntity sessionExerciseEntity;
    private WorkoutPlanRepository workoutPlanRepository;
    private WorkoutSessionRepository workoutSessionRepository;

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_exercise);
        exerciseRepository = new ExerciseRepository(this);
        sessionExerciseRepository = new SessionExerciseRepository(this);
        workoutPlanRepository = new WorkoutPlanRepository(this);
        workoutSessionRepository = new WorkoutSessionRepository(this);
        // Lấy dữ liệu từ Intent
        Intent intent = getIntent();
        currentIndex = intent.getIntExtra("currentIndex", 0);
        exerciseList = (List<ExerciseWithSessionStatus>) intent.getSerializableExtra("exerciseList");


        // Khởi tạo views
        initViews();
        // Thiết lập UI và sự kiện
        setupUI();
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
        skipButton = findViewById(R.id.skip_button);
        nextButton = findViewById(R.id.next_button);
        note = findViewById(R.id.exercise_note);
        exerciseGuideline = findViewById(R.id.exercise_guideline);

    }

    @OptIn(markerClass = UnstableApi.class)
    private void setupPlayer() {
        // Khởi tạo ExoPlayer
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        // Tải video từ tài nguyên
        try {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference videoRef = storage.getReference().child(exerciseEntity.getVideoURL());

            // 👇 LẤY URL HTTP TỪ FIREBASE
            videoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                MediaItem mediaItem = MediaItem.fromUri(uri);
                player.setMediaItem(mediaItem);
                player.prepare();
                player.play();
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Lỗi lấy video URL từ Firebase: " + e.getMessage());
                Log.d("haha",exerciseEntity.getName());
                Log.d("haha",exerciseEntity.getImageUrl());
                Toast.makeText(this, "Không thể phát video", Toast.LENGTH_SHORT).show();
            });
//            MediaItem mediaItem = MediaItem.fromUri(String.valueOf(videoRef));
//            player.setMediaItem(mediaItem);
//            player.prepare();
//            player.play();
            playerView.setShowFastForwardButton(true);
            playerView.setShowRewindButton(true);


            playerView.setFullscreenButtonClickListener(isFullscreen -> {
                if (isFullscreen) {
                    getWindow().getDecorView().setSystemUiVisibility(PlayerView.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                    playerView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                    findViewById(R.id.button_container).setVisibility(View.GONE);
                } else {
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    float pxheight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,200f,getResources().getDisplayMetrics());
                    ViewGroup.LayoutParams layoutParams = playerView.getLayoutParams();
                    layoutParams.height = (int)pxheight;
                    playerView.setLayoutParams(layoutParams);
                    findViewById(R.id.button_container).setVisibility(View.VISIBLE);
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
                    Log.d("EXOPLAYER", "Duration: " + duration); // duration phải > 0
                }
                if (state == Player.STATE_ENDED) {
                    runOnUiThread(() -> {
                        nextButton.setEnabled(true);
                        nextButton.setAlpha(1.0f); // hiện rõ nút Next
                    });
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                Log.e(TAG, "Player error: " + error.getMessage());
                Toast.makeText(DetailExerciseActivity.this, "Lỗi phát video: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OptIn(markerClass = UnstableApi.class)
    private void setupUI() {
        // Lấy dữ liệu từ Intent
        nextButton.setEnabled(false);
        nextButton.setAlpha(0.5f); // làm mờ

        Intent intent = getIntent();
        ExerciseWithSessionStatus currentExercise = exerciseList.get(currentIndex);
        int exerciseId = currentExercise.getExercise().getExerciseId();
        int sessionId = getIntent().getIntExtra("sessionId",-1);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            exerciseEntity = exerciseRepository.getExerciseById(exerciseId);
            String des = exerciseRepository.GetDesByExId(exerciseId);
            String guideline = exerciseRepository.GetGuideByExId(exerciseId);
            WorkoutSessionEntity workoutSessionEntity = workoutSessionRepository.getWorkoutSessionById(sessionId);
            WorkoutPlanEntity workoutPlanEntity = workoutPlanRepository.getWorkoutPlanById(workoutSessionEntity.getPlanId());
            if (exerciseEntity != null) {
                runOnUiThread(() -> {
                    // Cập nhật UI ở đây
                    exerciseTitle.setText(exerciseEntity != null ? exerciseEntity.getName() : "Tên bài tập (Exercise)");
                    exerciseDescription.setText(des);
                    if (guideline != null && !guideline.isEmpty()) {
                        String formattedGuideline = guideline.replaceAll(" (?=\\d+\\.)", "\n");
                        exerciseGuideline.setText(formattedGuideline);
                    } else {
                        exerciseGuideline.setText("");
                    }
                    String noteText = workoutPlanEntity.getNote();
                    if (noteText == null || noteText.isEmpty()) {
                        note.setText(""); // hoặc "Không có ghi chú"
                        note.setBackground(null); // Xóa background nếu cần
                    } else {
                        note.setText(noteText);

                    }

                });
            }
        });


        // Sự kiện nút đóng
        closeButton.setOnClickListener(v -> finish());

        // Sự kiện nút Skip
        skipButton.setOnClickListener(v -> {
        ExecutorService executorSkip = Executors.newSingleThreadExecutor();

            executorSkip.execute(() -> {
        // Cập nhật isMarked = true trong DB
          sessionExerciseEntity = sessionExerciseRepository.getSessionExercise(sessionId,exerciseId);

          sessionExerciseEntity.setMarked(true);
          sessionExerciseRepository.updateSessionExercise(sessionExerciseEntity);

        // Trở lại UI thread để kết thúc activity
        runOnUiThread(() -> {
            Toast.makeText(this, "Đã đánh dấu hoàn thành", Toast.LENGTH_SHORT).show();
            finish(); // Quay lại màn hình trước
        });
    });
});
        nextButton.setOnClickListener(v -> {
            ExecutorService executor1 = Executors.newSingleThreadExecutor();
            executor1.execute(() -> {
                SessionExerciseEntity sessionExerciseEntity =
                        sessionExerciseRepository.getSessionExercise(sessionId, exerciseList.get(currentIndex).getExercise().getExerciseId());

                sessionExerciseEntity.setMarked(true);
                sessionExerciseRepository.updateSessionExercise(sessionExerciseEntity);

                runOnUiThread(() -> {
                    if (currentIndex + 1 < exerciseList.size()) {
                        Intent nextIntent = new Intent(DetailExerciseActivity.this, DetailExerciseActivity.class);
                        nextIntent.putExtra("currentIndex", currentIndex + 1);
                        nextIntent.putExtra("exerciseList", (java.io.Serializable) exerciseList);
                        nextIntent.putExtra("sessionId", sessionId);
                        startActivity(nextIntent);
                        finish();
                    } else {
                        Toast.makeText(this, "Đã hoàn thành tất cả bài tập", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            });
        });




    }


    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.setPlayWhenReady(true); // Tiếp tục phát video
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null && player.isPlaying()) {
            player.setPlayWhenReady(false); // Tạm dừng video
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