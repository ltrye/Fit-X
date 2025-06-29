package com.example.fitnestx.data;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

public class DatabaseGenerator {

    public static void readUserCSV(Context context, String fileName, AppDatabase instance) {
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            reader.readLine(); // Skip header
            String line;
            UserDAO userDAO = instance.userDAO();
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                int id = Integer.parseInt(data[0]);
                String name = data[1];
                int age = Integer.parseInt(data[2]);
                boolean gender = Boolean.parseBoolean(data[3]);
                String email = data[4];
                String password = data[5];
                boolean isActive = Boolean.parseBoolean(data[6]);

                UserEntity user = new UserEntity(id, name, age, gender, email, password, isActive);
                userDAO.insertUser(user);
            }
            reader.close();
            inputStream.close();
        } catch (IOException e) {
            Log.e("DatabaseGenerator", "Error reading user CSV", e);
        }
    }

    public static void readWorkoutPlanCSV(Context context, String fileName, AppDatabase instance) {
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            reader.readLine(); // Skip header
            String line;
            WorkoutPlanDAO planDAO = instance.workoutPlanDAO();
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                int planId = Integer.parseInt(data[0]);
                int userId = Integer.parseInt(data[1]);
                String startDate = data[2];
                String durationInWeeks = data[3];
                int daysPerWeek = Integer.parseInt(data[4]);
                boolean isActive = Boolean.parseBoolean(data[5]);

                WorkoutPlanEntity plan = new WorkoutPlanEntity(planId, userId, startDate, durationInWeeks, daysPerWeek, isActive);
                planDAO.insertWorkoutPlan(plan);
            }
            reader.close();
            inputStream.close();
        } catch (IOException e) {
            Log.e("DatabaseGenerator", "Error reading workout plan CSV", e);
        }
    }

    public static void readWorkoutSessionCSV(Context context, String fileName, AppDatabase instance) {
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            reader.readLine(); // Skip header
            String line;
            WorkoutSessionDAO sessionDAO = instance.workoutSessionDAO();
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                int sessionId = Integer.parseInt(data[0]);
                int planId = Integer.parseInt(data[1]);
                String date = data[2];
                int intensity = Integer.parseInt(data[3]);
                boolean isCompleted = Boolean.parseBoolean(data[4]);

                WorkoutSessionEntity session = new WorkoutSessionEntity(sessionId, planId, date, intensity, isCompleted);
                sessionDAO.insertWorkoutSession(session);
            }
            reader.close();
            inputStream.close();
        } catch (IOException e) {
            Log.e("DatabaseGenerator", "Error reading workout session CSV", e);
        }
    }

    public static void readSessionExerciseCSV(Context context, String fileName, AppDatabase instance) {
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            reader.readLine(); // Skip header
            String line;
            SessionExerciseDAO exerciseDAO = instance.sessionExerciseDAO();
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                int sessionId = Integer.parseInt(data[0]);
                int exerciseId = Integer.parseInt(data[1]);
                int order = Integer.parseInt(data[2]);
                String restTime = data[3];
                int sets = Integer.parseInt(data[4]);
                int reps = Integer.parseInt(data[5]);
                boolean isOptional = Boolean.parseBoolean(data[6]);

                SessionExerciseEntity sessionExercise = new SessionExerciseEntity(sessionId, exerciseId, order, restTime, sets, reps, isOptional);
                exerciseDAO.insertSessionExercise(sessionExercise);
            }
            reader.close();
            inputStream.close();
        } catch (IOException e) {
            Log.e("DatabaseGenerator", "Error reading session exercise CSV", e);
        }
    }

    public static void readMuscleGroupCSV(Context context, String fileName, AppDatabase instance) {
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            reader.readLine(); // Skip header
            String line;
            MuscleGroupDAO groupDAO = instance.muscleGroupDAO();
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                int muscleGroupId = Integer.parseInt(data[0]);
                String name = data[1];
                double recoveryTimeInHours = Double.parseDouble(data[2]);
                // Nếu cột parentId trống -> đặt là null
                Integer parentId = null;
                if (data.length > 3 && !data[3].isEmpty()) {
                    parentId = Integer.parseInt(data[3]);
                }
                MuscleGroupEntity group = new MuscleGroupEntity(muscleGroupId, name, recoveryTimeInHours,parentId);
                groupDAO.insertMuscleGroup(group);
            }
            reader.close();
            inputStream.close();
        } catch (IOException e) {
            Log.e("DatabaseGenerator", "Error reading muscle group CSV", e);
        }
    }

    public static void readExerciseCSV(Context context, String fileName, AppDatabase instance) {
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            reader.readLine(); // Skip header
            String line;
            ExerciseDAO exerciseDAO = instance.exerciseDAO();
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                int exerciseId = Integer.parseInt(data[0]);
                String name = data[1];
                String description = data[2];
                String videoURL = data[3];
                int muscleGroupId = Integer.parseInt(data[4]);
                boolean equipmentRequired = Boolean.parseBoolean(data[5]);
                int difficulty = Integer.parseInt(data[6]);
                String imageUrl = data[7];
                boolean isMarked = Boolean.parseBoolean(data[8]);
                ExerciseEntity exercise = new ExerciseEntity(exerciseId, name, description, videoURL, muscleGroupId, equipmentRequired,difficulty ,imageUrl,isMarked);
                exerciseDAO.insertExercise(exercise);
            }
            reader.close();
            inputStream.close();
        } catch (IOException e) {
            Log.e("DatabaseGenerator", "Error reading exercise CSV", e);
        }
    }

    public static void readExerciseFeedbackCSV(Context context, String fileName, AppDatabase instance) {
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            reader.readLine(); // Skip header
            String line;
            ExerciseFeedbackDAO feedbackDAO = instance.exerciseFeedbackDAO();
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                int feedbackId = Integer.parseInt(data[0]);
                int userId = Integer.parseInt(data[1]);
                int exerciseId = Integer.parseInt(data[2]);
                int difficultyRated = Integer.parseInt(data[3]);
                String comment = data[4];
                String timestamp = data[5];

                ExerciseFeedbackEntity feedback = new ExerciseFeedbackEntity(feedbackId, userId, exerciseId, difficultyRated, comment, timestamp);
                feedbackDAO.insertFeedback(feedback);
            }
            reader.close();
            inputStream.close();
        } catch (IOException e) {
            Log.e("DatabaseGenerator", "Error reading exercise feedback CSV", e);
        }
    }

    public static void readNotificationCSV(Context context, String fileName, AppDatabase instance) {
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            reader.readLine(); // Skip header
            String line;
            NotificationDAO notificationDAO = instance.notificationDAO();
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                int notificationId = Integer.parseInt(data[0]);
                int userId = Integer.parseInt(data[1]);
                String content = data[2];
                boolean isRead = Boolean.parseBoolean(data[3]);
                String timestamp = data[4];

                NotificationEntity notification = new NotificationEntity(notificationId, userId, content, isRead, timestamp);
                notificationDAO.insertNotification(notification);
            }
            reader.close();
            inputStream.close();
        } catch (IOException e) {
            Log.e("DatabaseGenerator", "Error reading notification CSV", e);
        }
    }

    public static void readUserMetricsCSV(Context context, String fileName, AppDatabase instance) {
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            reader.readLine(); // Skip header
            String line;
            UserMetricsDAO metricsDAO = instance.userMetricsDAO();
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                int metricId = Integer.parseInt(data[0]);
                int userId = Integer.parseInt(data[1]);
                String timestamp = data[2];
                double weight = Double.parseDouble(data[3]);
                double height = Double.parseDouble(data[4]);
                double bmi = Double.parseDouble(data[5]);
                String goal = data[6];

                UserMetricsEntity metric = new UserMetricsEntity(metricId, userId, timestamp, weight, height, bmi, goal);
                metricsDAO.insertUserMetric(metric);
            }
            reader.close();
            inputStream.close();
        } catch (IOException e) {
            Log.e("DatabaseGenerator", "Error reading user metrics CSV", e);
        }
    }

    public static void readAuthProviderCSV(Context context, String fileName, AppDatabase instance) {
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            reader.readLine(); // Skip header
            String line;
            AuthProviderDAO authDAO = instance.authProviderDAO();
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                int providerId = Integer.parseInt(data[0]);
                int userId = Integer.parseInt(data[1]);
                String providerType = data[2];
                String providerUID = data[3];

                AuthProviderEntity auth = new AuthProviderEntity(providerId, userId, providerType, providerUID);
                authDAO.insertAuthProvider(auth);
            }
            reader.close();
            inputStream.close();
        } catch (IOException e) {
            Log.e("DatabaseGenerator", "Error reading auth provider CSV", e);
        }
    }

    private static Date StringToDate(String data) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.parse(data);
    }

    private static String DateToString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date);
    }
}
