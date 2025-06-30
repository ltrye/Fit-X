package com.example.fitnestx.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.fitnestx.data.converter.DateConverter;
import com.example.fitnestx.data.dao.AuthProviderDAO;
import com.example.fitnestx.data.dao.ExerciseDAO;
import com.example.fitnestx.data.dao.ExerciseFeedbackDAO;
import com.example.fitnestx.data.dao.MuscleGroupDAO;
import com.example.fitnestx.data.dao.NotificationDAO;
import com.example.fitnestx.data.dao.SessionExerciseDAO;
import com.example.fitnestx.data.dao.UserDAO;
import com.example.fitnestx.data.dao.UserMetricsDAO;
import com.example.fitnestx.data.dao.WorkoutPlanDAO;
import com.example.fitnestx.data.dao.WorkoutSessionDAO;
import com.example.fitnestx.data.entity.AuthProviderEntity;
import com.example.fitnestx.data.entity.ExerciseEntity;
import com.example.fitnestx.data.entity.ExerciseFeedbackEntity;
import com.example.fitnestx.data.entity.MuscleGroupEntity;
import com.example.fitnestx.data.entity.NotificationEntity;
import com.example.fitnestx.data.entity.SessionExerciseEntity;
import com.example.fitnestx.data.entity.UserEntity;
import com.example.fitnestx.data.entity.UserMetricsEntity;
import com.example.fitnestx.data.entity.WorkoutPlanEntity;
import com.example.fitnestx.data.entity.WorkoutSessionEntity;

import java.util.concurrent.Executors;

@Database(entities = {
        UserEntity.class,
        WorkoutSessionEntity.class,
        SessionExerciseEntity.class,
        WorkoutPlanEntity.class,
        MuscleGroupEntity.class,
        ExerciseEntity.class,
        ExerciseFeedbackEntity.class,
        NotificationEntity.class,
        UserMetricsEntity.class,
        AuthProviderEntity.class
}, version = 7, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDAO userDAO();
    public abstract WorkoutSessionDAO workoutSessionDAO();
    public abstract SessionExerciseDAO sessionExerciseDAO();
    public abstract WorkoutPlanDAO workoutPlanDAO();
    public abstract MuscleGroupDAO muscleGroupDAO();
    public abstract ExerciseDAO exerciseDAO();
    public abstract ExerciseFeedbackDAO exerciseFeedbackDAO();
    public abstract NotificationDAO notificationDAO();
    public abstract UserMetricsDAO userMetricsDAO();
    public abstract AuthProviderDAO authProviderDAO();

    private static AppDatabase sInstance;
    private static Context sAppContext;
    private static final String DB_NAME = "FitNestX";

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE INDEX IF NOT EXISTS index_WORKOUT_PLAN_userId ON WORKOUT_PLAN(userId)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_EXERCISE_muscleGroupId ON EXERCISE(muscleGroupId)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_EXERCISE_FEEDBACK_userId ON EXERCISE_FEEDBACK(userId)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_SESSION_EXERCISE_exerciseId ON SESSION_EXERCISE(exerciseId)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_NOTIFICATION_userId ON NOTIFICATION(userId)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_USER_METRICS_userId ON USER_METRICS(userId)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_AUTH_PROVIDER_userId ON AUTH_PROVIDER(userId)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_WORKOUT_SESSION_planId ON WORKOUT_SESSION(planId)");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE EXERCISE ADD COLUMN imageUrl TEXT");
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE EXERCISE ADD COLUMN isMarked INTEGER NOT NULL DEFAULT 0");
        }
    };

    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Skip this migration as it was causing issues
        }
    };

    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE MUSCLE_GROUP ADD COLUMN parentId INTEGER");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_MUSCLE_GROUP_parentId ON MUSCLE_GROUP(parentId)");
        }
    };

    static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP INDEX IF EXISTS index_WORKOUT_SESSION_planId");
        }
    };

    public static synchronized AppDatabase getInstance(final Context context) {
        if (sInstance == null) {
            sAppContext = context.getApplicationContext();
            sInstance = Room.databaseBuilder(sAppContext, AppDatabase.class, DB_NAME)
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build();
        }
        return sInstance;
    }

    private static final RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase instance = sInstance;
                DatabaseGenerator.readUserCSV(sAppContext, "users.csv", instance);
                DatabaseGenerator.readWorkoutPlanCSV(sAppContext, "workout_plans.csv", instance);
                DatabaseGenerator.readWorkoutSessionCSV(sAppContext, "workout_sessions.csv", instance);
                DatabaseGenerator.readSessionExerciseCSV(sAppContext, "session_exercises.csv", instance);
                DatabaseGenerator.readMuscleGroupCSV(sAppContext, "muscle_groups.csv", instance);
                DatabaseGenerator.readExerciseCSV(sAppContext, "exercises.csv", instance);
                DatabaseGenerator.readExerciseFeedbackCSV(sAppContext, "exercise_feedback.csv", instance);
                DatabaseGenerator.readNotificationCSV(sAppContext, "notifications.csv", instance);
                DatabaseGenerator.readUserMetricsCSV(sAppContext, "user_metrics.csv", instance);
                DatabaseGenerator.readAuthProviderCSV(sAppContext, "auth_providers.csv", instance);
            });
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
        }
    };
}
