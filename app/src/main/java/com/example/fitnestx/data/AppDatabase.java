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
}, version = 2, exportSchema = false)
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
            database.execSQL("CREATE INDEX IF NOT EXISTS index_WorkoutPlan_userId ON WorkoutPlan(userId)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_MuscleGroup_parentId ON MUSCLE_GROUP(parentId)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_Exercise_muscleGroupId ON Exercise(muscleGroupId)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_ExerciseFeedback_userId ON ExerciseFeedback(userId)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_SessionExercise_exerciseId ON SessionExercise(exerciseId)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_Notification_userId ON Notification(userId)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_UserMetrics_userId ON UserMetrics(userId)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_AuthProvider_userId ON AuthProvider(userId)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_WorkoutSession_planId ON WorkoutSession(planId)");
        }
    };


    public static synchronized AppDatabase getInstance(final Context context) {
        if (sInstance == null) {
            sAppContext = context.getApplicationContext();
            sInstance = Room.databaseBuilder(sAppContext, AppDatabase.class, DB_NAME)
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(roomCallback)
                    .build();
        }
        return sInstance;
    }

    private static final RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            AppDatabase instance = AppDatabase.getInstance(sAppContext);
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
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            // Optional: Refresh data on open if needed
        }
    };
}
