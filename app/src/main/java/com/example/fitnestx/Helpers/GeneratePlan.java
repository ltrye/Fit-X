package com.example.fitnestx.Helpers;

import android.util.Log;

import com.example.fitnestx.data.entity.ExerciseEntity;
import com.example.fitnestx.data.entity.MuscleGroupEntity;
import com.example.fitnestx.data.entity.SessionExerciseEntity;
import com.example.fitnestx.data.entity.WorkoutPlanEntity;
import com.example.fitnestx.data.entity.WorkoutSessionEntity;
import com.example.fitnestx.data.repository.ExerciseRepository;
import com.example.fitnestx.data.repository.MuscleGroupRepository;
import com.example.fitnestx.data.repository.SessionExerciseRepository;
import com.example.fitnestx.data.repository.WorkoutPlanRepository;
import com.example.fitnestx.data.repository.WorkoutSessionRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GeneratePlan {
    private int planId;
    private double bmi;
    private String goal;
    private boolean gender; // true = male, false = female
    private int daysPerWeek;

    private ExerciseRepository exerciseRepository;
    private MuscleGroupRepository muscleGroupRepository;
    private WorkoutSessionRepository workoutSessionRepository;
    private SessionExerciseRepository sessionExerciseRepository;
    private WorkoutPlanRepository workoutPlanRepository;

    // BMI Categories
    private static final double BMI_UNDERWEIGHT = 18.5;
    private static final double BMI_NORMAL = 25.0;
    private static final double BMI_OVERWEIGHT = 29.9;

    // Muscle Group IDs (based on your database structure)
    private static final int CHEST = 1;
    private static final int SHOULDERS = 2;
    private static final int BACK = 3;
    private static final int BICEPS = 4;
    private static final int TRICEPS = 5;
    private static final int QUADS = 6;
    private static final int GLUTES = 7;
    private static final int CARDIO = 29;

    public GeneratePlan(int planId, double bmi, String goal, boolean gender, int daysPerWeek,
                        ExerciseRepository exerciseRepository, MuscleGroupRepository muscleGroupRepository,
                        WorkoutSessionRepository workoutSessionRepository, SessionExerciseRepository sessionExerciseRepository,
                        WorkoutPlanRepository workoutPlanRepository) {
        this.planId = planId;
        this.bmi = bmi;
        this.goal = goal;
        this.gender = gender;
        this.daysPerWeek = daysPerWeek;
        this.exerciseRepository = exerciseRepository;
        this.muscleGroupRepository = muscleGroupRepository;
        this.workoutSessionRepository = workoutSessionRepository;
        this.sessionExerciseRepository = sessionExerciseRepository;
        this.workoutPlanRepository = workoutPlanRepository;

        Log.d("GeneratePlan", "Initialized with BMI: " + bmi + ", Goal: " + goal + ", Gender: " + (gender ? "Male" : "Female") + ", Days: " + daysPerWeek);
    }

    public void Generation() {
        Log.d("GeneratePlan", "Starting workout plan generation...");
        insertWorkoutSessions();
        try {
            Thread.sleep(200); // Wait for sessions to be inserted
        } catch (InterruptedException e) {
            Log.e("GeneratePlan", "Thread interrupted", e);
        }
        generateWorkoutPlan();
        addSpecialNotes();
        Log.d("GeneratePlan", "Workout plan generation completed");
    }

    // New method to regenerate exercises for existing sessions
    public void regeneratePlanExercises() {
        Log.d("GeneratePlan", "Starting exercise regeneration for planId: " + planId);
        // Delete existing session exercises for this plan
        List<WorkoutSessionEntity> sessions = workoutSessionRepository.getWorkoutSessionsByPlanId(planId);
        for (WorkoutSessionEntity session : sessions) {
            sessionExerciseRepository.deleteSessionExercisesBySessionId(session.getSessionId());
        }
        addSpecialNotes();
        // No need for Thread.sleep here if called from ExecutorService
        generateWorkoutPlan(); // This will re-populate exercises for existing sessions
        Log.d("GeneratePlan", "Exercise regeneration completed for planId: " + planId);
    }


    private void insertWorkoutSessions() {
        Log.d("GeneratePlan", "Inserting " + daysPerWeek + " workout sessions");
        for (int day = 1; day <= daysPerWeek; day++) {
            WorkoutSessionEntity session = new WorkoutSessionEntity(planId, "Ngày " + day, 1, false);
            workoutSessionRepository.insertWorkoutSession(session);
        }
    }

    private void generateWorkoutPlan() {
        List<WorkoutSessionEntity> sessions = workoutSessionRepository.getWorkoutSessionsByPlanId(planId);
        if (sessions.isEmpty()) {
            Log.e("GeneratePlan", "No sessions found for planId: " + planId);
            return;
        }

        Log.d("GeneratePlan", "Found " + sessions.size() + " sessions to populate");

        // Sort sessions by day number
        sessions.sort((s1, s2) -> {
            int day1 = extractDayNumber(s1.getDate());
            int day2 = extractDayNumber(s2.getDate());
            return Integer.compare(day1, day2);
        });

        BMICategory bmiCategory = getBMICategory();
        Log.d("GeneratePlan", "BMI Category: " + bmiCategory);

        if (gender) { // Male
            generateMaleWorkout(sessions, bmiCategory);
        } else { // Female
            generateFemaleWorkout(sessions, bmiCategory);
        }
    }

    private int extractDayNumber(String label) {
        try {
            return Integer.parseInt(label.replaceAll("[^0-9]", "").trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private BMICategory getBMICategory() {
        if (bmi < BMI_UNDERWEIGHT) return BMICategory.UNDERWEIGHT;
        if (bmi < BMI_NORMAL) return BMICategory.NORMAL;
        if (bmi <= BMI_OVERWEIGHT) return BMICategory.OVERWEIGHT;
        return BMICategory.OBESE;
    }

    private void generateMaleWorkout(List<WorkoutSessionEntity> sessions, BMICategory bmiCategory) {
        Log.d("GeneratePlan", "Generating male workout for " + bmiCategory + " with goal: " + goal);

        boolean includeCardio = shouldIncludeCardio(bmiCategory);
        int exercisesPerDay = getExercisesPerDay(bmiCategory, true);

        switch (goal) {
            case "improve_shape":
                generateMaleImproveShapeWorkout(sessions, includeCardio, exercisesPerDay);
                break;
            case "lean_tone":
            case "lose_fat":
                generateMaleLeanToneWorkout(sessions, exercisesPerDay);
                break;
            default:
                generateMaleImproveShapeWorkout(sessions, includeCardio, exercisesPerDay);
                break;
        }
    }

    private void generateFemaleWorkout(List<WorkoutSessionEntity> sessions, BMICategory bmiCategory) {
        Log.d("GeneratePlan", "Generating female workout for " + bmiCategory + " with goal: " + goal);

        boolean includeCardio = shouldIncludeCardio(bmiCategory);
        int exercisesPerDay = getExercisesPerDay(bmiCategory, false);

        switch (goal) {
            case "improve_shape":
                generateFemaleImproveShapeWorkout(sessions, includeCardio, exercisesPerDay);
                break;
            case "lean_tone":
            case "lose_fat":
                generateFemaleLeanToneWorkout(sessions, exercisesPerDay);
                break;
            default:
                generateFemaleImproveShapeWorkout(sessions, includeCardio, exercisesPerDay);
                break;
        }
    }

    private boolean shouldIncludeCardio(BMICategory bmiCategory) {
        boolean includeCardio = bmiCategory == BMICategory.OVERWEIGHT || bmiCategory == BMICategory.OBESE ||
                goal.equals("lean_tone") || goal.equals("lose_fat");

        // Don't include cardio for underweight users
        if (bmiCategory == BMICategory.UNDERWEIGHT) {
            includeCardio = false;
        }

        Log.d("GeneratePlan", "Include cardio: " + includeCardio);
        return includeCardio;
    }

    private int getExercisesPerDay(BMICategory bmiCategory, boolean isMale) {
        int exercises;
        if (bmiCategory == BMICategory.OBESE) {
            exercises = 5; // Reduced for obese users
        } else if (isMale) {
            exercises = 6; // Standard for males
        } else {
            exercises = 5; // Standard for females
        }
        Log.d("GeneratePlan", "Exercises per day: " + exercises);
        return exercises;
    }

    // MALE WORKOUT IMPLEMENTATIONS
    private void generateMaleImproveShapeWorkout(List<WorkoutSessionEntity> sessions, boolean addCardio, int exercisesPerDay) {
        Log.d("GeneratePlan", "Generating male improve_shape workout, " + daysPerWeek + " days");

        switch (daysPerWeek) {
            case 2:
                generateMaleUpperLowerSplit(sessions, addCardio, exercisesPerDay);
                break;
            case 3:
                generateMalePushPullLegSplit(sessions, addCardio, exercisesPerDay);
                break;
            case 4:
                generateMalePushPullLegFullBodySplit(sessions, addCardio, exercisesPerDay);
                break;
            case 5:
                generateMalePushPullLegUpperLowerSplit(sessions, addCardio, exercisesPerDay);
                break;
            case 6:
                generateMaleDoublePushPullLegSplit(sessions, addCardio, exercisesPerDay);
                break;
            default:
                generateMalePushPullLegSplit(sessions, addCardio, exercisesPerDay);
                break;
        }
    }

    private void generateMaleLeanToneWorkout(List<WorkoutSessionEntity> sessions, int exercisesPerDay) {
        Log.d("GeneratePlan", "Generating male lean_tone/lose_fat workout, " + daysPerWeek + " days");

        switch (daysPerWeek) {
            case 2:
                generateMaleUpperLowerSplitWithCardio(sessions, exercisesPerDay);
                break;
            case 3:
                generateMalePushPullLegSplitWithCardio(sessions, exercisesPerDay);
                break;
            case 4:
                generateMalePushPullLegCardioSplit(sessions, exercisesPerDay);
                break;
            case 5:
                generateMalePushPullLegFullBodyCardioSplit(sessions, exercisesPerDay);
                break;
            case 6:
                generateMalePushPullLegCardioFullBodyCardioSplit(sessions, exercisesPerDay);
                break;
            default:
                generateMalePushPullLegSplitWithCardio(sessions, exercisesPerDay);
                break;
        }
    }

    // FEMALE WORKOUT IMPLEMENTATIONS
    private void generateFemaleImproveShapeWorkout(List<WorkoutSessionEntity> sessions, boolean addCardio, int exercisesPerDay) {
        Log.d("GeneratePlan", "Generating female improve_shape workout, " + daysPerWeek + " days");

        switch (daysPerWeek) {
            case 2:
                generateFemaleUpperLowerSplit(sessions, addCardio, exercisesPerDay);
                break;
            case 3:
                generateFemaleLegPullPushSplit(sessions, addCardio, exercisesPerDay);
                break;
            case 4:
                generateFemaleLegPullPushCardioSplit(sessions, exercisesPerDay);
                break;
            case 5:
                generateFemaleLegPullPushCardioLowerSplit(sessions, exercisesPerDay);
                break;
            default:
                generateFemaleLegPullPushSplit(sessions, addCardio, exercisesPerDay);
                break;
        }
    }

    private void generateFemaleLeanToneWorkout(List<WorkoutSessionEntity> sessions, int exercisesPerDay) {
        Log.d("GeneratePlan", "Generating female lean_tone/lose_fat workout, " + daysPerWeek + " days");

        switch (daysPerWeek) {
            case 2:
                generateFemaleUpperLowerSplitWithCardio(sessions, exercisesPerDay);
                break;
            case 3:
                generateFemaleLegPullPushSplitWithCardio(sessions, exercisesPerDay);
                break;
            case 4:
                generateFemaleLegPullPushCardioSplit(sessions, exercisesPerDay);
                break;
            case 5:
                generateFemaleLegPullPushCardioLowerSplit(sessions, exercisesPerDay);
                break;
            default:
                generateFemaleLegPullPushSplitWithCardio(sessions, exercisesPerDay);
                break;
        }
    }

    // MALE WORKOUT SPLIT IMPLEMENTATIONS
    private void generateMaleUpperLowerSplit(List<WorkoutSessionEntity> sessions, boolean addCardio, int exercisesPerDay) {
        for (int i = 0; i < sessions.size(); i++) {
            WorkoutSessionEntity session = sessions.get(i);
            List<ExerciseEntity> exercises = new ArrayList<>();

            if (i % 2 == 0) { // Upper day
                exercises.addAll(getRandomExercisesOfParent(CHEST, 2));
                exercises.addAll(getRandomExercisesOfParent(BACK, 2));
                exercises.addAll(getRandomExercisesOfParent(SHOULDERS, 1));
                exercises.addAll(getRandomExercisesOfParent(BICEPS, 1));
            } else { // Lower day
                exercises.addAll(getRandomExercisesOfParent(QUADS, 3));
                exercises.addAll(getRandomExercisesOfParent(GLUTES, 3));
            }

            if (addCardio) {
                exercises.addAll(getRandomExercisesOfParent(CARDIO, 1));
            }

            adjustExerciseCount(exercises, exercisesPerDay);
            addExercisesToSession(session, exercises);
        }
    }

    private void generateMaleUpperLowerSplitWithCardio(List<WorkoutSessionEntity> sessions, int exercisesPerDay) {
        for (int i = 0; i < sessions.size(); i++) {
            WorkoutSessionEntity session = sessions.get(i);
            List<ExerciseEntity> exercises = new ArrayList<>();

            if (i % 2 == 0) { // Upper day + Cardio
                exercises.addAll(getRandomExercisesOfParent(CHEST, 2));
                exercises.addAll(getRandomExercisesOfParent(BACK, 2));
                exercises.addAll(getRandomExercisesOfParent(SHOULDERS, 1));
                exercises.addAll(getRandomExercisesOfParent(CARDIO, 1));
            } else { // Lower day + Cardio
                exercises.addAll(getRandomExercisesOfParent(QUADS, 2));
                exercises.addAll(getRandomExercisesOfParent(GLUTES, 2));
                exercises.addAll(getRandomExercisesOfParent(CARDIO, 1));
            }

            adjustExerciseCount(exercises, exercisesPerDay);
            addExercisesToSession(session, exercises);
        }
    }

    private void generateMalePushPullLegSplit(List<WorkoutSessionEntity> sessions, boolean addCardio, int exercisesPerDay) {
        for (int i = 0; i < sessions.size(); i++) {
            WorkoutSessionEntity session = sessions.get(i);
            List<ExerciseEntity> exercises = new ArrayList<>();

            int dayType = i % 3;
            switch (dayType) {
                case 0: // Push
                    exercises.addAll(getRandomExercisesOfParent(CHEST, 2));
                    exercises.addAll(getRandomExercisesOfParent(SHOULDERS, 2));
                    exercises.addAll(getRandomExercisesOfParent(TRICEPS, 2));
                    break;
                case 1: // Pull
                    exercises.addAll(getRandomExercisesOfParent(BACK, 3));
                    exercises.addAll(getRandomExercisesOfParent(BICEPS, 3));
                    break;
                case 2: // Leg
                    exercises.addAll(getRandomExercisesOfParent(QUADS, 3));
                    exercises.addAll(getRandomExercisesOfParent(GLUTES, 3));
                    break;
            }

            if (addCardio) {
                exercises.addAll(getRandomExercisesOfParent(CARDIO, 1));
            }

            adjustExerciseCount(exercises, exercisesPerDay);
            addExercisesToSession(session, exercises);
        }
    }

    private void generateMalePushPullLegSplitWithCardio(List<WorkoutSessionEntity> sessions, int exercisesPerDay) {
        for (int i = 0; i < sessions.size(); i++) {
            WorkoutSessionEntity session = sessions.get(i);
            List<ExerciseEntity> exercises = new ArrayList<>();

            int dayType = i % 3;
            switch (dayType) {
                case 0: // Push + Cardio
                    exercises.addAll(getRandomExercisesOfParent(CHEST, 2));
                    exercises.addAll(getRandomExercisesOfParent(SHOULDERS, 2));
                    exercises.addAll(getRandomExercisesOfParent(TRICEPS, 1));
                    exercises.addAll(getRandomExercisesOfParent(CARDIO, 1));
                    break;
                case 1: // Pull + Cardio
                    exercises.addAll(getRandomExercisesOfParent(BACK, 2));
                    exercises.addAll(getRandomExercisesOfParent(BICEPS, 2));
                    exercises.addAll(getRandomExercisesOfParent(CARDIO, 1));
                    break;
                case 2: // Leg + Cardio
                    exercises.addAll(getRandomExercisesOfParent(QUADS, 2));
                    exercises.addAll(getRandomExercisesOfParent(GLUTES, 2));
                    exercises.addAll(getRandomExercisesOfParent(CARDIO, 1));
                    break;
            }

            adjustExerciseCount(exercises, exercisesPerDay);
            addExercisesToSession(session, exercises);
        }
    }

    private void generateMalePushPullLegFullBodySplit(List<WorkoutSessionEntity> sessions, boolean addCardio, int exercisesPerDay) {
        for (int i = 0; i < sessions.size(); i++) {
            WorkoutSessionEntity session = sessions.get(i);
            List<ExerciseEntity> exercises = new ArrayList<>();

            switch (i) {
                case 0: // Push
                    exercises.addAll(getRandomExercisesOfParent(CHEST, 2));
                    exercises.addAll(getRandomExercisesOfParent(SHOULDERS, 2));
                    exercises.addAll(getRandomExercisesOfParent(TRICEPS, 2));
                    break;
                case 1: // Pull
                    exercises.addAll(getRandomExercisesOfParent(BACK, 3));
                    exercises.addAll(getRandomExercisesOfParent(BICEPS, 3));
                    break;
                case 2: // Leg
                    exercises.addAll(getRandomExercisesOfParent(QUADS, 3));
                    exercises.addAll(getRandomExercisesOfParent(GLUTES, 3));
                    break;
                case 3: // Full Body
                    exercises.addAll(getRandomExercisesOfParent(CHEST, 2));
                    exercises.addAll(getRandomExercisesOfParent(BACK, 2));
                    exercises.addAll(getRandomExercisesOfParent(QUADS, 2));
                    break;
            }

            if (addCardio) {
                exercises.addAll(getRandomExercisesOfParent(CARDIO, 1));
            }

            adjustExerciseCount(exercises, exercisesPerDay);
            addExercisesToSession(session, exercises);
        }
    }

    private void generateMalePushPullLegUpperLowerSplit(List<WorkoutSessionEntity> sessions, boolean addCardio, int exercisesPerDay) {
        for (int i = 0; i < sessions.size(); i++) {
            WorkoutSessionEntity session = sessions.get(i);
            List<ExerciseEntity> exercises = new ArrayList<>();

            switch (i) {
                case 0: // Push
                    exercises.addAll(getRandomExercisesOfParent(CHEST, 2));
                    exercises.addAll(getRandomExercisesOfParent(SHOULDERS, 2));
                    exercises.addAll(getRandomExercisesOfParent(TRICEPS, 2));
                    break;
                case 1: // Pull
                    exercises.addAll(getRandomExercisesOfParent(BACK, 3));
                    exercises.addAll(getRandomExercisesOfParent(BICEPS, 3));
                    break;
                case 2: // Leg
                    exercises.addAll(getRandomExercisesOfParent(QUADS, 3));
                    exercises.addAll(getRandomExercisesOfParent(GLUTES, 3));
                    break;
                case 3: // Upper
                    exercises.addAll(getRandomExercisesOfParent(CHEST, 1));
                    exercises.addAll(getRandomExercisesOfParent(BACK, 2));
                    exercises.addAll(getRandomExercisesOfParent(SHOULDERS, 1));
                    exercises.addAll(getRandomExercisesOfParent(BICEPS, 1));
                    exercises.addAll(getRandomExercisesOfParent(TRICEPS, 1));
                    break;
                case 4: // Lower
                    exercises.addAll(getRandomExercisesOfParent(QUADS, 3));
                    exercises.addAll(getRandomExercisesOfParent(GLUTES, 3));
                    break;
            }

            if (addCardio) {
                exercises.addAll(getRandomExercisesOfParent(CARDIO, 1));
            }

            adjustExerciseCount(exercises, exercisesPerDay);
            addExercisesToSession(session, exercises);
        }
    }

    private void generateMaleDoublePushPullLegSplit(List<WorkoutSessionEntity> sessions, boolean addCardio, int exercisesPerDay) {
        for (int i = 0; i < sessions.size(); i++) {
            WorkoutSessionEntity session = sessions.get(i);
            List<ExerciseEntity> exercises = new ArrayList<>();

            int dayType = i % 3;
            switch (dayType) {
                case 0: // Push
                    exercises.addAll(getRandomExercisesOfParent(CHEST, 2));
                    exercises.addAll(getRandomExercisesOfParent(SHOULDERS, 2));
                    exercises.addAll(getRandomExercisesOfParent(TRICEPS, 2));
                    break;
                case 1: // Pull
                    exercises.addAll(getRandomExercisesOfParent(BACK, 3));
                    exercises.addAll(getRandomExercisesOfParent(BICEPS, 3));
                    break;
                case 2: // Leg
                    exercises.addAll(getRandomExercisesOfParent(QUADS, 3));
                    exercises.addAll(getRandomExercisesOfParent(GLUTES, 3));
                    break;
            }

            if (addCardio) {
                exercises.addAll(getRandomExercisesOfParent(CARDIO, 1));
            }

            adjustExerciseCount(exercises, exercisesPerDay);
            addExercisesToSession(session, exercises);
        }
    }

    private void generateMalePushPullLegCardioSplit(List<WorkoutSessionEntity> sessions, int exercisesPerDay) {
        for (int i = 0; i < sessions.size(); i++) {
            WorkoutSessionEntity session = sessions.get(i);
            List<ExerciseEntity> exercises = new ArrayList<>();

            switch (i) {
                case 0: // Push
                    exercises.addAll(getRandomExercisesOfParent(CHEST, 2));
                    exercises.addAll(getRandomExercisesOfParent(SHOULDERS, 2));
                    exercises.addAll(getRandomExercisesOfParent(TRICEPS, 2));
                    break;
                case 1: // Pull
                    exercises.addAll(getRandomExercisesOfParent(BACK, 3));
                    exercises.addAll(getRandomExercisesOfParent(BICEPS, 3));
                    break;
                case 2: // Leg
                    exercises.addAll(getRandomExercisesOfParent(QUADS, 3));
                    exercises.addAll(getRandomExercisesOfParent(GLUTES, 3));
                    break;
                case 3: // Cardio
                    exercises.addAll(getRandomExercisesOfParent(CARDIO, exercisesPerDay));
                    break;
            }

            adjustExerciseCount(exercises, exercisesPerDay);
            addExercisesToSession(session, exercises);
        }
    }

    private void generateMalePushPullLegFullBodyCardioSplit(List<WorkoutSessionEntity> sessions, int exercisesPerDay) {
        for (int i = 0; i < sessions.size(); i++) {
            WorkoutSessionEntity session = sessions.get(i);
            List<ExerciseEntity> exercises = new ArrayList<>();

            switch (i) {
                case 0: // Push
                    exercises.addAll(getRandomExercisesOfParent(CHEST, 2));
                    exercises.addAll(getRandomExercisesOfParent(SHOULDERS, 2));
                    exercises.addAll(getRandomExercisesOfParent(TRICEPS, 2));
                    break;
                case 1: // Pull
                    exercises.addAll(getRandomExercisesOfParent(BACK, 3));
                    exercises.addAll(getRandomExercisesOfParent(BICEPS, 3));
                    break;
                case 2: // Leg
                    exercises.addAll(getRandomExercisesOfParent(QUADS, 3));
                    exercises.addAll(getRandomExercisesOfParent(GLUTES, 3));
                    break;
                case 3: // Full Body
                    exercises.addAll(getRandomExercisesOfParent(CHEST, 2));
                    exercises.addAll(getRandomExercisesOfParent(BACK, 2));
                    exercises.addAll(getRandomExercisesOfParent(QUADS, 2));
                    break;
                case 4: // Cardio
                    exercises.addAll(getRandomExercisesOfParent(CARDIO, exercisesPerDay));
                    break;
            }

            adjustExerciseCount(exercises, exercisesPerDay);
            addExercisesToSession(session, exercises);
        }
    }

    private void generateMalePushPullLegCardioFullBodyCardioSplit(List<WorkoutSessionEntity> sessions, int exercisesPerDay) {
        for (int i = 0; i < sessions.size(); i++) {
            WorkoutSessionEntity session = sessions.get(i);
            List<ExerciseEntity> exercises = new ArrayList<>();

            switch (i) {
                case 0: // Push
                    exercises.addAll(getRandomExercisesOfParent(CHEST, 2));
                    exercises.addAll(getRandomExercisesOfParent(SHOULDERS, 2));
                    exercises.addAll(getRandomExercisesOfParent(TRICEPS, 2));
                    break;
                case 1: // Pull
                    exercises.addAll(getRandomExercisesOfParent(BACK, 3));
                    exercises.addAll(getRandomExercisesOfParent(BICEPS, 3));
                    break;
                case 2: // Leg
                    exercises.addAll(getRandomExercisesOfParent(QUADS, 3));
                    exercises.addAll(getRandomExercisesOfParent(GLUTES, 3));
                    break;
                case 3: // Cardio
                    exercises.addAll(getRandomExercisesOfParent(CARDIO, exercisesPerDay));
                    break;
                case 4: // Full Body
                    exercises.addAll(getRandomExercisesOfParent(CHEST, 2));
                    exercises.addAll(getRandomExercisesOfParent(BACK, 2));
                    exercises.addAll(getRandomExercisesOfParent(QUADS, 2));
                    break;
                case 5: // Cardio
                    exercises.addAll(getRandomExercisesOfParent(CARDIO, exercisesPerDay));
                    break;
            }

            adjustExerciseCount(exercises, exercisesPerDay);
            addExercisesToSession(session, exercises);
        }
    }

    // FEMALE WORKOUT SPLIT IMPLEMENTATIONS
    private void generateFemaleUpperLowerSplit(List<WorkoutSessionEntity> sessions, boolean addCardio, int exercisesPerDay) {
        for (int i = 0; i < sessions.size(); i++) {
            WorkoutSessionEntity session = sessions.get(i);
            List<ExerciseEntity> exercises = new ArrayList<>();

            if (i % 2 == 0) { // Upper day
                exercises.addAll(getRandomExercisesOfParent(CHEST, 1));
                exercises.addAll(getRandomExercisesOfParent(BACK, 2));
                exercises.addAll(getRandomExercisesOfParent(SHOULDERS, 1));
                exercises.addAll(getRandomExercisesOfParent(BICEPS, 1));
            } else { // Lower day
                exercises.addAll(getRandomExercisesOfParent(QUADS, 2));
                exercises.addAll(getRandomExercisesOfParent(GLUTES, 3));
            }

            if (addCardio) {
                exercises.addAll(getRandomExercisesOfParent(CARDIO, 1));
            }

            adjustExerciseCount(exercises, exercisesPerDay);
            addExercisesToSession(session, exercises);
        }
    }

    private void generateFemaleUpperLowerSplitWithCardio(List<WorkoutSessionEntity> sessions, int exercisesPerDay) {
        for (int i = 0; i < sessions.size(); i++) {
            WorkoutSessionEntity session = sessions.get(i);
            List<ExerciseEntity> exercises = new ArrayList<>();

            if (i % 2 == 0) { // Upper day + Cardio
                exercises.addAll(getRandomExercisesOfParent(CHEST, 1));
                exercises.addAll(getRandomExercisesOfParent(BACK, 2));
                exercises.addAll(getRandomExercisesOfParent(SHOULDERS, 1));
                exercises.addAll(getRandomExercisesOfParent(CARDIO, 1));
            } else { // Lower day + Cardio
                exercises.addAll(getRandomExercisesOfParent(QUADS, 2));
                exercises.addAll(getRandomExercisesOfParent(GLUTES, 2));
                exercises.addAll(getRandomExercisesOfParent(CARDIO, 1));
            }

            adjustExerciseCount(exercises, exercisesPerDay);
            addExercisesToSession(session, exercises);
        }
    }

    private void generateFemaleLegPullPushSplit(List<WorkoutSessionEntity> sessions, boolean addCardio, int exercisesPerDay) {
        for (int i = 0; i < sessions.size(); i++) {
            WorkoutSessionEntity session = sessions.get(i);
            List<ExerciseEntity> exercises = new ArrayList<>();

            int dayType = i % 3;
            switch (dayType) {
                case 0: // Leg (priority for females)
                    exercises.addAll(getRandomExercisesOfParent(QUADS, 2));
                    exercises.addAll(getRandomExercisesOfParent(GLUTES, 3));
                    break;
                case 1: // Pull
                    exercises.addAll(getRandomExercisesOfParent(BACK, 3));
                    exercises.addAll(getRandomExercisesOfParent(BICEPS, 2));
                    break;
                case 2: // Push
                    exercises.addAll(getRandomExercisesOfParent(CHEST, 2));
                    exercises.addAll(getRandomExercisesOfParent(SHOULDERS, 2));
                    exercises.addAll(getRandomExercisesOfParent(TRICEPS, 1));
                    break;
            }

            if (addCardio) {
                exercises.addAll(getRandomExercisesOfParent(CARDIO, 1));
            }

            adjustExerciseCount(exercises, exercisesPerDay);
            addExercisesToSession(session, exercises);
        }
    }

    private void generateFemaleLegPullPushSplitWithCardio(List<WorkoutSessionEntity> sessions, int exercisesPerDay) {
        for (int i = 0; i < sessions.size(); i++) {
            WorkoutSessionEntity session = sessions.get(i);
            List<ExerciseEntity> exercises = new ArrayList<>();

            int dayType = i % 3;
            switch (dayType) {
                case 0: // Leg + Cardio
                    exercises.addAll(getRandomExercisesOfParent(QUADS, 2));
                    exercises.addAll(getRandomExercisesOfParent(GLUTES, 2));
                    exercises.addAll(getRandomExercisesOfParent(CARDIO, 1));
                    break;
                case 1: // Pull + Cardio
                    exercises.addAll(getRandomExercisesOfParent(BACK, 2));
                    exercises.addAll(getRandomExercisesOfParent(BICEPS, 2));
                    exercises.addAll(getRandomExercisesOfParent(CARDIO, 1));
                    break;
                case 2: // Push + Cardio
                    exercises.addAll(getRandomExercisesOfParent(CHEST, 1));
                    exercises.addAll(getRandomExercisesOfParent(SHOULDERS, 2));
                    exercises.addAll(getRandomExercisesOfParent(TRICEPS, 1));
                    exercises.addAll(getRandomExercisesOfParent(CARDIO, 1));
                    break;
            }

            adjustExerciseCount(exercises, exercisesPerDay);
            addExercisesToSession(session, exercises);
        }
    }

    private void generateFemaleLegPullPushCardioSplit(List<WorkoutSessionEntity> sessions, int exercisesPerDay) {
        for (int i = 0; i < sessions.size(); i++) {
            WorkoutSessionEntity session = sessions.get(i);
            List<ExerciseEntity> exercises = new ArrayList<>();

            switch (i) {
                case 0: // Leg
                    exercises.addAll(getRandomExercisesOfParent(QUADS, 2));
                    exercises.addAll(getRandomExercisesOfParent(GLUTES, 3));
                    break;
                case 1: // Pull
                    exercises.addAll(getRandomExercisesOfParent(BACK, 3));
                    exercises.addAll(getRandomExercisesOfParent(BICEPS, 2));
                    break;
                case 2: // Push
                    exercises.addAll(getRandomExercisesOfParent(CHEST, 2));
                    exercises.addAll(getRandomExercisesOfParent(SHOULDERS, 2));
                    exercises.addAll(getRandomExercisesOfParent(TRICEPS, 1));
                    break;
                case 3: // Cardio
                    exercises.addAll(getRandomExercisesOfParent(CARDIO, exercisesPerDay));
                    break;
            }

            adjustExerciseCount(exercises, exercisesPerDay);
            addExercisesToSession(session, exercises);
        }
    }

    private void generateFemaleLegPullPushCardioLowerSplit(List<WorkoutSessionEntity> sessions, int exercisesPerDay) {
        for (int i = 0; i < sessions.size(); i++) {
            WorkoutSessionEntity session = sessions.get(i);
            List<ExerciseEntity> exercises = new ArrayList<>();

            switch (i) {
                case 0: // Leg
                    exercises.addAll(getRandomExercisesOfParent(QUADS, 2));
                    exercises.addAll(getRandomExercisesOfParent(GLUTES, 3));
                    break;
                case 1: // Pull
                    exercises.addAll(getRandomExercisesOfParent(BACK, 3));
                    exercises.addAll(getRandomExercisesOfParent(BICEPS, 2));
                    break;
                case 2: // Push
                    exercises.addAll(getRandomExercisesOfParent(CHEST, 2));
                    exercises.addAll(getRandomExercisesOfParent(SHOULDERS, 2));
                    exercises.addAll(getRandomExercisesOfParent(TRICEPS, 1));
                    break;
                case 3: // Cardio
                    exercises.addAll(getRandomExercisesOfParent(CARDIO, exercisesPerDay));
                    break;
                case 4: // Lower (light)
                    exercises.addAll(getRandomExercisesOfParent(QUADS, 2));
                    exercises.addAll(getRandomExercisesOfParent(GLUTES, 3));
                    break;
            }

            adjustExerciseCount(exercises, exercisesPerDay);
            addExercisesToSession(session, exercises);
        }
    }

    // UTILITY METHODS
    private Map<Integer, List<ExerciseEntity>> groupExercisesByParent() {
        List<ExerciseEntity> exercises = exerciseRepository.getAllExercises();
        List<MuscleGroupEntity> allMuscles = muscleGroupRepository.getListMuscleGroup();
        Map<Integer, List<ExerciseEntity>> groupedByParent = new HashMap<>();
        Map<Integer, Integer> childToParentMap = new HashMap<>();

        for (MuscleGroupEntity mg : allMuscles) {
            if (mg.getParentId() != null) {
                childToParentMap.put(mg.getMuscleGroupId(), mg.getParentId());
            }
        }

        for (ExerciseEntity ex : exercises) {
            int childId = ex.getMuscleGroupId();
            Integer parentId = childToParentMap.get(childId);
            int key = (parentId != null) ? parentId : childId;

            groupedByParent.computeIfAbsent(key, k -> new ArrayList<>()).add(ex);
        }

        return groupedByParent;
    }

    private List<ExerciseEntity> getRandomExercisesOfParent(int parentId, int count) {
        Map<Integer, List<ExerciseEntity>> grouped = groupExercisesByParent();
        List<ExerciseEntity> list = grouped.getOrDefault(parentId, new ArrayList<>());

        if (list.isEmpty()) {
            Log.w("GeneratePlan", "No exercises found for muscle group: " + parentId);
            return new ArrayList<>();
        }

        if (list.size() <= count) {
            Log.d("GeneratePlan", "Returning all " + list.size() + " exercises for muscle group: " + parentId);
            return new ArrayList<>(list);
        }

        Collections.shuffle(list);
        List<ExerciseEntity> result = new ArrayList<>(list.subList(0, count));
        Log.d("GeneratePlan", "Selected " + result.size() + " exercises from " + list.size() + " available for muscle group: " + parentId);
        return result;
    }

    private void adjustExerciseCount(List<ExerciseEntity> exercises, int targetCount) {
        // Remove duplicates first
        List<ExerciseEntity> uniqueExercises = new ArrayList<>();
        for (ExerciseEntity exercise : exercises) {
            boolean exists = false;
            for (ExerciseEntity unique : uniqueExercises) {
                if (unique.getExerciseId() == exercise.getExerciseId()) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                uniqueExercises.add(exercise);
            }
        }
        exercises.clear();
        exercises.addAll(uniqueExercises);

        // Add more exercises if needed
        while (exercises.size() < targetCount) {
            List<Integer> muscleGroups = Arrays.asList(CHEST, SHOULDERS, BACK, BICEPS, TRICEPS, QUADS, GLUTES);
            int randomMuscle = muscleGroups.get(new Random().nextInt(muscleGroups.size()));
            List<ExerciseEntity> additional = getRandomExercisesOfParent(randomMuscle, 1);
            if (!additional.isEmpty()) {
                ExerciseEntity newExercise = additional.get(0);
                boolean exists = false;
                for (ExerciseEntity existing : exercises) {
                    if (existing.getExerciseId() == newExercise.getExerciseId()) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    exercises.add(newExercise);
                }
            }
        }

        // Remove excess exercises if needed
        while (exercises.size() > targetCount) {
            exercises.remove(exercises.size() - 1);
        }

        Log.d("GeneratePlan", "Adjusted exercise count to: " + exercises.size() + " (target: " + targetCount + ")");
    }

    private void addExercisesToSession(WorkoutSessionEntity session, List<ExerciseEntity> exercises) {
        Log.d("GeneratePlan", "Adding " + exercises.size() + " exercises to session: " + session.getDate());

        for (int i = 0; i < exercises.size(); i++) {
            ExerciseEntity exercise = exercises.get(i);
            SessionExerciseEntity sessionExercise = new SessionExerciseEntity(
                    session.getSessionId(),
                    exercise.getExerciseId(),
                    i + 1, // order
                    "60s", // rest time
                    getSetsForBMI(), // sets based on BMI
                    getRepsForBMI(), // reps based on BMI
                    false, // completed
                    false // skipped
            );
            sessionExerciseRepository.insertSessionExercise(sessionExercise);
        }
    }

    private int getSetsForBMI() {
        BMICategory category = getBMICategory();
        switch (category) {
            case UNDERWEIGHT:
                return 4; // More sets for muscle building
            case NORMAL:
                return 3;
            case OVERWEIGHT:
                return 3;
            case OBESE:
                return 2; // Fewer sets, focus on movement
            default:
                return 3;
        }
    }

    private int getRepsForBMI() {
        BMICategory category = getBMICategory();
        switch (category) {
            case UNDERWEIGHT:
                return 8; // Lower reps, higher weight for muscle building
            case NORMAL:
                return 10;
            case OVERWEIGHT:
                return 12; // Higher reps for fat burning
            case OBESE:
                return 10; // Moderate reps
            default:
                return 10;
        }
    }

    private void addSpecialNotes() {
        WorkoutPlanEntity workoutPlan = workoutPlanRepository.getWorkoutPlanById(planId);
        if (workoutPlan == null) {
            Log.w("GeneratePlan", "Workout plan not found for ID: " + planId);
            return;
        }

        String note = "";
        BMICategory category = getBMICategory();

        switch (category) {
            case UNDERWEIGHT:
                note = "Tạ nhẹ - Tập trung tăng cơ, không nên tập quá nặng. Ăn nhiều protein và carb để tăng cân.";
                break;
            case OVERWEIGHT:
                note = "Tăng cường cardio 2-3 lần mỗi tuần. Kết hợp chế độ ăn cân bằng để giảm cân hiệu quả.";
                break;
            case OBESE:
                note = "Tập trung giảm cân, bảo vệ khớp. Ưu tiên máy tập thay vì tạ tự do. Kết hợp chế độ ăn thâm hụt calo và cardio nhẹ nhàng.";
                break;
            default:
                if ("lose_fat".equals(goal)) {
                    note = "Chế độ ăn nên nạp nhiều protein, ít carb và hạn chế đồ dầu mỡ nhé";
                } else if ("lean_tone".equals(goal)) {
                    note = "Kết hợp tập luyện với cardio để có thân hình săn chắc, cân đối";
                }
                break;
        }

        if (!note.isEmpty()) {
            workoutPlan.setNote(note);
            workoutPlanRepository.updateWorkoutPlan(workoutPlan);
            Log.d("GeneratePlan", "Added special note: " + note);
        }
    }

    private enum BMICategory {
        UNDERWEIGHT, NORMAL, OVERWEIGHT, OBESE
    }
}