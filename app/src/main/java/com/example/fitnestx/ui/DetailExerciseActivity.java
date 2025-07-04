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
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

import com.example.fitnestx.R;
import com.example.fitnestx.data.entity.ExerciseEntity;
import com.example.fitnestx.data.repository.ExerciseRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetailExerciseActivity extends AppCompatActivity {
    public static final String EXTRA_EXERCISE_ID = "exercise_id";

    public static final String EXTRA_EXERCISE = "HAHA";
    private static final String TAG = "ExoPlayerDebug";

    private PlayerView playerView;
    private ExoPlayer player;
    private ImageView closeButton;
    private Button skipButton;
    private  ExerciseEntity exerciseEntity;
    private TextView exerciseTitle, exerciseDescription;
    private ExerciseRepository exerciseRepository;


    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_exercise);
        exerciseRepository = new ExerciseRepository(this);
        // Khởi tạo views
        initViews();

        // Thiết lập ExoPlayer
        setupPlayer();

        // Thiết lập UI và sự kiện
        setupUI();

    }

    private void initViews() {
        playerView = findViewById(R.id.player_view);
        exerciseTitle = findViewById(R.id.exercise_title);
        exerciseDescription = findViewById(R.id.exercise_description);
        closeButton = findViewById(R.id.close_button);
        skipButton = findViewById(R.id.skip_button);
//        nextButton = findViewById(R.id.next_button);

    }

    @OptIn(markerClass = UnstableApi.class)
    private void setupPlayer() {
        // Khởi tạo ExoPlayer
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        // Tải video từ tài nguyên
        try {
            MediaItem mediaItem = MediaItem.fromUri("android.resource://" + getPackageName() + "/" + R.raw.tiktok);
            player.setMediaItem(mediaItem);
            player.prepare();
            player.play();
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
            public void onPlayerError(PlaybackException error) {
                Log.e(TAG, "Player error: " + error.getMessage());
                Toast.makeText(DetailExerciseActivity.this, "Lỗi phát video: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OptIn(markerClass = UnstableApi.class)
    private void setupUI() {
        // Lấy dữ liệu từ Intent
        Intent intent = getIntent();
        int exerciseId = getIntent().getIntExtra(EXTRA_EXERCISE_ID, -1);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
             exerciseEntity = exerciseRepository.getExerciseById(exerciseId);
             String des = exerciseRepository.GetDesByExId(exerciseId);
            if (exerciseEntity != null) {
                runOnUiThread(() -> {
                    // Cập nhật UI ở đây
                    exerciseTitle.setText(exerciseEntity != null ? exerciseEntity.getName() : "Tên bài tập (Exercise)");
                    exerciseDescription.setText(des);
                    Log.d("hehe", "Name: " + exerciseEntity.toString());
                    Log.d("hehe","des: "+ des);
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
         exerciseEntity.setMarked(true);
        exerciseRepository.updateExercise(exerciseEntity);

        // Trở lại UI thread để kết thúc activity
        runOnUiThread(() -> {
            Toast.makeText(this, "Đã đánh dấu hoàn thành", Toast.LENGTH_SHORT).show();
            finish(); // Quay lại màn hình trước
        });
    });
});
//
//        // Sự kiện nút Next
//        nextButton.setOnClickListener(v -> {
//            // TODO: Thêm logic để chuyển sang video/bài tập tiếp theo
//            Toast.makeText(this, "Chuyển sang bài tập tiếp theo", Toast.LENGTH_SHORT).show();
//            // Ví dụ: MediaItem nextMediaItem = MediaItem.fromUri("NEW_VIDEO_URI");
//            // player.setMediaItem(nextMediaItem);
//            // player.prepare();
//        });


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